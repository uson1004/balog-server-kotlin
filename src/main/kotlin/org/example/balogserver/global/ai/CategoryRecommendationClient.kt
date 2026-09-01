package org.example.balogserver.global.ai

import org.example.balogserver.domain.category.domain.Category
import java.util.Optional

interface CategoryRecommendationClient { fun recommend(paymentTitle: String): Optional<Category> }
