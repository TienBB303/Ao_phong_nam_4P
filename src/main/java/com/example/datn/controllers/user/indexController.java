package com.example.datn.controllers.user;

import com.example.datn.entities.product_and_other.Product;
import com.example.datn.repositories.product_and_other.BrandRepository;
import com.example.datn.repositories.product_and_other.CategoryRepository;
import com.example.datn.repositories.product_and_other.MaterialRepository;
import com.example.datn.services.product_and_other.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("")
public class indexController {
    @Autowired
    ProductService productService;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    MaterialRepository materialRepository;

    @GetMapping("")
    public String getProduct(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) String name,
                             @RequestParam(required = false) Boolean status,
                             @RequestParam(required = false) Integer categoryId,
                             @RequestParam(required = false) Integer brandId,
                             @RequestParam(required = false) Integer materialId,
                             Model model, Principal principal) {

        Page<Product> listProduct;

        boolean hasFilter = name != null || status != null || categoryId != null || brandId != null || materialId != null;

        if (hasFilter) {
            if (name != null && name.trim().isEmpty()) {
                name = null;
            }
            listProduct = productService.searchAllFields(name, categoryId, brandId, materialId, PageRequest.of(page, size));
        } else {
            listProduct = productService.getAll(PageRequest.of(page, size)); // ← hiển thị tất cả sản phẩm
        }

        Map<Integer, Integer> totalQuantity = new HashMap<>();
        Map<Integer, String> priceRanges = productService.getMinMaxPriceByProduct();

        for (Product p : listProduct) {
            int total = productService.tongSoLuongSPCT(p.getId());
            totalQuantity.put(p.getId(), total);
        }

        model.addAttribute("totalQuantity", totalQuantity);
        model.addAttribute("priceRanges", priceRanges);
        model.addAttribute("listProduct", listProduct);
        model.addAttribute("currentPage", page);

// Gửi lại các giá trị lọc về giao diện để giữ trạng thái
        model.addAttribute("name", name);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("brandId", brandId);
        model.addAttribute("materialId", materialId);


        model.addAttribute("listBrand", brandRepository.findAllActive());
        model.addAttribute("listCategory", categoryRepository.findAllActive());
        model.addAttribute("listMaterial", materialRepository.findAllActive());

        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }

        return "user/index";

    }

}
