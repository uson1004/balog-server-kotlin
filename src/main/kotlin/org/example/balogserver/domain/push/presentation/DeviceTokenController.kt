package org.example.balogserver.domain.push.presentation

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.example.balogserver.domain.push.presentation.dto.DeviceTokenRequest
import org.example.balogserver.domain.push.service.DeviceTokenService
import org.example.balogserver.domain.user.facade.UserFacade
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/device-tokens")
@Tag(name = "디바이스토큰 API", description = "디바이스 토큰 관리 API")
class DeviceTokenController(
    private val deviceTokenService: DeviceTokenService,
    private val userFacade: UserFacade,
) {
    @Operation(summary = "디바이스 토큰 저장")
    @PostMapping
    fun saveDeviceToken(@Valid @RequestBody request: DeviceTokenRequest): ResponseEntity<Void> {
        deviceTokenService.save(userFacade.currentUserId, request.token)
        return ResponseEntity.ok().build()
    }
}
