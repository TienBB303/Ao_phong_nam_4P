package com.example.datn.controllers.product_and_other;

import com.example.datn.entities.product_and_other.Color;
import com.example.datn.services.product_and_other.ColorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/color")
public class ColorController {
    @Autowired
    ColorService colorService;

    @GetMapping("/hien-thi")
    public String color(
            @RequestParam(value = "colorNameSearch", defaultValue = "") String name,
            @RequestParam(value = "colorStatusSearch", defaultValue = "") Boolean status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int color,
            Model model) {
        if (page < 0) {
            page = 0;
        }
        Page<Color> listColor = colorService.searchPage(name.trim(), status, PageRequest.of(page, color));
        model.addAttribute("colorNameSearch", name.trim());
        model.addAttribute("colorStatusSearch", status != null ? status : "");
        model.addAttribute("listColor", listColor);
        model.addAttribute("currentPage", page);
        model.addAttribute("color", color);
        return "admin/product_and_other/other/ColorView";
    }

    @PostMapping("/add")
    public String add(
                      @RequestParam("colorCode") String colorCode,
                      @RequestParam("colorName") String colorName,
                      RedirectAttributes redirectAttributes){
        if (colorName == null || colorName.trim().isEmpty()){
            redirectAttributes.addFlashAttribute("alert", "Tên không được để trống");
            redirectAttributes.addFlashAttribute("type", "error");
            return "redirect:/admin/color/hien-thi";
        }
        Color checkTonTai = colorService.findByName(colorName.trim());
        if(checkTonTai != null){
            redirectAttributes.addFlashAttribute("alert", "Đã tồn tại màu sắc");
            redirectAttributes.addFlashAttribute("type", "error");
            return "redirect:/admin/color/hien-thi";
        } else {
//            String colorCode = colorService.taoMaTuDongColor();

            colorService.addColor(colorCode.trim(), colorName.trim());
            redirectAttributes.addFlashAttribute("alert", "Thêm màu sắc thành công!");
            redirectAttributes.addFlashAttribute("type", "success");
            return "redirect:/admin/color/hien-thi";
        }
    }

    @GetMapping("/detail/{id}")
    @ResponseBody
    public Color detailColor(@PathVariable("id") Integer id){
        return colorService.detail(id);
    }

    @PostMapping("/update")
    public String updateColor(
            @RequestParam("colorId") Integer id,
//            @RequestParam("colorCodeUpdate") String code,
            @RequestParam("colorNameUpdate") String name,
            RedirectAttributes redirectAttributes){
        if (name == null || name.trim().isEmpty()){
            redirectAttributes.addFlashAttribute("alert", "Tên không được để trống");
            redirectAttributes.addFlashAttribute("type", "error");
            return "redirect:/admin/color/hien-thi";
        }
        Color checkTonTai = colorService.findByName(name.trim());
        if(checkTonTai != null && !checkTonTai.getId().equals(id)){
            redirectAttributes.addFlashAttribute("alert", "Đã tồn tại màu sắc");
            redirectAttributes.addFlashAttribute("type", "error");
            return "redirect:/admin/color/hien-thi";
        } else {
            colorService.update(id, name.trim());
            redirectAttributes.addFlashAttribute("alert", "Cập nhật màu sắc thành công!");
            redirectAttributes.addFlashAttribute("type", "success");
            return "redirect:/admin/color/hien-thi";
        }
    }

    @GetMapping("/change-status/{id}")
    public String changeStatus(
            @PathVariable("id") Integer id, RedirectAttributes redirectAttributes){
        Color color = colorService.findById(id);
        if (color.getStatus()){
            long countTrue = colorService.getAll().stream()
                    .filter(c -> c.getStatus() && !c.getId().equals(id))
                    .count();

            if (countTrue == 0) {
                redirectAttributes.addFlashAttribute("alert", "Không được tắt hết hoạt động!");
                redirectAttributes.addFlashAttribute("type", "error");
                return "redirect:/admin/color/hien-thi";
            }
        }
        colorService.changeStatus(id);
        redirectAttributes.addFlashAttribute("alert", "Thay đổi trạng thái thành công!");
        redirectAttributes.addFlashAttribute("type", "success");
        return "redirect:/admin/color/hien-thi";
    }
}
