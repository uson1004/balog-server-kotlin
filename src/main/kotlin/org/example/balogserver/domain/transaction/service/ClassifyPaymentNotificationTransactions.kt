package org.example.balogserver.domain.transaction.service

import org.example.balogserver.domain.transaction.domain.Transaction
import org.example.balogserver.domain.transaction.domain.repository.TransactionRepository
import org.example.balogserver.global.ai.CategoryRecommendationClient
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ClassifyPaymentNotificationTransactions(
    private val transactionRepository: TransactionRepository,
    private val categoryRecommendationClient: CategoryRecommendationClient,
) {
    // ponytail: polling can repeat an AI call across multiple app instances; add DB leases only when the worker is scaled out.
    @Scheduled(fixedDelayString = "\${payment-notification.classification-interval-ms:300000}")
    fun execute() {
        transactionRepository.findTop50BySourceAndCategorySourceOrderByCreatedAtAsc(
            Transaction.TransactionSource.NOTIFICATION,
            Transaction.CategorySource.NOTIFICATION_PENDING,
        ).forEach { transaction ->
            val category = transaction.description?.let(categoryRecommendationClient::recommend)?.orElse(null) ?: return@forEach
            transactionRepository.updateNotificationCategoryIfPending(
                requireNotNull(transaction.id),
                category,
                Transaction.CategorySource.NOTIFICATION_PENDING,
                Transaction.CategorySource.AI,
            )
        }
    }
}
