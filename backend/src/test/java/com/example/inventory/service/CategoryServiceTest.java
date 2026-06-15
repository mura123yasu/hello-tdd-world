package com.example.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.inventory.domain.Category;
import com.example.inventory.dto.CategoryResponse;
import com.example.inventory.repository.CategoryRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CategoryService の単体テスト（F-06）。
 *
 * @author hello-tdd-world
 * @version 0.0.1
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("F-06-UT-01 全カテゴリ取得（名称昇順）")
    void F_06_UT_01_全カテゴリ取得名称昇順() {
        final Category catA = new Category("カテゴリA");
        final Category catB = new Category("カテゴリB");
        when(categoryRepository.findAllByOrderByCategoryNameAsc()).thenReturn(List.of(catA, catB));

        final List<CategoryResponse> result = categoryService.getAllCategories();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).categoryName()).isEqualTo("カテゴリA");
        assertThat(result.get(1).categoryName()).isEqualTo("カテゴリB");
    }
}
