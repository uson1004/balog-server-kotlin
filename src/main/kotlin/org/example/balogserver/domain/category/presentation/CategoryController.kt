package org.example.balogserver.domain.category.presentation

import org.example.balogserver.domain.category.domain.Category
import org.example.balogserver.domain.category.presentation.dto.CategoryListResponse
import org.example.balogserver.domain.category.presentation.dto.CategoryResponse
import org.example.balogserver.global.document.CategoryApiDocument
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/categories"], produces = [MediaType.APPLICATION_JSON_VALUE])
class CategoryController : CategoryApiDocument {
    @GetMapping
    override fun getCategories() = CategoryListResponse.from(Category.entries.map(CategoryResponse::from))
}
