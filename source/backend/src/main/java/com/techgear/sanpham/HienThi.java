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

    /** Các giá trị tùy chọn của biến thể {@code bt} nối bằng " / " (VD "Đen / 2m"); NULL nếu không có tùy chọn. */
    public static final String PHAN_LOAI = """
            (SELECT GROUP_CONCAT(gt.gia_tri ORDER BY tc.thu_tu, tc.id SEPARATOR ' / ')
             FROM bien_the_gia_tri btg
             JOIN gia_tri_tuy_chon gt ON gt.id = btg.gia_tri_tuy_chon_id
             JOIN tuy_chon tc ON tc.id = gt.tuy_chon_id
             WHERE btg.bien_the_id = bt.id)
            """;

    /** Ảnh của biến thể {@code bt}: ảnh gắn với giá trị tùy chọn (màu) của nó, không có thì ảnh chính. */
    public static final String ANH_BIEN_THE = """
            (SELECT ha.duong_dan FROM hinh_anh_san_pham ha
             WHERE ha.san_pham_id = bt.san_pham_id
             ORDER BY (ha.gia_tri_tuy_chon_id IS NOT NULL AND ha.gia_tri_tuy_chon_id IN
                       (SELECT btg.gia_tri_tuy_chon_id FROM bien_the_gia_tri btg WHERE btg.bien_the_id = bt.id)) DESC,
                      ha.la_anh_chinh DESC, ha.thu_tu, ha.id
             LIMIT 1)
            """;

    /** Tìm nguyên văn: % _ \ trong từ khóa không phải ký tự đại diện của LIKE. */
    public static String thoatLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private HienThi() {
    }
}
