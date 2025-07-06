package com.example.datn.services.product_and_other;

import com.example.datn.entities.product_and_other.Category;
import com.example.datn.repositories.product_and_other.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    @Autowired
    CategoryRepository categoryRepository;

    public List<Category> getAll(){
        return categoryRepository.getAll();
    }

    public Page<Category> getAll(Pageable pageable){
        return categoryRepository.getAll(pageable);
    }

    public Page<Category> searchPage(String name, Boolean status, Pageable pageable){
        return categoryRepository.search(name,status, pageable);
    }

    public Category detail(Integer id){
        Category category = categoryRepository.findByIdCategory(id);
        return category;
    }

    public Category findByName(String name){
        return categoryRepository.findByName(name);
    }

    public Category findById(Integer id){
        return categoryRepository.findByIdCategory(id);
    }
    public Category addCategory(Category category){
        return categoryRepository.save(category);
    }


    public Category addCategoryObj(Category category){
        return categoryRepository.save(category);
    }
    public Category addCategory(String name, String description){
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setStatus(true);
        return categoryRepository.save(category);
    }

    public Category changeStatus(Integer id){
        if (id == null){
            System.out.println("Do not have category with id = " + id);
            return null;
        }
        Category category = categoryRepository.findByIdCategory(id);
        category.setStatus(!category.getStatus());
        return categoryRepository.save(category);
    }

    public Category update(Integer id, String categoryName, String categoryDescription){
        if (id == null){
            System.out.println("Do not have category with id = " + id);
            return null;
        }
        Category category = categoryRepository.findByIdCategory(id);
        category.setName(categoryName);
        category.setDescription(categoryDescription);
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
