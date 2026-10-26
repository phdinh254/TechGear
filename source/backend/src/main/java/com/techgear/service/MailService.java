package com.techgear.service;

import com.techgear.event.YeuCauDatLaiMatKhauEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.HtmlUtils;

/**
 * Gửi email qua SMTP. Nếu chưa cấu hình MAIL_USERNAME thì chỉ ghi nội dung ra log để chạy thử.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String taiKhoanSmtp;

    @Value("${app.mail.from}")
    private String nguoiGui;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void guiEmailDatLaiMatKhau(YeuCauDatLaiMatKhauEvent e) {
        String noiDung = """
                <div style="font-family:Arial,sans-serif;font-size:15px;color:#222">
                  <p>Xin chào %s,</p>
                  <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản TechGear của bạn.</p>
                  <p><a href="%s" style="display:inline-block;padding:10px 18px;background:#0f766e;color:#fff;
                     text-decoration:none;border-radius:6px">Đặt lại mật khẩu</a></p>
                  <p>Liên kết có hiệu lực trong %d phút và chỉ dùng được một lần.
                     Nếu bạn không yêu cầu, hãy bỏ qua email này, mật khẩu của bạn không thay đổi.</p>
                  <p>TechGear</p>
                </div>
                """.formatted(HtmlUtils.htmlEscape(e.hoTen()), e.duongDanDatLai(), e.hieuLucPhut());
        gui(e.email(), "[TechGear] Đặt lại mật khẩu", noiDung, e.duongDanDatLai());
    }

    private void gui(String den, String tieuDe, String html, String duongDanDeLog) {
        if (taiKhoanSmtp == null || taiKhoanSmtp.isBlank()) {
            log.info("[EMAIL - chưa cấu hình SMTP, chỉ ghi log] Đến: {} | Tiêu đề: {} | Liên kết: {}",
                    den, tieuDe, duongDanDeLog);
            return;
        }
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, "UTF-8");
            helper.setFrom(nguoiGui);
            helper.setTo(den);
            helper.setSubject(tieuDe);
            helper.setText(html, true);
            mailSender.send(msg);
            log.info("Đã gửi email '{}' tới {}", tieuDe, den);
        } catch (MessagingException | MailException ex) {
            // Không ném lỗi ra ngoài: người dùng đã nhận thông báo chung, chỉ ghi log để quản trị kiểm tra SMTP
            log.error("Gửi email tới {} thất bại", den, ex);
        }
    }
}
