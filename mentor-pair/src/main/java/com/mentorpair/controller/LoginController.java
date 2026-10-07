package com.mentorpair.controller;

import com.mentorpair.common.BusinessException;
import com.mentorpair.common.Result;
import com.mentorpair.dto.LoginUser;
import com.mentorpair.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    public static String homeOf(LoginUser u) {
        if (u.isAdmin()) {
            return "/admin/dashboard";
        }
        if (u.isMentor()) {
            return "/mentor/projects";
        }
        return "/student/mentors";
    }

    /** 自助修改密码（三端通用） */
    @PostMapping("/api/account/password")
    @ResponseBody
    public Result<Void> changePassword(@RequestParam String oldPassword,
                                       @RequestParam String newPassword,
                                       HttpSession session) {
        LoginUser u = (LoginUser) session.getAttribute(LoginUser.SESSION_KEY);
        userService.changePassword(u.getId(), oldPassword, newPassword);
        return Result.ok();
    }

    public static String roleName(Integer role) {
        if (role != null && role == LoginUser.ROLE_ADMIN) {
            return "管理员端";
        }
        if (role != null && role == LoginUser.ROLE_MENTOR) {
            return "导师端";
        }
        return "学员端";
    }

    @GetMapping("/")
    public String root(HttpSession session) {
        LoginUser u = (LoginUser) session.getAttribute(LoginUser.SESSION_KEY);
        return "redirect:" + (u == null ? "/login" : homeOf(u));
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        LoginUser u = (LoginUser) session.getAttribute(LoginUser.SESSION_KEY);
        if (u != null) {
            return "redirect:" + homeOf(u);
        }
        return "login";
    }

    /** 登录：登录页需先选择登录端，账号角色须与所选端一致；登录成功重建会话（防会话固定） */
    @PostMapping("/login")
    public String login(@RequestParam(defaultValue = "3") Integer role,
                        @RequestParam String username,
                        @RequestParam String password,
                        HttpServletRequest request, Model model) {
        model.addAttribute("selectedRole", role);
        try {
            LoginUser u = userService.login(username, password);
            if (!role.equals(u.getRole())) {
                model.addAttribute("error", "该账号属于「" + roleName(u.getRole()) + "」，请在上方切换到对应登录端后再登录");
                model.addAttribute("username", username);
                return "login";
            }
            HttpSession old = request.getSession(false);
            if (old != null) {
                old.invalidate();
            }
            HttpSession session = request.getSession(true);
            session.setAttribute(LoginUser.SESSION_KEY, u);
            return "redirect:" + homeOf(u);
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("username", username);
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    /** 退出并关闭系统：延迟执行优雅停机，响应先返回给页面 */
    @PostMapping("/shutdown")
    @ResponseBody
    public Result<Void> shutdown(HttpSession session) {
        LoginUser u = (LoginUser) session.getAttribute(LoginUser.SESSION_KEY);
        log.info("用户 {} 请求退出系统，服务即将关闭", u == null ? "unknown" : u.getUsername());
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(800);
            } catch (InterruptedException ignored) {
            }
            System.exit(0);
        }, "shutdown-trigger");
        t.setDaemon(false);
        t.start();
        return Result.ok();
    }

    @GetMapping("/403")
    public String forbidden() {
        return "error/403";
    }
}
