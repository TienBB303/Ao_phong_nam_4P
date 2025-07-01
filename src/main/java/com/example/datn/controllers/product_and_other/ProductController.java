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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        Map<Integer, Integer> totalQuantity = new HashMap<>();

        for(Product p : listProduct){
            int total = productService.tongSoLuongSPCT(p.getId());
            totalQuantity.put(p.getId(),total);
        }
        model.addAttribute("totalQuantity", totalQuantity);
        model.addAttribute("listProduct", listProduct);
        model.addAttribute("currentPage", page);
        return "admin/product_and_other/product/ProductView";
    }

    @GetMapping("/detail/{id}")
    public Product detailProduct(@PathVariable("id") Integer id){
        return productService.detail(id);
    }

    @GetMapping("/view-add")
    public String viewAdd(Model model){
        if (!model.containsAttribute("productForm")) {
            ProductForm form = new ProductForm();
            form.setVariants(List.of(new ProductDetailForm())); // tạo trước 1 dòng
            model.addAttribute("productForm", form);
        }
        return "admin/product_and_other/product/ProductViewAdd";
    }

    @GetMapping("/view-atribute")
    public String viewAtribute(){
        return "admin/product_and_other/product/AtributeView";
    }


    @PostMapping("/add")
    public String AddNewProduct(
            @ModelAttribute("productForm") ProductForm productForm,
            Model model) {

        // Gán lại dữ liệu để hiển thị lại khi có lỗi
        model.addAttribute("productForm", productForm);

        if (productForm.getName() == null || productForm.getName().trim().isEmpty()) {
            model.addAttribute("alert", "Tên sản phẩm không được để trống");
            model.addAttribute("type", "error");
            return "admin/product_and_other/product/ProductViewAdd";
        }

        if (productService.checkNameTrung(productForm.getName())) {
            model.addAttribute("alert", "Tên sản phẩm trùng sản phẩm đã có");
            model.addAttribute("type", "error");
            return "admin/product_and_other/product/ProductViewAdd";
        }

        List<ProductDetailForm> variants = productForm.getVariants();
        if (variants == null || variants.isEmpty()) {
            model.addAttribute("alert", "Cần ít nhất một biến thể");
            model.addAttribute("type", "error");
            return "admin/product_and_other/product/ProductViewAdd";
        }

        for (ProductDetailForm pdf : variants) {
            if (pdf.getQuantity() == null || pdf.getQuantity() < 0) {
                model.addAttribute("alert", "Số lượng không hợp lệ");
                model.addAttribute("type", "error");
                return "admin/product_and_other/product/ProductViewAdd";
            }

            if (pdf.getPrice() == null || pdf.getPrice().intValue() < 0) {
                model.addAttribute("alert", "Giá không hợp lệ");
                model.addAttribute("type", "error");
                return "admin/product_and_other/product/ProductViewAdd";
            }

        }

        Product product = new Product();
        String code = (productForm.getCode() == null || productForm.getCode().trim().isEmpty())
                ? productService.taoMaTuDongSanPham()
                : productForm.getCode().trim();
        product.setCode(code);
        product.setName(productForm.getName());
        product.setDescription(productForm.getDescription());
        product.setStatus(true);

        product.setCategory(categoryService.findById(productForm.getCategoryId()));
        product.setBrand(brandService.findById(productForm.getBrandId()));
        product.setMaterial(materialService.findById(productForm.getMaterialId()));

        productService.addProduct(product);

        for (ProductDetailForm pdf : variants) {
            ProductDetail productDetail = new ProductDetail();
            productDetail.setProduct(product);
            productDetail.setColor(colorService.findById(pdf.getColorId()));
            productDetail.setSize(sizeService.findById(pdf.getSizeId()));
            productDetail.setPrice(pdf.getPrice());
            productDetail.setQuantity(pdf.getQuantity());

            String barcode = product.getCode() + "-C" + pdf.getColorId() + "-S" + pdf.getSizeId();
            productDetail.setBarcode(barcode);

            productService.addProductDetail(productDetail);
        }

        return "redirect:/admin/product/hien-thi";
    }

    @GetMapping("/view-detail/{id}")
    public String viewDetailProduct(@PathVariable("id") Integer id, Model model){
        Product product = productService.findByIdProduct(id);
        List<ProductDetail> listDetail = productService.findAllProductDetailByIdProduct(id);

        ProductForm form = new ProductForm();
        form.setCode(product.getCode());
        form.setName(product.getName());
        form.setCategoryId(product.getCategory().getId());
        form.setBrandId(product.getBrand().getId());
        form.setMaterialId(product.getMaterial().getId());
        form.setDescription(product.getDescription());

        model.addAttribute("productForm", form);
        model.addAttribute("listProductDetail", listDetail);

        return "admin/product_and_other/product/ProductDetailView";
    }

    @PostMapping("/update/{id}")
    public Product updateProduct(@PathVariable("id") Integer id, @RequestBody Product product){
        return productService.update(id, product);
    }



}

