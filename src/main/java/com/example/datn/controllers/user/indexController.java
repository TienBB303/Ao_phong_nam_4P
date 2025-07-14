package com.example.datn.controllers.user;

import com.example.datn.entities.product_and_other.Product;
import com.example.datn.services.product_and_other.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("")
public class indexController {
@Autowired
ProductService productService;
    @GetMapping
    public String getProduct(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             Model model){
        Page<Product> listProduct = productService.getAll(PageRequest.of(page, size));
        Map<Integer, Integer> totalQuantity = new HashMap<>();
        Map<Integer, String> priceRanges = productService.getMinMaxPriceByProduct();


        for(Product p : listProduct){
            int total = productService.tongSoLuongSPCT(p.getId());
            totalQuantity.put(p.getId(),total);
        }
        model.addAttribute("totalQuantity", totalQuantity);
        model.addAttribute("priceRanges", priceRanges);
        model.addAttribute("listProduct", listProduct);
        model.addAttribute("currentPage", page);
        return "user/index";
    }

}
