package com.example.datn.controllers.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/about")
    public String about() {
        return "user/about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "user/contact";
    }

    @GetMapping("/blog")
    public String blog() {
        return "user/blog";
    }



} 