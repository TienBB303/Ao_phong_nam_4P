
package com.example.datn.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("admin/bill")
public class BillController {

    @GetMapping
    public String index() {

        return "admin/bill";
    }
}

