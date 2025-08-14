package com.example.lizhi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
@Data
@Entity
@Table(name = "purchase_order")
public class PurchaseOrder{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer order_id;

    @Column(name = "order_no")
    private String orderNo;

    private BigDecimal purchase_quantity;

    @Enumerated(EnumType.STRING)
    private OrderStatus order_status;
    public enum OrderStatus {
        pending("待审核"),
        shipping("待发货"),
        shipped("已发货"),
        received("已接收"),
        cancelled("已取消"),   // 已取消
        rejected("拒收");
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

    //关联user表
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "purchaser_id")
    private User user;

    //关联litchi_variety表
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "purchase_variety",
            referencedColumnName = "variety_name",
            insertable = false,  // 禁止通过此关联字段插入
            updatable = false)    // 禁止通过此关联字段更新
    private LitchiVariety litchiVariety;

    @Temporal(TemporalType.TIMESTAMP)
    private Date create_time;

    private BigDecimal total_price;

    // 构造方法
    public PurchaseOrder() {
    }

//    @Override
//    public String toString() {
//        return "PurchaseOrder{" +
//                "orderId=" + order_id +
//                ", orderNo='" + order_no + '\'' +
//                ", purchaseQuantity=" + purchase_quantity +
//                ", orderStatus=" + order_status +
//                ", purchaseVariety='" + purchase_variety + '\'' +
//                ", createTime=" + create_time +
//                '}';
//    }
}
