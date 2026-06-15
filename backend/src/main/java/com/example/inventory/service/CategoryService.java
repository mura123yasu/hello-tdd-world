package com.example.inventory.service;

import com.example.inventory.dto.CategoryResponse;
import com.example.inventory.repository.CategoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * カテゴリサービス（F-06）。
 *
 * @author hello-tdd-world
 * @version 0.0.1
 */
@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * コンストラクタインジェクション。
     *
     * @param categoryRepository カテゴリリポジトリ
     */
    public CategoryService(final CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * カテゴリ名昇順で全カテゴリを取得する。
     *
     * @return カテゴリ応答リスト
     */
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllByOrderByCategoryNameAsc().stream()
                .map(CategoryResponse::from)
                .toList();
    }
}
