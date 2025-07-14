package com.example.datn.controllers.api;

import com.example.datn.entities.product_and_other.ProductDetail;
import com.example.datn.services.product_and_other.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/product-detail")
public class ApiProductController {
    @Autowired
    private ProductService productService;

    @GetMapping("/price")
    public ResponseEntity<?> getPriceAndQuantity(
            @RequestParam Integer productId,
            @RequestParam Integer colorId,
            @RequestParam Integer sizeId) {

        ProductDetail detail = productService.findProductDetailByColorAndSize(productId, colorId, sizeId);

        if (detail == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> data = new HashMap<>();
        data.put("price", detail.getPrice());
        data.put("quantity", detail.getQuantity());
        return ResponseEntity.ok(data);
    }
}
