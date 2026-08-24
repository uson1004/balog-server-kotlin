package org.example.bankramenserver.domain.transaction.service;

import org.example.bankramenserver.domain.category.domain.Category;
import org.example.bankramenserver.domain.transaction.domain.Transaction;
import org.example.bankramenserver.domain.transaction.domain.repository.TransactionRepository;
import org.example.bankramenserver.domain.transaction.event.PaymentTransactionRecordedEvent;
import org.example.bankramenserver.domain.transaction.presentation.dto.CreatePaymentNotificationTransactionRequest;
import org.example.bankramenserver.domain.user.domain.User;
import org.example.bankramenserver.domain.user.domain.repository.UserRepository;
import org.example.bankramenserver.domain.user.facade.UserFacade;
import org.example.bankramenserver.global.ai.CategoryRecommendationClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatePaymentNotificationTransactionServiceTest {

    @Mock
    private UserFacade userFacade;

    @Mock
    private CategoryRecommendationClient categoryRecommendationClient;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private CreatePaymentNotificationTransactionService service;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-08-12T00:00:00Z"), ZoneId.of("Asia/Seoul"));
        service = new CreatePaymentNotificationTransactionService(
                userFacade,
                categoryRecommendationClient,
                userRepository,
                transactionRepository,
                applicationEventPublisher,
                fixedClock
        );
    }

    @Test
    void executeSavesExpenseTransactionWithRecommendedCategory() {
        User currentUser = User.builder()
                .kakaoId("kakao-1")
                .nickname("사용자")
                .build();
        CreatePaymentNotificationTransactionRequest request = new CreatePaymentNotificationTransactionRequest(
                "스타벅스 강남점",
                4500L
        );
        UUID currentUserId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(categoryRecommendationClient.recommend("스타벅스 강남점")).thenReturn(Optional.of(Category.CAFE_SNACK));
        when(userFacade.getCurrentUserId()).thenReturn(currentUserId);
        when(userRepository.getReferenceById(currentUserId)).thenReturn(currentUser);

        service.execute(request);

        Transaction savedTransaction = captureSavedTransaction();

        ArgumentCaptor<PaymentTransactionRecordedEvent> eventCaptor = ArgumentCaptor.forClass(PaymentTransactionRecordedEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertThat(savedTransaction.getUser()).isEqualTo(currentUser);
        assertThat(savedTransaction.getCategory()).isEqualTo(Category.CAFE_SNACK);
        assertThat(savedTransaction.getType()).isEqualTo(Transaction.TransactionType.EXPENSE);
        assertThat(savedTransaction.getAmount()).isEqualTo(4500L);
        assertThat(savedTransaction.getDescription()).isEqualTo("스타벅스 강남점");
        assertThat(savedTransaction.getSource()).isEqualTo(Transaction.TransactionSource.NOTIFICATION);
        assertThat(savedTransaction.getTransactionDate()).isEqualTo(LocalDate.of(2026, 8, 12));
        assertThat(eventCaptor.getValue().eventId())
                .isNotNull()
                .isNotEqualTo(eventCaptor.getValue().transactionId());
    }

    @Test
    void executeFallsBackToUncategorizedWhenCategoryRecommendationFails() {
        User currentUser = User.builder()
                .kakaoId("kakao-1")
                .nickname("사용자")
                .build();
        CreatePaymentNotificationTransactionRequest request = new CreatePaymentNotificationTransactionRequest(
                "알 수 없는 결제처",
                4500L
        );
        UUID currentUserId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(categoryRecommendationClient.recommend("알 수 없는 결제처")).thenReturn(Optional.empty());
        when(userFacade.getCurrentUserId()).thenReturn(currentUserId);
        when(userRepository.getReferenceById(currentUserId)).thenReturn(currentUser);

        service.execute(request);

        Transaction savedTransaction = captureSavedTransaction();

        assertThat(savedTransaction.getCategory()).isEqualTo(Category.UNCATEGORIZED);
    }

    private Transaction captureSavedTransaction() {
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        return transactionCaptor.getValue();
    }
}
