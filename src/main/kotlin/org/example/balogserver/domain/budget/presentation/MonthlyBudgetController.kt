package org.example.balogserver.domain.budget.presentation

import jakarta.validation.Valid
import org.example.balogserver.domain.budget.presentation.dto.SetMonthlyBudgetRequest
import org.example.balogserver.domain.budget.service.MonthlyBudgetService
import org.example.balogserver.global.document.MonthlyBudgetApiDocument
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/budgets/monthly", produces = [MediaType.APPLICATION_JSON_VALUE])
class MonthlyBudgetController(private val service: MonthlyBudgetService) : MonthlyBudgetApiDocument {
    @GetMapping
    override fun get(@RequestParam year: Int, @RequestParam month: Int) = service.get(year, month)

    @PutMapping
    override fun set(@RequestParam year: Int, @RequestParam month: Int, @Valid @RequestBody request: SetMonthlyBudgetRequest) =
        service.set(year, month, requireNotNull(request.amount).longValueExact())
}
