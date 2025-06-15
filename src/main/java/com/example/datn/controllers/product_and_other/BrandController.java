package com.example.datn.controllers.product_and_other;

import com.example.datn.entities.product_and_other.Brand;
import com.example.datn.services.product_and_other.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/brand")
public class BrandController {
    @Autowired
    BrandService brandService;

    @GetMapping("/hien-thi")
    public List<Brand> getAll(){
        return brandService.getAll();
    }


    @GetMapping("/detail/{id}")
    public Brand detailBrand(@PathVariable("id") Integer id){
        return brandService.detail(id);
    }

    @PostMapping("/add")
    public Brand AddNewBrand(@RequestBody Brand brand){
        return brandService.addBrand(brand);
    }

    @PostMapping("/update/{id}")
    public Brand updateBrand(@PathVariable("id") Integer id, @RequestBody Brand brand){
        return brandService.update(id, brand);
    }
}
