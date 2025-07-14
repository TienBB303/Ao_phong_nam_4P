package com.example.datn.controllers;
import com.example.datn.dto.customer.CustomerDto;
import com.example.datn.entities.Customer;
import com.example.datn.services.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.datn.dto.AddressDto;
import com.example.datn.entities.ShippingAddress;


@Controller
@RequestMapping("/admin/customer")
public class CustomerController {
    @Autowired
    private CustomerService customerService;
    // Hiển thị danh sách khách hàng có tìm kiếm + phân trang
    @GetMapping("/view")
    public String viewCustomers(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(required = false) String keyword) {
        int pageSize = 5;
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Customer> customerPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            customerPage = customerService.searchCustomerEntity(keyword, pageable);
        } else {
            customerPage =customerService.getAllCustomersEntity(pageable);

        }

        model.addAttribute("customerPage", customerPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", customerPage.getTotalPages());
        model.addAttribute("keyword", keyword);

        return "admin/customer/customerList";
    }

    // Hiển thị form tạo khách hàng mới
    //Dùng DTO, có validate
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("customerDto", new CustomerDto());
        return "admin/customer/customerCreate1";
    }
    // Xử lý lưu khách hàng,Dùng DTO, validate, update theo id
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("customerDto") CustomerDto dto,
                       BindingResult result,
                       RedirectAttributes redirectAttributes) {
        System.out.println("DTO nhận được: " + dto);
        if (result.hasErrors()) {
            System.out.println("Lỗi validate: " + result.getAllErrors());
            return "admin/customer/customerCreate1";
        }

        // Kiểm tra email đã tồn tại
        if (customerService.isEmailExists(dto.getEmail())) {
            result.rejectValue("email", "error.customerDto", "Email đã tồn tại trong hệ thống.");
            return "admin/customer/customerCreate1";
        }

        customerService.createCustomerWithAddressAndAccount(dto);
        redirectAttributes.addFlashAttribute("message", "Lưu khách hàng thành công!");
        return "redirect:/admin/customer/view";
    }
    // Hiển thị form chỉnh sửa khách hàng
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable int id, Model model, RedirectAttributes redirect) {
        Customer customer = customerService.findById(id);
        if (customer == null) {
            redirect.addFlashAttribute("errorMessage", "Không tìm thấy khách hàng!");
            return "redirect:/admin/customer/view";
        }
        CustomerDto dto = convertToDto(customer);
        model.addAttribute("customerDto", dto);
        return "admin/customer/customerEdit";
    }
    //Dùng isActive
    @PostMapping("/update")
    public String updateCustomer(@Valid @ModelAttribute("customerDto") CustomerDto dto,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            // Nếu có lỗi validation, quay lại form chỉnh sửa
            return "admin/customer/customerEdit";
        }

        try {
            // KHÔNG CẦN convertToEntity ở đây vì service sẽ tự tìm và cập nhật entity
            Customer updated = customerService.updateCustomer(dto);
            if (updated == null) {
                result.reject("error", "Không tìm thấy khách hàng để cập nhật!");
                return "admin/customer/customerEdit";
            } else {
                redirectAttributes.addFlashAttribute("message", "Cập nhật khách hàng thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật khách hàng: " + e.getMessage());
            // In lỗi ra console để debug, rất quan trọng!
            e.printStackTrace();
        }

        return "redirect:/admin/customer/view";
    }
    @GetMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            customerService.softDeleteCustomer(id);
            redirect.addFlashAttribute("success", "Xóa khách hàng thành công (dạng ẩn)");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Không tìm thấy khách hàng.");
        }
        return "redirect:/admin/customer/view";
    }

    // Chi tiết khách hàng,Gọi theo id
    @GetMapping("/detail/{id}")
    public String detail(@PathVariable int id, Model model, RedirectAttributes redirect) {
        Customer customer = customerService.findById(id);
        if (customer == null) {
            redirect.addFlashAttribute("errorMessage", "Không tìm thấy khách hàng!");
            return "redirect:/admin/customer/view";
        }
        model.addAttribute("customer", customer);
        return "admin/customer/customerDetail";
    }
    // Hàm hỗ trợ convert từ entity -> dto
    private CustomerDto convertToDto(Customer customer) {
        CustomerDto dto = new CustomerDto();
        dto.setId(customer.getId());
        dto.setCode(customer.getCode());
        dto.setName(customer.getName());
        dto.setGender(customer.getGender());
        dto.setBirthday(customer.getBirthDate());
        dto.setPhoneNumber(customer.getPhoneNumber());
        dto.setIsActive(customer.getIsActive());
        if (customer.getAccount() != null) {
            dto.setEmail(customer.getAccount().getEmail());
        }
        if (customer.getAddresses() != null && !customer.getAddresses().isEmpty()) {
            ShippingAddress address = customer.getAddresses().get(0); // hoặc lấy địa chỉ mặc định
            AddressDto addressDto = new AddressDto();
            addressDto.setAddressDetail(address.getAddressDetail() != null ? address.getAddressDetail() : "");
            addressDto.setProvinceId(address.getProvinceId() != null ? address.getProvinceId() : 0);
            addressDto.setProvinceName(address.getProvinceName() != null ? address.getProvinceName() : "");
            addressDto.setDistrictId(address.getDistrictId() != null ? address.getDistrictId() : 0);
            addressDto.setDistrictName(address.getDistrictName() != null ? address.getDistrictName() : "");
            addressDto.setWardId(address.getWardId() != null ? address.getWardId() : "");
            addressDto.setWardName(address.getWardName() != null ? address.getWardName() : "");
            addressDto.setReceiverName(address.getReceiverName() != null ? address.getReceiverName() : "");
            addressDto.setReceiverPhoneNumber(address.getReceiverPhoneNumber() != null ? address.getReceiverPhoneNumber() : "");
            addressDto.setIsDefault(address.getIsDefault() != null ? address.getIsDefault() : false);
            dto.setAddress(addressDto);
        }
        return dto;
    }
    // Hàm hỗ trợ convert từ dto -> entity
    private Customer convertToEntity(CustomerDto dto) {
        Customer customer = new Customer();
        customer.setId(dto.getId());
        customer.setCode(dto.getCode());
        customer.setName(dto.getName());
        customer.setGender(dto.getGender());
        customer.setBirthDate(dto.getBirthday());
        customer.setPhoneNumber(dto.getPhoneNumber());
        customer.setIsActive(dto.getIsActive()); // <-- Lấy giá trị isActive từ DTO
        return customer;
    }
}
