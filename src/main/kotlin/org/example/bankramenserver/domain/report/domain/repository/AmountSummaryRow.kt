package org.example.bankramenserver.domain.report.domain.repository

class AmountSummaryRow(
    private val currentExpenseValue: Long?,
    private val previousExpenseValue: Long?,
    private val previousExpenseCountValue: Long?,
    private val currentIncomeValue: Long?,
    private val previousIncomeValue: Long?,
    private val previousIncomeCountValue: Long?,
) {
    val currentExpense get() = currentExpenseValue
    val previousExpense get() = previousExpenseValue
    val previousExpenseCount get() = previousExpenseCountValue
    val currentIncome get() = currentIncomeValue
    val previousIncome get() = previousIncomeValue
    val previousIncomeCount get() = previousIncomeCountValue
    fun currentExpense() = currentExpenseValue
    fun previousExpense() = previousExpenseValue
    fun previousExpenseCount() = previousExpenseCountValue
    fun currentIncome() = currentIncomeValue
    fun previousIncome() = previousIncomeValue
    fun previousIncomeCount() = previousIncomeCountValue

    companion object { @JvmStatic fun builder() = AmountSummaryRowBuilder() }
    class AmountSummaryRowBuilder {
        private var currentExpense: Long? = null
        private var previousExpense: Long? = null
        private var previousExpenseCount: Long? = null
        private var currentIncome: Long? = null
        private var previousIncome: Long? = null
        private var previousIncomeCount: Long? = null
        fun currentExpense(value: Long?) = apply { currentExpense = value }
        fun previousExpense(value: Long?) = apply { previousExpense = value }
        fun previousExpenseCount(value: Long?) = apply { previousExpenseCount = value }
        fun currentIncome(value: Long?) = apply { currentIncome = value }
        fun previousIncome(value: Long?) = apply { previousIncome = value }
        fun previousIncomeCount(value: Long?) = apply { previousIncomeCount = value }
        fun build() = AmountSummaryRow(currentExpense, previousExpense, previousExpenseCount, currentIncome, previousIncome, previousIncomeCount)
    }
}
