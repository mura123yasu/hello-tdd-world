package com.example.inventory.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.inventory.dto.CategoryResponse;
import com.example.inventory.service.CategoryService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * カテゴリコントローラのスライステスト（F-06）。
 *
 * @author hello-tdd-world
 * @version 0.0.1
 */
@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Test
    @DisplayName("F-06-UT-01 全カテゴリ取得（名称昇順）")
    void F_06_UT_01_全カテゴリ取得名称昇順() throws Exception {
        final List<CategoryResponse> categories = List.of(
                new CategoryResponse(1, "カテゴリA"),
                new CategoryResponse(2, "カテゴリB")
        );
        when(categoryService.getAllCategories()).thenReturn(categories);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryName").value("カテゴリA"))
                .andExpect(jsonPath("$[1].categoryName").value("カテゴリB"));
    }
}
