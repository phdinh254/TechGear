package com.techgear.sanpham;

import com.techgear.chung.PageResponse;
import com.techgear.chung.SlugService;
import com.techgear.chung.VanBan;
import com.techgear.chung.loi.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Quản trị sản phẩm: thông tin chung + thông số, tùy chọn, ảnh, biến thể. Mọi thao tác trả lại toàn bộ sản phẩm.
 * Giá hiển thị của sản phẩm tự tính lại (biến thể đang bán rẻ nhất) sau mỗi thay đổi biến thể.
 */
@Service
@RequiredArgsConstructor
public class AdminSanPhamService {

    private final JdbcClient jdbc;
    private final SlugService slugService;
    private final SanPhamRepository sanPhamRepository;

    // ---------- Sản phẩm ----------

    @Transactional(readOnly = true)
    public PageResponse<AdminSanPhamDto.DongDanhSach> danhSach(String tuKhoa, Long danhMucId, Long thuongHieuId,
                                                               Boolean hienThi, int trang, int kichThuoc) {
        StringBuilder where = new StringBuilder(" WHERE sp.deleted_at IS NULL");
        Map<String, Object> ts = new HashMap<>();
        if (tuKhoa != null && !tuKhoa.isBlank()) {
            where.append(" AND lower(unaccent(sp.ten)) LIKE '%' || lower(unaccent(:tuKhoa)) || '%'");
            ts.put("tuKhoa", HienThi.thoatLike(tuKhoa.trim()));
        }
        if (danhMucId != null) {
            where.append(" AND sp.danh_muc_id = :dm");
            ts.put("dm", danhMucId);
        }
        if (thuongHieuId != null) {
            where.append(" AND sp.thuong_hieu_id = :th");
            ts.put("th", thuongHieuId);
        }
        if (hienThi != null) {
            where.append(" AND sp.hien_thi = :hienThi");
            ts.put("hienThi", hienThi);
        }
        long tong = jdbc.sql("SELECT count(*) FROM san_pham sp" + where).params(ts).query(Long.class).single();
        ts.put("gioiHan", kichThuoc);
        ts.put("boQua", (long) trang * kichThuoc);
        List<AdminSanPhamDto.DongDanhSach> noiDung = tong == 0 ? List.of() : jdbc.sql("""
                        SELECT sp.id, sp.ten, sp.slug,
                               (SELECT ha.duong_dan FROM hinh_anh_san_pham ha WHERE ha.san_pham_id = sp.id
                                ORDER BY ha.la_anh_chinh DESC, ha.thu_tu, ha.id LIMIT 1) AS anh_chinh,
                               dm.ten AS ten_danh_muc, th.ten AS ten_thuong_hieu, sp.gia_ban,
                               (SELECT coalesce(sum(bt.so_luong_ton), 0) FROM bien_the bt
                                WHERE bt.san_pham_id = sp.id AND bt.deleted_at IS NULL) AS tong_ton,
                               (SELECT count(*) FROM bien_the bt WHERE bt.san_pham_id = sp.id AND bt.deleted_at IS NULL)::int
                                   AS so_bien_the,
                               sp.hien_thi, sp.noi_bat, sp.luot_xem, sp.created_at AS ngay_tao
                        FROM san_pham sp JOIN danh_muc dm ON dm.id = sp.danh_muc_id
                        LEFT JOIN thuong_hieu th ON th.id = sp.thuong_hieu_id""" + where + """

                        ORDER BY sp.created_at DESC, sp.id DESC LIMIT :gioiHan OFFSET :boQua
                        """)
                .params(ts).query(AdminSanPhamDto.DongDanhSach.class).list();
        return new PageResponse<>(noiDung, trang, kichThuoc, tong, (int) ((tong + kichThuoc - 1) / kichThuoc));
    }

    private record ThongTin(Long id, String ten, String slug, Long danhMucId, Long thuongHieuId, String moTaNgan,
                            String moTa, int baoHanhThang, boolean noiBat, boolean hienThi, long giaBan, Long giaGoc) {
    }

