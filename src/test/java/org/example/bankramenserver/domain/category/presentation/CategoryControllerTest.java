package org.example.bankramenserver.domain.category.presentation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoryControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CategoryController())
                .build();
    }

    @Test
    void getCategoriesReturnsDefaultCategoryList() throws Exception {
        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories.length()", greaterThan(0)))
                .andExpect(jsonPath("$.categories[*].code", hasItem("FOOD")))
                .andExpect(jsonPath("$.categories[*].displayName", hasItem("식비")))
                .andExpect(jsonPath("$.categories[*].code", hasItem("SALARY")))
                .andExpect(jsonPath("$.categories[*].displayName", hasItem("급여")));
    }
}
