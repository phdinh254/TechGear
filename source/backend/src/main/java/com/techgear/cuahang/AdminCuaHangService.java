package com.techgear.cuahang;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.techgear.chung.loi.ApiException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Quản trị banner (xóa hẳn – không bảng nào tham chiếu) và cấu hình cửa hàng (chỉ các khóa có sẵn). */
@Service
@RequiredArgsConstructor
public class AdminCuaHangService {

    private static final Set<String> KHOA_SO = Set.of(CaiDatService.PHI_VAN_CHUYEN, CaiDatService.MIEN_PHI_VAN_CHUYEN_TU);

    private final JdbcClient jdbc;

    public record BannerRequest(
            @Size(max = 255, message = "Tiêu đề tối đa 255 ký tự") String tieuDe,
            @NotBlank(message = "Vui lòng chọn ảnh banner") @Size(max = 255, message = "Đường dẫn ảnh tối đa 255 ký tự")
            String hinhAnh,
            @Size(max = 255, message = "Liên kết tối đa 255 ký tự")
            @Pattern(regexp = "(/|https?://)\\S*", message = "Liên kết phải bắt đầu bằng / hoặc http(s)://") String lienKet,
            Integer thuTu,
            Boolean hienThi,
            OffsetDateTime batDauLuc,
            OffsetDateTime ketThucLuc
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record BannerQT(Long id, String tieuDe, String hinhAnh, String lienKet, int thuTu, boolean hienThi,
                           OffsetDateTime batDauLuc, OffsetDateTime ketThucLuc) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CaiDat(String khoa, String giaTri, String moTa) {
    }

    private static final String CHON_BANNER =
            "SELECT id, tieu_de, hinh_anh, lien_ket, thu_tu, hien_thi, bat_dau_luc, ket_thuc_luc FROM banner";

    @Transactional(readOnly = true)
    public List<BannerQT> danhSachBanner() {
        return jdbc.sql(CHON_BANNER + " ORDER BY thu_tu, id").query(BannerQT.class).list();
    }

    /** {@code id} null = thêm; sửa thì {@code thuTu}, {@code hienThi} trống giữ nguyên, các trường khác thay toàn bộ. */
    @Transactional
    public BannerQT luuBanner(Long id, BannerRequest req) {
        if (req.batDauLuc() != null && req.ketThucLuc() != null && !req.ketThucLuc().isAfter(req.batDauLuc())) {
            throw ApiException.yeuCauKhongHopLe("Thời gian kết thúc phải sau thời gian bắt đầu");
        }
        BannerQT cu = id == null ? null : banner(id);
        Map<String, Object> cot = new HashMap<>();
        cot.put("tieuDe", req.tieuDe() == null || req.tieuDe().isBlank() ? null : req.tieuDe().trim());
        cot.put("hinhAnh", req.hinhAnh().trim());
        cot.put("lienKet", req.lienKet() == null || req.lienKet().isBlank() ? null : req.lienKet().trim());
        cot.put("thuTu", req.thuTu() != null ? req.thuTu() : cu == null ? 0 : cu.thuTu());
        cot.put("hienThi", req.hienThi() != null ? req.hienThi() : cu == null || cu.hienThi());
        cot.put("batDau", req.batDauLuc());
        cot.put("ketThuc", req.ketThucLuc());
        if (cu == null) {
            id = jdbc.sql("""
                            INSERT INTO banner (tieu_de, hinh_anh, lien_ket, thu_tu, hien_thi, bat_dau_luc, ket_thuc_luc)
                            VALUES (:tieuDe, :hinhAnh, :lienKet, :thuTu, :hienThi, :batDau, :ketThuc) RETURNING id
                            """)
                    .params(cot).query(Long.class).single();
        } else {
            cot.put("id", id);
            jdbc.sql("""
                            UPDATE banner SET tieu_de = :tieuDe, hinh_anh = :hinhAnh, lien_ket = :lienKet, thu_tu = :thuTu,
                                   hien_thi = :hienThi, bat_dau_luc = :batDau, ket_thuc_luc = :ketThuc, updated_at = now()
                            WHERE id = :id
                            """)
                    .params(cot).update();
        }
        return banner(id);
    }

    @Transactional
    public void xoaBanner(Long id) {
        if (jdbc.sql("DELETE FROM banner WHERE id = :id").param("id", id).update() == 0) {
            throw ApiException.khongTimThay("Không tìm thấy banner");
        }
    }

    @Transactional(readOnly = true)
    public List<CaiDat> caiDat() {
        return jdbc.sql("SELECT khoa, gia_tri, mo_ta FROM cai_dat ORDER BY khoa").query(CaiDat.class).list();
    }

    /** Chỉ sửa các khóa đã có (không thêm khóa mới – bảng này công khai); khóa tiền phải là số nguyên ≥ 0. */
    @Transactional
    public List<CaiDat> suaCaiDat(Map<String, String> giaTris) {
        Set<String> coSan = Set.copyOf(jdbc.sql("SELECT khoa FROM cai_dat").query(String.class).list());
        giaTris.forEach((khoa, giaTri) -> {
            if (!coSan.contains(khoa)) {
                throw ApiException.yeuCauKhongHopLe("Khóa cấu hình không hợp lệ: " + khoa);
            }
            if (giaTri == null || giaTri.isBlank() || giaTri.length() > 500) {
                throw ApiException.yeuCauKhongHopLe(khoa + " không được để trống (tối đa 500 ký tự)");
            }
            if (KHOA_SO.contains(khoa) && !giaTri.trim().matches("\\d{1,12}")) {
                throw ApiException.yeuCauKhongHopLe(khoa + " phải là số nguyên không âm");
            }
        });
        giaTris.forEach((khoa, giaTri) -> jdbc.sql("UPDATE cai_dat SET gia_tri = :giaTri, updated_at = now() WHERE khoa = :khoa")
                .param("giaTri", giaTri.trim()).param("khoa", khoa).update());
        return caiDat();
    }

    private BannerQT banner(Long id) {
        return jdbc.sql(CHON_BANNER + " WHERE id = :id").param("id", id).query(BannerQT.class).optional()
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy banner"));
    }
}
