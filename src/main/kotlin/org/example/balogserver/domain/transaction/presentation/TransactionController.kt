package org.example.balogserver.domain.transaction.presentation

import jakarta.validation.Valid
import org.example.balogserver.domain.transaction.presentation.dto.CreatePaymentNotificationTransactionRequest
import org.example.balogserver.domain.transaction.presentation.dto.CreateTransactionRequest
import org.example.balogserver.domain.transaction.presentation.dto.MonthlyExpenseTransactionListResponse
import org.example.balogserver.domain.transaction.presentation.dto.MonthlyIncomeTransactionListResponse
import org.example.balogserver.domain.transaction.presentation.dto.RecentTransactionListResponse
import org.example.balogserver.domain.transaction.presentation.dto.TransactionHistoryResponse
import org.example.balogserver.domain.transaction.presentation.dto.UpdateTransactionCategoryRequest
import org.example.balogserver.domain.transaction.service.CreatePaymentNotificationTransactionService
import org.example.balogserver.domain.transaction.service.CreateTransactionService
import org.example.balogserver.domain.transaction.service.DeleteTransactionService
import org.example.balogserver.domain.transaction.service.GetMonthlyExpenseTransactionListService
import org.example.balogserver.domain.transaction.service.GetMonthlyIncomeTransactionListService
import org.example.balogserver.domain.transaction.service.GetRecentTransactionListService
import org.example.balogserver.domain.transaction.service.UpdateTransactionCategoryService
import org.example.balogserver.global.document.TransactionApiDocument
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping(value = ["/transactions"], produces = [MediaType.APPLICATION_JSON_VALUE])
class TransactionController(
    private val getMonthlyIncomeTransactionListService: GetMonthlyIncomeTransactionListService,
    private val getMonthlyExpenseTransactionListService: GetMonthlyExpenseTransactionListService,
    private val getRecentTransactionListService: GetRecentTransactionListService,
    private val createTransactionService: CreateTransactionService,
    private val createPaymentNotificationTransactionService: CreatePaymentNotificationTransactionService,
    private val deleteTransactionService: DeleteTransactionService,
    private val updateTransactionCategoryService: UpdateTransactionCategoryService,
) : TransactionApiDocument {
    @GetMapping("/recent")
    override fun getRecentTransactions(@RequestParam(defaultValue = "5") limit: Int): RecentTransactionListResponse = getRecentTransactionListService.execute(limit)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun createTransaction(@Valid @RequestBody request: CreateTransactionRequest) = createTransactionService.execute(request)

    @PostMapping("/payment-notifications")
    @ResponseStatus(HttpStatus.CREATED)
    override fun createPaymentNotificationTransaction(@Valid @RequestBody request: CreatePaymentNotificationTransactionRequest) = createPaymentNotificationTransactionService.execute(request)

    @DeleteMapping("/{transactionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    override fun deleteTransaction(@PathVariable transactionId: UUID) = deleteTransactionService.execute(transactionId)

    @PatchMapping("/{transactionId}/category")
    override fun updateTransactionCategory(@PathVariable transactionId: UUID, @Valid @RequestBody request: UpdateTransactionCategoryRequest): TransactionHistoryResponse = updateTransactionCategoryService.execute(transactionId, request)

    @GetMapping("/incomes")
    override fun getMonthlyIncomeTransactions(@RequestParam year: Int, @RequestParam month: Int): MonthlyIncomeTransactionListResponse = getMonthlyIncomeTransactionListService.execute(year, month)

    @GetMapping("/expenses")
    override fun getMonthlyExpenseTransactions(@RequestParam year: Int, @RequestParam month: Int): MonthlyExpenseTransactionListResponse = getMonthlyExpenseTransactionListService.execute(year, month)
}
