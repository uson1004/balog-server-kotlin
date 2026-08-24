//package org.example.bankramenserver.domain.auth.service;
//
//import lombok.RequiredArgsConstructor;
//import org.example.bankramenserver.domain.auth.client.KakaoOAuthClient;
//import org.example.bankramenserver.domain.auth.dto.response.AuthTokenResponse;
//import org.example.bankramenserver.domain.auth.dto.response.KakaoUserResponse;
//import org.example.bankramenserver.domain.user.domain.User;
//import org.example.bankramenserver.domain.user.service.UserService;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class KakaoOAuthUseCase {
//
//    private final KakaoOAuthClient kakaoOAuthClient;
//    private final UserService userService;
//    private final JwtService jwtService;
//    private final RefreshTokenService refreshTokenService;
//
//    public AuthTokenResponse login(String kakaoAccessToken) {
//        KakaoUserResponse kakaoUser = kakaoOAuthClient.requestUserInfo(kakaoAccessToken);
//        User user = userService.saveOrUpdate(kakaoUser);
//
//        String accessToken = jwtService.generateAccessToken(user.getId());
//        String refreshToken = jwtService.generateRefreshToken(user.getId());
//
//        refreshTokenService.save(refreshToken, user.getId());
//
//        return new AuthTokenResponse(accessToken, refreshToken);
//    }
//
//    public AuthTokenResponse reissue(String refreshToken) {
//        UUID userId = jwtService.validateRefreshToken(refreshToken);
//
//        refreshTokenService.validate(refreshToken, userId);
//        refreshTokenService.delete(refreshToken);
//
//        String newAccessToken = jwtService.generateAccessToken(userId);
//        String newRefreshToken = jwtService.generateRefreshToken(userId);
//
//        refreshTokenService.save(newRefreshToken, userId);
//
//        return new AuthTokenResponse(newAccessToken, newRefreshToken);
//    }
//
//    public void logout(String refreshToken) {
//        UUID userId = jwtService.validateRefreshToken(refreshToken);
//        refreshTokenService.validate(refreshToken, userId);
//        refreshTokenService.delete(refreshToken);
//    }
//}