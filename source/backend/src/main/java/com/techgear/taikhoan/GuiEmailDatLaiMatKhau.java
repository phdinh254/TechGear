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
    private final boolean ghiLienKetRaLog;

    GuiEmailDatLaiMatKhau(JavaMailSender mailSender, @Value("${spring.mail.username:}") String taiKhoanSmtp,
                          @Value("${app.mail.from}") String nguoiGui,
                          @Value("${app.mail.ghi-lien-ket-ra-log:false}") boolean ghiLienKetRaLog) {
        this.mailSender = mailSender;
        this.taiKhoanSmtp = taiKhoanSmtp;
        this.nguoiGui = nguoiGui;
        this.ghiLienKetRaLog = ghiLienKetRaLog;
    }

    @Async
    @TransactionalEventListener
    void gui(YeuCauDatLaiMatKhau yeuCau) {
        if (taiKhoanSmtp.isBlank()) {
            // Liên kết chứa mã dùng được ngay: chỉ ghi ra log ở dev, nơi khác ai đọc log cũng chiếm được tài khoản
            if (ghiLienKetRaLog) {
                log.info("Chưa cấu hình MAIL_USERNAME – liên kết đặt lại mật khẩu cho {}: {}", yeuCau.email(), yeuCau.lienKet());
            } else {
                log.warn("Chưa cấu hình MAIL_USERNAME – không gửi được email đặt lại mật khẩu cho {}", yeuCau.email());
            }
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
