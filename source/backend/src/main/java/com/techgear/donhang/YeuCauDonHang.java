package com.techgear.donhang;

import com.techgear.giohang.DongGioRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Các yêu cầu nhỏ của module đơn hàng. */
public final class YeuCauDonHang {

    private YeuCauDonHang() {
    }

    /** Tính thử; {@code email} để kiểm lượt dùng mã của mỗi người. */
    public record TinhThu(
            @NotEmpty(message = "Chưa chọn sản phẩm nào") @Size(max = 50, message = "Mỗi đơn tối đa 50 sản phẩm")
            List<@Valid @NotNull DongGioRequest> dongs,
            @Size(max = 50, message = "Mã giảm giá không hợp lệ") String maGiamGia,
            @Email(message = "Email không đúng định dạng") @Size(max = 150, message = "Email tối đa 150 ký tự") String email
    ) {
    }

    public record TraCuu(
            @NotBlank(message = "Vui lòng nhập mã đơn hàng") @Size(max = 20, message = "Mã đơn hàng không hợp lệ")
            String maDonHang,
            @NotBlank(message = "Vui lòng nhập email") @Email(message = "Email không đúng định dạng")
            @Size(max = 150, message = "Email tối đa 150 ký tự") String email
    ) {
    }

    public record Huy(@Size(max = 500, message = "Lý do tối đa 500 ký tự") String lyDo) {
    }
}
