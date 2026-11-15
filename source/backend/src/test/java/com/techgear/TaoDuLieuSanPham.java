package com.techgear;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

/** Tạo nhanh danh mục, thương hiệu, sản phẩm, biến thể bằng SQL cho kiểm thử tích hợp (rollback sau mỗi test). */
public class TaoDuLieuSanPham {

    private final JdbcTemplate jdbc;

    public TaoDuLieuSanPham(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Hậu tố ngẫu nhiên để slug/tên không trùng dữ liệu khác. */
    public static String hauTo() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public long danhMuc(String ten, String slug, Long chaId, int thuTu, boolean hienThi) {
        return jdbc.queryForObject("""
                INSERT INTO danh_muc (ten, slug, danh_muc_cha_id, thu_tu, hien_thi) VALUES (?, ?, ?, ?, ?) RETURNING id
                """, Long.class, ten, slug, chaId, thuTu, hienThi);
    }

    public long thuongHieu(String ten, String slug, boolean hienThi) {
        return jdbc.queryForObject("""
                INSERT INTO thuong_hieu (ten, slug, hien_thi) VALUES (?, ?, ?) RETURNING id
                """, Long.class, ten, slug, hienThi);
    }

    /** Sản phẩm đang bật, chưa có biến thể (chưa hiển thị cho tới khi thêm biến thể). */
    public long sanPham(String ten, String slug, long danhMucId, Long thuongHieuId) {
        return jdbc.queryForObject("""
                INSERT INTO san_pham (ten, slug, danh_muc_id, thuong_hieu_id) VALUES (?, ?, ?, ?) RETURNING id
                """, Long.class, ten, slug, danhMucId, thuongHieuId);
    }

    /** Thêm biến thể đang bán và cập nhật giá hiển thị của sản phẩm = biến thể rẻ nhất. */
    public long bienThe(long sanPhamId, String sku, long giaBan, Long giaGoc, int ton) {
        long id = jdbc.queryForObject("""
                INSERT INTO bien_the (san_pham_id, sku, gia_ban, gia_goc, so_luong_ton) VALUES (?, ?, ?, ?, ?) RETURNING id
                """, Long.class, sanPhamId, sku, giaBan, giaGoc, ton);
        capNhatGiaHienThi(sanPhamId);
        return id;
    }

    public void capNhatGiaHienThi(long sanPhamId) {
        jdbc.update("""
                UPDATE san_pham sp SET gia_ban = r.gia_ban, gia_goc = r.gia_goc
                FROM (SELECT gia_ban, gia_goc FROM bien_the
                      WHERE san_pham_id = ? AND hien_thi AND deleted_at IS NULL
                      ORDER BY gia_ban, id LIMIT 1) r
                WHERE sp.id = ?
                """, sanPhamId, sanPhamId);
    }

    /** Sản phẩm hiển thị được ngay: một biến thể giá {@code gia}, còn 10 cái. */
    public long sanPhamCoBan(String ten, long danhMucId, Long thuongHieuId, long gia) {
        String slug = "sp-" + hauTo();
        long id = sanPham(ten, slug, danhMucId, thuongHieuId);
        bienThe(id, slug.toUpperCase(), gia, null, 10);
        return id;
    }

    public String slugSanPham(long id) {
        return jdbc.queryForObject("SELECT slug FROM san_pham WHERE id = ?", String.class, id);
    }

    public void sua(String sql, Object... thamSo) {
        jdbc.update(sql, thamSo);
    }
}
