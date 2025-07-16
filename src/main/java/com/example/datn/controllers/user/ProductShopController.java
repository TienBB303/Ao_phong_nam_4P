package com.example.datn.controllers.user;

import com.example.datn.entities.product_and_other.Product;
import com.example.datn.entities.product_and_other.ProductDetail;
import com.example.datn.repositories.product_and_other.BrandRepository;
import com.example.datn.repositories.product_and_other.CategoryRepository;
import com.example.datn.repositories.product_and_other.MaterialRepository;
import com.example.datn.services.product_and_other.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    BrandRepository brandRepository;
    @Autowired
    MaterialRepository materialRepository;
    @GetMapping("/product")
    public String getProduct(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) String name,
                             @RequestParam(required = false) Boolean status,
                             @RequestParam(required = false) Integer categoryId,
                             @RequestParam(required = false) Integer brandId,
                             @RequestParam(required = false) Integer materialId,
                             Model model) {

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
