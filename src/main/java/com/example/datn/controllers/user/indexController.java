package com.example.datn.controllers.user;

import com.example.datn.entities.product_and_other.Product;
import com.example.datn.services.product_and_other.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("")
public class indexController {

    @GetMapping
    public String hienThiTrangChu() {
        return "user/index";
    }

}
