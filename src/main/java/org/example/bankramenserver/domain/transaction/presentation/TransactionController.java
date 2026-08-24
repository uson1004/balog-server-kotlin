package org.example.bankramenserver.domain.transaction.presentation;

import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.transaction.presentation.dto.CreatePaymentNotificationTransactionRequest;
import org.example.bankramenserver.domain.transaction.presentation.dto.CreateTransactionRequest;
import org.example.bankramenserver.domain.transaction.presentation.dto.MonthlyExpenseTransactionListResponse;
import org.example.bankramenserver.domain.transaction.presentation.dto.MonthlyIncomeTransactionListResponse;
import org.example.bankramenserver.domain.transaction.presentation.dto.RecentTransactionListResponse;
import org.example.bankramenserver.domain.transaction.presentation.dto.TransactionHistoryResponse;
import org.example.bankramenserver.domain.transaction.presentation.dto.UpdateTransactionCategoryRequest;
import org.example.bankramenserver.domain.transaction.service.CreatePaymentNotificationTransactionService;
import org.example.bankramenserver.domain.transaction.service.CreateTransactionService;
import org.example.bankramenserver.domain.transaction.service.DeleteTransactionService;
import org.example.bankramenserver.domain.transaction.service.GetRecentTransactionListService;
import org.example.bankramenserver.domain.transaction.service.GetMonthlyExpenseTransactionListService;
import org.example.bankramenserver.domain.transaction.service.GetMonthlyIncomeTransactionListService;
import org.example.bankramenserver.domain.transaction.service.UpdateTransactionCategoryService;
import org.example.bankramenserver.global.document.TransactionApiDocument;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
public class TransactionController implements TransactionApiDocument {

    private final GetMonthlyIncomeTransactionListService getMonthlyIncomeTransactionListService;
    private final GetMonthlyExpenseTransactionListService getMonthlyExpenseTransactionListService;
    private final GetRecentTransactionListService getRecentTransactionListService;
    private final CreateTransactionService createTransactionService;
    private final CreatePaymentNotificationTransactionService createPaymentNotificationTransactionService;
    private final DeleteTransactionService deleteTransactionService;
    private final UpdateTransactionCategoryService updateTransactionCategoryService;

    @Override
    @GetMapping("/recent")
    public RecentTransactionListResponse getRecentTransactions(@RequestParam(defaultValue = "5") int limit) {
        return getRecentTransactionListService.execute(limit);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createTransaction(@RequestBody CreateTransactionRequest request) {
        createTransactionService.execute(request);
    }

    @Override
    @PostMapping("/payment-notifications")
    @ResponseStatus(HttpStatus.CREATED)
    public void createPaymentNotificationTransaction(@RequestBody CreatePaymentNotificationTransactionRequest request) {
        createPaymentNotificationTransactionService.execute(request);
    }

    @Override
    @DeleteMapping("/{transactionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTransaction(
            @PathVariable UUID transactionId
    ) {
        deleteTransactionService.execute(transactionId);
    }

    @Override
    @PatchMapping("/{transactionId}/category")
    public TransactionHistoryResponse updateTransactionCategory(
            @PathVariable UUID transactionId,
            @RequestBody UpdateTransactionCategoryRequest request
    ) {
        return updateTransactionCategoryService.execute(transactionId, request);
    }

    @Override
    @GetMapping("/incomes")
    public MonthlyIncomeTransactionListResponse getMonthlyIncomeTransactions(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return getMonthlyIncomeTransactionListService.execute(year, month);
    }

    @Override
    @GetMapping("/expenses")
    public MonthlyExpenseTransactionListResponse getMonthlyExpenseTransactions(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return getMonthlyExpenseTransactionListService.execute(year, month);
    }
}
