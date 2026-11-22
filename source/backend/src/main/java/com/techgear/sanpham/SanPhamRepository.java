package com.techgear.sanpham;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Truy vấn đọc sản phẩm cho phía khách (SQL thuần: tìm không dấu nhờ collation utf8mb4_0900_ai_ci, mỗi trang 2 truy vấn). */
@Repository
@RequiredArgsConstructor
class SanPhamRepository {

    private final JdbcClient jdbc;

    private static final String TU_BANG = HienThi.TU_BANG;
    private static final String DANG_HIEN_THI = HienThi.SAN_PHAM_DANG_HIEN_THI;

    private static final String COT_THE = """
            SELECT sp.id, sp.ten, sp.slug, sp.gia_ban, sp.gia_goc, th.ten AS ten_thuong_hieu,
                   (SELECT ha.duong_dan FROM hinh_anh_san_pham ha WHERE ha.san_pham_id = sp.id
                    ORDER BY ha.la_anh_chinh DESC, ha.thu_tu, ha.id LIMIT 1) AS anh_chinh,
                   EXISTS (SELECT 1 FROM bien_the bt WHERE bt.san_pham_id = sp.id AND bt.hien_thi
                           AND bt.deleted_at IS NULL AND bt.so_luong_ton > 0) AS con_hang
            """;

    record DieuKien(List<String> tuKhoas, String danhMuc, List<String> thuongHieus, Long giaTu, Long giaDen) {
    }

    record TrangKetQua(List<SanPhamTomTat> noiDung, long tongSoPhanTu) {
    }

    TrangKetQua tim(DieuKien dk, SapXep sapXep, int trang, int kichThuoc) {
        Map<String, Object> thamSo = new HashMap<>();
        StringBuilder where = new StringBuilder(" WHERE ").append(DANG_HIEN_THI);
        for (int i = 0; i < dk.tuKhoas().size(); i++) {
            where.append(" AND CONCAT(sp.ten, ' ', coalesce(th.ten, '')) LIKE CONCAT('%', :tu")
                    .append(i).append(", '%')");
            thamSo.put("tu" + i, HienThi.thoatLike(dk.tuKhoas().get(i)));
        }
        if (dk.danhMuc() != null) {
            where.append(" AND (dm.slug = :danhMuc OR dmc.slug = :danhMuc)");
            thamSo.put("danhMuc", dk.danhMuc());
        }
        if (!dk.thuongHieus().isEmpty()) {
            where.append(" AND th.slug IN (:thuongHieus)");
            thamSo.put("thuongHieus", dk.thuongHieus());
        }
        if (dk.giaTu() != null || dk.giaDen() != null) {
            where.append(" AND EXISTS (SELECT 1 FROM bien_the bg WHERE bg.san_pham_id = sp.id AND bg.hien_thi")
                    .append(" AND bg.deleted_at IS NULL AND bg.gia_ban BETWEEN :giaTu AND :giaDen)");
            thamSo.put("giaTu", dk.giaTu() == null ? 0L : dk.giaTu());
            thamSo.put("giaDen", dk.giaDen() == null ? Long.MAX_VALUE : dk.giaDen());
        }

        long tong = jdbc.sql("SELECT count(*) " + TU_BANG + where).params(thamSo).query(Long.class).single();
        if (tong == 0) {
            return new TrangKetQua(List.of(), 0);
        }
        thamSo.put("gioiHan", kichThuoc);
        thamSo.put("boQua", (long) trang * kichThuoc);
        List<SanPhamTomTat> noiDung = jdbc.sql(COT_THE + TU_BANG + where
                        + " ORDER BY " + sapXep.orderBy + " LIMIT :gioiHan OFFSET :boQua")
                .params(thamSo).query(SanPhamTomTat.class).list();
        return new TrangKetQua(noiDung, tong);
    }

    List<SanPhamTomTat> noiBat(int gioiHan) {
        return jdbc.sql(COT_THE + TU_BANG + " WHERE " + DANG_HIEN_THI
                        + " AND sp.noi_bat ORDER BY sp.luot_xem DESC, sp.id DESC LIMIT :gioiHan")
                .param("gioiHan", gioiHan).query(SanPhamTomTat.class).list();
    }

