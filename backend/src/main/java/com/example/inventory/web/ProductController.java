package com.example.inventory.web;

import com.example.inventory.dto.PageResponse;
import com.example.inventory.dto.ProductCreateRequest;
import com.example.inventory.dto.ProductResponse;
import com.example.inventory.dto.ProductSearchRequest;
import com.example.inventory.dto.ProductUpdateRequest;
import com.example.inventory.dto.StockResponse;
import com.example.inventory.dto.StockTransactionRequest;
import com.example.inventory.dto.StockTransactionResponse;
import com.example.inventory.service.ProductService;
import com.example.inventory.service.StockService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * 商品 API コントローラ（F-01〜F-05, F-07〜F-10）。
 *
 * @author hello-tdd-world
 * @version 0.0.1
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final StockService stockService;

    /**
     * コンストラクタインジェクション。
     *
     * @param productService 商品サービス
     * @param stockService   在庫サービス
     */
    public ProductController(final ProductService productService,
            final StockService stockService) {
        this.productService = productService;
        this.stockService = stockService;
    }

    /**
     * 商品検索（F-01）。
     *
     * @param code       商品コード（部分一致・任意）
     * @param name       商品名（部分一致・任意）
     * @param categoryId カテゴリID（任意）
     * @param status     ステータス（任意）
     * @param page       ページ番号（デフォルト 0）
     * @param size       ページサイズ（デフォルト 20）
     * @param sort       ソート指定（デフォルト "productId,asc"）
     * @return ページ応答
     */
    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> searchProducts(
            @RequestParam(required = false) final String code,
            @RequestParam(required = false) final String name,
            @RequestParam(required = false) final Integer categoryId,
            @RequestParam(required = false) final String status,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size,
            @RequestParam(defaultValue = "productId,asc") final String sort) {
        final ProductSearchRequest request = new ProductSearchRequest(
                code, name, categoryId, status, page, size, sort);
        return ResponseEntity.ok(productService.searchProducts(request));
    }

    /**
     * 商品詳細取得（F-02）。
     *
     * @param id 商品ID
     * @return 商品応答
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable final int id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    /**
     * 商品登録（F-03）。
     *
     * @param request 商品登録リクエスト
     * @return 登録された商品応答（201 Created + Location）
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody final ProductCreateRequest request) {
        final ProductResponse response = productService.createProduct(request);
        final URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.productId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    /**
     * 商品更新（F-04）。楽観ロック使用。
     *
     * @param id      商品ID
     * @param request 商品更新リクエスト
     * @return 更新された商品応答
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable final int id,
            @Valid @RequestBody final ProductUpdateRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    /**
     * 商品論理削除（F-05）。
     *
     * @param id 商品ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable final int id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 在庫照会（F-07）。
     *
     * @param id 商品ID
     * @return 在庫応答
     */
    @GetMapping("/{id}/stock")
    public ResponseEntity<StockResponse> getStock(@PathVariable final int id) {
        return ResponseEntity.ok(stockService.getStock(id));
    }

    /**
     * 入庫登録（F-08）。
     *
     * @param id      商品ID
     * @param request 入出庫リクエスト
     * @return 更新後の在庫応答
     */
    @PostMapping("/{id}/stock/in")
    public ResponseEntity<StockResponse> stockIn(
            @PathVariable final int id,
            @Valid @RequestBody final StockTransactionRequest request) {
        return ResponseEntity.ok(stockService.stockIn(id, request));
    }

    /**
     * 出庫登録（F-09）。
     *
     * @param id      商品ID
     * @param request 入出庫リクエスト
     * @return 更新後の在庫応答
     */
    @PostMapping("/{id}/stock/out")
    public ResponseEntity<StockResponse> stockOut(
            @PathVariable final int id,
            @Valid @RequestBody final StockTransactionRequest request) {
        return ResponseEntity.ok(stockService.stockOut(id, request));
    }

    /**
     * 入出庫履歴照会（F-10）。
     *
     * @param id 商品ID
     * @return 履歴リスト（新しい順）
     */
    @GetMapping("/{id}/stock/transactions")
    public ResponseEntity<List<StockTransactionResponse>> getTransactions(@PathVariable final int id) {
        return ResponseEntity.ok(stockService.getTransactions(id));
    }
}
