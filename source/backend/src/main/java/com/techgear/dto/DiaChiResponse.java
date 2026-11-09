package com.techgear.dto;

import com.techgear.entity.DiaChi;

public record DiaChiResponse(
        Long id,
        String tenNguoiNhan,
        String soDienThoai,
        String tinhThanh,
        String phuongXa,
        String diaChiChiTiet,
        boolean laMacDinh,
        /** Địa chỉ ghép sẵn để hiển thị: "12 Lê Lợi, Phường 1, TP. Trà Vinh" */
        String diaChiDayDu
) {
    public static DiaChiResponse tu(DiaChi dc) {
        return new DiaChiResponse(dc.getId(), dc.getTenNguoiNhan(), dc.getSoDienThoai(), dc.getTinhThanh(),
                dc.getPhuongXa(), dc.getDiaChiChiTiet(), Boolean.TRUE.equals(dc.getLaMacDinh()),
                dc.getDiaChiChiTiet() + ", " + dc.getPhuongXa() + ", " + dc.getTinhThanh());
    }
}
