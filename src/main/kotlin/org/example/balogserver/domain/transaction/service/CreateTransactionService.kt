package org.example.balogserver.domain.transaction.service

import org.example.balogserver.domain.transaction.domain.Transaction
import org.example.balogserver.domain.transaction.domain.repository.TransactionRepository
import org.example.balogserver.domain.transaction.presentation.dto.CreateTransactionRequest
import org.example.balogserver.domain.user.facade.UserFacade
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateTransactionService(
    private val userFacade: UserFacade,
    private val transactionRepository: TransactionRepository,
) {
    @Transactional
    fun execute(request: CreateTransactionRequest) {
        transactionRepository.save(Transaction.builder().user(userFacade.currentUser).category(request.category).type(request.type).amount(request.amount).description(request.title).source(Transaction.TransactionSource.MANUAL).transactionDate(request.transactionDate).build())
    }
}
