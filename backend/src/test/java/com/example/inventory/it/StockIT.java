package com.example.inventory.it;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.inventory.domain.Category;
import com.example.inventory.domain.Product;
import com.example.inventory.domain.Stock;
import com.example.inventory.repository.CategoryRepository;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.StockRepository;
import com.example.inventory.repository.StockTransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * 在庫 API の結合テスト（F-07〜F-10）。Testcontainers SQL Server を使用。
 *
 * @author hello-tdd-world
 * @version 0.0.1
 */
@AutoConfigureMockMvc
@Transactional
class StockIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private StockTransactionRepository stockTransactionRepository;

    private Product savedProduct;

    @BeforeEach
    void setUp() {
        stockTransactionRepository.deleteAll();
        stockRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        final Category category = categoryRepository.save(new Category("テストカテゴリ"));
        final Product product = new Product("P001", "テスト商品", category,
                new BigDecimal("1000.00"), "ACTIVE", null);
        savedProduct = productRepository.save(product);
        final Stock stock = new Stock(savedProduct);
        stock.addQuantity(100);
        stockRepository.save(stock);
    }

    // ---- F-07 IT ----

    @Test
    @DisplayName("F-07-IT-01 GET stock 200 数量が返る")
    void F_07_IT_01_在庫照会200() throws Exception {
        mockMvc.perform(get("/api/products/" + savedProduct.getProductId() + "/stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(savedProduct.getProductId()))
                .andExpect(jsonPath("$.quantity").value(100));
    }

    // ---- F-08 IT ----

    @Test
    @DisplayName("F-08-IT-01 POST stock/in 200 在庫＋履歴が更新される")
    void F_08_IT_01_入庫200() throws Exception {
        final Map<String, Object> body = Map.of("quantity", 50, "note", "入庫テスト");

        mockMvc.perform(post("/api/products/" + savedProduct.getProductId() + "/stock/in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(150));
    }

    // ---- F-09 IT ----

    @Test
    @DisplayName("F-09-IT-01 POST stock/out 200 在庫＋履歴が更新される")
    void F_09_IT_01_出庫200() throws Exception {
        final Map<String, Object> body = Map.of("quantity", 30, "note", "出庫テスト");

        mockMvc.perform(post("/api/products/" + savedProduct.getProductId() + "/stock/out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(70));
    }

    @Test
    @DisplayName("F-09-IT-02 在庫不足で out 409（ロールバックされる）")
    void F_09_IT_02_在庫不足で出庫409() throws Exception {
        final Map<String, Object> body = Map.of("quantity", 9999);

        mockMvc.perform(post("/api/products/" + savedProduct.getProductId() + "/stock/out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }

    // ---- F-10 IT ----

    @Test
    @DisplayName("F-10-IT-01 GET transactions 200 一覧が返る")
    void F_10_IT_01_入出庫履歴200() throws Exception {
        // 先に入庫して履歴を作る
        final Map<String, Object> inBody = Map.of("quantity", 10, "note", "入庫");
        mockMvc.perform(post("/api/products/" + savedProduct.getProductId() + "/stock/in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inBody)));

        mockMvc.perform(get("/api/products/" + savedProduct.getProductId() + "/stock/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
