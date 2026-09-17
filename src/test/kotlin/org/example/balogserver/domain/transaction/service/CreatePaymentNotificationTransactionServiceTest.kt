package org.example.balogserver.domain.transaction.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.example.balogserver.domain.category.domain.Category
import org.example.balogserver.domain.transaction.domain.Transaction
import org.example.balogserver.domain.transaction.domain.repository.TransactionRepository
import org.example.balogserver.domain.transaction.exception.IdempotencyKeyReusedException
import org.example.balogserver.domain.transaction.presentation.dto.CreatePaymentNotificationTransactionRequest
import org.example.balogserver.domain.user.domain.User
import org.example.balogserver.domain.user.domain.repository.UserRepository
import org.example.balogserver.domain.user.facade.UserFacade
import org.example.balogserver.global.ai.CategoryRecommendationClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.context.ApplicationEventPublisher
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class CreatePaymentNotificationTransactionServiceTest {
    @Mock private lateinit var userFacade: UserFacade
    @Mock private lateinit var categoryRecommendationClient: CategoryRecommendationClient
    @Mock private lateinit var userRepository: UserRepository
    @Mock private lateinit var transactionRepository: TransactionRepository
    @Mock private lateinit var applicationEventPublisher: ApplicationEventPublisher
    private lateinit var service: CreatePaymentNotificationTransactionService

    @BeforeEach
    fun setUp() {
        service = CreatePaymentNotificationTransactionService(userFacade, categoryRecommendationClient, userRepository, transactionRepository, applicationEventPublisher, Clock.fixed(Instant.parse("2026-08-12T00:00:00Z"), ZoneId.of("Asia/Seoul")))
    }

    @Test
    fun executeSavesUncategorizedTransactionBeforeClassification() {
        val currentUser = User.builder().nickname("사용자").build()
        val request = notificationRequest()
        val currentUserId = UUID.fromString("11111111-1111-1111-1111-111111111111")
        Mockito.`when`(userFacade.currentUserId).thenReturn(currentUserId)
        Mockito.`when`(userRepository.getReferenceById(currentUserId)).thenReturn(currentUser)
        Mockito.`when`(transactionRepository.findByUser_IdAndIdempotencyKey(currentUserId, requireNotNull(request.idempotencyKey))).thenReturn(Optional.empty())

        val result = service.execute(request)

        val savedTransaction = captureSavedTransaction()
        assertThat(savedTransaction.user).isEqualTo(currentUser)
        assertThat(savedTransaction.category).isEqualTo(Category.UNCATEGORIZED)
        assertThat(savedTransaction.categorySource).isEqualTo(Transaction.CategorySource.NOTIFICATION_PENDING)
        assertThat(savedTransaction.type).isEqualTo(Transaction.TransactionType.EXPENSE)
        assertThat(savedTransaction.amount).isEqualTo(4500L)
        assertThat(savedTransaction.description).isEqualTo("스타벅스 강남점")
        assertThat(savedTransaction.source).isEqualTo(Transaction.TransactionSource.NOTIFICATION)
        assertThat(savedTransaction.transactionDate).isEqualTo(request.occurredAt!!.toLocalDate())
        assertThat(savedTransaction.collectedAt).isEqualTo(request.collectedAt!!.toInstant())
        assertThat(savedTransaction.rawNotificationText).isNull()
        assertThat(result.created).isTrue()
    }

    @Test
    fun executeReturnsExistingTransactionForIdenticalRetry() {
        val request = notificationRequest()
        val currentUserId = UUID.fromString("11111111-1111-1111-1111-111111111111")
        Mockito.`when`(userFacade.currentUserId).thenReturn(currentUserId)
        Mockito.`when`(transactionRepository.findByUser_IdAndIdempotencyKey(currentUserId, requireNotNull(request.idempotencyKey))).thenReturn(Optional.of(notificationTransaction(request)))

        val result = service.execute(request)

        assertThat(result.created).isFalse()
        Mockito.verify(transactionRepository, Mockito.never()).save(Mockito.any())
        Mockito.verifyNoInteractions(applicationEventPublisher)
    }

    @Test
    fun executeRejectsDifferentPayloadForExistingIdempotencyKey() {
        val request = notificationRequest()
        val currentUserId = UUID.fromString("11111111-1111-1111-1111-111111111111")
        Mockito.`when`(userFacade.currentUserId).thenReturn(currentUserId)
        Mockito.`when`(transactionRepository.findByUser_IdAndIdempotencyKey(currentUserId, requireNotNull(request.idempotencyKey))).thenReturn(Optional.of(notificationTransaction(request)))

        assertThatThrownBy { service.execute(request.copy(amount = 5000)) }.isSameAs(IdempotencyKeyReusedException.EXCEPTION)
    }

    private fun captureSavedTransaction(): Transaction {
        val captor = ArgumentCaptor.forClass(Transaction::class.java)
        Mockito.verify(transactionRepository).save(captor.capture())
        return captor.value
    }

    @Test
    fun executeAcceptsLegacyTitleAndAmountRequest() {
        val currentUserId = UUID.fromString("11111111-1111-1111-1111-111111111111")
        Mockito.`when`(userFacade.currentUserId).thenReturn(currentUserId)
        Mockito.`when`(userRepository.getReferenceById(currentUserId)).thenReturn(User.builder().nickname("사용자").build())
        Mockito.`when`(categoryRecommendationClient.recommend("스타벅스 강남점")).thenReturn(Optional.of(Category.CAFE_SNACK))

        val result = service.execute(CreatePaymentNotificationTransactionRequest("스타벅스 강남점", 4500))

        assertThat(result.created).isTrue()
        assertThat(captureSavedTransaction().category).isEqualTo(Category.CAFE_SNACK)
    }

    private fun notificationRequest() = CreatePaymentNotificationTransactionRequest(
        "스타벅스 강남점",
        4500,
        "android:com.card:42:1700000000000",
        "android-notification-parser/2.1.0",
        "com.card:42:1700000000000",
        OffsetDateTime.parse("2026-08-12T14:30:00+09:00"),
        OffsetDateTime.parse("2026-08-12T14:29:00+09:00"),
    )

    private fun notificationTransaction(request: CreatePaymentNotificationTransactionRequest) = Transaction.builder()
        .user(User.builder().nickname("사용자").build())
        .category(Category.UNCATEGORIZED)
        .categorySource(Transaction.CategorySource.NOTIFICATION_PENDING)
        .type(Transaction.TransactionType.EXPENSE)
        .amount(request.amount)
        .description(request.title)
        .source(Transaction.TransactionSource.NOTIFICATION)
        .idempotencyKey(requireNotNull(request.idempotencyKey))
        .parserVersion(request.parserVersion)
        .sourceNotificationId(request.sourceNotificationId)
        .collectedAt(request.collectedAt!!.toInstant())
        .transactionDate(request.occurredAt!!.toLocalDate())
        .build()
}
