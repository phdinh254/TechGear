package com.techgear.giohang;

import com.techgear.sanpham.HienThi;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class GioHangRepository {

    private final JdbcClient jdbc;

    /** Tồn kho của biến thể nếu đang bán; rỗng nếu không tồn tại hoặc đã ngừng bán. */
    Optional<Integer> tonKhoDangBan(Long bienTheId) {
        return jdbc.sql("SELECT bt.so_luong_ton " + HienThi.TU_BANG + " JOIN bien_the bt ON bt.san_pham_id = sp.id"
                        + " WHERE bt.id = :id AND " + HienThi.BIEN_THE_DANG_BAN)
                .param("id", bienTheId).query(Integer.class).optional();
    }

    Optional<Integer> soLuongTrongGio(Long nguoiDungId, Long bienTheId) {
        return jdbc.sql("SELECT so_luong FROM gio_hang WHERE nguoi_dung_id = :nd AND bien_the_id = :bt")
                .param("nd", nguoiDungId).param("bt", bienTheId).query(Integer.class).optional();
    }

    int soDong(Long nguoiDungId) {
        return jdbc.sql("SELECT count(*) FROM gio_hang WHERE nguoi_dung_id = :nd")
                .param("nd", nguoiDungId).query(Integer.class).single();
    }

    /** Thêm dòng hoặc đặt lại số lượng; clock_timestamp để dòng thêm sau đứng trước kể cả trong cùng giao dịch. */
    void datSoLuong(Long nguoiDungId, Long bienTheId, int soLuong) {
        jdbc.sql("""
                        INSERT INTO gio_hang (nguoi_dung_id, bien_the_id, so_luong, created_at, updated_at)
                        VALUES (:nd, :bt, :sl, clock_timestamp(), clock_timestamp())
                        ON CONFLICT (nguoi_dung_id, bien_the_id)
                        DO UPDATE SET so_luong = EXCLUDED.so_luong, updated_at = clock_timestamp()
                        """)
                .param("nd", nguoiDungId).param("bt", bienTheId).param("sl", soLuong).update();
    }

    void xoa(Long nguoiDungId, Long bienTheId) {
        jdbc.sql("DELETE FROM gio_hang WHERE nguoi_dung_id = :nd AND bien_the_id = :bt")
                .param("nd", nguoiDungId).param("bt", bienTheId).update();
    }

    /** Các dòng giỏ kèm giá, tồn kho hiện tại; ảnh ưu tiên ảnh gắn với giá trị tùy chọn (màu) của biến thể. */
    List<DongGioHang> dongs(Long nguoiDungId) {
        return jdbc.sql("""
                        SELECT g.bien_the_id, sp.id AS san_pham_id, sp.slug AS san_pham_slug, sp.ten,
                               (SELECT string_agg(gt.gia_tri, ' / ' ORDER BY tc.thu_tu, tc.id)
                                FROM bien_the_gia_tri btg
                                JOIN gia_tri_tuy_chon gt ON gt.id = btg.gia_tri_tuy_chon_id
                                JOIN tuy_chon tc ON tc.id = gt.tuy_chon_id
                                WHERE btg.bien_the_id = bt.id) AS phan_loai,
                               (SELECT ha.duong_dan FROM hinh_anh_san_pham ha
                                WHERE ha.san_pham_id = sp.id
                                ORDER BY (ha.gia_tri_tuy_chon_id IS NOT NULL AND ha.gia_tri_tuy_chon_id IN
                                          (SELECT btg.gia_tri_tuy_chon_id FROM bien_the_gia_tri btg
                                           WHERE btg.bien_the_id = bt.id)) DESC,
                                         ha.la_anh_chinh DESC, ha.thu_tu, ha.id
                                LIMIT 1) AS anh,
                               bt.sku, bt.gia_ban, bt.gia_goc, g.so_luong, bt.so_luong_ton AS ton_kho,
                               (""" + HienThi.BIEN_THE_DANG_BAN + """
                               ) AS con_ban
                        FROM gio_hang g
                        JOIN bien_the bt ON bt.id = g.bien_the_id
                        JOIN san_pham sp ON sp.id = bt.san_pham_id
                        JOIN danh_muc dm ON dm.id = sp.danh_muc_id
                        LEFT JOIN danh_muc dmc ON dmc.id = dm.danh_muc_cha_id
                        WHERE g.nguoi_dung_id = :nd
                        ORDER BY g.created_at DESC, g.bien_the_id DESC
                        """)
                .param("nd", nguoiDungId).query(DongGioHang.class).list();
    }
}
