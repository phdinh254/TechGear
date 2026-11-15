package com.techgear.giohang;

import com.techgear.chung.loi.ApiException;
import com.techgear.sanpham.SanPhamService;
import com.techgear.sanpham.SanPhamTomTat;
import com.techgear.taikhoan.NguoiDungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Danh sách yêu thích: thêm lại sản phẩm đã có hay bỏ sản phẩm không có đều không lỗi. */
@Service
@RequiredArgsConstructor
public class YeuThichService {

    static final int TOI_DA = 100;

    private final JdbcClient jdbc;
    private final SanPhamService sanPhamService;
    private final NguoiDungRepository nguoiDungRepository;

    /** Yêu thích sau đứng trước; sản phẩm đã ẩn không hiện (vẫn giữ trong bảng). */
    @Transactional(readOnly = true)
    public List<SanPhamTomTat> danhSach(Long nguoiDungId) {
        List<Long> ids = jdbc.sql("""
                        SELECT san_pham_id FROM yeu_thich WHERE nguoi_dung_id = :nd
                        ORDER BY created_at DESC, san_pham_id DESC
                        """)
                .param("nd", nguoiDungId).query(Long.class).list();
        return sanPhamService.tomTatTheoThuTu(ids);
    }

    @Transactional
    public void them(Long nguoiDungId, Long sanPhamId) {
        if (!sanPhamService.dangHienThi(sanPhamId)) {
            throw ApiException.khongTimThay("Không tìm thấy sản phẩm");
        }
        nguoiDungRepository.khoaTheoId(nguoiDungId);
        boolean daCo = jdbc.sql("SELECT EXISTS (SELECT 1 FROM yeu_thich WHERE nguoi_dung_id = :nd AND san_pham_id = :sp)")
                .param("nd", nguoiDungId).param("sp", sanPhamId).query(Boolean.class).single();
        if (daCo) {
            return;
        }
        int soLuong = jdbc.sql("SELECT count(*) FROM yeu_thich WHERE nguoi_dung_id = :nd")
                .param("nd", nguoiDungId).query(Integer.class).single();
        if (soLuong >= TOI_DA) {
            throw ApiException.yeuCauKhongHopLe("Danh sách yêu thích tối đa " + TOI_DA + " sản phẩm");
        }
        jdbc.sql("INSERT INTO yeu_thich (nguoi_dung_id, san_pham_id, created_at) VALUES (:nd, :sp, clock_timestamp())")
                .param("nd", nguoiDungId).param("sp", sanPhamId).update();
    }

    @Transactional
    public void bo(Long nguoiDungId, Long sanPhamId) {
        jdbc.sql("DELETE FROM yeu_thich WHERE nguoi_dung_id = :nd AND san_pham_id = :sp")
                .param("nd", nguoiDungId).param("sp", sanPhamId).update();
    }
}
