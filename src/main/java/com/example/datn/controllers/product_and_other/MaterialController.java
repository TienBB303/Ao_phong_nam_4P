package com.example.datn.controllers.product_and_other;

import com.example.datn.entities.product_and_other.Material;
import com.example.datn.services.product_and_other.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/material")
public class MaterialController {
    @Autowired
    MaterialService materialService;

    @GetMapping("/hien-thi")
    public String material(
            @RequestParam(value = "materialNameSearch", defaultValue = "") String name,
            @RequestParam(value = "materialStatusSearch", defaultValue = "") Boolean status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int material,
            Model model) {
        if (page < 0) {
            page = 0;
        }
        Page<Material> listMaterial = materialService.searchPage(name, status, PageRequest.of(page, material));
        model.addAttribute("materialNameSearch", name);
        model.addAttribute("materialStatusSearch", status != null ? status : "");
        model.addAttribute("listMaterial", listMaterial);
        model.addAttribute("currentPage", page);
        model.addAttribute("material", material);
        return "admin/product_and_other/other/MaterialView";
    }

    @PostMapping("/add")
    public String add(@RequestParam("materialCode") String materialCode,
                      @RequestParam("materialName") String materialName,
                      RedirectAttributes redirectAttributes){
        if (materialCode == null || materialCode.trim().isEmpty()){
            redirectAttributes.addFlashAttribute("alert", "Mã không được để trống");
            redirectAttributes.addFlashAttribute("type", "error");
            return "redirect:/admin/material/hien-thi";
        }
        Material checkTonTai = materialService.findByCode(materialCode);
        if(checkTonTai != null){
            redirectAttributes.addFlashAttribute("alert", "Đã tồn tại mã chất liệu");
            redirectAttributes.addFlashAttribute("type", "error");
            return "redirect:/admin/material/hien-thi";
        } else {
            materialService.addMaterial(materialCode, materialName);
            redirectAttributes.addFlashAttribute("alert", "Thêm chất liệu thành công!");
            redirectAttributes.addFlashAttribute("type", "success");
            return "redirect:/admin/material/hien-thi";
        }
    }

    @GetMapping("/detail/{id}")
    @ResponseBody
    public Material detailMaterial(@PathVariable("id") Integer id){
        return materialService.detail(id);
    }

    @PostMapping("/update")
    public String updateMaterial(
            @RequestParam("materialId") Integer id,
            @RequestParam("materialCodeUpdate") String code,
            @RequestParam("materialNameUpdate") String name,
            RedirectAttributes redirectAttributes){
        if(code == null || code.trim().isEmpty()){
            redirectAttributes.addFlashAttribute("alert", "Mã không được để trống");
            redirectAttributes.addFlashAttribute("type","error");
            return "redirect:/admin/material/hien-thi";
        }
        Material checkTonTai = materialService.findByCode(code);
        if(checkTonTai != null && !checkTonTai.getId().equals(id)){
            redirectAttributes.addFlashAttribute("alert", "Đã tồn tại chất liệu");
            redirectAttributes.addFlashAttribute("type", "error");
            return "redirect:/admin/material/hien-thi";
        } else {
            materialService.update(id,code, name);
            redirectAttributes.addFlashAttribute("alert", "Cập nhật chất liệu thành công!");
            redirectAttributes.addFlashAttribute("type", "success");
            return "redirect:/admin/material/hien-thi";
        }
    }

    @GetMapping("/change-status/{id}")
    public String changeStatus(
            @PathVariable("id") Integer id, RedirectAttributes redirectAttributes){
        Material material = materialService.findById(id);
        if (material.getStatus()){
            long countTrue = materialService.getAll().stream()
                    .filter(c -> c.getStatus() && !c.getId().equals(id))
                    .count();

            if (countTrue == 0) {
                redirectAttributes.addFlashAttribute("alert", "Không được tắt hết hoạt động!");
                redirectAttributes.addFlashAttribute("type", "error");
                return "redirect:/admin/material/hien-thi";
            }
        }
        materialService.changeStatus(id);
        redirectAttributes.addFlashAttribute("alert", "Thay đổi trạng thái thành công!");
        redirectAttributes.addFlashAttribute("type", "success");
        return "redirect:/admin/material/hien-thi";
    }
}
