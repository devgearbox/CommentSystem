package com.example.lizhi.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AnalyseService {
    Long getTotalOrderCount();
    BigDecimal getTotalPurchaseAmountYuan(); // 总采购金额（元）
    BigDecimal getAverageOrderAmountYuan();  // 平均订单金额（元）
    String getCompletedOrderRatio();
    // 时间范围筛选（用于趋势图）
    List<Map<String, Object>> getPurchaseTrend(LocalDateTime start, LocalDateTime end, String timeUnit);
    // 获取上月同期总采购订单数
    Long getLastMonthTotalOrderCount();
    // 获取上月同期总采购金额（元）
    BigDecimal getLastMonthTotalPurchaseAmountYuan();
    // 获取上月同期已完成订单数
    Long getLastMonthReceivedOrderCount();
    // 计算各指标较上月增长率（返回Map，包含4个指标的增长率字符串）
    Map<String, Object> calculateMonthOnMonthGrowth(LocalDateTime currentStart, LocalDateTime currentEnd);

    // 获取按品种采购分布数据
    Map<String, Double> getPurchaseDistributionByVariety();

    // 获取供应商采购占比数据
    Map<String, Double> getSupplierPurchaseRatio();
}
