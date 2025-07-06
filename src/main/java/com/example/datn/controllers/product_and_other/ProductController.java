package com.example.datn.controllers.product_and_other;

import com.example.datn.dto.product.ProductDetailForm;
import com.example.datn.dto.product.ProductForm;
import com.example.datn.entities.product_and_other.*;
import com.example.datn.services.product_and_other.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/admin/product")
public class ProductController {

    private static final String SESSION_PRODUCT_FORM_KEY = "productFormSession";

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

    @Autowired
    ImageService imageService;

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
                        @RequestParam(value = "productNameSearch", defaultValue = "") String name,
                        @RequestParam(value = "productStatusSearch", defaultValue = "") Boolean status,
                        @RequestParam(value = "productCategorySearch", defaultValue = "") Integer categoryId,
                        @RequestParam(value = "productBrandSearch", defaultValue = "") Integer brandId,
                        @RequestParam(value = "productMaterialSearch", defaultValue = "") Integer materialId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "5") int size,
                          Model model) {
        Page<Product> listProduct = productService.searchPage(name.trim(), status, categoryId, brandId, materialId,PageRequest.of(page, size));

        Map<Integer, Integer> totalQuantity = new HashMap<>();

        for(Product p : listProduct){
            int total = productService.tongSoLuongSPCT(p.getId());
            totalQuantity.put(p.getId(),total);
        }
        model.addAttribute("categoryNameSearch", name.trim());
        model.addAttribute("categoryStatusSearch", status != null ? status : "");
        model.addAttribute("productCategorySearch", categoryId);
        model.addAttribute("productBrandSearch", brandId);
        model.addAttribute("productMaterialSearch", materialId);
        model.addAttribute("size", size);
        model.addAttribute("totalQuantity", totalQuantity);
        model.addAttribute("listProduct", listProduct);
        model.addAttribute("currentPage", page);
        return "admin/product_and_other/product/ProductView";
    }

    @GetMapping("/detail/{id}")
    public Product detailProduct(@PathVariable("id") Integer id){
        return productService.detail(id);
    }
