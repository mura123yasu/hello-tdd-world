package com.example.inventory.repository;

import com.example.inventory.domain.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * カテゴリリポジトリ。
 *
 * @author hello-tdd-world
 * @version 0.0.1
 */
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    /**
     * カテゴリ名昇順で全カテゴリを取得する。
     *
     * @return カテゴリリスト
     */
    List<Category> findAllByOrderByCategoryNameAsc();
}
