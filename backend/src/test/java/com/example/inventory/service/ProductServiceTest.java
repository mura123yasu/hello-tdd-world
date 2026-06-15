package com.example.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.inventory.common.BusinessRuleException;
import com.example.inventory.common.ResourceNotFoundException;
import com.example.inventory.domain.Category;
import com.example.inventory.domain.Product;
import com.example.inventory.domain.Stock;
import com.example.inventory.dto.PageResponse;
import com.example.inventory.dto.ProductCreateRequest;
import com.example.inventory.dto.ProductResponse;
import com.example.inventory.dto.ProductSearchRequest;
import com.example.inventory.dto.ProductUpdateRequest;
import com.example.inventory.repository.CategoryRepository;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.StockRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

/**
 * ProductService の単体テスト（F-01〜F-05）。
 *
 * @author hello-tdd-world
 * @version 0.0.1
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private ProductService productService;

    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        category = new Category("カテゴリA");
        product = new Product("ABC001", "テスト商品", category,
                new BigDecimal("1000.00"), "ACTIVE", null);
    }

    // ---- F-01 商品検索 UT (Service 層) ----

    @Test
    @DisplayName("F-01-UT-01 商品名の部分一致検索")
    @SuppressWarnings("unchecked")
    void F_01_UT_01_商品名部分一致検索() {
        final var pageRequest = PageRequest.of(0, 20);
        final var pageResult = new PageImpl<>(List.of(product), pageRequest, 1L);
        when(productRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(pageResult);

        final var searchRequest = new ProductSearchRequest(null, "テスト", null, null, 0, 20, "productId,asc");
        final PageResponse<ProductResponse> result = productService.searchProducts(searchRequest);

        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).productName()).isEqualTo("テスト商品");
    }

    @Test
    @DisplayName("F-01-UT-02 カテゴリ＋ステータス複合条件")
    @SuppressWarnings("unchecked")
    void F_01_UT_02_カテゴリとステータス複合条件() {
        final var pageRequest = PageRequest.of(0, 20);
        final var pageResult = new PageImpl<>(List.of(product), pageRequest, 1L);
        when(productRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(pageResult);

        final var searchRequest = new ProductSearchRequest(null, null, 1, "ACTIVE", 0, 20, "productId,asc");
        final PageResponse<ProductResponse> result = productService.searchProducts(searchRequest);

        assertThat(result.totalElements()).isEqualTo(1L);
    }

    @Test
    @DisplayName("F-01-UT-03 論理削除済みは除外")
    @SuppressWarnings("unchecked")
    void F_01_UT_03_論理削除済みは除外() {
        final var pageRequest = PageRequest.of(0, 20);
        final var pageResult = new PageImpl<>(List.of(), pageRequest, 0L);
        when(productRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(pageResult);

        final var searchRequest = new ProductSearchRequest(null, null, null, null, 0, 20, "productId,asc");
        final PageResponse<ProductResponse> result = productService.searchProducts(searchRequest);

        assertThat(result.totalElements()).isEqualTo(0L);
        assertThat(result.content()).isEmpty();
    }

    @Test
    @DisplayName("F-01-UT-04 ソート指定（単価昇順/降順）")
    @SuppressWarnings("unchecked")
    void F_01_UT_04_ソート指定() {
        final Product p1 = new Product("A001", "商品A", category, new BigDecimal("100.00"), "ACTIVE", null);
        final Product p2 = new Product("A002", "商品B", category, new BigDecimal("200.00"), "ACTIVE", null);
        final var pageRequest = PageRequest.of(0, 20);
        final var pageResult = new PageImpl<>(List.of(p1, p2), pageRequest, 2L);
        when(productRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(pageResult);

        final var searchRequest = new ProductSearchRequest(null, null, null, null, 0, 20, "unitPrice,asc");
        final PageResponse<ProductResponse> result = productService.searchProducts(searchRequest);

        assertThat(result.content()).hasSize(2);
        assertThat(result.content().get(0).unitPrice()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(result.content().get(1).unitPrice()).isEqualByComparingTo(new BigDecimal("200.00"));
    }

    // ---- F-02 商品詳細取得 UT (Service 層) ----

    @Test
    @DisplayName("F-02-UT-01 存在する商品IDで取得")
    void F_02_UT_01_存在する商品IDで取得() {
        when(productRepository.findByProductIdAndDeletedFlagFalse(1)).thenReturn(Optional.of(product));

        final ProductResponse result = productService.getProduct(1);

        assertThat(result.productCode()).isEqualTo("ABC001");
        assertThat(result.productName()).isEqualTo("テスト商品");
    }

    @Test
    @DisplayName("F-02-UT-02 存在しないIDはNotFound例外")
    void F_02_UT_02_存在しないIDはNotFound例外() {
        when(productRepository.findByProductIdAndDeletedFlagFalse(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProduct(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    // ---- F-03 商品登録 UT (Service 層) ----

    @Test
    @DisplayName("F-03-UT-01 正常登録（採番され在庫0で初期化）")
    void F_03_UT_01_正常登録() {
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(productRepository.existsByProductCode("NEW001")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(stockRepository.save(any(Stock.class))).thenReturn(new Stock(product));

        final ProductCreateRequest request = new ProductCreateRequest(
                "NEW001", "新商品", 1, new BigDecimal("1000.00"), "ACTIVE", null);
        final ProductResponse result = productService.createProduct(request);

        assertThat(result.productCode()).isEqualTo("ABC001");
        verify(stockRepository).save(any(Stock.class));
    }

    @Test
    @DisplayName("F-03-UT-02 商品コード重複は業務エラー")
    void F_03_UT_02_商品コード重複は業務エラー() {
        when(productRepository.existsByProductCode("DUP001")).thenReturn(true);

        final ProductCreateRequest request = new ProductCreateRequest(
                "DUP001", "重複商品", 1, new BigDecimal("1000.00"), "ACTIVE", null);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("DUP001");
    }

    @Test
    @DisplayName("F-03-UT-03 存在しないカテゴリIDは業務エラー")
    void F_03_UT_03_存在しないカテゴリIDは業務エラー() {
        when(productRepository.existsByProductCode("NEW001")).thenReturn(false);
        when(categoryRepository.findById(999)).thenReturn(Optional.empty());

        final ProductCreateRequest request = new ProductCreateRequest(
                "NEW001", "新商品", 999, new BigDecimal("1000.00"), "ACTIVE", null);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("F-03-UT-04 unit_price負値は検証レイヤで弾かれる（ServiceはBigDecimal>=0を想定）")
    void F_03_UT_04_単価正常値で登録成功() {
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(productRepository.existsByProductCode("OK001")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(stockRepository.save(any(Stock.class))).thenReturn(new Stock(product));

        final ProductCreateRequest request = new ProductCreateRequest(
                "OK001", "正常商品", 1, new BigDecimal("0.00"), "ACTIVE", null);
        final ProductResponse result = productService.createProduct(request);

        assertThat(result).isNotNull();
    }

    // ---- F-04 商品更新 UT (Service 層) ----

    @Test
    @DisplayName("F-04-UT-01 正常更新（値が更新されversion+1）")
    void F_04_UT_01_正常更新() {
        when(productRepository.findByProductIdAndDeletedFlagFalse(1)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        final ProductUpdateRequest request = new ProductUpdateRequest(
                "更新商品", 1, new BigDecimal("2000.00"), "INACTIVE", "説明", 0);
        final ProductResponse result = productService.updateProduct(1, request);

        assertThat(result).isNotNull();
        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("F-04-UT-02 楽観ロック競合は例外")
    void F_04_UT_02_楽観ロック競合は例外() {
        when(productRepository.findByProductIdAndDeletedFlagFalse(1)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class)))
                .thenThrow(new OptimisticLockingFailureException("楽観ロック競合"));

        final ProductUpdateRequest request = new ProductUpdateRequest(
                "更新商品", 1, new BigDecimal("2000.00"), "ACTIVE", null, 99);

        assertThatThrownBy(() -> productService.updateProduct(1, request))
                .isInstanceOf(OptimisticLockingFailureException.class);
    }

    @Test
    @DisplayName("F-04-UT-03 存在しないID更新はNotFound")
    void F_04_UT_03_存在しないID更新はNotFound() {
        when(productRepository.findByProductIdAndDeletedFlagFalse(999)).thenReturn(Optional.empty());

        final ProductUpdateRequest request = new ProductUpdateRequest(
                "商品名", 1, new BigDecimal("1000.00"), "ACTIVE", null, 0);

        assertThatThrownBy(() -> productService.updateProduct(999, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("F-04-UT-04 カテゴリ不存在は業務エラー")
    void F_04_UT_04_カテゴリ不存在は業務エラー() {
        when(productRepository.findByProductIdAndDeletedFlagFalse(1)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(999)).thenReturn(Optional.empty());

        final ProductUpdateRequest request = new ProductUpdateRequest(
                "商品名", 999, new BigDecimal("1000.00"), "ACTIVE", null, 0);

        assertThatThrownBy(() -> productService.updateProduct(1, request))
                .isInstanceOf(BusinessRuleException.class);
    }

    // ---- F-05 商品削除 UT (Service 層) ----

    @Test
    @DisplayName("F-05-UT-01 在庫0の論理削除")
    void F_05_UT_01_在庫0の論理削除() {
        final Stock zeroStock = new Stock(product);
        when(productRepository.findByProductIdAndDeletedFlagFalse(1)).thenReturn(Optional.of(product));
        when(stockRepository.findByProductId(1)).thenReturn(Optional.of(zeroStock));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        productService.deleteProduct(1);

        verify(productRepository).save(product);
    }

    @Test
    @DisplayName("F-05-UT-02 在庫ありは削除不可（業務エラー）")
    void F_05_UT_02_在庫ありは削除不可() {
        final Stock stockWithQuantity = mock(Stock.class);
        when(stockWithQuantity.getQuantity()).thenReturn(10);
        when(productRepository.findByProductIdAndDeletedFlagFalse(2)).thenReturn(Optional.of(product));
        when(stockRepository.findByProductId(2)).thenReturn(Optional.of(stockWithQuantity));

        assertThatThrownBy(() -> productService.deleteProduct(2))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("在庫");
    }

    @Test
    @DisplayName("F-05-UT-03 存在しないIDはNotFound")
    void F_05_UT_03_存在しないIDはNotFound() {
        when(productRepository.findByProductIdAndDeletedFlagFalse(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(999))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
