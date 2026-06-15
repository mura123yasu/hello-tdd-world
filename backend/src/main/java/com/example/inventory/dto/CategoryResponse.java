package com.example.inventory.dto;

import com.example.inventory.domain.Category;

/**
 * カテゴリ応答 DTO。
 *
 * @param categoryId   カテゴリID
 * @param categoryName カテゴリ名
 * @author hello-tdd-world
 * @version 0.0.1
 */
public record CategoryResponse(
        Integer categoryId,
        String categoryName) {

    /**
     * エンティティから変換する。
     *
     * @param category カテゴリエンティティ
     * @return CategoryResponse
     */
    public static CategoryResponse from(final Category category) {
        return new CategoryResponse(category.getCategoryId(), category.getCategoryName());
    }
}
