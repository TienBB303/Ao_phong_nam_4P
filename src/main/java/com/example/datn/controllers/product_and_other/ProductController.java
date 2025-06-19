package com.example.datn.controllers.product_and_other;

import com.example.datn.entities.product_and_other.Product;
import com.example.datn.services.product_and_other.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/product")
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping("/hien-thi")
    public String sanPham(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "5") int size,
                          Model model) {
        Page<Product> listProduct = productService.getAll(PageRequest.of(page, size));

        model.addAttribute("listProduct", listProduct);
        model.addAttribute("currentPage", page);
        return "admin/product_and_other/product/ProductView";
    }

    @GetMapping("/detail/{id}")
    public Product detailProduct(@PathVariable("id") Integer id){
        return productService.detail(id);
    }

    @GetMapping("/view-add")
    public String viewAdd(){
        return "admin/product_and_other/product/ProductViewAdd";
    }
    @PostMapping("/add")
    public Product AddNewProduct(@RequestBody Product product){
        return productService.addProduct(product);
    }

    @PostMapping("/update/{id}")
    public Product updateProduct(@PathVariable("id") Integer id, @RequestBody Product product){
        return productService.update(id, product);
    }


//    demo Body -> Raw (JSON)
//    {
//        "code": "SP002",
//        "name": "Áo nike",
//        "status": true,
//        "description": "Cập nhật mô tả",
//        "category": { "id": 1 },
//        "brand": { "id": 2 },
//        "material": { "id": 1 }
//    }

}

