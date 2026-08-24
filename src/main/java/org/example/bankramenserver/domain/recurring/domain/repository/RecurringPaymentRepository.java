package org.example.bankramenserver.domain.recurring.domain.repository;

import org.example.bankramenserver.domain.recurring.domain.RecurringPayment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface RecurringPaymentRepository extends JpaRepository<RecurringPayment, UUID> {

    @EntityGraph(attributePaths = {"transactions", "transactions.transaction"})
    List<RecurringPayment> findAllByUser_IdAndActiveTrueOrderByNextBillingDateAsc(UUID userId);

    List<RecurringPayment> findAllByActiveTrueAndConfirmedTrueAndNextBillingDateBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    List<RecurringPayment> findAllByUser_IdAndActiveTrueAndConfirmedTrueAndNextBillingDateBetween(
            UUID userId,
            LocalDateTime start,
            LocalDateTime end
    );

    boolean existsByUser_IdAndNameAndAmountAndCycleAndActiveTrue(
            UUID userId,
            String name,
            Long amount,
            RecurringPayment.Cycle cycle
    );

    List<RecurringPayment> findAllByActiveTrueAndConfirmedTrueAndNextBillingDateGreaterThanEqualAndNextBillingDateLessThan(
            LocalDateTime start,
            LocalDateTime end
    );
}