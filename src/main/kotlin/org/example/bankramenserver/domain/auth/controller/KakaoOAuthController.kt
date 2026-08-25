package org.example.bankramenserver.domain.auth.controller
import jakarta.validation.Valid
import org.example.bankramenserver.domain.auth.dto.TokenRequest
import org.example.bankramenserver.domain.auth.dto.response.AuthTokenResponse
import org.example.bankramenserver.domain.auth.dto.response.LoginUrlResponse
import org.example.bankramenserver.domain.auth.dto.response.MessageResponse
import org.example.bankramenserver.domain.auth.exception.InvalidStateException
import org.example.bankramenserver.domain.auth.service.KakaoOAuthService
import org.example.bankramenserver.domain.auth.service.StateService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
@RestController @RequestMapping("/auth/kakao") class KakaoOAuthController(private val kakaoOAuthService: KakaoOAuthService, private val stateService: StateService) { @GetMapping("/login") fun login(): ResponseEntity<LoginUrlResponse> = ResponseEntity.ok(LoginUrlResponse(kakaoOAuthService.getLoginUrl(stateService.generateState()))); @GetMapping("/callback") fun callback(@RequestParam code: String, @RequestParam state: String): ResponseEntity<AuthTokenResponse> { if (!stateService.validateState(state)) throw InvalidStateException.EXCEPTION; return ResponseEntity.ok(kakaoOAuthService.kakaoLogin(code)) }; @PostMapping("/reissue") fun reissue(@Valid @RequestBody request: TokenRequest) = ResponseEntity.ok(kakaoOAuthService.reissue(request.refreshToken)); @PostMapping("/logout") fun logout(@Valid @RequestBody request: TokenRequest) = ResponseEntity.ok(MessageResponse("로그아웃이 완료되었습니다.").also { kakaoOAuthService.logout(request.refreshToken) }) }
