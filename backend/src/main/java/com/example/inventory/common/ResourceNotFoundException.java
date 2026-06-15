package com.example.inventory.common;

/**
 * 対象リソースが存在しない場合にスローする例外。HTTP 404 に対応する。
 *
 * @author hello-tdd-world
 * @version 0.0.1
 */
public class ResourceNotFoundException extends RuntimeException {

	public ResourceNotFoundException(String message) {
		super(message);
	}
}
