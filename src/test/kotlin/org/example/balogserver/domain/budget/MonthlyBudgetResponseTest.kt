package org.example.balogserver.domain.budget

import org.assertj.core.api.Assertions.assertThat
import org.example.balogserver.domain.budget.presentation.dto.MonthlyBudgetResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.LocalDate
import java.time.YearMonth

class MonthlyBudgetResponseTest {
    @Test
    fun calendarUsesFixedTargetAndOnlyJudgesCompletedDays() {
        val month = YearMonth.of(2028, 2)
        val result = MonthlyBudgetResponse.of(month, 2900, 201, month.atDay(4), mapOf(
            month.atDay(1) to 100L, month.atDay(2) to 101L,
        ))
        assertThat(result.dailyTargetAmount).isEqualTo(100)
        assertThat(result.days).hasSize(29)
        assertThat(result.days.take(5).map { it.status.name })
            .containsExactly("SUCCESS", "FAIL", "SUCCESS", "PENDING", "PENDING")
        assertThat(result.days.take(3).map { it.expenseAmount }).containsExactly(100L, 101L, 0L)
        assertThat(result.days.last().date).isEqualTo(month.atEndOfMonth())
        val unset = MonthlyBudgetResponse.of(month, null, 201, month.atDay(4))
        assertThat(unset.days.all { it.targetAmount == null && it.status.name == "UNSET" }).isTrue()
        val revised = MonthlyBudgetResponse.of(month, 5800, 201, month.atDay(4), mapOf(month.atDay(2) to 101L))
        assertThat(revised.days[1].status.name).isEqualTo("SUCCESS")
    }

    @ParameterizedTest
    @CsvSource(
        "2026-09, 2026-09-01, 30, 26666",
        "2026-09, 2026-09-08, 23, 34782",
        "2026-09, 2026-09-30, 1, 800000",
        "2026-08, 2026-09-08, 0, 0",
        "2026-10, 2026-09-08, 31, 25806",
        "2028-02, 2028-02-28, 2, 400000",
        "2028-02, 2028-02-29, 1, 800000",
        "2027-02, 2027-02-28, 1, 800000",
    )
    fun dailyAmountUsesInclusiveDaysAndFloorsWon(month: String, today: String, days: Int, daily: Long) {
        val result = MonthlyBudgetResponse.of(YearMonth.parse(month), 1000000, 200000, LocalDate.parse(today))
        assertThat(result.remainingAmount).isEqualTo(800000)
        assertThat(result.remainingDays).isEqualTo(days)
        assertThat(result.dailyAvailableAmount).isEqualTo(daily)
    }

    @Test
    fun unsetBudgetKeepsSpendingButDoesNotInventAvailableMoney() {
        val result = MonthlyBudgetResponse.of(YearMonth.of(2026, 9), null, 25000, LocalDate.of(2026, 9, 8))
        assertThat(result.budgetAmount).isNull()
        assertThat(result.remainingAmount).isNull()
        assertThat(result.dailyAvailableAmount).isNull()
        assertThat(result.totalExpense).isEqualTo(25000)
    }

    @ParameterizedTest
    @CsvSource("100, 101, -1", "0, 0, 0", "100, 100, 0")
    fun exhaustedBudgetHasZeroDailyAllowance(budget: Long, expense: Long, remaining: Long) {
        val result = MonthlyBudgetResponse.of(YearMonth.of(2026, 9), budget, expense, LocalDate.of(2026, 9, 8))
        assertThat(result.remainingAmount).isEqualTo(remaining)
        assertThat(result.dailyAvailableAmount).isZero()
    }
}
