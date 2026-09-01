package org.example.balogserver.domain.transaction.service

import org.example.balogserver.domain.transaction.domain.repository.TransactionRepository
import org.example.balogserver.domain.transaction.exception.TransactionNotFoundException
import org.example.balogserver.domain.transaction.presentation.dto.TransactionHistoryResponse
import org.example.balogserver.domain.transaction.presentation.dto.UpdateTransactionCategoryRequest
import org.example.balogserver.domain.user.facade.UserFacade
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UpdateTransactionCategoryService(private val userFacade: UserFacade, private val transactionRepository: TransactionRepository) {
    @Transactional
    fun execute(transactionId: UUID, request: UpdateTransactionCategoryRequest): TransactionHistoryResponse {
        val transaction = transactionRepository.findByIdAndUser_Id(transactionId, requireNotNull(userFacade.currentUser.id)).orElseThrow { TransactionNotFoundException.EXCEPTION }
        transaction.updateCategory(request.category)
        return TransactionHistoryResponse.from(transaction)
    }
}
