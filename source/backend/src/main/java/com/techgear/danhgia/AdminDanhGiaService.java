package com.techgear.danhgia;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.techgear.chung.PageResponse;
import com.techgear.chung.loi.ApiException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Quản trị đánh giá: lọc, duyệt / ẩn / về chờ duyệt, xóa hẳn (khách viết lại được). */
@Service
@RequiredArgsConstructor
public class AdminDanhGiaService {

    public enum TrangThai { cho_duyet, da_duyet, da_an }

    private final JdbcClient jdbc;

    public record TrangThaiRequest(@NotNull(message = "Vui lòng chọn trạng thái") TrangThai trangThai) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record DanhGiaQT(Long id, Long sanPhamId, String tenSanPham, String hoTen, String email, String maDonHang, int soSao,
                     String noiDung, String trangThai, OffsetDateTime ngayTao) {
    }

    private static final String CHON = """
            SELECT dg.id, dg.san_pham_id, sp.ten AS ten_san_pham, nd.ho_ten, nd.email, dh.ma_don_hang, dg.so_sao,
                   dg.noi_dung, dg.trang_thai, dg.created_at AS ngay_tao
            FROM danh_gia dg
            JOIN san_pham sp ON sp.id = dg.san_pham_id
            JOIN nguoi_dung nd ON nd.id = dg.nguoi_dung_id
            JOIN don_hang dh ON dh.id = dg.don_hang_id
            WHERE TRUE""";

    @Transactional(readOnly = true)
    public PageResponse<DanhGiaQT> danhSach(TrangThai trangThai, Long sanPhamId, Integer soSao, int trang, int kichThuoc) {
        StringBuilder where = new StringBuilder();
        Map<String, Object> ts = new HashMap<>();
        if (trangThai != null) {
            where.append(" AND dg.trang_thai = :tt");
            ts.put("tt", trangThai.name());
        }
        if (sanPhamId != null) {
            where.append(" AND dg.san_pham_id = :sp");
            ts.put("sp", sanPhamId);
        }
        if (soSao != null) {
            where.append(" AND dg.so_sao = :soSao");
            ts.put("soSao", soSao);
        }
        long tong = jdbc.sql("SELECT count(*) FROM danh_gia dg WHERE TRUE" + where).params(ts).query(Long.class).single();
        ts.put("gioiHan", kichThuoc);
        ts.put("boQua", (long) trang * kichThuoc);
        List<DanhGiaQT> noiDung = tong == 0 ? List.of() : jdbc.sql(CHON + where
                        + " ORDER BY dg.created_at DESC, dg.id DESC LIMIT :gioiHan OFFSET :boQua")
                .params(ts).query(DanhGiaQT.class).list();
        return new PageResponse<>(noiDung, trang, kichThuoc, tong, (int) ((tong + kichThuoc - 1) / kichThuoc));
    }

    @Transactional
    public DanhGiaQT doiTrangThai(Long id, TrangThai trangThai) {
        if (jdbc.sql("UPDATE danh_gia SET trang_thai = :tt, updated_at = now() WHERE id = :id")
                .param("tt", trangThai.name()).param("id", id).update() == 0) {
            throw khongTimThay();
        }
        return jdbc.sql(CHON + " AND dg.id = :id").param("id", id).query(DanhGiaQT.class).single();
    }

    @Transactional
    public void xoa(Long id) {
        if (jdbc.sql("DELETE FROM danh_gia WHERE id = :id").param("id", id).update() == 0) {
            throw khongTimThay();
        }
    }

    private static ApiException khongTimThay() {
        return ApiException.khongTimThay("Không tìm thấy đánh giá");
    }
}
