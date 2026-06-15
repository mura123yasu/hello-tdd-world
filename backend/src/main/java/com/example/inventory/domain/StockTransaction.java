package com.example.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 入出庫履歴エンティティ。
 *
 * @author hello-tdd-world
 * @version 0.0.1
 */
@Entity
@Table(name = "stock_transaction")
public class StockTransaction {

    /** 入庫区分。 */
    public static final String TYPE_IN = "IN";

    /** 出庫区分。 */
    public static final String TYPE_OUT = "OUT";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "transaction_type", nullable = false, length = 3)
    private String transactionType;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "transaction_at", nullable = false)
    private LocalDateTime transactionAt;

    @Column(name = "note", length = 200)
    private String note;

    /** JPA 用デフォルトコンストラクタ。 */
    protected StockTransaction() {
    }

    /**
     * 入出庫履歴を生成する。
     *
     * @param product         対象商品
     * @param transactionType 取引区分（IN / OUT）
     * @param quantity        数量
     * @param note            備考（null 可）
     */
    public StockTransaction(final Product product, final String transactionType,
            final int quantity, final String note) {
        this.product = product;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.transactionAt = LocalDateTime.now();
        this.note = note;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public Product getProduct() {
        return product;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDateTime getTransactionAt() {
        return transactionAt;
    }

    public String getNote() {
        return note;
    }
}
