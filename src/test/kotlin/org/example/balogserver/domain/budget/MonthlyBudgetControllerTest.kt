package org.example.balogserver.domain.budget

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.example.balogserver.domain.budget.domain.repository.MonthlyBudgetRepository
import org.example.balogserver.domain.budget.presentation.MonthlyBudgetController
import org.example.balogserver.domain.budget.service.MonthlyBudgetService
import org.example.balogserver.domain.transaction.domain.repository.TransactionHistoryRow
import org.example.balogserver.domain.transaction.domain.Transaction
import org.example.balogserver.domain.transaction.domain.repository.TransactionRepository
import org.example.balogserver.domain.user.facade.UserFacade
import org.example.balogserver.global.error.GlobalExceptionHandler
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mockito.*
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

class MonthlyBudgetControllerTest {
    private val user = mock(UserFacade::class.java)
    private val budgets = mock(MonthlyBudgetRepository::class.java)
    private val reports = mock(TransactionRepository::class.java)
    private val userId = UUID.randomUUID()
    private lateinit var mvc: MockMvc

    @BeforeEach
    fun setup() {
        val clock = Clock.fixed(Instant.parse("2026-09-07T15:00:00Z"), ZoneId.of("Asia/Seoul"))
        val service = MonthlyBudgetService(user, budgets, reports, clock)
        mvc = MockMvcBuilders.standaloneSetup(MonthlyBudgetController(service))
            .setControllerAdvice(GlobalExceptionHandler())
            .setMessageConverters(MappingJackson2HttpMessageConverter(jacksonObjectMapper().findAndRegisterModules()))
            .build()
    }

    private fun spending() {
        `when`(user.currentUserId).thenReturn(userId)
        `when`(reports.findTransactionHistories(userId, Transaction.TransactionType.EXPENSE, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)))
            .thenReturn(listOf(TransactionHistoryRow.builder().transactionDate(LocalDate.of(2026, 9, 1)).amount(200000).build()))
    }

    @Test
    fun getUsesAuthenticatedUserAndReturnsCompleteSummary() {
        spending()
        `when`(budgets.findAmount(userId, 2026, 9)).thenReturn(1000000)
        mvc.perform(get("/budgets/monthly?year=2026&month=9"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.yearMonth").value("2026-09"))
            .andExpect(jsonPath("$.budgetAmount").value(1000000))
            .andExpect(jsonPath("$.totalExpense").value(200000))
            .andExpect(jsonPath("$.remainingAmount").value(800000))
            .andExpect(jsonPath("$.remainingDays").value(23))
            .andExpect(jsonPath("$.dailyAvailableAmount").value(34782))
            .andExpect(jsonPath("$.dailyTargetAmount").value(33333))
            .andExpect(jsonPath("$.days.length()").value(30))
            .andExpect(jsonPath("$.days[0].date").value("2026-09-01"))
            .andExpect(jsonPath("$.days[0].status").value("FAIL"))
            .andExpect(jsonPath("$.days[1].status").value("SUCCESS"))
            .andExpect(jsonPath("$.days[7].status").value("PENDING"))
        verify(reports).findTransactionHistories(userId, Transaction.TransactionType.EXPENSE, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30))
        verify(budgets).findAmount(userId, 2026, 9)
    }

    @Test
    fun getRejectsExpenseTotalThatExceedsLongRange() {
        `when`(user.currentUserId).thenReturn(userId)
        `when`(reports.findTransactionHistories(userId, Transaction.TransactionType.EXPENSE, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)))
            .thenReturn(listOf(
                TransactionHistoryRow.builder().transactionDate(LocalDate.of(2026, 9, 1)).amount(Long.MAX_VALUE).build(),
                TransactionHistoryRow.builder().transactionDate(LocalDate.of(2026, 9, 1)).amount(1).build(),
            ))

        mvc.perform(get("/budgets/monthly?year=2026&month=9"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
    }

    @Test
    fun getRejectsMonthlyExpenseTotalThatExceedsLongRange() {
        `when`(user.currentUserId).thenReturn(userId)
        `when`(reports.findTransactionHistories(userId, Transaction.TransactionType.EXPENSE, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)))
            .thenReturn(listOf(
                TransactionHistoryRow.builder().transactionDate(LocalDate.of(2026, 9, 1)).amount(Long.MAX_VALUE).build(),
                TransactionHistoryRow.builder().transactionDate(LocalDate.of(2026, 9, 2)).amount(1).build(),
            ))

        mvc.perform(get("/budgets/monthly?year=2026&month=9"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
    }

    @Test
    fun putWritesOnlyPrincipalBudgetAndReturnsIt() {
        spending()
        `when`(budgets.findAmount(userId, 2026, 9)).thenReturn(1000000)
        mvc.perform(put("/budgets/monthly?year=2026&month=9").contentType(MediaType.APPLICATION_JSON).content("""{"amount":1000000}"""))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.budgetAmount").value(1000000))
        val writes = mockingDetails(budgets).invocations.filter { it.method.name == "upsert" }
        assertThat(writes).hasSize(1)
        assertThat(writes.single().arguments.drop(1)).containsExactly(userId, 2026, 9, 1000000L)
    }

    @ParameterizedTest
    @ValueSource(strings = ["{\"amount\":1.5}", "{}", "{\"amount\":null}", "{\"amount\":-1}", "{\"amount\":9223372036854775808}", "{\"amount\":\"bad\"}"])
    fun invalidAmountsReturn400WithoutWriting(body: String) {
        mvc.perform(put("/budgets/monthly?year=2026&month=9").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
        verifyNoInteractions(budgets, reports)
    }

    @ParameterizedTest
    @ValueSource(strings = ["year=1999&month=9", "year=3000&month=9", "year=2026&month=0", "year=2026&month=13", "year=x&month=9", "year=2026"])
    fun invalidPeriodReturns400WithoutQuerying(query: String) {
        mvc.perform(get("/budgets/monthly?$query"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
        verifyNoInteractions(budgets, reports)
    }
}
