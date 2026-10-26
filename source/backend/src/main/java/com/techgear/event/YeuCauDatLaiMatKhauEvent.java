package com.techgear.event;

/** Phát ra khi đã lưu mã đặt lại mật khẩu; email chỉ được gửi sau khi giao dịch commit. */
public record YeuCauDatLaiMatKhauEvent(String email, String hoTen, String duongDanDatLai, long hieuLucPhut) {
}
