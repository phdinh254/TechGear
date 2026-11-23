package com.techgear.donhang;

import com.techgear.chung.Csdl;
import com.techgear.sanpham.HienThi;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
class DonHangRepository {

    private final JdbcClient jdbc;

    /** Biến thể đang bán kèm dữ liệu chụp vào đơn và tồn kho hiện tại. */
    record DongKho(Long bienTheId, Long sanPhamId, String sanPhamSlug, String tenSanPham, String sku, String phanLoai,
                   String hinhAnh, long donGia, int soLuongTon) {
    }

    /**
     * {@code khoa} = true: khóa dòng các biến thể (FOR UPDATE) theo thứ tự id để hai đơn chung biến thể không khóa chéo;
     * đọc có khóa của InnoDB luôn lấy tồn kho mới nhất.
     */
    Map<Long, DongKho> dongKho(Collection<Long> bienTheIds, boolean khoa) {
        return jdbc.sql("SELECT bt.id AS bien_the_id, sp.id AS san_pham_id, sp.slug AS san_pham_slug, "
                        + "sp.ten AS ten_san_pham, bt.sku, " + HienThi.PHAN_LOAI + " AS phan_loai, "
                        + HienThi.ANH_BIEN_THE + " AS hinh_anh, bt.gia_ban AS don_gia, bt.so_luong_ton "
                        + HienThi.TU_BANG + " JOIN bien_the bt ON bt.san_pham_id = sp.id"
                        + " WHERE bt.id IN (:ids) AND " + HienThi.BIEN_THE_DANG_BAN
                        + " ORDER BY bt.id" + (khoa ? " FOR UPDATE OF bt" : ""))
                .param("ids", bienTheIds).query(DongKho.class).list().stream()
                .collect(Collectors.toMap(DongKho::bienTheId, Function.identity()));
    }

    /** Trừ (âm) hoặc cộng (dương) tồn kho; trả tồn kho sau thay đổi (dòng đang bị khóa bởi UPDATE). CSDL chặn tồn kho âm. */
    int doiTonKho(Long bienTheId, int thayDoi) {
        jdbc.sql("UPDATE bien_the SET so_luong_ton = so_luong_ton + :thayDoi, updated_at = now() WHERE id = :id")
                .param("thayDoi", thayDoi).param("id", bienTheId).update();
        return jdbc.sql("SELECT so_luong_ton FROM bien_the WHERE id = :id").param("id", bienTheId).query(Integer.class).single();
    }

    void ghiLichSuKho(Long bienTheId, Long donHangId, String loai, int thayDoi, int sau, Long nguoiThucHienId) {
        jdbc.sql("""
                        INSERT INTO lich_su_ton_kho (bien_the_id, don_hang_id, loai, so_luong_thay_doi, so_luong_sau,
                                                     nguoi_thuc_hien_id)
                        VALUES (:bt, :dh, :loai, :thayDoi, :sau, :nguoi)
                        """)
                .param("bt", bienTheId).param("dh", donHangId).param("loai", loai).param("thayDoi", thayDoi)
                .param("sau", sau).param("nguoi", nguoiThucHienId).update();
    }

    boolean maDaTonTai(String maDonHang) {
        return jdbc.sql("SELECT EXISTS (SELECT 1 FROM don_hang WHERE ma_don_hang = :ma)")
                .param("ma", maDonHang).query(Boolean.class).single();
    }

    Long themDonHang(Map<String, Object> cot) {
        return Csdl.themLayId(jdbc.sql("""
                        INSERT INTO don_hang (ma_don_hang, nguoi_dung_id, ma_giam_gia_id, ten_nguoi_nhan, email,
                                              so_dien_thoai, dia_chi_giao_hang, ghi_chu, tam_tinh, tien_giam_gia,
                                              phi_van_chuyen, tong_tien, phuong_thuc_thanh_toan)
                        VALUES (:ma, :nd, :maGiamGiaId, :tenNguoiNhan, :email, :soDienThoai, :diaChi, :ghiChu, :tamTinh,
                                :tienGiamGia, :phiVanChuyen, :tongTien, :phuongThuc)
                        """)
                .params(cot));
    }

