package org.example.bankramenserver.infrastructure.mcp;

import org.example.bankramenserver.domain.category.domain.Category;
import org.example.bankramenserver.domain.report.presentation.dto.MonthlyAmountSummaryResponse;
import org.example.bankramenserver.domain.report.presentation.dto.MonthlyCategoryExpenseListResponse;
import org.example.bankramenserver.domain.report.service.GetMonthlyAmountSummaryService;
import org.example.bankramenserver.domain.report.service.GetMonthlyCategoryExpenseListService;
import org.example.bankramenserver.domain.transaction.domain.Transaction;
import org.example.bankramenserver.domain.transaction.presentation.dto.RecentTransactionListResponse;
import org.example.bankramenserver.domain.transaction.presentation.dto.TransactionHistoryResponse;
import org.example.bankramenserver.domain.transaction.service.GetRecentTransactionListService;
import org.example.bankramenserver.infrastructure.mcp.auth.AgentScope;
import org.example.bankramenserver.infrastructure.mcp.auth.McpAgentPrincipal;
import org.example.bankramenserver.infrastructure.mcp.auth.McpAgentPrincipalResolver;
import org.example.bankramenserver.infrastructure.mcp.auth.McpScopeDeniedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class McpFinancialToolsTest {

    private static final UUID LINKED_USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private McpAgentPrincipalResolver principalResolver;

    @Mock
    private GetRecentTransactionListService getRecentTransactionListService;

    @Mock
    private GetMonthlyAmountSummaryService getMonthlyAmountSummaryService;

    @Mock
    private GetMonthlyCategoryExpenseListService getMonthlyCategoryExpenseListService;

    @InjectMocks
    private McpFinancialTools tools;

    @Test
    void getRecentTransactionsUsesOnlyTheLinkedUser() {
        when(principalResolver.requireScope(AgentScope.TRANSACTIONS_READ)).thenReturn(principal());
        when(getRecentTransactionListService.execute(LINKED_USER_ID, 3)).thenReturn(
                RecentTransactionListResponse.from(List.of(transaction()))
        );

        Map<String, Object> result = tools.getRecentTransactions(Map.of("limit", 3));

        assertThat(result).containsEntry("transactions", List.of(Map.of(
                "transactionId", "22222222-2222-2222-2222-222222222222",
                "title", "Coffee",
                "amount", 4500L,
                "category", "FOOD",
                "type", "EXPENSE",
                "transactionDate", "2026-08-12"
        )));
        verify(getRecentTransactionListService).execute(LINKED_USER_ID, 3);
    }

    @Test
    void getRecentTransactionsPreservesAMissingTitleAsNull() {
        when(principalResolver.requireScope(AgentScope.TRANSACTIONS_READ)).thenReturn(principal());
        when(getRecentTransactionListService.execute(LINKED_USER_ID, 1)).thenReturn(
                RecentTransactionListResponse.from(List.of(transactionWithoutTitle()))
        );

        Map<String, Object> result = tools.getRecentTransactions(Map.of("limit", 1));

        Map<?, ?> transaction = (Map<?, ?>) ((List<?>) result.get("transactions")).get(0);
        assertThat(transaction.get("title")).isNull();
    }

    @Test
    void getMonthlyExpenseSummaryDelegatesToTheMonthlyAmountReadPath() {
        when(principalResolver.requireScope(AgentScope.REPORTS_READ)).thenReturn(principal());
        when(getMonthlyAmountSummaryService.execute(LINKED_USER_ID, 2026, 8)).thenReturn(summary());

        Map<String, Object> result = tools.getMonthlyExpenseSummary(Map.of("year", 2026, "month", 8));

        assertThat(result).containsEntry("expense", Map.of(
                "currentAmount", 4500L,
                "previousAmount", 3000L,
                "hasPreviousMonthData", true,
                "differenceRate", BigDecimal.valueOf(50.0)
        ));
        verify(getMonthlyAmountSummaryService).execute(LINKED_USER_ID, 2026, 8);
    }

    @Test
    void getCategoryExpensesDelegatesToTheCategoryExpenseReadPath() {
        when(principalResolver.requireScope(AgentScope.REPORTS_READ)).thenReturn(principal());
        when(getMonthlyCategoryExpenseListService.execute(LINKED_USER_ID, 2026, 8)).thenReturn(categories());

        Map<String, Object> result = tools.getCategoryExpenses(Map.of("year", 2026, "month", 8));

        assertThat(result).containsEntry("totalExpense", 4500L);
        verify(getMonthlyCategoryExpenseListService).execute(LINKED_USER_ID, 2026, 8);
    }

    @Test
    void getMonthlyIncomeExpenseSummaryDelegatesToTheMonthlyAmountReadPath() {
        when(principalResolver.requireScope(AgentScope.REPORTS_READ)).thenReturn(principal());
        when(getMonthlyAmountSummaryService.execute(LINKED_USER_ID, 2026, 8)).thenReturn(summary());

        Map<String, Object> result = tools.getMonthlyIncomeExpenseSummary(Map.of("year", 2026, "month", 8));

        assertThat(result).containsKeys("expense", "income", "yearMonth");
        verify(getMonthlyAmountSummaryService).execute(LINKED_USER_ID, 2026, 8);
    }

    @Test
    void rejectsInvalidArgumentsBeforeQueryingData() {
        Map<String, Object> result = tools.getRecentTransactions(Map.of("limit", 0));

        assertThat(result).containsEntry("error", "limit must be between 1 and 50");
        verifyNoInteractions(principalResolver, getRecentTransactionListService,
                getMonthlyAmountSummaryService, getMonthlyCategoryExpenseListService);
    }

    @Test
    void rejectsAnInvalidMonthBeforeCheckingReportScope() {
        Map<String, Object> result = tools.getCategoryExpenses(Map.of("year", 2026, "month", 13));

        assertThat(result).containsEntry("error", "month must be between 1 and 12");
        verifyNoInteractions(principalResolver, getRecentTransactionListService,
                getMonthlyAmountSummaryService, getMonthlyCategoryExpenseListService);
    }

    @Test
    void rejectsAnInvalidExpenseSummaryMonthBeforeCheckingReportScope() {
        Map<String, Object> result = tools.getMonthlyExpenseSummary(Map.of("year", 2026, "month", 13));

        assertThat(result).containsEntry("error", "month must be between 1 and 12");
        verifyNoInteractions(principalResolver, getRecentTransactionListService,
                getMonthlyAmountSummaryService, getMonthlyCategoryExpenseListService);
    }

    @Test
    void rejectsAToolCallWithoutItsRequiredScope() {
        when(principalResolver.requireScope(AgentScope.REPORTS_READ)).thenThrow(new McpScopeDeniedException());

        Map<String, Object> result = tools.getCategoryExpenses(Map.of("year", 2026, "month", 8));

        assertThat(result).containsEntry("error", "missing required MCP scope");
        verifyNoInteractions(getMonthlyCategoryExpenseListService);
    }

    private McpAgentPrincipal principal() {
        return new McpAgentPrincipal(
                "hermes-local",
                "hermes-agent",
                LINKED_USER_ID,
                Set.of(AgentScope.TRANSACTIONS_READ, AgentScope.REPORTS_READ)
        );
    }

    private TransactionHistoryResponse transaction() {
        return TransactionHistoryResponse.builder()
                .transactionId(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .title("Coffee")
                .amount(4500L)
                .category(Category.FOOD)
                .type(Transaction.TransactionType.EXPENSE)
                .transactionDate(LocalDate.of(2026, 8, 12))
                .build();
    }

    private TransactionHistoryResponse transactionWithoutTitle() {
        return TransactionHistoryResponse.builder()
                .transactionId(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .title(null)
                .amount(4500L)
                .category(Category.FOOD)
                .type(Transaction.TransactionType.EXPENSE)
                .transactionDate(LocalDate.of(2026, 8, 12))
                .build();
    }

    private MonthlyAmountSummaryResponse summary() {
        return MonthlyAmountSummaryResponse.of(
                YearMonth.of(2026, 8),
                MonthlyAmountSummaryResponse.AmountComparison.of(4500L, 3000L, BigDecimal.valueOf(50.0)),
                MonthlyAmountSummaryResponse.AmountComparison.of(10000L, 9000L, BigDecimal.valueOf(11.1))
        );
    }

    private MonthlyCategoryExpenseListResponse categories() {
        return MonthlyCategoryExpenseListResponse.of(
                YearMonth.of(2026, 8),
                4500L,
                List.of(MonthlyCategoryExpenseListResponse.CategoryExpense.of(
                        Category.FOOD, "Food", 4500L, BigDecimal.valueOf(100.0), true
                ))
        );
    }
}
