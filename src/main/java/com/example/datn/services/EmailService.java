package com.example.datn.services;

/**
 * Service gửi email cho khách hàng khi tạo tài khoản mới.
 */
public interface EmailService {
    /**
     * Gửi email thông báo tạo tài khoản mới cho khách hàng.
     * @param toEmail        Email người nhận (khách hàng)
     * @param customerName   Tên khách hàng
     * @param accountEmail   Email đăng nhập tài khoản (có thể trùng toEmail)
     * @param password       Mật khẩu tài khoản (dạng plain text)
     */
    void sendAccountCreatedMail(String toEmail, String customerName, String accountEmail, String password);
    /**
            * Gửi email thông báo tạo tài khoản mới với link đổi mật khẩu.
     * @param toEmail        Email người nhận (khách hàng)
     * @param customerName   Tên khách hàng
     * @param accountEmail   Email đăng nhập tài khoản
     * @param password       Mật khẩu tài khoản (dạng plain text)
     * @param resetToken     Token để đổi mật khẩu (nullable)
     */
    void sendAccountCreatedMail(String toEmail, String customerName, String accountEmail, String password, String resetToken);
/**
 * Gửi email với link đổi mật khẩu cho khách hàng.
 * TODO: Sẽ được implement sau
 * @param toEmail        Email người nhận
 * @param customerName   Tên khách hàng
 * @param resetToken     Token để đổi mật khẩu
 */
void sendPasswordResetMail(String toEmail, String customerName, String resetToken);
}
