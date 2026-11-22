package com.techgear.thuonghieu;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.techgear.chung.Csdl;
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

@Service
@RequiredArgsConstructor
class AdminThuongHieuService {

    private final JdbcClient jdbc;
    private final SlugService slugService;

    record ThuongHieuRequest(
            @NotBlank(message = "Vui lòng nhập tên thương hiệu") @Size(max = 100, message = "Tên tối đa 100 ký tự") String ten,
            @Size(max = 100, message = "Slug tối đa 100 ký tự")
            @Pattern(regexp = "[a-z0-9]+(-[a-z0-9]+)*", message = "Slug chỉ gồm chữ thường không dấu, số và dấu gạch ngang")
            String slug,
            @Size(max = 255, message = "Đường dẫn logo tối đa 255 ký tự") String logo,
            String moTa,
            Boolean hienThi
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record ThuongHieuQT(Long id, String ten, String slug, String logo, String moTa, boolean hienThi, int soSanPham) {
    }

    private static final String CHON = """
            SELECT th.id, th.ten, th.slug, th.logo, th.mo_ta, th.hien_thi,
                   (SELECT count(*) FROM san_pham sp WHERE sp.thuong_hieu_id = th.id AND sp.deleted_at IS NULL)
                       AS so_san_pham
            FROM thuong_hieu th WHERE th.deleted_at IS NULL
            """;

    @Transactional(readOnly = true)
    List<ThuongHieuQT> danhSach() {
        return jdbc.sql(CHON + " ORDER BY th.ten, th.id").query(ThuongHieuQT.class).list();
    }

    /** {@code id} null = thêm mới; sửa thì {@code hienThi} để trống giữ nguyên. */
    @Transactional
    ThuongHieuQT luu(Long id, ThuongHieuRequest req) {
        ThuongHieuQT cu = id == null ? null : mot(id);
        String slug = slugService.chon("thuong_hieu", req.slug(), req.ten(), id);
        String logo = req.logo() == null || req.logo().isBlank() ? null : req.logo().trim();
        String moTa = req.moTa() == null || req.moTa().isBlank() ? null : req.moTa().trim();
        boolean hienThi = req.hienThi() != null ? req.hienThi() : cu == null || cu.hienThi();
        if (cu == null) {
            id = Csdl.themLayId(jdbc.sql("""
                            INSERT INTO thuong_hieu (ten, slug, logo, mo_ta, hien_thi)
                            VALUES (:ten, :slug, :logo, :moTa, :hienThi)
                            """)
                    .param("ten", req.ten().trim()).param("slug", slug).param("logo", logo).param("moTa", moTa)
                    .param("hienThi", hienThi));
        } else {
            jdbc.sql("""
                            UPDATE thuong_hieu SET ten = :ten, slug = :slug, logo = :logo, mo_ta = :moTa,
                                   hien_thi = :hienThi, updated_at = now()
                            WHERE id = :id
                            """)
                    .param("ten", req.ten().trim()).param("slug", slug).param("logo", logo).param("moTa", moTa)
                    .param("hienThi", hienThi).param("id", id).update();
        }
        return mot(id);
    }

    @Transactional
    void xoa(Long id) {
        if (mot(id).soSanPham() > 0) {
            throw ApiException.xungDot("Thương hiệu còn sản phẩm");
        }
        jdbc.sql("UPDATE thuong_hieu SET deleted_at = now() WHERE id = :id").param("id", id).update();
    }

    private ThuongHieuQT mot(Long id) {
        return jdbc.sql(CHON + " AND th.id = :id").param("id", id).query(ThuongHieuQT.class).optional()
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy thương hiệu"));
    }
}
