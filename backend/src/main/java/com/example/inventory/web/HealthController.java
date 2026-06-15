package com.example.inventory.web;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 稼働確認用エンドポイント。
 *
 * @author hello-tdd-world
 * @version 0.0.1
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

	@GetMapping
	public Map<String, String> health() {
		return Map.of("status", "UP");
	}
}
