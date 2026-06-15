package com.example.inventory.dto;

import com.example.inventory.domain.StockTransaction;
import java.time.LocalDateTime;

/**
 * 入出庫履歴応答 DTO。
 *
 * @param transactionId   取引ID
 * @param productId       商品ID
 * @param transactionType 取引区分（IN / OUT）
 * @param quantity        数量
 * @param transactionAt   取引日時
 * @param note            備考
 * @author hello-tdd-world
 * @version 0.0.1
 */
public record StockTransactionResponse(
        Long transactionId,
        Integer productId,
        String transactionType,
        int quantity,
        LocalDateTime transactionAt,
        String note) {

    /**
     * エンティティから変換する。
     *
     * @param tx 入出庫履歴エンティティ
     * @return StockTransactionResponse
     */
    public static StockTransactionResponse from(final StockTransaction tx) {
        return new StockTransactionResponse(
                tx.getTransactionId(),
                tx.getProduct().getProductId(),
                tx.getTransactionType(),
                tx.getQuantity(),
                tx.getTransactionAt(),
                tx.getNote());
    }
}
