package com.example.inventory.repository;

import com.example.inventory.domain.StockTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 入出庫履歴リポジトリ。
 *
 * @author hello-tdd-world
 * @version 0.0.1
 */
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {

    /**
     * 商品IDの入出庫履歴を取引日時降順で取得する。
     *
     * @param productId 商品ID
     * @return 入出庫履歴リスト（新しい順）
     */
    List<StockTransaction> findByProductIdOrderByTransactionAtDesc(Integer productId);

    /**
     * 商品IDの履歴を全件削除する（テスト用）。
     *
     * @param productId 商品ID
     */
    void deleteByProductId(Integer productId);
}
