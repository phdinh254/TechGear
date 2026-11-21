package com.techgear.khuyenmai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.techgear.chung.PageResponse;
import com.techgear.chung.loi.ApiException;
import com.techgear.sanpham.HienThi;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Quản trị mã giảm giá; mã lưu chữ hoa, xóa mềm (đơn cũ vẫn tham chiếu). */
@Service
@RequiredArgsConstructor
public class AdminMaGiamGiaService {

    private final JdbcClient jdbc;

    public record MaGiamGiaRequest(
            @NotBlank(message = "Vui lòng nhập mã") @Size(max = 50, message = "Mã tối đa 50 ký tự")
            @Pattern(regexp = "[A-Za-z0-9_-]+", message = "Mã chỉ gồm chữ không dấu, số, _ và -") String ma,
            @Size(max = 255, message = "Mô tả tối đa 255 ký tự") String moTa,
            @NotNull(message = "Vui lòng chọn loại giảm")
            @Pattern(regexp = "phan_tram|so_tien", message = "Loại giảm là phan_tram hoặc so_tien") String loaiGiam,
            @NotNull(message = "Vui lòng nhập giá trị giảm") @Positive(message = "Giá trị giảm phải lớn hơn 0") Long giaTriGiam,
            @PositiveOrZero(message = "Đơn tối thiểu không được âm") Long donToiThieu,
            @Positive(message = "Giảm tối đa phải lớn hơn 0") Long giamToiDa,
            @Min(value = 1, message = "Tổng lượt dùng phải từ 1") Integer tongLuotDung,
            @Min(value = 1, message = "Lượt mỗi người phải từ 1") Integer luotDungMoiNguoi,
            @NotNull(message = "Vui lòng nhập thời gian bắt đầu") OffsetDateTime batDauLuc,
            @NotNull(message = "Vui lòng nhập thời gian kết thúc") OffsetDateTime ketThucLuc,
            Boolean kichHoat
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MaGiamGiaQT(Long id, String ma, String moTa, String loaiGiam, long giaTriGiam, long donToiThieu,
                              Long giamToiDa, Integer tongLuotDung, Integer luotDungMoiNguoi, int daDung,
                              OffsetDateTime batDauLuc, OffsetDateTime ketThucLuc, boolean kichHoat) {
    }

    private static final String CHON = """
            SELECT id, ma, mo_ta, loai_giam, gia_tri_giam, don_toi_thieu, giam_toi_da, tong_luot_dung, luot_dung_moi_nguoi,
                   da_dung, bat_dau_luc, ket_thuc_luc, kich_hoat
            FROM ma_giam_gia WHERE deleted_at IS NULL
            """;

    @Transactional(readOnly = true)
    public PageResponse<MaGiamGiaQT> danhSach(String tuKhoa, Boolean kichHoat, int trang, int kichThuoc) {
        StringBuilder where = new StringBuilder();
        Map<String, Object> ts = new HashMap<>();
        if (tuKhoa != null && !tuKhoa.isBlank()) {
            where.append(" AND (upper(ma) LIKE '%' || upper(:tuKhoa) || '%' OR lower(unaccent(coalesce(mo_ta, '')))")
                    .append(" LIKE '%' || lower(unaccent(:tuKhoa)) || '%')");
            ts.put("tuKhoa", HienThi.thoatLike(tuKhoa.trim()));
        }
        if (kichHoat != null) {
            where.append(" AND kich_hoat = :kichHoat");
            ts.put("kichHoat", kichHoat);
        }
        long tong = jdbc.sql("SELECT count(*) FROM ma_giam_gia WHERE deleted_at IS NULL" + where)
                .params(ts).query(Long.class).single();
        ts.put("gioiHan", kichThuoc);
        ts.put("boQua", (long) trang * kichThuoc);
        List<MaGiamGiaQT> noiDung = tong == 0 ? List.of() : jdbc.sql(CHON + where
                        + " ORDER BY created_at DESC, id DESC LIMIT :gioiHan OFFSET :boQua")
                .params(ts).query(MaGiamGiaQT.class).list();
        return new PageResponse<>(noiDung, trang, kichThuoc, tong, (int) ((tong + kichThuoc - 1) / kichThuoc));
    }

    /** {@code id} null = thêm; sửa thì khóa dòng mã (đơn đang đặt dùng mã chờ xong) và giữ số lượt đã dùng. */
    @Transactional
    public MaGiamGiaQT luu(Long id, MaGiamGiaRequest req) {
        if (!req.ketThucLuc().isAfter(req.batDauLuc())) {
            throw ApiException.yeuCauKhongHopLe("Thời gian kết thúc phải sau thời gian bắt đầu");
        }
        if ("phan_tram".equals(req.loaiGiam()) && req.giaTriGiam() > 100) {
            throw ApiException.yeuCauKhongHopLe("Giảm theo phần trăm tối đa 100");
        }
        String ma = req.ma().trim().toUpperCase(Locale.ROOT);
        boolean trung = jdbc.sql("SELECT EXISTS (SELECT 1 FROM ma_giam_gia WHERE upper(ma) = :ma AND deleted_at IS NULL AND id <> :id)")
                .param("ma", ma).param("id", id == null ? -1L : id).query(Boolean.class).single();
        if (trung) {
            throw ApiException.xungDot("Mã giảm giá đã tồn tại");
        }
        Map<String, Object> cot = new HashMap<>();
        cot.put("ma", ma);
        cot.put("moTa", req.moTa() == null || req.moTa().isBlank() ? null : req.moTa().trim());
        cot.put("loai", req.loaiGiam());
        cot.put("giaTri", req.giaTriGiam());
        cot.put("toiThieu", req.donToiThieu() == null ? 0L : req.donToiThieu());
        cot.put("toiDa", req.giamToiDa());
        cot.put("tongLuot", req.tongLuotDung());
        cot.put("moiNguoi", req.luotDungMoiNguoi());
        cot.put("batDau", req.batDauLuc());
        cot.put("ketThuc", req.ketThucLuc());
        cot.put("kichHoat", req.kichHoat() == null || req.kichHoat());
        if (id == null) {
            id = jdbc.sql("""
                            INSERT INTO ma_giam_gia (ma, mo_ta, loai_giam, gia_tri_giam, don_toi_thieu, giam_toi_da,
                                                     tong_luot_dung, luot_dung_moi_nguoi, bat_dau_luc, ket_thuc_luc, kich_hoat)
                            VALUES (:ma, :moTa, :loai, :giaTri, :toiThieu, :toiDa, :tongLuot, :moiNguoi, :batDau, :ketThuc,
                                    :kichHoat) RETURNING id
                            """)
                    .params(cot).query(Long.class).single();
        } else {
            int daDung = jdbc.sql("SELECT da_dung FROM ma_giam_gia WHERE id = :id AND deleted_at IS NULL FOR UPDATE")
                    .param("id", id).query(Integer.class).optional()
                    .orElseThrow(AdminMaGiamGiaService::khongTimThay);
            if (req.tongLuotDung() != null && req.tongLuotDung() < daDung) {
                throw ApiException.yeuCauKhongHopLe("Tổng lượt không được nhỏ hơn số lượt đã dùng");
            }
            cot.put("id", id);
            jdbc.sql("""
                            UPDATE ma_giam_gia SET ma = :ma, mo_ta = :moTa, loai_giam = :loai, gia_tri_giam = :giaTri,
                                   don_toi_thieu = :toiThieu, giam_toi_da = :toiDa, tong_luot_dung = :tongLuot,
                                   luot_dung_moi_nguoi = :moiNguoi, bat_dau_luc = :batDau, ket_thuc_luc = :ketThuc,
                                   kich_hoat = :kichHoat, updated_at = now()
                            WHERE id = :id
                            """)
                    .params(cot).update();
        }
        return mot(id);
    }

    @Transactional
    public void xoa(Long id) {
        if (jdbc.sql("UPDATE ma_giam_gia SET deleted_at = now() WHERE id = :id AND deleted_at IS NULL").param("id", id).update() == 0) {
            throw khongTimThay();
        }
    }

    private MaGiamGiaQT mot(Long id) {
        return jdbc.sql(CHON + " AND id = :id").param("id", id).query(MaGiamGiaQT.class).optional()
                .orElseThrow(AdminMaGiamGiaService::khongTimThay);
    }

    private static ApiException khongTimThay() {
        return ApiException.khongTimThay("Không tìm thấy mã giảm giá");
    }
}
