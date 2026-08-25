package org.example.bankramenserver.domain.report.domain.repository

import com.querydsl.core.Tuple
import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.core.types.dsl.NumberExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import org.example.bankramenserver.domain.transaction.domain.QTransaction
import org.example.bankramenserver.domain.transaction.domain.Transaction
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
class MonthlyReportRepositoryImpl(private val jpaQueryFactory: JPAQueryFactory) : MonthlyReportRepositoryCustom {
    private val transaction = QTransaction.transaction

    override fun findAmountSummary(userId: UUID?, currentStartDate: LocalDate?, currentEndDate: LocalDate?, previousStartDate: LocalDate?, previousEndDate: LocalDate?): AmountSummaryRow {
        val currentExpense = sumAmountWhen(transaction.type.eq(Transaction.TransactionType.EXPENSE).and(transaction.transactionDate.between(currentStartDate, currentEndDate)))
        val previousExpense = sumAmountWhen(transaction.type.eq(Transaction.TransactionType.EXPENSE).and(transaction.transactionDate.between(previousStartDate, previousEndDate)))
        val previousExpenseCount = countWhen(transaction.type.eq(Transaction.TransactionType.EXPENSE).and(transaction.transactionDate.between(previousStartDate, previousEndDate)))
        val currentIncome = sumAmountWhen(transaction.type.eq(Transaction.TransactionType.INCOME).and(transaction.transactionDate.between(currentStartDate, currentEndDate)))
        val previousIncome = sumAmountWhen(transaction.type.eq(Transaction.TransactionType.INCOME).and(transaction.transactionDate.between(previousStartDate, previousEndDate)))
        val previousIncomeCount = countWhen(transaction.type.eq(Transaction.TransactionType.INCOME).and(transaction.transactionDate.between(previousStartDate, previousEndDate)))
        val tuple: Tuple? = jpaQueryFactory.select(currentExpense, previousExpense, previousExpenseCount, currentIncome, previousIncome, previousIncomeCount)
            .from(transaction)
            .where(transaction.user.id.eq(userId), transaction.type.`in`(Transaction.TransactionType.EXPENSE, Transaction.TransactionType.INCOME), transaction.transactionDate.between(previousStartDate, currentEndDate))
            .fetchOne()

        return if (tuple == null) AmountSummaryRow.builder().currentExpense(0L).previousExpense(0L).previousExpenseCount(0L).currentIncome(0L).previousIncome(0L).previousIncomeCount(0L).build()
        else AmountSummaryRow.builder().currentExpense(tuple.get(currentExpense)).previousExpense(tuple.get(previousExpense)).previousExpenseCount(tuple.get(previousExpenseCount)).currentIncome(tuple.get(currentIncome)).previousIncome(tuple.get(previousIncome)).previousIncomeCount(tuple.get(previousIncomeCount)).build()
    }

    override fun findCategoryExpenseComparisons(userId: UUID?, currentStartDate: LocalDate?, currentEndDate: LocalDate?, previousStartDate: LocalDate?, previousEndDate: LocalDate?): List<CategoryExpenseRow> {
        val currentExpense = sumAmountWhen(transaction.transactionDate.between(currentStartDate, currentEndDate))
        val previousExpense = sumAmountWhen(transaction.transactionDate.between(previousStartDate, previousEndDate))
        return jpaQueryFactory.select(Projections.constructor(CategoryExpenseRow::class.java, transaction.category, currentExpense, previousExpense))
            .from(transaction)
            .where(transaction.user.id.eq(userId), transaction.type.eq(Transaction.TransactionType.EXPENSE), transaction.transactionDate.between(previousStartDate, currentEndDate))
            .groupBy(transaction.category)
            .having(currentExpense.gt(0L))
            .orderBy(currentExpense.desc())
            .fetch()
    }

    private fun sumAmountWhen(condition: BooleanExpression): NumberExpression<Long> = CaseBuilder().`when`(condition).then(transaction.amount).otherwise(0L).sum().coalesce(0L)
    private fun countWhen(condition: BooleanExpression): NumberExpression<Long> = CaseBuilder().`when`(condition).then(1L).otherwise(0L).sum().coalesce(0L)
}
