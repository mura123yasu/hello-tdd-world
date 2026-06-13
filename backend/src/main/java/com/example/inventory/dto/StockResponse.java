package com.example.inventory.dto;

/**
 * 在庫照会応答 DTO。
 *
 * @param productId 商品ID
 * @param quantity  在庫数量
 * @author hello-tdd-world
 * @version 0.0.1
 */
public record StockResponse(
        Integer productId,
        int quantity) {
}
