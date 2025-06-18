package com.example.datn.services.product_and_other;

import com.example.datn.entities.product_and_other.Product;
import com.example.datn.repositories.product_and_other.BrandRepository;
import com.example.datn.repositories.product_and_other.CategoryRepository;
import com.example.datn.repositories.product_and_other.MaterialCategory;
import com.example.datn.repositories.product_and_other.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    @Autowired
    ProductRepository productRepository;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    MaterialCategory materialCategory;

    public List<Product> getAll(){
        return productRepository.getAll();
    }

    public Product detail(Integer id){
        Product product = productRepository.findByIdProduct(id);
        return product;
    }

    public Product addProduct(Product product){
        product.setCategory(categoryRepository.findById(product.getCategory().getId()).orElse(null));
        product.setBrand(brandRepository.findById(product.getBrand().getId()).orElse(null));
        product.setMaterial(materialCategory.findById(product.getMaterial().getId()).orElse(null));
        return productRepository.save(product);
    }

    public Product update(Integer id, Product updateProduct){
        if (id == null){
            System.out.println("Do not have product with id = " + id);
            return null;
        }
        Product product = productRepository.findByIdProduct(id);
        product.setCode(updateProduct.getCode());
        product.setName(updateProduct.getName());
        product.setStatus(updateProduct.getStatus());
        product.setDescription(updateProduct.getDescription());
        product.setCategory(updateProduct.getCategory());
        product.setBrand(updateProduct.getBrand());
        product.setMaterial(updateProduct.getMaterial());

        System.out.println("Product save done!");
        return productRepository.save(product);
    }
}
