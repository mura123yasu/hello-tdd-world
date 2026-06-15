package com.example.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 入出庫登録リクエスト DTO。
 *
 * @param quantity 数量（必須、1以上）
 * @param note     備考（null 可）
 * @author hello-tdd-world
 * @version 0.0.1
 */
public record StockTransactionRequest(
        @NotNull(message = "数量は必須です。")
        @Min(value = 1, message = "数量は 1 以上の整数で入力してください。")
        Integer quantity,

        String note) {
}
