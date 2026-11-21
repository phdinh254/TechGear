package com.techgear.dto.admin;

import com.techgear.entity.enums.LoaiTuyChon;

import java.math.BigDecimal;
import java.util.List;

/** Dữ liệu form sửa sản phẩm: cùng cấu trúc với {@link AdminSanPhamRequest}, thêm id và slug. */
public record AdminSanPhamChiTietResponse(
        Long id,
        String ten,
        String slug,
        Long danhMucId,
        Long thuongHieuId,
        String moTaNgan,
        String moTa,
        BigDecimal giaBan,
        BigDecimal giaGoc,
        Integer baoHanhThang,
        Boolean noiBat,
        Boolean hienThi,
        List<HinhAnh> hinhAnhs,
        List<ThongSo> thongSos,
        List<TuyChon> tuyChons,
        List<BienThe> bienThes
) {

    /** tuyChon + giaTri null = ảnh chung. */
    public record HinhAnh(Long id, String duongDan, String moTa, boolean laAnhChinh, String tuyChon, String giaTri) {
    }

    public record ThongSo(String ten, String giaTri) {
    }

    public record TuyChon(Long id, String ten, LoaiTuyChon loai, List<GiaTri> giaTris) {
    }

    public record GiaTri(Long id, String giaTri, String maMau) {
    }

    /** giaTris theo đúng thứ tự tuyChons; giaBan / giaGoc null = lấy giá sản phẩm. */
    public record BienThe(Long id, List<String> giaTris, String sku, BigDecimal giaBan, BigDecimal giaGoc,
                          int soLuongTon, int nguongCanhBao, boolean dangBan) {
    }
}
