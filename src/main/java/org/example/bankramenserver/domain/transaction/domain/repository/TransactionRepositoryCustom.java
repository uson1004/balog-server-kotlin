package org.example.bankramenserver.domain.transaction.domain.repository;

import org.example.bankramenserver.domain.transaction.domain.Transaction;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TransactionRepositoryCustom {

    List<TransactionHistoryRow> findTransactionHistories(
            UUID userId,
            Transaction.TransactionType transactionType,
            LocalDate startDate,
            LocalDate endDate
    );

    List<TransactionHistoryRow> findRecentTransactionHistories(
            UUID userId,
            int limit
    );

    List<UUID> findUserIdsHavingTransactionsBetween(
            LocalDate startDate,
            LocalDate endDate
    );

    boolean existsSameExpenseTransactionBetween(
            UUID userId,
            String description,
            Long amount,
            LocalDate startDate,
            LocalDate endDate
    );
}
