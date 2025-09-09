package com.example.datn.services.product_and_other;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.datn.entities.product_and_other.Image;
import com.example.datn.entities.product_and_other.ProductDetail;
import com.example.datn.repositories.product_and_other.ImageRepository;
import com.example.datn.repositories.product_and_other.ProductDetailRepository;
import jakarta.annotation.PostConstruct;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ImageService {
    private static final Set<String> ALLOWED_MIME = Set.of("image/jpeg", "image/png");
    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024; // 5MB

    private static final int MAX_WIDTH = 8000;   // tuỳ bạn
    private static final int MAX_HEIGHT = 8000;  // tuỳ bạn

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

    public Image saveImage(MultipartFile file, ProductDetail productDetail) throws Exception {
        // 1) Null/empty
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File rỗng");
        }

        // 2) Kích thước
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BadRequestException("File quá lớn (> 5MB)");
        }

        // 3) Đọc bytes 1 lần
        final byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("Không đọc được dữ liệu file");
        }

        // 4) MIME được trình duyệt báo + đối chiếu magic bytes
        String clientMime = file.getContentType();
        String realMime = sniffMimeFromMagic(bytes);
        if (!ALLOWED_MIME.contains(realMime)) {
            throw new BadRequestException("Chỉ cho phép JPEG/PNG");
        }
        // (tuỳ chọn) so sánh clientMime và realMime để phát hiện giả mạo
        if (clientMime != null && !clientMime.equalsIgnoreCase(realMime)) {
            // không bắt buộc chặn, nhưng nên log
        }

        // 5) Kiểm tra kích thước ảnh (đảm bảo là ảnh thật)
        try (var bais = new ByteArrayInputStream(bytes)) {
            BufferedImage img = ImageIO.read(bais);
            if (img == null) throw new BadRequestException("File không phải là ảnh hợp lệ");
            if (img.getWidth() <= 0 || img.getHeight() <= 0
                    || img.getWidth() > MAX_WIDTH || img.getHeight() > MAX_HEIGHT) {
                throw new BadRequestException("Kích thước ảnh không hợp lệ");
            }
        } catch (IOException e) {
            throw new BadRequestException("Không đọc được ảnh");
        }

        // 6) Upload Cloudinary (resource_type = image)
        try {
            String publicId = "4pstore/product-detail/" + UUID.randomUUID();
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    bytes,
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "folder", "4pstore/product-detail",
                            "overwrite", true,
                            "resource_type", "image"   // bắt buộc là image
                    )
            );

            String secureUrl = (String) uploadResult.get("secure_url");
            String savedPublicId = (String) uploadResult.get("public_id"); // nếu cần lưu

            Image image = new Image();
            image.setName(safeFilename(file.getOriginalFilename()));
            image.setPath_file(secureUrl);
            image.setProductDetail(productDetail);
            return imageRepository.save(image);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi upload Cloudinary: " + e.getMessage(), e);
        }
//
//        try {
//            // Upload lên Cloudinary (đặt folder cho dễ quản lý)
//            String publicId = "4pstore/product-detail/" + UUID.randomUUID();
//            Map<?, ?> uploadResult = cloudinary.uploader().upload(
//                    file.getBytes(),
//                    ObjectUtils.asMap(
//                            "public_id", publicId,
//                            "folder", "4pstore/product-detail",
//                            "overwrite", true,
//                            "resource_type", "image"));
//
//            String secureUrl = (String) uploadResult.get("secure_url"); // URL public dùng để hiển thị
//            String savedPublicId = (String) uploadResult.get("public_id");
//
//            Image image = new Image();
//            image.setName(file.getOriginalFilename());
//            // LƯU TRỰC TIẾP URL VÀO DB
//            image.setPath_file(secureUrl);
//
//            image.setProductDetail(productDetail);
//            return imageRepository.save(image);
//
//        } catch (Exception e) {
//            throw new RuntimeException("Lỗi upload Cloudinary: " + e.getMessage(), e);
//        }
    }

    private String sniffMimeFromMagic(byte[] bytes) {
        if (bytes == null || bytes.length < 4) return "application/octet-stream";
        int b0 = bytes[0] & 0xFF, b1 = bytes[1] & 0xFF, b2 = bytes[2] & 0xFF, b3 = bytes[3] & 0xFF;
        // PNG: 89 50 4E 47
        if (b0 == 0x89 && b1 == 0x50 && b2 == 0x4E && b3 == 0x47) return "image/png";
        // JPEG: FF D8 FF (tiếp theo 00-EF)
        if (b0 == 0xFF && b1 == 0xD8 && b2 == 0xFF) return "image/jpeg";
        return "application/octet-stream";
    }

    /** Tránh path traversal, ký tự lạ… */
    private String safeFilename(String original) {
        if (original == null) return "unknown";
        String name = original.replace("\\", "/");
        int idx = name.lastIndexOf('/');
        if (idx >= 0) name = name.substring(idx + 1);
        // cắt ký tự nguy hiểm
        name = name.replaceAll("[\\r\\n\\t]", "_");
        return name;
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
