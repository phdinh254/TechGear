package com.techgear.taikhoan;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;

/** Thông tin tài khoản trả frontend (frontend hiện đọc id, hoTen, email, soDienThoai, vaiTro). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record NguoiDungResponse(
        Long id,
        String hoTen,
        String email,
        String soDienThoai,
        NguoiDung.GioiTinh gioiTinh,
        LocalDate ngaySinh,
        String anhDaiDien,
        NguoiDung.VaiTro vaiTro
) {
    static NguoiDungResponse tu(NguoiDung n) {
        return new NguoiDungResponse(n.getId(), n.getHoTen(), n.getEmail(), n.getSoDienThoai(), n.getGioiTinh(),
                n.getNgaySinh(), n.getAnhDaiDien(), n.getVaiTro());
    }
}
