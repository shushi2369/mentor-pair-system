package com.mentorpair.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminPageController {

    @GetMapping
    public String index() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/window")
    public String window() {
        return "admin/window";
    }

    @GetMapping("/users")
    public String users() {
        return "admin/users";
    }

    @GetMapping("/projects")
    public String projects() {
        return "admin/projects";
    }

    @GetMapping("/submissions")
    public String submissions() {
        return "admin/submissions";
    }

    @GetMapping("/guidances")
    public String guidances() {
        return "admin/guidances";
    }
}
