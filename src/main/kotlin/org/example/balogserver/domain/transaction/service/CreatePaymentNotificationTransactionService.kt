package org.example.balogserver.domain.transaction.service

import org.example.balogserver.domain.category.domain.Category
import org.example.balogserver.domain.transaction.domain.Transaction
import org.example.balogserver.domain.transaction.domain.repository.TransactionRepository
import org.example.balogserver.domain.transaction.exception.IdempotencyKeyReusedException
import org.example.balogserver.domain.transaction.event.PaymentTransactionRecordedEvent
import org.example.balogserver.domain.transaction.presentation.dto.CreatePaymentNotificationTransactionRequest
import org.example.balogserver.domain.transaction.presentation.dto.PaymentNotificationTransactionResponse
import org.example.balogserver.domain.user.domain.repository.UserRepository
import org.example.balogserver.domain.user.facade.UserFacade
import org.example.balogserver.global.ai.CategoryRecommendationClient
import org.example.balogserver.global.error.exception.ErrorCode
import org.example.balogserver.global.error.exception.GlobalException
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
    fun execute(request: CreatePaymentNotificationTransactionRequest): PaymentNotificationTransactionResponse {
        val currentUserId = userFacade.currentUserId
        if (request.hasPartialCollectionMetadata()) throw GlobalException(ErrorCode.INVALID_REQUEST)
        if (!request.hasCollectionMetadata()) return createLegacyTransaction(currentUserId, request)
        val idempotencyKey = requireNotNull(request.idempotencyKey)
        val existing = transactionRepository.findByUser_IdAndIdempotencyKey(currentUserId, idempotencyKey).orElse(null)
        if (existing != null) {
            if (!existing.matches(request)) throw IdempotencyKeyReusedException.EXCEPTION
            return PaymentNotificationTransactionResponse(existing.id, false)
        }
        val transaction = Transaction.builder()
            .user(userRepository.getReferenceById(currentUserId))
            .category(Category.UNCATEGORIZED)
            .categorySource(Transaction.CategorySource.NOTIFICATION_PENDING)
            .type(Transaction.TransactionType.EXPENSE)
            .amount(request.amount)
            .description(request.title)
            .source(Transaction.TransactionSource.NOTIFICATION)
            .idempotencyKey(idempotencyKey)
            .parserVersion(request.parserVersion)
            .sourceNotificationId(request.sourceNotificationId)
            .collectedAt(requireNotNull(request.collectedAt).toInstant())
            .transactionDate((request.occurredAt ?: requireNotNull(request.collectedAt)).toLocalDate())
            .build()
        transactionRepository.save(transaction)
        applicationEventPublisher.publishEvent(PaymentTransactionRecordedEvent(currentUserId, transaction.id, UUID.randomUUID(), transaction.description, transaction.amount, transaction.category, transaction.transactionDate))
        return PaymentNotificationTransactionResponse(transaction.id, true)
    }

    private fun createLegacyTransaction(currentUserId: UUID, request: CreatePaymentNotificationTransactionRequest): PaymentNotificationTransactionResponse {
        val transaction = Transaction.builder()
            .user(userRepository.getReferenceById(currentUserId))
            .category(categoryRecommendationClient.recommend(request.title).orElse(Category.UNCATEGORIZED))
            .categorySource(Transaction.CategorySource.AI)
            .type(Transaction.TransactionType.EXPENSE)
            .amount(request.amount)
            .description(request.title)
            .source(Transaction.TransactionSource.NOTIFICATION)
            .transactionDate(LocalDate.now(clock))
            .build()
        transactionRepository.save(transaction)
        applicationEventPublisher.publishEvent(PaymentTransactionRecordedEvent(currentUserId, transaction.id, UUID.randomUUID(), transaction.description, transaction.amount, transaction.category, transaction.transactionDate))
        return PaymentNotificationTransactionResponse(transaction.id, true)
    }

    private fun Transaction.matches(request: CreatePaymentNotificationTransactionRequest) =
        description == request.title && amount == request.amount && parserVersion == request.parserVersion && sourceNotificationId == request.sourceNotificationId && collectedAt?.equals(requireNotNull(request.collectedAt).toInstant()) == true && transactionDate.equals((request.occurredAt ?: requireNotNull(request.collectedAt)).toLocalDate())
}
