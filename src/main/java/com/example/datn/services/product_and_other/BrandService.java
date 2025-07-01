package com.example.datn.services.product_and_other;

import com.example.datn.entities.product_and_other.Brand;
import com.example.datn.repositories.product_and_other.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrandService {
    @Autowired
    BrandRepository brandRepository;

    public List<Brand> getAll(){
        return brandRepository.getAll();
    }

    public Page<Brand> getAll(Pageable pageable){
        return brandRepository.getAll(pageable);
    }

    public Page<Brand> searchPage(String name, Boolean status, Pageable pageable){
        return brandRepository.search(name,status, pageable);
    }

    public Brand detail(Integer id){
        Brand brand = brandRepository.findByIdBrand(id);
        return brand;
    }

    public Brand findByCode(String code){
        return brandRepository.findByCode(code);
    }

    public Brand findByName(String name){
        return brandRepository.findByName(name);
    }

    public Brand findById(Integer id){
        return brandRepository.findByIdBrand(id);
    }

    public Brand addBrand(Brand brand){
        return brandRepository.save(brand);
    }

    public Brand addBrand(String code, String name){
        Brand brand = new Brand();
        brand.setCode(code);
        brand.setName(name);
        brand.setStatus(true);
        return brandRepository.save(brand);
    }

    public Brand changeStatus(Integer id){
        if (id == null){
            System.out.println("Do not have brand with id = " + id);
            return null;
        }
        Brand brand = brandRepository.findByIdBrand(id);
        brand.setStatus(!brand.getStatus());
        return brandRepository.save(brand);
    }

    public Brand update(Integer id,String brandName){
        if (id == null){
            System.out.println("Do not have brand with id = " + id);
            return null;
        }
        Brand brand = brandRepository.findByIdBrand(id);
        brand.setName(brandName);
        return brandRepository.save(brand);
    }

    public String findLastCodeBrand(){
        return brandRepository.findMaxCodeBrand();
    }

    public String taoMaTuDongBrand(){
        String lastCode = findLastCodeBrand();
        int nextCode = 1;

        if(lastCode != null && !lastCode.trim().isEmpty()){
            try{
                String numberPart = lastCode.substring(2); // lay so phia sau BR
                nextCode = Integer.parseInt(numberPart) + 1; // cong them 1
            }catch (NumberFormatException e){
//                hihi
            }
        }
        return String.format("BR%03d",nextCode);
    }
}
