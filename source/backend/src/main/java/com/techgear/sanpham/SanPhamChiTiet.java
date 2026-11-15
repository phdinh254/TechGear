package com.techgear.sanpham;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/** Chi tiết sản phẩm cho trang sản phẩm (hợp đồng với frontend: ProductDetailPage). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SanPhamChiTiet(
        Long id,
        String ten,
        String slug,
        String moTaNgan,
        String moTa,
        long giaBan,
        Long giaGoc,
        int baoHanhThang,
        boolean conHang,
        ThamChieu danhMuc,
        ThamChieu thuongHieu,
        List<HinhAnh> hinhAnhs,
        List<ThongSo> thongSos,
        List<TuyChon> tuyChons,
        List<BienThe> bienThes
) {
    public record ThamChieu(Long id, String ten, String slug) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record HinhAnh(Long id, String duongDan, String moTa, Long giaTriTuyChonId, boolean laAnhChinh) {
    }

    public record ThongSo(String ten, String giaTri) {
    }

    public record TuyChon(Long id, String ten, String loai, List<GiaTri> giaTris) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record GiaTri(Long id, String giaTri, String maMau) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record BienThe(Long id, String sku, long giaBan, Long giaGoc, int soLuongTon, List<Long> giaTriIds) {
    }
}