    @Transactional(readOnly = true)
    public AdminSanPhamDto.SanPhamQT chiTiet(Long id) {
        ThongTin t = thongTin(id);
        List<AdminSanPhamDto.HinhAnhQT> hinhAnhs = jdbc.sql("""
                        SELECT id, duong_dan, mo_ta, gia_tri_tuy_chon_id, la_anh_chinh, thu_tu FROM hinh_anh_san_pham
                        WHERE san_pham_id = :id ORDER BY la_anh_chinh DESC, thu_tu, id
                        """)
                .param("id", id).query(AdminSanPhamDto.HinhAnhQT.class).list();
        List<AdminSanPhamDto.BienTheQT> bienThes = jdbc.sql("""
                        SELECT bt.id, bt.sku, bt.gia_ban, bt.gia_goc, bt.so_luong_ton, bt.nguong_canh_bao, bt.hien_thi,
                               COALESCE(array_agg(btg.gia_tri_tuy_chon_id ORDER BY btg.gia_tri_tuy_chon_id)
                                        FILTER (WHERE btg.gia_tri_tuy_chon_id IS NOT NULL), '{}'::bigint[]) AS gia_tri_ids
                        FROM bien_the bt LEFT JOIN bien_the_gia_tri btg ON btg.bien_the_id = bt.id
                        WHERE bt.san_pham_id = :id AND bt.deleted_at IS NULL
                        GROUP BY bt.id ORDER BY bt.id
                        """)
                .param("id", id)
                .query((rs, i) -> {
                    Array mang = rs.getArray("gia_tri_ids");
                    return new AdminSanPhamDto.BienTheQT(rs.getLong("id"), rs.getString("sku"), rs.getLong("gia_ban"),
                            rs.getObject("gia_goc", Long.class), rs.getInt("so_luong_ton"), rs.getInt("nguong_canh_bao"),
                            rs.getBoolean("hien_thi"), Arrays.asList((Long[]) mang.getArray()));
                }).list();
        return new AdminSanPhamDto.SanPhamQT(t.id(), t.ten(), t.slug(), t.danhMucId(), t.thuongHieuId(), t.moTaNgan(),
                t.moTa(), t.baoHanhThang(), t.noiBat(), t.hienThi(), t.giaBan(), t.giaGoc(),
                sanPhamRepository.thongSos(id), sanPhamRepository.tuyChons(id), hinhAnhs, bienThes);
    }

    @Transactional
    public AdminSanPhamDto.SanPhamQT tao(AdminSanPhamDto.SanPhamRequest req) {
        kiemTraThamChieu(req);
        Long id = jdbc.sql("""
                        INSERT INTO san_pham (ten, slug, danh_muc_id, thuong_hieu_id, mo_ta_ngan, mo_ta, bao_hanh_thang,
                                              noi_bat, hien_thi)
                        VALUES (:ten, :slug, :dm, :th, :moTaNgan, :moTa, :baoHanh, :noiBat, :hienThi) RETURNING id
                        """)
                .params(cotSanPham(req, null, null)).query(Long.class).single();
        ghiThongSo(id, req.thongSos());
        return chiTiet(id);
    }

    /** Thay toàn bộ (kể cả thông số); {@code noiBat}, {@code hienThi}, {@code baoHanhThang} trống thì giữ nguyên. */
    @Transactional
    public AdminSanPhamDto.SanPhamQT sua(Long id, AdminSanPhamDto.SanPhamRequest req) {
        ThongTin cu = thongTin(id);
        kiemTraThamChieu(req);
        jdbc.sql("""
                        UPDATE san_pham SET ten = :ten, slug = :slug, danh_muc_id = :dm, thuong_hieu_id = :th,
                               mo_ta_ngan = :moTaNgan, mo_ta = :moTa, bao_hanh_thang = :baoHanh, noi_bat = :noiBat,
                               hien_thi = :hienThi, updated_at = now()
                        WHERE id = :id
                        """)
                .params(cotSanPham(req, id, cu)).update();
        jdbc.sql("DELETE FROM thong_so WHERE san_pham_id = :id").param("id", id).update();
        ghiThongSo(id, req.thongSos());
        return chiTiet(id);
    }

