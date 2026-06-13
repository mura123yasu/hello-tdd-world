package com.example.inventory.web;

import com.example.inventory.dto.CategoryResponse;
import com.example.inventory.service.CategoryService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * カテゴリ API コントローラ（F-06）。
 *
 * @author hello-tdd-world
 * @version 0.0.1
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * コンストラクタインジェクション。
     *
     * @param categoryService カテゴリサービス
     */
    public CategoryController(final CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * カテゴリ一覧を取得する（F-06）。名称昇順で返す。
     *
     * @return カテゴリ応答リスト
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }
}
