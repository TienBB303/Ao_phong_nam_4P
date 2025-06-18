package com.example.datn.services.product_and_other;

import com.example.datn.entities.product_and_other.Category;
import com.example.datn.repositories.product_and_other.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    @Autowired
    CategoryRepository categoryRepository;

    public List<Category> getAll(){
        return categoryRepository.getAll();
    }

    public Category detail(Integer id){
        Category category = categoryRepository.findByIdCategory(id);
        return category;
    }

    public Category addCategory(Category category){
        return categoryRepository.save(category);
    }

    public Category update(Integer id, Category updateCategory){
        if (id == null){
            System.out.println("Do not have category with id = " + id);
            return null;
        }
        Category category = categoryRepository.findByIdCategory(id);
        category.setName(updateCategory.getName());
        category.setStatus(updateCategory.getStatus());
        category.setDescription(updateCategory.getDescription());


        System.out.println("Category save done!");
        return categoryRepository.save(category);
    }
}
