package org.example.bankramenserver.domain.transaction.service

import org.assertj.core.api.Assertions.assertThat
import org.example.bankramenserver.domain.category.domain.Category
import org.example.bankramenserver.domain.transaction.domain.Transaction
import org.example.bankramenserver.domain.transaction.domain.repository.TransactionRepository
import org.example.bankramenserver.domain.transaction.event.PaymentTransactionRecordedEvent
import org.example.bankramenserver.domain.transaction.presentation.dto.CreatePaymentNotificationTransactionRequest
import org.example.bankramenserver.domain.user.domain.User
import org.example.bankramenserver.domain.user.domain.repository.UserRepository
import org.example.bankramenserver.domain.user.facade.UserFacade
import org.example.bankramenserver.global.ai.CategoryRecommendationClient
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
import java.time.LocalDate
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
    fun executeSavesExpenseTransactionWithRecommendedCategory() {
        val currentUser = User.builder().kakaoId("kakao-1").nickname("사용자").build()
        val request = CreatePaymentNotificationTransactionRequest("스타벅스 강남점", 4500)
        val currentUserId = UUID.fromString("11111111-1111-1111-1111-111111111111")
        Mockito.`when`(categoryRecommendationClient.recommend("스타벅스 강남점")).thenReturn(Optional.of(Category.CAFE_SNACK))
        Mockito.`when`(userFacade.currentUserId).thenReturn(currentUserId)
        Mockito.`when`(userRepository.getReferenceById(currentUserId)).thenReturn(currentUser)

        service.execute(request)

        val savedTransaction = captureSavedTransaction()
        val eventCaptor = ArgumentCaptor.forClass(PaymentTransactionRecordedEvent::class.java)
        Mockito.verify(applicationEventPublisher).publishEvent(eventCaptor.capture())
        assertThat(savedTransaction.user).isEqualTo(currentUser)
        assertThat(savedTransaction.category).isEqualTo(Category.CAFE_SNACK)
        assertThat(savedTransaction.type).isEqualTo(Transaction.TransactionType.EXPENSE)
        assertThat(savedTransaction.amount).isEqualTo(4500L)
        assertThat(savedTransaction.description).isEqualTo("스타벅스 강남점")
        assertThat(savedTransaction.source).isEqualTo(Transaction.TransactionSource.NOTIFICATION)
        assertThat(savedTransaction.transactionDate).isEqualTo(LocalDate.of(2026, 8, 12))
        assertThat(eventCaptor.value.eventId).isNotNull().isNotEqualTo(eventCaptor.value.transactionId)
    }

    @Test
    fun executeFallsBackToUncategorizedWhenCategoryRecommendationFails() {
        val currentUser = User.builder().kakaoId("kakao-1").nickname("사용자").build()
        val request = CreatePaymentNotificationTransactionRequest("알 수 없는 결제처", 4500)
        val currentUserId = UUID.fromString("11111111-1111-1111-1111-111111111111")
        Mockito.`when`(categoryRecommendationClient.recommend("알 수 없는 결제처")).thenReturn(Optional.empty())
        Mockito.`when`(userFacade.currentUserId).thenReturn(currentUserId)
        Mockito.`when`(userRepository.getReferenceById(currentUserId)).thenReturn(currentUser)

        service.execute(request)

        assertThat(captureSavedTransaction().category).isEqualTo(Category.UNCATEGORIZED)
    }

    private fun captureSavedTransaction(): Transaction {
        val captor = ArgumentCaptor.forClass(Transaction::class.java)
        Mockito.verify(transactionRepository).save(captor.capture())
        return captor.value
    }
}
