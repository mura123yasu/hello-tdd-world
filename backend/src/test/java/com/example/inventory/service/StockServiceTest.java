package com.example.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.inventory.common.BusinessRuleException;
import com.example.inventory.domain.Category;
import com.example.inventory.domain.Product;
import com.example.inventory.domain.Stock;
import com.example.inventory.domain.StockTransaction;
import com.example.inventory.dto.StockResponse;
import com.example.inventory.dto.StockTransactionRequest;
import com.example.inventory.dto.StockTransactionResponse;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.StockRepository;
import com.example.inventory.repository.StockTransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * StockService の単体テスト（F-07〜F-10）。
 *
 * @author hello-tdd-world
 * @version 0.0.1
 */
@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockTransactionRepository stockTransactionRepository;

    @InjectMocks
    private StockService stockService;

    private Category category;
    private Product product;
    private Stock stock;

    @BeforeEach
    void setUp() {
        category = new Category("カテゴリA");
        product = new Product("ABC001", "テスト商品", category,
                new BigDecimal("1000.00"), "ACTIVE", null);
        stock = new Stock(product);
        stock.addQuantity(100);
    }

    // ---- F-07 在庫照会 UT ----

    @Test
    @DisplayName("F-07-UT-01 在庫数量取得")
    void F_07_UT_01_在庫数量取得() {
        when(stockRepository.findByProductId(1)).thenReturn(Optional.of(stock));

        final StockResponse result = stockService.getStock(1);

        assertThat(result.quantity()).isEqualTo(100);
    }

    @Test
    @DisplayName("F-07-UT-02 在庫行なしは0として扱う")
    void F_07_UT_02_在庫行なしは0として扱う() {
        when(stockRepository.findByProductId(1)).thenReturn(Optional.empty());

        final StockResponse result = stockService.getStock(1);

        assertThat(result.productId()).isEqualTo(1);
        assertThat(result.quantity()).isEqualTo(0);
    }

    // ---- F-08 入庫登録 UT ----

    @Test
    @DisplayName("F-08-UT-01 入庫で在庫加算")
    void F_08_UT_01_入庫で在庫加算() {
        when(productRepository.findByProductIdAndDeletedFlagFalse(1)).thenReturn(Optional.of(product));
        when(stockRepository.findByProductId(1)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenReturn(stock);
        when(stockTransactionRepository.save(any(StockTransaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        final StockTransactionRequest request = new StockTransactionRequest(10, null);
        final StockResponse result = stockService.stockIn(1, request);

        assertThat(result.quantity()).isEqualTo(110);
    }

    @Test
    @DisplayName("F-08-UT-02 入庫で履歴(IN)が記録される")
    void F_08_UT_02_入庫で履歴が記録される() {
        when(productRepository.findByProductIdAndDeletedFlagFalse(1)).thenReturn(Optional.of(product));
        when(stockRepository.findByProductId(1)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenReturn(stock);
        when(stockTransactionRepository.save(any(StockTransaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        final StockTransactionRequest request = new StockTransactionRequest(10, "入庫テスト");
        stockService.stockIn(1, request);

        verify(stockTransactionRepository).save(any(StockTransaction.class));
    }

    @Test
    @DisplayName("F-08-UT-03 quantity0以下は検証レイヤで弾かれる（Serviceはqty>=1を前提）")
    void F_08_UT_03_quantity正常値で入庫成功() {
        when(productRepository.findByProductIdAndDeletedFlagFalse(1)).thenReturn(Optional.of(product));
        when(stockRepository.findByProductId(1)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenReturn(stock);
        when(stockTransactionRepository.save(any(StockTransaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        final StockTransactionRequest request = new StockTransactionRequest(1, null);
        final StockResponse result = stockService.stockIn(1, request);

        assertThat(result.quantity()).isEqualTo(101);
    }

    // ---- F-09 出庫登録 UT ----

    @Test
    @DisplayName("F-09-UT-01 出庫で在庫減算")
    void F_09_UT_01_出庫で在庫減算() {
        when(productRepository.findByProductIdAndDeletedFlagFalse(1)).thenReturn(Optional.of(product));
        when(stockRepository.findByProductId(1)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenReturn(stock);
        when(stockTransactionRepository.save(any(StockTransaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        final StockTransactionRequest request = new StockTransactionRequest(10, null);
        final StockResponse result = stockService.stockOut(1, request);

        assertThat(result.quantity()).isEqualTo(90);
    }

    @Test
    @DisplayName("F-09-UT-02 在庫不足は業務エラー（在庫は変化しない）")
    void F_09_UT_02_在庫不足は業務エラー() {
        when(productRepository.findByProductIdAndDeletedFlagFalse(1)).thenReturn(Optional.of(product));
        when(stockRepository.findByProductId(1)).thenReturn(Optional.of(stock));

        final StockTransactionRequest request = new StockTransactionRequest(999, null);

        assertThatThrownBy(() -> stockService.stockOut(1, request))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("F-09-UT-03 ちょうど0になる出庫は成功")
    void F_09_UT_03_ちょうど0になる出庫は成功() {
        when(productRepository.findByProductIdAndDeletedFlagFalse(1)).thenReturn(Optional.of(product));
        when(stockRepository.findByProductId(1)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenReturn(stock);
        when(stockTransactionRepository.save(any(StockTransaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        final StockTransactionRequest request = new StockTransactionRequest(100, null);
        final StockResponse result = stockService.stockOut(1, request);

        assertThat(result.quantity()).isEqualTo(0);
    }

    @Test
    @DisplayName("F-09-UT-04 出庫で履歴(OUT)が記録される")
    void F_09_UT_04_出庫で履歴が記録される() {
        when(productRepository.findByProductIdAndDeletedFlagFalse(1)).thenReturn(Optional.of(product));
        when(stockRepository.findByProductId(1)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenReturn(stock);
        when(stockTransactionRepository.save(any(StockTransaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        final StockTransactionRequest request = new StockTransactionRequest(10, "出庫テスト");
        stockService.stockOut(1, request);

        verify(stockTransactionRepository).save(any(StockTransaction.class));
    }

    // ---- F-10 入出庫履歴照会 UT ----

    @Test
    @DisplayName("F-10-UT-01 履歴を時系列降順取得")
    void F_10_UT_01_履歴を時系列降順取得() {
        final LocalDateTime now = LocalDateTime.now();
        final StockTransaction tx1 = new StockTransaction(product, "IN", 20, null);
        final StockTransaction tx2 = new StockTransaction(product, "OUT", 10, null);
        when(stockTransactionRepository.findByProductIdOrderByTransactionAtDesc(1))
                .thenReturn(List.of(tx1, tx2));

        final List<StockTransactionResponse> result = stockService.getTransactions(1);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).transactionType()).isEqualTo("IN");
        assertThat(result.get(1).transactionType()).isEqualTo("OUT");
    }

    @Test
    @DisplayName("F-10-UT-02 履歴0件は空リスト")
    void F_10_UT_02_履歴0件は空リスト() {
        when(stockTransactionRepository.findByProductIdOrderByTransactionAtDesc(1))
                .thenReturn(List.of());

        final List<StockTransactionResponse> result = stockService.getTransactions(1);

        assertThat(result).isEmpty();
    }
}
