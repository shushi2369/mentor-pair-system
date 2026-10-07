package com.mentorpair.interceptor;

import com.mentorpair.dto.LoginUser;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 角色校验：/admin/** 仅管理员，/mentor/** 仅导师，/student/** 仅学员 */
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        LoginUser user = (LoginUser) request.getSession().getAttribute(LoginUser.SESSION_KEY);
        if (user == null) {
            return false; // 登录拦截器已处理
        }
        Integer required = null;
        String uri = request.getRequestURI();
        if (uri.startsWith("/admin/")) {
            required = LoginUser.ROLE_ADMIN;
        } else if (uri.startsWith("/mentor/")) {
            required = LoginUser.ROLE_MENTOR;
        } else if (uri.startsWith("/student/")) {
            required = LoginUser.ROLE_STUDENT;
        }
        if (required == null || required.equals(user.getRole())) {
            return true;
        }
        if (LoginInterceptor.isAjax(request)) {
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"msg\":\"没有权限执行该操作\"}");
        } else {
            response.sendRedirect("/403");
        }
        return false;
    }
}
