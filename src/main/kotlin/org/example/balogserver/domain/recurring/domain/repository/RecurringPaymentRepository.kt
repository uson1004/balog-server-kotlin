package org.example.balogserver.domain.recurring.domain.repository

import org.example.balogserver.domain.recurring.domain.RecurringPayment
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime
import java.util.UUID

interface RecurringPaymentRepository : JpaRepository<RecurringPayment, UUID> {
    @EntityGraph(attributePaths = ["transactions", "transactions.transaction"])
    fun findAllByUser_IdAndActiveTrueOrderByNextBillingDateAsc(userId: UUID?): List<RecurringPayment>
    fun findAllByActiveTrueAndConfirmedTrueAndNextBillingDateBetween(start: LocalDateTime?, end: LocalDateTime?): List<RecurringPayment>
    fun findAllByUser_IdAndActiveTrueAndConfirmedTrueAndNextBillingDateBetween(userId: UUID?, start: LocalDateTime?, end: LocalDateTime?): List<RecurringPayment>
    fun existsByUser_IdAndNameAndAmountAndCycleAndActiveTrue(userId: UUID?, name: String?, amount: Long?, cycle: RecurringPayment.Cycle?): Boolean
    fun findAllByActiveTrueAndConfirmedTrueAndNextBillingDateGreaterThanEqualAndNextBillingDateLessThan(start: LocalDateTime?, end: LocalDateTime?): List<RecurringPayment>
}
