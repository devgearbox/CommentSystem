package com.example.lizhi.controller;
import com.example.lizhi.service.AnalyseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AnalyseController {
    @Autowired
    private AnalyseService analyseService;

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

        // 传递timeUnit到Service（无需单独传递format，Service内部已映射）
        result.put("trendData", analyseService.getPurchaseTrend(start, end, timeUnit));
        return result;
    }
}