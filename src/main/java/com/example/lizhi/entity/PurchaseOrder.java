package com.example.lizhi.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
    // 新增软删除字段
    @Column(name = "is_deleted")
    private boolean isDeleted = false; // 默认未删除

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private OrderStatus orderStatus;
    public enum OrderStatus {
        pending("待审核"),
        paid("已支付"),
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
    @JoinColumn(name = "variety_id",  // 新增的外键字段
            referencedColumnName = "id",  // 关联到LitchiVariety的主键
            insertable = true,
            updatable = true)
    private LitchiVariety litchiVariety;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_time")
    @CreationTimestamp
    private Date createTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "update_time")
    @UpdateTimestamp
    private Date updateTime;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    // 构造方法
    public PurchaseOrder() {
    }
    // 根据枚举值获取中文标签的静态方法
    public static String getLabelByValue(String value) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.name().equals(value)) {
                return status.getLabel();
            }
        }
        return "未知状态";
    }
}
