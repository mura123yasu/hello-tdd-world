package com.example.inventory.service;

import com.example.inventory.common.ResourceNotFoundException;
import com.example.inventory.domain.Product;
import com.example.inventory.domain.Stock;
import com.example.inventory.domain.StockTransaction;
import com.example.inventory.dto.StockResponse;
import com.example.inventory.dto.StockTransactionRequest;
import com.example.inventory.dto.StockTransactionResponse;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.StockRepository;
import com.example.inventory.repository.StockTransactionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 在庫サービス（F-07〜F-10）。
 *
 * @author hello-tdd-world
 * @version 0.0.1
 */
@Service
@Transactional(readOnly = true)
public class StockService {

    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final StockTransactionRepository stockTransactionRepository;

    /**
     * コンストラクタインジェクション。
     *
     * @param productRepository          商品リポジトリ
     * @param stockRepository            在庫リポジトリ
     * @param stockTransactionRepository 入出庫履歴リポジトリ
     */
    public StockService(final ProductRepository productRepository,
            final StockRepository stockRepository,
            final StockTransactionRepository stockTransactionRepository) {
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
        this.stockTransactionRepository = stockTransactionRepository;
    }

    /**
     * 在庫数量を取得する（F-07）。在庫行がない場合は 0 を返す。
     *
     * @param productId 商品ID
     * @return 在庫応答
     */
    public StockResponse getStock(final int productId) {
        final int quantity = stockRepository.findByProductId(productId)
                .map(Stock::getQuantity)
                .orElse(0);
        return new StockResponse(productId, quantity);
    }

    /**
     * 入庫登録（F-08）。在庫を加算し履歴（IN）を記録する。
     *
     * @param productId 商品ID
     * @param request   入出庫リクエスト
     * @return 更新後の在庫応答
     * @throws ResourceNotFoundException 商品が存在しない場合
     */
    @Transactional
    public StockResponse stockIn(final int productId, final StockTransactionRequest request) {
        final Product product = findActiveProduct(productId);
        final Stock stock = findOrCreateStock(productId, product);
        stock.addQuantity(request.quantity());
        stockRepository.save(stock);
        stockTransactionRepository.save(
                new StockTransaction(product, StockTransaction.TYPE_IN, request.quantity(), request.note()));
        return new StockResponse(productId, stock.getQuantity());
    }

    /**
     * 出庫登録（F-09）。在庫を減算し履歴（OUT）を記録する。在庫不足は BusinessRuleException。
     *
     * @param productId 商品ID
     * @param request   入出庫リクエスト
     * @return 更新後の在庫応答
     * @throws ResourceNotFoundException                   商品が存在しない場合
     * @throws com.example.inventory.common.BusinessRuleException 在庫不足の場合
     */
    @Transactional
    public StockResponse stockOut(final int productId, final StockTransactionRequest request) {
        final Product product = findActiveProduct(productId);
        final Stock stock = findOrCreateStock(productId, product);
        stock.subtractQuantity(request.quantity());
        stockRepository.save(stock);
        stockTransactionRepository.save(
                new StockTransaction(product, StockTransaction.TYPE_OUT, request.quantity(), request.note()));
        return new StockResponse(productId, stock.getQuantity());
    }

    /**
     * 入出庫履歴を取引日時降順で取得する（F-10）。
     *
     * @param productId 商品ID
     * @return 履歴リスト（新しい順）
     */
    public List<StockTransactionResponse> getTransactions(final int productId) {
        return stockTransactionRepository.findByProductIdOrderByTransactionAtDesc(productId)
                .stream()
                .map(StockTransactionResponse::from)
                .toList();
    }

    private Product findActiveProduct(final int productId) {
        return productRepository.findByProductIdAndDeletedFlagFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "商品が見つかりません。ID: " + productId));
    }

    private Stock findOrCreateStock(final int productId, final Product product) {
        return stockRepository.findByProductId(productId)
                .orElseGet(() -> new Stock(product));
    }
}
