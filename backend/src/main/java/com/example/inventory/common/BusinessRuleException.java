package com.example.inventory.common;

/**
 * 業務制約違反（在庫不足・在庫保持中の削除など）の場合にスローする例外。HTTP 409 に対応する。
 *
 * @author hello-tdd-world
 * @version 0.0.1
 */
public class BusinessRuleException extends RuntimeException {

	public BusinessRuleException(String message) {
		super(message);
	}
}
