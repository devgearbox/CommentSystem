// MessageServiceImpl.java
package com.example.lizhi.service.Impl;

import com.example.lizhi.entity.Message;
import com.example.lizhi.repository.MessageRepository;
import com.example.lizhi.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;

    @Override
    @Transactional
    public void sendMessage(Message message) {
        messageRepository.save(message);
    }

    @Override
    @Transactional
    public void sendOrderShippedMessage(String orderNo, Long purchaserId, String supplierName) {
        Message message = new Message();
        message.setTitle("订单发货提醒");
        message.setContent(String.format("您的订单 %s 已由供应商 %s 发货，请注意查收。", orderNo, supplierName));
        message.setType(Message.MessageType.ORDER_SHIPPED);
        message.setOrderNo(orderNo);
        message.setRecipientId(purchaserId);
        message.setSenderId(0L); // 0表示系统发送

        messageRepository.save(message);
    }

    @Override
    public Page<Message> getMessagesByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return messageRepository.findByRecipientIdOrderByCreateTimeDesc(userId, pageable);
    }

    @Override
    public Page<Message> getUnreadMessagesByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return messageRepository.findByRecipientIdAndStatusOrderByCreateTimeDesc(userId, Message.MessageStatus.UNREAD, pageable);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return messageRepository.countByRecipientIdAndStatus(userId, Message.MessageStatus.UNREAD);
    }

    @Override
    @Transactional
    public void markAsRead(List<Integer> messageIds, Long userId) {
        messageRepository.markAsRead(messageIds, userId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        messageRepository.markAllAsRead(userId);
    }

    @Override
    @Transactional
    public void deleteMessages(List<Integer> messageIds, Long userId) {
        // 添加权限校验，确保用户只能删除自己的消息
        List<Message> messages = messageRepository.findAllById(messageIds);

        // 过滤出属于当前用户的消息
        List<Integer> userMessageIds = messages.stream()
                .filter(message -> message.getRecipientId().equals(userId))
                .map(Message::getId)
                .collect(Collectors.toList());

        if (!userMessageIds.isEmpty()) {
            messageRepository.deleteAllById(userMessageIds);
        }
    }

    @Override
    @Transactional
    public void sendFreshnessUrgentMessage(String orderNo, Long recipientId, String content) {
        Message message = new Message();
        message.setTitle("库存保鲜紧急提醒");
        message.setContent(content);
        message.setType(Message.MessageType.WARNING);
        message.setOrderNo(orderNo);
        message.setRecipientId(recipientId);
        message.setSenderId(0L); // 0表示系统发送

        messageRepository.save(message);
    }
}