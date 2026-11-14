package com.techgear.taikhoan;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DiaChiRequest(
        @NotBlank(message = "Vui lòng nhập tên người nhận")
        @Size(max = 100, message = "Tên người nhận tối đa 100 ký tự")
        String tenNguoiNhan,

        @NotBlank(message = "Vui lòng nhập số điện thoại")
        @Pattern(regexp = "0\\d{9}", message = "Số điện thoại gồm 10 chữ số, bắt đầu bằng 0")
        String soDienThoai,

        @NotBlank(message = "Vui lòng chọn tỉnh/thành phố")
        @Size(max = 100, message = "Tỉnh/thành phố tối đa 100 ký tự")
        String tinhThanh,

        @NotBlank(message = "Vui lòng chọn phường/xã")
        @Size(max = 100, message = "Phường/xã tối đa 100 ký tự")
        String phuongXa,

        @NotBlank(message = "Vui lòng nhập số nhà, tên đường")
        @Size(max = 255, message = "Địa chỉ chi tiết tối đa 255 ký tự")
        String diaChiChiTiet,

        /** true: đặt làm mặc định; false/null: giữ nguyên (không bỏ được mặc định bằng cách này). */
        Boolean laMacDinh
) {
}
