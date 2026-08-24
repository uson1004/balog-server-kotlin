package org.example.bankramenserver.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.auth.dto.TokenRequest;
import org.example.bankramenserver.domain.auth.dto.response.AuthTokenResponse;
import org.example.bankramenserver.domain.auth.dto.response.LoginUrlResponse;
import org.example.bankramenserver.domain.auth.dto.response.MessageResponse;
import org.example.bankramenserver.domain.auth.exception.InvalidStateException;
import org.example.bankramenserver.domain.auth.service.KakaoOAuthService;
import org.example.bankramenserver.domain.auth.service.StateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/kakao")
@RequiredArgsConstructor
@Tag(
        name = "인증 API",
        description = "카카오 OAuth 로그인 및 인증 관리 관련 API"
)
public class KakaoOAuthController {

    private final KakaoOAuthService kakaoOAuthService;
    private final StateService stateService;

    @Operation(
            summary = "카카오 로그인 URL 발급",
            description = "카카오 로그인 페이지로 이동하기 위한 URL을 생성합니다."
    )
    @GetMapping("/login")
    public ResponseEntity<LoginUrlResponse> login() {

        String state = stateService.generateState();

        String loginUrl =
                kakaoOAuthService.getLoginUrl(state);

        return ResponseEntity.ok(
                new LoginUrlResponse(loginUrl)
        );
    }

    @Operation(
            summary = "카카오 로그인 콜백",
            description = "카카오 인증 서버로부터 전달받은 인가 코드로 로그인합니다."
    )
    @GetMapping("/callback")
    public ResponseEntity<AuthTokenResponse> callback(

            @Parameter(description = "카카오 인가 코드")
            @RequestParam String code,

            @Parameter(description = "CSRF 방지 state")
            @RequestParam String state
    ) {

        if (!stateService.validateState(state)) {
            throw InvalidStateException.EXCEPTION;
        }

        return ResponseEntity.ok(
                kakaoOAuthService.kakaoLogin(code)
        );
    }

    @Operation(
            summary = "토큰 재발급",
            description = "Refresh Token으로 Access Token과 Refresh Token을 재발급합니다."
    )
    @PostMapping("/reissue")
    public ResponseEntity<AuthTokenResponse> reissue(
            @Valid @RequestBody TokenRequest request
    ) {

        return ResponseEntity.ok(
                kakaoOAuthService.reissue(
                        request.refreshToken()
                )
        );
    }

    @Operation(
            summary = "로그아웃",
            description = "Refresh Token을 무효화합니다."
    )
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(
            @Valid @RequestBody TokenRequest request
    ) {

        kakaoOAuthService.logout(
                request.refreshToken()
        );

        return ResponseEntity.ok(
                new MessageResponse(
                        "로그아웃이 완료되었습니다."
                )
        );
    }
}