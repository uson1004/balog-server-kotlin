package org.example.bankramenserver.domain.transaction.presentation;

import org.example.bankramenserver.domain.category.domain.Category;
import org.example.bankramenserver.domain.transaction.domain.Transaction;
import org.example.bankramenserver.domain.transaction.presentation.dto.MonthlyExpenseTransactionListResponse;
import org.example.bankramenserver.domain.transaction.presentation.dto.MonthlyIncomeTransactionListResponse;
import org.example.bankramenserver.domain.transaction.presentation.dto.RecentTransactionListResponse;
import org.example.bankramenserver.domain.transaction.presentation.dto.TransactionHistoryResponse;
import org.example.bankramenserver.domain.transaction.service.CreatePaymentNotificationTransactionService;
import org.example.bankramenserver.domain.transaction.service.CreateTransactionService;
import org.example.bankramenserver.domain.transaction.service.DeleteTransactionService;
import org.example.bankramenserver.domain.transaction.service.GetMonthlyExpenseTransactionListService;
import org.example.bankramenserver.domain.transaction.service.GetMonthlyIncomeTransactionListService;
import org.example.bankramenserver.domain.transaction.service.GetRecentTransactionListService;
import org.example.bankramenserver.domain.transaction.service.UpdateTransactionCategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    private static final UUID TRANSACTION_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Mock
    private GetMonthlyIncomeTransactionListService getMonthlyIncomeTransactionListService;

    @Mock
    private GetMonthlyExpenseTransactionListService getMonthlyExpenseTransactionListService;

    @Mock
    private GetRecentTransactionListService getRecentTransactionListService;

    @Mock
    private CreateTransactionService createTransactionService;

    @Mock
    private CreatePaymentNotificationTransactionService createPaymentNotificationTransactionService;

    @Mock
    private DeleteTransactionService deleteTransactionService;

    @Mock
    private UpdateTransactionCategoryService updateTransactionCategoryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TransactionController(
                        getMonthlyIncomeTransactionListService,
                        getMonthlyExpenseTransactionListService,
                        getRecentTransactionListService,
                        createTransactionService,
                        createPaymentNotificationTransactionService,
                        deleteTransactionService,
                        updateTransactionCategoryService
                ))
                .build();
    }

    @Test
    void getRecentTransactionsReturnsRecentHistories() throws Exception {
        RecentTransactionListResponse response = RecentTransactionListResponse.from(
                List.of(expenseResponse("스타벅스 강남점", 4500L, Category.FOOD))
        );

        when(getRecentTransactionListService.execute(5)).thenReturn(response);

        mockMvc.perform(get("/transactions/recent")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions[0].transactionId").value(TRANSACTION_ID.toString()))
                .andExpect(jsonPath("$.transactions[0].title").value("스타벅스 강남점"))
                .andExpect(jsonPath("$.transactions[0].amount").value(4500))
                .andExpect(jsonPath("$.transactions[0].type").value("EXPENSE"))
                .andExpect(jsonPath("$.transactions[0].category").value("FOOD"));

        verify(getRecentTransactionListService).execute(5);
    }

    @Test
    void createTransactionReturnsCreated() throws Exception {
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "EXPENSE",
                                  "amount": 4500,
                                  "title": "스타벅스 강남점",
                                  "category": "FOOD",
                                  "transactionDate": "2026-08-12"
                                }
                                """))
                .andExpect(status().isCreated());

        verify(createTransactionService).execute(any());
    }

    @Test
    void createPaymentNotificationTransactionReturnsCreated() throws Exception {
        mockMvc.perform(post("/transactions/payment-notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "스타벅스 강남점",
                                  "amount": 4500
                                }
                                """))
                .andExpect(status().isCreated());

        verify(createPaymentNotificationTransactionService).execute(any());
    }

    @Test
    void createPaymentNotificationTransactionRejectsInvalidRequest() throws Exception {
        mockMvc.perform(post("/transactions/payment-notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "amount": 0
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createPaymentNotificationTransactionService);
    }

    @Test
    void createTransactionRejectsInvalidRequest() throws Exception {
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "EXPENSE",
                                  "amount": 0,
                                  "title": "",
                                  "category": "FOOD",
                                  "transactionDate": "2026-08-12"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createTransactionService);
    }

    @Test
    void deleteTransactionReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/transactions/{transactionId}", TRANSACTION_ID))
                .andExpect(status().isNoContent());

        verify(deleteTransactionService).execute(TRANSACTION_ID);
    }

    @Test
    void updateTransactionCategoryReturnsUpdatedTransaction() throws Exception {
        TransactionHistoryResponse response = expenseResponse("스타벅스 강남점", 4500L, Category.CAFE_SNACK);
        when(updateTransactionCategoryService.execute(eq(TRANSACTION_ID), any())).thenReturn(response);

        mockMvc.perform(patch("/transactions/{transactionId}/category", TRANSACTION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "category": "CAFE_SNACK"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(TRANSACTION_ID.toString()))
                .andExpect(jsonPath("$.category").value("CAFE_SNACK"))
                .andExpect(jsonPath("$.categoryName").value("카페/간식"));

        verify(updateTransactionCategoryService).execute(eq(TRANSACTION_ID), any());
    }

    @Test
    void getMonthlyIncomeTransactionsReturnsIncomeHistories() throws Exception {
        MonthlyIncomeTransactionListResponse response = MonthlyIncomeTransactionListResponse.of(
                YearMonth.of(2026, 8),
                List.of(incomeResponse("월급", 3500000L, Category.SALARY))
        );

        when(getMonthlyIncomeTransactionListService.execute(2026, 8)).thenReturn(response);

        mockMvc.perform(get("/transactions/incomes")
                        .param("year", "2026")
                        .param("month", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.yearMonth").value("2026-08"))
                .andExpect(jsonPath("$.incomes[0].title").value("월급"))
                .andExpect(jsonPath("$.incomes[0].transactionDate").value("2026-08-25"))
                .andExpect(jsonPath("$.incomes[0].transactionTime").value("09:30:00"))
                .andExpect(jsonPath("$.incomes[0].amount").value(3500000))
                .andExpect(jsonPath("$.incomes[0].type").value("INCOME"))
                .andExpect(jsonPath("$.incomes[0].category").value("SALARY"))
                .andExpect(jsonPath("$.incomes[0].categoryName").value("급여"));

        verify(getMonthlyIncomeTransactionListService).execute(2026, 8);
    }

    @Test
    void getMonthlyExpenseTransactionsReturnsExpenseHistories() throws Exception {
        MonthlyExpenseTransactionListResponse response = MonthlyExpenseTransactionListResponse.of(
                YearMonth.of(2026, 8),
                List.of(expenseResponse("스타벅스 강남점", 1400L, Category.FOOD))
        );

        when(getMonthlyExpenseTransactionListService.execute(2026, 8)).thenReturn(response);

        mockMvc.perform(get("/transactions/expenses")
                        .param("year", "2026")
                        .param("month", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.yearMonth").value("2026-08"))
                .andExpect(jsonPath("$.expenses[0].title").value("스타벅스 강남점"))
                .andExpect(jsonPath("$.expenses[0].transactionDate").value("2026-08-12"))
                .andExpect(jsonPath("$.expenses[0].transactionTime").value("14:30:00"))
                .andExpect(jsonPath("$.expenses[0].amount").value(1400))
                .andExpect(jsonPath("$.expenses[0].type").value("EXPENSE"))
                .andExpect(jsonPath("$.expenses[0].category").value("FOOD"))
                .andExpect(jsonPath("$.expenses[0].categoryName").value("식비"));

        verify(getMonthlyExpenseTransactionListService).execute(2026, 8);
    }

    private TransactionHistoryResponse incomeResponse(String title, long amount, Category category) {
        return TransactionHistoryResponse.builder()
                .transactionId(TRANSACTION_ID)
                .title(title)
                .transactionDate(LocalDate.of(2026, 8, 25))
                .transactionTime(LocalTime.of(9, 30))
                .amount(amount)
                .type(Transaction.TransactionType.INCOME)
                .category(category)
                .categoryName(category.getDisplayName())
                .build();
    }

    private TransactionHistoryResponse expenseResponse(String title, long amount, Category category) {
        return TransactionHistoryResponse.builder()
                .transactionId(TRANSACTION_ID)
                .title(title)
                .transactionDate(LocalDate.of(2026, 8, 12))
                .transactionTime(LocalTime.of(14, 30))
                .amount(amount)
                .type(Transaction.TransactionType.EXPENSE)
                .category(category)
                .categoryName(category.getDisplayName())
                .build();
    }
}
