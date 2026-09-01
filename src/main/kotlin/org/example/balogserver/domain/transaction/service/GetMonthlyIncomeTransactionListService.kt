package org.example.balogserver.domain.transaction.service

import org.example.balogserver.domain.transaction.domain.Transaction
import org.example.balogserver.domain.transaction.domain.repository.TransactionRepository
import org.example.balogserver.domain.transaction.presentation.dto.MonthlyIncomeTransactionListResponse
import org.example.balogserver.domain.transaction.presentation.dto.TransactionHistoryResponse
import org.example.balogserver.domain.user.facade.UserFacade
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.YearMonth

@Service
class GetMonthlyIncomeTransactionListService(private val userFacade: UserFacade, private val transactionRepository: TransactionRepository) {
    @Transactional(readOnly = true)
    fun execute(year: Int, month: Int): MonthlyIncomeTransactionListResponse {
        val yearMonth = YearMonth.of(year, month)
        return MonthlyIncomeTransactionListResponse.of(yearMonth, transactionRepository.findTransactionHistories(requireNotNull(userFacade.currentUser.id), Transaction.TransactionType.INCOME, yearMonth.atDay(1), yearMonth.atEndOfMonth()).map(TransactionHistoryResponse::from))
    }
}
