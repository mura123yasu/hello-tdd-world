package com.example.inventory.service;

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
import com.example.inventory.repository.ProductSpecification;
import com.example.inventory.repository.StockRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商品サービス（F-01〜F-05）。
 *
 * @author hello-tdd-world
 * @version 0.0.1
 */
@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StockRepository stockRepository;

    /**
     * コンストラクタインジェクション。
     *
     * @param productRepository  商品リポジトリ
     * @param categoryRepository カテゴリリポジトリ
     * @param stockRepository    在庫リポジトリ
     */
    public ProductService(final ProductRepository productRepository,
            final CategoryRepository categoryRepository,
            final StockRepository stockRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.stockRepository = stockRepository;
    }

    /**
     * 商品を検索する（F-01）。論理削除済みは除外。
     *
     * @param request 検索リクエスト
     * @return ページ応答
     */
    public PageResponse<ProductResponse> searchProducts(final ProductSearchRequest request) {
        final Sort sort = buildSort(request.sort());
        final PageRequest pageRequest = PageRequest.of(request.page(), request.size(), sort);
        final Page<Product> page = productRepository.findAll(
                ProductSpecification.of(request), pageRequest);
        return new PageResponse<>(
                page.getContent().stream().map(ProductResponse::from).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements());
    }

    /**
     * 商品詳細を取得する（F-02）。
     *
     * @param productId 商品ID
     * @return 商品応答
     * @throws ResourceNotFoundException 商品が存在しない場合
     */
    public ProductResponse getProduct(final int productId) {
        final Product product = findActiveProduct(productId);
        return ProductResponse.from(product);
    }

    /**
     * 商品を登録する（F-03）。在庫 0 で初期化する。
     *
     * @param request 商品登録リクエスト
     * @return 登録された商品応答
     * @throws BusinessRuleException 商品コード重複・カテゴリ不存在の場合
     */
    @Transactional
    public ProductResponse createProduct(final ProductCreateRequest request) {
        if (productRepository.existsByProductCode(request.productCode())) {
            throw new BusinessRuleException(
                    "商品コード「" + request.productCode() + "」は既に使用されています。");
        }
        final Category category = findCategory(request.categoryId());
        final Product product = new Product(
                request.productCode(),
                request.productName(),
                category,
                request.unitPrice(),
                request.status(),
                request.description());
        final Product saved = productRepository.save(product);
        stockRepository.save(new Stock(saved));
        return ProductResponse.from(saved);
    }

    /**
     * 商品を更新する（F-04）。楽観ロックを使用。
     *
     * @param productId 商品ID
     * @param request   商品更新リクエスト
     * @return 更新された商品応答
     * @throws ResourceNotFoundException    商品が存在しない場合
     * @throws BusinessRuleException        カテゴリ不存在の場合
     */
    @Transactional
    public ProductResponse updateProduct(final int productId, final ProductUpdateRequest request) {
        final Product product = findActiveProduct(productId);
        // 楽観ロック: クライアントが保持する version が現在の version と一致しない場合は競合とみなす。
        // （永続化済みエンティティの version は常に非 null。null の場合は検証をスキップする。）
        final Integer currentVersion = product.getVersion();
        if (currentVersion != null && !currentVersion.equals(request.version())) {
            throw new OptimisticLockingFailureException(
                    "他のユーザーによって更新されています。最新の状態を再取得してください。");
        }
        final Category category = findCategory(request.categoryId());
        product.update(request.productName(), category, request.unitPrice(),
                request.status(), request.description());
        final Product saved = productRepository.save(product);
        return ProductResponse.from(saved);
    }

    /**
     * 商品を論理削除する（F-05）。在庫がある場合は削除不可。
     *
     * @param productId 商品ID
     * @throws ResourceNotFoundException 商品が存在しない場合
     * @throws BusinessRuleException     在庫がある場合
     */
    @Transactional
    public void deleteProduct(final int productId) {
        final Product product = findActiveProduct(productId);
        stockRepository.findByProductId(productId).ifPresent(stock -> {
            if (stock.getQuantity() > 0) {
                throw new BusinessRuleException(
                        "在庫が残っている商品は削除できません。在庫数量: " + stock.getQuantity());
            }
        });
        product.delete();
        productRepository.save(product);
    }

    private Product findActiveProduct(final int productId) {
        return productRepository.findByProductIdAndDeletedFlagFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "商品が見つかりません。ID: " + productId));
    }

    private Category findCategory(final int categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessRuleException(
                        "カテゴリが見つかりません。ID: " + categoryId));
    }

    private Sort buildSort(final String sortParam) {
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "productId");
        }
        final String[] parts = sortParam.split(",");
        if (parts.length == 2) {
            final Sort.Direction direction = "desc".equalsIgnoreCase(parts[1])
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            return Sort.by(direction, parts[0].trim());
        }
        return Sort.by(Sort.Direction.ASC, parts[0].trim());
    }
}
