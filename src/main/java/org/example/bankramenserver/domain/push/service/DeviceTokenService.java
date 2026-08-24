package org.example.bankramenserver.domain.push.service;

import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.push.domain.DeviceToken;
import org.example.bankramenserver.domain.push.domain.repository.DeviceTokenRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;

    public void save(UUID memberId, String token) {

        deviceTokenRepository.findByToken(token)
                .ifPresentOrElse(
                        existing -> existing.updateMember(memberId),
                        () -> insertOrAttach(memberId, token)
                );
    }

    private void insertOrAttach(UUID memberId, String token) {
        try {
            deviceTokenRepository.saveAndFlush(
                DeviceToken.builder()
                        .memberId(memberId)
                        .token(token)
                        .build()
            );
        } catch (DataIntegrityViolationException e) {
            deviceTokenRepository.findByToken(token)
                    .ifPresent(existing -> existing.updateMember(memberId));
        }

    }

        public void delete(UUID memberId) {
        deviceTokenRepository.deleteByMemberId(memberId);
    }
}