package com.example.datn.services.product_and_other;

import com.example.datn.entities.product_and_other.Image;
import com.example.datn.repositories.product_and_other.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImageService {
    @Autowired
    ImageRepository imageRepository;

    public List<Image> getAll(){
        return imageRepository.getAll();
    }

    public Page<Image> getAll(Pageable pageable){
        return imageRepository.getAll(pageable);
    }

    public Image detail(Integer id){
        Image image = imageRepository.findByIdImage(id);
        return image;
    }

    public Image addImage(Image image){
        return imageRepository.save(image);
    }

    public Image update(Integer id, Image updateImage){
        if (id == null){
            System.out.println("Do not have image with id = " + id);
            return null;
        }
        Image image = imageRepository.findByIdImage(id);
        image.setPath_file(updateImage.getPath_file());
        image.setName(updateImage.getName());
//        image.setProduct(updateImage.getProduct());

        System.out.println("Image save done!");
        return imageRepository.save(image);
    }
}
