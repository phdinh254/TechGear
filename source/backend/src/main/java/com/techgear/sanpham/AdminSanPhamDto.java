package com.techgear.sanpham;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;

/** Kiểu dữ liệu vào/ra của API quản trị sản phẩm (xem docs/thiet-ke/dac-ta-api.md, mục 9.1). */
public final class AdminSanPhamDto {

    private AdminSanPhamDto() {
    }

    public record ThongSoRequest(
            @NotBlank(message = "Vui lòng nhập tên thông số") @Size(max = 100, message = "Tên thông số tối đa 100 ký tự") String ten,
            @NotBlank(message = "Vui lòng nhập giá trị thông số") @Size(max = 255, message = "Giá trị tối đa 255 ký tự") String giaTri
    ) {
    }

    public record SanPhamRequest(
            @NotBlank(message = "Vui lòng nhập tên sản phẩm") @Size(max = 255, message = "Tên tối đa 255 ký tự") String ten,
            @Size(max = 200, message = "Slug tối đa 200 ký tự")
            @Pattern(regexp = "[a-z0-9]+(-[a-z0-9]+)*", message = "Slug chỉ gồm chữ thường không dấu, số và dấu gạch ngang")
            String slug,
            @NotNull(message = "Vui lòng chọn danh mục") Long danhMucId,
            Long thuongHieuId,
            @Size(max = 500, message = "Mô tả ngắn tối đa 500 ký tự") String moTaNgan,
            @Size(max = 100_000, message = "Mô tả quá dài") String moTa,
            @Min(value = 0, message = "Bảo hành không được âm") @Max(value = 120, message = "Bảo hành tối đa 120 tháng")
            Integer baoHanhThang,
            Boolean noiBat,
            Boolean hienThi,
            @Size(max = 50, message = "Tối đa 50 thông số") List<@Valid @NotNull ThongSoRequest> thongSos
    ) {
    }

    public record GiaTriRequest(
            Long id,
            @NotBlank(message = "Vui lòng nhập giá trị") @Size(max = 50, message = "Giá trị tối đa 50 ký tự") String giaTri,
            @Pattern(regexp = "#[0-9A-Fa-f]{6}", message = "Mã màu dạng #RRGGBB") String maMau
    ) {
    }

    public record TuyChonRequest(
            @NotBlank(message = "Vui lòng nhập tên tùy chọn") @Size(max = 50, message = "Tên tối đa 50 ký tự") String ten,
            @NotNull(message = "Vui lòng chọn loại tùy chọn")
            @Pattern(regexp = "mau_sac|dung_luong|phien_ban|khac", message = "Loại là mau_sac, dung_luong, phien_ban hoặc khac")
            String loai,
            @NotEmpty(message = "Cần ít nhất một giá trị") @Size(max = 30, message = "Tối đa 30 giá trị")
            List<@Valid @NotNull GiaTriRequest> giaTris
    ) {
    }

    public record HinhAnhRequest(
            @NotBlank(message = "Thiếu đường dẫn ảnh") @Size(max = 255, message = "Đường dẫn tối đa 255 ký tự") String duongDan,
            @Size(max = 255, message = "Mô tả ảnh tối đa 255 ký tự") String moTa,
            Long giaTriTuyChonId,
            Boolean laAnhChinh,
            Integer thuTu
    ) {
    }

    public record BienTheRequest(
            @NotBlank(message = "Vui lòng nhập SKU") @Size(max = 100, message = "SKU tối đa 100 ký tự") String sku,
            @NotNull(message = "Vui lòng nhập giá bán") @PositiveOrZero(message = "Giá bán không được âm") Long giaBan,
            @PositiveOrZero(message = "Giá gốc không được âm") Long giaGoc,
            @PositiveOrZero(message = "Tồn kho không được âm") @Max(value = 1_000_000, message = "Tồn kho tối đa 1.000.000")
            Integer soLuongTon,
            @PositiveOrZero(message = "Ngưỡng cảnh báo không được âm") Integer nguongCanhBao,
            Boolean hienThi,
            List<@NotNull Long> giaTriIds
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record HinhAnhQT(Long id, String duongDan, String moTa, Long giaTriTuyChonId, boolean laAnhChinh, int thuTu) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record BienTheQT(Long id, String sku, long giaBan, Long giaGoc, int soLuongTon, int nguongCanhBao,
                            boolean hienThi, List<Long> giaTriIds) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SanPhamQT(Long id, String ten, String slug, Long danhMucId, Long thuongHieuId, String moTaNgan,
                            String moTa, int baoHanhThang, boolean noiBat, boolean hienThi, long giaBan, Long giaGoc,
                            List<SanPhamChiTiet.ThongSo> thongSos, List<SanPhamChiTiet.TuyChon> tuyChons,
                            List<HinhAnhQT> hinhAnhs, List<BienTheQT> bienThes) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record DongDanhSach(Long id, String ten, String slug, String anhChinh, String tenDanhMuc,
                               String tenThuongHieu, long giaBan, long tongTon, int soBienThe, boolean hienThi,
                               boolean noiBat, int luotXem, OffsetDateTime ngayTao) {
    }
}
