package com.example.inventory.dto;

import com.example.inventory.domain.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品応答 DTO。
 *
 * @param productId    商品ID
 * @param productCode  商品コード
 * @param productName  商品名
 * @param categoryId   カテゴリID
 * @param categoryName カテゴリ名
 * @param unitPrice    単価
 * @param status       ステータス
 * @param description  説明
 * @param version      楽観ロックバージョン
 * @param createdAt    作成日時
 * @param updatedAt    更新日時
 * @author hello-tdd-world
 * @version 0.0.1
 */
public record ProductResponse(
        Integer productId,
        String productCode,
        String productName,
        Integer categoryId,
        String categoryName,
        BigDecimal unitPrice,
        String status,
        String description,
        Integer version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    /**
     * エンティティから変換する。
     *
     * @param product 商品エンティティ
     * @return ProductResponse
     */
    public static ProductResponse from(final Product product) {
        return new ProductResponse(
                product.getProductId(),
                product.getProductCode(),
                product.getProductName(),
                product.getCategory().getCategoryId(),
                product.getCategory().getCategoryName(),
                product.getUnitPrice(),
                product.getStatus(),
                product.getDescription(),
                product.getVersion(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}
