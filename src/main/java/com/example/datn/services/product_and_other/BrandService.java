package com.example.datn.services.product_and_other;

import com.example.datn.entities.product_and_other.Brand;
import com.example.datn.repositories.product_and_other.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrandService {
    @Autowired
    BrandRepository brandRepository;

    public List<Brand> getAll(){
        return brandRepository.getAll();
    }

    public Brand detail(Integer id){
        Brand brand = brandRepository.findByIdBrand(id);
        return brand;
    }

    public Brand addBrand(Brand brand){
        return brandRepository.save(brand);
    }

    public Brand update(Integer id, Brand updateBrand){
        if (id == null){
            System.out.println("Do not have brand with id = " + id);
            return null;
        }
        Brand brand = brandRepository.findByIdBrand(id);
        brand.setCode(updateBrand.getCode());
        brand.setName(updateBrand.getName());
        brand.setStatus(updateBrand.getStatus());

        System.out.println("Brand save done!");
        return brandRepository.save(brand);
    }
}