    void themDong(Long donHangId, DonHangResponse.Dong d) {
        jdbc.sql("""
                        INSERT INTO chi_tiet_don_hang (don_hang_id, san_pham_id, bien_the_id, ten_san_pham, sku, phan_loai,
                                                       hinh_anh, don_gia, so_luong, thanh_tien)
                        VALUES (:dh, :sp, :bt, :ten, :sku, :phanLoai, :hinhAnh, :donGia, :soLuong, :thanhTien)
                        """)
                .param("dh", donHangId).param("sp", d.sanPhamId()).param("bt", d.bienTheId())
                .param("ten", d.tenSanPham()).param("sku", d.sku()).param("phanLoai", d.phanLoai())
                .param("hinhAnh", d.hinhAnh()).param("donGia", d.donGia()).param("soLuong", d.soLuong())
                .param("thanhTien", d.thanhTien()).update();
    }

    /** SYSDATE(6): các bước trong cùng giao dịch vẫn có thứ tự thời gian. */
    void ghiLichSuDon(Long donHangId, TrangThaiDonHang trangThai, String ghiChu, Long nguoiThucHienId) {
        jdbc.sql("""
                        INSERT INTO lich_su_don_hang (don_hang_id, trang_thai, ghi_chu, nguoi_thuc_hien_id, created_at)
                        VALUES (:dh, :tt, :ghiChu, :nguoi, SYSDATE(6))
                        """)
                .param("dh", donHangId).param("tt", trangThai.name()).param("ghiChu", ghiChu)
                .param("nguoi", nguoiThucHienId).update();
    }

    void xoaKhoiGio(Long nguoiDungId, Collection<Long> bienTheIds) {
        jdbc.sql("DELETE FROM gio_hang WHERE nguoi_dung_id = :nd AND bien_the_id IN (:ids)")
                .param("nd", nguoiDungId).param("ids", bienTheIds).update();
    }

    record DonKhoa(Long id, String trangThai, Long maGiamGiaId) {
    }

    /** Khóa dòng đơn của người dùng trước khi đổi trạng thái (hai lần hủy cùng lúc chỉ một lần hoàn kho). */
    Optional<DonKhoa> khoaDonCuaNguoiDung(String maDonHang, Long nguoiDungId) {
        return jdbc.sql("""
                        SELECT id, trang_thai, ma_giam_gia_id FROM don_hang
                        WHERE ma_don_hang = upper(:ma) AND nguoi_dung_id = :nd FOR UPDATE
                        """)
                .param("ma", maDonHang).param("nd", nguoiDungId).query(DonKhoa.class).optional();
    }

    /** Id các đơn PayOS chưa thanh toán đặt trước {@code moc} (chưa khóa). */
    List<Long> donPayOsQuaHan(java.time.OffsetDateTime moc) {
        return jdbc.sql("""
                        SELECT id FROM don_hang
                        WHERE phuong_thuc_thanh_toan = 'payos' AND trang_thai = 'cho_xac_nhan'
                          AND trang_thai_thanh_toan = 'chua_thanh_toan' AND created_at < :moc
                        ORDER BY id LIMIT 100
                        """)
                .param("moc", moc).query(Long.class).list();
    }

    /** Khóa lại một đơn quá hạn, kiểm lại điều kiện; bỏ qua nếu IPN/khách đang giữ khóa đơn. */
    Optional<DonKhoa> khoaDonPayOsQuaHan(Long id, java.time.OffsetDateTime moc) {
        return jdbc.sql("""
                        SELECT id, trang_thai, ma_giam_gia_id FROM don_hang
                        WHERE id = :id AND phuong_thuc_thanh_toan = 'payos' AND trang_thai = 'cho_xac_nhan'
                          AND trang_thai_thanh_toan = 'chua_thanh_toan' AND created_at < :moc
                        FOR UPDATE SKIP LOCKED
                        """)
                .param("id", id).param("moc", moc).query(DonKhoa.class).optional();
    }

