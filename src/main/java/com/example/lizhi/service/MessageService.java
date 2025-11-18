// MessageService.java
package com.example.lizhi.service;

import com.example.lizhi.entity.Message;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MessageService {

    // 发送消息
    void sendMessage(Message message);

    // 发送订单发货提醒
    void sendOrderShippedMessage(String orderNo, Long purchaserId, String supplierName);

    // 获取用户消息列表
    Page<Message> getMessagesByUserId(Long userId, int page, int size);

    // 获取用户未读消息
    Page<Message> getUnreadMessagesByUserId(Long userId, int page, int size);

    // 获取未读消息数量
    long getUnreadCount(Long userId);

    // 标记消息为已读
    void markAsRead(List<Integer> messageIds, Long userId);

    // 标记所有消息为已读
    void markAllAsRead(Long userId);

    // 删除消息
    void deleteMessages(List<Integer> messageIds, Long userId);

    void sendFreshnessUrgentMessage(String orderNo, Long recipientId, String content);
}