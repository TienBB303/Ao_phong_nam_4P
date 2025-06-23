package com.example.datn.controllers.product_and_other;

import com.example.datn.entities.product_and_other.Category;
import com.example.datn.entities.product_and_other.Product;
import com.example.datn.services.product_and_other.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/category")
public class CategoryController {
    @Autowired
    CategoryService categoryService;

    @GetMapping("/hien-thi")
    public String category(
            @RequestParam(value = "categoryNameSearch", defaultValue = "") String name,
            @RequestParam(value = "categoryStatusSearch", defaultValue = "") Boolean status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {
        if (page < 0) {
            page = 0;
        }
        Page<Category> listCategory = categoryService.searchPage(name, status, PageRequest.of(page, size));
        model.addAttribute("categoryNameSearch", name);
        model.addAttribute("categoryStatusSearch", status != null ? status : "");
        model.addAttribute("listCategory", listCategory);
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        return "admin/product_and_other/other/CategoryView";
    }

    @PostMapping("/add")
    public String add(@RequestParam("categoryName") String categoryName,
                      @RequestParam("categoryDescription") String categoryDescription,
                      RedirectAttributes redirectAttributes){
        if (categoryName == null || categoryName.trim().isEmpty()){
            redirectAttributes.addFlashAttribute("alert", "Tên không được để trống");
            redirectAttributes.addFlashAttribute("type", "error");
            return "redirect:/admin/category/hien-thi";
        }
        Category checkTonTai = categoryService.findByName(categoryName);
        if(checkTonTai != null){
            redirectAttributes.addFlashAttribute("alert", "Đã tồn tại kiểu loại");
            redirectAttributes.addFlashAttribute("type", "error");
            return "redirect:/admin/category/hien-thi";
        } else {
            categoryService.addCategory(categoryName, categoryDescription);
            redirectAttributes.addFlashAttribute("alert", "Thêm kiểu loại thành công!");
            redirectAttributes.addFlashAttribute("type", "success");
            return "redirect:/admin/category/hien-thi";
        }
    }

    @GetMapping("/detail/{id}")
    @ResponseBody
    public Category detailCategory(@PathVariable("id") Integer id){
        return categoryService.detail(id);
    }

    @PostMapping("/update")
    public String updateCategory(
            @RequestParam("categoryId") Integer id,
            @RequestParam("categoryNameUpdate") String name,
            @RequestParam("categoryDescriptionUpdate") String description,
            RedirectAttributes redirectAttributes){
        if(name == null || name.trim().isEmpty()){
            redirectAttributes.addFlashAttribute("alert", "Không được để trống tên");
            redirectAttributes.addFlashAttribute("type","error");
            return "redirect:/admin/category/hien-thi";
        }
        Category checkTonTai = categoryService.findByName(name);
        if(checkTonTai != null && !checkTonTai.getId().equals(id)){
            redirectAttributes.addFlashAttribute("alert", "Đã tồn tại kiểu loại");
            redirectAttributes.addFlashAttribute("type", "error");
            return "redirect:/admin/category/hien-thi";
        } else {
            categoryService.update(id,name, description);
            redirectAttributes.addFlashAttribute("alert", "Cập nhật kiểu loại thành công!");
            redirectAttributes.addFlashAttribute("type", "success");
            return "redirect:/admin/category/hien-thi";
        }
    }

    @GetMapping("/change-status/{id}")
    public String changeStatus(
            @PathVariable("id") Integer id, RedirectAttributes redirectAttributes){
        Category category = categoryService.findById(id);
        if (category.getStatus()){
            long countTrue = categoryService.getAll().stream()
                    .filter(c -> c.getStatus() && !c.getId().equals(id))
                    .count();

            if (countTrue == 0) {
                redirectAttributes.addFlashAttribute("alert", "Không được tắt hết hoạt động!");
                redirectAttributes.addFlashAttribute("type", "error");
                return "redirect:/admin/category/hien-thi";
            }
        }
        categoryService.changeStatus(id);
        redirectAttributes.addFlashAttribute("alert", "Thay đổi trạng thái thành công!");
        redirectAttributes.addFlashAttribute("type", "success");
        return "redirect:/admin/category/hien-thi";
    }

}