    /** Khóa dòng đơn theo mã (quản trị). */
    Optional<DonKhoa> khoaDon(String maDonHang) {
        return jdbc.sql("SELECT id, trang_thai, ma_giam_gia_id FROM don_hang WHERE ma_don_hang = upper(:ma) FOR UPDATE")
                .param("ma", maDonHang).query(DonKhoa.class).optional();
    }

    record DongHoan(Long bienTheId, int soLuong) {
    }

    /** Các dòng còn biến thể để hoàn kho, theo thứ tự id biến thể (cùng thứ tự khóa với đặt hàng). */
    List<DongHoan> dongHoanKho(Long donHangId) {
        return jdbc.sql("""
                        SELECT bien_the_id, sum(so_luong) AS so_luong FROM chi_tiet_don_hang
                        WHERE don_hang_id = :id AND bien_the_id IS NOT NULL
                        GROUP BY bien_the_id ORDER BY bien_the_id
                        """)
                .param("id", donHangId).query(DongHoan.class).list();
    }

    /** Hủy đơn đã thanh toán (VD PayOS trả trước khi quản trị xác nhận): ghi chú để quản trị hoàn tiền, như IPN đến trễ. */
    void doiTrangThai(Long donHangId, TrangThaiDonHang trangThai) {
        jdbc.sql("""
                        UPDATE don_hang SET trang_thai = :tt, updated_at = now(),
                               ghi_chu_quan_tri = CASE WHEN :tt = 'da_huy' AND trang_thai_thanh_toan = 'da_thanh_toan'
                                   THEN concat_ws(' | ', ghi_chu_quan_tri, 'Đơn đã thanh toán bị hủy – cần hoàn tiền')
                                   ELSE ghi_chu_quan_tri END
                        WHERE id = :id
                        """)
                .param("tt", trangThai.name()).param("id", donHangId).update();
    }

    Optional<Long> idCuaNguoiDung(String maDonHang, Long nguoiDungId) {
        return jdbc.sql("SELECT id FROM don_hang WHERE ma_don_hang = upper(:ma) AND nguoi_dung_id = :nd")
                .param("ma", maDonHang).param("nd", nguoiDungId).query(Long.class).optional();
    }

    Optional<Long> idTheoEmail(String maDonHang, String email) {
        return jdbc.sql("SELECT id FROM don_hang WHERE ma_don_hang = upper(:ma) AND lower(email) = lower(:email)")
                .param("ma", maDonHang).param("email", email).query(Long.class).optional();
    }

    record TrangKetQua(List<DonHangResponse.TomTat> noiDung, long tongSoPhanTu) {
    }

    TrangKetQua cuaNguoiDung(Long nguoiDungId, TrangThaiDonHang trangThai, int trang, int kichThuoc) {
        String loc = trangThai == null ? "" : " AND dh.trang_thai = :tt";
        Map<String, Object> thamSo = new HashMap<>();
        thamSo.put("nd", nguoiDungId);
        if (trangThai != null) {
            thamSo.put("tt", trangThai.name());
        }
        long tong = jdbc.sql("SELECT count(*) FROM don_hang dh WHERE dh.nguoi_dung_id = :nd" + loc)
                .params(thamSo).query(Long.class).single();
        thamSo.put("gioiHan", kichThuoc);
        thamSo.put("boQua", (long) trang * kichThuoc);
        List<DonHangResponse.TomTat> noiDung = tong == 0 ? List.of() : jdbc.sql("""
                        SELECT dh.ma_don_hang, dh.trang_thai, dh.trang_thai_thanh_toan, dh.phuong_thuc_thanh_toan,
                               dh.tong_tien,
                               (SELECT sum(c.so_luong) FROM chi_tiet_don_hang c WHERE c.don_hang_id = dh.id)
                                   AS so_luong_san_pham,
                               (SELECT c.ten_san_pham FROM chi_tiet_don_hang c WHERE c.don_hang_id = dh.id
                                ORDER BY c.id LIMIT 1) AS ten_san_pham_dau,
                               (SELECT c.hinh_anh FROM chi_tiet_don_hang c WHERE c.don_hang_id = dh.id
                                ORDER BY c.id LIMIT 1) AS hinh_anh_dau,
                               dh.created_at AS ngay_dat
                        FROM don_hang dh WHERE dh.nguoi_dung_id = :nd""" + loc + """

                        ORDER BY dh.created_at DESC, dh.id DESC LIMIT :gioiHan OFFSET :boQua
                        """)
                .params(thamSo).query(DonHangResponse.TomTat.class).list();
        return new TrangKetQua(noiDung, tong);
    }

