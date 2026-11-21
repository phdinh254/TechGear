package com.techgear.dto.admin;

import com.techgear.entity.DanhMuc;

import java.util.List;

public record AdminDanhMucResponse(
        Long id,
        String ten,
        String slug,
        Long danhMucChaId,
        String moTa,
        String hinhAnh,
        Integer thuTu,
        Boolean hienThi,
        long soSanPham,
        List<AdminDanhMucResponse> danhMucCons
) {
    public static AdminDanhMucResponse tu(DanhMuc d, long soSanPham, List<AdminDanhMucResponse> danhMucCons) {
        return new AdminDanhMucResponse(d.getId(), d.getTen(), d.getSlug(),
                d.getDanhMucCha() == null ? null : d.getDanhMucCha().getId(),
                d.getMoTa(), d.getHinhAnh(), d.getThuTu(), d.getHienThi(), soSanPham, danhMucCons);
    }
}
