package com.example.datn.controllers.user;

import com.example.datn.dto.AddressDto;
import com.example.datn.dto.customer.CustomerDto;
import com.example.datn.entities.Account;
import com.example.datn.entities.Customer;
import com.example.datn.services.AddressService;
import com.example.datn.services.CustomerService;
import com.example.datn.services.EmailService;
import com.example.datn.repositories.ShippingAddressRepository;
import jakarta.persistence.criteria.Order;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomerService customerService;
    @Autowired
    private ShippingAddressRepository shippingAddressRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AddressService addressService;

    @GetMapping("/home")
    public String userHome(Model model, HttpSession session) {
        Account account = (Account) session.getAttribute("userAccount");
        model.addAttribute("account", account);
        return "user/index";  // dùng index.html làm trang chính
    }

    // Hiển thị trang đăng ký
    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        CustomerDto customerDto = new CustomerDto();
        model.addAttribute("customerDto", customerDto);
        return "user/signup";
    }

    // Xử lý đăng ký
    @PostMapping("/signup")
    public String processSignup(@ModelAttribute CustomerDto customerDto,
                                RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra email đã tồn tại chưa
            if (customerService.existsByEmail(customerDto.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "Email đã được sử dụng. Vui lòng chọn email khác.");
                return "redirect:/user/signup";
            }

            // Tạo khách hàng mới với tài khoản
            Customer customer = customerService.createCustomerWithAddressAndAccount(customerDto);

            redirectAttributes.addFlashAttribute("success",
                    "Đăng ký thành công! Vui lòng kiểm tra email để lấy thông tin đăng nhập.");

            return "redirect:/user/login";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đăng ký thất bại: " + e.getMessage());
            return "redirect:/user/signup";
        }
    }

    // Hiển thị trang đăng nhập
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        return "user/login";
    }

    // Hiển thị trang đổi mật khẩu
    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model, HttpSession session) {
        Account account = (Account) session.getAttribute("userAccount");
        if (account == null) {
            return "redirect:/user/login";
        }
        return "user/change-password";
    }

    // Xử lý đổi mật khẩu
    @PostMapping("/change-password")
    public String processChangePassword(@RequestParam String currentPassword,
                                        @RequestParam String newPassword,
                                        @RequestParam String confirmPassword,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        try {
            Account account = (Account) session.getAttribute("userAccount");
            if (account == null) {
                return "redirect:/user/login";
            }

            // Kiểm tra mật khẩu hiện tại
            if (!customerService.verifyPassword(currentPassword, account.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng.");
                return "redirect:/user/change-password";
            }

            // Kiểm tra mật khẩu mới
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp.");
                return "redirect:/user/change-password";
            }

            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự.");
                return "redirect:/user/change-password";
            }

            // Cập nhật mật khẩu
            customerService.updatePassword(account.getId(), newPassword);

            // Cập nhật trạng thái tài khoản
            customerService.updateAccountStatus(account.getId(), true);

            redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công!");
            return "redirect:/";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đổi mật khẩu thất bại: " + e.getMessage());
            return "redirect:/user/change-password";
        }
    }

    // Hiển thị trang profile
    // Method: showProfile(Model model, HttpSession session)
    @GetMapping("/profile")
    @Transactional(readOnly = true)
    public String showProfile(Model model, HttpSession session) {
        Account account = (Account) session.getAttribute("userAccount");
        if (account == null) {
            return "redirect:/user/login";
        }
        // Lấy lại Customer với addresses được load sẵn từ database
        Integer customerId = account.getCustomer().getId();
        Customer customerEntity = customerService.findByIdWithAddresses(customerId);
        if (customerEntity == null) {
            return "redirect:/user/login";
        }
        model.addAttribute("account", account);
        model.addAttribute("customerEntity", customerEntity);

        // Sắp xếp địa chỉ: mặc định lên đầu
        if (customerEntity.getAddresses() != null && !customerEntity.getAddresses().isEmpty()) {
            customerEntity.getAddresses().sort(java.util.Comparator.comparing(
                    (com.example.datn.entities.ShippingAddress a) -> Boolean.TRUE.equals(a.getIsDefault())
            ).reversed());
        }
        model.addAttribute("account", account);
        CustomerDto customerDto = new CustomerDto();
        if (customerEntity != null) {
            customerDto.setId(customerEntity.getId());
            customerDto.setCode(customerEntity.getCode());
            customerDto.setName(customerEntity.getName());
            customerDto.setGender(customerEntity.getGender());
            customerDto.setBirthday(customerEntity.getBirthDate());
            customerDto.setPhoneNumber(customerEntity.getPhoneNumber());
            customerDto.setEmail(account.getEmail());
            customerDto.setIsActive(customerEntity.getIsActive());
            // Nếu có địa chỉ mặc định
            if (customerEntity.getAddresses() != null && !customerEntity.getAddresses().isEmpty()) {
                // // [Removed] Không chọn địa chỉ mặc định nữa
                // var defaultAddress = customerEntity.getAddresses().stream()
                //     .filter(a -> Boolean.TRUE.equals(a.getIsDefault()))
                //     .findFirst()
                //     .orElse(customerEntity.getAddresses().get(0));
//                var firstAddress = customerEntity.getAddresses().get(0); // lấy địa chỉ đầu tiên
                // Lấy địa chỉ mặc định nếu có, nếu không lấy địa chỉ đầu tiên
                var selectedAddress = customerEntity.getAddresses().stream()
                        .filter(a -> Boolean.TRUE.equals(a.getIsDefault()))
                        .findFirst()
                        .orElse(customerEntity.getAddresses().get(0));
                com.example.datn.dto.AddressDto addressDto = new com.example.datn.dto.AddressDto();
//                addressDto.setAddressDetail(firstAddress.getAddressDetail());
//                addressDto.setProvinceId(firstAddress.getProvinceId());
//                addressDto.setProvinceName(firstAddress.getProvinceName());
//                addressDto.setDistrictId(firstAddress.getDistrictId());
//                addressDto.setDistrictName(firstAddress.getDistrictName());
//                addressDto.setWardId(firstAddress.getWardId());
//                addressDto.setWardName(firstAddress.getWardName());
                addressDto.setAddressDetail(selectedAddress.getAddressDetail());
                addressDto.setProvinceId(selectedAddress.getProvinceId());
                addressDto.setProvinceName(selectedAddress.getProvinceName());
                addressDto.setDistrictId(selectedAddress.getDistrictId());
                addressDto.setDistrictName(selectedAddress.getDistrictName());
                addressDto.setWardId(selectedAddress.getWardId());
                addressDto.setWardName(selectedAddress.getWardName());
                customerDto.setAddress(addressDto);
            }
        }
        model.addAttribute("customer", customerDto);
        model.addAttribute("addresses", customerEntity.getAddresses());
        return "user/profile";
    }

    // Hiển thị trang đổi mật khẩu (cho user đã login)
    @GetMapping("/change-password-request")
    public String showChangePasswordRequestPage(HttpSession session, RedirectAttributes redirectAttributes) {
        Account account = (Account) session.getAttribute("userAccount");
        if (account == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập trước khi đổi mật khẩu.");
            return "redirect:/user/login";
        }
        return "user/change-password-request";

    }

    @PostMapping("/change-password-request")
    public String handleChangePasswordRequest(@RequestParam String oldPassword,
                                              @RequestParam String newPassword,
                                              @RequestParam String confirmPassword,
                                              HttpSession session,
                                              RedirectAttributes redirectAttributes) {
        try {
            Account account = (Account) session.getAttribute("userAccount");
            if (account == null) {
                redirectAttributes.addFlashAttribute("error", "Phiên đăng nhập đã hết hạn.");
                return "redirect:/user/login";
            }

//            // Kiểm tra mật khẩu xác nhận
//            if (!newPassword.equals(confirmPassword)) {
//                redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
//                return "redirect:/user/change-password-request";
//            }

            // Kiểm tra độ dài mật khẩu mới
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự.");
                return "redirect:/user/change-password-request";
            }

            // Kiểm tra mật khẩu mới không trùng mật khẩu cũ
            if (oldPassword.equals(newPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu mới phải khác mật khẩu hiện tại!");
                return "redirect:/user/change-password-request";
            }

            // Kiểm tra mật khẩu xác nhận không được trùng mật khẩu cũ
            if (confirmPassword.equals(oldPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không được trùng với mật khẩu hiện tại!");
                return "redirect:/user/change-password-request";
            }

            // Kiểm tra mật khẩu xác nhận khớp với mật khẩu mới
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
                return "redirect:/user/change-password-request";
            }
            // Kiểm tra mật khẩu cũ
            if (!customerService.verifyPassword(oldPassword, account.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng!");
                return "redirect:/user/change-password-request";
            }

            // Cập nhật mật khẩu mới
            customerService.updatePassword(account.getId(), newPassword);

            // Cập nhật lại Account trong session với thông tin mới
            Account updatedAccount = customerService.findAccountById(account.getId());
            if (updatedAccount != null) {
                session.setAttribute("userAccount", updatedAccount);
            }

            redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công!");
            return "redirect:/user/profile";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đổi mật khẩu thất bại: " + e.getMessage());
            return "redirect:/user/change-password-request";
        }
    }

    @GetMapping("/orders")
    public String showOrders(Model model, HttpSession session) {
        Account account = (Account) session.getAttribute("userAccount");
        if (account == null) {
            return "redirect:/user/login";
        }
        // TODO: Implement order listing logic
        model.addAttribute("customer", account.getCustomer());
        return "user/orders";
    }

    // Đăng xuất
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Đăng xuất thành công!");
        return "redirect:/";
    }

    // Xử lý cập nhật thông tin cá nhân
    @PostMapping("/profile/edit")
    public String editProfile(@ModelAttribute CustomerDto customerDto,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Account account = (Account) session.getAttribute("userAccount");
        if (account == null) {
            return "redirect:/user/login";
        }
        try {
            // Cập nhật thông tin cá nhân, địa chỉ mặc định
            customerService.updateCustomerProfile(account.getCustomer().getId(), customerDto);
            // Cập nhật lại thông tin trong session
            Customer updatedCustomer = customerService.findById(account.getCustomer().getId());
            account.setCustomer(updatedCustomer);
            session.setAttribute("userCustomer", updatedCustomer);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cập nhật thất bại: " + e.getMessage());
        }
        return "redirect:/user/profile";
    }
    // API: Thêm địa chỉ mới (online)
