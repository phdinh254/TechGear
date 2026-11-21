package com.techgear.khuyenmai;

import com.techgear.chung.loi.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class KhuyenMaiService {

    private final JdbcClient jdbc;
    private final Clock clock;

    public record KetQua(Long id, String ma, long tienGiam) {
    }

    /**
     * Kiểm tra và tính tiền giảm của mã (không phân biệt hoa thường).
     * {@code datHang} = true: khóa dòng mã (FOR UPDATE) để các đơn dùng cùng mã xếp hàng, rồi ghi nhận một lượt dùng.
     * Lượt mỗi người đếm các đơn chưa hủy theo tài khoản hoặc email (vãng lai); không có cả hai thì bỏ qua.
     */
    @Transactional
    public KetQua apDung(String ma, long tamTinh, Long nguoiDungId, String email, boolean datHang) {
        MaGiamGia m = jdbc.sql("""
                        SELECT id, ma, loai_giam, gia_tri_giam, don_toi_thieu, giam_toi_da, tong_luot_dung,
                               luot_dung_moi_nguoi, da_dung, bat_dau_luc, ket_thuc_luc, kich_hoat
                        FROM ma_giam_gia WHERE upper(ma) = upper(:ma) AND deleted_at IS NULL
                        """ + (datHang ? " FOR UPDATE" : ""))
                .param("ma", ma).query(MaGiamGia.class).optional()
                .orElseThrow(() -> ApiException.yeuCauKhongHopLe("Mã giảm giá không tồn tại"));

        long daDungCuaKhach = m.luotDungMoiNguoi() == null ? 0 : demLuotCuaKhach(m.id(), nguoiDungId, email);
        String loi = m.loiKhiApDung(OffsetDateTime.now(clock), tamTinh, daDungCuaKhach);
        if (loi != null) {
            throw ApiException.yeuCauKhongHopLe(loi);
        }
        if (datHang) {
            jdbc.sql("UPDATE ma_giam_gia SET da_dung = da_dung + 1, updated_at = now() WHERE id = :id")
                    .param("id", m.id()).update();
        }
        return new KetQua(m.id(), m.ma(), m.tienGiam(tamTinh));
    }

    /** Trả lại một lượt khi đơn dùng mã bị hủy. */
    @Transactional
    public void traLuot(Long maGiamGiaId) {
        jdbc.sql("UPDATE ma_giam_gia SET da_dung = da_dung - 1, updated_at = now() WHERE id = :id AND da_dung > 0")
                .param("id", maGiamGiaId).update();
    }

    private long demLuotCuaKhach(Long maGiamGiaId, Long nguoiDungId, String email) {
        if (nguoiDungId == null && email == null) {
            return 0;
        }
        String ai = nguoiDungId == null ? "lower(email) = lower(:email)"
                : email == null ? "nguoi_dung_id = :nd" : "(nguoi_dung_id = :nd OR lower(email) = lower(:email))";
        JdbcClient.StatementSpec sql = jdbc.sql("SELECT count(*) FROM don_hang WHERE ma_giam_gia_id = :id"
                        + " AND trang_thai <> 'da_huy' AND " + ai)
                .param("id", maGiamGiaId);
        if (nguoiDungId != null) {
            sql = sql.param("nd", nguoiDungId);
        }
        if (email != null) {
            sql = sql.param("email", email);
        }
        return sql.query(Long.class).single();
    }
}
