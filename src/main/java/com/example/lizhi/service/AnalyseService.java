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
}
