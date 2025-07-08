package com.example.datn.controllers.product_and_other;

import com.example.datn.dto.product.ProductDetailForm;
import com.example.datn.dto.product.ProductForm;
import com.example.datn.entities.product_and_other.*;
import com.example.datn.repositories.product_and_other.ProductRepository;
import com.example.datn.services.product_and_other.*;
import com.google.zxing.qrcode.decoder.Mode;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/admin/product")
public class ProductController {

//    private static final String SESSION_PRODUCT_FORM_KEY = "productFormSession";

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
    @Autowired
    private ProductRepository productRepository;

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

    @GetMapping("/view-atribute")
    public String viewAtribute(){
        return "admin/product_and_other/product/AtributeView";
    }

    @GetMapping("/view-add")
    public String viewAdd(Model model, HttpSession session) {
        String sessionKey = (String) session.getAttribute("sessionKey");
        if (sessionKey == null) {
            sessionKey = UUID.randomUUID().toString();
            session.setAttribute("sessionKey", sessionKey);
        }

        ProductForm productForm = (ProductForm) session.getAttribute("add1" + sessionKey);
        if (productForm == null) {
            productForm = new ProductForm();
            session.setAttribute("add1" + sessionKey, productForm);
        }

        model.addAttribute("productForm", productForm);
        model.addAttribute("listColor", colorService.getAll());
        model.addAttribute("listSize", sizeService.getAll());

        return "admin/product_and_other/product/ProductViewAdd";
    }



    @PostMapping("/add1")
    public String add1(@ModelAttribute("productForm") ProductForm productForm, HttpSession session, Model model, RedirectAttributes redirectAttributes){
        if(productService.checkMaTrung(productForm.getCode())){
            redirectAttributes.addFlashAttribute("alert", "Mã bị trùng sản phẩm trước đó");
            redirectAttributes.addFlashAttribute("type", "error");
            return "redirect:/admin/product/view-add";
        }
        String code = (productForm.getCode() == null || productForm.getCode().trim().isEmpty())
                ? productService.taoMaTuDongSanPham()
                : productForm.getCode().trim();
        productForm.setCode(code.trim());
        if (productForm.getName() == null || productForm.getName().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("alert", "Tên sản phẩm không được để trống");
            redirectAttributes.addFlashAttribute("type", "error");
            return "redirect:/admin/product/view-add";
        }

        if (productService.checkNameTrung(productForm.getName())) {
            redirectAttributes.addFlashAttribute("alert", "Tên sản phẩm trùng sản phẩm đã có");
            redirectAttributes.addFlashAttribute("type", "error");
            return "redirect:/admin/product/view-add";
        }
        productForm.setName(productForm.getName().trim());
        productForm.setDescription(productForm.getDescription().trim());

        String sessionKey = UUID.randomUUID().toString(); // tao key giu lai session random
        session.setAttribute("sessionKey", sessionKey);
        session.setAttribute("add1" + sessionKey, productForm); // giu lai san pham da tao

        return "admin/product_and_other/product/ProductViewAddNext";
    }

    @PostMapping("/add2")
    @ResponseBody
    public String addVariant(@RequestParam("colorId") Integer colorId,
                                             @RequestParam("sizeId") Integer sizeId,
                                             @RequestParam(value = "quantity", required = false, defaultValue = "0") Integer quantity,
                                             @RequestParam(value = "price", required = false, defaultValue = "0") BigDecimal price,
                                             HttpSession session,Model model) {
        String sessionKey = (String) session.getAttribute("sessionKey");
        ProductForm productForm = (ProductForm) session.getAttribute("add1" + sessionKey);

        if (productForm == null) {
            return "Session hết hạn";
        }

        boolean exists = productForm.getVariants() != null &&
                productForm.getVariants().stream().anyMatch(p ->
                        p.getColorId().equals(colorId) && p.getSizeId().equals(sizeId));

        if (exists) {
            return "Đã tồn tại";
        }

        // Nếu chưa có thì thêm
        ProductDetailForm newVariant = new ProductDetailForm();
        newVariant.setColorId(colorId);
        newVariant.setSizeId(sizeId);
        newVariant.setQuantity(quantity);
        newVariant.setPrice(price);

        if (productForm.getVariants() == null) {
            productForm.setVariants(new ArrayList<>());
        }
        List<Integer> selectedColorIds = productForm.getVariants().stream()
                .map(ProductDetailForm::getColorId)
                .filter(Objects::nonNull)
                .distinct().toList();
        List<Integer> selectedSizeIds = productForm.getVariants().stream()
                .map(ProductDetailForm::getSizeId)
                .filter(Objects::nonNull)
                .distinct().toList();

        model.addAttribute("selectedColorIds", selectedColorIds);
        model.addAttribute("selectedSizeIds", selectedSizeIds);

        productForm.getVariants().add(newVariant);
        session.setAttribute("add1" + sessionKey, productForm);

        return "success";
    }

