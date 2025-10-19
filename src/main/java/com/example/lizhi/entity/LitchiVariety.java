package com.example.lizhi.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class LitchiVariety {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String variety_name;
    private BigDecimal price;
    private Integer stock;
    private String description;
    private String specification;

    @Column(name = "order_count")
    private Integer orderCount = 0;

    @Column(name = "image_path")
    private String imagePath;

    // 关联供应商（多对一）
    @ManyToOne(fetch = FetchType.EAGER) // EAGER 立即加载关联数据
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    // Getter 和 Setter 方法（可使用 Lombok 的 @Data 注解简化，需引入 Lombok 依赖）
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getVarietyName() {
        return variety_name;
    }

    public void setVarietyName(String varietyName) {
        this.variety_name = varietyName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public String getImagePath() {return imagePath;}

    public void setImagePath(String imagePath) {this.imagePath = imagePath;}

    public Integer getOrderCount() {return orderCount;}

    public void setOrderCount(Integer orderCount) {this.orderCount = orderCount;}

    public String getSpecification() {return specification;}

    public void setSpecification(String specification) {this.specification = specification;}
}