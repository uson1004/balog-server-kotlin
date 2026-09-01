package org.example.balogserver.domain.transaction.service

import org.example.balogserver.domain.transaction.domain.repository.TransactionRepository
import org.example.balogserver.domain.transaction.exception.TransactionNotFoundException
import org.example.balogserver.domain.user.facade.UserFacade
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class DeleteTransactionService(private val userFacade: UserFacade, private val transactionRepository: TransactionRepository) {
    @Transactional
    fun execute(transactionId: UUID) {
        transactionRepository.delete(transactionRepository.findByIdAndUser_Id(transactionId, requireNotNull(userFacade.currentUser.id)).orElseThrow { TransactionNotFoundException.EXCEPTION })
    }
}
