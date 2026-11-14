package com.techgear.dto;

import com.techgear.entity.DanhGia;

import java.time.LocalDateTime;

/** Đánh giá đã duyệt hiển thị công khai ở trang chi tiết sản phẩm. */
public record DanhGiaResponse(
        Long id,
        Integer soSao,
        String noiDung,
        String tenNguoiDung,
        String anhDaiDien,
        LocalDateTime ngayTao
) {
    public static DanhGiaResponse tu(DanhGia dg) {
        return new DanhGiaResponse(dg.getId(), dg.getSoSao(), dg.getNoiDung(),
                dg.getNguoiDung().getHoTen(), dg.getNguoiDung().getAnhDaiDien(), dg.getCreatedAt());
    }
}
