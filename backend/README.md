#  Backend

## π¨κΈ°μ  μ€ν 
| Tech | Version | 
|--|--|
| Java | 1.8 |
|SpringBoot|2.7.1
| SpringSecurity | 2.7.1 |
| Gradle | 7.4.1 |
| Hibernate | 5.6.9 | 
| Docker | 22.06.0 | 
| AWS EC2 Ubuntu | 20.04 LTS | 
| AWS S3 |  | 



| Tech | DockerImage |
|--|--| 
| OpenVidu | 2.22.0 | 
| Kurento Media Server| 6.18.0 | 
| MySQL | 8.0.28 | 
| Redis | alpine | 
| Nginx | stable-alpine |
| Jenkins | LTS | 

## π§κ°λ°νκ²½ κ΅¬μ±

- IntelliJ IDEA 2022.1.3
- JDK 1.8
- Windows 10
- Junit 4.13.2
- Postman
- GitLab
- Jira


## πνλ‘μ νΈ κ΅¬μ‘°

```
βββ/main
	βββ java
	β	βββ com
	β		βββ ssafy
	β			βββ backend
	β				βββ config  /* νκ²½μ€μ  νμΌ */
	β				β 	βββ properties
	β				|	β	βββ	AppProperties.java
	β				|	βββ AsyncConfig.java
	β				|	βββ AwsConfig.java
	β				|	βββ JwtSecurityConfig.java
	β				|	βββ	RedisConfig.java
	β				|	βββ SecurityConfig.java
	β				|	βββ WebClientConfig.java
	β				|	βββ WebConfig.java
	β				βββ controller
	β				|	βββ AuthController.java
	β				|	βββ BoardCommentController.java
	β				|	βββ BoardController.java
	β				|	βββ EditorController.java
	β				|	βββ LolController.java
	β				|	βββ RoomCommentController.java
	β				|	βββ RoomController.java
	β				|	βββ SessionController.java
	β				|	βββ UserController.java
	β				|	βββ VoteController.java
	β				βββ filter
	β				|	βββ JwtAuthFilter.java
	β				βββ handler
	β				|	βββ security
	|				|			βββ AuthenticationEntryPointHandler.java
	|				|			βββ OAuth2AuthenticationFailureHandler.java
	|				|			βββ OAuth2AuthenticationSuccessHandler.java
	|				|			βββ TokenAccessDeniedHandler.java
	|				|			βββ WebAccessDeniedHandler.java
	β				βββ info /* μμ λ‘κ·ΈμΈ μ μ  μ λ³΄ */
	β				β	βββ impl
	β				β	β	βββ GoogleOAuth2UserInfo.java
	β				β	β	βββ KakaoOAuth2UserInfo.java
	β				β	β	βββ NaverOAuth2UserInfo.java
	β				β	βββ OAuth2UserInfo.java
	β				β	βββ OAuth2UserInfoFactory.java
	β				βββ model
	β				β	βββ compositekey /* λ³΅ν©ν€ */
	β				β	β	βββ VoteRecordId.java
	β				β	βββ dto
	β				β	β	βββ lol /* λ¦¬κ·Έμ€λΈ λ μ λ κ΄λ ¨ dto*/ 
	β				β	β	β	βββ MatchDto.java
	β				β	β	β	βββ ParticipantDto.java
	β				β	β	βββ BoardCommentDto.java
	β				β	β	βββ BoardDto.java
	β				β	β	βββ LolInfoDto.java
	β				β	β	βββ PasswordDto.java
	β				β	β	βββ RoomCommentDto.java
	β				β	β	βββ RoomDto.java
	β				β	β	βββ SessionEventDto.java
	β				β	β	βββ UserDto.java
	β				β	β	βββ VoteDto.java
	β				β	βββ entity
	β				β	β	βββ Board.java
	β				β	β	βββ BoardComment.java
	β				β	β	βββ LolAuth.java
	β				β	β	βββ Match.java
	β				β	β	βββ ProviderType.java
	β				β	β	βββ Room.java
	β				β	β	βββ RoomComment.java
	β				β	β	βββ User.java
	β				β	β	βββ UserAuthToken.java
	β				β	β	βββ UserPrincipal.java
	β				β	β	βββ UserRole.java
	β				β	β	βββ Video.java
	β				β	β	βββ Vote.java
	β				β	β	βββ VoteRecord.java
	β				β	βββ exception /* μ»€μ€ν μμΈ */
	β				β	β	βββ BoardNotFoundException.java
	β				β	β	βββ CommentNotFoundException.java
	β				β	β	βββ DuplicatedEnterSession.java
	β				β	β	βββ DuplicatedTokenException.java
	β				β	β	βββ ExpiredEmailAuthKeyException.java
	β				β	β	βββ FileTypeException.java
	β				β	β	βββ InputDataNullException.java
	β				β	β	βββ InvalidSessionCreate.java
	β				β	β	βββ PasswordNotMatchException.java
	β				β	β	βββ RoomNotFoundException.java
	β				β	β	βββ SessionIsNotValid.java
	β				β	β	βββ SessionNotFoundException.java
	β				β	β	βββ SessionTokenNotValid.java
	β				β	β	βββ SocialUserException.java
	β				β	β	βββ TokenValidFailedException.java
	β				β	β	βββ UserNotFoundException.java
	β				β	β	βββ VoteNotFoundException.java
	β				β	βββ mapper
	β				β	β	βββ EntityMapper.java
	β				β	β	βββ MatchMapper.java
	β				β	β	βββ RoomMapper.java
	β				β	β	βββ UserMapper.java
	β				β	βββ repository
	β				β	β	βββ BoardCommentRepository.java
	β				β	β	βββ BoardRepository.java
	β				β	β	βββ LolAuthRepository.java
	β				β	β	βββ MatchRepository.java
	β				β	β	βββ OAuth2AuthorizationRequestBasedOnCookieRepository.java
	β				β	β	βββ RoomCommentRepository.java
	β				β	β	βββ RoomRepository.java
	β				β	β	βββ UserAuthTokenRepository.java
	β				β	β	βββ UserRepository.java
	β				β	β	βββ VideoRepository.java
	β				β	β	βββ VoteRecordRepository.java
	β				β	β	βββ VoteRepository.java
	β				β	βββ response
	β				β		βββ BaseResponseBody.java
	β				β		βββ BoardResponse.java
	β				β		βββ EditorResponse.java
	β				β		βββ LoginResponse.java
	β				β		βββ MatchResponse.java
	β				β		βββ MessageResponse.java
	β				β		βββ RecordingResponse.java
	β				β		βββ RoomResponse.java
	β				β		βββ SessionEnterResponse.java
	β				β		βββ UserInfoResponse.java
	β				β		βββ VideoResponse.java
	β				β		
	β				βββ security
	β				β	βββ JwtProvider.java
	β				β	βββ MyUserDetailService.java
	β				βββ service
	β				β	βββ AuthService.java
	β				β	βββ AuthServiceImpl.java
	β				β	βββ BoardCommentService.java
	β				β	βββ BoardCommentServiceImpl.java
	β				β	βββ BoardService.java
	β				β	βββ BoardServiceImpl.java
	β				β	βββ CustomOAuth2UserService.java
	β				β	βββ LOLService.java
	β				β	βββ LOLServiceImpl.java
	β				β	βββ MailService.java
	β				β	βββ MailServiceImpl.java
	β				β	βββ RoomCommentService.java
	β				β	βββ RoomCommentServiceImpl.java
	β				β	βββ RoomService.java
	β				β	βββ RoomServiceImpl.java
	β				β	βββ SessionService.java
	β				β	βββ SessionServiceImpl.java
	β				β	βββ UserService.java
	β				β	βββ UserServiceImpl.java
	β				β	βββ VoteService.java
	β				β	βββ VoteServiceImpl.java
	β				βββ util
	β				β	βββ AwsService.java
	β				β	βββ BaseControllerAdvice.java
	β				β	βββ CookieUtil.java
	β				β	βββ DeleteUserService.java
	β				β	βββ LolTier.java
	β				β	βββ RedisService.java
	β				β	βββ SetCookie.java
	β				β	βββ SnsType.java
	β				βββ BackendApplication.java
	βββ resources
		βββ application.properties
		βββ application-prod.properties
		
```

## ERD

![erd](https://user-images.githubusercontent.com/57943574/190897297-2d25581b-e082-4489-ad7f-8f3d5da1409e.jpg)



## API Docs

[API Docs](https://documenter.getpostman.com/view/17324798/UzXKUxjY)

