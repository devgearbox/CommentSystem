// FeedbackServiceImpl.java
package com.example.lizhi.service.Impl;

import com.example.lizhi.entity.Feedback;
import com.example.lizhi.entity.User;
import com.example.lizhi.repository.FeedbackRepository;
import com.example.lizhi.service.FeedbackService;
import com.example.lizhi.service.MessageService;
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

    @Autowired
    private MessageService messageService;

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

        Integer oldStatus = feedback.getStatus();

        feedback.setStatus(status);
        feedback.setProcessRemark(processRemark);
        feedback.setProcessorId(processorId);
        feedback.setProcessorName(processorName);

        if (status == 2) { // 已处理状态
            feedback.setProcessTime(LocalDateTime.now());

            if (oldStatus != 2 && feedback.getSubmitterId() != null) {
                sendFeedbackProcessedMessage(feedback);
            }
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

    /**
     * 发送反馈已处理消息
     */
    private void sendFeedbackProcessedMessage(Feedback feedback) {
        try {
            String messageContent = String.format(
                    "您的反馈已被处理。反馈内容：%s。处理备注：%s",
                    feedback.getId(),
                    // 截取内容前50个字符，避免消息过长
                    feedback.getContent().length() > 50 ?
                            feedback.getContent().substring(0, 50) + "..." : feedback.getContent(),
                    feedback.getProcessRemark() != null ? feedback.getProcessRemark() : "无"
            );

            // 创建消息对象
            com.example.lizhi.entity.Message message = new com.example.lizhi.entity.Message();
            message.setTitle("反馈处理完成通知");
            message.setContent(messageContent);
            message.setType(com.example.lizhi.entity.Message.MessageType.SYSTEM_NOTICE);
            message.setRecipientId(feedback.getSubmitterId());
            message.setSenderId(0L); // 系统发送

            // 保存消息
            messageService.sendMessage(message);

        } catch (Exception e) {
            // 记录错误但不影响主要业务流程
            System.err.println("发送反馈处理消息失败: " + e.getMessage());
        }
    }
}