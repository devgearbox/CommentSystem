package com.example.lizhi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "return_order")
@Data
public class ReturnOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer return_id;

    @Column(name = "return_no", unique = true, nullable = false)
    private String returnNo;

    @Column(name = "order_no", nullable = false)
    private String orderNo;

    private String litchi_variety;

    private BigDecimal quantity;

    private String reason;

    @Enumerated(EnumType.STRING)
    private ReturnStatus return_status;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "operator_name")
    private String operatorName;

    @Column(name = "operator_id")
    private Integer operatorId;

    @Column(name = "refund_amount")
    private BigDecimal refundAmount;

    @Column(name = "supplier_name")
    private String supplierName;

    @Column(name = "purchaser_name")
    private String purchaserName;

    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        if (this.return_status == null) {
            this.return_status = ReturnStatus.pending;
        }
        if (this.returnNo == null) {
            this.returnNo = "RET" + System.currentTimeMillis();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }

    public enum ReturnStatus {
        pending("待审核"),
        approved("已审核"),
        refunded("已退款"),
        completed("已完成"),
        rejected("已拒绝");

        private final String label;

        ReturnStatus(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}