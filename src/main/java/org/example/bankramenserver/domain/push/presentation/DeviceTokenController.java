package org.example.bankramenserver.domain.push.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.push.presentation.dto.DeviceTokenRequest;
import org.example.bankramenserver.domain.push.service.DeviceTokenService;
import org.example.bankramenserver.domain.user.facade.UserFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/kakao")
@Tag(name = "디바이스토큰 API", description = "디바이스 토큰 관리 API")
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;
    private final UserFacade userFacade;

    @Operation(summary = "디바이스 토큰 저장")
    @PostMapping("/token")
    public ResponseEntity<Void> saveDeviceToken(
            @Valid @RequestBody DeviceTokenRequest request
    ) {

        UUID memberId = userFacade.getCurrentUserId();
        deviceTokenService.save(memberId, request.token());

        return ResponseEntity.ok().build();
    }
}
