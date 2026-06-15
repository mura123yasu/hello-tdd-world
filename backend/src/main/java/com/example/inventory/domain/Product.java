package com.example.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品エンティティ。楽観ロックのために version を持つ。
 *
 * @author hello-tdd-world
 * @version 0.0.1
 */
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "product_code", nullable = false, unique = true, length = 20)
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "status", nullable = false, length = 10)
    private String status;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "deleted_flag", nullable = false)
    private boolean deletedFlag;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** JPA 用デフォルトコンストラクタ。 */
    protected Product() {
    }

    /**
     * 商品を生成する。
     *
     * @param productCode 商品コード
     * @param productName 商品名
     * @param category    カテゴリ
     * @param unitPrice   単価
     * @param status      ステータス
     * @param description 説明（null 可）
     */
    public Product(final String productCode, final String productName, final Category category,
            final BigDecimal unitPrice, final String status, final String description) {
        this.productCode = productCode;
        this.productName = productName;
        this.category = category;
        this.unitPrice = unitPrice;
        this.status = status;
        this.description = description;
        this.deletedFlag = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getProductId() {
        return productId;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getProductName() {
        return productName;
    }

    public Category getCategory() {
        return category;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public String getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public boolean isDeletedFlag() {
        return deletedFlag;
    }

    public Integer getVersion() {
        return version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 商品情報を更新する。
     *
     * @param productName 商品名
     * @param category    カテゴリ
     * @param unitPrice   単価
     * @param status      ステータス
     * @param description 説明（null 可）
     */
    public void update(final String productName, final Category category,
            final BigDecimal unitPrice, final String status, final String description) {
        this.productName = productName;
        this.category = category;
        this.unitPrice = unitPrice;
        this.status = status;
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 楽観ロック用バージョンを設定する。
     * クライアントから送られた version を JPA の @Version フィールドにセットすることで、
     * DB の version と一致しない場合に OptimisticLockingFailureException をスローさせる。
     *
     * @param version クライアントから送られたバージョン
     */
    public void setVersion(final int version) {
        this.version = version;
    }

    /**
     * 論理削除する。
     */
    public void delete() {
        this.deletedFlag = true;
        this.updatedAt = LocalDateTime.now();
    }
}
