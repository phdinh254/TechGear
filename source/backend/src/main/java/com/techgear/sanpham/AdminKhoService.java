package com.techgear.sanpham;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.techgear.chung.PageResponse;
import com.techgear.chung.loi.ApiException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Kho: nhập, điều chỉnh (khóa dòng biến thể như lúc đặt hàng), lịch sử, biến thể sắp hết. */
@Service
@RequiredArgsConstructor
public class AdminKhoService {

    public enum LoaiKho { khoi_tao, nhap_kho, ban_hang, huy_don, dieu_chinh }

    private final JdbcClient jdbc;
    private final AdminSanPhamService adminSanPhamService;

    public record NhapKhoRequest(
            @NotNull(message = "Số lượng nhập từ 1 đến 100.000") @Min(value = 1, message = "Số lượng nhập từ 1 đến 100.000")
            @Max(value = 100_000, message = "Số lượng nhập từ 1 đến 100.000") Integer soLuong,
            @Size(max = 255, message = "Ghi chú tối đa 255 ký tự") String ghiChu
    ) {
    }

    public record DieuChinhRequest(
            @NotNull(message = "Vui lòng nhập số lượng mới") @Min(value = 0, message = "Số lượng mới không được âm")
            @Max(value = 1_000_000, message = "Số lượng mới tối đa 1.000.000") Integer soLuongMoi,
            @NotBlank(message = "Vui lòng nhập lý do điều chỉnh") @Size(max = 255, message = "Lý do tối đa 255 ký tự")
            String ghiChu
    ) {
    }

    public record TonKho(Long bienTheId, int soLuongTon) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record LichSu(Long id, Long bienTheId, String sku, String tenSanPham, String loai, int soLuongThayDoi,
                         int soLuongSau, String ghiChu, String maDonHang, String nguoiThucHien, OffsetDateTime thoiGian) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SapHet(Long bienTheId, Long sanPhamId, String tenSanPham, String sku, String phanLoai, int soLuongTon,
                         int nguongCanhBao) {
    }

    public enum TrangThaiKho { con_hang, sap_het, het_hang }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record DongKho(Long bienTheId, Long sanPhamId, String tenSanPham, String sku, String phanLoai, int soLuongTon,
                          int nguongCanhBao, String trangThai, boolean hienThi) {
    }

    private static final String TRANG_THAI_KHO = """
            CASE WHEN bt.so_luong_ton = 0 THEN 'het_hang'
                 WHEN bt.so_luong_ton <= bt.nguong_canh_bao THEN 'sap_het' ELSE 'con_hang' END""";

    /** Tồn kho mọi biến thể chưa xóa; tìm không dấu theo tên sản phẩm hoặc SKU. */
    @Transactional(readOnly = true)
    public PageResponse<DongKho> danhSach(String tuKhoa, TrangThaiKho trangThai, int trang, int kichThuoc) {
        StringBuilder where = new StringBuilder(" WHERE bt.deleted_at IS NULL AND sp.deleted_at IS NULL");
        Map<String, Object> ts = new HashMap<>();
        if (tuKhoa != null && !tuKhoa.isBlank()) {
            where.append(" AND (sp.ten LIKE CONCAT('%', :tuKhoa, '%') OR bt.sku LIKE CONCAT('%', :tuKhoa, '%'))");
            ts.put("tuKhoa", HienThi.thoatLike(tuKhoa.trim()));
        }
        if (trangThai != null) {
            where.append(" AND ").append(TRANG_THAI_KHO).append(" = :trangThai");
            ts.put("trangThai", trangThai.name());
        }
        String tu = " FROM bien_the bt JOIN san_pham sp ON sp.id = bt.san_pham_id" + where;
        long tong = jdbc.sql("SELECT count(*)" + tu).params(ts).query(Long.class).single();
        ts.put("gioiHan", kichThuoc);
        ts.put("boQua", (long) trang * kichThuoc);
        List<DongKho> noiDung = tong == 0 ? List.of() : jdbc.sql(
                        "SELECT bt.id AS bien_the_id, sp.id AS san_pham_id, sp.ten AS ten_san_pham, bt.sku, "
                        + HienThi.PHAN_LOAI + " AS phan_loai, bt.so_luong_ton, bt.nguong_canh_bao, "
                        + TRANG_THAI_KHO + " AS trang_thai, bt.hien_thi" + tu
                        + " ORDER BY sp.ten, bt.id LIMIT :gioiHan OFFSET :boQua")
                .params(ts).query(DongKho.class).list();
        return new PageResponse<>(noiDung, trang, kichThuoc, tong, (int) ((tong + kichThuoc - 1) / kichThuoc));
    }