    /** Xóa mềm sản phẩm và các biến thể (đơn cũ vẫn giữ ảnh chụp và tham chiếu). */
    @Transactional
    public void xoa(Long id) {
        thongTin(id);
        jdbc.sql("UPDATE bien_the SET deleted_at = now() WHERE san_pham_id = :id AND deleted_at IS NULL").param("id", id).update();
        jdbc.sql("UPDATE san_pham SET deleted_at = now() WHERE id = :id").param("id", id).update();
    }

    // ---------- Tùy chọn ----------

    @Transactional
    public AdminSanPhamDto.SanPhamQT themTuyChon(Long sanPhamId, AdminSanPhamDto.TuyChonRequest req) {
        thongTin(sanPhamId);
        kiemTraTenTuyChon(sanPhamId, req.ten(), null);
        if (coBienThe(sanPhamId)) {
            throw ApiException.xungDot("Sản phẩm đã có biến thể, xóa biến thể trước khi thêm tùy chọn");
        }
        kiemTraGiaTriTrung(req);
        Long tc = jdbc.sql("""
                        INSERT INTO tuy_chon (san_pham_id, ten, loai, thu_tu)
                        VALUES (:sp, :ten, :loai, (SELECT count(*) FROM tuy_chon WHERE san_pham_id = :sp)) RETURNING id
                        """)
                .param("sp", sanPhamId).param("ten", req.ten().trim()).param("loai", req.loai()).query(Long.class).single();
        for (int i = 0; i < req.giaTris().size(); i++) {
            themGiaTri(tc, req.giaTris().get(i), i);
        }
        return chiTiet(sanPhamId);
    }

    /** Giá trị có id là sửa, không có id là thêm, giá trị cũ không còn trong danh sách bị xóa (nếu chưa dùng ở biến thể). */
    @Transactional
    public AdminSanPhamDto.SanPhamQT suaTuyChon(Long tuyChonId, AdminSanPhamDto.TuyChonRequest req) {
        Long sanPhamId = sanPhamCuaTuyChon(tuyChonId);
        kiemTraTenTuyChon(sanPhamId, req.ten(), tuyChonId);
        kiemTraGiaTriTrung(req);
        Set<Long> cu = new HashSet<>(jdbc.sql("SELECT id FROM gia_tri_tuy_chon WHERE tuy_chon_id = :tc")
                .param("tc", tuyChonId).query(Long.class).list());
        Set<Long> giu = new HashSet<>();
        for (AdminSanPhamDto.GiaTriRequest g : req.giaTris()) {
            if (g.id() != null) {
                if (!cu.contains(g.id())) {
                    throw ApiException.yeuCauKhongHopLe("Giá trị không thuộc tùy chọn này");
                }
                giu.add(g.id());
            }
        }
        Set<Long> xoa = new HashSet<>(cu);
        xoa.removeAll(giu);
        if (!xoa.isEmpty()) {
            chanXoaGiaTriDangDung(xoa);
            jdbc.sql("DELETE FROM gia_tri_tuy_chon WHERE id IN (:ids)").param("ids", xoa).update();
        }
        jdbc.sql("UPDATE tuy_chon SET ten = :ten, loai = :loai WHERE id = :id")
                .param("ten", req.ten().trim()).param("loai", req.loai()).param("id", tuyChonId).update();
        for (int i = 0; i < req.giaTris().size(); i++) {
            AdminSanPhamDto.GiaTriRequest g = req.giaTris().get(i);
            if (g.id() == null) {
                themGiaTri(tuyChonId, g, i);
            } else {
                jdbc.sql("UPDATE gia_tri_tuy_chon SET gia_tri = :giaTri, ma_mau = :maMau, thu_tu = :thuTu WHERE id = :id")
                        .param("giaTri", g.giaTri().trim()).param("maMau", g.maMau()).param("thuTu", i)
                        .param("id", g.id()).update();
            }
        }
        return chiTiet(sanPhamId);
    }

