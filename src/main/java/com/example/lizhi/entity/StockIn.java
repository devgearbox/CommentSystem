package com.example.lizhi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "stock_in")
@Data
public class StockIn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_id")
    private Integer stockId;

    @Column(name = "order_no", unique = true, nullable = false)
    private String orderNo;

    private String litchi_variety;

    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    private StockInStatus stock_in_status;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    private LocalDateTime update_time;
    private LocalDateTime stock_in_time;
    private String operator_name;

    @Column(name = "operator_id")
    private Integer operatorId;
    @Transient
    private FreshnessStatus freshness_status;


    // 关联订单实体
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_no", referencedColumnName = "order_no", insertable = false, updatable = false)
    private PurchaseOrder order;

    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        this.update_time = LocalDateTime.now();
        this.stock_in_status = StockInStatus.pending;
    }

    // 计算保鲜状态的方法
    public FreshnessStatus getFreshness_status() {
        if (createTime == null) {
            return FreshnessStatus.UNKNOWN;
        }

        long daysBetween = ChronoUnit.DAYS.between(createTime, LocalDateTime.now());

        if (daysBetween <= 3) {
            return FreshnessStatus.FRESH;
        } else if (daysBetween <= 7) {
            return FreshnessStatus.WARNING;
        } else if (daysBetween <= 10) {
            return FreshnessStatus.URGENT;
        } else {
            return FreshnessStatus.EXPIRED;
        }
    }

    public enum StockInStatus {
        pending("待入库"),
        checking("验收中"),
        rejected("拒收"),
        partial("部分入库"),
        completed("已入库");


        private final String label;

        StockInStatus(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    // 保鲜状态枚举
    public enum FreshnessStatus {
        FRESH("新鲜", "fresh"),
        WARNING("预警", "warning"),
        URGENT("紧急", "urgent"),
        EXPIRED("过期", "expired"),
        UNKNOWN("未知", "unknown");

        private final String label;
        private final String cssClass;

        FreshnessStatus(String label, String cssClass) {
            this.label = label;
            this.cssClass = cssClass;
        }

        public String getLabel() {
            return label;
        }

        public String getCssClass() {
            return cssClass;
        }
    }
}