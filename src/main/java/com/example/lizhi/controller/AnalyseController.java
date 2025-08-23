package com.example.lizhi.controller;
import com.example.lizhi.repository.AnalyseRepository;
import com.example.lizhi.service.AnalyseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AnalyseController {
    @Autowired
    private AnalyseService analyseService;
    @Autowired
    private AnalyseRepository analyseRepository;

    @GetMapping("/analyse")
    public String showAnalysePage(Model model) {
        model.addAttribute("totalPurchaseAmountYuan", analyseService.getTotalPurchaseAmountYuan());
        model.addAttribute("averageOrderAmountYuan", analyseService.getAverageOrderAmountYuan());
        model.addAttribute("totalOrderCount", analyseService.getTotalOrderCount());
        model.addAttribute("completedOrderRatio", analyseService.getCompletedOrderRatio());
        // 初始化趋势图（本月数据）
        LocalDateTime thisMonthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime thisMonthEnd = LocalDateTime.now().withDayOfMonth(LocalDateTime.now().toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
        model.addAttribute("trendData", analyseService.getPurchaseTrend(thisMonthStart, thisMonthEnd, "month"));
        // 2. 新增：计算并传递较上月增长率
        // 1.3 新增：传递“较上月”HTML片段（核心修改）
        Map<String, Object> growthMap = analyseService.calculateMonthOnMonthGrowth(thisMonthStart, thisMonthEnd);
        model.addAttribute("orderGrowthHtml", growthMap.get("orderGrowthHtml")); // 总订单数HTML
        model.addAttribute("amountGrowthHtml", growthMap.get("amountGrowthHtml")); // 总金额HTML
        model.addAttribute("avgAmountGrowthHtml", growthMap.get("avgAmountGrowthHtml")); // 平均金额HTML
        model.addAttribute("ratioGrowthHtml", growthMap.get("ratioGrowthHtml")); // 完成率HTML

        // 传递按品种采购分布数据
        Map<String, Double> varietyDistribution = analyseService.getPurchaseDistributionByVariety();
        model.addAttribute("varietyDistribution", varietyDistribution);

        // 传递供应商采购占比数据
        Map<String, Double> supplierRatio = analyseService.getSupplierPurchaseRatio();
        model.addAttribute("supplierRatio", supplierRatio);

        return "analyse";
    }

    @GetMapping("/api/purchase/statistics")
    @ResponseBody
    public Map<String, Object> getStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "month") String timeUnit
    ) {
        Map<String, Object> result = new HashMap<>();
        // 关键指标（不变）
        result.put("totalOrderCount", analyseService.getTotalOrderCount());
        result.put("totalPurchaseAmount", analyseService.getTotalPurchaseAmountYuan());
        result.put("averageOrderAmount", analyseService.getAverageOrderAmountYuan());
        result.put("completedOrderRatio", analyseService.getCompletedOrderRatio());

        // 时间范围默认值（本月）
        if (start == null || end == null) {
            LocalDateTime now = LocalDateTime.now();
            start = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            end = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(999_999_999);
        }

        result.put("totalOrderCount", analyseRepository.countOrdersByTimeRange(start, end));
        BigDecimal totalAmount = analyseRepository.sumTotalPriceByTimeRange(start, end);
        result.put("totalPurchaseAmount", totalAmount == null ? BigDecimal.ZERO : totalAmount);
        Long totalOrder = analyseRepository.countOrdersByTimeRange(start, end);
        BigDecimal avgAmount = totalOrder == 0 ? BigDecimal.ZERO : totalAmount.divide(new BigDecimal(totalOrder), 2, RoundingMode.HALF_UP);
        result.put("averageOrderAmount", avgAmount);
        Long receivedOrder = analyseRepository.countReceivedOrdersByTimeRange(start, end);
        BigDecimal ratio = totalOrder == 0 ? BigDecimal.ZERO : new BigDecimal(receivedOrder).divide(new BigDecimal(totalOrder), 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).setScale(1, RoundingMode.HALF_UP);
        result.put("completedOrderRatio", ratio + "%");

        // 2.3 新增：传递“较上月”HTML片段（核心修改）
        Map<String, Object> growthMap = analyseService.calculateMonthOnMonthGrowth(start, end);
        result.put("orderGrowthHtml", growthMap.get("orderGrowthHtml"));
        result.put("amountGrowthHtml", growthMap.get("amountGrowthHtml"));
        result.put("avgAmountGrowthHtml", growthMap.get("avgAmountGrowthHtml"));
        result.put("ratioGrowthHtml", growthMap.get("ratioGrowthHtml"));
        // 传递timeUnit到Service（无需单独传递format，Service内部已映射）
        result.put("trendData", analyseService.getPurchaseTrend(start, end, timeUnit));
        return result;
    }
}