package org.example.bankramenserver.domain.transaction.service

import org.example.bankramenserver.domain.transaction.domain.repository.TransactionRepository
import org.example.bankramenserver.domain.transaction.presentation.dto.RecentTransactionListResponse
import org.example.bankramenserver.domain.transaction.presentation.dto.TransactionHistoryResponse
import org.example.bankramenserver.domain.user.facade.UserFacade
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class GetRecentTransactionListService(private val userFacade: UserFacade, private val transactionRepository: TransactionRepository) {
    @Transactional(readOnly = true)
    fun execute(limit: Int): RecentTransactionListResponse = execute(requireNotNull(userFacade.currentUser.id), limit)

    @Transactional(readOnly = true)
    fun execute(userId: UUID, limit: Int): RecentTransactionListResponse = RecentTransactionListResponse.from(transactionRepository.findRecentTransactionHistories(userId, limit).map(TransactionHistoryResponse::from))
}
