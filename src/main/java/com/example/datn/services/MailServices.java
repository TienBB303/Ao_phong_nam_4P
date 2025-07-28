package com.example.datn.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@EnableAsync
public class MailServices {


    private final Logger log = LoggerFactory.getLogger(MailServices.class);

    @Autowired
    private JavaMailSender javaMailSender;


    final static String username = "thaitvph40872@fpt.edu.vn";

    @Async
    public void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        log.debug(
                "Send email[multipart '{}' and html '{}'] to '{}' with subject '{}' and content={}",
                isMultipart,
                isHtml,
                to,
                subject,
                content
        );

        // Prepare message using a Spring helper
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart, StandardCharsets.UTF_8.name());
            message.setTo(to);
            message.setFrom(username);
            message.setSubject(subject);
            System.out.println("subject: " + subject);
            message.setText(content, isHtml);
            System.out.println("content: " + content);
            javaMailSender.send(mimeMessage);
            log.debug("Sent email to User '{}'", to);
        } catch (MailException | MessagingException e) {
            log.warn("Email could not be sent to user '{}'", to, e);
        }
    }


    public String buildOrderConfirmationEmailTemplate(String orderId, String orderDate, double totalAmount,
                                                      String shippingAddress, String orderNotes, String customerName,
                                                      String supportEmail) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f8f9fa; padding: 20px;">
                    <div style="max-width: 600px; margin: auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                        <h2 style="color: #333;">Xin chào %s,</h2>
                        <p style="color: #555;">Cảm ơn bạn đã tin tưởng và đặt hàng tại <strong>4PStore</strong>!</p>
                        <p>Chúng tôi đã nhận được đơn hàng của bạn và đang tiến hành xử lý. Dưới đây là thông tin chi tiết về đơn hàng:</p>

                        <p style="margin-top: 20px;"><strong>Chi tiết đơn hàng:</strong></p>
                        <ul style="color: #555; line-height: 1.6;">
                            <li><strong>Mã tra cứu đơn hàng:</strong> %s</li>
                            <li><strong>Ngày đặt hàng:</strong> %s</li>
                            <li><strong>Tổng thanh toán:</strong> %,.2f VND</li>
                            <li><strong>Địa chỉ giao hàng:</strong> %s</li>
                            <li><strong>Ghi chú:</strong> %s</li>
                        </ul>

                        <p style="margin-top: 20px;">Chúng tôi sẽ gửi thông báo đến bạn ngay khi đơn hàng được vận chuyển. Vui lòng giữ liên lạc qua email hoặc điện thoại để đảm bảo giao hàng thành công.</p>

                        <p style="font-size: 13px; color: #999;">Nếu bạn có bất kỳ thắc mắc nào, đừng ngần ngại liên hệ với chúng tôi qua email: <a href="mailto:%s">%s</a>.</p>
                        
                        <hr style="margin: 30px 0; border: none; border-top: 1px solid #eee;">
                        <div style="font-size: 14px; color: #666;">
                            <b>Website bán áo phông nam 4PStore <br/>
                            <strong>SĐT:</strong> 0398121210 <br/>
                            <strong>Địa chỉ:</strong> FPT Polytechnic Trịnh Văn Bô <br/>
                            <strong> Email: 4PStore@gmail.com
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                customerName,
                orderId,
                orderDate,
                totalAmount,
                shippingAddress,
                orderNotes.isEmpty() ? "Không có ghi chú" : orderNotes,
                supportEmail,
                supportEmail
        );
    }

}
