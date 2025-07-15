package com.example.datn.services.serviceImpl;
import com.example.datn.services.EmailService;
import jakarta.mail.MessagingException;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailServiceImpl implements EmailService{
    
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Override
    public void sendAccountCreatedMail(String toEmail, String name, String username, String password) {
        sendAccountCreatedMail(toEmail, name, username, password, null);
    }
    
    @Override
    public void sendAccountCreatedMail(String toEmail, String name, String username, String password, String resetToken) {
        try {
            logger.info("Bắt đầu gửi email đến: {}", toEmail);
            logger.info("Sử dụng mail sender: {}", mailSender.getClass().getSimpleName());
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("🎉 Chào mừng đến với 4PStore - Thông tin tài khoản");

            String content = buildEmailContent(name, username, password, resetToken);

            helper.setText(content, true);
            helper.setFrom("linhtnph31789@fpt.edu.vn");

            logger.info("Đang gửi email...");
            mailSender.send(message);
            logger.info("Email đã được gửi thành công đến: {}", toEmail);
            
        } catch (MessagingException e) {
            logger.error("Lỗi gửi email: {}", e.getMessage(), e);
            throw new RuntimeException("Gửi email thất bại: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Lỗi không xác định khi gửi email: {}", e.getMessage(), e);
            throw new RuntimeException("Gửi email thất bại: " + e.getMessage(), e);
        }
    }
    private String buildEmailContent(String name, String username, String password, String resetToken) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>"
                + "<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0;'>"
                + "<h1 style='margin: 0; font-size: 28px;'> Chào mừng đến với 4PStore!</h1>"
                + "</div>"

                + "<div style='background: #f8f9fa; padding: 30px; border-radius: 0 0 10px 10px; border: 1px solid #e9ecef;'>"
                + "<p style='font-size: 18px; color: #333; margin-bottom: 20px;'>Xin chào <strong style='color: #667eea;'>" + name + "</strong>,</p>"

                + "<p style='color: #666; line-height: 1.6;'>Tài khoản của bạn đã được tạo thành công tại <strong>4PStore</strong>. "
                + "Dưới đây là thông tin đăng nhập của bạn:</p>"

                + "<div style='background: white; padding: 20px; border-radius: 8px; border-left: 4px solid #667eea; margin: 20px 0;'>"
                + "<h3 style='color: #333; margin-top: 0;'>📧 Thông tin đăng nhập:</h3>"
                + "<p style='margin: 10px 0;'><strong>Email:</strong> <code style='background: #f1f3f4; padding: 4px 8px; border-radius: 4px;'>" + username + "</code></p>"
                + "<p style='margin: 10px 0;'><strong>Mật khẩu tạm thời:</strong> <code style='background: #f1f3f4; padding: 4px 8px; border-radius: 4px; color: #d73a49;'>" + password + "</code></p>"
                + "</div>"

                + "<div style='background: #fff3cd; padding: 15px; border-radius: 8px; border-left: 4px solid #ffc107; margin: 20px 0;'>"
                + "<h4 style='color: #856404; margin-top: 0;'>Lưu ý quan trọng:</h4>"
                + "<ul style='color: #856404; margin: 0; padding-left: 20px;'>"
                + "<li>Vui lòng đăng nhập và <strong>đổi mật khẩu ngay lập tức</strong> sau lần đăng nhập đầu tiên</li>"
                + "<li>Không chia sẻ thông tin đăng nhập với bất kỳ ai</li>"
                + "<li>Sử dụng mật khẩu mạnh khi thay đổi</li>"
                + "</ul>"
                + "</div>"

                + "<div style='text-align: center; margin: 30px 0;'>"
                + "<a href='http://localhost:8080/login' style='background: #667eea; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; font-weight: bold; margin-right: 10px;'>🔐 Đăng nhập ngay</a>"
                + (resetToken != null ?
                "<a href='" + buildPasswordResetLink(resetToken) + "' style='background: #28a745; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; font-weight: bold;'>🔄 Đổi mật khẩu</a>"
                : "<!-- Link đổi mật khẩu sẽ được bổ sung sau -->")
                + "</div>"

                + "<hr style='border: none; border-top: 1px solid #e9ecef; margin: 30px 0;'>"

                + "<div style='color: #6c757d; font-size: 14px; text-align: center;'>"
                + "<p>Nếu bạn không yêu cầu tạo tài khoản này, vui lòng liên hệ với chúng tôi ngay lập tức.</p>"
                + "<p><strong>4PStore</strong> - Thời trang nam chất lượng cao</p>"
                + "<p>Hotline: 1900-xxxx | Email: support@4pstore.com</p>"
                + "</div>"
                + "</div>"
                + "</div>";
    }
    @Override
    public void sendPasswordResetMail(String toEmail, String customerName, String resetToken) {
        // TODO: Implement gửi email đổi mật khẩu
        // Sẽ được bổ sung sau khi có chức năng reset password
        throw new UnsupportedOperationException("Chức năng đổi mật khẩu qua email sẽ được bổ sung sau");
    }
    /**
     * Method để tạo link đổi mật khẩu - sẽ được implement sau
     * @param resetToken Token để đổi mật khẩu
     * @return URL đầy đủ để đổi mật khẩu
     */
    private String buildPasswordResetLink(String resetToken) {
        // TODO: Implement tạo link đổi mật khẩu
        return "http://localhost:8080/reset-password?token=" + resetToken;
    }
}
