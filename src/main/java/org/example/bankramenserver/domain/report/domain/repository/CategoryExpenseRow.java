package org.example.bankramenserver.domain.report.domain.repository;

import lombok.Builder;
import org.example.bankramenserver.domain.category.domain.Category;

@Builder
public record CategoryExpenseRow(Category category, Long currentExpense, Long previousExpense) {

}
