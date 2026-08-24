package org.example.bankramenserver.domain.report.service;

import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.report.domain.repository.CategoryExpenseRow;
import org.example.bankramenserver.domain.report.domain.repository.MonthlyReportRepository;
import org.example.bankramenserver.domain.report.presentation.dto.MonthlyCategoryExpenseListResponse;
import org.example.bankramenserver.domain.user.facade.UserFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetMonthlyCategoryExpenseListService {

    private final UserFacade userFacade;
    private final MonthlyReportRepository monthlyReportRepository;

    @Transactional(readOnly = true)
    public MonthlyCategoryExpenseListResponse execute(int year, int month) {
        return execute(userFacade.getCurrentUser().getId(), year, month);
    }

    @Transactional(readOnly = true)
    public MonthlyCategoryExpenseListResponse execute(UUID userId, int year, int month) {
        YearMonth currentMonth = YearMonth.of(year, month);
        YearMonth previousMonth = currentMonth.minusMonths(1);

        List<CategoryExpenseRow> rows = monthlyReportRepository.findCategoryExpenseComparisons(
                userId,
                currentMonth.atDay(1),
                currentMonth.atEndOfMonth(),
                previousMonth.atDay(1),
                previousMonth.atEndOfMonth()
        );
        long totalExpense = rows.stream()
                .mapToLong(row -> getAmount(row.currentExpense()))
                .sum();

        List<MonthlyCategoryExpenseListResponse.CategoryExpense> categories = rows.stream()
                .map(row -> toCategoryExpense(row, totalExpense))
                .toList();

        return MonthlyCategoryExpenseListResponse.of(
                currentMonth,
                totalExpense,
                categories
        );
    }

    private MonthlyCategoryExpenseListResponse.CategoryExpense toCategoryExpense(
            CategoryExpenseRow row,
            long totalExpense
    ) {
        long expenseAmount = getAmount(row.currentExpense());
        long previousExpenseAmount = getAmount(row.previousExpense());

        return MonthlyCategoryExpenseListResponse.CategoryExpense.of(
                row.category(),
                row.category().getDisplayName(),
                expenseAmount,
                calculateRatio(expenseAmount, totalExpense),
                expenseAmount > previousExpenseAmount
        );
    }

    private long getAmount(Long amount) {
        return amount == null ? 0L : amount;
    }

    private BigDecimal calculateRatio(long amount, long totalExpense) {
        if (totalExpense <= 0) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }

        return BigDecimal.valueOf(amount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalExpense), 1, RoundingMode.HALF_UP);
    }

}
