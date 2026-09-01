package org.example.balogserver.domain.recurring.service

import org.example.balogserver.domain.recurring.domain.RecurringPayment
import org.example.balogserver.domain.recurring.domain.RecurringPaymentTransaction
import org.example.balogserver.domain.recurring.domain.repository.RecurringPaymentRepository
import org.example.balogserver.domain.recurring.exception.DuplicateRecurringPaymentException
import org.example.balogserver.domain.recurring.exception.InvalidRecurringPaymentTransactionException
import org.example.balogserver.domain.recurring.presentation.dto.request.CreateRecurringPaymentRequest
import org.example.balogserver.domain.recurring.presentation.dto.response.CreateRecurringPaymentResponse
import org.example.balogserver.domain.transaction.domain.Transaction
import org.example.balogserver.domain.transaction.domain.repository.TransactionRepository
import org.example.balogserver.domain.transaction.exception.TransactionNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime
import java.util.UUID

@Service
class CreateRecurringPaymentService(
    private val recurringPaymentRepository: RecurringPaymentRepository,
    private val transactionRepository: TransactionRepository,
    private val clock: Clock,
) {
    @Transactional
    fun execute(userId: UUID, request: CreateRecurringPaymentRequest): CreateRecurringPaymentResponse {
        val transaction = transactionRepository.findByIdAndUser_Id(request.transactionId!!, userId).orElseThrow(::TransactionNotFoundException)
        if (transaction.type != Transaction.TransactionType.EXPENSE || transaction.description.isNullOrBlank()) throw InvalidRecurringPaymentTransactionException()
        if (recurringPaymentRepository.existsByUser_IdAndNameAndAmountAndCycleAndActiveTrue(userId, transaction.description, transaction.amount, request.cycle!!)) throw DuplicateRecurringPaymentException()
        val recurringPayment = RecurringPayment.builder().user(transaction.user).category(transaction.category).name(transaction.description).amount(transaction.amount)
            .cycle(request.cycle).billingDay(transaction.transactionDate.dayOfMonth).nextBillingDate(request.nextBillingDate!!.atStartOfDay())
            .registrationType(RecurringPayment.RegistrationType.MANUAL).confirmed(true).build()
        val matchedAt = LocalDateTime.now(clock)
        transactionRepository.findAllByUser_IdAndTypeAndDescriptionAndAmountOrderByTransactionDateDesc(userId, Transaction.TransactionType.EXPENSE, transaction.description, transaction.amount).forEach {
            recurringPayment.addTransaction(it, if (it.id == transaction.id) RecurringPaymentTransaction.MatchType.INITIAL else RecurringPaymentTransaction.MatchType.MANUAL_ADDED, matchedAt)
        }
        return CreateRecurringPaymentResponse(recurringPaymentRepository.save(recurringPayment).id!!)
    }
}
