package org.example.bankramenserver.infrastructure.mcp

import org.example.bankramenserver.domain.report.presentation.dto.MonthlyAmountSummaryResponse
import org.example.bankramenserver.domain.report.presentation.dto.MonthlyCategoryExpenseListResponse
import org.example.bankramenserver.domain.report.service.GetMonthlyAmountSummaryService
import org.example.bankramenserver.domain.report.service.GetMonthlyCategoryExpenseListService
import org.example.bankramenserver.domain.transaction.presentation.dto.TransactionHistoryResponse
import org.example.bankramenserver.domain.transaction.service.GetRecentTransactionListService
import org.example.bankramenserver.infrastructure.mcp.auth.AgentScope
import org.example.bankramenserver.infrastructure.mcp.auth.McpAgentPrincipalResolver
import org.example.bankramenserver.infrastructure.mcp.auth.McpScopeDeniedException
import org.springframework.stereotype.Service
import java.util.LinkedHashMap

@Service
class McpFinancialTools(
    private val principalResolver: McpAgentPrincipalResolver,
    private val getRecentTransactionListService: GetRecentTransactionListService,
    private val getMonthlyAmountSummaryService: GetMonthlyAmountSummaryService,
    private val getMonthlyCategoryExpenseListService: GetMonthlyCategoryExpenseListService,
) {
    fun getRecentTransactions(arguments: Map<String, Any>): Map<String, Any> = execute {
        val limit = requiredInt(arguments, "limit", 1, 50)
        val principal = principalResolver.requireScope(AgentScope.TRANSACTIONS_READ)
        mapOf("transactions" to getRecentTransactionListService.execute(principal.linkedUserId, limit).transactions.map(::transaction))
    }

    fun getMonthlyExpenseSummary(arguments: Map<String, Any>): Map<String, Any> = execute {
        val year = requiredInt(arguments, "year", 2000, 2100)
        val month = requiredInt(arguments, "month", 1, 12)
        val principal = principalResolver.requireScope(AgentScope.REPORTS_READ)
        val summary = getMonthlyAmountSummaryService.execute(principal.linkedUserId, year, month)
        mapOf("yearMonth" to summary.yearMonth, "expense" to amountComparison(summary.expense))
    }

    fun getCategoryExpenses(arguments: Map<String, Any>): Map<String, Any> = execute {
        val year = requiredInt(arguments, "year", 2000, 2100)
        val month = requiredInt(arguments, "month", 1, 12)
        val principal = principalResolver.requireScope(AgentScope.REPORTS_READ)
        val response = getMonthlyCategoryExpenseListService.execute(principal.linkedUserId, year, month)
        mapOf(
            "yearMonth" to response.yearMonth,
            "totalExpense" to response.totalExpense,
            "categories" to response.categories.map {
                mapOf(
                    "category" to it.category.name,
                    "categoryName" to it.categoryName,
                    "expenseAmount" to it.expenseAmount,
                    "expenseRatio" to it.expenseRatio,
                    "spentMoreThanPreviousMonth" to it.spentMoreThanPreviousMonth,
                )
            },
        )
    }

    fun getMonthlyIncomeExpenseSummary(arguments: Map<String, Any>): Map<String, Any> = execute {
        val year = requiredInt(arguments, "year", 2000, 2100)
        val month = requiredInt(arguments, "month", 1, 12)
        val principal = principalResolver.requireScope(AgentScope.REPORTS_READ)
        val summary = getMonthlyAmountSummaryService.execute(principal.linkedUserId, year, month)
        mapOf(
            "yearMonth" to summary.yearMonth,
            "expense" to amountComparison(summary.expense),
            "income" to amountComparison(summary.income),
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun transaction(response: TransactionHistoryResponse): Map<String, Any> = LinkedHashMap<String, Any?>().apply {
        put("transactionId", response.transactionId!!.toString())
        put("title", response.title)
        put("amount", response.amount)
        put("category", response.category!!.name)
        put("type", response.type!!.name)
        put("transactionDate", response.transactionDate!!.toString())
    } as Map<String, Any>

    @Suppress("UNCHECKED_CAST")
    private fun amountComparison(comparison: MonthlyAmountSummaryResponse.AmountComparison): Map<String, Any> = linkedMapOf<String, Any?>().apply {
        put("currentAmount", comparison.currentAmount)
        put("previousAmount", comparison.previousAmount)
        put("hasPreviousMonthData", comparison.hasPreviousMonthData)
        put("differenceRate", comparison.differenceRate)
    } as Map<String, Any>

    private fun execute(query: () -> Map<String, Any>): Map<String, Any> = try {
        query()
    } catch (exception: McpInvalidArgumentException) {
        mapOf("error" to checkNotNull(exception.message))
    } catch (_: McpScopeDeniedException) {
        mapOf("error" to "missing required MCP scope")
    }

    private fun requiredInt(arguments: Map<String, Any>, name: String, minimum: Int, maximum: Int): Int {
        val value = arguments[name] as? Number
            ?: throw McpInvalidArgumentException("$name must be between $minimum and $maximum")
        if (value.toDouble() != Math.rint(value.toDouble()) || value.toLong() !in minimum.toLong()..maximum.toLong()) {
            throw McpInvalidArgumentException("$name must be between $minimum and $maximum")
        }
        return value.toInt()
    }

    private class McpInvalidArgumentException(message: String) : RuntimeException(message)
}
