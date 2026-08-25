package org.example.bankramenserver.domain.auth.client
import org.example.bankramenserver.domain.auth.dto.response.KakaoUserResponse
import org.example.bankramenserver.domain.auth.exception.KaKaoUserInfoRequestFailedException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
@Component class KakaoOAuthClient(private val restTemplate: RestTemplate, @Value("\${kakao.api-url}") private val apiUrl: String) { private val log = LoggerFactory.getLogger(javaClass); fun requestUserInfo(kakaoAccessToken: String): KakaoUserResponse = try { val headers = HttpHeaders().also { it.setBearerAuth(kakaoAccessToken) }; restTemplate.exchange("$apiUrl/v2/user/me", HttpMethod.GET, HttpEntity<Void>(headers), KakaoUserResponse::class.java).body?.takeIf { it.id != null } ?: throw KaKaoUserInfoRequestFailedException.EXCEPTION } catch (e: Exception) { log.error("카카오 사용자 정보 요청 실패", e); throw KaKaoUserInfoRequestFailedException.EXCEPTION } }
