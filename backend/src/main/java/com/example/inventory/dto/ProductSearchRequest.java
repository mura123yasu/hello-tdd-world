package com.example.inventory.dto;

/**
 * 商品検索リクエスト DTO。
 *
 * @param code       商品コード（部分一致・null 可）
 * @param name       商品名（部分一致・null 可）
 * @param categoryId カテゴリID（null 可）
 * @param status     ステータス（null 可）
 * @param page       ページ番号（0 始まり）
 * @param size       ページサイズ
 * @param sort       ソート指定（例: "unitPrice,asc"）
 * @author hello-tdd-world
 * @version 0.0.1
 */
public record ProductSearchRequest(
        String code,
        String name,
        Integer categoryId,
        String status,
        int page,
        int size,
        String sort) {
}
