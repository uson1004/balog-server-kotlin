package org.example.bankramenserver.domain.transaction.service

import org.example.bankramenserver.domain.transaction.domain.Transaction
import org.example.bankramenserver.domain.transaction.domain.repository.TransactionRepository
import org.example.bankramenserver.domain.transaction.presentation.dto.MonthlyExpenseTransactionListResponse
import org.example.bankramenserver.domain.transaction.presentation.dto.TransactionHistoryResponse
import org.example.bankramenserver.domain.user.facade.UserFacade
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.YearMonth

@Service
class GetMonthlyExpenseTransactionListService(private val userFacade: UserFacade, private val transactionRepository: TransactionRepository) {
    @Transactional(readOnly = true)
    fun execute(year: Int, month: Int): MonthlyExpenseTransactionListResponse {
        val yearMonth = YearMonth.of(year, month)
        return MonthlyExpenseTransactionListResponse.of(yearMonth, transactionRepository.findTransactionHistories(requireNotNull(userFacade.currentUser.id), Transaction.TransactionType.EXPENSE, yearMonth.atDay(1), yearMonth.atEndOfMonth()).map(TransactionHistoryResponse::from))
    }
}
