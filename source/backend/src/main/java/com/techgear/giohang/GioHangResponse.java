package com.techgear.giohang;

import java.util.List;

/** Tổng số lượng và tạm tính chỉ gồm các dòng còn bán. */
public record GioHangResponse(List<DongGioHang> dongs, int tongSoLuong, long tamTinh) {

    static GioHangResponse tu(List<DongGioHang> dongs) {
        List<DongGioHang> conBan = dongs.stream().filter(DongGioHang::conBan).toList();
        return new GioHangResponse(dongs,
                conBan.stream().mapToInt(DongGioHang::soLuong).sum(),
                conBan.stream().mapToLong(d -> d.giaBan() * d.soLuong()).sum());
    }
}
