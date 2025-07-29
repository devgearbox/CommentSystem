package com.example.lizhi.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "purchase_order")
public class PurchaseOrder{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer order_id;

    private String order_no;
    private BigDecimal purchase_quantity;

    @Enumerated(EnumType.STRING)
    private OrderStatus order_status;
    public enum OrderStatus {
        pending("待审核"),
        approved("已批准"),
        purchased("已采购"),
        shipped("已发货"),
        received("已接收"),
        cancelled("已取消");   // 已取消
        private final String label;

        OrderStatus(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private String purchase_variety;

    //关联supplier表
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    //关联user表
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "purchaser_id")
    private User user;
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    @Temporal(TemporalType.TIMESTAMP)
    private Date create_time;

    // 构造方法
    public PurchaseOrder() {
    }

    // Getter和Setter方法
    public Integer getOrder_id() {
        return order_id;
    }

    public void setOrder_id(Integer order_id) {
        this.order_id = order_id;
    }

    public String getOrder_no() {
        return order_no;
    }

    public void setOrder_no(String order_no) {
        this.order_no = order_no;
    }

    public BigDecimal getPurchase_quantity() {
        return purchase_quantity;
    }

    public void setPurchase_quantity(BigDecimal purchase_quantity) {
        this.purchase_quantity = purchase_quantity;
    }

    public OrderStatus getOrder_status() {
        return order_status;
    }

    public void setOrder_status(OrderStatus order_status) {
        this.order_status = order_status;
    }

    public String getPurchase_variety() {
        return purchase_variety;
    }

    public void setPurchase_variety(String purchase_variety) {
        this.purchase_variety = purchase_variety;
    }

    public Date getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }

    @Override
    public String toString() {
        return "PurchaseOrder{" +
                "orderId=" + order_id +
                ", orderNo='" + order_no + '\'' +
                ", purchaseQuantity=" + purchase_quantity +
                ", orderStatus=" + order_status +
                ", purchaseVariety='" + purchase_variety + '\'' +
                ", createTime=" + create_time +
                '}';
    }
}
