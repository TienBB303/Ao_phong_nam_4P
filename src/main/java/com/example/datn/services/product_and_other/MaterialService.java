package com.example.datn.services.product_and_other;

import com.example.datn.entities.product_and_other.Material;
import com.example.datn.repositories.product_and_other.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaterialService {
    @Autowired
    MaterialRepository materialRepository;

    public List<Material> getAll(){
        return materialRepository.getAll();
    }

    public Page<Material> getAll(Pageable pageable){
        return materialRepository.getAll(pageable);
    }

    public Page<Material> searchPage(String name, Boolean status, Pageable pageable){
        return materialRepository.search(name,status, pageable);
    }

    public Material detail(Integer id){
        Material material = materialRepository.findByIdMaterial(id);
        return material;
    }

    public Material findByCode(String code){
        return materialRepository.findByCode(code);
    }

    public Material findByName(String name){
        return materialRepository.findByName(name);
    }

    public Material findById(Integer id){
        return materialRepository.findByIdMaterial(id);
    }
    public Material addMaterial(Material material){
        return materialRepository.save(material);
    }

    public Material addMaterial(String code, String name){
        Material material = new Material();
        material.setCode(code);
        material.setName(name);
        material.setStatus(true);
        return materialRepository.save(material);
    }

    public Material changeStatus(Integer id){
        if (id == null){
            System.out.println("Do not have material with id = " + id);
            return null;
        }
        Material material = materialRepository.findByIdMaterial(id);
        material.setStatus(!material.getStatus());
        return materialRepository.save(material);
    }

    public Material update(Integer id, String materialCode,String materialName){
        if (id == null){
            System.out.println("Do not have material with id = " + id);
            return null;
        }
        Material material = materialRepository.findByIdMaterial(id);
        material.setCode(materialCode);
        material.setName(materialName);
        return materialRepository.save(material);
    }
}
