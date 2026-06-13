package com.example.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 在庫エンティティ。商品と 1:1 の関係。
 *
 * @author hello-tdd-world
 * @version 0.0.1
 */
@Entity
@Table(name = "stock")
public class Stock {

    @Id
    @Column(name = "product_id")
    private Integer productId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** JPA 用デフォルトコンストラクタ。 */
    protected Stock() {
    }

    /**
     * 在庫を生成する（商品登録時に quantity=0 で初期化）。
     *
     * @param product 対応商品
     */
    public Stock(final Product product) {
        this.product = product;
        this.quantity = 0;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getProductId() {
        return productId;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 入庫により在庫数量を加算する。
     *
     * @param amount 加算数量（正値）
     */
    public void addQuantity(final int amount) {
        this.quantity += amount;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 出庫により在庫数量を減算する。
     *
     * @param amount 減算数量（正値）
     * @throws com.example.inventory.common.BusinessRuleException 在庫不足の場合
     */
    public void subtractQuantity(final int amount) {
        if (this.quantity - amount < 0) {
            throw new com.example.inventory.common.BusinessRuleException(
                    "在庫が不足しています。現在庫: " + this.quantity + ", 出庫数量: " + amount);
        }
        this.quantity -= amount;
        this.updatedAt = LocalDateTime.now();
    }
}
