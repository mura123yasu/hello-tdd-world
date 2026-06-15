package com.example.inventory.dto;

import java.util.List;

/**
 * ページング応答の共通 record。
 *
 * @param <T>           コンテンツの型
 * @param content       ページ内コンテンツ
 * @param page          現在ページ番号（0 始まり）
 * @param size          ページサイズ
 * @param totalElements 総件数
 * @author hello-tdd-world
 * @version 0.0.1
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements) {
}
