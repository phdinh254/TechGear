package com.techgear.taikhoan;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/** Gửi email đặt lại mật khẩu sau khi giao dịch lưu mã thành công, chạy nền không chặn phản hồi. */
@Slf4j
@Component
class GuiEmailDatLaiMatKhau {

    private final JavaMailSender mailSender;
    private final String taiKhoanSmtp;
    private final String nguoiGui;

    GuiEmailDatLaiMatKhau(JavaMailSender mailSender, @Value("${spring.mail.username:}") String taiKhoanSmtp,
                          @Value("${app.mail.from}") String nguoiGui) {
        this.mailSender = mailSender;
        this.taiKhoanSmtp = taiKhoanSmtp;
        this.nguoiGui = nguoiGui;
    }

    @Async
    @TransactionalEventListener
    void gui(YeuCauDatLaiMatKhau yeuCau) {
        if (taiKhoanSmtp.isBlank()) {
            log.info("Chưa cấu hình MAIL_USERNAME – liên kết đặt lại mật khẩu cho {}: {}", yeuCau.email(), yeuCau.lienKet());
            return;
        }
        SimpleMailMessage thu = new SimpleMailMessage();
        thu.setFrom(nguoiGui);
        thu.setTo(yeuCau.email());
        thu.setSubject("TechGear – Đặt lại mật khẩu");
        thu.setText("""
                Xin chào %s,

                Bạn (hoặc ai đó) vừa yêu cầu đặt lại mật khẩu tài khoản TechGear.
                Bấm liên kết sau để đặt mật khẩu mới (hiệu lực 15 phút, dùng một lần):
                %s

                Nếu bạn không yêu cầu, hãy bỏ qua email này – mật khẩu của bạn không thay đổi.
                """.formatted(yeuCau.hoTen(), yeuCau.lienKet()));
        try {
            mailSender.send(thu);
        } catch (MailException e) {
            log.warn("Không gửi được email đặt lại mật khẩu tới {}: {}", yeuCau.email(), e.getMessage());
        }
    }
}
