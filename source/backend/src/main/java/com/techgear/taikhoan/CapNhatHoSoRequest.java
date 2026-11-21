package com.techgear.taikhoan;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Ghi đè cả 4 trường; trường bỏ trống thì xóa giá trị cũ. */
public record CapNhatHoSoRequest(
        @NotBlank(message = "Vui lòng nhập họ tên")
        @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
        String hoTen,

        @Pattern(regexp = DangKyRequest.SO_DIEN_THOAI_HOAC_RONG, message = "Số điện thoại gồm 10 chữ số, bắt đầu bằng 0")
        String soDienThoai,

        NguoiDung.GioiTinh gioiTinh,

        @Past(message = "Ngày sinh phải trước hôm nay")
        LocalDate ngaySinh
) {
}
