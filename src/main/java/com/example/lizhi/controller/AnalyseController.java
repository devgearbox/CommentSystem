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
        // 1. 总采购金额（元）
        model.addAttribute("totalPurchaseAmountYuan", analyseService.getTotalPurchaseAmountYuan());

        // 2. 平均订单金额（元）
        model.addAttribute("averageOrderAmountYuan", analyseService.getAverageOrderAmountYuan());

        // 3. 其他数据
        model.addAttribute("totalOrderCount", analyseService.getTotalOrderCount());
        model.addAttribute("completedOrderRatio", analyseService.getCompletedOrderRatio());
        // 初始化趋势图（本月数据）
        LocalDateTime thisMonthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime thisMonthEnd = LocalDateTime.now().withDayOfMonth(LocalDateTime.now().toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
        model.addAttribute("trendData", analyseService.getPurchaseTrend(thisMonthStart, thisMonthEnd, "month"));

        return "analyse";
    }

    // 动态筛选接口（用于前端异步更新）
    @GetMapping("/api/purchase/statistics")
    @ResponseBody
    public Map<String, Object> getStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "month") String timeUnit,
            // 新增 format 参数，由前端传递
            @RequestParam(defaultValue = "%Y-%m") String format
    ) {
        // 关键指标
        Map<String, Object> result = new HashMap<>();
        result.put("totalOrderCount", analyseService.getTotalOrderCount());
        result.put("totalPurchaseAmount", analyseService.getTotalPurchaseAmountYuan());
        result.put("averageOrderAmount", analyseService.getAverageOrderAmountYuan());
        result.put("completedOrderRatio", analyseService.getCompletedOrderRatio());

        // 趋势图数据
        if (start == null || end == null) {
            // 无时间参数时，默认取本月
            LocalDateTime now = LocalDateTime.now();
            start = now.withDayOfMonth(1).toLocalDate().atTime(0, 0, 0, 0);
            end = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).toLocalDate().atTime(23, 59, 59, 999_999_999);
        }

        // 根据 timeUnit 动态调整 format（可选，也可完全由前端控制）
        if ("week".equals(timeUnit)) {
            format = "%Y-%u"; // 按周分组（MySQL 周格式）
        } else if ("month".equals(timeUnit)) {
            format = "%Y-%m"; // 按月分组
        } else if ("year".equals(timeUnit)) {
            format = "%Y"; // 按年分组
        } else if ("custom".equals(timeUnit)) {
            format = "%Y-%m-%d"; // 自定义时间默认按天
        }

        // 传递 format 到 Service
        result.put("trendData", analyseService.getPurchaseTrend(start, end, format));

        return result;
    }
}
