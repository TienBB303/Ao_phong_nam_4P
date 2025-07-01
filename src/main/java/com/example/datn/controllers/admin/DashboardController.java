package com.example.datn.controllers.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping({"/admin", "/admin/dashboard"})
public class DashboardController {

    @GetMapping
    public String dashboardGUI() {

        return "admin/admin-layout";
    }
}