    private record ThongTinDon(String maDonHang, String trangThai, String trangThaiThanhToan,
                               String phuongThucThanhToan, String tenNguoiNhan, String email, String soDienThoai,
                               String diaChiGiaoHang, String ghiChu, String maGiamGia, long tamTinh, long tienGiamGia,
                               long phiVanChuyen, long tongTien, java.time.OffsetDateTime ngayDat) {
    }

    DonHangResponse.ChiTiet chiTiet(Long donHangId, boolean choPhepHuy) {
        return chiTiet(donHangId, choPhepHuy, false);
    }

    /** Chi tiết đơn; {@code choPhepHuy} = người xem là chủ đơn đã đăng nhập; {@code quanTri}: lịch sử kèm người thực hiện. */
    DonHangResponse.ChiTiet chiTiet(Long donHangId, boolean choPhepHuy, boolean quanTri) {
        ThongTinDon d = jdbc.sql("""
                        SELECT dh.ma_don_hang, dh.trang_thai, dh.trang_thai_thanh_toan, dh.phuong_thuc_thanh_toan,
                               dh.ten_nguoi_nhan, dh.email, dh.so_dien_thoai, dh.dia_chi_giao_hang, dh.ghi_chu,
                               mg.ma AS ma_giam_gia, dh.tam_tinh, dh.tien_giam_gia, dh.phi_van_chuyen, dh.tong_tien,
                               dh.created_at AS ngay_dat
                        FROM don_hang dh LEFT JOIN ma_giam_gia mg ON mg.id = dh.ma_giam_gia_id
                        WHERE dh.id = :id
                        """)
                .param("id", donHangId).query(ThongTinDon.class).single();
        List<DonHangResponse.Dong> dongs = jdbc.sql("""
                        SELECT c.bien_the_id, c.san_pham_id, sp.slug AS san_pham_slug, c.ten_san_pham, c.sku,
                               c.phan_loai, c.hinh_anh, c.don_gia, c.so_luong, c.thanh_tien
                        FROM chi_tiet_don_hang c
                        LEFT JOIN san_pham sp ON sp.id = c.san_pham_id AND sp.deleted_at IS NULL
                        WHERE c.don_hang_id = :id ORDER BY c.id
                        """)
                .param("id", donHangId).query(DonHangResponse.Dong.class).list();
        List<DonHangResponse.LichSu> lichSu = jdbc.sql("""
                        SELECT ls.trang_thai, ls.ghi_chu, ls.created_at AS thoi_gian,
                               CASE WHEN :quanTri THEN nd.ho_ten END AS nguoi_thuc_hien
                        FROM lich_su_don_hang ls LEFT JOIN nguoi_dung nd ON nd.id = ls.nguoi_thuc_hien_id
                        WHERE ls.don_hang_id = :id ORDER BY ls.created_at, ls.id
                        """)
                .param("id", donHangId).param("quanTri", quanTri).query(DonHangResponse.LichSu.class).list();
        boolean coTheHuy = choPhepHuy && TrangThaiDonHang.cho_xac_nhan.name().equals(d.trangThai());
        return new DonHangResponse.ChiTiet(d.maDonHang(), d.trangThai(), d.trangThaiThanhToan(),
                d.phuongThucThanhToan(), d.tenNguoiNhan(), d.email(), d.soDienThoai(), d.diaChiGiaoHang(), d.ghiChu(),
                d.maGiamGia(), d.tamTinh(), d.tienGiamGia(), d.phiVanChuyen(), d.tongTien(), d.ngayDat(), coTheHuy,
                dongs, lichSu);
    }
}
