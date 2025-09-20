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
        CustomerDto customerDto = new CustomerDto();
        customerDto.setIsActive(true); // Mặc định là hoạt động
        model.addAttribute("customerDto", customerDto);
        return "admin/customer/customerCreate1";
    }

    // Xử lý lưu khách hàng,Dùng DTO, validate, update theo id
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("customerDto") CustomerDto dto,
                       BindingResult result,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/customer/customerCreate1";
        }

        if (customerService.isEmailExists(dto.getEmail())) {
            result.rejectValue("email", "error.customerDto", "Email đã tồn tại trong hệ thống.");
            return "admin/customer/customerCreate1";
        }

        try {
            customerService.createCustomerWithAddressAndAccount(dto);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Số điện thoại")) {
                result.rejectValue("phoneNumber", "error.customerDto", e.getMessage());
                return "admin/customer/customerCreate1";
            }
            result.reject("error", e.getMessage());
            return "admin/customer/customerCreate1";
        }

        // Tính trang cuối cùng
        int pageSize = 5; // giống như trong viewCustomers
        long totalCustomers = customerService.countAllCustomers(); // cần thêm hàm này
        int lastPage = (int) ((totalCustomers - 1) / pageSize);

        redirectAttributes.addFlashAttribute("message", "Lưu khách hàng thành công!");
        return "redirect:/admin/customer/view?page=" + lastPage;
    }
    // Hiển thị form chỉnh sửa khách hàng
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable int id, Model model, RedirectAttributes redirect) {
        // Customer customer = customerService.findById(id);
        Customer customer = customerService.findByIdWithAddressesAndAccount(id);
        if (customer == null) {
            redirect.addFlashAttribute("errorMessage", "Không tìm thấy khách hàng!");
            return "redirect:/admin/customer/view";
        }
        CustomerDto dto = convertToDto(customer);
        System.out.println("Birthday in entity: " + customer.getBirthDate());
        System.out.println("Birthday in DTO: " + dto.getBirthday());
        model.addAttribute("customerDto", dto);
        return "admin/customer/customerEdit";
    }
    //Dùng isActive
    @PostMapping("/update")
    public String updateCustomer(@Valid @ModelAttribute("customerDto") CustomerDto dto,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes) {
        System.out.println("[DEBUG] UpdateCustomer incoming DTO: "
                + "id=" + dto.getId()
                + ", gender=" + dto.getGender()
                + ", birthday=" + dto.getBirthday()
                + ", name=" + dto.getName()
                + ", email=" + dto.getEmail()
                + ", phone=" + dto.getPhoneNumber()
                + ", isActive=" + dto.getIsActive());
        if (result.hasErrors()) {
            System.out.println("[DEBUG] BindingResult has errors:");
            result.getAllErrors().forEach(err -> System.out.println("  - " + err));
            // Nếu có lỗi validation, quay lại form chỉnh sửa
            return "admin/customer/customerEdit";
        }
        try {
            // KHÔNG CẦN convertToEntity ở đây vì service sẽ tự tìm và cập nhật entity
            Customer updated = customerService.updateCustomer(dto);
            if (updated == null) {
                System.out.println("[DEBUG] Update failed: customerService.updateCustomer(dto) returned null");
                result.reject("error", "Không tìm thấy khách hàng để cập nhật!");
                return "admin/customer/customerEdit";
            } else {
                System.out.println("[DEBUG] Update success for customer id=" + updated.getId());
                redirectAttributes.addFlashAttribute("message", "Cập nhật khách hàng thành công!");
            }
        } catch (Exception e) {
            System.out.println("[DEBUG] Exception in updateCustomer: " + e.getMessage());
            e.printStackTrace();
//            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật khách hàng: " + e.getMessage());
            String msg = e.getMessage() != null ? e.getMessage() : "Đã xảy ra lỗi";
            if (msg.contains("Email đã tồn tại")) {
                result.rejectValue("email", "error.customerDto", "Email đã tồn tại.");
            } else {
                result.reject("error", msg);
            }
            return "admin/customer/customerEdit";
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
    @GetMapping("/restore/{id}")
    public String restoreCustomer(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            customerService.restoreCustomer(id);
            redirect.addFlashAttribute("success", "Khôi phục khách hàng thành công");
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
    // Method: convertToDto(Customer customer)
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
            var address = customer.getAddresses().get(0);
            com.example.datn.dto.AddressDto addressDto = new com.example.datn.dto.AddressDto();
            addressDto.setAddressDetail(address.getAddressDetail() != null ? address.getAddressDetail() : "");
            addressDto.setProvinceId(address.getProvinceId());
            addressDto.setProvinceName(address.getProvinceName() != null ? address.getProvinceName() : "");
            addressDto.setDistrictId(address.getDistrictId());
            addressDto.setDistrictName(address.getDistrictName() != null ? address.getDistrictName() : "");
            addressDto.setWardId(address.getWardId());
            addressDto.setWardName(address.getWardName() != null ? address.getWardName() : "");
            // addressDto.setReceiverName(address.getReceiverName() != null ? address.getReceiverName() : ""); // [Removed]
            // addressDto.setReceiverPhoneNumber(address.getReceiverPhoneNumber() != null ? address.getReceiverPhoneNumber() : ""); // [Removed]
            // addressDto.setIsDefault(address.getIsDefault() != null ? address.getIsDefault() : false); // [Removed]

            // // [Removed] Debug log liên quan isDefault
            // System.out.println("Address isDefault: " + address.getIsDefault());
            // System.out.println("AddressDto isDefault: " + addressDto.getIsDefault());

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