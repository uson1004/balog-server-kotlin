package org.example.bankramenserver.domain.report.domain.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
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
public class MonthlyReportRepositoryImpl implements MonthlyReportRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    private static final QTransaction transaction = QTransaction.transaction;

    @Override
    public AmountSummaryRow findAmountSummary(
            UUID userId,
            LocalDate currentStartDate,
            LocalDate currentEndDate,
            LocalDate previousStartDate,
            LocalDate previousEndDate
    ) {
        NumberExpression<Long> currentExpense = sumAmountWhen(
                transaction.type.eq(Transaction.TransactionType.EXPENSE)
                        .and(transaction.transactionDate.between(currentStartDate, currentEndDate))
        );
        NumberExpression<Long> previousExpense = sumAmountWhen(
                transaction.type.eq(Transaction.TransactionType.EXPENSE)
                        .and(transaction.transactionDate.between(previousStartDate, previousEndDate))
        );
        NumberExpression<Long> previousExpenseCount = countWhen(
                transaction.type.eq(Transaction.TransactionType.EXPENSE)
                        .and(transaction.transactionDate.between(previousStartDate, previousEndDate))
        );
        NumberExpression<Long> currentIncome = sumAmountWhen(
                transaction.type.eq(Transaction.TransactionType.INCOME)
                        .and(transaction.transactionDate.between(currentStartDate, currentEndDate))
        );
        NumberExpression<Long> previousIncome = sumAmountWhen(
                transaction.type.eq(Transaction.TransactionType.INCOME)
                        .and(transaction.transactionDate.between(previousStartDate, previousEndDate))
        );
        NumberExpression<Long> previousIncomeCount = countWhen(
                transaction.type.eq(Transaction.TransactionType.INCOME)
                        .and(transaction.transactionDate.between(previousStartDate, previousEndDate))
        );

        Tuple tuple = jpaQueryFactory
                .select(
                        currentExpense,
                        previousExpense,
                        previousExpenseCount,
                        currentIncome,
                        previousIncome,
                        previousIncomeCount
                )
                .from(transaction)
                .where(
                        transaction.user.id.eq(userId),
                        transaction.type.in(Transaction.TransactionType.EXPENSE, Transaction.TransactionType.INCOME),
                        transaction.transactionDate.between(previousStartDate, currentEndDate)
                )
                .fetchOne();

        if (tuple == null) {
            return AmountSummaryRow.builder()
                    .currentExpense(0L)
                    .previousExpense(0L)
                    .previousExpenseCount(0L)
                    .currentIncome(0L)
                    .previousIncome(0L)
                    .previousIncomeCount(0L)
                    .build();
        }

        return AmountSummaryRow.builder()
                .currentExpense(tuple.get(currentExpense))
                .previousExpense(tuple.get(previousExpense))
                .previousExpenseCount(tuple.get(previousExpenseCount))
                .currentIncome(tuple.get(currentIncome))
                .previousIncome(tuple.get(previousIncome))
                .previousIncomeCount(tuple.get(previousIncomeCount))
                .build();
    }

    @Override
    public List<CategoryExpenseRow> findCategoryExpenseComparisons(
            UUID userId,
            LocalDate currentStartDate,
            LocalDate currentEndDate,
            LocalDate previousStartDate,
            LocalDate previousEndDate
    ) {
        NumberExpression<Long> currentExpense = sumAmountWhen(
                transaction.transactionDate.between(currentStartDate, currentEndDate)
        );
        NumberExpression<Long> previousExpense = sumAmountWhen(
                transaction.transactionDate.between(previousStartDate, previousEndDate)
        );

        return jpaQueryFactory
                .select(Projections.constructor(
                        CategoryExpenseRow.class,
                        transaction.category,
                        currentExpense,
                        previousExpense
                ))
                .from(transaction)
                .where(
                        transaction.user.id.eq(userId),
                        transaction.type.eq(Transaction.TransactionType.EXPENSE),
                        transaction.transactionDate.between(previousStartDate, currentEndDate)
                )
                .groupBy(transaction.category)
                .having(currentExpense.gt(0L))
                .orderBy(currentExpense.desc())
                .fetch();
    }

    private NumberExpression<Long> sumAmountWhen(BooleanExpression condition) {
        return new CaseBuilder()
                .when(condition)
                .then(transaction.amount)
                .otherwise(0L)
                .sum()
                .coalesce(0L);
    }

    private NumberExpression<Long> countWhen(BooleanExpression condition) {
        return new CaseBuilder()
                .when(condition)
                .then(1L)
                .otherwise(0L)
                .sum()
                .coalesce(0L);
    }
}
