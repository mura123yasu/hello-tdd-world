package com.example.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 商品登録リクエスト DTO。
 *
 * @param productCode 商品コード（必須、半角英数字、1〜20文字）
 * @param productName 商品名（必須、1〜100文字）
 * @param categoryId  カテゴリID（必須）
 * @param unitPrice   単価（必須、0以上、小数2桁まで）
 * @param status      ステータス（ACTIVE / INACTIVE）
 * @param description 説明（null 可）
 * @author hello-tdd-world
 * @version 0.0.1
 */
public record ProductCreateRequest(
        @NotBlank(message = "商品コードは必須です。")
        @Pattern(regexp = "^[a-zA-Z0-9]{1,20}$", message = "商品コードは半角英数字 1〜20 文字で入力してください。")
        String productCode,

        @NotBlank(message = "商品名は必須です。")
        @Size(max = 100, message = "商品名は 100 文字以内で入力してください。")
        String productName,

        @NotNull(message = "カテゴリIDは必須です。")
        Integer categoryId,

        @NotNull(message = "単価は必須です。")
        @DecimalMin(value = "0", message = "単価は 0 以上で入力してください。")
        @Digits(integer = 10, fraction = 2, message = "単価は整数 10 桁・小数 2 桁以内で入力してください。")
        BigDecimal unitPrice,

        @NotBlank(message = "ステータスは必須です。")
        @Pattern(regexp = "^(ACTIVE|INACTIVE)$", message = "ステータスは ACTIVE または INACTIVE を指定してください。")
        String status,

        String description) {
}
