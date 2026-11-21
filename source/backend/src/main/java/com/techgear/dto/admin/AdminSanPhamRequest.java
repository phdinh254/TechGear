package com.techgear.dto.admin;

import com.techgear.entity.enums.LoaiTuyChon;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record AdminSanPhamRequest(
        @NotBlank(message = "Vui lòng nhập tên sản phẩm")
        @Size(max = 255, message = "Tên sản phẩm tối đa 255 ký tự") String ten,
        @NotNull(message = "Vui lòng chọn danh mục") Long danhMucId,
        Long thuongHieuId,
        @Size(max = 500, message = "Mô tả ngắn tối đa 500 ký tự") String moTaNgan,
        String moTa,
        @NotNull(message = "Vui lòng nhập giá bán") @Positive(message = "Giá bán phải lớn hơn 0")
        @Digits(integer = 12, fraction = 0, message = "Giá phải là số nguyên, tối đa 12 chữ số") BigDecimal giaBan,
        @Positive(message = "Giá gốc phải lớn hơn 0")
        @Digits(integer = 12, fraction = 0, message = "Giá phải là số nguyên, tối đa 12 chữ số") BigDecimal giaGoc,
        @NotNull(message = "Vui lòng nhập thời gian bảo hành") @Min(value = 0, message = "Bảo hành không được âm")
        @Max(value = 120, message = "Bảo hành tối đa 120 tháng") Integer baoHanhThang,
        Boolean noiBat,
        Boolean hienThi,
        @Valid List<HinhAnh> hinhAnhs,
        @Valid List<ThongSo> thongSos,
        @Valid @Size(max = 3, message = "Tối đa 3 tùy chọn") List<TuyChon> tuyChons,
        @Valid @NotEmpty(message = "Cần ít nhất một biến thể") @Size(max = 50, message = "Tối đa 50 biến thể") List<BienThe> bienThes
) {

    /** tuyChon + giaTri null = ảnh chung. */
    public record HinhAnh(@NotBlank(message = "Vui lòng nhập đường dẫn ảnh") @Size(max = 255, message = "Đường dẫn ảnh tối đa 255 ký tự") String duongDan,
                          @Size(max = 255, message = "Mô tả ảnh tối đa 255 ký tự") String moTa,
                          boolean laAnhChinh,
                          String tuyChon,
                          String giaTri) {
    }

    public record ThongSo(@NotBlank(message = "Vui lòng nhập tên thông số") @Size(max = 100, message = "Tên thông số tối đa 100 ký tự") String ten,
                          @NotBlank(message = "Vui lòng nhập giá trị thông số") @Size(max = 255, message = "Giá trị thông số tối đa 255 ký tự") String giaTri) {
    }

    public record TuyChon(Long id,
                          @NotBlank(message = "Vui lòng nhập tên tùy chọn") @Size(max = 50, message = "Tên tùy chọn tối đa 50 ký tự") String ten,
                          @NotNull(message = "Vui lòng chọn loại tùy chọn") LoaiTuyChon loai,
                          @Valid @NotEmpty(message = "Tùy chọn cần ít nhất một giá trị") List<GiaTri> giaTris) {
    }

    public record GiaTri(Long id,
                         @NotBlank(message = "Vui lòng nhập giá trị tùy chọn") @Size(max = 50, message = "Giá trị tùy chọn tối đa 50 ký tự") String giaTri,
                         @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Mã màu dạng #RRGGBB") String maMau) {
    }

    public record BienThe(
            /** Giá trị theo đúng thứ tự tuyChons. */
            @NotNull(message = "Thiếu giá trị tùy chọn của biến thể") List<String> giaTris,
            @NotBlank(message = "Vui lòng nhập SKU") @Size(max = 100, message = "SKU tối đa 100 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "SKU chỉ gồm chữ, số, dấu chấm, gạch ngang, gạch dưới")
            String sku,
            @Positive(message = "Giá bán phải lớn hơn 0")
            @Digits(integer = 12, fraction = 0, message = "Giá phải là số nguyên, tối đa 12 chữ số") BigDecimal giaBan,
            @Positive(message = "Giá gốc phải lớn hơn 0")
            @Digits(integer = 12, fraction = 0, message = "Giá phải là số nguyên, tối đa 12 chữ số") BigDecimal giaGoc,
            @Min(value = 0, message = "Tồn kho ban đầu không được âm") Integer tonKhoBanDau,
            @NotNull(message = "Vui lòng nhập ngưỡng cảnh báo") @Min(value = 0, message = "Ngưỡng cảnh báo không được âm") Integer nguongCanhBao,
            Boolean dangBan) {
    }
}
