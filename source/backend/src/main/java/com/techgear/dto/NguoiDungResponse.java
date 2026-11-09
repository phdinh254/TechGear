package com.techgear.dto;

import com.techgear.entity.NguoiDung;

import java.time.LocalDate;

public record NguoiDungResponse(
        Long id,
        String hoTen,
        String email,
        String soDienThoai,
        String gioiTinh,
        LocalDate ngaySinh,
        String anhDaiDien,
        String vaiTro,
        String trangThai
) {
    public static NguoiDungResponse tu(NguoiDung nd) {
        return new NguoiDungResponse(nd.getId(), nd.getHoTen(), nd.getEmail(),
                nd.getSoDienThoai(), nd.getGioiTinh() == null ? null : nd.getGioiTinh().name(),
                nd.getNgaySinh(), nd.getAnhDaiDien(), nd.getVaiTro().name(), nd.getTrangThai().name());
    }
}
