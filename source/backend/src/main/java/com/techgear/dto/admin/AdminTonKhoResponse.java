package com.techgear.dto.admin;

/** trangThai: con_hang | sap_het | het_hang. */
public record AdminTonKhoResponse(
        Long bienTheId,
        Long sanPhamId,
        String tenSanPham,
        String phanLoai,
        String sku,
        int soLuongTon,
        int nguongCanhBao,
        String trangThai,
        boolean dangBan
) {
}
