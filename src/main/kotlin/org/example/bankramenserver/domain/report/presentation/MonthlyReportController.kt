package org.example.bankramenserver.domain.report.presentation

import org.example.bankramenserver.domain.report.presentation.dto.MonthlyAmountSummaryResponse
import org.example.bankramenserver.domain.report.presentation.dto.MonthlyCategoryExpenseListResponse
import org.example.bankramenserver.domain.report.service.GetMonthlyAmountSummaryService
import org.example.bankramenserver.domain.report.service.GetMonthlyCategoryExpenseListService
import org.example.bankramenserver.global.document.MonthlyReportApiDocument
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/reports/monthly"], produces = [MediaType.APPLICATION_JSON_VALUE])
class MonthlyReportController(
    private val getMonthlyAmountSummaryService: GetMonthlyAmountSummaryService,
    private val getMonthlyCategoryExpenseListService: GetMonthlyCategoryExpenseListService,
) : MonthlyReportApiDocument {
    @GetMapping("/summary")
    override fun getMonthlyAmountSummary(@RequestParam year: Int, @RequestParam month: Int): MonthlyAmountSummaryResponse = getMonthlyAmountSummaryService.execute(year, month)

    @GetMapping("/categories")
    override fun getMonthlyCategoryExpenses(@RequestParam year: Int, @RequestParam month: Int): MonthlyCategoryExpenseListResponse = getMonthlyCategoryExpenseListService.execute(year, month)
}
