package com.example.inventory.web;

import java.util.List;

import com.example.inventory.common.BusinessRuleException;
import com.example.inventory.common.ResourceNotFoundException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全コントローラ共通の例外ハンドリング。
 * 入力検証=400 / リソース未存在=404 / 業務制約違反・楽観ロック競合=409 に対応づける。
 *
 * @author hello-tdd-world
 * @version 0.0.1
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
		List<ApiError.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
				.map(fe -> new ApiError.FieldError(fe.getField(), fe.getDefaultMessage()))
				.toList();
		ApiError body = ApiError.of(HttpStatus.BAD_REQUEST.value(), "Bad Request", "入力値が不正です。", fieldErrors);
		return ResponseEntity.badRequest().body(body);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
		ApiError body = ApiError.of(HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	}

	@ExceptionHandler({BusinessRuleException.class, OptimisticLockingFailureException.class})
	public ResponseEntity<ApiError> handleConflict(RuntimeException ex) {
		ApiError body = ApiError.of(HttpStatus.CONFLICT.value(), "Conflict", ex.getMessage());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
	}
}
