package org.example.bankramenserver.domain.report.domain.repository;

import lombok.Builder;

@Builder
public record AmountSummaryRow(
        Long currentExpense,
        Long previousExpense,
        Long previousExpenseCount,
        Long currentIncome,
        Long previousIncome,
        Long previousIncomeCount
) { }
