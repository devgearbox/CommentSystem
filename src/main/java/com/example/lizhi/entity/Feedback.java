// Feedback.java
package com.example.lizhi.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "feedback_type", nullable = false)
    private String feedbackType; // bug, suggest, experience, other

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "contact_info")
    private String contactInfo;

    @Column(name = "attachment_paths")
    private String attachmentPaths; // 存储附件路径，多个用逗号分隔

    @Column(name = "status", nullable = false)
    private Integer status = 0; // 0-未处理, 1-已读, 2-已处理

    @Column(name = "submitter_id")
    private Long submitterId; // 提交者ID

    @Column(name = "submitter_name")
    private String submitterName; // 提交者姓名

    @Column(name = "processor_id")
    private Long processorId; // 处理者ID

    @Column(name = "processor_name")
    private String processorName; // 处理者姓名

    @Column(name = "process_remark")
    private String processRemark; // 处理备注

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "process_time")
    private LocalDateTime processTime; // 处理时间

    // 状态文本显示
    public String getStatusText() {
        switch (status) {
            case 0: return "未处理";
            case 1: return "已读";
            case 2: return "已处理";
            default: return "未知";
        }
    }

    // 反馈类型文本显示
    public String getTypeText() {
        switch (feedbackType) {
            case "bug": return "系统Bug";
            case "suggest": return "功能建议";
            case "experience": return "体验优化";
            case "other": return "其他问题";
            default: return "未知";
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFeedbackType() { return feedbackType; }
    public void setFeedbackType(String feedbackType) { this.feedbackType = feedbackType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public String getAttachmentPaths() { return attachmentPaths; }
    public void setAttachmentPaths(String attachmentPaths) { this.attachmentPaths = attachmentPaths; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Long getSubmitterId() { return submitterId; }
    public void setSubmitterId(Long submitterId) { this.submitterId = submitterId; }

    public String getSubmitterName() { return submitterName; }
    public void setSubmitterName(String submitterName) { this.submitterName = submitterName; }

    public Long getProcessorId() { return processorId; }
    public void setProcessorId(Long processorId) { this.processorId = processorId; }

    public String getProcessorName() { return processorName; }
    public void setProcessorName(String processorName) { this.processorName = processorName; }

    public String getProcessRemark() { return processRemark; }
    public void setProcessRemark(String processRemark) { this.processRemark = processRemark; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public LocalDateTime getProcessTime() { return processTime; }
    public void setProcessTime(LocalDateTime processTime) { this.processTime = processTime; }
}