    @Transactional
    public AdminSanPhamDto.SanPhamQT xoaTuyChon(Long tuyChonId) {
        Long sanPhamId = sanPhamCuaTuyChon(tuyChonId);
        chanXoaGiaTriDangDung(new HashSet<>(jdbc.sql("SELECT id FROM gia_tri_tuy_chon WHERE tuy_chon_id = :tc")
                .param("tc", tuyChonId).query(Long.class).list()));
        jdbc.sql("DELETE FROM tuy_chon WHERE id = :id").param("id", tuyChonId).update();
        return chiTiet(sanPhamId);
    }

    // ---------- Ảnh ----------

    @Transactional
    public AdminSanPhamDto.SanPhamQT themAnh(Long sanPhamId, AdminSanPhamDto.HinhAnhRequest req) {
        thongTin(sanPhamId);
        kiemTraGiaTriCuaSanPham(sanPhamId, req.giaTriTuyChonId());
        if (Boolean.TRUE.equals(req.laAnhChinh())) {
            boAnhChinh(sanPhamId);
        }
        jdbc.sql("""
                        INSERT INTO hinh_anh_san_pham (san_pham_id, gia_tri_tuy_chon_id, duong_dan, mo_ta, thu_tu, la_anh_chinh)
                        VALUES (:sp, :gt, :duongDan, :moTa,
                                coalesce(:thuTu, (SELECT coalesce(max(thu_tu) + 1, 0) FROM hinh_anh_san_pham WHERE san_pham_id = :sp)),
                                :chinh)
                        """)
                .params(cotAnh(sanPhamId, req)).update();
        return chiTiet(sanPhamId);
    }

    @Transactional
    public AdminSanPhamDto.SanPhamQT suaAnh(Long anhId, AdminSanPhamDto.HinhAnhRequest req) {
        Long sanPhamId = sanPhamCuaAnh(anhId);
        kiemTraGiaTriCuaSanPham(sanPhamId, req.giaTriTuyChonId());
        if (Boolean.TRUE.equals(req.laAnhChinh())) {
            boAnhChinh(sanPhamId);
        }
        Map<String, Object> cot = cotAnh(sanPhamId, req);
        cot.put("id", anhId);
        jdbc.sql("""
                        UPDATE hinh_anh_san_pham SET gia_tri_tuy_chon_id = :gt, duong_dan = :duongDan, mo_ta = :moTa,
                               thu_tu = coalesce(:thuTu, thu_tu), la_anh_chinh = :chinh
                        WHERE id = :id
                        """)
                .params(cot).update();
        return chiTiet(sanPhamId);
    }

    @Transactional
    public AdminSanPhamDto.SanPhamQT xoaAnh(Long anhId) {
        Long sanPhamId = sanPhamCuaAnh(anhId);
        jdbc.sql("DELETE FROM hinh_anh_san_pham WHERE id = :id").param("id", anhId).update();
        return chiTiet(sanPhamId);
    }

    // ---------- Biến thể ----------

