// FeedbackServiceImpl.java
package com.example.lizhi.service.Impl;

import com.example.lizhi.entity.Feedback;
import com.example.lizhi.entity.User;
import com.example.lizhi.repository.FeedbackRepository;
import com.example.lizhi.service.FeedbackService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Override
    public Feedback submitFeedback(Feedback feedback) {
        // 设置提交者信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpSession session = attributes.getRequest().getSession();
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser != null) {
            feedback.setSubmitterId(currentUser.getId());
            feedback.setSubmitterName(currentUser.getUsername());
        }

        feedback.setStatus(0); // 默认未处理状态
        return feedbackRepository.save(feedback);
    }

    @Override
    public Page<Feedback> getAllFeedbacks(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return feedbackRepository.findAll(pageable);
    }

    @Override
    public Page<Feedback> getFeedbacksByStatus(Integer status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return feedbackRepository.findByStatus(status, pageable);
    }

    @Override
    public Page<Feedback> searchFeedbacks(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return feedbackRepository.searchByKeyword(keyword, pageable);
    }

    @Override
    public Page<Feedback> searchFeedbacksByStatus(Integer status, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return feedbackRepository.searchByStatusAndKeyword(status, keyword, pageable);
    }

    @Override
    public Feedback getFeedbackById(Long id) {
        return feedbackRepository.findById(id).orElse(null);
    }

    @Override
    public Feedback updateFeedbackStatus(Long id, Integer status, String processRemark,
                                         Long processorId, String processorName) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("反馈不存在"));

        feedback.setStatus(status);
        feedback.setProcessRemark(processRemark);
        feedback.setProcessorId(processorId);
        feedback.setProcessorName(processorName);

        if (status == 2) { // 已处理状态
            feedback.setProcessTime(LocalDateTime.now());
        }

        return feedbackRepository.save(feedback);
    }

    @Override
    public void deleteFeedback(Long id) {
        feedbackRepository.deleteById(id);
    }

    @Override
    public Map<String, Long> getFeedbackStats() {
        Map<String, Long> stats = new HashMap<>();

        // 修复参数数量问题 - 使用正确的方法
        stats.put("total", feedbackRepository.count());

        // 统计各状态数量
        List<Object[]> statusCounts = feedbackRepository.countByStatus();
        stats.put("pending", 0L);
        stats.put("read", 0L);
        stats.put("processed", 0L);

        for (Object[] statusCount : statusCounts) {
            Integer status = (Integer) statusCount[0];
            Long count = (Long) statusCount[1];

            switch (status) {
                case 0:
                    stats.put("pending", count);
                    break;
                case 1:
                    stats.put("read", count);
                    break;
                case 2:
                    stats.put("processed", count);
                    break;
            }
        }

        return stats;
    }
}