package com.techgear.sanpham;

/**
 * Quy tắc "đang bán" dùng chung cho mọi truy vấn phía khách (sản phẩm, giỏ hàng, yêu thích, đặt hàng):
 * bí danh sp = san_pham, dm = danh_muc, dmc = danh_muc cha, th = thuong_hieu.
 */
public final class HienThi {

    public static final String TU_BANG = """
            FROM san_pham sp
            JOIN danh_muc dm ON dm.id = sp.danh_muc_id
            LEFT JOIN danh_muc dmc ON dmc.id = dm.danh_muc_cha_id
            LEFT JOIN thuong_hieu th ON th.id = sp.thuong_hieu_id
            """;

    /** Sản phẩm hiển thị: đang bật, danh mục (và cha) đang bật, có ít nhất một biến thể đang bán. */
    public static final String SAN_PHAM_DANG_HIEN_THI = """
            sp.hien_thi AND sp.deleted_at IS NULL
            AND dm.hien_thi AND dm.deleted_at IS NULL
            AND (dmc.id IS NULL OR (dmc.hien_thi AND dmc.deleted_at IS NULL))
            AND EXISTS (SELECT 1 FROM bien_the bt WHERE bt.san_pham_id = sp.id AND bt.hien_thi AND bt.deleted_at IS NULL)
            """;

    /** Biến thể bí danh {@code bt} đang bán: bản thân đang bật và sản phẩm của nó đang hiển thị. */
    public static final String BIEN_THE_DANG_BAN = "bt.hien_thi AND bt.deleted_at IS NULL AND " + SAN_PHAM_DANG_HIEN_THI;

    private HienThi() {
    }
}