//    @PostMapping("/addresses")
//    @ResponseBody
//    public java.util.Map<String, Object> addAddress(@RequestBody com.example.datn.dto.AddressDto addressDto,
//                                                    HttpSession session){
//        java.util.Map<String, Object> resp = new java.util.HashMap<>();
//        Account account = (Account) session.getAttribute("userAccount");
//        if (account == null) {
//            resp.put("success", false);
//            resp.put("message", "Bạn cần đăng nhập");
//            return resp;
//        }
//        try {
//            com.example.datn.entities.ShippingAddress saved = customerService.createAddressForCustomer(account.getCustomer().getId(), addressDto);
//            resp.put("success", true);
//            resp.put("id", saved.getId());
//        } catch (Exception e){
//            resp.put("success", false);
//            resp.put("message", e.getMessage());
//        }
//        return resp;
//    }

    @PostMapping("/addresses")
    @ResponseBody
    public Map<String, Object> addAddress(
            @Valid @RequestBody AddressDto addressDto,
            Principal principal) {
        Map<String, Object> resp = new HashMap<>();
        try {
            addressService.save(addressDto, principal);
            resp.put("success", true);
            resp.put("message", "Thêm địa chỉ thành công");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", e.getMessage());
        }
        return resp;
    }
    // API: Đặt địa chỉ mặc định
    @PostMapping("/addresses/{id}/default")
    @ResponseBody
    public java.util.Map<String, Object> setDefault(@PathVariable Integer id, HttpSession session) {
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        Account account = (Account) session.getAttribute("userAccount");
        if (account == null) {
            resp.put("success", false);
            resp.put("message", "Bạn cần đăng nhập");
            return resp;
        }
        try {
            // Xác thực quyền sở hữu địa chỉ
            java.util.Optional<com.example.datn.entities.ShippingAddress> opt = shippingAddressRepository.findById(id);
            if (opt.isEmpty() || !opt.get().getCustomer().getId().equals(account.getCustomer().getId())) {
                resp.put("success", false);
                resp.put("message", "Địa chỉ không hợp lệ");
                return resp;
            }
            // Set tất cả về false rồi set địa chỉ này true
            shippingAddressRepository.updateAllDefaultFalseByCustomerId(account.getCustomer().getId());
            com.example.datn.entities.ShippingAddress address = opt.get();
            address.setIsDefault(true);
            shippingAddressRepository.save(address);
            resp.put("success", true);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", e.getMessage());
        }
        return resp;
    }
    // API: Lấy chi tiết địa chỉ (phục vụ modal sửa)
    @GetMapping("/addresses/{id}")
    @ResponseBody
    public Map<String, Object> getAddress(@PathVariable Integer id, HttpSession session) {
        Map<String, Object> resp = new HashMap<>();
        Account account = (Account) session.getAttribute("userAccount");
        if (account == null) {
            resp.put("success", false);
            resp.put("message", "Bạn cần đăng nhập");
            return resp;
        }
        try {
            var opt = shippingAddressRepository.findById(id);
            if (opt.isEmpty() || !opt.get().getCustomer().getId().equals(account.getCustomer().getId())) {
                resp.put("success", false);
                resp.put("message", "Địa chỉ không hợp lệ");
                return resp;
            }
            var a = opt.get();
            Map<String, Object> data = new HashMap<>();
            data.put("id", a.getId());
            data.put("receiverName", a.getReceiverName());
            data.put("receiverPhoneNumber", a.getReceiverPhoneNumber());
            data.put("addressDetail", a.getAddressDetail());
            data.put("provinceId", a.getProvinceId());
            data.put("provinceName", a.getProvinceName());
            data.put("districtId", a.getDistrictId());
            data.put("districtName", a.getDistrictName());
            data.put("wardId", a.getWardId());
            data.put("wardName", a.getWardName());
            data.put("isDefault", a.getIsDefault());
            resp.put("success", true);
            resp.put("data", data);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", e.getMessage());
        }
        return resp;
    }

    // API: Cập nhật địa chỉ
    @PutMapping("/addresses/{id}")
    @ResponseBody
    public Map<String, Object> updateAddress(@PathVariable Integer id,
                                             @Valid @RequestBody AddressDto addressDto,
                                             HttpSession session) {
        Map<String, Object> resp = new HashMap<>();
        Account account = (Account) session.getAttribute("userAccount");
        if (account == null) {
            resp.put("success", false);
            resp.put("message", "Bạn cần đăng nhập");
            return resp;
        }
        try {
            var opt = shippingAddressRepository.findById(id);
            if (opt.isEmpty() || !opt.get().getCustomer().getId().equals(account.getCustomer().getId())) {
                resp.put("success", false);
                resp.put("message", "Địa chỉ không hợp lệ");
                return resp;
            }
            var address = opt.get();

            // Nếu set là mặc định, clear các mặc định khác trước
            if (Boolean.TRUE.equals(addressDto.getIsDefault())) {
                shippingAddressRepository.updateAllDefaultFalseByCustomerId(account.getCustomer().getId());
                address.setIsDefault(true);
            } else {
                address.setIsDefault(Boolean.TRUE.equals(address.getIsDefault()) && Boolean.FALSE.equals(addressDto.getIsDefault()) ? false : address.getIsDefault());
                // Nếu không gửi hoặc gửi false, giữ nguyên mặc định hiện tại trừ khi explicit false
                if (addressDto.getIsDefault() != null) {
                    address.setIsDefault(addressDto.getIsDefault());
                }
            }

            address.setReceiverName(addressDto.getReceiverName());
            address.setReceiverPhoneNumber(addressDto.getReceiverPhoneNumber());
            address.setAddressDetail(addressDto.getAddressDetail());
            address.setProvinceId(addressDto.getProvinceId());
            address.setProvinceName(addressDto.getProvinceName());
            address.setDistrictId(addressDto.getDistrictId());
            address.setDistrictName(addressDto.getDistrictName());
            address.setWardId(addressDto.getWardId());
            address.setWardName(addressDto.getWardName());

            shippingAddressRepository.save(address);
            resp.put("success", true);
            resp.put("message", "Cập nhật địa chỉ thành công");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", e.getMessage());
        }
        return resp;
    }

    // API: Xoá địa chỉ
    @DeleteMapping("/addresses/{id}")
    @ResponseBody
    public java.util.Map<String, Object> deleteAddress(@PathVariable Integer id, HttpSession session) {
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        Account account = (Account) session.getAttribute("userAccount");
        if (account == null) {
            resp.put("success", false);
            resp.put("message", "Bạn cần đăng nhập");
            return resp;
        }
        try {
            var opt = shippingAddressRepository.findById(id);
            if (opt.isEmpty() || !opt.get().getCustomer().getId().equals(account.getCustomer().getId())) {
                resp.put("success", false);
                resp.put("message", "Địa chỉ không hợp lệ");
                return resp;
            }
            boolean wasDefault = Boolean.TRUE.equals(opt.get().getIsDefault());
            shippingAddressRepository.deleteById(id);

            // Nếu xoá địa chỉ mặc định, set địa chỉ đầu tiên (nếu còn) làm mặc định
            var remaining = shippingAddressRepository.findByCustomerId(account.getCustomer().getId());
            if (wasDefault && remaining != null && !remaining.isEmpty()) {
                shippingAddressRepository.updateAllDefaultFalseByCustomerId(account.getCustomer().getId());
                var first = remaining.get(0);
                first.setIsDefault(true);
                shippingAddressRepository.save(first);
            }

            resp.put("success", true);
            resp.put("message", "Xoá địa chỉ thành công");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", e.getMessage());
        }
        return resp;
    }
}