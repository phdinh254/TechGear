package com.techgear.dto;

import com.techgear.entity.NguoiDung;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record NguoiDungResponse(
        Long id,
        String hoTen,
        String email,
        String soDienThoai,
        String gioiTinh,
        LocalDate ngaySinh,
        String anhDaiDien,
        String vaiTro,
        LocalDateTime ngayTao
) {
    public static NguoiDungResponse tu(NguoiDung nd) {
        return new NguoiDungResponse(nd.getId(), nd.getHoTen(), nd.getEmail(), nd.getSoDienThoai(),
                nd.getGioiTinh() == null ? null : nd.getGioiTinh().name(), nd.getNgaySinh(),
                nd.getAnhDaiDien(), nd.getVaiTro().name(), nd.getCreatedAt());
    }
}
