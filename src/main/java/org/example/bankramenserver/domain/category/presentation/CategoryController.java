package org.example.bankramenserver.domain.category.presentation;

import lombok.RequiredArgsConstructor;
import org.example.bankramenserver.domain.category.domain.Category;
import org.example.bankramenserver.domain.category.presentation.dto.CategoryListResponse;
import org.example.bankramenserver.domain.category.presentation.dto.CategoryResponse;
import org.example.bankramenserver.global.document.CategoryApiDocument;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/categories", produces = MediaType.APPLICATION_JSON_VALUE)
public class CategoryController implements CategoryApiDocument {

    @Override
    @GetMapping
    public CategoryListResponse getCategories() {
        List<CategoryResponse> categories = Arrays.stream(Category.values())
                .map(CategoryResponse::from)
                .toList();

        return CategoryListResponse.from(categories);
    }
}
