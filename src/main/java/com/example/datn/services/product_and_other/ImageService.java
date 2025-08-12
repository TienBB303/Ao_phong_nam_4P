package com.example.datn.services.product_and_other;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.datn.entities.product_and_other.Image;
import com.example.datn.entities.product_and_other.ProductDetail;
import com.example.datn.repositories.product_and_other.ImageRepository;
import com.example.datn.repositories.product_and_other.ProductDetailRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageService {
    @Autowired
    ImageRepository imageRepository;

    @Autowired
    ProductDetailRepository productDetailRepository;

    @Value("${cloudinary.cloud_name}")
    String cloudName;

    @Value("${cloudinary.api_key}")
    String apiKey;

    @Value("${cloudinary.api_secret}")
    String apiSecret;

    private Cloudinary cloudinary;

    @PostConstruct
    void init() {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret));
    }

    public Image saveImage(MultipartFile file, ProductDetail productDetail) {
        try {
            // Upload lên Cloudinary (đặt folder cho dễ quản lý)
            String publicId = "4pstore/product-detail/" + UUID.randomUUID();
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "folder", "4pstore/product-detail",
                            "overwrite", true,
                            "resource_type", "image"));

            String secureUrl = (String) uploadResult.get("secure_url"); // URL public dùng để hiển thị
            String savedPublicId = (String) uploadResult.get("public_id");

            Image image = new Image();
            image.setName(file.getOriginalFilename());
            // LƯU TRỰC TIẾP URL VÀO DB
            image.setPath_file(secureUrl);

            image.setProductDetail(productDetail);
            return imageRepository.save(image);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi upload Cloudinary: " + e.getMessage(), e);
        }
    }

    public List<Image> getAll(){
        return imageRepository.getAll();
    }

    public Page<Image> getAll(Pageable pageable){
        return imageRepository.getAll(pageable);
    }

    public Image findById(Integer id){
        return imageRepository.findByIdImage(id);
    }

//    public Image saveImage(MultipartFile file, ProductDetail productDetail) throws IOException {
//        try {
//            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
//            Path path = Paths.get("D:/pictures/" + fileName);
//            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//
//            Image image = new Image();
//            image.setName(file.getOriginalFilename());
////            image.setPath_file("/pictures/" + fileName);
//            image.setPath_file(fileName);
//            image.setProductDetail(productDetail);
//            System.out.println("Lưu ảnh: " + file.getOriginalFilename());
//            return imageRepository.save(image); // Lưu vào DB
//        } catch (IOException e) {
//            throw new RuntimeException("Lỗi lưu ảnh: " + e.getMessage());
//        }
//    }

}
