package com.example.datn.controllers.selling;

import com.example.datn.entities.product_and_other.Product;
import com.example.datn.entities.product_and_other.ProductDetail;
import com.example.datn.services.product_and_other.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/sell-inline")
public class SellingInlineController {
    @Autowired
    ProductService productService;

    @ModelAttribute("listProduct")
    public List<Product> listProduct() {
        return productService.getAll();
    }

    @ModelAttribute("listProductDetail")
    public List<ProductDetail> listProductDetail() {
        return productService.getAllProductDetails();
    }

    @GetMapping("/hien-thi")
    public String sellInlineView(){
        return "admin/selling/inline";
    }
}
