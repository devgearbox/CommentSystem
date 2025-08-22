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

    @Override
    public Long getTotalOrderCount() {
        return analyseRepository.countAllOrders();
    }

    @Override
    public BigDecimal getTotalPurchaseAmountYuan() {
        BigDecimal total = analyseRepository.sumTotalPurchaseAmount();
        return total == null ? BigDecimal.ZERO : total;
    }

    @Override
    public BigDecimal getAverageOrderAmountYuan() {
        Long totalCount = getTotalOrderCount();
        if (totalCount == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalAmount = getTotalPurchaseAmountYuan();
        return totalAmount == null ? BigDecimal.ZERO :
                totalAmount.divide(new BigDecimal(totalCount), 2, RoundingMode.HALF_UP);
    }

    @Override
    public String getCompletedOrderRatio() {
        Long totalCount = getTotalOrderCount();
        if (totalCount == 0) {
            return "0%";
        }
        Long receivedCount = analyseRepository.countReceivedOrders();
        receivedCount = receivedCount == null ? 0L : receivedCount;
        BigDecimal ratio = new BigDecimal(receivedCount)
                .divide(new BigDecimal(totalCount), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100))
                .setScale(1, RoundingMode.HALF_UP);
        return ratio + "%";
    }

    @Override
    public List<Map<String, Object>> getPurchaseTrend(LocalDateTime start, LocalDateTime end, String timeUnit) {
        // 关键调整：按时间单位定义分组格式（周视图按日分组，用于计算每日平均值）
        String format = switch (timeUnit) {
            case "week" -> "%Y-%m-%d";   // 周视图：按“日”分组（后续计算每日平均值）
            case "month" -> "%Y-%m-%d";  // 月视图：按“日”分组（后续按周/按日聚合）
            case "year" -> "%Y-%m";      // 年视图：按“月”分组（后续按偶数月聚合）
            default -> "%Y-%m-%d";       // 自定义：按日分组
        };

        List<Object[]> results = analyseRepository.sumTotalPriceByTimeRange(start, end, format);
        List<Map<String, Object>> trendData = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> data = new HashMap<>();
            data.put("time", row[0].toString());    // 时间标签（日：2025-08-14；月：2025-08）
            BigDecimal amountYuan = (BigDecimal) row[1];
            // 元转万元，保留2位小数
            BigDecimal amountTenThousand = amountYuan.divide(new BigDecimal("10000"), 2, RoundingMode.HALF_UP);
            data.put("amount", amountTenThousand);
            trendData.add(data);
        }
        return trendData;
    }
}