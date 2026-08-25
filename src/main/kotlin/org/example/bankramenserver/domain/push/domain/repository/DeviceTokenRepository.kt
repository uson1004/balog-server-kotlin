package org.example.bankramenserver.domain.push.domain.repository

import org.example.bankramenserver.domain.push.domain.DeviceToken
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface DeviceTokenRepository : JpaRepository<DeviceToken, Long> {
    fun findAllByMemberId(memberId: UUID?): List<DeviceToken>
    fun findByToken(token: String?): Optional<DeviceToken>
    fun deleteByMemberId(memberId: UUID?)
    fun deleteByToken(token: String?)
}
