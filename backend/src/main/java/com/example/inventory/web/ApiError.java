package com.example.inventory.web;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * API 共通エラー応答。
 *
 * @author hello-tdd-world
 * @version 0.0.1
 */
public record ApiError(
		OffsetDateTime timestamp,
		int status,
		String error,
		String message,
		List<FieldError> fieldErrors) {

	/**
	 * フィールド単位の検証エラー。
	 */
	public record FieldError(String field, String message) {
	}

	public static ApiError of(int status, String error, String message) {
		return new ApiError(OffsetDateTime.now(), status, error, message, List.of());
	}

	public static ApiError of(int status, String error, String message, List<FieldError> fieldErrors) {
		return new ApiError(OffsetDateTime.now(), status, error, message, fieldErrors);
	}
}
