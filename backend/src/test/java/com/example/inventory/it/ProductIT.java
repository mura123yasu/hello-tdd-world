package com.example.inventory.it;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.inventory.domain.Category;
import com.example.inventory.domain.Product;
import com.example.inventory.domain.Stock;
import com.example.inventory.repository.CategoryRepository;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.StockRepository;
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
 * 商品 API の結合テスト（F-01〜F-05）。Testcontainers SQL Server を使用。
 *
 * @author hello-tdd-world
 * @version 0.0.1
 */
@AutoConfigureMockMvc
@Transactional
class ProductIT extends AbstractIntegrationTest {

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

    private Category savedCategory;

    @BeforeEach
    void setUp() {
        stockRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        savedCategory = categoryRepository.save(new Category("テストカテゴリ"));
    }

    // ---- F-01 IT ----

    @Test
    @DisplayName("F-01-IT-01 検索API 200・ページング情報が正しい")
    void F_01_IT_01_検索API200ページング() throws Exception {
        final Product p = new Product("P001", "商品A", savedCategory,
                new BigDecimal("1000.00"), "ACTIVE", null);
        productRepository.save(p);

        mockMvc.perform(get("/api/products").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    @DisplayName("F-01-IT-02 条件なし検索（全件・削除除く）")
    void F_01_IT_02_条件なし検索全件() throws Exception {
        final Product p1 = new Product("P001", "商品A", savedCategory,
                new BigDecimal("1000.00"), "ACTIVE", null);
        final Product p2 = new Product("P002", "商品B", savedCategory,
                new BigDecimal("2000.00"), "ACTIVE", null);
        final Product p3 = new Product("P003", "削除済商品", savedCategory,
                new BigDecimal("3000.00"), "ACTIVE", null);
        p3.delete();
        productRepository.save(p1);
        productRepository.save(p2);
        productRepository.save(p3);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    // ---- F-02 IT ----

    @Test
    @DisplayName("F-02-IT-01 GET /products/{id} 200・単票項目が揃う")
    void F_02_IT_01_商品詳細取得200() throws Exception {
        final Product p = new Product("P001", "商品A", savedCategory,
                new BigDecimal("1000.00"), "ACTIVE", "説明文");
        final Product saved = productRepository.save(p);

        mockMvc.perform(get("/api/products/" + saved.getProductId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productCode").value("P001"))
                .andExpect(jsonPath("$.productName").value("商品A"))
                .andExpect(jsonPath("$.categoryId").exists())
                .andExpect(jsonPath("$.unitPrice").value(1000.00))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("F-02-IT-02 GET 存在しないID 404")
    void F_02_IT_02_存在しないID404() throws Exception {
        mockMvc.perform(get("/api/products/99999"))
                .andExpect(status().isNotFound());
    }

    // ---- F-03 IT ----

    @Test
    @DisplayName("F-03-IT-01 POST 201 + Location（DBに登録され在庫0行が作られる）")
    void F_03_IT_01_商品登録201() throws Exception {
        final Map<String, Object> body = Map.of(
                "productCode", "NEW001",
                "productName", "新商品",
                "categoryId", savedCategory.getCategoryId(),
                "unitPrice", 1500,
                "status", "ACTIVE"
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.productCode").value("NEW001"));
    }

    @Test
    @DisplayName("F-03-IT-02 重複コードで POST 409")
    void F_03_IT_02_重複コードPOST409() throws Exception {
        final Product existing = new Product("EXIST001", "既存商品", savedCategory,
                new BigDecimal("1000.00"), "ACTIVE", null);
        productRepository.save(existing);

        final Map<String, Object> body = Map.of(
                "productCode", "EXIST001",
                "productName", "別商品",
                "categoryId", savedCategory.getCategoryId(),
                "unitPrice", 2000,
                "status", "ACTIVE"
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("F-03-IT-03 不正 body で POST 400 + フィールド別メッセージ")
    void F_03_IT_03_不正bodyPOST400() throws Exception {
        final Map<String, Object> body = Map.of(
                "productName", "商品名のみ"
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    // ---- F-04 IT ----

    @Test
    @DisplayName("F-04-IT-01 PUT 200 DBが更新される")
    void F_04_IT_01_商品更新200() throws Exception {
        final Product p = new Product("P001", "商品A", savedCategory,
                new BigDecimal("1000.00"), "ACTIVE", null);
        final Product saved = productRepository.save(p);

        final Map<String, Object> body = Map.of(
                "productName", "更新後商品名",
                "categoryId", savedCategory.getCategoryId(),
                "unitPrice", 2000,
                "status", "INACTIVE",
                "version", saved.getVersion()
        );

        mockMvc.perform(put("/api/products/" + saved.getProductId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("更新後商品名"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @DisplayName("F-04-IT-02 古い version で PUT 409")
    void F_04_IT_02_古いversionでPUT409() throws Exception {
        final Product p = new Product("P001", "商品A", savedCategory,
                new BigDecimal("1000.00"), "ACTIVE", null);
        final Product saved = productRepository.save(p);

        final Map<String, Object> body = Map.of(
                "productName", "更新後商品名",
                "categoryId", savedCategory.getCategoryId(),
                "unitPrice", 2000,
                "status", "ACTIVE",
                "version", 9999
        );

        mockMvc.perform(put("/api/products/" + saved.getProductId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }

    // ---- F-05 IT ----

    @Test
    @DisplayName("F-05-IT-01 DELETE 204（一覧・詳細から除外される）")
    void F_05_IT_01_商品削除204() throws Exception {
        final Product p = new Product("DEL001", "削除対象商品", savedCategory,
                new BigDecimal("1000.00"), "ACTIVE", null);
        final Product saved = productRepository.save(p);
        stockRepository.save(new Stock(saved));

        mockMvc.perform(delete("/api/products/" + saved.getProductId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/" + saved.getProductId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("F-05-IT-02 在庫あり DELETE 409")
    void F_05_IT_02_在庫ありDELETE409() throws Exception {
        final Product p = new Product("STOCKED001", "在庫あり商品", savedCategory,
                new BigDecimal("1000.00"), "ACTIVE", null);
        final Product saved = productRepository.save(p);
        final Stock stock = new Stock(saved);
        stock.addQuantity(5);
        stockRepository.save(stock);

        mockMvc.perform(delete("/api/products/" + saved.getProductId()))
                .andExpect(status().isConflict());
    }
}