    @Transactional
    public AdminSanPhamDto.SanPhamQT themBienThe(Long sanPhamId, AdminSanPhamDto.BienTheRequest req,
                                                 Long nguoiThucHienId) {
        thongTin(sanPhamId);
        String sku = chuanHoaSku(req.sku());
        kiemTraGiaVaSku(req, sku, null);
        List<Long> toHop = kiemTraToHop(sanPhamId, req.giaTriIds() == null ? List.of() : req.giaTriIds());
        int ton = req.soLuongTon() == null ? 0 : req.soLuongTon();
        Long id = jdbc.sql("""
                        INSERT INTO bien_the (san_pham_id, sku, gia_ban, gia_goc, so_luong_ton, nguong_canh_bao, hien_thi)
                        VALUES (:sp, :sku, :giaBan, :giaGoc, :ton, :nguong, :hienThi) RETURNING id
                        """)
                .param("sp", sanPhamId).param("sku", sku).param("giaBan", req.giaBan()).param("giaGoc", req.giaGoc())
                .param("ton", ton).param("nguong", req.nguongCanhBao() == null ? 5 : req.nguongCanhBao())
                .param("hienThi", req.hienThi() == null || req.hienThi()).query(Long.class).single();
        for (Long gt : toHop) {
            jdbc.sql("INSERT INTO bien_the_gia_tri (bien_the_id, gia_tri_tuy_chon_id) VALUES (:bt, :gt)")
                    .param("bt", id).param("gt", gt).update();
        }
        if (ton > 0) {
            ghiLichSuKho(id, "khoi_tao", ton, ton, null, nguoiThucHienId);
        }
        capNhatGiaHienThi(sanPhamId);
        return chiTiet(sanPhamId);
    }

    /** Sửa SKU, giá, ngưỡng, ẩn/hiện; tổ hợp giá trị và tồn kho không đổi ở đây. */
    @Transactional
    public AdminSanPhamDto.SanPhamQT suaBienThe(Long bienTheId, AdminSanPhamDto.BienTheRequest req) {
        Long sanPhamId = sanPhamCuaBienThe(bienTheId);
        String sku = chuanHoaSku(req.sku());
        kiemTraGiaVaSku(req, sku, bienTheId);
        jdbc.sql("""
                        UPDATE bien_the SET sku = :sku, gia_ban = :giaBan, gia_goc = :giaGoc,
                               nguong_canh_bao = coalesce(:nguong, nguong_canh_bao),
                               hien_thi = coalesce(:hienThi, hien_thi), updated_at = now()
                        WHERE id = :id
                        """)
                .param("sku", sku).param("giaBan", req.giaBan()).param("giaGoc", req.giaGoc())
                .param("nguong", req.nguongCanhBao()).param("hienThi", req.hienThi()).param("id", bienTheId).update();
        capNhatGiaHienThi(sanPhamId);
        return chiTiet(sanPhamId);
    }

    @Transactional
    public AdminSanPhamDto.SanPhamQT xoaBienThe(Long bienTheId) {
        Long sanPhamId = sanPhamCuaBienThe(bienTheId);
        jdbc.sql("UPDATE bien_the SET deleted_at = now() WHERE id = :id").param("id", bienTheId).update();
        capNhatGiaHienThi(sanPhamId);
        return chiTiet(sanPhamId);
    }

    // ---------- Dùng chung (cả module kho) ----------

    void ghiLichSuKho(Long bienTheId, String loai, int thayDoi, int sau, String ghiChu, Long nguoiThucHienId) {
        jdbc.sql("""
                        INSERT INTO lich_su_ton_kho (bien_the_id, loai, so_luong_thay_doi, so_luong_sau, ghi_chu,
                                                     nguoi_thuc_hien_id, created_at)
                        VALUES (:bt, :loai, :thayDoi, :sau, :ghiChu, :nguoi, clock_timestamp())
                        """)
                .param("bt", bienTheId).param("loai", loai).param("thayDoi", thayDoi).param("sau", sau)
                .param("ghiChu", ghiChu).param("nguoi", nguoiThucHienId).update();
    }

    /** Giá hiển thị = biến thể đang bật, chưa xóa, rẻ nhất; không còn biến thể nào thì 0. */
    private void capNhatGiaHienThi(Long sanPhamId) {
        jdbc.sql("""
                        UPDATE san_pham SET
                            gia_ban = coalesce((SELECT gia_ban FROM bien_the WHERE san_pham_id = :id AND hien_thi
                                                AND deleted_at IS NULL ORDER BY gia_ban, id LIMIT 1), 0),
                            gia_goc = (SELECT gia_goc FROM bien_the WHERE san_pham_id = :id AND hien_thi
                                       AND deleted_at IS NULL ORDER BY gia_ban, id LIMIT 1),
                            updated_at = now()
                        WHERE id = :id
                        """)
                .param("id", sanPhamId).update();
    }

