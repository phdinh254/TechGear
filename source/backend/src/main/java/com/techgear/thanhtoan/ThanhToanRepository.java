package com.techgear.thanhtoan;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
class ThanhToanRepository {

    private final JdbcClient jdbc;

    record Don(Long id, String maDonHang, String phuongThucThanhToan, String trangThai, String trangThaiThanhToan,
               long tongTien, OffsetDateTime createdAt) {
    }

    /** Khóa đơn để hai lần tạo URL cùng lúc không trùng số thứ tự mã tham chiếu. */
    Optional<Don> khoaDon(String maDonHang) {
        return jdbc.sql("""
                        SELECT id, ma_don_hang, phuong_thuc_thanh_toan, trang_thai, trang_thai_thanh_toan, tong_tien, created_at
                        FROM don_hang WHERE ma_don_hang = upper(:ma) FOR UPDATE
                        """)
                .param("ma", maDonHang).query(Don.class).optional();
    }

    int soLanThu(Long donHangId) {
        return jdbc.sql("SELECT count(*) FROM thanh_toan WHERE don_hang_id = :id")
                .param("id", donHangId).query(Integer.class).single();
    }

    void themGiaoDich(Long donHangId, long soTien, String maThamChieu) {
        jdbc.sql("""
                        INSERT INTO thanh_toan (don_hang_id, phuong_thuc, so_tien, ma_tham_chieu)
                        VALUES (:dh, 'vnpay', :soTien, :ma)
                        """)
                .param("dh", donHangId).param("soTien", soTien).param("ma", maThamChieu).update();
    }

    record GiaoDich(Long id, Long donHangId, String maDonHang, long soTien, String trangThai,
                    String trangThaiThanhToan) {
    }

    private static final String CHON_GIAO_DICH = """
            SELECT tt.id, tt.don_hang_id, dh.ma_don_hang, tt.so_tien, tt.trang_thai, dh.trang_thai_thanh_toan
            FROM thanh_toan tt JOIN don_hang dh ON dh.id = tt.don_hang_id
            WHERE tt.ma_tham_chieu = :ma
            """;

    /** Khóa giao dịch: hai IPN cùng mã tham chiếu đến cùng lúc chỉ một lần được ghi. */
    Optional<GiaoDich> khoaGiaoDich(String maThamChieu) {
        return jdbc.sql(CHON_GIAO_DICH + " FOR UPDATE OF tt").param("ma", maThamChieu).query(GiaoDich.class).optional();
    }

    Optional<GiaoDich> giaoDich(String maThamChieu) {
        return jdbc.sql(CHON_GIAO_DICH).param("ma", maThamChieu).query(GiaoDich.class).optional();
    }

    void ketThucGiaoDich(Long id, boolean thanhCong, String maGiaoDich, String duLieuJson, OffsetDateTime luc) {
        jdbc.sql("""
                        UPDATE thanh_toan SET trang_thai = :tt, ma_giao_dich = :maGiaoDich,
                               du_lieu_phan_hoi = CAST(:duLieu AS jsonb), thanh_toan_luc = :luc, updated_at = now()
                        WHERE id = :id
                        """)
                .param("tt", thanhCong ? "thanh_cong" : "that_bai").param("maGiaoDich", maGiaoDich)
                .param("duLieu", duLieuJson).param("luc", thanhCong ? luc : null).param("id", id).update();
    }

    /** Đơn đã bị hủy (quá hạn, khách hủy) mà tiền vẫn về: giữ trạng thái hủy, ghi chú để quản trị hoàn tiền. */
    void danhDauDaThanhToan(Long donHangId) {
        jdbc.sql("""
                        UPDATE don_hang SET trang_thai_thanh_toan = 'da_thanh_toan', updated_at = now(),
                               ghi_chu_quan_tri = CASE WHEN trang_thai = 'da_huy'
                                   THEN concat_ws(' | ', ghi_chu_quan_tri, 'Đã nhận tiền VNPay sau khi đơn bị hủy – cần hoàn tiền')
                                   ELSE ghi_chu_quan_tri END
                        WHERE id = :id
                        """)
                .param("id", donHangId).update();
    }
}
