// MessageController.java
package com.example.lizhi.controller;

import com.example.lizhi.entity.Message;
import com.example.lizhi.service.MessageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/messages")
    public String getMessagesPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            HttpSession session) {

        // 检查用户权限，只有采购员和管理员可以访问
        com.example.lizhi.entity.User currentUser = (com.example.lizhi.entity.User) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() == 3) {
            return "redirect:/work"; // 供应商角色重定向到首页
        }

        Page<Message> messagePage = messageService.getMessagesByUserId(currentUser.getId(), page, size);
        long unreadCount = messageService.getUnreadCount(currentUser.getId());

        model.addAttribute("messages", messagePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", messagePage.getTotalPages());
        model.addAttribute("totalItems", messagePage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("unreadCount", unreadCount);

        return "messages";
    }

    @GetMapping("/messages/unread/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUnreadCount(HttpSession session) {
        com.example.lizhi.entity.User currentUser = (com.example.lizhi.entity.User) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() == 3) {
            return ResponseEntity.ok(Map.of("count", 0));
        }

        long unreadCount = messageService.getUnreadCount(currentUser.getId());
        return ResponseEntity.ok(Map.of("count", unreadCount));
    }

    @PostMapping("/messages/mark-read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAsRead(
            @RequestBody Map<String, List<Integer>> request,
            HttpSession session) {

        com.example.lizhi.entity.User currentUser = (com.example.lizhi.entity.User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "用户未登录"));
        }

        List<Integer> messageIds = request.get("ids");
        try {
            messageService.markAsRead(messageIds, currentUser.getId());
            return ResponseEntity.ok(Map.of("success", true, "message", "标记为已读成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "标记失败：" + e.getMessage()));
        }
    }

    @PostMapping("/messages/mark-all-read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAllAsRead(HttpSession session) {
        com.example.lizhi.entity.User currentUser = (com.example.lizhi.entity.User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "用户未登录"));
        }

        try {
            messageService.markAllAsRead(currentUser.getId());
            return ResponseEntity.ok(Map.of("success", true, "message", "全部标记为已读成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "标记失败：" + e.getMessage()));
        }
    }

    @DeleteMapping("/messages/delete/batch")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteMessages(
            @RequestBody Map<String, List<Integer>> request,
            HttpSession session) {

        com.example.lizhi.entity.User currentUser = (com.example.lizhi.entity.User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "用户未登录"));
        }

        List<Integer> messageIds = request.get("ids");
        try {
            messageService.deleteMessages(messageIds, currentUser.getId());
            return ResponseEntity.ok(Map.of("success", true, "message", "删除消息成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "删除失败：" + e.getMessage()));
        }
    }
}