    @PostMapping("/save-product")
    public String saveProduct(@RequestParam MultiValueMap<String, MultipartFile> colorImages,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        String sessionKey = (String) session.getAttribute("sessionKey");
        ProductForm productForm = (ProductForm) session.getAttribute("add1" + sessionKey);

        if (productForm == null) {
            redirectAttributes.addFlashAttribute("alert", "Dữ liệu sản phẩm không hợp lệ.");
            redirectAttributes.addFlashAttribute("type", "error");
            return "redirect:/admin/product/view-add";
        }

        // Tạo entity Product từ productForm
        Product product = new Product();
        product.setCode(productForm.getCode());
        product.setName(productForm.getName());
        product.setDescription(productForm.getDescription());
        product.setCategory(categoryService.findById(productForm.getCategoryId()));
        product.setBrand(brandService.findById(productForm.getBrandId()));
        product.setMaterial(materialService.findById(productForm.getMaterialId()));
        product.setStatus(true);

        product = productRepository.save(product);

        // Map lưu image theo colorId
        Map<Integer, Image> colorImageMap = new HashMap<>();
        for (String key : colorImages.keySet()) {
            Integer colorId = Integer.parseInt(key.replaceAll("[^0-9]", ""));
            List<MultipartFile> files = colorImages.get(key);

            if (files != null && !files.isEmpty()) {
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        Image image = imageService.saveImage(file);
//                        colorImageMap.putIfAbsent(colorId, image); // lấy ảnh đầu tiên làm đại diện
                        break;
                    }
                }
            }
        }

        for (ProductDetailForm form : productForm.getVariants()) {
            ProductDetail detail = new ProductDetail();
            detail.setProduct(product);
            detail.setColor(colorService.findById(form.getColorId()));
            detail.setSize(sizeService.findById(form.getSizeId()));
            detail.setQuantity(form.getQuantity());
            detail.setPrice(form.getPrice());
            detail.setBarcode(product.getCode() + "-C" + form.getColorId() + "-S" + form.getSizeId());

            if (colorImageMap.containsKey(form.getColorId())) {
                detail.setImage(colorImageMap.get(form.getColorId()));
            }

            productService.addProductDetail(detail);
        }

        // clear session
        session.removeAttribute("add1" + sessionKey);
        session.removeAttribute("sessionKey");

        redirectAttributes.addFlashAttribute("alert", "Lưu sản phẩm thành công!");
        redirectAttributes.addFlashAttribute("type", "success");
        return "redirect:/admin/product/hien-thi";
    }

    @PostMapping("/add-nhanh/category")
    public String addNhanhCategory(@RequestParam("categoryName") String categoryName,
                                   @RequestParam("categoryDescription") String categoryDescription,
                                   @RequestParam("productCode") String productCode,
                                   @RequestParam("productName") String productName,
                                   @RequestParam("productDescription") String productDescription,
                                   HttpSession session,
                                   Model model,
                                   RedirectAttributes  redirectAttributes) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("alert", "Tên kiểu loại không được để trống");
            redirectAttributes.addFlashAttribute("type", "error");
            return "redirect:/admin/product/view-add";
        }

        Category checkTonTai = categoryService.findByName(categoryName.trim());
        if (checkTonTai != null) {
            redirectAttributes.addFlashAttribute("alert", "Đã tồn tại kiểu loại");
            redirectAttributes.addFlashAttribute("type", "error");
            return "redirect:/admin/product/view-add";
        }
        Category category = new Category();
        category.setName(categoryName.trim());
        category.setDescription(categoryDescription.trim());
        category.setStatus(true);

        categoryService.addCategoryObj(category);


        redirectAttributes.addFlashAttribute("alert", "Thêm kiểu loại thành công!");
        redirectAttributes.addFlashAttribute("type", "success");
        model.addAttribute("listCategory", categoryService.getAll());
        model.addAttribute("listBrand", brandService.getAll());
        model.addAttribute("listMaterial", materialService.getAll());
        return "redirect:/admin/product/view-add";
    }

    @PostMapping("/add-nhanh/brand")
    public String addNhanhBrand(@RequestParam("brandName") String brandName,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (brandName == null || brandName.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("alert", "Tên hãng không được để trống");
            redirectAttributes.addFlashAttribute("type", "error");
            return "redirect:/admin/product/view-add";
        }

        Brand checkTonTai = brandService.findByName(brandName.trim());
        if (checkTonTai != null) {
            redirectAttributes.addFlashAttribute("alert", "Đã tồn tại hãng");
            redirectAttributes.addFlashAttribute("type", "error");
            return "redirect:/admin/product/view-add";
        }
        Brand brand = new Brand();
        brand.setCode(brandService.taoMaTuDongBrand());
        brand.setName(brandName.trim());
        brand.setStatus(true);
        brandService.addBrand(brand);

        redirectAttributes.addFlashAttribute("alert", "Thêm hãng thành công!");
        redirectAttributes.addFlashAttribute("type", "success");
        model.addAttribute("listCategory", categoryService.getAll());
        model.addAttribute("listBrand", brandService.getAll());
        model.addAttribute("listMaterial", materialService.getAll());
        return "redirect:/admin/product/view-add";
    }

    @PostMapping("/add-nhanh/material")
    public String addNhanhMaterial(@RequestParam("materialName") String materialName,
                                   HttpSession session,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (materialName == null || materialName.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("alert", "Tên chất liệu không được để trống");
            redirectAttributes.addFlashAttribute("type", "error");
            return "redirect:/admin/product/view-add";
        }

        Material checkTonTai = materialService.findByName(materialName.trim());
        if (checkTonTai != null) {
            redirectAttributes.addFlashAttribute("alert", "Đã tồn tại chất liệu");
            redirectAttributes.addFlashAttribute("type", "error");
            return "redirect:/admin/product/view-add";
        }
        Material material = new Material();
        material.setCode(materialService.taoMaTuDongMaterial());
        material.setName(materialName.trim());
        material.setStatus(true);
        materialService.addMaterial(material);

        redirectAttributes.addFlashAttribute("alert", "Thêm chất liệu thành công!");
        redirectAttributes.addFlashAttribute("type", "success");
        model.addAttribute("listCategory", categoryService.getAll());
        model.addAttribute("listBrand", brandService.getAll());
        model.addAttribute("listMaterial", materialService.getAll());
        return "redirect:/admin/product/view-add";
    }
