package org.example.bankramenserver.domain.report.presentation;

import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.report.presentation.dto.MonthlyAmountSummaryResponse;
import org.example.bankramenserver.domain.report.presentation.dto.MonthlyCategoryExpenseListResponse;
import org.example.bankramenserver.domain.report.service.GetMonthlyAmountSummaryService;
import org.example.bankramenserver.domain.report.service.GetMonthlyCategoryExpenseListService;
import org.example.bankramenserver.global.document.MonthlyReportApiDocument;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/reports/monthly", produces = MediaType.APPLICATION_JSON_VALUE)
public class MonthlyReportController implements MonthlyReportApiDocument {

    private final GetMonthlyAmountSummaryService getMonthlyAmountSummaryService;
    private final GetMonthlyCategoryExpenseListService getMonthlyCategoryExpenseListService;

    @Override
    @GetMapping("/summary")
    public MonthlyAmountSummaryResponse getMonthlyAmountSummary(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return getMonthlyAmountSummaryService.execute(year, month);
    }

    @Override
    @GetMapping("/categories")
    public MonthlyCategoryExpenseListResponse getMonthlyCategoryExpenses(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return getMonthlyCategoryExpenseListService.execute(year, month);
    }
}
