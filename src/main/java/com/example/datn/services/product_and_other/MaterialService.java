package com.example.datn.services.product_and_other;

import com.example.datn.entities.product_and_other.Material;
import com.example.datn.repositories.product_and_other.MaterialCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaterialService {

    @Autowired
    MaterialCategory materialCategory;

    public List<Material> getAll(){
        return materialCategory.getAll();
    }

    public Material detail(Integer id){
        Material material = materialCategory.findByIdMaterial(id);
        return material;
    }

    public Material addMaterial(Material material){
        return materialCategory.save(material);
    }

    public Material update(Integer id, Material updateMaterial){
        if (id == null){
            System.out.println("Do not have material with id = " + id);
            return null;
        }
        Material material = materialCategory.findByIdMaterial(id);
        material.setCode(updateMaterial.getCode());
        material.setName(updateMaterial.getName());
        material.setStatus(updateMaterial.getStatus());

        System.out.println("Material save done!");
        return materialCategory.save(material);
    }
}
