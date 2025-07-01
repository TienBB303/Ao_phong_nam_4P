package com.example.datn.controllers.product_and_other;

import com.example.datn.entities.product_and_other.Brand;
import com.example.datn.services.product_and_other.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/brand")
public class BrandController {
    @Autowired
    BrandService brandService;

    @GetMapping("/hien-thi")
    public String brand(
            @RequestParam(value = "brandNameSearch", defaultValue = "") String name,
            @RequestParam(value = "brandStatusSearch", defaultValue = "") Boolean status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int brand,
            Model model) {
        if (page < 0) {
            page = 0;
        }
        Page<Brand> listBrand = brandService.searchPage(name.trim(), status, PageRequest.of(page, brand));
        model.addAttribute("brandNameSearch", name.trim());
        model.addAttribute("brandStatusSearch", status != null ? status : "");
        model.addAttribute("listBrand", listBrand);
        model.addAttribute("currentPage", page);
        model.addAttribute("brand", brand);
        return "admin/product_and_other/other/BrandView";
    }

    @PostMapping("/add")
    public String add(
//            @RequestParam("brandCode") String brandCode,
                      @RequestParam("brandName") String brandName,
                      RedirectAttributes redirectAttributes){
        if (brandName == null || brandName.trim().isEmpty()){
            redirectAttributes.addFlashAttribute("alert", "Tên hãng không được để trống");
            redirectAttributes.addFlashAttribute("type", "error");
            return "redirect:/admin/brand/hien-thi";
        }
        Brand checkTonTai = brandService.findByName(brandName.trim());
        if(checkTonTai != null){
            redirectAttributes.addFlashAttribute("alert", "Đã tồn tại hãng");
            redirectAttributes.addFlashAttribute("type", "error");
            return "redirect:/admin/brand/hien-thi";
        } else {
            String brandCode = brandService.taoMaTuDongBrand();
            brandService.addBrand(brandCode.trim(), brandName.trim());
            redirectAttributes.addFlashAttribute("alert", "Thêm hãng mới thành công!");
            redirectAttributes.addFlashAttribute("type", "success");
            return "redirect:/admin/brand/hien-thi";
        }
    }

    @GetMapping("/detail/{id}")
    @ResponseBody
    public Brand detailBrand(@PathVariable("id") Integer id){
        return brandService.detail(id);
    }

    @PostMapping("/update")
    public String updateBrand(
            @RequestParam("brandId") Integer id,
//            @RequestParam("brandCodeUpdate") String code,
            @RequestParam("brandNameUpdate") String name,
            RedirectAttributes redirectAttributes){
        if(name == null || name.trim().isEmpty()){
            redirectAttributes.addFlashAttribute("alert", "Mã không được để trống");
            redirectAttributes.addFlashAttribute("type","error");
            return "redirect:/admin/brand/hien-thi";
        }
        Brand checkTonTai = brandService.findByName(name.trim());
        if(checkTonTai != null && !checkTonTai.getId().equals(id)){
            redirectAttributes.addFlashAttribute("alert", "Đã tồn tại hãng");
            redirectAttributes.addFlashAttribute("type", "error");
            return "redirect:/admin/brand/hien-thi";
        } else {
            brandService.update(id, name.trim());
            redirectAttributes.addFlashAttribute("alert", "Cập nhật hãng thành công!");
            redirectAttributes.addFlashAttribute("type", "success");
            return "redirect:/admin/brand/hien-thi";
        }
    }

    @GetMapping("/change-status/{id}")
    public String changeStatus(
            @PathVariable("id") Integer id, RedirectAttributes redirectAttributes){
        Brand brand = brandService.findById(id);
        if (brand.getStatus()){
            long countTrue = brandService.getAll().stream()
                    .filter(c -> c.getStatus() && !c.getId().equals(id))
                    .count();

            if (countTrue == 0) {
                redirectAttributes.addFlashAttribute("alert", "Không được tắt hết hoạt động!");
                redirectAttributes.addFlashAttribute("type", "error");
                return "redirect:/admin/brand/hien-thi";
            }
        }
        brandService.changeStatus(id);
        redirectAttributes.addFlashAttribute("alert", "Thay đổi trạng thái thành công!");
        redirectAttributes.addFlashAttribute("type", "success");
        return "redirect:/admin/brand/hien-thi";
    }
}
