package org.example.bankramenserver.domain.transaction.domain.repository

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import org.example.bankramenserver.domain.transaction.domain.QTransaction
import org.example.bankramenserver.domain.transaction.domain.Transaction
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
class TransactionRepositoryImpl(private val jpaQueryFactory: JPAQueryFactory) : TransactionRepositoryCustom {
    private val transaction = QTransaction.transaction

    override fun findTransactionHistories(userId: UUID?, transactionType: Transaction.TransactionType?, startDate: LocalDate?, endDate: LocalDate?): List<TransactionHistoryRow> =
        jpaQueryFactory.select(Projections.constructor(TransactionHistoryRow::class.java, transaction.id, transaction.description, transaction.transactionDate, transaction.createdAt, transaction.amount, transaction.type, transaction.category))
            .from(transaction)
            .where(transaction.user.id.eq(userId), transaction.type.eq(transactionType), transaction.transactionDate.between(startDate, endDate))
            .orderBy(transaction.transactionDate.desc(), transaction.createdAt.desc(), transaction.id.desc())
            .fetch()

    override fun findRecentTransactionHistories(userId: UUID?, limit: Int): List<TransactionHistoryRow> =
        jpaQueryFactory.select(Projections.constructor(TransactionHistoryRow::class.java, transaction.id, transaction.description, transaction.transactionDate, transaction.createdAt, transaction.amount, transaction.type, transaction.category))
            .from(transaction)
            .where(transaction.user.id.eq(userId))
            .orderBy(transaction.transactionDate.desc(), transaction.createdAt.desc(), transaction.id.desc())
            .limit(limit.toLong())
            .fetch()

    override fun findUserIdsHavingTransactionsBetween(startDate: LocalDate?, endDate: LocalDate?): List<UUID> =
        jpaQueryFactory.select(transaction.user.id).distinct().from(transaction).where(transaction.transactionDate.between(startDate, endDate)).fetch()

    override fun existsSameExpenseTransactionBetween(userId: UUID?, description: String?, amount: Long?, startDate: LocalDate?, endDate: LocalDate?): Boolean =
        jpaQueryFactory.selectOne().from(transaction)
            .where(transaction.user.id.eq(userId), transaction.type.eq(Transaction.TransactionType.EXPENSE), transaction.description.eq(description), transaction.amount.eq(amount), transaction.transactionDate.between(startDate, endDate))
            .fetchFirst() != null
}
