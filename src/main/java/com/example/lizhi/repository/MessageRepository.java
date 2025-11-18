// MessageRepository.java
package com.example.lizhi.repository;

import com.example.lizhi.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {

    // 根据接收者ID查找消息
    Page<Message> findByRecipientIdOrderByCreateTimeDesc(Long recipientId, Pageable pageable);

    // 根据接收者ID和状态查找消息
    Page<Message> findByRecipientIdAndStatusOrderByCreateTimeDesc(Long recipientId, Message.MessageStatus status, Pageable pageable);

    // 统计未读消息数量
    long countByRecipientIdAndStatus(Long recipientId, Message.MessageStatus status);

    // 批量标记为已读
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.status = 'READ', m.readTime = CURRENT_TIMESTAMP WHERE m.id IN :ids AND m.recipientId = :recipientId")
    int markAsRead(@Param("ids") List<Integer> ids, @Param("recipientId") Long recipientId);

    // 标记所有消息为已读
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.status = 'READ', m.readTime = CURRENT_TIMESTAMP WHERE m.recipientId = :recipientId AND m.status = 'UNREAD'")
    int markAllAsRead(@Param("recipientId") Long recipientId);
}