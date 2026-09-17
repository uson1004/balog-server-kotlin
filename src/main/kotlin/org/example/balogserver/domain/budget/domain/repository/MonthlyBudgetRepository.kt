package org.example.balogserver.domain.budget.domain.repository

import org.example.balogserver.domain.budget.domain.MonthlyBudget
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface MonthlyBudgetRepository : JpaRepository<MonthlyBudget, UUID> {
    @Query("select b.amount from MonthlyBudget b where b.userId = :userId and b.year = :year and b.month = :month")
    fun findAmount(@Param("userId") userId: UUID, @Param("year") year: Int, @Param("month") month: Int): Long?

    @Modifying
    @Query(value = """
        INSERT INTO monthly_budgets (id, user_id, year, month, amount)
        VALUES (:id, :userId, :year, :month, :amount)
        ON DUPLICATE KEY UPDATE amount = :amount
    """, nativeQuery = true)
    fun upsert(
        @Param("id") id: UUID,
        @Param("userId") userId: UUID,
        @Param("year") year: Int,
        @Param("month") month: Int,
        @Param("amount") amount: Long,
    )
}
