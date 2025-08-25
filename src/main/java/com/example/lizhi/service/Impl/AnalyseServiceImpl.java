package com.example.lizhi.service.Impl;
import com.example.lizhi.repository.AnalyseRepository;
import com.example.lizhi.service.AnalyseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
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
    public Long getTotalOrderCountByUser(Long userId) {
        // 假设PurchaseOrder实体中有user属性关联用户，通过JPQL查询
        return analyseRepository.countOrdersByUserId(userId);
    }

    @Override
    public BigDecimal getTotalPurchaseAmountYuanByUser(Long userId) {
        BigDecimal total = analyseRepository.sumTotalPriceByUserId(userId);
        return total == null ? BigDecimal.ZERO : total;
    }

    @Override
    public BigDecimal getAverageOrderAmountYuanByUser(Long userId) {
        Long totalCount = getTotalOrderCountByUser(userId);
        if (totalCount == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalAmount = getTotalPurchaseAmountYuanByUser(userId);
        return totalAmount == null? BigDecimal.ZERO :
                totalAmount.divide(new BigDecimal(totalCount), 2, RoundingMode.HALF_UP);
    }

    @Override
    public String getCompletedOrderRatioByUser(Long userId) {
        Long totalCount = getTotalOrderCountByUser(userId);
        if (totalCount == 0) {
            return "0%";
        }
        Long receivedCount = analyseRepository.countReceivedOrdersByUserId(userId);
        receivedCount = receivedCount == null? 0L : receivedCount;
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
    @Override
    public List<Map<String, Object>> getPurchaseTrendByUser(LocalDateTime start, LocalDateTime end, String timeUnit, Long userId) {
        // 时间分组格式（与全局趋势图保持一致）
        String format = switch (timeUnit) {
            case "week" -> "%Y-%m-%d";   // 周视图：按日分组
            case "month" -> "%Y-%m-%d";  // 月视图：按日分组
            case "year" -> "%Y-%m";      // 年视图：按月分组
            default -> "%Y-%m-%d";
        };

        // 调用Repository的用户+时间范围查询
        List<Object[]> results = analyseRepository.sumTotalPriceByUserAndTimeRange(userId, start, end, format);
        List<Map<String, Object>> trendData = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> data = new HashMap<>();
            data.put("time", row[0].toString());    // 时间标签
            BigDecimal amountYuan = (BigDecimal) row[1];
            // 元转万元，保留2位小数
            BigDecimal amountTenThousand = amountYuan.divide(new BigDecimal("10000"), 2, RoundingMode.HALF_UP);
            data.put("amount", amountTenThousand);
            trendData.add(data);
        }
        return trendData;
    }

    // 计算上月同期时间范围：上月1日00:00:00 到 上月最后1日23:59:59
    private LocalDateTime getLastMonthStart() {
        return LocalDateTime.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    private LocalDateTime getLastMonthEnd() {
        return LocalDateTime.now().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(999_999_999);
    }

    @Override
    public Long getLastMonthTotalOrderCount() {
        LocalDateTime lastMonthStart = getLastMonthStart();
        LocalDateTime lastMonthEnd = getLastMonthEnd();
        return analyseRepository.countOrdersByTimeRange(lastMonthStart, lastMonthEnd);
    }

    @Override
    public BigDecimal getLastMonthTotalPurchaseAmountYuan() {
        LocalDateTime lastMonthStart = getLastMonthStart();
        LocalDateTime lastMonthEnd = getLastMonthEnd();
        BigDecimal lastMonthTotal = analyseRepository.sumTotalPriceByTimeRange(lastMonthStart, lastMonthEnd);
        return lastMonthTotal == null ? BigDecimal.ZERO : lastMonthTotal;
    }

    @Override
    public Long getLastMonthReceivedOrderCount() {
        LocalDateTime lastMonthStart = getLastMonthStart();
        LocalDateTime lastMonthEnd = getLastMonthEnd();
        Long lastMonthReceived = analyseRepository.countReceivedOrdersByTimeRange(lastMonthStart, lastMonthEnd);
        return lastMonthReceived == null ? 0L : lastMonthReceived;
    }

    // 3. 新增：拼接“带图标+样式”的HTML片段（核心修改）
    private String buildGrowthHtml(String growthRate) {
        String iconClass;
        String displayText;
        // 根据增长率正负匹配图标和文本
        if (growthRate.startsWith("+")) {
            iconClass = "fa-arrow-up text-success";
            displayText = growthRate.substring(1); // 去掉“+”号，避免重复显示
        } else if (growthRate.startsWith("-")) {
            iconClass = "fa-arrow-down text-danger";
            displayText = growthRate.substring(1); // 去掉“-”号，由图标体现负增长
        } else {
            iconClass = "fa-minus text-secondary";
            displayText = growthRate;
        }
        // 返回完整HTML片段（包含图标和样式）
        return String.format("<i class='fa %s'></i> %s 较上月", iconClass, displayText);
    }

    // 新增：按用户计算所有指标的“较上月”数据（含HTML片段）
    public Map<String, Object> calculateMonthOnMonthGrowthByUser(LocalDateTime currentStart, LocalDateTime currentEnd, Long userId) {
        Map<String, Object> growthResult = new HashMap<>();

        // -------------------------- 1. 计算当前周期用户指标 --------------------------
        // 1.1 用户总采购订单数（当前周期）
        Long currentTotalOrder = analyseRepository.countOrdersByUserIdAndTimeRange(userId, currentStart, currentEnd);
        currentTotalOrder = currentTotalOrder == null ? 0L : currentTotalOrder;
        // 1.2 用户总采购金额（当前周期，元）
        BigDecimal currentTotalAmount = analyseRepository.sumTotalPriceByUserIdAndTimeRange(userId, currentStart, currentEnd);
        currentTotalAmount = currentTotalAmount == null ? BigDecimal.ZERO : currentTotalAmount;
        // 1.3 用户已完成订单数（当前周期）
        Long currentReceivedOrder = analyseRepository.countReceivedOrdersByUserIdAndTimeRange(userId, currentStart, currentEnd);
        currentReceivedOrder = currentReceivedOrder == null ? 0L : currentReceivedOrder;
        // 1.4 用户平均订单金额（当前周期，元）
        BigDecimal currentAvgAmount = currentTotalOrder == 0 ? BigDecimal.ZERO :
                currentTotalAmount.divide(new BigDecimal(currentTotalOrder), 2, RoundingMode.HALF_UP);
        // 1.5 用户已完成订单占比（当前周期）
        BigDecimal currentRatio = currentTotalOrder == 0 ? BigDecimal.ZERO :
                new BigDecimal(currentReceivedOrder).divide(new BigDecimal(currentTotalOrder), 4, RoundingMode.HALF_UP);

        // -------------------------- 2. 计算上月同期用户指标 --------------------------
        LocalDateTime lastMonthStart = getLastMonthStart();
        LocalDateTime lastMonthEnd = getLastMonthEnd();
        // 2.1 用户总采购订单数（上月同期）
        Long lastMonthTotalOrder = analyseRepository.countOrdersByUserIdAndTimeRange(userId, lastMonthStart, lastMonthEnd);
        lastMonthTotalOrder = lastMonthTotalOrder == null ? 0L : lastMonthTotalOrder;
        // 2.2 用户总采购金额（上月同期，元）
        BigDecimal lastMonthTotalAmount = analyseRepository.sumTotalPriceByUserIdAndTimeRange(userId, lastMonthStart, lastMonthEnd);
        lastMonthTotalAmount = lastMonthTotalAmount == null ? BigDecimal.ZERO : lastMonthTotalAmount;
        // 2.3 用户已完成订单数（上月同期）
        Long lastMonthReceivedOrder = analyseRepository.countReceivedOrdersByUserIdAndTimeRange(userId, lastMonthStart, lastMonthEnd);
        lastMonthReceivedOrder = lastMonthReceivedOrder == null ? 0L : lastMonthReceivedOrder;
        // 2.4 用户平均订单金额（上月同期，元）
        BigDecimal lastMonthAvgAmount = lastMonthTotalOrder == 0 ? BigDecimal.ZERO :
                lastMonthTotalAmount.divide(new BigDecimal(lastMonthTotalOrder), 2, RoundingMode.HALF_UP);
        // 2.5 用户已完成订单占比（上月同期）
        BigDecimal lastMonthRatio = lastMonthTotalOrder == 0 ? BigDecimal.ZERO :
                new BigDecimal(lastMonthReceivedOrder).divide(new BigDecimal(lastMonthTotalOrder), 4, RoundingMode.HALF_UP);

        // -------------------------- 3. 计算增长率 + 拼接HTML片段 --------------------------
        // 3.1 总采购订单数：增长率 + HTML片段
        String orderGrowth = calculateGrowthRate(new BigDecimal(currentTotalOrder), new BigDecimal(lastMonthTotalOrder));
        growthResult.put("orderGrowth", orderGrowth);
        growthResult.put("orderGrowthHtml", buildGrowthHtml(orderGrowth));
        // 3.2 总采购金额：增长率 + HTML片段
        String amountGrowth = calculateGrowthRate(currentTotalAmount, lastMonthTotalAmount);
        growthResult.put("amountGrowth", amountGrowth);
        growthResult.put("amountGrowthHtml", buildGrowthHtml(amountGrowth));
        // 3.3 平均订单金额：增长率 + HTML片段
        String avgAmountGrowth = calculateGrowthRate(currentAvgAmount, lastMonthAvgAmount);
        growthResult.put("avgAmountGrowth", avgAmountGrowth);
        growthResult.put("avgAmountGrowthHtml", buildGrowthHtml(avgAmountGrowth));
        // 3.4 已完成订单占比：增长率 + HTML片段
        String ratioGrowth = calculateGrowthRate(currentRatio, lastMonthRatio);
        growthResult.put("ratioGrowth", ratioGrowth);
        growthResult.put("ratioGrowthHtml", buildGrowthHtml(ratioGrowth));

        return growthResult;
    }
    // 4. 对外提供：计算所有指标的“较上月”数据（含HTML片段）
    public Map<String, Object> calculateMonthOnMonthGrowth(LocalDateTime currentStart, LocalDateTime currentEnd) {
        Map<String, Object> growthResult = new HashMap<>();

        // -------------------------- 1. 计算当前周期指标 --------------------------
        // 1.1 总采购订单数（当前周期）
        Long currentTotalOrder = analyseRepository.countOrdersByTimeRange(currentStart, currentEnd);
        currentTotalOrder = currentTotalOrder == null ? 0L : currentTotalOrder;
        // 1.2 总采购金额（当前周期，元）
        BigDecimal currentTotalAmount = analyseRepository.sumTotalPriceByTimeRange(currentStart, currentEnd);
        currentTotalAmount = currentTotalAmount == null ? BigDecimal.ZERO : currentTotalAmount;
        // 1.3 已完成订单数（当前周期）
        Long currentReceivedOrder = analyseRepository.countReceivedOrdersByTimeRange(currentStart, currentEnd);
        currentReceivedOrder = currentReceivedOrder == null ? 0L : currentReceivedOrder;
        // 1.4 平均订单金额（当前周期，元）
        BigDecimal currentAvgAmount = currentTotalOrder == 0 ? BigDecimal.ZERO :
                currentTotalAmount.divide(new BigDecimal(currentTotalOrder), 2, RoundingMode.HALF_UP);
        // 1.5 已完成订单占比（当前周期）
        BigDecimal currentRatio = currentTotalOrder == 0 ? BigDecimal.ZERO :
                new BigDecimal(currentReceivedOrder).divide(new BigDecimal(currentTotalOrder), 4, RoundingMode.HALF_UP);

        // -------------------------- 2. 计算上月同期指标 --------------------------
        LocalDateTime lastMonthStart = getLastMonthStart();
        LocalDateTime lastMonthEnd = getLastMonthEnd();
        // 2.1 总采购订单数（上月同期）
        Long lastMonthTotalOrder = analyseRepository.countOrdersByTimeRange(lastMonthStart, lastMonthEnd);
        lastMonthTotalOrder = lastMonthTotalOrder == null ? 0L : lastMonthTotalOrder;
        // 2.2 总采购金额（上月同期，元）
        BigDecimal lastMonthTotalAmount = analyseRepository.sumTotalPriceByTimeRange(lastMonthStart, lastMonthEnd);
        lastMonthTotalAmount = lastMonthTotalAmount == null ? BigDecimal.ZERO : lastMonthTotalAmount;
        // 2.3 已完成订单数（上月同期）
        Long lastMonthReceivedOrder = analyseRepository.countReceivedOrdersByTimeRange(lastMonthStart, lastMonthEnd);
        lastMonthReceivedOrder = lastMonthReceivedOrder == null ? 0L : lastMonthReceivedOrder;
        // 2.4 平均订单金额（上月同期，元）
        BigDecimal lastMonthAvgAmount = lastMonthTotalOrder == 0 ? BigDecimal.ZERO :
                lastMonthTotalAmount.divide(new BigDecimal(lastMonthTotalOrder), 2, RoundingMode.HALF_UP);
        // 2.5 已完成订单占比（上月同期）
        BigDecimal lastMonthRatio = lastMonthTotalOrder == 0 ? BigDecimal.ZERO :
                new BigDecimal(lastMonthReceivedOrder).divide(new BigDecimal(lastMonthTotalOrder), 4, RoundingMode.HALF_UP);

        // -------------------------- 3. 计算增长率 + 拼接HTML片段 --------------------------
        // 3.1 总采购订单数：增长率 + HTML片段
        String orderGrowth = calculateGrowthRate(new BigDecimal(currentTotalOrder), new BigDecimal(lastMonthTotalOrder));
        growthResult.put("orderGrowth", orderGrowth); // 纯增长率字符串（备用）
        growthResult.put("orderGrowthHtml", buildGrowthHtml(orderGrowth)); // 带样式的HTML片段
        // 3.2 总采购金额：增长率 + HTML片段
        String amountGrowth = calculateGrowthRate(currentTotalAmount, lastMonthTotalAmount);
        growthResult.put("amountGrowth", amountGrowth);
        growthResult.put("amountGrowthHtml", buildGrowthHtml(amountGrowth));
        // 3.3 平均订单金额：增长率 + HTML片段
        String avgAmountGrowth = calculateGrowthRate(currentAvgAmount, lastMonthAvgAmount);
        growthResult.put("avgAmountGrowth", avgAmountGrowth);
        growthResult.put("avgAmountGrowthHtml", buildGrowthHtml(avgAmountGrowth));
        // 3.4 已完成订单占比：增长率 + HTML片段
        String ratioGrowth = calculateGrowthRate(currentRatio, lastMonthRatio);
        growthResult.put("ratioGrowth", ratioGrowth);
        growthResult.put("ratioGrowthHtml", buildGrowthHtml(ratioGrowth));

        return growthResult;
    }

    // 通用增长率计算方法
    private String calculateGrowthRate(BigDecimal current, BigDecimal lastMonth) {
        if (lastMonth.compareTo(BigDecimal.ZERO) == 0) {
            if (current.compareTo(BigDecimal.ZERO) > 0) {
                return "+100.0%";
            } else {
                return "0.0%";
            }
        }
        // 增长率 = (当前 - 上月) / 上月 * 100%，保留1位小数
        BigDecimal growth = current.subtract(lastMonth).divide(lastMonth, 3, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
        String prefix = growth.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        return prefix + growth.setScale(1, RoundingMode.HALF_UP) + "%";
    }

    @Override
    public Map<String, Double> getPurchaseDistributionByVariety() {
        List<Object[]> results = analyseRepository.countPurchaseQuantityByVariety();
        Map<String, Double> varietyDistribution = new HashMap<>();
        for (Object[] row : results) {
            String variety = (String) row[0];
            BigDecimal quantity = (BigDecimal) row[1];
            varietyDistribution.put(variety, quantity.doubleValue());
        }
        return varietyDistribution;
    }

    @Override
    public Map<String, Double> getSupplierPurchaseRatio() {
        List<Object[]> results = analyseRepository.countPurchaseAmountBySupplier();
        Map<String, Double> supplierRatio = new HashMap<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Object[] row : results) {
            BigDecimal amount = (BigDecimal) row[1];
            totalAmount = totalAmount.add(amount);
        }
        for (Object[] row : results) {
            String supplier = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            double ratio = amount.divide(totalAmount, 4, BigDecimal.ROUND_HALF_UP).doubleValue() * 100;
            supplierRatio.put(supplier, ratio);
        }
        return supplierRatio;
    }
}