package com.techgear.dto;

import com.techgear.entity.enums.PhuongThucThanhToan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DatHangRequest(
        @NotBlank(message = "Vui lòng nhập tên người nhận")
        @Size(max = 100, message = "Tên người nhận tối đa 100 ký tự")
        String tenNguoiNhan,

        @NotBlank(message = "Vui lòng nhập email")
        @Email(message = "Email không hợp lệ")
        @Size(max = 150, message = "Email tối đa 150 ký tự")
        String email,

        @NotBlank(message = "Vui lòng nhập số điện thoại")
        @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại gồm 10 chữ số, bắt đầu bằng 0")
        String soDienThoai,

        @NotBlank(message = "Vui lòng nhập địa chỉ giao hàng")
        @Size(max = 500, message = "Địa chỉ giao hàng tối đa 500 ký tự")
        String diaChiGiaoHang,

        @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
        String ghiChu,

        @Size(max = 50, message = "Mã giảm giá tối đa 50 ký tự")
        String maGiamGia,

        @NotNull(message = "Vui lòng chọn phương thức thanh toán")
        PhuongThucThanhToan phuongThucThanhToan,

        @Size(max = 100, message = "Giỏ hàng tối đa 100 dòng")
        List<@Valid GioHangItemRequest> items
) {
}
