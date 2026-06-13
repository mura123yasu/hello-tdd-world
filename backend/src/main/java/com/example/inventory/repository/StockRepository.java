package com.example.inventory.repository;

import com.example.inventory.domain.Stock;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 在庫リポジトリ。
 *
 * @author hello-tdd-world
 * @version 0.0.1
 */
public interface StockRepository extends JpaRepository<Stock, Integer> {

    /**
     * 商品IDで在庫を取得する。
     *
     * @param productId 商品ID
     * @return 在庫（存在しない場合は空）
     */
    Optional<Stock> findByProductId(Integer productId);
}
