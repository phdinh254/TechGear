package com.techgear.thongke;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.techgear.chung.loi.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Thống kê theo ngày đặt hàng (giờ Việt Nam); doanh thu chỉ tính đơn hoàn thành. */
@Service
@RequiredArgsConstructor
public class ThongKeService {

    public enum Theo { ngay, thang }

    private static final String NGAY_VN = "(dh.created_at AT TIME ZONE 'Asia/Ho_Chi_Minh')::date";
    private static final List<String> TRANG_THAI = List.of("cho_xac_nhan", "da_xac_nhan", "dang_giao", "hoan_thanh", "da_huy");

    private final JdbcClient jdbc;

    public record Moc(String moc, long doanhThu, long soDon) {
    }

    public record DonHang(long tongDon, Map<String, Long> theoTrangThai, long doanhThu) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record BanChay(Long sanPhamId, String tenSanPham, long soLuongBan, long doanhThu) {
    }

    /** Đủ mọi mốc trong khoảng (mốc không có đơn = 0) để vẽ biểu đồ liền mạch. */
    @Transactional(readOnly = true)
    public List<Moc> doanhThu(LocalDate tu, LocalDate den, Theo theo) {
        kiemTraKhoang(tu, den);
        if (theo == Theo.ngay && ChronoUnit.DAYS.between(tu, den) >= 366) {
            throw ApiException.yeuCauKhongHopLe("Thống kê theo ngày tối đa 366 ngày");
        }
        if (theo == Theo.thang && ChronoUnit.MONTHS.between(tu.withDayOfMonth(1), den.withDayOfMonth(1)) >= 60) {
            throw ApiException.yeuCauKhongHopLe("Thống kê theo tháng tối đa 60 tháng");
        }
        String buoc = theo == Theo.ngay ? "day" : "month";
        String dinhDang = theo == Theo.ngay ? "YYYY-MM-DD" : "YYYY-MM";
        return jdbc.sql("""
                        SELECT to_char(m.moc, '%s') AS moc, coalesce(sum(dh.tong_tien), 0) AS doanh_thu, count(dh.id) AS so_don
                        FROM generate_series(date_trunc('%s', CAST(:tu AS date)), CAST(:den AS date), interval '1 %s') AS m(moc)
                        LEFT JOIN don_hang dh ON dh.trang_thai = 'hoan_thanh'
                             AND date_trunc('%s', %s) = m.moc
                             AND %s BETWEEN CAST(:tu AS date) AND CAST(:den AS date)
                        GROUP BY m.moc ORDER BY m.moc
                        """.formatted(dinhDang, buoc, buoc, buoc, NGAY_VN, NGAY_VN))
                .param("tu", tu).param("den", den).query(Moc.class).list();
    }

    @Transactional(readOnly = true)
    public DonHang donHang(LocalDate tu, LocalDate den) {
        kiemTraKhoang(tu, den);
        Map<String, Object> ts = new HashMap<>();
        String where = loc(tu, den, ts);
        Map<String, Long> theoTrangThai = new LinkedHashMap<>();
        TRANG_THAI.forEach(t -> theoTrangThai.put(t, 0L));
        long[] tong = {0, 0};
        jdbc.sql("SELECT dh.trang_thai, count(*) AS so, coalesce(sum(dh.tong_tien), 0) AS tien FROM don_hang dh WHERE TRUE"
                        + where + " GROUP BY dh.trang_thai")
                .params(ts).query(rs -> {
                    theoTrangThai.put(rs.getString("trang_thai"), rs.getLong("so"));
                    tong[0] += rs.getLong("so");
                    if ("hoan_thanh".equals(rs.getString("trang_thai"))) {
                        tong[1] = rs.getLong("tien");
                    }
                });
        return new DonHang(tong[0], theoTrangThai, tong[1]);
    }

    /** Theo số lượng trong đơn hoàn thành; sản phẩm đã xóa hẳn gộp theo tên lúc mua. */
    @Transactional(readOnly = true)
    public List<BanChay> banChay(LocalDate tu, LocalDate den, int gioiHan) {
        kiemTraKhoang(tu, den);
        Map<String, Object> ts = new HashMap<>();
        String where = loc(tu, den, ts);
        ts.put("gioiHan", gioiHan);
        return jdbc.sql("""
                        SELECT c.san_pham_id, coalesce(max(sp.ten), max(c.ten_san_pham)) AS ten_san_pham,
                               sum(c.so_luong) AS so_luong_ban, sum(c.thanh_tien) AS doanh_thu
                        FROM chi_tiet_don_hang c
                        JOIN don_hang dh ON dh.id = c.don_hang_id AND dh.trang_thai = 'hoan_thanh'
                        LEFT JOIN san_pham sp ON sp.id = c.san_pham_id
                        WHERE TRUE""" + where + """

                        GROUP BY c.san_pham_id, CASE WHEN c.san_pham_id IS NULL THEN c.ten_san_pham END
                        ORDER BY so_luong_ban DESC, doanh_thu DESC LIMIT :gioiHan
                        """)
                .params(ts).query(BanChay.class).list();
    }

    private static String loc(LocalDate tu, LocalDate den, Map<String, Object> ts) {
        StringBuilder where = new StringBuilder();
        if (tu != null) {
            where.append(" AND ").append(NGAY_VN).append(" >= CAST(:tu AS date)");
            ts.put("tu", tu);
        }
        if (den != null) {
            where.append(" AND ").append(NGAY_VN).append(" <= CAST(:den AS date)");
            ts.put("den", den);
        }
        return where.toString();
    }

    private static void kiemTraKhoang(LocalDate tu, LocalDate den) {
        if (tu != null && den != null && tu.isAfter(den)) {
            throw ApiException.yeuCauKhongHopLe("Từ ngày phải trước hoặc bằng đến ngày");
        }
    }
}
