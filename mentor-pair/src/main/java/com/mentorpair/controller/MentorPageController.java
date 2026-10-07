package com.mentorpair.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mentor")
public class MentorPageController {

    @GetMapping
    public String index() {
        return "redirect:/mentor/profile";
    }

    @GetMapping("/profile")
    public String profile() {
        return "mentor/profile";
    }

    @GetMapping("/projects")
    public String projects() {
        return "mentor/projects";
    }

    @GetMapping("/selections")
    public String selections() {
        return "mentor/selections";
    }

    @GetMapping("/submissions")
    public String submissions() {
        return "mentor/submissions";
    }

    @GetMapping("/courses")
    public String courses() {
        return "mentor/courses";
    }
}
