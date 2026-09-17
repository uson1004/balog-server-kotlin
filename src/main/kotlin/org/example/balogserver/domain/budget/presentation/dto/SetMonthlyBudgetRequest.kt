package org.example.balogserver.domain.budget.presentation.dto

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.Digits
import java.math.BigDecimal
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero

data class SetMonthlyBudgetRequest(
    @field:NotNull @field:PositiveOrZero @field:Digits(integer = 19, fraction = 0)
    @field:DecimalMax("9223372036854775807") val amount: BigDecimal?,
)
