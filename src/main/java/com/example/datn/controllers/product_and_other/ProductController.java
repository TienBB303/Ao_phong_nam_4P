package com.example.datn.controllers.product_and_other;

import com.example.datn.dto.product.ProductDetailForm;
import com.example.datn.dto.product.ProductForm;
import com.example.datn.entities.product_and_other.*;
import com.example.datn.repositories.product_and_other.ProductRepository;
import com.example.datn.services.product_and_other.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.zxing.WriterException;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @Autowired
    ImageService imageService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private Barcode barcodeService;

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
    public String viewAdd1(Model model, HttpSession session) {
        String sessionKey = (String) session.getAttribute("sessionKey");
        if (sessionKey == null) {
            sessionKey = UUID.randomUUID().toString();
            session.setAttribute("sessionKey", sessionKey);
        }

        ProductForm productForm = (ProductForm) session.getAttribute("add1" + sessionKey);
        if (productForm == null) {
            productForm = new ProductForm();
        }

        // Nếu có flash attribute từ lần redirect trước -> ưu tiên
        if(model.containsAttribute("productForm")){
            productForm = (ProductForm) model.asMap().get("productForm");
            // Đồng bộ lại vào session để không mất khi reload
            session.setAttribute("add1" + sessionKey, productForm);
        }

        model.addAttribute("productForm", productForm);
        model.addAttribute("listCategory", categoryService.getAll());
        model.addAttribute("listBrand", brandService.getAll());
        model.addAttribute("listMaterial", materialService.getAll());

        return "admin/product_and_other/product/ProductViewAdd";
    }

    @GetMapping("/view-add2")
    public String viewAdd2(Model model, HttpSession session) throws Exception  {
        String sessionKey = (String) session.getAttribute("sessionKey");
        if (sessionKey == null) {
            sessionKey = UUID.randomUUID().toString();
            session.setAttribute("sessionKey", sessionKey);
        }

        ProductForm productForm = (ProductForm) session.getAttribute("add1" + sessionKey);
        if (productForm == null) {
            productForm = new ProductForm();
        }

        if(model.containsAttribute("productForm")){
            productForm = (ProductForm) model.asMap().get("productForm");
            // Đồng bộ lại vào session để không mất khi reload
            session.setAttribute("add1" + sessionKey, productForm);
        }

        String variantsJson = new com.fasterxml.jackson.databind.ObjectMapper()
                .writeValueAsString(productForm.getVariants() == null ? java.util.List.of() : productForm.getVariants());

        model.addAttribute("productForm", productForm);
        model.addAttribute("listColor", colorService.getAll());
        model.addAttribute("listSize", sizeService.getAll());

        model.addAttribute("variantsJson", variantsJson);
        return "admin/product_and_other/product/ProductViewAddNext";
    }

    @PostMapping("/add1")
    public String add1(@ModelAttribute("productForm") ProductForm productForm, HttpSession session, Model model, RedirectAttributes redirectAttributes){
        if(productService.checkMaTrung(productForm.getCode())){
            redirectAttributes.addFlashAttribute("alert", "Mã bị trùng sản phẩm trước đó");
            redirectAttributes.addFlashAttribute("type", "error");

            redirectAttributes.addFlashAttribute("productForm", productForm);
            return "redirect:/admin/product/view-add";
        }

        String code = (productForm.getCode() == null || productForm.getCode().trim().isEmpty())
                ? productService.taoMaTuDongSanPham()
                : productForm.getCode().trim();
        productForm.setCode(code.trim());

        if (productForm.getName() == null || productForm.getName().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("alert", "Tên sản phẩm không được để trống");
            redirectAttributes.addFlashAttribute("type", "error");

            redirectAttributes.addFlashAttribute("productForm", productForm);
            return "redirect:/admin/product/view-add";
        }

        if (productService.checkNameTrung(productForm.getName())) {
            redirectAttributes.addFlashAttribute("alert", "Tên sản phẩm trùng sản phẩm đã có");
            redirectAttributes.addFlashAttribute("type", "error");

            redirectAttributes.addFlashAttribute("productForm", productForm);
            return "redirect:/admin/product/view-add";
        }
        productForm.setName(productForm.getName().trim());
        productForm.setDescription(productForm.getDescription() == null ? "" : productForm.getDescription().trim());

        // nếu chưa có ket session -> tạo mới
        String sessionKey = (String) session.getAttribute("sessionKey");
        if (sessionKey == null) {
            sessionKey = UUID.randomUUID().toString();
            session.setAttribute("sessionKey", sessionKey);
        }
        session.setAttribute("add1" + sessionKey, productForm);
//        return "admin/product_and_other/product/ProductViewAddNext";
        return "redirect:/admin/product/view-add2";
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
        newVariant.setQuantity(quantity != null ? quantity : 0);
        newVariant.setPrice(price != null ? price : BigDecimal.ZERO);

        if (productForm.getVariants() == null) {
            productForm.setVariants(new ArrayList<>());
        }

        productForm.getVariants().add(newVariant);
        session.setAttribute("add1" + sessionKey, productForm);

        return "success";
    }

    @PostMapping("/apply-price-all")
    @ResponseBody
    public String applyPriceAll(@RequestParam Integer quantity,
                                @RequestParam BigDecimal price,
                                HttpSession session) {
        String sessionKey = (String) session.getAttribute("sessionKey");
        ProductForm productForm = (ProductForm) session.getAttribute("add1" + sessionKey);
        if (productForm == null || productForm.getVariants() == null) return "Session hết hạn hoặc chưa có biến thể";

        for (ProductDetailForm v : productForm.getVariants()) {
            v.setQuantity(quantity);
            v.setPrice(price);
        }
        session.setAttribute("add1" + sessionKey, productForm);
        return "success";
    }

    @PostMapping("/save-product")
    public ResponseEntity<?> saveProduct(  @ModelAttribute("productForm") ProductForm productFormFinal,
                                @RequestParam MultiValueMap<String, MultipartFile> colorImages,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) throws IOException {
        Map<String, Object> resp = new HashMap<>();

        String sessionKey = (String) session.getAttribute("sessionKey");
        ProductForm productForm = (ProductForm) session.getAttribute("add1" + sessionKey);

        if (productForm == null) {
//            redirectAttributes.addFlashAttribute("alert", "Dữ liệu sản phẩm không hợp lệ.");
//            redirectAttributes.addFlashAttribute("type", "error");
//            return "redirect:/admin/product/view-add2";
            resp.put("ok", false);
            resp.put("message", "Dữ liệu sản phẩm không hợp lệ hoặc phiên làm việc đã hết hạn.");
            return ResponseEntity.badRequest().body(resp);
        }

        if (productFormFinal != null && productFormFinal.getVariants() != null) {
            productForm.setVariants(productFormFinal.getVariants());
            session.setAttribute("add1" + sessionKey, productForm);
        }

        if (productForm.getVariants() == null || productForm.getVariants().isEmpty()) {
//            redirectAttributes.addFlashAttribute("alert", "Vui lòng tạo ít nhất 1 biến thể.");
//            redirectAttributes.addFlashAttribute("type", "error");
//            redirectAttributes.addFlashAttribute("productForm", productForm);
//            return "redirect:/admin/product/view-add2";

            resp.put("ok", false);
            resp.put("message", "Vui lòng tạo ít nhất 1 biến thể.");
            return ResponseEntity.badRequest().body(resp);
        }

        for (int i = 0; i < productForm.getVariants().size(); i++) {
            ProductDetailForm v = productForm.getVariants().get(i);
            if (v.getQuantity() == null || v.getQuantity() < 1) {
//                redirectAttributes.addFlashAttribute("alert", "Số lượng phải > 0");
//                redirectAttributes.addFlashAttribute("type", "error");
//                redirectAttributes.addFlashAttribute("productForm", productForm);
//                return "redirect:/admin/product/view-add2";

                resp.put("ok", false);
                resp.put("message", "Số lượng mỗi biến thể phải ≥ 1");
                return ResponseEntity.badRequest().body(resp);
            }
            if (v.getPrice() == null || v.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
//                redirectAttributes.addFlashAttribute("alert", "Giá phải > 0 ");
//                redirectAttributes.addFlashAttribute("type", "error");
//                redirectAttributes.addFlashAttribute("productForm", productForm);
//                return "redirect:/admin/product/view-add2";

                resp.put("ok", false);
                resp.put("message", "Giá mỗi biến thể phải > 0");
                return ResponseEntity.badRequest().body(resp);
            }
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

        for (ProductDetailForm form : productForm.getVariants()) {
            ProductDetail detail = new ProductDetail();
            detail.setProduct(product);
            detail.setColor(colorService.findById(form.getColorId()));
            detail.setSize(sizeService.findById(form.getSizeId()));
            detail.setQuantity(form.getQuantity());
            detail.setPrice(form.getPrice());
            detail.setStatus(true);

            // ====================tạo barcode=========================================================
            detail.setBarcode(product.getCode() + "-C" + form.getColorId() + "-S" + form.getSizeId());

            String barcode = product.getCode() + "-C" + form.getColorId() + "-S" + form.getSizeId();
            detail.setBarcode(barcode);
            // Sinh ảnh barcode và lưu vào thư mục D:\barcode ở bên barcode service
            try {
                barcodeService.generateBarcodeImage(barcode);
            } catch (IOException | WriterException e) {
                e.printStackTrace();
                // Option: có thể log thêm hoặc hiển thị thông báo lỗi ở redirectAttributes
            }
            //=============================kết thúc tạo barcode ==================================

            // Lưu ProductDetail trước để có ID để lưu nhièu ảnh theo id đó
            productService.addProductDetail(detail);

            String key = "colorImages[" + form.getColorId() + "]";
            List<MultipartFile> files = colorImages.get(key);
            if(files != null){
                for(MultipartFile file : files){
                    if(!file.isEmpty()){
                        imageService.saveImage(file,detail);
                    }
                }
            }

        }

        // clear session
        session.removeAttribute("add1" + sessionKey);
        session.removeAttribute("sessionKey");

//        redirectAttributes.addFlashAttribute("alert", "Lưu sản phẩm thành công!");
//        redirectAttributes.addFlashAttribute("type", "success");
//        return "redirect:/admin/product/hien-thi";

        resp.put("ok", true);
        resp.put("message", "Lưu sản phẩm thành công!");
        resp.put("redirect", "/admin/product/hien-thi");
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/remove-variant")
    @ResponseBody
    public String removeVariant(@RequestParam("colorId") Integer colorId,
                                @RequestParam("sizeId") Integer sizeId,
                                HttpSession session, Model model) {
        String sessionKey = (String) session.getAttribute("sessionKey");
        ProductForm productForm = (ProductForm) session.getAttribute("add1" + sessionKey);

        if (productForm == null || productForm.getVariants() == null) {
            return "Session hết hạn";
        }

        // Xóa biến thể khớp colorId & sizeId
        productForm.getVariants().removeIf(p ->
                p.getColorId().equals(colorId) && p.getSizeId().equals(sizeId)
        );

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
        // Cập nhật lại session
        session.setAttribute("add1" + sessionKey, productForm);
        return "success";
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
        String sessionKey = (String) session.getAttribute("sessionKey");
        if (sessionKey == null) {
            sessionKey = UUID.randomUUID().toString();
            session.setAttribute("sessionKey", sessionKey);
        }

        ProductForm productForm = (ProductForm) session.getAttribute("add1" + sessionKey);
        if (productForm == null) {
            productForm = new ProductForm();
        }

        if (productCode != null) productForm.setCode(productCode.trim());
        if (productName != null) productForm.setName(productName.trim());
        if (productDescription != null) productForm.setDescription(productDescription.trim());

        if (categoryName == null || categoryName.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("alert", "Tên kiểu loại không được để trống");
            redirectAttributes.addFlashAttribute("type", "error");

            redirectAttributes.addFlashAttribute("productForm", productForm);
            return "redirect:/admin/product/view-add";
        }

        Category checkTonTai = categoryService.findByName(categoryName.trim());
        if (checkTonTai != null) {
            redirectAttributes.addFlashAttribute("alert", "Đã tồn tại kiểu loại");
            redirectAttributes.addFlashAttribute("type", "error");

            redirectAttributes.addFlashAttribute("productForm", productForm);
            return "redirect:/admin/product/view-add";
        }
        Category category = new Category();
        category.setName(categoryName.trim());
        category.setDescription(categoryDescription == null ? "" : categoryDescription.trim());
        category.setStatus(true);

        categoryService.addCategory(category);
        productForm.setCategoryId(category.getId());

        session.setAttribute("add1" + sessionKey, productForm);
        redirectAttributes.addFlashAttribute("alert", "Thêm kiểu loại thành công!");
        redirectAttributes.addFlashAttribute("type", "success");

        redirectAttributes.addFlashAttribute("productForm", productForm);
        return "redirect:/admin/product/view-add";
    }

    @PostMapping("/add-nhanh/brand")
    public String addNhanhBrand(@RequestParam("brandName") String brandName,
                                @RequestParam("productCode") String productCode,
                                @RequestParam("productName") String productName,
                                @RequestParam("productDescription") String productDescription,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        String sessionKey = (String) session.getAttribute("sessionKey");
        if (sessionKey == null) {
            sessionKey = UUID.randomUUID().toString();
            session.setAttribute("sessionKey", sessionKey);
        }

        ProductForm productForm = (ProductForm) session.getAttribute("add1" + sessionKey);
        if (productForm == null) {
            productForm = new ProductForm();
        }

        if (productCode != null) productForm.setCode(productCode.trim());
        if (productName != null) productForm.setName(productName.trim());
        if (productDescription != null) productForm.setDescription(productDescription.trim());

        if (brandName == null || brandName.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("alert", "Tên hãng không được để trống");
            redirectAttributes.addFlashAttribute("type", "error");

            redirectAttributes.addFlashAttribute("productForm", productForm);
            return "redirect:/admin/product/view-add";
        }

        Brand checkTonTai = brandService.findByName(brandName.trim());
        if (checkTonTai != null) {
            redirectAttributes.addFlashAttribute("alert", "Đã tồn tại hãng");
            redirectAttributes.addFlashAttribute("type", "error");

            redirectAttributes.addFlashAttribute("productForm", productForm);
            return "redirect:/admin/product/view-add";
        }
        Brand brand = new Brand();
        brand.setCode(brandService.taoMaTuDongBrand());
        brand.setName(brandName.trim());
        brand.setStatus(true);
        brandService.addBrand(brand);
        productForm.setBrandId(brand.getId());

        session.setAttribute("add1" + sessionKey, productForm);
        redirectAttributes.addFlashAttribute("alert", "Thêm hãng thành công!");
        redirectAttributes.addFlashAttribute("type", "success");

        redirectAttributes.addFlashAttribute("productForm", productForm);
        return "redirect:/admin/product/view-add";
    }


    @PostMapping("/add-nhanh/material")
    public String addNhanhMaterial(@RequestParam("materialName") String materialName,
                                   @RequestParam("productCode") String productCode,
                                   @RequestParam("productName") String productName,
                                   @RequestParam("productDescription") String productDescription,
                                   HttpSession session,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        String sessionKey = (String) session.getAttribute("sessionKey");
        if (sessionKey == null) {
            sessionKey = UUID.randomUUID().toString();
            session.setAttribute("sessionKey", sessionKey);
        }

        ProductForm productForm = (ProductForm) session.getAttribute("add1" + sessionKey);
        if (productForm == null) {
            productForm = new ProductForm();
        }

        if (productCode != null) productForm.setCode(productCode.trim());
        if (productName != null) productForm.setName(productName.trim());
        if (productDescription != null) productForm.setDescription(productDescription.trim());

        if (materialName == null || materialName.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("alert", "Tên chất liệu không được để trống");
            redirectAttributes.addFlashAttribute("type", "error");

            redirectAttributes.addFlashAttribute("productForm", productForm);
            return "redirect:/admin/product/view-add";
        }

        Material checkTonTai = materialService.findByName(materialName.trim());
        if (checkTonTai != null) {
            redirectAttributes.addFlashAttribute("alert", "Đã tồn tại chất liệu");
            redirectAttributes.addFlashAttribute("type", "error");

            redirectAttributes.addFlashAttribute("productForm", productForm);
            return "redirect:/admin/product/view-add";
        }
        Material material = new Material();
        material.setCode(materialService.taoMaTuDongMaterial());
        material.setName(materialName.trim());
        material.setStatus(true);
        materialService.addMaterial(material);
        productForm.setMaterialId(material.getId());

        session.setAttribute("add1" + sessionKey, productForm);
        redirectAttributes.addFlashAttribute("alert", "Thêm chất liệu thành công!");
        redirectAttributes.addFlashAttribute("type", "success");

        redirectAttributes.addFlashAttribute("productForm", productForm);
        return "redirect:/admin/product/view-add";
    }


    @PostMapping("/add-nhanh/color")
    public String addNhanhColor(@RequestParam("colorCode") String code,
                                @RequestParam("colorName") String name,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        String sessionKey = (String) session.getAttribute("sessionKey");
        if (sessionKey == null) {
            sessionKey = UUID.randomUUID().toString();
            session.setAttribute("sessionKey", sessionKey);
        }

        ProductForm productForm = (ProductForm) session.getAttribute("add1" + sessionKey);
        if (productForm == null) {
            productForm = new ProductForm();
        }

        if (name == null || name.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("alert", "Tên không được để trống");
            redirectAttributes.addFlashAttribute("type", "error");

            redirectAttributes.addFlashAttribute("productForm", productForm);
            return "redirect:/admin/product/view-add2";
        }

        Color checkTonTai = colorService.findByName(name.trim());
        if (checkTonTai != null) {
            redirectAttributes.addFlashAttribute("alert", "Đã tồn tại màu sắc");
            redirectAttributes.addFlashAttribute("type", "error");

            redirectAttributes.addFlashAttribute("productForm", productForm);
            return "redirect:/admin/product/view-add2";
        }
        Color color = new Color();
        color.setCode(code);
        color.setName(name.trim());
        color.setStatus(true);
        colorService.addColor(color);

        redirectAttributes.addFlashAttribute("alert", "Thêm màu sắc thành công!");
        redirectAttributes.addFlashAttribute("type", "success");

        session.setAttribute("add1" + sessionKey, productForm);
        redirectAttributes.addFlashAttribute("productForm", productForm);
        return "redirect:/admin/product/view-add2";
    }

    @PostMapping("/add-nhanh/size")
    public String addNhanhSize(@RequestParam("sizeCode") String code,
                                @RequestParam("sizeName") String name,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        String sessionKey = (String) session.getAttribute("sessionKey");
        if (sessionKey == null) {
            sessionKey = UUID.randomUUID().toString();
            session.setAttribute("sessionKey", sessionKey);
        }

        ProductForm productForm = (ProductForm) session.getAttribute("add1" + sessionKey);
        if (productForm == null) {
            productForm = new ProductForm();
        }

        if (code == null || code.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("alert", "Tên không được để trống");
            redirectAttributes.addFlashAttribute("type", "error");

            redirectAttributes.addFlashAttribute("productForm", productForm);
            return "redirect:/admin/product/view-add2";
        }

        Size checkTonTai = sizeService.findByCode(code.trim());
        if (checkTonTai != null) {
            redirectAttributes.addFlashAttribute("alert", "Đã tồn tại kích thước");
            redirectAttributes.addFlashAttribute("type", "error");

            redirectAttributes.addFlashAttribute("productForm", productForm);
            return "redirect:/admin/product/view-add2";
        }
        Size size = new Size();
        size.setCode(code);
        size.setName(name.trim());
        size.setStatus(true);
        sizeService.addSize(size);

        redirectAttributes.addFlashAttribute("alert", "Thêm kích thước thành công!");
        redirectAttributes.addFlashAttribute("type", "success");

        session.setAttribute("add1" + sessionKey, productForm);
        redirectAttributes.addFlashAttribute("productForm", productForm);
        return "redirect:/admin/product/view-add2";
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

        model.addAttribute("idProduct", product.getId());
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

