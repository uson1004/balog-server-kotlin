package org.example.bankramenserver.domain.recurring.service;

import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.recurring.domain.RecurringPayment;
import org.example.bankramenserver.domain.recurring.domain.RecurringPaymentTransaction;
import org.example.bankramenserver.domain.recurring.domain.repository.RecurringPaymentRepository;
import org.example.bankramenserver.domain.recurring.exception.DuplicateRecurringPaymentException;
import org.example.bankramenserver.domain.recurring.exception.InvalidRecurringPaymentTransactionException;
import org.example.bankramenserver.domain.recurring.presentation.dto.request.CreateRecurringPaymentRequest;
import org.example.bankramenserver.domain.recurring.presentation.dto.response.CreateRecurringPaymentResponse;
import org.example.bankramenserver.domain.transaction.domain.Transaction;
import org.example.bankramenserver.domain.transaction.domain.repository.TransactionRepository;
import org.example.bankramenserver.domain.transaction.exception.TransactionNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateRecurringPaymentService {

    private final RecurringPaymentRepository recurringPaymentRepository;
    private final TransactionRepository transactionRepository;
    private final Clock clock;

    @Transactional
    public CreateRecurringPaymentResponse execute(UUID userId, CreateRecurringPaymentRequest request) {
        Transaction transaction = transactionRepository.findByIdAndUser_Id(
                        request.transactionId(),
                        userId
                )
                .orElseThrow(TransactionNotFoundException::new);

        if (transaction.getType() != Transaction.TransactionType.EXPENSE) {
            throw new InvalidRecurringPaymentTransactionException();
        }

        if (transaction.getDescription() == null || transaction.getDescription().isBlank()) {
            throw new InvalidRecurringPaymentTransactionException();
        }

        boolean alreadyExists = recurringPaymentRepository.existsByUser_IdAndNameAndAmountAndCycleAndActiveTrue(
                userId,
                transaction.getDescription(),
                transaction.getAmount(),
                request.cycle()
        );

        if (alreadyExists) {
            throw new DuplicateRecurringPaymentException();
        }

        RecurringPayment recurringPayment = RecurringPayment.builder()
                .user(transaction.getUser())
                .category(transaction.getCategory())
                .name(transaction.getDescription())
                .amount(transaction.getAmount())
                .cycle(request.cycle())
                .billingDay(transaction.getTransactionDate().getDayOfMonth())
                .nextBillingDate(request.nextBillingDate().atStartOfDay())
                .registrationType(RecurringPayment.RegistrationType.MANUAL)
                .confirmed(true)
                .build();

        List<Transaction> relatedTransactions =
                transactionRepository.findAllByUser_IdAndTypeAndDescriptionAndAmountOrderByTransactionDateDesc(
                        userId,
                        Transaction.TransactionType.EXPENSE,
                        transaction.getDescription(),
                        transaction.getAmount()
                );

        LocalDateTime matchedAt = LocalDateTime.now(clock);

        relatedTransactions.forEach(item ->
                recurringPayment.addTransaction(
                        item,
                        item.getId().equals(transaction.getId())
                                ? RecurringPaymentTransaction.MatchType.INITIAL
                                : RecurringPaymentTransaction.MatchType.MANUAL_ADDED,
                        matchedAt
                )
        );

        RecurringPayment saved = recurringPaymentRepository.save(recurringPayment);

        return new CreateRecurringPaymentResponse(saved.getId());
    }
}