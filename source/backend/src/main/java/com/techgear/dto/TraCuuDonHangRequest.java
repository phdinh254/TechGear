package com.techgear.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Tra cứu đơn hàng cho khách vãng lai: mã đơn + email hoặc số điện thoại đã dùng khi đặt. */
public record TraCuuDonHangRequest(
        @NotBlank(message = "Vui lòng nhập mã đơn hàng")
        @Size(max = 30, message = "Mã đơn hàng tối đa 30 ký tự")
        String maDonHang,

        @NotBlank(message = "Vui lòng nhập email hoặc số điện thoại đặt hàng")
        @Size(max = 150, message = "Email hoặc số điện thoại tối đa 150 ký tự")
        String lienHe
) {
}
