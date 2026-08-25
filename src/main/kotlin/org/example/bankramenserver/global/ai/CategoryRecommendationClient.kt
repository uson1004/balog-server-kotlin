package org.example.bankramenserver.global.ai

import org.example.bankramenserver.domain.category.domain.Category
import java.util.Optional

interface CategoryRecommendationClient { fun recommend(paymentTitle: String): Optional<Category> }
