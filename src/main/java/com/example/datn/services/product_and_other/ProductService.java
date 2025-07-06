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

import java.util.List;

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

    public List<Product> getAll(){
        return productRepository.getAll();
    }

    public Page<Product> getAll(Pageable pageable){
        return productRepository.getAll(pageable);
    }

    public Page<Product> searchPage(String name, Boolean status,Integer categoryId, Integer brandId, Integer materialId, Pageable pageable){
        return productRepository.search(name,status, categoryId, brandId, materialId, pageable);
    }

    public ProductDetail detailProductDetail(Integer id){
        return productDetailRepository.findProductDetailById(id);
    }

    public ProductDetail findProductDetailById(Integer id){
        return productDetailRepository.findProductDetailById(id);
    }

    public Product findByIdProduct(Integer id){
        return productRepository.findByIdProduct(id);
    }

    public List<ProductDetail> findAllProductDetailByIdProduct(Integer id){
        return productRepository.findAllProductDetail(id);
    }
    public Product detail(Integer id){
        Product product = productRepository.findByIdProduct(id);
        return product;
    }

    public Product addProduct(Product product){
        return productRepository.save(product);
    }

    public ProductDetail addProductDetail(ProductDetail productDetail){
        return productDetailRepository.save(productDetail);
    }

    public Product update(Product product){
//        if (id == null){
//            System.out.println("Do not have product with id = " + id);
//            return null;
//        }
//        Product product = productRepository.findByIdProduct(id);
//        product.setCode(updateProduct.getCode());
//        product.setName(updateProduct.getName());
//        product.setStatus(updateProduct.getStatus());
//        product.setDescription(updateProduct.getDescription());
//        product.setCategory(updateProduct.getCategory());
//        product.setBrand(updateProduct.getBrand());
//        product.setMaterial(updateProduct.getMaterial());
//
//        System.out.println("ProductForm save done!");
        return productRepository.save(product);
    }

    public String findLastCodeProduct(){
        return productRepository.findMaxCodeProduct();
    }

    public String taoMaTuDongSanPham(){
        String lastCode = findLastCodeProduct();
        int nextCode = 1;

        if(lastCode != null && !lastCode.trim().isEmpty()){
            try{
                String numberPart = lastCode.substring(2); // lay so phia sau SP
                nextCode = Integer.parseInt(numberPart) + 1; // cong them 1
            }catch (NumberFormatException e){
//                hihi
            }
        }
        return String.format("SP%03d",nextCode);
    }

    public Boolean checkNameTrung(String name){
        Product product = productRepository.findNameAlreadyHave(name);
        return product != null;
    }

    public Integer tongSoLuongSPCT(Integer id){
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

    public void updateProductDetail(ProductDetail productDetail){
        productDetailRepository.save(productDetail);
    }
}
