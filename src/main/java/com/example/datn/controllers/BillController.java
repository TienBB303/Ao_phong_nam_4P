package com.example.datn.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
<<<<<<< HEAD:src/main/java/com/example/datn/controllers/indexController.java
//@RequestMapping
public class indexController {
    @GetMapping
    public String hienThiTrangChu() {
        return "admin/index";
=======
@RequestMapping("/admin/bill")
public class BillController {
    @GetMapping("/view")
    public String billView(){
        return "admin/bill";
>>>>>>> 96abaefb9b462e2fe9335df8722f6dea85121bdc:src/main/java/com/example/datn/controllers/BillController.java
    }
}
