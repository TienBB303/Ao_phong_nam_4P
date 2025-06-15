package com.example.datn.controllers.product_and_other;

import com.example.datn.entities.product_and_other.Category;
import com.example.datn.services.product_and_other.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    CategoryService categoryService;

    @GetMapping("/hien-thi")
    public List<Category> getAll(){
        return categoryService.getAll();
    }


    @GetMapping("/detail/{id}")
    public Category detailCategory(@PathVariable("id") Integer id){
        return categoryService.detail(id);
    }

    @PostMapping("/add")
    public Category AddNewCategory(@RequestBody Category category){
        return categoryService.addCategory(category);
    }

    @PostMapping("/update/{id}")
    public Category updateCategory(@PathVariable("id") Integer id, @RequestBody Category category){
        return categoryService.update(id, category);
    }
}
