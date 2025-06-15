package com.example.datn.controllers.product_and_other;

import com.example.datn.entities.product_and_other.Product;
import com.example.datn.services.product_and_other.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping("/hien-thi")
    public List<Product> getAll(){
        return productService.getAll();
    }


    @GetMapping("/detail/{id}")
    public Product detailProduct(@PathVariable("id") Integer id){
        return productService.detail(id);
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

