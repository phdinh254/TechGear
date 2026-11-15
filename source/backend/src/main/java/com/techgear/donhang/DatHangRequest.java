package com.techgear.donhang;

import com.techgear.giohang.DongGioRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Đặt hàng; {@code email} bắt buộc với khách vãng lai, khách hàng đã đăng nhập dùng email tài khoản. */
public record DatHangRequest(
        @NotEmpty(message = "Chưa chọn sản phẩm nào") @Size(max = 50, message = "Mỗi đơn tối đa 50 sản phẩm")
        List<@Valid @NotNull DongGioRequest> dongs,
        @NotBlank(message = "Vui lòng nhập tên người nhận") @Size(max = 100, message = "Tên người nhận tối đa 100 ký tự")
        String tenNguoiNhan,
        @NotBlank(message = "Vui lòng nhập số điện thoại")
        @Pattern(regexp = "0\\d{9}", message = "Số điện thoại gồm 10 chữ số, bắt đầu bằng 0") String soDienThoai,
        @NotBlank(message = "Vui lòng chọn tỉnh/thành phố") @Size(max = 100, message = "Tỉnh/thành phố tối đa 100 ký tự")
        String tinhThanh,
        @NotBlank(message = "Vui lòng chọn phường/xã") @Size(max = 100, message = "Phường/xã tối đa 100 ký tự")
        String phuongXa,
        @NotBlank(message = "Vui lòng nhập số nhà, tên đường") @Size(max = 255, message = "Địa chỉ chi tiết tối đa 255 ký tự")
        String diaChiChiTiet,
        @Email(message = "Email không đúng định dạng") @Size(max = 150, message = "Email tối đa 150 ký tự") String email,
        @Size(max = 50, message = "Mã giảm giá không hợp lệ") String maGiamGia,
        @NotNull(message = "Vui lòng chọn phương thức thanh toán") PhuongThucThanhToan phuongThucThanhToan,
        @Size(max = 500, message = "Ghi chú tối đa 500 ký tự") String ghiChu
) {
    public enum PhuongThucThanhToan { cod, vnpay }
}