    private ThongTin thongTin(Long id) {
        return jdbc.sql("""
                        SELECT id, ten, slug, danh_muc_id, thuong_hieu_id, mo_ta_ngan, mo_ta, bao_hanh_thang, noi_bat,
                               hien_thi, gia_ban, gia_goc
                        FROM san_pham WHERE id = :id AND deleted_at IS NULL
                        """)
                .param("id", id).query(ThongTin.class).optional()
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy sản phẩm"));
    }

    private void kiemTraThamChieu(AdminSanPhamDto.SanPhamRequest req) {
        if (!tonTai("danh_muc", req.danhMucId())) {
            throw ApiException.yeuCauKhongHopLe("Danh mục không tồn tại");
        }
        if (req.thuongHieuId() != null && !tonTai("thuong_hieu", req.thuongHieuId())) {
            throw ApiException.yeuCauKhongHopLe("Thương hiệu không tồn tại");
        }
    }

    private boolean tonTai(String bang, Long id) {
        return jdbc.sql("SELECT EXISTS (SELECT 1 FROM " + bang + " WHERE id = :id AND deleted_at IS NULL)")
                .param("id", id).query(Boolean.class).single();
    }

    private Map<String, Object> cotSanPham(AdminSanPhamDto.SanPhamRequest req, Long id, ThongTin cu) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("ten", req.ten().trim());
        m.put("slug", slugService.chon("san_pham", req.slug(), req.ten(), id));
        m.put("dm", req.danhMucId());
        m.put("th", req.thuongHieuId());
        m.put("moTaNgan", req.moTaNgan() == null || req.moTaNgan().isBlank() ? null : req.moTaNgan().trim());
        m.put("moTa", VanBan.locHtml(req.moTa()));
        m.put("baoHanh", req.baoHanhThang() != null ? req.baoHanhThang() : cu == null ? 0 : cu.baoHanhThang());
        m.put("noiBat", req.noiBat() != null ? req.noiBat() : cu != null && cu.noiBat());
        m.put("hienThi", req.hienThi() != null ? req.hienThi() : cu == null || cu.hienThi());
        return m;
    }

    private void ghiThongSo(Long sanPhamId, List<AdminSanPhamDto.ThongSoRequest> thongSos) {
        if (thongSos == null) {
            return;
        }
        for (int i = 0; i < thongSos.size(); i++) {
            jdbc.sql("INSERT INTO thong_so (san_pham_id, ten, gia_tri, thu_tu) VALUES (:sp, :ten, :giaTri, :thuTu)")
                    .param("sp", sanPhamId).param("ten", thongSos.get(i).ten().trim())
                    .param("giaTri", thongSos.get(i).giaTri().trim()).param("thuTu", i).update();
        }
    }

    private boolean coBienThe(Long sanPhamId) {
        return jdbc.sql("SELECT EXISTS (SELECT 1 FROM bien_the WHERE san_pham_id = :sp AND deleted_at IS NULL)")
                .param("sp", sanPhamId).query(Boolean.class).single();
    }

    private void kiemTraTenTuyChon(Long sanPhamId, String ten, Long boQua) {
        boolean trung = jdbc.sql("""
                        SELECT EXISTS (SELECT 1 FROM tuy_chon WHERE san_pham_id = :sp AND lower(ten) = lower(:ten) AND id <> :id)
                        """)
                .param("sp", sanPhamId).param("ten", ten.trim()).param("id", boQua == null ? -1L : boQua)
                .query(Boolean.class).single();
        if (trung) {
            throw ApiException.xungDot("Tùy chọn đã tồn tại");
        }
    }

    private static void kiemTraGiaTriTrung(AdminSanPhamDto.TuyChonRequest req) {
        Set<String> da = new HashSet<>();
        for (AdminSanPhamDto.GiaTriRequest g : req.giaTris()) {
            if (!da.add(g.giaTri().trim().toLowerCase(Locale.ROOT))) {
                throw ApiException.yeuCauKhongHopLe("Giá trị \"" + g.giaTri().trim() + "\" bị trùng");
            }
        }
    }

    private void themGiaTri(Long tuyChonId, AdminSanPhamDto.GiaTriRequest g, int thuTu) {
        jdbc.sql("INSERT INTO gia_tri_tuy_chon (tuy_chon_id, gia_tri, ma_mau, thu_tu) VALUES (:tc, :giaTri, :maMau, :thuTu)")
                .param("tc", tuyChonId).param("giaTri", g.giaTri().trim()).param("maMau", g.maMau())
                .param("thuTu", thuTu).update();
    }

    private void chanXoaGiaTriDangDung(Set<Long> giaTriIds) {
        if (giaTriIds.isEmpty()) {
            return;
        }
        Optional<String> dangDung = jdbc.sql("""
                        SELECT gt.gia_tri FROM gia_tri_tuy_chon gt
                        JOIN bien_the_gia_tri btg ON btg.gia_tri_tuy_chon_id = gt.id
                        JOIN bien_the bt ON bt.id = btg.bien_the_id AND bt.deleted_at IS NULL
                        WHERE gt.id IN (:ids) ORDER BY gt.thu_tu, gt.id LIMIT 1
                        """)
                .param("ids", giaTriIds).query(String.class).optional();
        if (dangDung.isPresent()) {
            throw ApiException.xungDot("Giá trị \"" + dangDung.get() + "\" đang được dùng ở biến thể");
        }
    }

    private Long sanPhamCuaTuyChon(Long tuyChonId) {
        return jdbc.sql("""
                        SELECT tc.san_pham_id FROM tuy_chon tc JOIN san_pham sp ON sp.id = tc.san_pham_id
                        WHERE tc.id = :id AND sp.deleted_at IS NULL
                        """)
                .param("id", tuyChonId).query(Long.class).optional()
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy tùy chọn"));
    }

    private Long sanPhamCuaAnh(Long anhId) {
        return jdbc.sql("""
                        SELECT ha.san_pham_id FROM hinh_anh_san_pham ha JOIN san_pham sp ON sp.id = ha.san_pham_id
                        WHERE ha.id = :id AND sp.deleted_at IS NULL
                        """)
                .param("id", anhId).query(Long.class).optional()
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy ảnh"));
    }

    Long sanPhamCuaBienThe(Long bienTheId) {
        return jdbc.sql("""
                        SELECT bt.san_pham_id FROM bien_the bt JOIN san_pham sp ON sp.id = bt.san_pham_id
                        WHERE bt.id = :id AND bt.deleted_at IS NULL AND sp.deleted_at IS NULL
                        """)
                .param("id", bienTheId).query(Long.class).optional()
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy biến thể"));
    }

    private void kiemTraGiaTriCuaSanPham(Long sanPhamId, Long giaTriId) {
        if (giaTriId == null) {
            return;
        }
        boolean thuoc = jdbc.sql("""
                        SELECT EXISTS (SELECT 1 FROM gia_tri_tuy_chon gt JOIN tuy_chon tc ON tc.id = gt.tuy_chon_id
                                       WHERE gt.id = :gt AND tc.san_pham_id = :sp)
                        """)
                .param("gt", giaTriId).param("sp", sanPhamId).query(Boolean.class).single();
        if (!thuoc) {
            throw ApiException.yeuCauKhongHopLe("Giá trị tùy chọn không thuộc sản phẩm này");
        }
    }

    private void boAnhChinh(Long sanPhamId) {
        jdbc.sql("UPDATE hinh_anh_san_pham SET la_anh_chinh = FALSE WHERE san_pham_id = :sp AND la_anh_chinh")
                .param("sp", sanPhamId).update();
    }

    private static Map<String, Object> cotAnh(Long sanPhamId, AdminSanPhamDto.HinhAnhRequest req) {
        Map<String, Object> m = new HashMap<>();
        m.put("sp", sanPhamId);
        m.put("gt", req.giaTriTuyChonId());
        m.put("duongDan", req.duongDan().trim());
        m.put("moTa", req.moTa() == null || req.moTa().isBlank() ? null : req.moTa().trim());
        m.put("thuTu", req.thuTu());
        m.put("chinh", Boolean.TRUE.equals(req.laAnhChinh()));
        return m;
    }

    /** Đúng một giá trị cho mỗi tùy chọn của sản phẩm, và chưa có biến thể (chưa xóa) cùng tổ hợp; trả tổ hợp đã sắp xếp. */
    private List<Long> kiemTraToHop(Long sanPhamId, List<Long> giaTriIds) {
        Map<Long, Long> tuyChonCuaGiaTri = new HashMap<>();
        jdbc.sql("""
                        SELECT gt.id, gt.tuy_chon_id FROM gia_tri_tuy_chon gt JOIN tuy_chon tc ON tc.id = gt.tuy_chon_id
                        WHERE tc.san_pham_id = :sp
                        """)
                .param("sp", sanPhamId).query(rs -> {
                    tuyChonCuaGiaTri.put(rs.getLong("id"), rs.getLong("tuy_chon_id"));
                });
        int soTuyChon = jdbc.sql("SELECT count(*) FROM tuy_chon WHERE san_pham_id = :sp")
                .param("sp", sanPhamId).query(Integer.class).single();
        Set<Long> tuyChonDaChon = new HashSet<>();
        for (Long gt : giaTriIds) {
            Long tc = tuyChonCuaGiaTri.get(gt);
            if (tc == null || !tuyChonDaChon.add(tc)) {
                throw ApiException.yeuCauKhongHopLe("Mỗi tùy chọn phải chọn đúng một giá trị");
            }
        }
        if (tuyChonDaChon.size() != soTuyChon) {
            throw ApiException.yeuCauKhongHopLe("Mỗi tùy chọn phải chọn đúng một giá trị");
        }
        List<Long> toHop = giaTriIds.stream().sorted().toList();
        boolean trung = jdbc.sql("""
                        SELECT EXISTS (
                            SELECT 1 FROM bien_the bt
                            WHERE bt.san_pham_id = :sp AND bt.deleted_at IS NULL
                              AND coalesce((SELECT array_agg(btg.gia_tri_tuy_chon_id ORDER BY btg.gia_tri_tuy_chon_id)
                                            FROM bien_the_gia_tri btg WHERE btg.bien_the_id = bt.id), '{}'::bigint[])
                                  = CAST(:toHop AS bigint[]))
                        """)
                .param("sp", sanPhamId)
                .param("toHop", "{" + String.join(",", toHop.stream().map(String::valueOf).toList()) + "}")
                .query(Boolean.class).single();
        if (trung) {
            throw ApiException.xungDot("Đã có biến thể với tổ hợp này");
        }
        return toHop;
    }

    private void kiemTraGiaVaSku(AdminSanPhamDto.BienTheRequest req, String sku, Long boQua) {
        if (req.giaGoc() != null && req.giaGoc() < req.giaBan()) {
            throw ApiException.yeuCauKhongHopLe("Giá gốc phải lớn hơn hoặc bằng giá bán");
        }
        boolean trung = jdbc.sql("SELECT EXISTS (SELECT 1 FROM bien_the WHERE sku = :sku AND deleted_at IS NULL AND id <> :id)")
                .param("sku", sku).param("id", boQua == null ? -1L : boQua).query(Boolean.class).single();
        if (trung) {
            throw ApiException.xungDot("SKU đã được dùng");
        }
    }

    private static String chuanHoaSku(String sku) {
        return sku.trim().toUpperCase(Locale.ROOT);
    }
}
