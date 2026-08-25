package org.example.bankramenserver.domain.report.domain.repository

import org.example.bankramenserver.domain.category.domain.Category

class CategoryExpenseRow(private val categoryValue: Category?, private val currentExpenseValue: Long?, private val previousExpenseValue: Long?) {
    val category get() = categoryValue!!
    val currentExpense get() = currentExpenseValue
    val previousExpense get() = previousExpenseValue
    fun category() = categoryValue
    fun currentExpense() = currentExpenseValue
    fun previousExpense() = previousExpenseValue

    companion object { @JvmStatic fun builder() = CategoryExpenseRowBuilder() }
    class CategoryExpenseRowBuilder {
        private var category: Category? = null
        private var currentExpense: Long? = null
        private var previousExpense: Long? = null
        fun category(value: Category?) = apply { category = value }
        fun currentExpense(value: Long?) = apply { currentExpense = value }
        fun previousExpense(value: Long?) = apply { previousExpense = value }
        fun build() = CategoryExpenseRow(category, currentExpense, previousExpense)
    }
}
