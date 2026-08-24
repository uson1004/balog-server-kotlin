package org.example.bankramenserver.domain.transaction.domain.repository;

import lombok.Builder;
import org.example.bankramenserver.domain.category.domain.Category;
import org.example.bankramenserver.domain.transaction.domain.Transaction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record TransactionHistoryRow(
        UUID transactionId,
        String title,
        LocalDate transactionDate,
        LocalDateTime createdAt,
        Long amount,
        Transaction.TransactionType type,
        Category category
) { }
