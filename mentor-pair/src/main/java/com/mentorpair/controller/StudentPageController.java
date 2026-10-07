package com.mentorpair.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/student")
public class StudentPageController {

    @GetMapping
    public String index() {
        return "redirect:/student/mentors";
    }

    @GetMapping("/mentors")
    public String mentors() {
        return "student/mentors";
    }

    @GetMapping("/projects")
    public String projects() {
        return "student/projects";
    }

    @GetMapping("/selections")
    public String selections() {
        return "student/selections";
    }

    @GetMapping("/submissions")
    public String submissions() {
        return "student/submissions";
    }

    @GetMapping("/guidances")
    public String guidances() {
        return "student/guidances";
    }
}
