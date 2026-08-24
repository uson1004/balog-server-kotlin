package org.example.bankramenserver.domain.report.presentation;

import org.example.bankramenserver.domain.category.domain.Category;
import org.example.bankramenserver.domain.report.presentation.dto.MonthlyAmountSummaryResponse;
import org.example.bankramenserver.domain.report.presentation.dto.MonthlyCategoryExpenseListResponse;
import org.example.bankramenserver.domain.report.service.GetMonthlyAmountSummaryService;
import org.example.bankramenserver.domain.report.service.GetMonthlyCategoryExpenseListService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MonthlyReportControllerTest {

    @Mock
    private GetMonthlyAmountSummaryService getMonthlyAmountSummaryService;

    @Mock
    private GetMonthlyCategoryExpenseListService getMonthlyCategoryExpenseListService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new MonthlyReportController(
                        getMonthlyAmountSummaryService,
                        getMonthlyCategoryExpenseListService
                ))
                .build();
    }

    @Test
    void getMonthlyAmountSummaryReturnsIncomeAndExpenseSummary() throws Exception {
        MonthlyAmountSummaryResponse response = MonthlyAmountSummaryResponse.of(
                YearMonth.of(2026, 8),
                MonthlyAmountSummaryResponse.AmountComparison.of(1250000L, 1500000L, BigDecimal.valueOf(-16.7)),
                MonthlyAmountSummaryResponse.AmountComparison.of(3500000L, 3000000L, BigDecimal.valueOf(16.7))
        );

        when(getMonthlyAmountSummaryService.execute(2026, 8)).thenReturn(response);

        mockMvc.perform(get("/reports/monthly/summary")
                        .param("year", "2026")
                        .param("month", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.yearMonth").value("2026-08"))
                .andExpect(jsonPath("$.expense.currentAmount").value(1250000))
                .andExpect(jsonPath("$.expense.previousAmount").value(1500000))
                .andExpect(jsonPath("$.expense.hasPreviousMonthData").value(true))
                .andExpect(jsonPath("$.expense.differenceRate").value(-16.7))
                .andExpect(jsonPath("$.income.currentAmount").value(3500000))
                .andExpect(jsonPath("$.income.previousAmount").value(3000000))
                .andExpect(jsonPath("$.income.hasPreviousMonthData").value(true))
                .andExpect(jsonPath("$.income.differenceRate").value(16.7));

        verify(getMonthlyAmountSummaryService).execute(2026, 8);
    }

    @Test
    void getMonthlyCategoryExpensesReturnsCategoryExpenseList() throws Exception {
        MonthlyCategoryExpenseListResponse response = MonthlyCategoryExpenseListResponse.of(
                YearMonth.of(2026, 8),
                1250000L,
                List.of(
                        MonthlyCategoryExpenseListResponse.CategoryExpense.of(
                                Category.FOOD,
                                "식비",
                                500000L,
                                BigDecimal.valueOf(40.0),
                                true
                        ),
                        MonthlyCategoryExpenseListResponse.CategoryExpense.of(
                                Category.TRANSPORT_CAR,
                                "교통",
                                250000L,
                                BigDecimal.valueOf(20.0),
                                false
                        )
                )
        );

        when(getMonthlyCategoryExpenseListService.execute(2026, 8)).thenReturn(response);

        mockMvc.perform(get("/reports/monthly/categories")
                        .param("year", "2026")
                        .param("month", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.yearMonth").value("2026-08"))
                .andExpect(jsonPath("$.totalExpense").value(1250000))
                .andExpect(jsonPath("$.categories[0].category").value("FOOD"))
                .andExpect(jsonPath("$.categories[0].categoryName").value("식비"))
                .andExpect(jsonPath("$.categories[0].expenseAmount").value(500000))
                .andExpect(jsonPath("$.categories[0].expenseRatio").value(40.0))
                .andExpect(jsonPath("$.categories[0].spentMoreThanPreviousMonth").value(true))
                .andExpect(jsonPath("$.categories[1].category").value("TRANSPORT_CAR"))
                .andExpect(jsonPath("$.categories[1].spentMoreThanPreviousMonth").value(false));

        verify(getMonthlyCategoryExpenseListService).execute(2026, 8);
    }
}
