package org.example.balogserver.global.config

import org.example.balogserver.domain.category.domain.Category
import org.example.balogserver.domain.transaction.domain.Transaction
import org.example.balogserver.domain.transaction.presentation.dto.CreateTransactionRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AppConfigObjectMapperTest {
    @Test
    fun deserializesCreateTransactionRequest() {
        val request = AppConfig().objectMapper().readValue(
            """{"amount":1000,"category":"FOOD","title":"라ㅏㅇ","transactionDate":"2026-08-26","type":"EXPENSE"}""",
            CreateTransactionRequest::class.java,
        )

        assertEquals(1000, request.amount)
        assertEquals(Category.FOOD, request.category)
        assertEquals("라ㅏㅇ", request.title)
        assertEquals(LocalDate.of(2026, 8, 26), request.transactionDate)
        assertEquals(Transaction.TransactionType.EXPENSE, request.type)
    }
}
