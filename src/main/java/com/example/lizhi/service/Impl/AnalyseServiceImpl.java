package com.example.lizhi.service.Impl;

import com.example.lizhi.repository.AnalyseRepository;
import com.example.lizhi.service.AnalyseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyseServiceImpl implements AnalyseService {

    @Autowired
    private AnalyseRepository analyseRepository;

    // 已实现的方法
    @Override
    public Long getTotalOrderCount() {
        return analyseRepository.countAllOrders();
    }

    @Override
    public BigDecimal getTotalPurchaseAmountYuan() {
        BigDecimal total = analyseRepository.sumTotalPurchaseAmount();
        return total == null ? BigDecimal.ZERO : total;
    }

    // 2. 平均订单金额（元）：返回原始金额（元）
    @Override
    public BigDecimal getAverageOrderAmountYuan() {
        Long totalCount = getTotalOrderCount();
        if (totalCount == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalAmount = getTotalPurchaseAmountYuan(); // 复用总金额（元）
        if (totalAmount == null) {
            return BigDecimal.ZERO;
        }

        // 总金额（元）÷ 订单数 = 平均金额（元）
        return totalAmount.divide(new BigDecimal(totalCount), 2, RoundingMode.HALF_UP);
    }

    @Override
    public String getCompletedOrderRatio() {
        Long totalCount = getTotalOrderCount();
        if (totalCount == 0) {
            return "0%"; // 无订单时显示0%
        }

        Long receivedCount = analyseRepository.countReceivedOrders();
        if (receivedCount == null) {
            receivedCount = 0L;
        }

        // 计算百分比：(已接收订单数 ÷ 总订单数) × 100，保留1位小数
        BigDecimal ratio = new BigDecimal(receivedCount)
                .divide(new BigDecimal(totalCount), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100))
                .setScale(1, RoundingMode.HALF_UP);

        return ratio + "%";
    }

    // 趋势图数据：按时间维度统计
    @Override
    public List<Map<String, Object>> getPurchaseTrend(LocalDateTime start, LocalDateTime end, String timeUnit) {
        String format = switch (timeUnit) {
            case "week" -> "%Y-%u";   // 年-周
            case "month" -> "%Y-%m";  // 年-月
            case "year" -> "%Y";      // 年
            default -> "%Y-%m-%d";    // 天（自定义时间）
        };

        List<Object[]> results = analyseRepository.sumTotalPriceByTimeRange(start, end, format);
        List<Map<String, Object>> trendData = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> data = new HashMap<>();
            data.put("time", row[0].toString());    // 时间标签（如 2025-08）

            // 元 → 万元转换（保留2位小数）
            BigDecimal amountYuan = (BigDecimal) row[1];
            BigDecimal amountTenThousand = amountYuan.divide(new BigDecimal("10000"), 2, RoundingMode.HALF_UP);
            data.put("amount", amountTenThousand);  // 直接返回万元数值

            trendData.add(data);
        }
        return trendData;
    }
}
