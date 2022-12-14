package com.ssafy.backend.service;

import com.ssafy.backend.model.dto.PasswordDto;
import com.ssafy.backend.model.dto.UserDto;
import com.ssafy.backend.model.entity.LolAuth;
import com.ssafy.backend.model.entity.User;
import com.ssafy.backend.model.entity.UserAuthToken;
import com.ssafy.backend.model.entity.UserRole;
import com.ssafy.backend.model.exception.*;
import com.ssafy.backend.model.mapper.UserMapper;
import com.ssafy.backend.model.repository.LolAuthRepository;
import com.ssafy.backend.model.repository.UserAuthTokenRepository;
import com.ssafy.backend.model.repository.UserRepository;
import com.ssafy.backend.util.AwsService;
import com.ssafy.backend.util.RedisService;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    private final UserAuthTokenRepository userAuthTokenRepository;

    private final LolAuthRepository lolAuthRepository;

    private final MailService mailService;

    private final RedisService redisService;

    private final AwsService awsService;

    @Value("${email.expire-day}")
    private int expireDay;

    @Value("${email.auth-key-size}")
    private int authKeySize;

    @Value("${password.reset.expire-time}")
    private int resetTokenExpireTime;

    @Value("${default-profile-image-url}")
    private String defaultProfileImageUrl;

    private final int USER_ID_MIN_LENGTH = 5;
    private final int USER_ID_MAX_LENGTH = 40;
    private final int PASSWORD_MIN_LENGTH = 8;
    private final int PASSWORD_MAX_LENGTH = 16;

    public UserServiceImpl(UserRepository userRepository, UserAuthTokenRepository userAuthTokenRepository, MailService mailService, RedisService redisService, AwsService awsService, LolAuthRepository lolAuthRepository) {
        this.userRepository = userRepository;
        this.userAuthTokenRepository = userAuthTokenRepository;
        this.lolAuthRepository = lolAuthRepository;
        this.mailService = mailService;
        this.redisService = redisService;
        this.awsService = awsService;
    }

    /**
     * ????????????
     * @param user { id, password, nickName }
     */
    @Override
    public boolean registUser(UserDto user, MultipartFile profileImage) throws MessagingException {
        // ????????? ??????
        if((user.getId().length() < USER_ID_MIN_LENGTH
                || user.getId().length() > USER_ID_MAX_LENGTH
                || user.getPassword().length() < PASSWORD_MIN_LENGTH
                || user.getPassword().length() > PASSWORD_MAX_LENGTH
                || user.getId().contains("=")
                || user.getNickname().contains("deleteuser")
        )){
            return false;
        }
        String encrypt = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()); // 10?????????
        // ?????? db ??????
        if(!profileImage.isEmpty()){
            userRepository.save(new User.Builder(user.getId(), user.getNickname(), encrypt, awsService.imageUpload(profileImage)).build());
        }else{
            userRepository.save(new User.Builder(user.getId(), user.getNickname(), encrypt).build());
        }
        // ????????? ????????? ??????
        String authKey = "";
        UserAuthToken tokenResult;
        do {
            authKey = RandomStringUtils.randomAlphanumeric(authKeySize);
            tokenResult = userAuthTokenRepository.findByToken(authKey).orElse(null);
        } while (tokenResult != null);

        try {
            userAuthTokenRepository.save(new UserAuthToken.Builder(user.getId(), authKey, LocalDateTime.now(),
                    LocalDateTime.now().plusDays(expireDay)).build());
        } catch (DataIntegrityViolationException e){
            throw new DataIntegrityViolationException("???????????? ??? ????????? ??????");
        }

        // ?????? ?????? ??????
        LOGGER.info("send mail start");
        mailService.sendAuthMail(user.getId(), authKey);
        return true;
    }

    @Override
    public boolean modifyUser(String userId, UserDto changeUser) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("User Not Found")
        );

        // ????????? deleteUser ???????????? ??????
        if(changeUser.getNickname().contains("deleteUser")){
            return false;
        }
        // ?????? ????????? ??????
        if(user.getSnsType() != 0){
            user.updateInfo(changeUser.getNickname());
            userRepository.save(user);
            return true;
        }
        // ???????????? ??????
        boolean isValidate = BCrypt.checkpw(changeUser.getPassword(), user.getPassword());
        if(isValidate) {
            user.updateInfo(changeUser.getNickname());
            userRepository.save(user);
        } else {
            throw new PasswordNotMatchException("Password is Not Match");
        }
        return true;
    }

    /**
     * ?????? ????????? ?????? ??????
     * @param userId ?????? ?????????
     */
    @Override
    public boolean checkDuplicatedID(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        return user != null;
    }

    /**
     * ?????? ????????? ?????? ??????
     * @param nickName ?????????
     */
    @Override
    public boolean checkDuplicatedNickName(String nickName) {
        User user = userRepository.findByNickname(nickName).orElse(null);
        return user != null;
    }

    /**
     * ?????? ??????
     * user ???????????? ??????
     * @param reqPassword ?????? ????????????
     */
    @Override
    public void deleteUser(String userId, String reqPassword) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("User Not Found")
        );

        String password = user.getPassword();
        int snsType = user.getSnsType();
        LolAuth lolAuth = lolAuthRepository.findByUserId(user.getId()).orElse(null);
        if(lolAuth != null){
            lolAuthRepository.deleteById(lolAuth.getUserId());
        }
        user.deleteUser();
        if(snsType != 0){
            userRepository.save(user);
            return;
        }
        boolean isValidate = BCrypt.checkpw(reqPassword, password);
        if(isValidate) {
            userRepository.save(user);
        } else {
            throw new PasswordNotMatchException("Password is Not Match");
        }
    }

    /**
     * ???????????? ?????? ?????? ????????? ????????????, ???????????? ?????? ????????? ????????? ?????????.
     * @param id ????????? ????????? ?????????
     * @return
     */
    @Override
    public void findPassword(String id) throws MessagingException {
        LOGGER.info("Find Password By Sending a Email");

        // ???????????? ???????????? ????????????,
        User user = userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User Not Found By Id")
        );

        // ?????? ????????? ???????????? ??????.
        if(user.getSnsType() != 0){
            throw new SocialUserException("Social User Can't Find Password!");
        }

        String tokenResult = "";
        String resetToken = "";

        // ???????????? ????????? ????????? ????????????. (Redis??? ????????????.)
        do {
            resetToken = RandomStringUtils.randomAlphanumeric(authKeySize);
            tokenResult = redisService.getStringValue(resetToken);
        } while(tokenResult != null);

        // ?????? ?????? ?????????
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + resetTokenExpireTime);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS Z")
                .withZone(ZoneId.systemDefault());

        String expireDate = format.format(expireTime.toInstant());
        String value = id + "=" + expireDate;
        redisService.setStringValueAndExpire(resetToken, value, resetTokenExpireTime);
        mailService.sendPasswordResetMail(id, resetToken);
    }

    /**
     * ???????????? ??? ?????? ??????
     * @param authKey ????????????
     */
    @Override
    public void authUser(String authKey) {
        // JPA ???????????? ??? ????????????
        UserAuthToken userAuthToken = userAuthTokenRepository.findByToken(authKey).orElseThrow(
                () -> new UserNotFoundException("User Auth Token Not Found")
        );

        if(LocalDateTime.now().isAfter(userAuthToken.getExpireDate())){
            throw new ExpiredEmailAuthKeyException("????????? ?????? ????????????.");
        }
        User user = userRepository.findById(userAuthToken.getUserId()).orElseThrow(
                () -> new UserNotFoundException("User Not Found")
        );
        user.updateUserRole(UserRole.ROLE_USER);
        userRepository.save(user);
        userAuthTokenRepository.delete(userAuthToken);
    }

    /**
     * ????????? ??? ????????????, ????????? ?????????
     * @param userId ???????????????
     */
    @Override
    public void resendAuthMail(String userId) throws MessagingException {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("User Not Found")
        );

        if(user.getSnsType() != 0){
            throw new SocialUserException("Social User Can't Send ReAuth Mail");
        }

        String authKey = "";
        UserAuthToken tokenResult;
        // ?????? ?????? ??? ?????? ??? ?????? ??????
        do {
            authKey = RandomStringUtils.randomAlphanumeric(authKeySize);
            tokenResult = userAuthTokenRepository.findByToken(authKey).orElse(null);
        } while (tokenResult != null);

        try {
            userAuthTokenRepository.save(new UserAuthToken.Builder(userId, authKey, LocalDateTime.now(),
                    LocalDateTime.now().plusDays(expireDay)).build());
        } catch (IllegalArgumentException e){
            throw new DuplicatedTokenException("?????? ?????? ??????!");
        }

        // ?????? ?????? ??????
        LOGGER.info("send mail start");
        mailService.sendAuthMail(userId, authKey);
    }

    /**
     * ???????????? ??????
     * @param id ???????????????
     * @param passwords ???????????? Dto
     */
    public int modifyPassword(String id, PasswordDto passwords)  {
        User user = userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User Not Found")
        );

        if(user.getSnsType() != 0){
            throw new SocialUserException("Social User Can't Modify Password");
        }

        String oldPassword = passwords.getOldPassword();
        String newPassword = passwords.getNewPassword();

        // ?????? ??????????????? ?????? ?????? ?????? false
        if(!BCrypt.checkpw(oldPassword, user.getPassword()))
            return -1;
        else {
            // ????????? ??????????????? ??????
            String encrypt = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            return userRepository.updatePassword(encrypt, user.getId());
        }
    }

    /**
     * User entity??? ????????? UserDto??? ?????? ???, ???????????? ??????
     * ?????? ????????? ????????? ?????? ?????????, ???????????? ???????????? UserDto ??????
     */
    @Override
    public UserDto setUserInfo(String userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("User Not Found")
        );

        if(user.getSnsType() != 0){
            return new UserDto(user.getNickname(), user.getCreateDate().toString(),
                    user.getModifyDate().toString(), user.getProfileImg(),
                    user.getSnsType(), user.getRole().toString());
        }
        return UserMapper.mapper.toDto(user);
    }

    /**
     * ?????? ????????? ?????? ???, ???????????? ??????
     */
    @Override
    public String validateResetToken(String resetToken) {
        LOGGER.info("Validate a Reset Token");
        String result = redisService.getStringValue(resetToken);
        if (result==null) {
            return null;
        }

        String[] splitStr = result.split("=");
        if (splitStr.length != 2) {
            return null;
        }
        return splitStr[1];
    }

    /**
     * ???????????? ?????????
     * @param password
     * @param token
     * @return
     */
    @Override
    public boolean resetPassword(String password, String token) {
        LOGGER.info("Reset Password");
        // ?????? ????????? ???????????? ????????? ???????????? ?????? ?????? ??????.
        String result = redisService.getStringValue(token);
        if (result == null || password == null) {
            return false;
        }
        // Redis?????? ??????
        redisService.deleteKey(token);

        String[] splitStr = result.split("=");
        if (splitStr.length != 2) {
            return false;
        }

        String userId = splitStr[0];
        String encrypt = BCrypt.hashpw(password, BCrypt.gensalt());
        return userRepository.updatePassword(encrypt, userId) == 1;
    }


    /**
     * ????????? ????????? ?????? ??? ??????
     * @param userId
     * @param file
     */
    @Override
    public void registProfileImage(String userId, MultipartFile file) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("USER NOT FOUND"));
        String path = awsService.imageUpload(file);
//        deleteOldFile(user);
        userRepository.updateProfileImage(user.getIdx(), path);
    }

    /**
     * ????????? ????????? ??????
     * @param userId
     */
    @Override
    public void deleteProfileImage(String userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("USER NOT FOUND"));
//        deleteOldFile(user);
        userRepository.updateProfileImage(user.getIdx(), defaultProfileImageUrl);
    }

    /**
     * ?????? ????????? ????????? S3 ???????????? ??????
     * @param user
     */
    @Async("awsExecutor")
    void deleteOldFile(User user){
        String oldPath = user.getProfileImg();
        if(!oldPath.equals(defaultProfileImageUrl)){
            String oldFileName = oldPath.substring(oldPath.lastIndexOf('/') + 1);
            awsService.fileDelete(oldFileName);
        }
    }

    @Override
    public Map<String, String> getMyTier(String userId) {
        Map<String, String> result = new HashMap<>();
        result.put("tier", "");
        result.put("summonerName", "");
        LolAuth lolAuth = lolAuthRepository.findByUserId(userId).orElse(null);
        if(lolAuth != null){
            result.put("tier", String.valueOf(lolAuth.getTier()));
            result.put("summonerName", lolAuth.getSummonerName());
        }
        return result;
    }

}
