package com.example.datn.services.product_and_other;

import com.example.datn.entities.product_and_other.Color;
import com.example.datn.entities.product_and_other.Image;
import com.example.datn.entities.product_and_other.Product;
import com.example.datn.entities.product_and_other.ProductDetail;
import com.example.datn.repositories.product_and_other.ColorRepository;
import com.example.datn.repositories.product_and_other.ImageRepository;
import com.example.datn.repositories.product_and_other.ProductDetailRepository;
import com.example.datn.repositories.product_and_other.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class ImageService {
    @Autowired
    ImageRepository imageRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ColorRepository colorRepository;
    @Autowired
    private ProductDetailRepository productDetailRepository;

//    public String saveFile(MultipartFile file) {
//        try {
//            String originalFilename = file.getOriginalFilename();
//            String fileName = UUID.randomUUID() + "_" + originalFilename;
//
//            Path path = Paths.get("D:/pictures/" + fileName); // dùng dấu /
//            Files.createDirectories(path.getParent());
//            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//
//            return "/pictures/" + fileName; // trả đường dẫn tương đối
//        } catch (IOException e) {
//            throw new RuntimeException("Không thể lưu file: " + file.getOriginalFilename(), e);
//        }
//    }

    public Image saveImage(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path path = Paths.get("D:/pictures/" + fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            Image image = new Image();
            image.setName(file.getOriginalFilename());
            image.setPath_file("/pictures/" + fileName);
            return imageRepository.save(image); // Lưu vào DB

        } catch (IOException e) {
            throw new RuntimeException("Lỗi lưu ảnh: " + e.getMessage());
        }
    }

    public List<Image> getAll(){
        return imageRepository.getAll();
    }

    public Page<Image> getAll(Pageable pageable){
        return imageRepository.getAll(pageable);
    }

}
