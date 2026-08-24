package org.example.bankramenserver.infrastructure.mcp;

import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.report.presentation.dto.MonthlyAmountSummaryResponse;
import org.example.bankramenserver.domain.report.presentation.dto.MonthlyCategoryExpenseListResponse;
import org.example.bankramenserver.domain.report.service.GetMonthlyAmountSummaryService;
import org.example.bankramenserver.domain.report.service.GetMonthlyCategoryExpenseListService;
import org.example.bankramenserver.domain.transaction.presentation.dto.TransactionHistoryResponse;
import org.example.bankramenserver.domain.transaction.service.GetRecentTransactionListService;
import org.example.bankramenserver.infrastructure.mcp.auth.AgentScope;
import org.example.bankramenserver.infrastructure.mcp.auth.McpAgentPrincipal;
import org.example.bankramenserver.infrastructure.mcp.auth.McpAgentPrincipalResolver;
import org.example.bankramenserver.infrastructure.mcp.auth.McpScopeDeniedException;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class McpFinancialTools {

    private final McpAgentPrincipalResolver principalResolver;
    private final GetRecentTransactionListService getRecentTransactionListService;
    private final GetMonthlyAmountSummaryService getMonthlyAmountSummaryService;
    private final GetMonthlyCategoryExpenseListService getMonthlyCategoryExpenseListService;

    public Map<String, Object> getRecentTransactions(Map<String, Object> arguments) {
        return execute(() -> {
            int limit = requiredInt(arguments, "limit", 1, 50);
            McpAgentPrincipal principal = principalResolver.requireScope(AgentScope.TRANSACTIONS_READ);
            List<Map<String, Object>> transactions = getRecentTransactionListService
                    .execute(principal.linkedUserId(), limit)
                    .transactions()
                    .stream()
                    .map(this::transaction)
                    .toList();
            return Map.of("transactions", transactions);
        });
    }

    public Map<String, Object> getMonthlyExpenseSummary(Map<String, Object> arguments) {
        return execute(() -> {
            int year = requiredInt(arguments, "year", 2000, 2100);
            int month = requiredInt(arguments, "month", 1, 12);
            McpAgentPrincipal principal = principalResolver.requireScope(AgentScope.REPORTS_READ);
            MonthlyAmountSummaryResponse summary = getMonthlyAmountSummaryService
                    .execute(principal.linkedUserId(), year, month);
            return Map.of("yearMonth", summary.yearMonth(), "expense", amountComparison(summary.expense()));
        });
    }

    public Map<String, Object> getCategoryExpenses(Map<String, Object> arguments) {
        return execute(() -> {
            int year = requiredInt(arguments, "year", 2000, 2100);
            int month = requiredInt(arguments, "month", 1, 12);
            McpAgentPrincipal principal = principalResolver.requireScope(AgentScope.REPORTS_READ);
            MonthlyCategoryExpenseListResponse response = getMonthlyCategoryExpenseListService
                    .execute(principal.linkedUserId(), year, month);
            return Map.of(
                    "yearMonth", response.yearMonth(),
                    "totalExpense", response.totalExpense(),
                    "categories", response.categories().stream().map(category -> Map.<String, Object>of(
                            "category", category.category().name(),
                            "categoryName", category.categoryName(),
                            "expenseAmount", category.expenseAmount(),
                            "expenseRatio", category.expenseRatio(),
                            "spentMoreThanPreviousMonth", category.spentMoreThanPreviousMonth()
                    )).toList()
            );
        });
    }

    public Map<String, Object> getMonthlyIncomeExpenseSummary(Map<String, Object> arguments) {
        return execute(() -> {
            int year = requiredInt(arguments, "year", 2000, 2100);
            int month = requiredInt(arguments, "month", 1, 12);
            McpAgentPrincipal principal = principalResolver.requireScope(AgentScope.REPORTS_READ);
            MonthlyAmountSummaryResponse summary = getMonthlyAmountSummaryService
                    .execute(principal.linkedUserId(), year, month);
            return Map.of(
                    "yearMonth", summary.yearMonth(),
                    "expense", amountComparison(summary.expense()),
                    "income", amountComparison(summary.income())
            );
        });
    }

    private Map<String, Object> transaction(TransactionHistoryResponse response) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("transactionId", response.transactionId().toString());
        result.put("title", response.title());
        result.put("amount", response.amount());
        result.put("category", response.category().name());
        result.put("type", response.type().name());
        result.put("transactionDate", response.transactionDate().toString());
        return result;
    }

    private Map<String, Object> amountComparison(MonthlyAmountSummaryResponse.AmountComparison comparison) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("currentAmount", comparison.currentAmount());
        result.put("previousAmount", comparison.previousAmount());
        result.put("hasPreviousMonthData", comparison.hasPreviousMonthData());
        result.put("differenceRate", comparison.differenceRate());
        return result;
    }

    private Map<String, Object> execute(Supplier<Map<String, Object>> query) {
        try {
            return query.get();
        } catch (McpInvalidArgumentException exception) {
            return Map.of("error", exception.getMessage());
        } catch (McpScopeDeniedException exception) {
            return Map.of("error", "missing required MCP scope");
        }
    }

    private int requiredInt(Map<String, Object> arguments, String name, int minimum, int maximum) {
        Object value = arguments.get(name);
        if (!(value instanceof Number number)
                || number.doubleValue() != Math.rint(number.doubleValue())
                || number.longValue() < minimum
                || number.longValue() > maximum) {
            throw new McpInvalidArgumentException(name + " must be between " + minimum + " and " + maximum);
        }
        return number.intValue();
    }

    private static class McpInvalidArgumentException extends RuntimeException {

        private McpInvalidArgumentException(String message) {
            super(message);
        }
    }
}
