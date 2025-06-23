package com.example.datn.controllers.product_and_other;

import com.example.datn.entities.product_and_other.Size;
import com.example.datn.services.product_and_other.SizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/size")
public class SizeController {
    @Autowired
    SizeService sizeService;

    @GetMapping("/hien-thi")
    public String size(
            @RequestParam(value = "sizeNameSearch", defaultValue = "") String name,
            @RequestParam(value = "sizeStatusSearch", defaultValue = "") Boolean status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {
        if (page < 0) {
            page = 0;
        }
        Page<Size> listSize = sizeService.searchPage(name, status, PageRequest.of(page, size));
        model.addAttribute("sizeNameSearch", name);
        model.addAttribute("sizeStatusSearch", status != null ? status : "");
        model.addAttribute("listSize", listSize);
        model.addAttribute("currentPage", page);
        model.addAttribute("size", size);
        return "admin/product_and_other/other/SizeView";
    }

    @PostMapping("/add")
    public String add(@RequestParam("sizeCode") String sizeCode,
                      @RequestParam("sizeName") String sizeName,
                      RedirectAttributes redirectAttributes){
        if (sizeCode == null || sizeCode.trim().isEmpty()){
            redirectAttributes.addFlashAttribute("alert", "Mã không được để trống");
            redirectAttributes.addFlashAttribute("type", "error");
            return "redirect:/admin/size/hien-thi";
        }
        Size checkTonTai = sizeService.findByCode(sizeCode);
        if(checkTonTai != null){
            redirectAttributes.addFlashAttribute("alert", "Đã tồn tại mã kích thước");
            redirectAttributes.addFlashAttribute("type", "error");
            return "redirect:/admin/size/hien-thi";
        } else {
            sizeService.addSize(sizeCode, sizeName);
            redirectAttributes.addFlashAttribute("alert", "Thêm kích thước thành công!");
            redirectAttributes.addFlashAttribute("type", "success");
            return "redirect:/admin/size/hien-thi";
        }
    }

    @GetMapping("/detail/{id}")
    @ResponseBody
    public Size detailSize(@PathVariable("id") Integer id){
        return sizeService.detail(id);
    }

    @PostMapping("/update")
    public String updateSize(
            @RequestParam("sizeId") Integer id,
            @RequestParam("sizeCodeUpdate") String code,
            @RequestParam("sizeNameUpdate") String name,
            RedirectAttributes redirectAttributes){
        if(code == null || code.trim().isEmpty()){
            redirectAttributes.addFlashAttribute("alert", "Mã không được để trống");
            redirectAttributes.addFlashAttribute("type","error");
            return "redirect:/admin/size/hien-thi";
        }
        Size checkTonTai = sizeService.findByCode(code);
        if(checkTonTai != null && !checkTonTai.getId().equals(id)){
            redirectAttributes.addFlashAttribute("alert", "Đã tồn tại kích thước");
            redirectAttributes.addFlashAttribute("type", "error");
            return "redirect:/admin/size/hien-thi";
        } else {
            sizeService.update(id,code, name);
            redirectAttributes.addFlashAttribute("alert", "Cập nhật kích thước thành công!");
            redirectAttributes.addFlashAttribute("type", "success");
            return "redirect:/admin/size/hien-thi";
        }
    }

    @GetMapping("/change-status/{id}")
    public String changeStatus(
            @PathVariable("id") Integer id, RedirectAttributes redirectAttributes){
        Size size = sizeService.findById(id);
        if (size.getStatus()){
            long countTrue = sizeService.getAll().stream()
                    .filter(c -> c.getStatus() && !c.getId().equals(id))
                    .count();

            if (countTrue == 0) {
                redirectAttributes.addFlashAttribute("alert", "Không được tắt hết hoạt động!");
                redirectAttributes.addFlashAttribute("type", "error");
                return "redirect:/admin/size/hien-thi";
            }
        }
        sizeService.changeStatus(id);
        redirectAttributes.addFlashAttribute("alert", "Thay đổi trạng thái thành công!");
        redirectAttributes.addFlashAttribute("type", "success");
        return "redirect:/admin/size/hien-thi";
    }

}
