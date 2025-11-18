package com.example.lizhi.config;

import com.example.lizhi.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.*;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    // 不需要拦截的路径
    private static final Set<String> ALLOWED_PATHS = new HashSet<>() {{
        add("/login");
        add("/register");
        add("/css/");
        add("/js/");
        add("/images/");
        add("/error");
        add("/api/public/");
    }};

    // 各角色禁止访问的页面映射
    private static final Map<Integer, Set<String>> ROLE_FORBIDDEN_PATHS = new HashMap<>();

    static {
        // 管理员(role=1) - 没有禁止访问的页面
        ROLE_FORBIDDEN_PATHS.put(1, Collections.emptySet());

        // 采购人员(role=2) - 禁止访问反馈管理
        Set<String> purchaserForbidden = new HashSet<>();
        purchaserForbidden.add("/feedback/manage");
        ROLE_FORBIDDEN_PATHS.put(2, purchaserForbidden);

        // 供应商(role=3) - 禁止访问多个页面
        Set<String> supplierForbidden = new HashSet<>();
        supplierForbidden.add("/stock");
        supplierForbidden.add("/analyse");
        supplierForbidden.add("/feedback/manage");
        ROLE_FORBIDDEN_PATHS.put(3, supplierForbidden);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        // 检查是否为允许访问的路径（静态资源等）
        for (String allowedPath : ALLOWED_PATHS) {
            if (path.startsWith(allowedPath)) {
                return true;
            }
        }

        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("currentUser");

        // 未登录，重定向到登录页
        if (currentUser == null) {
            response.sendRedirect("/login");
            return false;
        }

        // 检查当前用户是否有权限访问该页面
        if (!checkPermission(path, currentUser.getRole())) {
            response.sendRedirect("/403");
            return false;
        }

        // 检查当前用户是否有权限访问该页面
        return checkPermission(path, currentUser.getRole());
    }

    /**
     * 基于"禁止访问"逻辑进行权限控制
     * 如果当前路径在用户角色的禁止列表中，则拒绝访问
     */
    private boolean checkPermission(String path, Integer role) {
        // 获取该角色禁止访问的页面集合
        Set<String> forbiddenPaths = ROLE_FORBIDDEN_PATHS.get(role);

        if (forbiddenPaths == null) {
            // 未知角色，默认禁止访问
            return false;
        }

        // 检查当前路径是否在禁止列表中
        for (String forbiddenPath : forbiddenPaths) {
            if (path.startsWith(forbiddenPath)) {
                // 路径在禁止列表中，拒绝访问
                return false;
            }
        }

        // 不在禁止列表中，允许访问
        return true;
    }
}