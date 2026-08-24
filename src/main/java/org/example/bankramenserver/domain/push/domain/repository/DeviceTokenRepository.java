package org.example.bankramenserver.domain.push.domain.repository;

import org.example.bankramenserver.domain.push.domain.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    List<DeviceToken> findAllByMemberId(UUID memberId);

    Optional<DeviceToken> findByToken(String token);

    void deleteByMemberId(UUID memberId);

    void deleteByToken(String token);
}