package com.example.lizhi.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.thymeleaf.exceptions.TemplateProcessingException;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
        // 不处理静态资源找不到的错误
        if (ex instanceof NoResourceFoundException) {
            return null;
        }

        // 处理 Thymeleaf 模板错误
        if (ex instanceof TemplateProcessingException) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "模板渲染错误: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "服务器内部错误: " + ex.getMessage());

        // 记录异常详细信息到日志
        ex.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, Object>> handleIOException(IOException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "文件操作错误: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(NoSuchFileException.class)
    public ResponseEntity<Map<String, Object>> handleNoSuchFileException(NoSuchFileException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "文件不存在: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "文件大小超过限制");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}