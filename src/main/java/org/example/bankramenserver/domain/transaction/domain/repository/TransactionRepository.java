package org.example.bankramenserver.domain.transaction.domain.repository;

import org.example.bankramenserver.domain.transaction.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, TransactionRepositoryCustom {

    Optional<Transaction> findByIdAndUser_Id(UUID transactionId, UUID userId);

    List<Transaction> findAllByUser_IdAndTypeAndDescriptionAndAmountAndTransactionDateBetweenOrderByTransactionDateDesc(
            UUID userId,
            Transaction.TransactionType type,
            String description,
            Long amount,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Transaction> findAllByUser_IdAndTypeAndDescriptionAndAmountOrderByTransactionDateDesc(
            UUID userId,
            Transaction.TransactionType type,
            String description,
            Long amount
    );
}