package org.example.bankramenserver.domain.transaction.presentation

import org.example.bankramenserver.domain.category.domain.Category
import org.example.bankramenserver.domain.transaction.domain.Transaction
import org.example.bankramenserver.domain.transaction.presentation.dto.MonthlyExpenseTransactionListResponse
import org.example.bankramenserver.domain.transaction.presentation.dto.MonthlyIncomeTransactionListResponse
import org.example.bankramenserver.domain.transaction.presentation.dto.RecentTransactionListResponse
import org.example.bankramenserver.domain.transaction.presentation.dto.TransactionHistoryResponse
import org.example.bankramenserver.domain.transaction.service.CreatePaymentNotificationTransactionService
import org.example.bankramenserver.domain.transaction.service.CreateTransactionService
import org.example.bankramenserver.domain.transaction.service.DeleteTransactionService
import org.example.bankramenserver.domain.transaction.service.GetMonthlyExpenseTransactionListService
import org.example.bankramenserver.domain.transaction.service.GetMonthlyIncomeTransactionListService
import org.example.bankramenserver.domain.transaction.service.GetRecentTransactionListService
import org.example.bankramenserver.domain.transaction.service.UpdateTransactionCategoryService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class TransactionControllerTest {
    @Mock private lateinit var getMonthlyIncomeTransactionListService: GetMonthlyIncomeTransactionListService
    @Mock private lateinit var getMonthlyExpenseTransactionListService: GetMonthlyExpenseTransactionListService
    @Mock private lateinit var getRecentTransactionListService: GetRecentTransactionListService
    @Mock private lateinit var createTransactionService: CreateTransactionService
    @Mock private lateinit var createPaymentNotificationTransactionService: CreatePaymentNotificationTransactionService
    @Mock private lateinit var deleteTransactionService: DeleteTransactionService
    @Mock private lateinit var updateTransactionCategoryService: UpdateTransactionCategoryService
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(TransactionController(getMonthlyIncomeTransactionListService, getMonthlyExpenseTransactionListService, getRecentTransactionListService, createTransactionService, createPaymentNotificationTransactionService, deleteTransactionService, updateTransactionCategoryService)).build()
    }

    @Test
    fun getRecentTransactionsReturnsRecentHistories() {
        Mockito.`when`(getRecentTransactionListService.execute(5)).thenReturn(RecentTransactionListResponse.from(listOf(expenseResponse("스타벅스 강남점", 4500, Category.FOOD))))
        mockMvc.perform(get("/transactions/recent").param("limit", "5"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.transactions[0].transactionId").value(TRANSACTION_ID.toString()))
            .andExpect(jsonPath("$.transactions[0].title").value("스타벅스 강남점"))
            .andExpect(jsonPath("$.transactions[0].amount").value(4500))
            .andExpect(jsonPath("$.transactions[0].type").value("EXPENSE"))
            .andExpect(jsonPath("$.transactions[0].category").value("FOOD"))
        Mockito.verify(getRecentTransactionListService).execute(5)
    }

    @Test
    fun createTransactionReturnsCreated() {
        mockMvc.perform(post("/transactions").contentType(MediaType.APPLICATION_JSON).content("""{"type":"EXPENSE","amount":4500,"title":"스타벅스 강남점","category":"FOOD","transactionDate":"2026-08-12"}"""))
            .andExpect(status().isCreated)
        Mockito.verify(createTransactionService).execute(org.example.bankramenserver.domain.transaction.presentation.dto.CreateTransactionRequest(Transaction.TransactionType.EXPENSE, 4500, "스타벅스 강남점", Category.FOOD, LocalDate.of(2026, 8, 12)))
    }

    @Test
    fun createPaymentNotificationTransactionReturnsCreated() {
        mockMvc.perform(post("/transactions/payment-notifications").contentType(MediaType.APPLICATION_JSON).content("""{"title":"스타벅스 강남점","amount":4500}"""))
            .andExpect(status().isCreated)
        Mockito.verify(createPaymentNotificationTransactionService).execute(org.example.bankramenserver.domain.transaction.presentation.dto.CreatePaymentNotificationTransactionRequest("스타벅스 강남점", 4500))
    }

    @Test
    fun createPaymentNotificationTransactionRejectsInvalidRequest() {
        mockMvc.perform(post("/transactions/payment-notifications").contentType(MediaType.APPLICATION_JSON).content("""{"title":"","amount":0}"""))
            .andExpect(status().isBadRequest)
        Mockito.verifyNoInteractions(createPaymentNotificationTransactionService)
    }

    @Test
    fun createTransactionRejectsInvalidRequest() {
        mockMvc.perform(post("/transactions").contentType(MediaType.APPLICATION_JSON).content("""{"type":"EXPENSE","amount":0,"title":"","category":"FOOD","transactionDate":"2026-08-12"}"""))
            .andExpect(status().isBadRequest)
        Mockito.verifyNoInteractions(createTransactionService)
    }

    @Test
    fun deleteTransactionReturnsNoContent() {
        mockMvc.perform(delete("/transactions/{transactionId}", TRANSACTION_ID)).andExpect(status().isNoContent)
        Mockito.verify(deleteTransactionService).execute(TRANSACTION_ID)
    }

    @Test
    fun updateTransactionCategoryReturnsUpdatedTransaction() {
        val response = expenseResponse("스타벅스 강남점", 4500, Category.CAFE_SNACK)
        val request = org.example.bankramenserver.domain.transaction.presentation.dto.UpdateTransactionCategoryRequest(Category.CAFE_SNACK)
        Mockito.`when`(updateTransactionCategoryService.execute(TRANSACTION_ID, request)).thenReturn(response)
        mockMvc.perform(patch("/transactions/{transactionId}/category", TRANSACTION_ID).contentType(MediaType.APPLICATION_JSON).content("""{"category":"CAFE_SNACK"}"""))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.transactionId").value(TRANSACTION_ID.toString()))
            .andExpect(jsonPath("$.category").value("CAFE_SNACK"))
            .andExpect(jsonPath("$.categoryName").value("카페/간식"))
        Mockito.verify(updateTransactionCategoryService).execute(TRANSACTION_ID, request)
    }

    @Test
    fun getMonthlyIncomeTransactionsReturnsIncomeHistories() {
        Mockito.`when`(getMonthlyIncomeTransactionListService.execute(2026, 8)).thenReturn(MonthlyIncomeTransactionListResponse.of(YearMonth.of(2026, 8), listOf(incomeResponse("월급", 3500000, Category.SALARY))))
        mockMvc.perform(get("/transactions/incomes").param("year", "2026").param("month", "8"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.yearMonth").value("2026-08"))
            .andExpect(jsonPath("$.incomes[0].title").value("월급"))
            .andExpect(jsonPath("$.incomes[0].transactionDate").value("2026-08-25"))
            .andExpect(jsonPath("$.incomes[0].transactionTime").value("09:30:00"))
            .andExpect(jsonPath("$.incomes[0].amount").value(3500000))
            .andExpect(jsonPath("$.incomes[0].type").value("INCOME"))
            .andExpect(jsonPath("$.incomes[0].category").value("SALARY"))
            .andExpect(jsonPath("$.incomes[0].categoryName").value("급여"))
        Mockito.verify(getMonthlyIncomeTransactionListService).execute(2026, 8)
    }

    @Test
    fun getMonthlyExpenseTransactionsReturnsExpenseHistories() {
        Mockito.`when`(getMonthlyExpenseTransactionListService.execute(2026, 8)).thenReturn(MonthlyExpenseTransactionListResponse.of(YearMonth.of(2026, 8), listOf(expenseResponse("스타벅스 강남점", 1400, Category.FOOD))))
        mockMvc.perform(get("/transactions/expenses").param("year", "2026").param("month", "8"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.yearMonth").value("2026-08"))
            .andExpect(jsonPath("$.expenses[0].title").value("스타벅스 강남점"))
            .andExpect(jsonPath("$.expenses[0].transactionDate").value("2026-08-12"))
            .andExpect(jsonPath("$.expenses[0].transactionTime").value("14:30:00"))
            .andExpect(jsonPath("$.expenses[0].amount").value(1400))
            .andExpect(jsonPath("$.expenses[0].type").value("EXPENSE"))
            .andExpect(jsonPath("$.expenses[0].category").value("FOOD"))
            .andExpect(jsonPath("$.expenses[0].categoryName").value("식비"))
        Mockito.verify(getMonthlyExpenseTransactionListService).execute(2026, 8)
    }

    private fun incomeResponse(title: String, amount: Long, category: Category) = TransactionHistoryResponse(TRANSACTION_ID, title, LocalDate.of(2026, 8, 25), LocalTime.of(9, 30), amount, Transaction.TransactionType.INCOME, category, category.displayName)
    private fun expenseResponse(title: String, amount: Long, category: Category) = TransactionHistoryResponse(TRANSACTION_ID, title, LocalDate.of(2026, 8, 12), LocalTime.of(14, 30), amount, Transaction.TransactionType.EXPENSE, category, category.displayName)

    private companion object {
        val TRANSACTION_ID: UUID = UUID.fromString("33333333-3333-3333-3333-333333333333")
    }
}
