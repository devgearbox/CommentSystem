package com.example.lizhi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_in")
@Data
public class StockIn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer stock_id;

    @Column(name = "order_no", unique = true, nullable = false)
    private String orderNo;

    private String litchi_variety;

    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    private StockInStatus stock_in_status;

    private LocalDateTime create_time;
    private String operator_name;
    private Integer operator_id;

    @PrePersist
    public void prePersist() {
        this.create_time = LocalDateTime.now();
        this.stock_in_status = StockInStatus.pending;
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
}