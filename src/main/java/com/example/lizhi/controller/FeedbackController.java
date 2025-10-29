// FeedbackController.java
package com.example.lizhi.controller;

import com.example.lizhi.entity.Feedback;
import com.example.lizhi.entity.User;
import com.example.lizhi.service.FeedbackService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    // 提交反馈接口
    @PostMapping("/feedback/submit")
    @ResponseBody
    public ResponseEntity<?> submitFeedback(@RequestBody Feedback feedback) {
        try {
            Feedback saved = feedbackService.submitFeedback(feedback);
            return ResponseEntity.ok(Map.of("success", true, "data", saved));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 反馈管理页面
    @GetMapping("/feedback/manage")
    public String feedbackManagePage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            Model model,
            HttpSession session) {

        Page<Feedback> feedbackPage;

        if (status != null && keyword != null && !keyword.trim().isEmpty()) {
            feedbackPage = feedbackService.searchFeedbacksByStatus(status, keyword.trim(), page, size);
        } else if (status != null) {
            feedbackPage = feedbackService.getFeedbacksByStatus(status, page, size);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            feedbackPage = feedbackService.searchFeedbacks(keyword.trim(), page, size);
        } else {
            feedbackPage = feedbackService.getAllFeedbacks(page, size);
        }

        // 获取统计信息
        Map<String, Long> stats = feedbackService.getFeedbackStats();

        model.addAttribute("feedbacks", feedbackPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", feedbackPage.getTotalPages());
        model.addAttribute("totalItems", feedbackPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentKeyword", keyword);
        model.addAttribute("stats", stats);

        return "feedback-manage";
    }

    // 获取反馈详情
    @GetMapping("/feedback/detail/{id}")
    @ResponseBody
    public ResponseEntity<?> getFeedbackDetail(@PathVariable Long id) {
        try {
            Feedback feedback = feedbackService.getFeedbackById(id);
            if (feedback != null) {
                return ResponseEntity.ok(feedback);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 更新反馈状态
    @PutMapping("/feedback/update-status")
    @ResponseBody
    public ResponseEntity<?> updateFeedbackStatus(
            @RequestBody Map<String, Object> request,
            HttpSession session) {

        try {
            Long id = Long.valueOf(request.get("id").toString());
            Integer status = Integer.valueOf(request.get("status").toString());
            String processRemark = (String) request.get("processRemark");

            User currentUser = (User) session.getAttribute("currentUser");
            Long processorId = currentUser != null ? currentUser.getId() : null;
            String processorName = currentUser != null ? currentUser.getUsername() : "系统";

            Feedback updated = feedbackService.updateFeedbackStatus(id, status, processRemark, processorId, processorName);

            return ResponseEntity.ok(Map.of("success", true, "data", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 删除反馈
    @DeleteMapping("/feedback/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteFeedback(@PathVariable Long id) {
        try {
            feedbackService.deleteFeedback(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}