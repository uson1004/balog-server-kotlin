package org.example.balogserver.domain.transaction.service

import org.assertj.core.api.Assertions.assertThat
import org.example.balogserver.domain.category.domain.Category
import org.example.balogserver.domain.transaction.domain.Transaction
import org.example.balogserver.domain.transaction.domain.repository.TransactionRepository
import org.example.balogserver.domain.transaction.event.PaymentTransactionRecordedEvent
import org.example.balogserver.domain.transaction.presentation.dto.CreateTransactionRequest
import org.example.balogserver.domain.user.domain.User
import org.example.balogserver.domain.user.facade.UserFacade
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDate
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class CreateTransactionServiceTest {
    @Mock private lateinit var userFacade: UserFacade
    @Mock private lateinit var transactionRepository: TransactionRepository
    @Mock private lateinit var applicationEventPublisher: ApplicationEventPublisher

    @Test
    fun executePublishesPaymentRecordedEventForManualTransaction() {
        val userId = UUID.fromString("11111111-1111-1111-1111-111111111111")
        Mockito.`when`(userFacade.currentUserId).thenReturn(userId)
        Mockito.`when`(userFacade.currentUser).thenReturn(User.singleUser(userId))

        CreateTransactionService(userFacade, transactionRepository, applicationEventPublisher).execute(
            CreateTransactionRequest(Transaction.TransactionType.EXPENSE, 4500, "스타벅스 강남점", Category.CAFE_SNACK, LocalDate.of(2026, 8, 12)),
        )

        val captor = ArgumentCaptor.forClass(PaymentTransactionRecordedEvent::class.java)
        Mockito.verify(applicationEventPublisher).publishEvent(captor.capture())
        assertThat(captor.value.userId).isEqualTo(userId)
        assertThat(captor.value.title).isEqualTo("스타벅스 강남점")
        assertThat(captor.value.amount).isEqualTo(4500)
        assertThat(captor.value.category).isEqualTo(Category.CAFE_SNACK)
        assertThat(captor.value.occurredAt).isEqualTo(LocalDate.of(2026, 8, 12))
    }
}
