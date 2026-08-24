package org.example.bankramenserver.domain.transaction.domain.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.transaction.domain.QTransaction;
import org.example.bankramenserver.domain.transaction.domain.Transaction;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    private static final QTransaction transaction = QTransaction.transaction;

    @Override
    public List<TransactionHistoryRow> findTransactionHistories(
            UUID userId,
            Transaction.TransactionType transactionType,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        TransactionHistoryRow.class,
                        transaction.id,
                        transaction.description,
                        transaction.transactionDate,
                        transaction.createdAt,
                        transaction.amount,
                        transaction.type,
                        transaction.category
                ))
                .from(transaction)
                .where(
                        transaction.user.id.eq(userId),
                        transaction.type.eq(transactionType),
                        transaction.transactionDate.between(startDate, endDate)
                )
                .orderBy(
                        transaction.transactionDate.desc(),
                        transaction.createdAt.desc(),
                        transaction.id.desc()
                )
                .fetch();
    }

    @Override
    public List<TransactionHistoryRow> findRecentTransactionHistories(
            UUID userId,
            int limit
    ) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        TransactionHistoryRow.class,
                        transaction.id,
                        transaction.description,
                        transaction.transactionDate,
                        transaction.createdAt,
                        transaction.amount,
                        transaction.type,
                        transaction.category
                ))
                .from(transaction)
                .where(transaction.user.id.eq(userId))
                .orderBy(
                        transaction.transactionDate.desc(),
                        transaction.createdAt.desc(),
                        transaction.id.desc()
                )
                .limit(limit)
                .fetch();
    }

    @Override
    public List<UUID> findUserIdsHavingTransactionsBetween(
            LocalDate startDate,
            LocalDate endDate
    ) {
        return jpaQueryFactory
                .select(transaction.user.id)
                .distinct()
                .from(transaction)
                .where(transaction.transactionDate.between(startDate, endDate))
                .fetch();
    }

    @Override
    public boolean existsSameExpenseTransactionBetween(
            UUID userId,
            String description,
            Long amount,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Integer result = jpaQueryFactory
                .selectOne()
                .from(transaction)
                .where(
                        transaction.user.id.eq(userId),
                        transaction.type.eq(Transaction.TransactionType.EXPENSE),
                        transaction.description.eq(description),
                        transaction.amount.eq(amount),
                        transaction.transactionDate.between(startDate, endDate)
                )
                .fetchFirst();

        return result != null;
    }
}