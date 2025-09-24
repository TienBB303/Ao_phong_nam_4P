package com.example.datn.services.product_and_other;

import com.example.datn.entities.product_and_other.Size;
import com.example.datn.repositories.product_and_other.SizeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SizeService {
    @Autowired
    SizeRepository sizeRepository;

    public List<Size> getAll(){
        return sizeRepository.getAll();
    }

    public List<Size> getAllSizeOn(){
        return sizeRepository.getAllSizeOn();
    }

    public Page<Size> getAll(Pageable pageable){
        return sizeRepository.getAll(pageable);
    }

    public Page<Size> searchPage(String name, Boolean status, Pageable pageable){
        return sizeRepository.search(name,status, pageable);
    }

    public Size detail(Integer id){
        Size size = sizeRepository.findByIdSize(id);
        return size;
    }

    public Size findByCode(String code){
        return sizeRepository.findByCode(code);
    }

    public Size findByName(String name){
        return sizeRepository.findByName(name);
    }

    public Size findById(Integer id){
        return sizeRepository.findByIdSize(id);
    }
    public Size addSize(Size size){
        return sizeRepository.save(size);
    }

    public Size addSize(String code, String name){
        Size size = new Size();
        size.setCode(code);
        size.setName(name);
        size.setStatus(true);
        return sizeRepository.save(size);
    }

    public Size changeStatus(Integer id){
        if (id == null){
            System.out.println("Do not have size with id = " + id);
            return null;
        }
        Size size = sizeRepository.findByIdSize(id);
        size.setStatus(!size.getStatus());
        return sizeRepository.save(size);
    }

    public Size update(Integer id, String sizeCode,String sizeName){
        if (id == null){
            System.out.println("Do not have size with id = " + id);
            return null;
        }
        Size size = sizeRepository.findByIdSize(id);
        size.setCode(sizeCode);
        size.setName(sizeName);
        return sizeRepository.save(size);
    }
}
