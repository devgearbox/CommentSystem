package com.example.lizhi.controller;

import com.example.lizhi.entity.User;
import com.example.lizhi.repository.AnalyseRepository;
import com.example.lizhi.service.AnalyseService;
import jakarta.servlet.http.HttpSession;
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

    /**
     * 页面渲染接口 - 展示分析页面
     */
    @GetMapping("/analyse")
    public String showAnalysePage(Model model, HttpSession session) {
        // 获取当前登录用户
        User currentUser = (User) session.getAttribute("currentUser");
        // 计算本月时间范围
        LocalDateTime thisMonthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime thisMonthEnd = LocalDateTime.now()
                .withDayOfMonth(LocalDateTime.now().toLocalDate().lengthOfMonth())
                .withHour(23).withMinute(59).withSecond(59);

        // 根据用户角色加载不同数据
        if (currentUser != null) {
            // 普通采购员角色(2)
            if (currentUser.getRole() == 2) {
                loadUserData(model, currentUser, thisMonthStart, thisMonthEnd);
            }
            // 管理员角色
            else {
                loadAdminData(model, thisMonthStart, thisMonthEnd);
            }
        }
        // 未登录状态，可根据需求调整，这里默认加载管理员视图或空数据
        else {
            loadAdminData(model, thisMonthStart, thisMonthEnd);
        }

        // 加载通用的分布图表数据
        model.addAttribute("varietyDistribution", analyseService.getPurchaseDistributionByVariety());
        model.addAttribute("supplierRatio", analyseService.getSupplierPurchaseRatio());

        return "analyse";
    }

    /**
     * API接口 - 提供采购统计数据，支持动态切换时间维度
     */
    @GetMapping("/api/purchase/statistics")
    @ResponseBody
    public Map<String, Object> getStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "month") String timeUnit,
            HttpSession session
    ) {
        Map<String, Object> result = new HashMap<>();
        User currentUser = (User) session.getAttribute("currentUser");
        boolean isUser = currentUser != null && currentUser.getRole() == 2;

        // 设置默认时间范围（本月）
        LocalDateTime[] defaultDates = getDefaultDateRange();
        if (start == null || end == null) {
            start = defaultDates[0];
            end = defaultDates[1];
        }

        // 根据用户角色加载不同统计数据
        if (isUser) {
            loadUserStatistics(result, currentUser.getId(), start, end, timeUnit);
        } else {
            loadAdminStatistics(result, start, end, timeUnit);
        }

        return result;
    }

    /**
     * 加载普通用户数据到模型
     */
    private void loadUserData(Model model, User user, LocalDateTime start, LocalDateTime end) {
        Long userId = user.getId();

        // 关键指标数据
        model.addAttribute("totalPurchaseAmountYuan", analyseService.getTotalPurchaseAmountYuanByUser(userId));
        model.addAttribute("averageOrderAmountYuan", analyseService.getAverageOrderAmountYuanByUser(userId));
        model.addAttribute("totalOrderCount", analyseService.getTotalOrderCountByUser(userId));
        model.addAttribute("completedOrderRatio", analyseService.getCompletedOrderRatioByUser(userId));

        // 趋势图数据
        model.addAttribute("trendData", analyseService.getPurchaseTrendByUser(start, end, "month", userId));

        // 较上月增长率数据
        Map<String, Object> growthMap = analyseService.calculateMonthOnMonthGrowthByUser(start, end, userId);
        addGrowthDataToModel(model, growthMap);
    }

    /**
     * 加载管理员数据到模型
     */
    private void loadAdminData(Model model, LocalDateTime start, LocalDateTime end) {
        // 关键指标数据
        model.addAttribute("totalPurchaseAmountYuan", analyseService.getTotalPurchaseAmountYuan());
        model.addAttribute("averageOrderAmountYuan", analyseService.getAverageOrderAmountYuan());
        model.addAttribute("totalOrderCount", analyseService.getTotalOrderCount());
        model.addAttribute("completedOrderRatio", analyseService.getCompletedOrderRatio());

        // 趋势图数据
        model.addAttribute("trendData", analyseService.getPurchaseTrend(start, end, "month"));

        // 较上月增长率数据
        Map<String, Object> growthMap = analyseService.calculateMonthOnMonthGrowth(start, end);
        addGrowthDataToModel(model, growthMap);
    }

    /**
     * 将增长率数据添加到模型
     */
    private void addGrowthDataToModel(Model model, Map<String, Object> growthMap) {
        model.addAttribute("orderGrowthHtml", growthMap.get("orderGrowthHtml"));
        model.addAttribute("amountGrowthHtml", growthMap.get("amountGrowthHtml"));
        model.addAttribute("avgAmountGrowthHtml", growthMap.get("avgAmountGrowthHtml"));
        model.addAttribute("ratioGrowthHtml", growthMap.get("ratioGrowthHtml"));
    }

    /**
     * 获取默认的日期范围（本月）
     */
    private LocalDateTime[] getDefaultDateRange() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = now.withDayOfMonth(now.toLocalDate().lengthOfMonth())
                .withHour(23).withMinute(59).withSecond(59).withNano(999_999_999);
        return new LocalDateTime[]{start, end};
    }

    /**
     * 加载普通用户的统计数据到结果集
     */
    private void loadUserStatistics(Map<String, Object> result, Long userId,
                                    LocalDateTime start, LocalDateTime end, String timeUnit) {
        // 关键指标数据
        result.put("totalOrderCount", analyseService.getTotalOrderCountByUser(userId));
        result.put("totalPurchaseAmount", analyseService.getTotalPurchaseAmountYuanByUser(userId));
        result.put("averageOrderAmount", analyseService.getAverageOrderAmountYuanByUser(userId));
        result.put("completedOrderRatio", analyseService.getCompletedOrderRatioByUser(userId));

        // 趋势图数据
        result.put("trendData", analyseService.getPurchaseTrendByUser(start, end, timeUnit, userId));

        // 较上月增长率数据
        Map<String, Object> growthMap = analyseService.calculateMonthOnMonthGrowthByUser(start, end, userId);
        addGrowthDataToResult(result, growthMap);
    }

    /**
     * 加载管理员的统计数据到结果集
     */
    private void loadAdminStatistics(Map<String, Object> result,
                                     LocalDateTime start, LocalDateTime end, String timeUnit) {
        // 关键指标数据
        result.put("totalOrderCount", analyseRepository.countOrdersByTimeRange(start, end));

        BigDecimal totalAmount = analyseRepository.sumTotalPriceByTimeRange(start, end);
        result.put("totalPurchaseAmount", totalAmount == null ? BigDecimal.ZERO : totalAmount);

        Long totalOrder = analyseRepository.countOrdersByTimeRange(start, end);
        BigDecimal avgAmount = totalOrder == 0 ? BigDecimal.ZERO :
                totalAmount.divide(new BigDecimal(totalOrder), 2, RoundingMode.HALF_UP);
        result.put("averageOrderAmount", avgAmount);

        Long receivedOrder = analyseRepository.countReceivedOrdersByTimeRange(start, end);
        BigDecimal ratio = totalOrder == 0 ? BigDecimal.ZERO :
                new BigDecimal(receivedOrder).divide(new BigDecimal(totalOrder), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100)).setScale(1, RoundingMode.HALF_UP);
        result.put("completedOrderRatio", ratio + "%");

        // 趋势图数据
        result.put("trendData", analyseService.getPurchaseTrend(start, end, timeUnit));

        // 较上月增长率数据
        Map<String, Object> growthMap = analyseService.calculateMonthOnMonthGrowth(start, end);
        addGrowthDataToResult(result, growthMap);
    }

    /**
     * 将增长率数据添加到结果集
     */
    private void addGrowthDataToResult(Map<String, Object> result, Map<String, Object> growthMap) {
        result.put("orderGrowthHtml", growthMap.get("orderGrowthHtml"));
        result.put("amountGrowthHtml", growthMap.get("amountGrowthHtml"));
        result.put("avgAmountGrowthHtml", growthMap.get("avgAmountGrowthHtml"));
        result.put("ratioGrowthHtml", growthMap.get("ratioGrowthHtml"));
    }
}
