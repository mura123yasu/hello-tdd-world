package com.example.inventory.repository;

import com.example.inventory.domain.Product;
import com.example.inventory.dto.ProductSearchRequest;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/**
 * 商品検索条件を組み立てる Specification ファクトリ。
 *
 * @author hello-tdd-world
 * @version 0.0.1
 */
public final class ProductSpecification {

    private ProductSpecification() {
    }

    /**
     * 検索リクエストから Specification を組み立てる。
     * 論理削除済み（deleted_flag=1）は常に除外する。
     *
     * @param request 検索リクエスト
     * @return Specification
     */
    public static Specification<Product> of(final ProductSearchRequest request) {
        return (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();

            // 論理削除除外は常に適用
            predicates.add(cb.isFalse(root.get("deletedFlag")));

            if (request.code() != null && !request.code().isBlank()) {
                predicates.add(cb.like(root.get("productCode"), "%" + request.code() + "%"));
            }
            if (request.name() != null && !request.name().isBlank()) {
                predicates.add(cb.like(root.get("productName"), "%" + request.name() + "%"));
            }
            if (request.categoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("categoryId"), request.categoryId()));
            }
            if (request.status() != null && !request.status().isBlank()) {
                predicates.add(cb.equal(root.get("status"), request.status()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
