package com.techgear.dto;

/** Tổng quan và danh sách đánh giá đã duyệt của một sản phẩm. */
public record DanhGiaSanPhamResponse(
        double diemTrungBinh,
        long tongDanhGia,
        PageResponse<DanhGiaResponse> danhSach
) {
}
