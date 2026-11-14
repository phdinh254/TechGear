package com.techgear.taikhoan;

public record DiaChiResponse(
        Long id,
        String tenNguoiNhan,
        String soDienThoai,
        String tinhThanh,
        String phuongXa,
        String diaChiChiTiet,
        boolean laMacDinh
) {
    static DiaChiResponse tu(DiaChi d) {
        return new DiaChiResponse(d.getId(), d.getTenNguoiNhan(), d.getSoDienThoai(), d.getTinhThanh(),
                d.getPhuongXa(), d.getDiaChiChiTiet(), d.isLaMacDinh());
    }
}
