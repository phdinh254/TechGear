package com.techgear.danhgia;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

/** Kiểu dữ liệu vào/ra của module đánh giá (xem docs/thiet-ke/dac-ta-api.md). */
public final class DanhGiaDto {

    private DanhGiaDto() {
    }

    public record VietRequest(
            @NotBlank(message = "Vui lòng nhập mã đơn hàng") @Size(max = 20, message = "Mã đơn hàng không hợp lệ")
            String maDonHang,
            @NotNull(message = "Thiếu sản phẩm") Long sanPhamId,
            @NotNull(message = "Số sao từ 1 đến 5") @Min(value = 1, message = "Số sao từ 1 đến 5")
            @Max(value = 5, message = "Số sao từ 1 đến 5") Integer soSao,
            @Size(max = 2000, message = "Nội dung tối đa 2000 ký tự") String noiDung
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record DanhGia(Long id, Long sanPhamId, int soSao, String noiDung, String trangThai, OffsetDateTime ngayTao) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CoTheViet(String maDonHang, Long sanPhamId, String sanPhamSlug, String tenSanPham, String hinhAnh,
                            OffsetDateTime ngayDat) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CongKhai(Long id, String hoTen, int soSao, String noiDung, OffsetDateTime ngayTao) {
    }
}
