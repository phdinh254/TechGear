package com.techgear.dto;

import com.techgear.entity.DiaChi;

public record DiaChiResponse(
        Long id,
        String tenNguoiNhan,
        String soDienThoai,
        String tinhThanh,
        String phuongXa,
        String diaChiChiTiet,
        boolean laMacDinh
) {
    public static DiaChiResponse tu(DiaChi diaChi) {
        return new DiaChiResponse(
                diaChi.getId(), diaChi.getTenNguoiNhan(), diaChi.getSoDienThoai(),
                diaChi.getTinhThanh(), diaChi.getPhuongXa(), diaChi.getDiaChiChiTiet(),
                Boolean.TRUE.equals(diaChi.getLaMacDinh())
        );
    }
}