//
//    @PostMapping("/add")
//    public String AddNewProduct(HttpSession session,
//                                @RequestParam Map<String, String> params,
//                                @ModelAttribute("productForm") ProductForm productForm,
//                                Model model) {
//
//        // Lấy từ session, giữ lại giá tri của ô select multi
//        List<Integer> selectedColorIds = new ArrayList<>();
//        List<Integer> selectedSizeIds = new ArrayList<>();
//
//        if (productForm.getVariants() != null) {
//            selectedColorIds = productForm.getVariants().stream()
//                    .map(ProductDetailForm::getColorId)
//                    .filter(Objects::nonNull)
//                    .distinct()
//                    .toList();
//
//            selectedSizeIds = productForm.getVariants().stream()
//                    .map(ProductDetailForm::getSizeId)
//                    .filter(Objects::nonNull)
//                    .distinct()
//                    .toList();
//        }
//
//        model.addAttribute("selectedColorIds", selectedColorIds);
//        model.addAttribute("selectedSizeIds", selectedSizeIds);
//        ProductForm sessionForm = (ProductForm) session.getAttribute(SESSION_PRODUCT_FORM_KEY);
//        if (sessionForm == null) {
//            sessionForm = new ProductForm();
//        }
//
//        sessionForm.setName(productForm.getName());
//        sessionForm.setCode(productForm.getCode());
//        sessionForm.setCategoryId(productForm.getCategoryId());
//        sessionForm.setBrandId(productForm.getBrandId());
//        sessionForm.setMaterialId(productForm.getMaterialId());
//        sessionForm.setDescription(productForm.getDescription());
//
//        sessionForm.setVariants(productForm.getVariants());
//        // Cập nhật các giá trị từ variants đang submit (quantity, price)
//        List<ProductDetailForm> currentVariants = sessionForm.getVariants();
//
//        // Validate
//        if (sessionForm.getName() == null || sessionForm.getName().trim().isEmpty()) {
//            model.addAttribute("alert", "Tên sản phẩm không được để trống");
//            model.addAttribute("type", "error");
//            model.addAttribute("productForm", sessionForm);
//            return "admin/product_and_other/product/ProductViewAdd";
//        }
//
//        if (productService.checkNameTrung(sessionForm.getName())) {
//            model.addAttribute("alert", "Tên sản phẩm trùng sản phẩm đã có");
//            model.addAttribute("type", "error");
//            model.addAttribute("productForm", sessionForm);
//            return "admin/product_and_other/product/ProductViewAdd";
//        }
//
//        if (currentVariants == null || currentVariants.isEmpty()) {
//            model.addAttribute("alert", "Cần ít nhất một biến thể");
//            model.addAttribute("type", "error");
//            model.addAttribute("productForm", sessionForm);
//            return "admin/product_and_other/product/ProductViewAdd";
//        }
//
//        for (ProductDetailForm pdf : currentVariants) {
//            if (pdf.getQuantity() == null || pdf.getQuantity() < 0) {
//                model.addAttribute("alert", "Số lượng không hợp lệ");
//                model.addAttribute("type", "error");
//                model.addAttribute("productForm", sessionForm);
//                return "admin/product_and_other/product/ProductViewAdd";
//            }
//            if (pdf.getPrice() == null || pdf.getPrice().compareTo(BigDecimal.ZERO) < 0) {
//                model.addAttribute("alert", "Giá không hợp lệ");
//                model.addAttribute("type", "error");
//                model.addAttribute("productForm", sessionForm);
//                return "admin/product_and_other/product/ProductViewAdd";
//            }
//        }
//
//        Product product = new Product();
//        String code = (sessionForm.getCode() == null || sessionForm.getCode().trim().isEmpty())
//                ? productService.taoMaTuDongSanPham()
//                : sessionForm.getCode().trim();
//        product.setCode(code.trim());
//        product.setName(sessionForm.getName().trim());
//        product.setDescription(sessionForm.getDescription().trim());
//        product.setStatus(true);
//        product.setCategory(categoryService.findById(sessionForm.getCategoryId()));
//        product.setBrand(brandService.findById(sessionForm.getBrandId()));
//        product.setMaterial(materialService.findById(sessionForm.getMaterialId()));
//
//        productService.addProduct(product);
//
//        for (ProductDetailForm pdf : currentVariants) {
//            ProductDetail productDetail = new ProductDetail();
//            productDetail.setProduct(product);
//            productDetail.setColor(colorService.findById(pdf.getColorId()));
//            productDetail.setSize(sizeService.findById(pdf.getSizeId()));
//            productDetail.setPrice(pdf.getPrice());
//            productDetail.setQuantity(pdf.getQuantity());
//            productDetail.setBarcode(product.getCode() + "-C" + pdf.getColorId() + "-S" + pdf.getSizeId());
//
//            productService.addProductDetail(productDetail);
//        }
//        session.removeAttribute(SESSION_PRODUCT_FORM_KEY);
//        model.addAttribute("alert", "Thêm sản phẩm thành công");
//        model.addAttribute("type", "success");
//        return "redirect:/admin/product/hien-thi";
//    }

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



}

