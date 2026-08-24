package org.example.bankramenserver.domain.report.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface MonthlyReportRepositoryCustom {

    AmountSummaryRow findAmountSummary(
            UUID userId,
            LocalDate currentStartDate,
            LocalDate currentEndDate,
            LocalDate previousStartDate,
            LocalDate previousEndDate
    );

    List<CategoryExpenseRow> findCategoryExpenseComparisons(
            UUID userId,
            LocalDate currentStartDate,
            LocalDate currentEndDate,
            LocalDate previousStartDate,
            LocalDate previousEndDate
    );
}
