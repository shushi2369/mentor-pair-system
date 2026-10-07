package com.mentorpair.interceptor;

import com.mentorpair.dto.LoginUser;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 登录校验：未登录的 AJAX 返回 401 JSON，页面跳转登录页 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Object user = request.getSession().getAttribute(LoginUser.SESSION_KEY);
        if (user != null) {
            return true;
        }
        if (isAjax(request)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"请先登录\"}");
        } else {
            response.sendRedirect("/login");
        }
        return false;
    }

    static boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"))
                || request.getRequestURI().contains("/api/");
    }
}
