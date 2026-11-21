package com.techgear.khuyenmai;

import java.time.OffsetDateTime;

/** Một dòng ma_giam_gia; {@code loaiGiam} là "phan_tram" hoặc "so_tien". */
public record MaGiamGia(
        Long id,
        String ma,
        String loaiGiam,
        long giaTriGiam,
        long donToiThieu,
        Long giamToiDa,
        Integer tongLuotDung,
        Integer luotDungMoiNguoi,
        int daDung,
        OffsetDateTime batDauLuc,
        OffsetDateTime ketThucLuc,
        boolean kichHoat
) {

    /** Thông báo lỗi khi áp mã, null nếu hợp lệ; {@code daDungCuaKhach} = số đơn chưa hủy của khách đã dùng mã. */
    public String loiKhiApDung(OffsetDateTime bayGio, long tamTinh, long daDungCuaKhach) {
        if (!kichHoat) {
            return "Mã giảm giá đã ngừng áp dụng";
        }
        if (bayGio.isBefore(batDauLuc)) {
            return "Mã giảm giá chưa đến thời gian sử dụng";
        }
        if (!bayGio.isBefore(ketThucLuc)) {
            return "Mã giảm giá đã hết hạn";
        }
        if (tamTinh < donToiThieu) {
            return "Đơn hàng tối thiểu " + tien(donToiThieu) + " để dùng mã này";
        }
        if (tongLuotDung != null && daDung >= tongLuotDung) {
            return "Mã giảm giá đã hết lượt sử dụng";
        }
        if (luotDungMoiNguoi != null && daDungCuaKhach >= luotDungMoiNguoi) {
            return "Bạn đã dùng hết lượt của mã giảm giá này";
        }
        return null;
    }

    /** Tiền giảm làm tròn xuống đến đồng, không vượt giảm tối đa (mã %) và không vượt tạm tính. */
    public long tienGiam(long tamTinh) {
        long giam = "phan_tram".equals(loaiGiam)
                ? tamTinh / 100 * giaTriGiam + tamTinh % 100 * giaTriGiam / 100 // tách để tamTinh × % không tràn số
                : giaTriGiam;
        if (giamToiDa != null) {
            giam = Math.min(giam, giamToiDa);
        }
        return Math.min(giam, tamTinh);
    }

    static String tien(long dong) {
        return String.format("%,d", dong).replace(',', '.') + "đ";
    }
}
