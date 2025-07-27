package com.example.datn.controllers.api;

import com.example.datn.entities.product_and_other.ProductDetail;
import com.example.datn.repositories.product_and_other.ProductDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
@RestController
public class ProductCartRestController {

    @Autowired
    private ProductDetailRepository productDetailRepository;

    @GetMapping("/api/product-detail/id")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getProductDetailId(
            @RequestParam Integer productId,
            @RequestParam Integer colorId,
            @RequestParam Integer sizeId
    ) {
        ProductDetail pd = productDetailRepository.findByProductIdAndColorIdAndSizeId(productId, colorId, sizeId);
        if (pd != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("id", pd.getId());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "Not found"));
    }

}
