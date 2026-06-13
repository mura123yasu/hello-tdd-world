package com.example.inventory.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.inventory.common.BusinessRuleException;
import com.example.inventory.common.ResourceNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 共通例外ハンドラのスライステスト（DB 不要・ローカル実行可）。
 * standalone MockMvc + ControllerAdvice で 400/404/409 のマッピングを検証する。
 */
class GlobalExceptionHandlerTest {

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		this.mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	@DisplayName("SKELETON-UT-02 リソース未存在は 404 を返す")
	void skeleton_ut_02_not_found_returns_404() throws Exception {
		mockMvc.perform(get("/test/not-found"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	@DisplayName("SKELETON-UT-03 業務制約違反は 409 を返す")
	void skeleton_ut_03_business_rule_returns_409() throws Exception {
		mockMvc.perform(get("/test/business"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
	}

	@Test
	@DisplayName("SKELETON-UT-04 入力検証エラーは 400 とフィールドエラーを返す")
	void skeleton_ut_04_validation_returns_400() throws Exception {
		mockMvc.perform(post("/test/validate")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.fieldErrors[0].field").value("name"));
	}

	@RestController
	static class TestController {

		@GetMapping("/test/not-found")
		void notFound() {
			throw new ResourceNotFoundException("not found");
		}

		@GetMapping("/test/business")
		void business() {
			throw new BusinessRuleException("business rule violated");
		}

		@PostMapping("/test/validate")
		void validate(@Valid @RequestBody Sample body) {
			// 検証成功時は何もしない
		}
	}

	record Sample(@NotBlank String name) {
	}
}
