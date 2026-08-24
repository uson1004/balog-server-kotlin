package org.example.bankramenserver.global.ai;

import org.example.bankramenserver.domain.category.domain.Category;

import java.util.Optional;

public interface CategoryRecommendationClient {

    Optional<Category> recommend(String paymentTitle);
}
