package com.techgear.dto;

import java.math.BigDecimal;
import java.util.List;

/** Dữ liệu trang chi tiết sản phẩm. */
public record SanPhamChiTietResponse(
        Long id,
        String ten,
        String slug,
        String moTaNgan,
        String moTa,
        BigDecimal giaBan,
        BigDecimal giaGoc,
        Integer baoHanhThang,
        Integer luotXem,
        DanhMucResponse danhMuc,
        ThuongHieuResponse thuongHieu,
        List<HinhAnh> hinhAnhs,
        List<ThongSo> thongSos,
        List<TuyChon> tuyChons,
        List<BienThe> bienThes
) {
    public record HinhAnh(Long id, String duongDan, String moTa, Long giaTriTuyChonId, boolean laAnhChinh) {
    }

    public record ThongSo(String ten, String giaTri) {
    }

    public record TuyChon(Long id, String ten, String loai, List<GiaTri> giaTris) {
    }

    public record GiaTri(Long id, String giaTri, String maMau) {
    }

    public record BienThe(Long id, String sku, BigDecimal giaBan, BigDecimal giaGoc,
                          Integer soLuongTon, List<Long> giaTriIds) {
    }
}