    @Transactional
    public TonKho nhapKho(Long bienTheId, NhapKhoRequest req, Long nguoiThucHienId) {
        int cu = khoaTonKho(bienTheId);
        return doi(bienTheId, "nhap_kho", req.soLuong(), cu + req.soLuong(), req.ghiChu(), nguoiThucHienId);
    }

    @Transactional
    public TonKho dieuChinh(Long bienTheId, DieuChinhRequest req, Long nguoiThucHienId) {
        int cu = khoaTonKho(bienTheId);
        if (cu == req.soLuongMoi()) {
            throw ApiException.yeuCauKhongHopLe("Số lượng mới bằng tồn kho hiện tại");
        }
        return doi(bienTheId, "dieu_chinh", req.soLuongMoi() - cu, req.soLuongMoi(), req.ghiChu(), nguoiThucHienId);
    }

    @Transactional(readOnly = true)
    public PageResponse<LichSu> lichSu(Long bienTheId, LoaiKho loai, int trang, int kichThuoc) {
        StringBuilder where = new StringBuilder(" WHERE TRUE");
        Map<String, Object> ts = new HashMap<>();
        if (bienTheId != null) {
            where.append(" AND k.bien_the_id = :bt");
            ts.put("bt", bienTheId);
        }
        if (loai != null) {
            where.append(" AND k.loai = :loai");
            ts.put("loai", loai.name());
        }
        long tong = jdbc.sql("SELECT count(*) FROM lich_su_ton_kho k" + where).params(ts).query(Long.class).single();
        ts.put("gioiHan", kichThuoc);
        ts.put("boQua", (long) trang * kichThuoc);
        List<LichSu> noiDung = tong == 0 ? List.of() : jdbc.sql("""
                        SELECT k.id, k.bien_the_id, bt.sku, sp.ten AS ten_san_pham, k.loai, k.so_luong_thay_doi,
                               k.so_luong_sau, k.ghi_chu, dh.ma_don_hang, nd.ho_ten AS nguoi_thuc_hien,
                               k.created_at AS thoi_gian
                        FROM lich_su_ton_kho k
                        JOIN bien_the bt ON bt.id = k.bien_the_id
                        JOIN san_pham sp ON sp.id = bt.san_pham_id
                        LEFT JOIN don_hang dh ON dh.id = k.don_hang_id
                        LEFT JOIN nguoi_dung nd ON nd.id = k.nguoi_thuc_hien_id""" + where + """

                        ORDER BY k.created_at DESC, k.id DESC LIMIT :gioiHan OFFSET :boQua
                        """)
                .params(ts).query(LichSu.class).list();
        return new PageResponse<>(noiDung, trang, kichThuoc, tong, (int) ((tong + kichThuoc - 1) / kichThuoc));
    }

    /** Biến thể chưa xóa (của sản phẩm chưa xóa) có tồn ≤ ngưỡng cảnh báo; ít hàng trước. */
    @Transactional(readOnly = true)
    public List<SapHet> sapHet() {
        return jdbc.sql("SELECT bt.id AS bien_the_id, sp.id AS san_pham_id, sp.ten AS ten_san_pham, bt.sku, "
                        + HienThi.PHAN_LOAI + """
                         AS phan_loai, bt.so_luong_ton, bt.nguong_canh_bao
                        FROM bien_the bt JOIN san_pham sp ON sp.id = bt.san_pham_id
                        WHERE bt.deleted_at IS NULL AND sp.deleted_at IS NULL AND bt.so_luong_ton <= bt.nguong_canh_bao
                        ORDER BY bt.so_luong_ton, bt.id
                        """)
                .query(SapHet.class).list();
    }

    /** Khóa dòng biến thể (FOR UPDATE) – cùng cơ chế với đặt hàng nên không ghi đè lẫn nhau. */
    private int khoaTonKho(Long bienTheId) {
        adminSanPhamService.sanPhamCuaBienThe(bienTheId);
        return jdbc.sql("SELECT so_luong_ton FROM bien_the WHERE id = :id FOR UPDATE")
                .param("id", bienTheId).query(Integer.class).single();
    }

    private TonKho doi(Long bienTheId, String loai, int thayDoi, int moi, String ghiChu, Long nguoiThucHienId) {
        jdbc.sql("UPDATE bien_the SET so_luong_ton = :moi, updated_at = now() WHERE id = :id")
                .param("moi", moi).param("id", bienTheId).update();
        adminSanPhamService.ghiLichSuKho(bienTheId, loai, thayDoi, moi,
                ghiChu == null || ghiChu.isBlank() ? null : ghiChu.trim(), nguoiThucHienId);
        return new TonKho(bienTheId, moi);
    }
}