//VIEW
    @GetMapping("/view-add")
    public String viewAdd(HttpSession session, Model model){
        ProductForm form = (ProductForm) session.getAttribute(SESSION_PRODUCT_FORM_KEY);
        if (form == null) {
            form = new ProductForm();
            session.setAttribute(SESSION_PRODUCT_FORM_KEY, form);
        }
        model.addAttribute("productForm", form);
        return "admin/product_and_other/product/ProductViewAdd";
    }

    @GetMapping("/view-atribute")
    public String viewAtribute(){
        return "admin/product_and_other/product/AtributeView";
    }

    @PostMapping("/add-variant")
    @ResponseBody
    public String addVariant(@RequestParam Integer colorId,
                             @RequestParam Integer sizeId,
//                             @RequestParam Integer quantity,
//                             @RequestParam BigDecimal price,
                             HttpSession session) {
        ProductForm productForm = (ProductForm) session.getAttribute(SESSION_PRODUCT_FORM_KEY);
        if (productForm == null) {
            productForm = new ProductForm();
        }

        if (productForm.getVariants() == null) {
            productForm.setVariants(new ArrayList<>());
        }

        boolean isDuplicate = productForm.getVariants().stream()
                .anyMatch(p -> Objects.equals(p.getColorId(), colorId) && Objects.equals(p.getSizeId(), sizeId));

        if (isDuplicate) {
            return "duplicate";
        }

        ProductDetailForm variant = new ProductDetailForm();
        variant.setColorId(colorId);
        variant.setSizeId(sizeId);
//        variant.setQuantity(quantity);
//        variant.setPrice(price);
        productForm.getVariants().add(variant);

        session.setAttribute(SESSION_PRODUCT_FORM_KEY, productForm);
        return "success";
    }

    @PostMapping("/add")
    public String AddNewProduct(HttpSession session,
                                @RequestParam Map<String, String> params,
                                @RequestParam Map<String, MultipartFile[]> colorImages,
                                @ModelAttribute("productForm") ProductForm productForm,
                                Model model) {

        // Lấy từ session, giữ lại giá tri của ô select multi
        List<Integer> selectedColorIds = new ArrayList<>();
        List<Integer> selectedSizeIds = new ArrayList<>();

        if (productForm.getVariants() != null) {
            selectedColorIds = productForm.getVariants().stream()
                    .map(ProductDetailForm::getColorId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            selectedSizeIds = productForm.getVariants().stream()
                    .map(ProductDetailForm::getSizeId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
        }

        model.addAttribute("selectedColorIds", selectedColorIds);
        model.addAttribute("selectedSizeIds", selectedSizeIds);
        ProductForm sessionForm = (ProductForm) session.getAttribute(SESSION_PRODUCT_FORM_KEY);
        if (sessionForm == null) {
            sessionForm = new ProductForm();
        }

        sessionForm.setName(productForm.getName());
        sessionForm.setCode(productForm.getCode());
        sessionForm.setCategoryId(productForm.getCategoryId());
        sessionForm.setBrandId(productForm.getBrandId());
        sessionForm.setMaterialId(productForm.getMaterialId());
        sessionForm.setDescription(productForm.getDescription());

        sessionForm.setVariants(productForm.getVariants());
        // Cập nhật các giá trị từ variants đang submit (quantity, price)
        List<ProductDetailForm> currentVariants = sessionForm.getVariants();

        // Validate
        if (sessionForm.getName() == null || sessionForm.getName().trim().isEmpty()) {
            model.addAttribute("alert", "Tên sản phẩm không được để trống");
            model.addAttribute("type", "error");
            model.addAttribute("productForm", sessionForm);
            return "admin/product_and_other/product/ProductViewAdd";
        }

        if (productService.checkNameTrung(sessionForm.getName())) {
            model.addAttribute("alert", "Tên sản phẩm trùng sản phẩm đã có");
            model.addAttribute("type", "error");
            model.addAttribute("productForm", sessionForm);
            return "admin/product_and_other/product/ProductViewAdd";
        }

        if (currentVariants == null || currentVariants.isEmpty()) {
            model.addAttribute("alert", "Cần ít nhất một biến thể");
            model.addAttribute("type", "error");
            model.addAttribute("productForm", sessionForm);
            return "admin/product_and_other/product/ProductViewAdd";
        }

        for (ProductDetailForm pdf : currentVariants) {
            if (pdf.getQuantity() == null || pdf.getQuantity() < 0) {
                model.addAttribute("alert", "Số lượng không hợp lệ");
                model.addAttribute("type", "error");
                model.addAttribute("productForm", sessionForm);
                return "admin/product_and_other/product/ProductViewAdd";
            }
            if (pdf.getPrice() == null || pdf.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                model.addAttribute("alert", "Giá không hợp lệ");
                model.addAttribute("type", "error");
                model.addAttribute("productForm", sessionForm);
                return "admin/product_and_other/product/ProductViewAdd";
            }
        }

        Product product = new Product();
        String code = (sessionForm.getCode() == null || sessionForm.getCode().trim().isEmpty())
                ? productService.taoMaTuDongSanPham()
                : sessionForm.getCode().trim();
        product.setCode(code.trim());
        product.setName(sessionForm.getName().trim());
        product.setDescription(sessionForm.getDescription().trim());
        product.setStatus(true);
        product.setCategory(categoryService.findById(sessionForm.getCategoryId()));
        product.setBrand(brandService.findById(sessionForm.getBrandId()));
        product.setMaterial(materialService.findById(sessionForm.getMaterialId()));

        productService.addProduct(product);

        for (ProductDetailForm pdf : currentVariants) {
            ProductDetail productDetail = new ProductDetail();
            productDetail.setProduct(product);
            productDetail.setColor(colorService.findById(pdf.getColorId()));
            productDetail.setSize(sizeService.findById(pdf.getSizeId()));
            productDetail.setPrice(pdf.getPrice());
            productDetail.setQuantity(pdf.getQuantity());
            productDetail.setBarcode(product.getCode() + "-C" + pdf.getColorId() + "-S" + pdf.getSizeId());

            productService.addProductDetail(productDetail);
        }
//        for (Integer colorId : selectedColorIds) {
//            MultipartFile[] files = colorImages.get("colorImages[" + colorId + "]");
//            if (files != null && files.length > 0) {
//                for (MultipartFile file : files) {
//                    if (!file.isEmpty()) {
////                        String imagePath = imageService.saveFile(file); // lưu ảnh
////                        imageService.savaImage(product, colorId, imagePath);
//                        imageService.savaImage(product, colorId, file);
//                    }
//                }
//            }
//        }
        session.removeAttribute(SESSION_PRODUCT_FORM_KEY);
        model.addAttribute("alert", "Thêm sản phẩm thành công");
        model.addAttribute("type", "success");
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

    @GetMapping("/get-detail/{id}")
    @ResponseBody
    public Product getDetail(@PathVariable("id") Integer id){
        return productService.detail(id);
    }
//update chung
    @PostMapping("/update")
    public String updateProduct(
            @RequestParam("productId") Integer productId,
            @RequestParam("productName") String productName,
            @RequestParam("categoryId") Integer categoryId,
            @RequestParam("brandId") Integer brandId,
            @RequestParam("materialId") Integer materialId,
            RedirectAttributes redirectAttributes) {

        Product product = productService.findByIdProduct(productId);
        if (product == null) {
            redirectAttributes.addFlashAttribute("alert", "Không tìm thấy sản phẩm");
            redirectAttributes.addFlashAttribute("type", "error");
            return "redirect:/admin/product/hien-thi";
        }
        if (!productName.trim().equalsIgnoreCase(product.getName()) && productService.checkNameTrung(productName.trim())) {
            redirectAttributes.addFlashAttribute("alert", "Tên sản phẩm trùng sản phẩm đã có");
            redirectAttributes.addFlashAttribute("type", "error");
            return "redirect:/admin/product/hien-thi";
        }
        product.setName(productName.trim());
        product.setCategory(categoryService.findById(categoryId));
        product.setBrand(brandService.findById(brandId));
        product.setMaterial(materialService.findById(materialId));
        productService.update(product);

        redirectAttributes.addFlashAttribute("alert", "Cập nhật thành công");
        redirectAttributes.addFlashAttribute("type", "success");

        return "redirect:/admin/product/hien-thi";
    }

    @PostMapping("/add-nhanh/category")
    public String addNhanhCategory(@RequestParam("categoryName") String categoryName,
                           @RequestParam("categoryDescription") String categoryDescription,
                           HttpSession session,
                           Model model) {
        ProductForm productForm = (ProductForm) session.getAttribute(SESSION_PRODUCT_FORM_KEY);
        model.addAttribute("productForm", productForm);
        if (categoryName == null || categoryName.trim().isEmpty()) {
            model.addAttribute("alert", "Tên kiểu loại không được để trống");
            model.addAttribute("type", "error");
            return "admin/product_and_other/product/ProductViewAdd";
        }

        Category checkTonTai = categoryService.findByName(categoryName.trim());
        if (checkTonTai != null) {
            model.addAttribute("alert", "Đã tồn tại kiểu loại");
            model.addAttribute("type", "error");
            return "admin/product_and_other/product/ProductViewAdd";
        }
        Category category = new Category();
        category.setName(categoryName.trim());
        category.setDescription(categoryDescription.trim());

        categoryService.addCategoryObj(category);

        model.addAttribute("alert", "Thêm kiểu loại thành công!");
        model.addAttribute("type", "success");
        model.addAttribute("listCategory", categoryService.getAll());
        model.addAttribute("listBrand", brandService.getAll());
        model.addAttribute("listMaterial", materialService.getAll());
        return "admin/product_and_other/product/ProductViewAdd";
    }

    @PostMapping("/add-nhanh/brand")
    public String addNhanhBrand(@RequestParam("brandName") String brandName,
                           HttpSession session,
                           Model model) {
        ProductForm productForm = (ProductForm) session.getAttribute(SESSION_PRODUCT_FORM_KEY);
        model.addAttribute("productForm", productForm);
        if (brandName == null || brandName.trim().isEmpty()) {
            model.addAttribute("alert", "Tên hãng không được để trống");
            model.addAttribute("type", "error");
            return "admin/product_and_other/product/ProductViewAdd";
        }

        Brand checkTonTai = brandService.findByName(brandName.trim());
        if (checkTonTai != null) {
            model.addAttribute("alert", "Đã tồn tại hãng");
            model.addAttribute("type", "error");
            return "admin/product_and_other/product/ProductViewAdd";
        }
        Brand brand = new Brand();
        brand.setCode(brandService.taoMaTuDongBrand());
        brand.setName(brandName.trim());
        brand.setStatus(true);
        brandService.addBrand(brand);

        model.addAttribute("alert", "Thêm hãng thành công!");
        model.addAttribute("type", "success");
        model.addAttribute("listCategory", categoryService.getAll());
        model.addAttribute("listBrand", brandService.getAll());
        model.addAttribute("listMaterial", materialService.getAll());
        return "admin/product_and_other/product/ProductViewAdd";
    }

    @PostMapping("/add-nhanh/material")
    public String addNhanhMaterial(@RequestParam("materialName") String materialName,
                           HttpSession session,
                           Model model) {
        ProductForm productForm = (ProductForm) session.getAttribute(SESSION_PRODUCT_FORM_KEY);
        model.addAttribute("productForm", productForm);
        if (materialName == null || materialName.trim().isEmpty()) {
            model.addAttribute("alert", "Tên chất liệu không được để trống");
            model.addAttribute("type", "error");
            return "admin/product_and_other/product/ProductViewAdd";
        }

        Material checkTonTai = materialService.findByName(materialName.trim());
        if (checkTonTai != null) {
            model.addAttribute("alert", "Đã tồn tại chất liệu");
            model.addAttribute("type", "error");
            return "admin/product_and_other/product/ProductViewAdd";
        }
        Material material = new Material();
        material.setCode(materialService.taoMaTuDongMaterial());
        material.setName(materialName.trim());
        material.setStatus(true);
        materialService.addMaterial(material);

        model.addAttribute("alert", "Thêm chất liệu thành công!");
        model.addAttribute("type", "success");
        model.addAttribute("listCategory", categoryService.getAll());
        model.addAttribute("listBrand", brandService.getAll());
        model.addAttribute("listMaterial", materialService.getAll());
        return "admin/product_and_other/product/ProductViewAdd";
    }

}

