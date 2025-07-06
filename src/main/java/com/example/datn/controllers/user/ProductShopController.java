package com.example.datn.controllers.user;

import com.example.datn.entities.product_and_other.Product;
import com.example.datn.services.product_and_other.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
public class ProductShopController {
    @Autowired
    private ProductService productService;
    @Autowired
    private BrandService brandService;

    @Autowired
    private MaterialService materialService;

    @Autowired
    private ColorService colorService;

    @Autowired
    private SizeService sizeService;
    @GetMapping("/product")
    public String getProduct(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "5") int size,
                             Model model){
        Page<Product> listProduct = productService.getAll(PageRequest.of(page, size));
        Map<Integer, Integer> totalQuantity = new HashMap<>();

        for(Product p : listProduct){
            int total = productService.tongSoLuongSPCT(p.getId());
            totalQuantity.put(p.getId(),total);
        }
        model.addAttribute("totalQuantity", totalQuantity);
        model.addAttribute("listProduct", listProduct);
        model.addAttribute("currentPage", page);
        return "user/productShop";
    }
    @GetMapping("/product-detail/{code}")
    public String prodcutDetail(@PathVariable("code") String code, Model model) {
        Product product = productService.findByCode(code);
        if (product == null) {
            return "redirect:/product";
        }

        model.addAttribute("product", product);
        model.addAttribute("productDetails", productService.findAllProductDetailByIdProduct(product.getId()));
        model.addAttribute("totalQuantity", productService.tongSoLuongSPCT(product.getId()));

        // Truyền các dữ liệu phụ thuộc từ DB
        model.addAttribute("brands", brandService.getAll());
        model.addAttribute("materials", materialService.getAll());
        model.addAttribute("colors", productService.findColorsByProductId(product.getId()));
        model.addAttribute("sizes", productService.findSizesByProductId(product.getId()));
        return "user/productDetail";
    }
}
