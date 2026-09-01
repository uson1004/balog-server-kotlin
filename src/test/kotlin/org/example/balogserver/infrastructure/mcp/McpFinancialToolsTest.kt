package org.example.balogserver.infrastructure.mcp

import org.assertj.core.api.Assertions.assertThat
import org.example.balogserver.domain.category.domain.Category
import org.example.balogserver.domain.report.presentation.dto.MonthlyAmountSummaryResponse
import org.example.balogserver.domain.report.presentation.dto.MonthlyCategoryExpenseListResponse
import org.example.balogserver.domain.report.service.GetMonthlyAmountSummaryService
import org.example.balogserver.domain.report.service.GetMonthlyCategoryExpenseListService
import org.example.balogserver.domain.transaction.domain.Transaction
import org.example.balogserver.domain.transaction.presentation.dto.RecentTransactionListResponse
import org.example.balogserver.domain.transaction.presentation.dto.TransactionHistoryResponse
import org.example.balogserver.domain.transaction.service.GetRecentTransactionListService
import org.example.balogserver.infrastructure.mcp.auth.AgentScope
import org.example.balogserver.infrastructure.mcp.auth.McpAgentPrincipal
import org.example.balogserver.infrastructure.mcp.auth.McpAgentPrincipalResolver
import org.example.balogserver.infrastructure.mcp.auth.McpScopeDeniedException
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

class McpFinancialToolsTest {
    private val principalResolver = mock(McpAgentPrincipalResolver::class.java)
    private val getRecentTransactionListService = mock(GetRecentTransactionListService::class.java)
    private val getMonthlyAmountSummaryService = mock(GetMonthlyAmountSummaryService::class.java)
    private val getMonthlyCategoryExpenseListService = mock(GetMonthlyCategoryExpenseListService::class.java)
    private val tools = McpFinancialTools(
        principalResolver,
        getRecentTransactionListService,
        getMonthlyAmountSummaryService,
        getMonthlyCategoryExpenseListService,
    )

    @Test
    fun getRecentTransactionsUsesOnlyTheLinkedUser() {
        `when`(principalResolver.requireScope(AgentScope.TRANSACTIONS_READ)).thenReturn(principal())
        `when`(getRecentTransactionListService.execute(LINKED_USER_ID, 3)).thenReturn(RecentTransactionListResponse(listOf(transaction())))

        val result = tools.getRecentTransactions(mapOf("limit" to 3))

        assertThat(result).containsEntry("transactions", listOf(mapOf(
            "transactionId" to "22222222-2222-2222-2222-222222222222", "title" to "Coffee", "amount" to 4500L,
            "category" to "FOOD", "type" to "EXPENSE", "transactionDate" to "2026-08-12",
        )))
        verify(getRecentTransactionListService).execute(LINKED_USER_ID, 3)
    }

    @Test
    fun getRecentTransactionsPreservesAMissingTitleAsNull() {
        `when`(principalResolver.requireScope(AgentScope.TRANSACTIONS_READ)).thenReturn(principal())
        `when`(getRecentTransactionListService.execute(LINKED_USER_ID, 1)).thenReturn(RecentTransactionListResponse(listOf(transaction(title = null))))

        val result = tools.getRecentTransactions(mapOf("limit" to 1))

        val transaction = (result["transactions"] as List<*>).first() as Map<*, *>
        assertThat(transaction["title"]).isNull()
    }

    @Test
    fun getMonthlyExpenseSummaryDelegatesToTheMonthlyAmountReadPath() {
        `when`(principalResolver.requireScope(AgentScope.REPORTS_READ)).thenReturn(principal())
        `when`(getMonthlyAmountSummaryService.execute(LINKED_USER_ID, 2026, 8)).thenReturn(summary())

        val result = tools.getMonthlyExpenseSummary(mapOf("year" to 2026, "month" to 8))

        assertThat(result).containsEntry("expense", mapOf(
            "currentAmount" to 4500L, "previousAmount" to 3000L, "hasPreviousMonthData" to true, "differenceRate" to BigDecimal.valueOf(50.0),
        ))
        verify(getMonthlyAmountSummaryService).execute(LINKED_USER_ID, 2026, 8)
    }

