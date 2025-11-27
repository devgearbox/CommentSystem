package com.example.lizhi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取项目根目录的绝对路径
        String uploadPath = Paths.get("").toAbsolutePath().toString() + "/uploads/img/";

        registry.addResourceHandler("/uploads/img/**")
                .addResourceLocations("file:" + uploadPath);

        // 添加日志输出，便于调试
        System.out.println("图片资源映射: " + uploadPath);
    }

//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(loginInterceptor)
//                .addPathPatterns("/**")
//                .excludePathPatterns(
//                        "/login",
//                        "/register",
//                        "/css/**",
//                        "/js/**",
//                        "/images/**",
//                        "/img/**",
//                        "/error",
//                        "/api/public/**",
//                        "/uploads/img/**",
//                        "/payment/**"
//                );
//    }
}