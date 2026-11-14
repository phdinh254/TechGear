package com.techgear.taikhoan;

/** Sự kiện phát ra khi tạo mã đặt lại mật khẩu; email gửi sau khi giao dịch lưu thành công. */
public record YeuCauDatLaiMatKhau(String email, String hoTen, String lienKet) {
}
