package com.techgear.danhgia;

import com.techgear.chung.PageResponse;
import com.techgear.chung.loi.ApiException;
import com.techgear.sanpham.HienThi;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Khách viết đánh giá cho sản phẩm trong đơn đã hoàn thành (mỗi sản phẩm mỗi đơn một lần), chờ quản trị duyệt. */
@Service
@RequiredArgsConstructor
public class DanhGiaService {

    private final JdbcClient jdbc;

    private record Don(Long id, String trangThai) {
    }

    @Transactional
    public DanhGiaDto.DanhGia viet(Long nguoiDungId, DanhGiaDto.VietRequest req) {
        Don don = jdbc.sql("SELECT id, trang_thai FROM don_hang WHERE ma_don_hang = upper(:ma) AND nguoi_dung_id = :nd")
                .param("ma", req.maDonHang().trim()).param("nd", nguoiDungId).query(Don.class).optional()
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy đơn hàng"));
        boolean coSanPham = jdbc.sql("""
                        SELECT EXISTS (SELECT 1 FROM chi_tiet_don_hang WHERE don_hang_id = :dh AND san_pham_id = :sp)
                        """)
                .param("dh", don.id()).param("sp", req.sanPhamId()).query(Boolean.class).single();
        if (!coSanPham) {
            throw ApiException.khongTimThay("Sản phẩm không có trong đơn hàng");
        }
        if (!"hoan_thanh".equals(don.trangThai())) {
            throw ApiException.xungDot("Chỉ đánh giá được đơn đã hoàn thành");
        }
        String noiDung = req.noiDung() == null || req.noiDung().isBlank() ? null : req.noiDung().trim();
        // ON CONFLICT: hai lần gửi cùng lúc chỉ một lần được ghi
        return jdbc.sql("""
                        INSERT INTO danh_gia (san_pham_id, nguoi_dung_id, don_hang_id, so_sao, noi_dung, created_at)
                        VALUES (:sp, :nd, :dh, :soSao, :noiDung, clock_timestamp())
                        ON CONFLICT (nguoi_dung_id, san_pham_id, don_hang_id) DO NOTHING
                        RETURNING id, san_pham_id, so_sao, noi_dung, trang_thai, created_at AS ngay_tao
                        """)
                .param("sp", req.sanPhamId()).param("nd", nguoiDungId).param("dh", don.id())
                .param("soSao", req.soSao()).param("noiDung", noiDung)
                .query(DanhGiaDto.DanhGia.class).optional()
                .orElseThrow(() -> ApiException.xungDot("Bạn đã đánh giá sản phẩm này trong đơn hàng này"));
    }

    /** Dòng của đơn đã hoàn thành, sản phẩm còn tồn tại, chưa đánh giá; đơn mới trước. */
    @Transactional(readOnly = true)
    public List<DanhGiaDto.CoTheViet> coTheViet(Long nguoiDungId) {
        return jdbc.sql("""
                        SELECT DISTINCT ON (dh.created_at, dh.id, c.san_pham_id)
                               dh.ma_don_hang, c.san_pham_id, sp.slug AS san_pham_slug, c.ten_san_pham AS ten_san_pham,
                               c.hinh_anh, dh.created_at AS ngay_dat
                        FROM don_hang dh
                        JOIN chi_tiet_don_hang c ON c.don_hang_id = dh.id
                        JOIN san_pham sp ON sp.id = c.san_pham_id AND sp.deleted_at IS NULL
                        WHERE dh.nguoi_dung_id = :nd AND dh.trang_thai = 'hoan_thanh'
                          AND NOT EXISTS (SELECT 1 FROM danh_gia dg WHERE dg.nguoi_dung_id = :nd
                                          AND dg.don_hang_id = dh.id AND dg.san_pham_id = c.san_pham_id)
                        ORDER BY dh.created_at DESC, dh.id DESC, c.san_pham_id, c.id
                        """)
                .param("nd", nguoiDungId).query(DanhGiaDto.CoTheViet.class).list();
    }

    /** Đánh giá đã duyệt của sản phẩm đang hiển thị, mới nhất trước. */
    @Transactional(readOnly = true)
    public PageResponse<DanhGiaDto.CongKhai> cuaSanPham(String slug, int trang, int kichThuoc) {
        Long sanPhamId = jdbc.sql("SELECT sp.id " + HienThi.TU_BANG + " WHERE " + HienThi.SAN_PHAM_DANG_HIEN_THI
                        + " AND sp.slug = :slug")
                .param("slug", slug).query(Long.class).optional()
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy sản phẩm"));
        long tong = jdbc.sql("SELECT count(*) FROM danh_gia WHERE san_pham_id = :sp AND trang_thai = 'da_duyet'")
                .param("sp", sanPhamId).query(Long.class).single();
        List<DanhGiaDto.CongKhai> noiDung = tong == 0 ? List.of() : jdbc.sql("""
                        SELECT dg.id, nd.ho_ten, dg.so_sao, dg.noi_dung, dg.created_at AS ngay_tao
                        FROM danh_gia dg JOIN nguoi_dung nd ON nd.id = dg.nguoi_dung_id
                        WHERE dg.san_pham_id = :sp AND dg.trang_thai = 'da_duyet'
                        ORDER BY dg.created_at DESC, dg.id DESC LIMIT :gioiHan OFFSET :boQua
                        """)
                .param("sp", sanPhamId).param("gioiHan", kichThuoc).param("boQua", (long) trang * kichThuoc)
                .query(DanhGiaDto.CongKhai.class).list();
        return new PageResponse<>(noiDung, trang, kichThuoc, tong, (int) ((tong + kichThuoc - 1) / kichThuoc));
    }
}
