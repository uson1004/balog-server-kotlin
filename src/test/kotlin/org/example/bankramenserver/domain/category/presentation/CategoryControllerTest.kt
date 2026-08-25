package org.example.bankramenserver.domain.category.presentation

import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class CategoryControllerTest {
    private val mockMvc = MockMvcBuilders.standaloneSetup(CategoryController()).build()

    @Test
    fun getCategoriesReturnsDefaultCategoryList() {
        mockMvc.get("/categories") {
            accept = org.springframework.http.MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.categories.length()", greaterThan(0))
            jsonPath("$.categories[*].code", hasItem("FOOD"))
            jsonPath("$.categories[*].displayName", hasItem("식비"))
            jsonPath("$.categories[*].code", hasItem("SALARY"))
            jsonPath("$.categories[*].displayName", hasItem("급여"))
        }
    }
}
