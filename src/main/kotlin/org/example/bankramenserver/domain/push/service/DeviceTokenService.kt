package org.example.bankramenserver.domain.push.service

import org.example.bankramenserver.domain.push.domain.DeviceToken
import org.example.bankramenserver.domain.push.domain.repository.DeviceTokenRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class DeviceTokenService(private val deviceTokenRepository: DeviceTokenRepository) {
    fun save(memberId: UUID, token: String) {
        deviceTokenRepository.findByToken(token).ifPresentOrElse({ it.updateMember(memberId) }) { insertOrAttach(memberId, token) }
    }

    private fun insertOrAttach(memberId: UUID, token: String) {
        try {
            deviceTokenRepository.saveAndFlush(DeviceToken.builder().memberId(memberId).token(token).build())
        } catch (_: DataIntegrityViolationException) {
            deviceTokenRepository.findByToken(token).ifPresent { it.updateMember(memberId) }
        }
    }

    fun delete(memberId: UUID) = deviceTokenRepository.deleteByMemberId(memberId)
}
