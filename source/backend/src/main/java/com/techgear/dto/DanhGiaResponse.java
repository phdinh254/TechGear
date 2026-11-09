package com.techgear.dto;

import com.techgear.entity.DanhGia;

import java.time.LocalDateTime;

/** Đánh giá đã được duyệt để hiển thị công khai trên trang sản phẩm. */
public record DanhGiaResponse(
        Long id,
        Integer soSao,
        String noiDung,
        String tenNguoiDung,
        String anhDaiDien,
        LocalDateTime ngayDanhGia
) {
    public static DanhGiaResponse tu(DanhGia danhGia) {
        return new DanhGiaResponse(
                danhGia.getId(),
                danhGia.getSoSao(),
                danhGia.getNoiDung(),
                danhGia.getNguoiDung().getHoTen(),
                danhGia.getNguoiDung().getAnhDaiDien(),
                danhGia.getCreatedAt()
        );
    }
}
