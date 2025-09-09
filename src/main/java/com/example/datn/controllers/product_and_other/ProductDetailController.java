package com.example.datn.controllers.product_and_other;

import com.example.datn.entities.product_and_other.Material;
import com.example.datn.entities.product_and_other.ProductDetail;
import com.example.datn.services.product_and_other.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/product-detail")
public class ProductDetailController {
    @Autowired
    ProductService productService;

    @GetMapping("/detail/{id}")
    @ResponseBody
    public ProductDetail viewDetail(@PathVariable("id") Integer id) {
        return productService.detailProductDetail(id);
    }

    @PostMapping("/update")
    public String updateProductDetail(
            @RequestParam("ProductDetailId") Integer id,
            @RequestParam("productPriceUpdate") String priceStr,
            @RequestParam("productQuantityUpdate") String quantityStr,
            RedirectAttributes redirectAttributes) {

        ProductDetail productDetail = productService.findProductDetailById(id);
        BigDecimal price;
        Integer quantity;

//        try {
//            price = new BigDecimal(priceStr);
//            if (price.compareTo(BigDecimal.ZERO) <= 0) {
//                throw new NumberFormatException("Giá nhỏ hơn hoặc bằng 0");
//            }
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("alert", "Giá không hợp lệ");
//            redirectAttributes.addFlashAttribute("type", "error");
//            return "redirect:/admin/product/view-detail/" + productDetail.getProduct().getId();
//        }

        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                throw new NumberFormatException("Số lượng <= 0");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("alert", "Số lượng không hợp lệ");
            redirectAttributes.addFlashAttribute("type", "error");
            return "redirect:/admin/product/view-detail/" + productDetail.getProduct().getId();
        }

//        productDetail.setPrice(price);
        productDetail.setQuantity(quantity);
        productService.updateProductDetail(productDetail);

        return "redirect:/admin/product/view-detail/" + productDetail.getProduct().getId();
    }

    @GetMapping("/change-status/{id}")
    public String changeStatus( @PathVariable("id") Integer id, RedirectAttributes redirectAttributes){
        ProductDetail productDetail = productService.changeStatus(id);

        redirectAttributes.addFlashAttribute("alert", "Thay đổi trạng thái thành công!");
        redirectAttributes.addFlashAttribute("type", "success");
        return "redirect:/admin/product/view-detail/" + productDetail.getProduct().getId();
    }



}
