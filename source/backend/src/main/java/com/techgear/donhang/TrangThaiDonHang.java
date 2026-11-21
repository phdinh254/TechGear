package com.techgear.donhang;

/** cho_xac_nhan → da_xac_nhan → dang_giao → hoan_thanh; hủy được trước khi giao. */
public enum TrangThaiDonHang {
    cho_xac_nhan, da_xac_nhan, dang_giao, hoan_thanh, da_huy;

    public boolean coTheChuyenSang(TrangThaiDonHang moi) {
        return switch (this) {
            case cho_xac_nhan -> moi == da_xac_nhan || moi == da_huy;
            case da_xac_nhan -> moi == dang_giao || moi == da_huy;
            case dang_giao -> moi == hoan_thanh;
            case hoan_thanh, da_huy -> false;
        };
    }
}
