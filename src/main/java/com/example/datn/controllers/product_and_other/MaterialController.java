package com.example.datn.controllers.product_and_other;

import com.example.datn.entities.product_and_other.Material;
import com.example.datn.services.product_and_other.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/material")
public class MaterialController {
    @Autowired
    MaterialService materialService;

    @GetMapping("/hien-thi")
    public List<Material> getAll(){
        return materialService.getAll();
    }


    @GetMapping("/detail/{id}")
    public Material detailMaterial(@PathVariable("id") Integer id){
        return materialService.detail(id);
    }

    @PostMapping("/add")
    public Material AddNewMaterial(@RequestBody Material material){
        return materialService.addMaterial(material);
    }

    @PostMapping("/update/{id}")
    public Material updateMaterial(@PathVariable("id") Integer id, @RequestBody Material material){
        return materialService.update(id, material);
    }
}
