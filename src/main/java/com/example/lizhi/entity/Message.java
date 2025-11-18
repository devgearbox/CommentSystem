// Message.java
package com.example.lizhi.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "message")
@Data
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;           // 消息标题

    @Column(columnDefinition = "TEXT")
    private String content;         // 消息内容

    @Enumerated(EnumType.STRING)
    private MessageType type;       // 消息类型

    @Enumerated(EnumType.STRING)
    private MessageStatus status = MessageStatus.UNREAD; // 消息状态

    @Column(name = "order_no")
    private String orderNo;         // 关联的订单编号

    @Column(name = "recipient_id")
    private Long recipientId;       // 接收者ID（采购员ID）

    @Column(name = "sender_id")
    private Long senderId;          // 发送者ID（系统或供应商）

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "read_time")
    private LocalDateTime readTime;

    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
    }

    // 消息类型枚举
    public enum MessageType {
        ORDER_SHIPPED("订单发货提醒"),
        SYSTEM_NOTICE("系统通知"),
        WARNING("预警提醒");

        private final String label;

        MessageType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    // 消息状态枚举
    public enum MessageStatus {
        UNREAD("未读"),
        READ("已读");

        private final String label;

        MessageStatus(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}