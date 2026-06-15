package com.example.inventory.repository;

import com.example.inventory.domain.Product;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 商品リポジトリ。JpaSpecificationExecutor で動的条件検索を実現する。
 *
 * @author hello-tdd-world
 * @version 0.0.1
 */
public interface ProductRepository extends JpaRepository<Product, Integer>,
        JpaSpecificationExecutor<Product> {

    /**
     * 論理削除されていない商品を ID で取得する。
     *
     * @param productId 商品ID
     * @return 商品（存在しない場合は空）
     */
    Optional<Product> findByProductIdAndDeletedFlagFalse(Integer productId);

    /**
     * 商品コードが既に存在するか確認する。
     *
     * @param productCode 商品コード
     * @return 存在する場合 true
     */
    boolean existsByProductCode(String productCode);
}