    List<SanPhamTomTat> theoIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return jdbc.sql(COT_THE + TU_BANG + " WHERE " + DANG_HIEN_THI + " AND sp.id IN (:ids)")
                .param("ids", ids).query(SanPhamTomTat.class).list();
    }

    boolean dangHienThi(Long id) {
        return jdbc.sql("SELECT EXISTS (SELECT 1 " + TU_BANG + " WHERE " + DANG_HIEN_THI + " AND sp.id = :id)")
                .param("id", id).query(Boolean.class).single();
    }

    record ThongTinChung(Long id, String ten, String slug, String moTaNgan, String moTa, long giaBan, Long giaGoc,
                         int baoHanhThang, Long dmId, String dmTen, String dmSlug, Long thId, String thTen, String thSlug,
                         long soDanhGia, Double diemTrungBinh) {
    }

    Optional<ThongTinChung> timTheoSlug(String slug) {
        return jdbc.sql("""
                        SELECT sp.id, sp.ten, sp.slug, sp.mo_ta_ngan, sp.mo_ta, sp.gia_ban, sp.gia_goc, sp.bao_hanh_thang,
                               dm.id AS dm_id, dm.ten AS dm_ten, dm.slug AS dm_slug,
                               th.id AS th_id, th.ten AS th_ten, th.slug AS th_slug,
                               (SELECT count(*) FROM danh_gia dg WHERE dg.san_pham_id = sp.id
                                AND dg.trang_thai = 'da_duyet') AS so_danh_gia,
                               (SELECT CAST(round(avg(dg.so_sao), 1) AS DOUBLE) FROM danh_gia dg WHERE dg.san_pham_id = sp.id
                                AND dg.trang_thai = 'da_duyet') AS diem_trung_binh
                        """ + TU_BANG + " WHERE " + DANG_HIEN_THI + " AND sp.slug = :slug")
                .param("slug", slug).query(ThongTinChung.class).optional();
    }

    void tangLuotXem(Long id) {
        jdbc.sql("UPDATE san_pham SET luot_xem = luot_xem + 1 WHERE id = :id").param("id", id).update();
    }

    /** Ảnh chính đứng đầu, rồi theo thứ tự. */
    List<SanPhamChiTiet.HinhAnh> hinhAnhs(Long sanPhamId) {
        return jdbc.sql("""
                        SELECT id, duong_dan, mo_ta, gia_tri_tuy_chon_id, la_anh_chinh FROM hinh_anh_san_pham
                        WHERE san_pham_id = :id ORDER BY la_anh_chinh DESC, thu_tu, id
                        """)
                .param("id", sanPhamId).query(SanPhamChiTiet.HinhAnh.class).list();
    }

    List<SanPhamChiTiet.ThongSo> thongSos(Long sanPhamId) {
        return jdbc.sql("SELECT ten, gia_tri FROM thong_so WHERE san_pham_id = :id ORDER BY thu_tu, id")
                .param("id", sanPhamId).query(SanPhamChiTiet.ThongSo.class).list();
    }

    List<SanPhamChiTiet.TuyChon> tuyChons(Long sanPhamId) {
        Map<Long, SanPhamChiTiet.TuyChon> theoId = new LinkedHashMap<>();
        jdbc.sql("""
                        SELECT tc.id AS tc_id, tc.ten AS tc_ten, tc.loai, gt.id AS gt_id, gt.gia_tri, gt.ma_mau
                        FROM tuy_chon tc LEFT JOIN gia_tri_tuy_chon gt ON gt.tuy_chon_id = tc.id
                        WHERE tc.san_pham_id = :id
                        ORDER BY tc.thu_tu, tc.id, gt.thu_tu, gt.id
                        """)
                .param("id", sanPhamId)
                .query(rs -> {
                    long tcId = rs.getLong("tc_id");
                    SanPhamChiTiet.TuyChon tc = theoId.get(tcId);
                    if (tc == null) {
                        tc = new SanPhamChiTiet.TuyChon(tcId, rs.getString("tc_ten"), rs.getString("loai"), new ArrayList<>());
                        theoId.put(tcId, tc);
                    }
                    Long gtId = rs.getObject("gt_id", Long.class);
                    if (gtId != null) {
                        tc.giaTris().add(new SanPhamChiTiet.GiaTri(gtId, rs.getString("gia_tri"), rs.getString("ma_mau")));
                    }
                });
        return List.copyOf(theoId.values());
    }

    /** Chỉ biến thể đang bán; giaTriIds = các giá trị tùy chọn tạo nên biến thể. */
    List<SanPhamChiTiet.BienThe> bienThes(Long sanPhamId) {
        return jdbc.sql("""
                        SELECT bt.id, bt.sku, bt.gia_ban, bt.gia_goc, bt.so_luong_ton,
                               GROUP_CONCAT(btg.gia_tri_tuy_chon_id ORDER BY btg.gia_tri_tuy_chon_id) AS gia_tri_ids
                        FROM bien_the bt LEFT JOIN bien_the_gia_tri btg ON btg.bien_the_id = bt.id
                        WHERE bt.san_pham_id = :id AND bt.hien_thi AND bt.deleted_at IS NULL
                        GROUP BY bt.id
                        ORDER BY bt.id
                        """)
                .param("id", sanPhamId)
                .query((rs, i) -> new SanPhamChiTiet.BienThe(rs.getLong("id"), rs.getString("sku"), rs.getLong("gia_ban"),
                        rs.getObject("gia_goc", Long.class), rs.getInt("so_luong_ton"), danhSachId(rs.getString("gia_tri_ids"))))
                .list();
    }

    /** Chuỗi id GROUP_CONCAT ("3,7") thành danh sách; NULL (không có dòng nào) thành danh sách rỗng. */
    static List<Long> danhSachId(String chuoi) {
        return chuoi == null ? List.of() : Arrays.stream(chuoi.split(",")).map(Long::valueOf).toList();
    }
}
