package org.example.balogserver.domain.transaction.service

import org.example.balogserver.domain.transaction.domain.Transaction
import org.example.balogserver.domain.transaction.domain.repository.TransactionRepository
import org.example.balogserver.domain.transaction.event.PaymentTransactionRecordedEvent
import org.example.balogserver.domain.transaction.presentation.dto.CreateTransactionRequest
import org.example.balogserver.domain.user.facade.UserFacade
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class CreateTransactionService(
    private val userFacade: UserFacade,
    private val transactionRepository: TransactionRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    @Transactional
    fun execute(request: CreateTransactionRequest) {
        val currentUserId = userFacade.currentUserId
        val transaction = Transaction.builder().user(userFacade.currentUser).category(request.category).type(request.type).amount(request.amount).description(request.title).source(Transaction.TransactionSource.MANUAL).transactionDate(request.transactionDate).build()
        transactionRepository.save(transaction)
        applicationEventPublisher.publishEvent(PaymentTransactionRecordedEvent(currentUserId, transaction.id, UUID.randomUUID(), transaction.description, transaction.amount, transaction.category, transaction.transactionDate))
    }
}
