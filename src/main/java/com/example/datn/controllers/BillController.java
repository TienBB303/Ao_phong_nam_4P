package com.example.datn.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/bill")
public class BillController {
    @GetMapping("/view")
    public String billView(){
        return "admin/bill";
    }
}
