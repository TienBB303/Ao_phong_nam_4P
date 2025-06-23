package com.example.datn.controllers.product_and_other;

import com.example.datn.dto.product.ProductDetailForm;
import com.example.datn.dto.product.ProductForm;
import com.example.datn.entities.product_and_other.*;
import com.example.datn.services.product_and_other.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/product")
public class ProductController {
    @Autowired
    private ProductService productService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    BrandService brandService;

    @Autowired
    MaterialService materialService;

    @Autowired
    ColorService colorService;

    @Autowired
    SizeService sizeService;

    @ModelAttribute("listCategory")
    public List<Category> listCategory() {
        return categoryService.getAll();
    }

    @ModelAttribute("listBrand")
    public List<Brand> listBrand() {
        return brandService.getAll();
    }

    @ModelAttribute("listMaterial")
    public List<Material> listMaterial() {
        return materialService.getAll();
    }

    @ModelAttribute("listColor")
    public List<Color> listColor() {
        return colorService.getAll();
    }
    @ModelAttribute("listSize")
    public List<Size> listSize() {
        return sizeService.getAll();
    }

    @GetMapping("/hien-thi")
    public String sanPham(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "5") int size,
                          Model model) {
        Page<Product> listProduct = productService.getAll(PageRequest.of(page, size));

        model.addAttribute("listProduct", listProduct);
        model.addAttribute("currentPage", page);
        return "admin/product_and_other/product/ProductView";
    }

    @GetMapping("/detail/{id}")
    public Product detailProduct(@PathVariable("id") Integer id){
        return productService.detail(id);
    }

    @GetMapping("/view-add")
    public String viewAdd(){
        return "admin/product_and_other/product/ProductViewAdd";
    }

    @GetMapping("/view-atribute")
    public String viewAtribute(){
        return "admin/product_and_other/product/AtributeView";
    }

    @PostMapping("/add")
    public String AddNewProduct(
            @ModelAttribute ProductForm productForm// khi binding thuôc tính thì name ngoài view phải trừng trong enity ProductForm
            ){
        Product product = new Product();
        String code = (productForm.getCode() == null || productForm.getCode().trim().isEmpty()) ? productService.taoMaTuDongSanPham() : productForm.getCode().trim();
        product.setCode(code);
        product.setName(productForm.getName());
        product.setStatus(true);
        product.setDescription(productForm.getDescription());

        Category category = categoryService.findById(productForm.getCategoryId());
        Brand brand = brandService.findById(productForm.getBrandId());
        Material material = materialService.findById(productForm.getMaterialId());

        product.setCategory(category);
        System.out.println(category.getId());
        product.setBrand(brand);
        product.setMaterial(material);

        productService.addProduct(product);

        for (ProductDetailForm pdf : productForm.getVariants()){
            ProductDetail productDetail = new ProductDetail();
            productDetail.setProduct(product);
            productDetail.setColor(colorService.findById(pdf.getColorId()));
            productDetail.setSize(sizeService.findById(pdf.getSizeId()));
            productDetail.setPrice(pdf.getPrice());
            productDetail.setQuantity(pdf.getQuantity());

//            String path = "src/main/resources/static/barcode/" + code + ".png";
//            Ulities.generateBarcodeImage(code, path);
            String barcode = product.getCode() + "-C" + pdf.getColorId() + "-S" + pdf.getSizeId();
            productDetail.setBarcode(barcode);

            productService.addProductDetail(productDetail);
        }

        return "redirect:/admin/product/hien-thi";
    }

    @PostMapping("/update/{id}")
    public Product updateProduct(@PathVariable("id") Integer id, @RequestBody Product product){
        return productService.update(id, product);
    }



}

