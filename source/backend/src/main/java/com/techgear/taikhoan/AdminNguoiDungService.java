package com.techgear.taikhoan;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.techgear.chung.PageResponse;
import com.techgear.chung.loi.ApiException;
import com.techgear.sanpham.HienThi;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Quản trị người dùng: tìm, lọc, khóa/mở (khóa thì mọi token của người đó bị từ chối ngay). */
@Service
@RequiredArgsConstructor
public class AdminNguoiDungService {

    private final JdbcClient jdbc;
    private final NguoiDungRepository nguoiDungRepository;

    public record TrangThaiRequest(@NotNull(message = "Vui lòng chọn trạng thái") NguoiDung.TrangThai trangThai) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record NguoiDungQT(Long id, String hoTen, String email, String soDienThoai, String vaiTro, String trangThai,
                              long soDonHang, OffsetDateTime ngayTao) {
    }

    private static final String CHON = """
            SELECT nd.id, nd.ho_ten, nd.email, nd.so_dien_thoai, nd.vai_tro, nd.trang_thai,
                   (SELECT count(*) FROM don_hang dh WHERE dh.nguoi_dung_id = nd.id) AS so_don_hang,
                   nd.created_at AS ngay_tao
            FROM nguoi_dung nd WHERE TRUE""";

    @Transactional(readOnly = true)
    public PageResponse<NguoiDungQT> danhSach(String tuKhoa, NguoiDung.VaiTro vaiTro, NguoiDung.TrangThai trangThai,
                                              int trang, int kichThuoc) {
        StringBuilder where = new StringBuilder();
        Map<String, Object> ts = new HashMap<>();
        if (tuKhoa != null && !tuKhoa.isBlank()) {
            where.append(" AND (lower(nd.email) = lower(:goc) OR nd.so_dien_thoai = :goc")
                    .append(" OR lower(unaccent(nd.ho_ten)) LIKE '%' || lower(unaccent(:tuKhoa)) || '%')");
            ts.put("goc", tuKhoa.trim());
            ts.put("tuKhoa", HienThi.thoatLike(tuKhoa.trim()));
        }
        if (vaiTro != null) {
            where.append(" AND nd.vai_tro = :vaiTro");
            ts.put("vaiTro", vaiTro.name());
        }
        if (trangThai != null) {
            where.append(" AND nd.trang_thai = :trangThai");
            ts.put("trangThai", trangThai.name());
        }
        long tong = jdbc.sql("SELECT count(*) FROM nguoi_dung nd WHERE TRUE" + where).params(ts).query(Long.class).single();
        ts.put("gioiHan", kichThuoc);
        ts.put("boQua", (long) trang * kichThuoc);
        List<NguoiDungQT> noiDung = tong == 0 ? List.of() : jdbc.sql(CHON + where
                        + " ORDER BY nd.created_at DESC, nd.id DESC LIMIT :gioiHan OFFSET :boQua")
                .params(ts).query(NguoiDungQT.class).list();
        return new PageResponse<>(noiDung, trang, kichThuoc, tong, (int) ((tong + kichThuoc - 1) / kichThuoc));
    }

    /** Sửa qua entity để bộ kiểm token (đọc qua JPA) thấy trạng thái mới ngay trong cùng giao dịch. */
    @Transactional
    public NguoiDungQT doiTrangThai(Long id, NguoiDung.TrangThai trangThai, Long quanTriId) {
        if (id.equals(quanTriId) && trangThai == NguoiDung.TrangThai.bi_khoa) {
            throw ApiException.yeuCauKhongHopLe("Không thể tự khóa tài khoản của mình");
        }
        NguoiDung n = nguoiDungRepository.khoaTheoId(id)
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy người dùng"));
        n.setTrangThai(trangThai);
        nguoiDungRepository.flush();
        return jdbc.sql(CHON + " AND nd.id = :id").param("id", id).query(NguoiDungQT.class).single();
    }
}