    @Test
    fun getCategoryExpensesDelegatesToTheCategoryExpenseReadPath() {
        `when`(principalResolver.requireScope(AgentScope.REPORTS_READ)).thenReturn(principal())
        `when`(getMonthlyCategoryExpenseListService.execute(LINKED_USER_ID, 2026, 8)).thenReturn(categories())

        val result = tools.getCategoryExpenses(mapOf("year" to 2026, "month" to 8))

        assertThat(result).containsEntry("totalExpense", 4500L)
        verify(getMonthlyCategoryExpenseListService).execute(LINKED_USER_ID, 2026, 8)
    }

    @Test
    fun getMonthlyIncomeExpenseSummaryDelegatesToTheMonthlyAmountReadPath() {
        `when`(principalResolver.requireScope(AgentScope.REPORTS_READ)).thenReturn(principal())
        `when`(getMonthlyAmountSummaryService.execute(LINKED_USER_ID, 2026, 8)).thenReturn(summary())

        val result = tools.getMonthlyIncomeExpenseSummary(mapOf("year" to 2026, "month" to 8))

        assertThat(result).containsKeys("expense", "income", "yearMonth")
        verify(getMonthlyAmountSummaryService).execute(LINKED_USER_ID, 2026, 8)
    }

    @Test
    fun rejectsInvalidArgumentsBeforeQueryingData() {
        val result = tools.getRecentTransactions(mapOf("limit" to 0))

        assertThat(result).containsEntry("error", "limit must be between 1 and 50")
        verifyNoInteractions(principalResolver, getRecentTransactionListService, getMonthlyAmountSummaryService, getMonthlyCategoryExpenseListService)
    }

    @Test
    fun rejectsAnInvalidMonthBeforeCheckingReportScope() {
        val result = tools.getCategoryExpenses(mapOf("year" to 2026, "month" to 13))

        assertThat(result).containsEntry("error", "month must be between 1 and 12")
        verifyNoInteractions(principalResolver, getRecentTransactionListService, getMonthlyAmountSummaryService, getMonthlyCategoryExpenseListService)
    }

    @Test
    fun rejectsAnInvalidExpenseSummaryMonthBeforeCheckingReportScope() {
        val result = tools.getMonthlyExpenseSummary(mapOf("year" to 2026, "month" to 13))

        assertThat(result).containsEntry("error", "month must be between 1 and 12")
        verifyNoInteractions(principalResolver, getRecentTransactionListService, getMonthlyAmountSummaryService, getMonthlyCategoryExpenseListService)
    }

    @Test
    fun rejectsAToolCallWithoutItsRequiredScope() {
        `when`(principalResolver.requireScope(AgentScope.REPORTS_READ)).thenThrow(McpScopeDeniedException())

        val result = tools.getCategoryExpenses(mapOf("year" to 2026, "month" to 8))

        assertThat(result).containsEntry("error", "missing required MCP scope")
        verifyNoInteractions(getMonthlyCategoryExpenseListService)
    }

    private fun principal() = McpAgentPrincipal(
        "hermes-local", "hermes-agent", LINKED_USER_ID, setOf(AgentScope.TRANSACTIONS_READ, AgentScope.REPORTS_READ),
    )

    private fun transaction(title: String? = "Coffee") = TransactionHistoryResponse(
        UUID.fromString("22222222-2222-2222-2222-222222222222"), title, LocalDate.of(2026, 8, 12), null,
        4500L, Transaction.TransactionType.EXPENSE, Category.FOOD, null,
    )

    private fun summary() = MonthlyAmountSummaryResponse.of(
        YearMonth.of(2026, 8),
        MonthlyAmountSummaryResponse.AmountComparison.of(4500L, 3000L, BigDecimal.valueOf(50.0)),
        MonthlyAmountSummaryResponse.AmountComparison.of(10000L, 9000L, BigDecimal.valueOf(11.1)),
    )

    private fun categories() = MonthlyCategoryExpenseListResponse.of(
        YearMonth.of(2026, 8),
        4500L,
        listOf(MonthlyCategoryExpenseListResponse.CategoryExpense.of(Category.FOOD, "Food", 4500L, BigDecimal.valueOf(100.0), true)),
    )

    private companion object {
        val LINKED_USER_ID: UUID = UUID.fromString("11111111-1111-1111-1111-111111111111")
    }
}
