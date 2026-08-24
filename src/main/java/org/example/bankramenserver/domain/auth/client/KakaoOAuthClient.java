package org.example.bankramenserver.domain.auth.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bankramenserver.domain.auth.dto.response.KakaoUserResponse;
import org.example.bankramenserver.domain.auth.exception.KaKaoUserInfoRequestFailedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoOAuthClient {

    private final RestTemplate restTemplate;

    @Value("${kakao.api-url}")
    private String apiUrl;

    public KakaoUserResponse requestUserInfo(String kakaoAccessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(kakaoAccessToken);

        try {
            KakaoUserResponse response = restTemplate.exchange(
                    apiUrl + "/v2/user/me",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    KakaoUserResponse.class
            ).getBody();

            if (response == null || response.id() == null) {
                throw KaKaoUserInfoRequestFailedException.EXCEPTION;
            }

            return response;
        } catch (Exception e) {
            log.error("카카오 사용자 정보 요청 실패", e);
            throw KaKaoUserInfoRequestFailedException.EXCEPTION;
        }
    }
}