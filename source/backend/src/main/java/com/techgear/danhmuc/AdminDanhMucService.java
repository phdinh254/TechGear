package com.techgear.danhmuc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.techgear.chung.SlugService;
import com.techgear.chung.loi.ApiException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Quản trị danh mục 2 cấp; xóa mềm, không xóa khi còn danh mục con hoặc sản phẩm. */
@Service
@RequiredArgsConstructor
public class AdminDanhMucService {

    private final JdbcClient jdbc;
    private final SlugService slugService;

    public record DanhMucRequest(
            @NotBlank(message = "Vui lòng nhập tên danh mục") @Size(max = 100, message = "Tên tối đa 100 ký tự") String ten,
            @Size(max = 100, message = "Slug tối đa 100 ký tự")
            @Pattern(regexp = "[a-z0-9]+(-[a-z0-9]+)*", message = "Slug chỉ gồm chữ thường không dấu, số và dấu gạch ngang")
            String slug,
            String moTa,
            @Size(max = 255, message = "Đường dẫn ảnh tối đa 255 ký tự") String hinhAnh,
            Integer thuTu,
            Boolean hienThi,
            Long danhMucChaId
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record DanhMucQT(Long id, String ten, String slug, String moTa, String hinhAnh, int thuTu, boolean hienThi,
                            Long danhMucChaId, int soDanhMucCon, int soSanPham) {
    }

    private static final String CHON = """
            SELECT dm.id, dm.ten, dm.slug, dm.mo_ta, dm.hinh_anh, dm.thu_tu, dm.hien_thi, dm.danh_muc_cha_id,
                   (SELECT count(*) FROM danh_muc c WHERE c.danh_muc_cha_id = dm.id AND c.deleted_at IS NULL)::int
                       AS so_danh_muc_con,
                   (SELECT count(*) FROM san_pham sp WHERE sp.danh_muc_id = dm.id AND sp.deleted_at IS NULL)::int
                       AS so_san_pham
            FROM danh_muc dm WHERE dm.deleted_at IS NULL
            """;

    @Transactional(readOnly = true)
    public List<DanhMucQT> danhSach() {
        return jdbc.sql(CHON + " ORDER BY dm.thu_tu, dm.id").query(DanhMucQT.class).list();
    }

    @Transactional
    public DanhMucQT tao(DanhMucRequest req) {
        kiemTraCha(req.danhMucChaId(), null);
        Long id = jdbc.sql("""
                        INSERT INTO danh_muc (ten, slug, mo_ta, hinh_anh, thu_tu, hien_thi, danh_muc_cha_id)
                        VALUES (:ten, :slug, :moTa, :hinhAnh, :thuTu, :hienThi, :cha) RETURNING id
                        """)
                .param("ten", req.ten().trim()).param("slug", slugService.chon("danh_muc", req.slug(), req.ten(), null))
                .param("moTa", rong(req.moTa())).param("hinhAnh", rong(req.hinhAnh()))
                .param("thuTu", req.thuTu() == null ? 0 : req.thuTu())
                .param("hienThi", req.hienThi() == null || req.hienThi()).param("cha", req.danhMucChaId())
                .query(Long.class).single();
        return mot(id);
    }

    /** Thay toàn bộ; {@code thuTu}, {@code hienThi} để trống thì giữ nguyên. */
    @Transactional
    public DanhMucQT sua(Long id, DanhMucRequest req) {
        DanhMucQT cu = mot(id);
        kiemTraCha(req.danhMucChaId(), cu);
        jdbc.sql("""
                        UPDATE danh_muc SET ten = :ten, slug = :slug, mo_ta = :moTa, hinh_anh = :hinhAnh, thu_tu = :thuTu,
                               hien_thi = :hienThi, danh_muc_cha_id = :cha, updated_at = now()
                        WHERE id = :id
                        """)
                .param("ten", req.ten().trim()).param("slug", slugService.chon("danh_muc", req.slug(), req.ten(), id))
                .param("moTa", rong(req.moTa())).param("hinhAnh", rong(req.hinhAnh()))
                .param("thuTu", req.thuTu() == null ? cu.thuTu() : req.thuTu())
                .param("hienThi", req.hienThi() == null ? cu.hienThi() : req.hienThi())
                .param("cha", req.danhMucChaId()).param("id", id).update();
        return mot(id);
    }

    @Transactional
    public void xoa(Long id) {
        DanhMucQT dm = mot(id);
        if (dm.soDanhMucCon() > 0) {
            throw ApiException.xungDot("Danh mục còn danh mục con");
        }
        if (dm.soSanPham() > 0) {
            throw ApiException.xungDot("Danh mục còn sản phẩm");
        }
        jdbc.sql("UPDATE danh_muc SET deleted_at = now() WHERE id = :id").param("id", id).update();
    }

    private DanhMucQT mot(Long id) {
        return jdbc.sql(CHON + " AND dm.id = :id").param("id", id).query(DanhMucQT.class).optional()
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy danh mục"));
    }

    /** Chỉ 2 cấp: cha phải là danh mục cấp 1 khác chính nó; danh mục đang có con không thành con được. */
    private void kiemTraCha(Long chaId, DanhMucQT dangSua) {
        if (chaId == null) {
            return;
        }
        boolean chaHopLe = jdbc.sql("""
                        SELECT EXISTS (SELECT 1 FROM danh_muc WHERE id = :id AND deleted_at IS NULL AND danh_muc_cha_id IS NULL)
                        """)
                .param("id", chaId).query(Boolean.class).single();
        if (!chaHopLe || (dangSua != null && (chaId.equals(dangSua.id()) || dangSua.soDanhMucCon() > 0))) {
            throw ApiException.yeuCauKhongHopLe("Danh mục cha phải là danh mục cấp 1");
        }
    }

    private static String rong(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
