package org.example.balogserver.domain.auth.controller

import org.example.balogserver.domain.auth.dto.response.AuthTokenResponse
import org.example.balogserver.domain.auth.service.LocalAuthService
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Profile("local")
@RequestMapping("/auth/local")
class LocalAuthController(private val localAuthService: LocalAuthService) {
    @GetMapping("/login")
    fun login(): ResponseEntity<AuthTokenResponse> = ResponseEntity.ok(localAuthService.login())
}
