package org.example.bankramenserver.domain.transaction.service

import org.example.bankramenserver.domain.category.domain.Category
import org.example.bankramenserver.domain.transaction.domain.Transaction
import org.example.bankramenserver.domain.transaction.domain.repository.TransactionRepository
import org.example.bankramenserver.domain.transaction.event.PaymentTransactionRecordedEvent
import org.example.bankramenserver.domain.transaction.presentation.dto.CreatePaymentNotificationTransactionRequest
import org.example.bankramenserver.domain.user.domain.repository.UserRepository
import org.example.bankramenserver.domain.user.facade.UserFacade
import org.example.bankramenserver.global.ai.CategoryRecommendationClient
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate
import java.util.UUID

@Service
class CreatePaymentNotificationTransactionService(
    private val userFacade: UserFacade,
    private val categoryRecommendationClient: CategoryRecommendationClient,
    private val userRepository: UserRepository,
    private val transactionRepository: TransactionRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val clock: Clock,
) {
    @Transactional
    fun execute(request: CreatePaymentNotificationTransactionRequest) {
        val currentUserId = userFacade.currentUserId
        val transaction = Transaction.builder().user(userRepository.getReferenceById(currentUserId)).category(categoryRecommendationClient.recommend(request.title).orElse(Category.UNCATEGORIZED)).type(Transaction.TransactionType.EXPENSE).amount(request.amount).description(request.title).source(Transaction.TransactionSource.NOTIFICATION).transactionDate(LocalDate.now(clock)).build()
        transactionRepository.save(transaction)
        applicationEventPublisher.publishEvent(PaymentTransactionRecordedEvent(currentUserId, transaction.id, UUID.randomUUID(), transaction.description, transaction.amount, transaction.category, transaction.transactionDate))
    }
}
