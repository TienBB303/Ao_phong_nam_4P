package com.example.datn.services.product_and_other;

import com.example.datn.entities.product_and_other.Color;
import com.example.datn.entities.product_and_other.Product;
import com.example.datn.entities.product_and_other.ProductDetail;
import com.example.datn.entities.product_and_other.Size;
import com.example.datn.repositories.product_and_other.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {
    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductDetailRepository productDetailRepository;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    MaterialRepository materialCategory;

    public List<Product> getAll() {
        return productRepository.getAll();
    }

    public Page<Product> getAll(Pageable pageable) {
        return productRepository.getAll(pageable);
    }

    public Page<Product> searchPage(String name, Boolean status, Integer categoryId, Integer brandId, Integer materialId, Pageable pageable) {
        return productRepository.search(name, status, categoryId, brandId, materialId, pageable);
    }

    public ProductDetail detailProductDetail(Integer id) {
        return productDetailRepository.findProductDetailById(id);
    }

    public ProductDetail findProductDetailById(Integer id) {
        return productDetailRepository.findProductDetailById(id);
    }

    public Product findByIdProduct(Integer id) {
        return productRepository.findByIdProduct(id);
    }

    public List<ProductDetail> findAllProductDetailByIdProduct(Integer id) {
        return productRepository.findAllProductDetail(id);
    }

    public Product detail(Integer id) {
        Product product = productRepository.findByIdProduct(id);
        return product;
    }

    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    public ProductDetail addProductDetail(ProductDetail productDetail) {
        return productDetailRepository.save(productDetail);
    }

    public Product update(Product product) {
        return productRepository.save(product);
    }

    public String findLastCodeProduct() {
        return productRepository.findMaxCodeProduct();
    }

    public String taoMaTuDongSanPham() {
        String lastCode = findLastCodeProduct();
        int nextCode = 1;

        if (lastCode != null && !lastCode.trim().isEmpty()) {
            try {
                String numberPart = lastCode.substring(2); // lay so phia sau SP
                nextCode = Integer.parseInt(numberPart) + 1; // cong them 1
            } catch (NumberFormatException e) {
//                hihi
            }
        }
        return String.format("SP%03d", nextCode);
    }

    public Boolean checkMaTrung(String code) {
        Product product = productRepository.findByCodeProduct(code);
        return product != null;
    }

    public Boolean checkNameTrung(String name) {
        Product product = productRepository.findNameAlreadyHave(name);
        return product != null;
    }

    public List<ProductDetail> searchProductDetail(String keyword) {
        return productDetailRepository.searchProductDetailByKeyword(keyword);
    }

    public Integer tongSoLuongSPCT(Integer id) {
        Integer result = productRepository.tongSoLuongTheoSanPham(id);
        return result != null ? result : 0;
    }

    public List<Color> findColorsByProductId(Integer productId) {
        return productRepository.findColorsByProductId(productId);
    }

    public List<Size> findSizesByProductId(Integer productId) {
        return productRepository.findSizesByProductId(productId);
    }

    public Product findByCode(String code) {
        return productRepository.findByCode(code);
    }

    public void updateProductDetail(ProductDetail productDetail) {
        productDetailRepository.save(productDetail);
    }

    public List<ProductDetail> getAllProductDetails() {
        return productDetailRepository.getAllProductDetails();
    }


    public Map<Integer, String> getMinMaxPriceByProduct() {
        List<Object[]> results = productDetailRepository.findMinMaxPricesGroupedByProductId();
        Map<Integer, String> priceMap = new HashMap<>();

        for (Object[] row : results) {
            Integer productId = (Integer) row[0];
            BigDecimal min = (BigDecimal) row[1];
            BigDecimal max = (BigDecimal) row[2];

            if (min != null && max != null) {
                String priceRange;
                if (min.compareTo(max) == 0) {
                    priceRange = formatCurrency(min); // Ví dụ: "100.000đ"
                } else {
                    priceRange = formatCurrency(min) + " - " + formatCurrency(max);
                }
                priceMap.put(productId, priceRange);
            }
        }

        return priceMap;
    }

    private String formatCurrency(BigDecimal value) {
        return String.format("%,.0f", value) + "đ"; // Định dạng kiểu "100.000đ"
    }

    public ProductDetail findProductDetailByColorAndSize(Integer productId, Integer colorId, Integer sizeId) {
        return productDetailRepository.findByProductIdAndColorIdAndSizeId(productId, colorId, sizeId);
    }

    public Page<Product> searchAllFields(String name, Integer categoryId, Integer brandId, Integer materialId, Pageable pageable) {
        return productRepository.searchAllFields(name, categoryId, brandId, materialId, pageable);
    }

//    public void addProductDetail(Integer productId, BigDecimal price, Integer quantity,
//                                 Integer colorId, Integer sizeId) {
//        Product product = productRepository.findById(productId).orElseThrow();
//        Color color = colorRepository.findById(colorId).orElseThrow();
//        Size size = sizeRepository.findById(sizeId).orElseThrow();
//
//        ProductDetail detail = new ProductDetail();
//        detail.setProduct(product);
//        detail.setColor(color);
//        detail.setSize(size);
//        detail.setPrice(price);
//        detail.setQuantity(quantity);
//
//        productDetailRepository.save(detail);
//    }


}

