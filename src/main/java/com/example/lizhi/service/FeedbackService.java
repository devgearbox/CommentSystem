// FeedbackService.java
package com.example.lizhi.service;

import com.example.lizhi.entity.Feedback;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface FeedbackService {

    Feedback submitFeedback(Feedback feedback);

    Page<Feedback> getAllFeedbacks(int page, int size);

    Page<Feedback> getFeedbacksByStatus(Integer status, int page, int size);

    Page<Feedback> searchFeedbacks(String keyword, int page, int size);

    Page<Feedback> searchFeedbacksByStatus(Integer status, String keyword, int page, int size);

    Feedback getFeedbackById(Long id);

    Feedback updateFeedbackStatus(Long id, Integer status, String processRemark, Long processorId, String processorName);

    void deleteFeedback(Long id);

    Map<String, Long> getFeedbackStats();
}