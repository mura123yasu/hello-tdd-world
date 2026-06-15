package com.example.inventory.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.inventory.common.BusinessRuleException;
import com.example.inventory.common.ResourceNotFoundException;
import com.example.inventory.dto.PageResponse;
import com.example.inventory.dto.ProductResponse;
import com.example.inventory.dto.StockResponse;
import com.example.inventory.dto.StockTransactionResponse;
import com.example.inventory.service.ProductService;
import com.example.inventory.service.StockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 商品コントローラのスライステスト（F-01〜F-05, F-07〜F-10）。
 *
 * @author hello-tdd-world
 * @version 0.0.1
 */
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private StockService stockService;

    // ---- F-01 商品検索 UT ----

    @Test
    @DisplayName("F-01-UT-01 商品名の部分一致検索")
    void F_01_UT_01_商品名部分一致検索() throws Exception {
        final ProductResponse product = buildProductResponse(1, "ABC001", "テスト商品");
        final PageResponse<ProductResponse> page = new PageResponse<>(List.of(product), 0, 20, 1L);
        when(productService.searchProducts(any())).thenReturn(page);

        mockMvc.perform(get("/api/products").param("name", "テスト"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productName").value("テスト商品"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("F-01-UT-02 カテゴリ＋ステータス複合条件")
    void F_01_UT_02_カテゴリとステータス複合条件() throws Exception {
        final ProductResponse product = buildProductResponse(2, "ABC002", "複合条件商品");
        final PageResponse<ProductResponse> page = new PageResponse<>(List.of(product), 0, 20, 1L);
        when(productService.searchProducts(any())).thenReturn(page);

        mockMvc.perform(get("/api/products")
                        .param("categoryId", "1")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("F-01-UT-03 論理削除済みは除外")
    void F_01_UT_03_論理削除済みは除外() throws Exception {
        final PageResponse<ProductResponse> page = new PageResponse<>(List.of(), 0, 20, 0L);
        when(productService.searchProducts(any())).thenReturn(page);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("F-01-UT-04 ソート指定（単価昇順/降順）")
    void F_01_UT_04_ソート指定() throws Exception {
        final ProductResponse p1 = buildProductResponseWithPrice(1, "A001", "商品A", new BigDecimal("100.00"));
        final ProductResponse p2 = buildProductResponseWithPrice(2, "A002", "商品B", new BigDecimal("200.00"));
        final PageResponse<ProductResponse> page = new PageResponse<>(List.of(p1, p2), 0, 20, 2L);
        when(productService.searchProducts(any())).thenReturn(page);

        mockMvc.perform(get("/api/products").param("sort", "unitPrice,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].unitPrice").value(100.00))
                .andExpect(jsonPath("$.content[1].unitPrice").value(200.00));
    }

    // ---- F-02 商品詳細取得 UT ----

    @Test
    @DisplayName("F-02-UT-01 存在する商品IDで取得")
    void F_02_UT_01_存在する商品IDで取得() throws Exception {
        final ProductResponse product = buildProductResponse(1, "ABC001", "テスト商品");
        when(productService.getProduct(1)).thenReturn(product);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.productCode").value("ABC001"));
    }

    @Test
    @DisplayName("F-02-UT-02 存在しないIDはNotFound")
    void F_02_UT_02_存在しないIDはNotFound() throws Exception {
        when(productService.getProduct(999)).thenThrow(new ResourceNotFoundException("商品が見つかりません。"));

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound());
    }

    // ---- F-03 商品登録 UT ----

    @Test
    @DisplayName("F-03-UT-01 正常登録")
    void F_03_UT_01_正常登録() throws Exception {
        final ProductResponse product = buildProductResponse(1, "NEW001", "新商品");
        when(productService.createProduct(any())).thenReturn(product);

        final Map<String, Object> body = Map.of(
                "productCode", "NEW001",
                "productName", "新商品",
                "categoryId", 1,
                "unitPrice", 1000,
                "status", "ACTIVE"
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.productId").value(1));
    }

    @Test
    @DisplayName("F-03-UT-02 商品コード重複は業務エラー(409)")
    void F_03_UT_02_商品コード重複は業務エラー() throws Exception {
        when(productService.createProduct(any()))
                .thenThrow(new BusinessRuleException("商品コードが既に使用されています。"));

        final Map<String, Object> body = Map.of(
                "productCode", "DUP001",
                "productName", "重複商品",
                "categoryId", 1,
                "unitPrice", 1000,
                "status", "ACTIVE"
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("F-03-UT-03 必須項目欠落は検証エラー(400)")
    void F_03_UT_03_必須項目欠落は検証エラー() throws Exception {
        final Map<String, Object> body = Map.of(
                "productName", "商品名のみ"
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    @DisplayName("F-03-UT-04 unit_price負値は検証エラー(400)")
    void F_03_UT_04_単価負値は検証エラー() throws Exception {
        final Map<String, Object> body = Map.of(
                "productCode", "ABC001",
                "productName", "テスト商品",
                "categoryId", 1,
                "unitPrice", -1,
                "status", "ACTIVE"
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // ---- F-04 商品更新 UT ----

    @Test
    @DisplayName("F-04-UT-01 正常更新")
    void F_04_UT_01_正常更新() throws Exception {
        final ProductResponse product = buildProductResponse(1, "ABC001", "更新商品");
        when(productService.updateProduct(eq(1), any())).thenReturn(product);

        final Map<String, Object> body = Map.of(
                "productName", "更新商品",
                "categoryId", 1,
                "unitPrice", 2000,
                "status", "ACTIVE",
                "version", 0
        );

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("更新商品"));
    }

    @Test
    @DisplayName("F-04-UT-02 楽観ロック競合(409)")
    void F_04_UT_02_楽観ロック競合() throws Exception {
        when(productService.updateProduct(eq(1), any()))
                .thenThrow(new OptimisticLockingFailureException("競合が発生しました。"));

        final Map<String, Object> body = Map.of(
                "productName", "商品名",
                "categoryId", 1,
                "unitPrice", 1000,
                "status", "ACTIVE",
                "version", 0
        );

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("F-04-UT-03 存在しないID更新はNotFound")
    void F_04_UT_03_存在しないID更新はNotFound() throws Exception {
        when(productService.updateProduct(eq(999), any()))
                .thenThrow(new ResourceNotFoundException("商品が見つかりません。"));

        final Map<String, Object> body = Map.of(
                "productName", "商品名",
                "categoryId", 1,
                "unitPrice", 1000,
                "status", "ACTIVE",
                "version", 0
        );

        mockMvc.perform(put("/api/products/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("F-04-UT-04 商品名空は検証エラー(400)")
    void F_04_UT_04_商品名空は検証エラー() throws Exception {
        final Map<String, Object> body = Map.of(
                "productName", "",
                "categoryId", 1,
                "unitPrice", 1000,
                "status", "ACTIVE",
                "version", 0
        );

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // ---- F-05 商品削除 UT ----

    @Test
    @DisplayName("F-05-UT-01 在庫0の論理削除")
    void F_05_UT_01_在庫0の論理削除() throws Exception {
        doNothing().when(productService).deleteProduct(1);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("F-05-UT-02 在庫ありは削除不可(409)")
    void F_05_UT_02_在庫ありは削除不可() throws Exception {
        doThrow(new BusinessRuleException("在庫が残っている商品は削除できません。"))
                .when(productService).deleteProduct(2);

        mockMvc.perform(delete("/api/products/2"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("F-05-UT-03 存在しないIDは404")
    void F_05_UT_03_存在しないIDは404() throws Exception {
        doThrow(new ResourceNotFoundException("商品が見つかりません。"))
                .when(productService).deleteProduct(999);

        mockMvc.perform(delete("/api/products/999"))
                .andExpect(status().isNotFound());
    }

    // ---- F-07 在庫照会 UT ----

    @Test
    @DisplayName("F-07-UT-01 在庫数量取得")
    void F_07_UT_01_在庫数量取得() throws Exception {
        when(stockService.getStock(1)).thenReturn(new StockResponse(1, 50));

        mockMvc.perform(get("/api/products/1/stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.quantity").value(50));
    }

    @Test
    @DisplayName("F-07-UT-02 在庫行なしは0として扱う")
    void F_07_UT_02_在庫行なしは0として扱う() throws Exception {
        when(stockService.getStock(1)).thenReturn(new StockResponse(1, 0));

        mockMvc.perform(get("/api/products/1/stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(0));
    }

    // ---- F-08 入庫登録 UT ----

    @Test
    @DisplayName("F-08-UT-01 入庫で在庫加算")
    void F_08_UT_01_入庫で在庫加算() throws Exception {
        when(stockService.stockIn(eq(1), any())).thenReturn(new StockResponse(1, 110));

        final Map<String, Object> body = Map.of("quantity", 10);

        mockMvc.perform(post("/api/products/1/stock/in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(110));
    }

    @Test
    @DisplayName("F-08-UT-02 入庫で履歴(IN)が記録される")
    void F_08_UT_02_入庫で履歴が記録される() throws Exception {
        when(stockService.stockIn(eq(1), any())).thenReturn(new StockResponse(1, 10));

        final Map<String, Object> body = Map.of("quantity", 10, "note", "入庫テスト");

        mockMvc.perform(post("/api/products/1/stock/in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("F-08-UT-03 quantity0以下は検証エラー(400)")
    void F_08_UT_03_quantity0以下は検証エラー() throws Exception {
        final Map<String, Object> body = Map.of("quantity", 0);

        mockMvc.perform(post("/api/products/1/stock/in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // ---- F-09 出庫登録 UT ----

    @Test
    @DisplayName("F-09-UT-01 出庫で在庫減算")
    void F_09_UT_01_出庫で在庫減算() throws Exception {
        when(stockService.stockOut(eq(1), any())).thenReturn(new StockResponse(1, 90));

        final Map<String, Object> body = Map.of("quantity", 10);

        mockMvc.perform(post("/api/products/1/stock/out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(90));
    }

    @Test
    @DisplayName("F-09-UT-02 在庫不足は業務エラー(409)")
    void F_09_UT_02_在庫不足は業務エラー() throws Exception {
        when(stockService.stockOut(eq(1), any()))
                .thenThrow(new BusinessRuleException("在庫が不足しています。"));

        final Map<String, Object> body = Map.of("quantity", 9999);

        mockMvc.perform(post("/api/products/1/stock/out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("F-09-UT-03 ちょうど0になる出庫は成功")
    void F_09_UT_03_ちょうど0になる出庫は成功() throws Exception {
        when(stockService.stockOut(eq(1), any())).thenReturn(new StockResponse(1, 0));

        final Map<String, Object> body = Map.of("quantity", 100);

        mockMvc.perform(post("/api/products/1/stock/out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(0));
    }

    @Test
    @DisplayName("F-09-UT-04 出庫で履歴(OUT)が記録される")
    void F_09_UT_04_出庫で履歴が記録される() throws Exception {
        when(stockService.stockOut(eq(1), any())).thenReturn(new StockResponse(1, 90));

        final Map<String, Object> body = Map.of("quantity", 10, "note", "出庫テスト");

        mockMvc.perform(post("/api/products/1/stock/out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    // ---- F-10 入出庫履歴照会 UT ----

    @Test
    @DisplayName("F-10-UT-01 履歴を時系列降順取得")
    void F_10_UT_01_履歴を時系列降順取得() throws Exception {
        final LocalDateTime now = LocalDateTime.now();
        final StockTransactionResponse tx1 = new StockTransactionResponse(2L, 1, "IN", 20, now, null);
        final StockTransactionResponse tx2 = new StockTransactionResponse(1L, 1, "OUT", 10, now.minusHours(1), null);
        when(stockService.getTransactions(1)).thenReturn(List.of(tx1, tx2));

        mockMvc.perform(get("/api/products/1/stock/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transactionId").value(2))
                .andExpect(jsonPath("$[1].transactionId").value(1));
    }

    @Test
    @DisplayName("F-10-UT-02 履歴0件は空リスト")
    void F_10_UT_02_履歴0件は空リスト() throws Exception {
        when(stockService.getTransactions(1)).thenReturn(List.of());

        mockMvc.perform(get("/api/products/1/stock/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ---- Helper methods ----

    private ProductResponse buildProductResponse(final int id, final String code, final String name) {
        return new ProductResponse(id, code, name, 1, "カテゴリA",
                new BigDecimal("1000.00"), "ACTIVE", null, 0,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private ProductResponse buildProductResponseWithPrice(
            final int id, final String code, final String name, final BigDecimal price) {
        return new ProductResponse(id, code, name, 1, "カテゴリA",
                price, "ACTIVE", null, 0,
                LocalDateTime.now(), LocalDateTime.now());
    }
}
