package com.techgear.giohang;

import com.techgear.chung.loi.ApiException;
import com.techgear.taikhoan.NguoiDungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Giỏ hàng trên server. Mọi thao tác ghi khóa dòng người dùng để hai yêu cầu cùng lúc không vượt giới hạn. */
@Service
@RequiredArgsConstructor
public class GioHangService {

    static final int TOI_DA_DONG = 50;
    static final int TOI_DA_SO_LUONG = 999;

    private final GioHangRepository gioHangRepository;
    private final NguoiDungRepository nguoiDungRepository;

    @Transactional(readOnly = true)
    public GioHangResponse xem(Long nguoiDungId) {
        return GioHangResponse.tu(gioHangRepository.dongs(nguoiDungId));
    }

    /** Thêm vào giỏ, cộng dồn nếu đã có. */
    @Transactional
    public GioHangResponse them(Long nguoiDungId, Long bienTheId, int soLuong) {
        nguoiDungRepository.khoaTheoId(nguoiDungId);
        int ton = tonKhoDangBan(bienTheId);
        Optional<Integer> daCo = gioHangRepository.soLuongTrongGio(nguoiDungId, bienTheId);
        if (daCo.isEmpty() && gioHangRepository.soDong(nguoiDungId) >= TOI_DA_DONG) {
            throw ApiException.yeuCauKhongHopLe("Giỏ hàng tối đa " + TOI_DA_DONG + " sản phẩm");
        }
        int moi = daCo.orElse(0) + soLuong;
        kiemTraSoLuong(ton, moi);
        gioHangRepository.datSoLuong(nguoiDungId, bienTheId, moi);
        return xem(nguoiDungId);
    }

    @Transactional
    public GioHangResponse sua(Long nguoiDungId, Long bienTheId, int soLuong) {
        nguoiDungRepository.khoaTheoId(nguoiDungId);
        if (gioHangRepository.soLuongTrongGio(nguoiDungId, bienTheId).isEmpty()) {
            throw ApiException.khongTimThay("Sản phẩm không có trong giỏ hàng");
        }
        kiemTraSoLuong(tonKhoDangBan(bienTheId), soLuong);
        gioHangRepository.datSoLuong(nguoiDungId, bienTheId, soLuong);
        return xem(nguoiDungId);
    }

    @Transactional
    public GioHangResponse xoa(Long nguoiDungId, Long bienTheId) {
        gioHangRepository.xoa(nguoiDungId, bienTheId);
        return xem(nguoiDungId);
    }

    /**
     * Gộp giỏ tạm của trình duyệt khi đăng nhập: cộng dồn, cắt theo tồn kho; bỏ dòng không còn bán,
     * hết hàng hoặc vượt số dòng tối đa (không báo lỗi từng dòng).
     */
    @Transactional
    public GioHangResponse gop(Long nguoiDungId, List<DongGioRequest> gioTam) {
        nguoiDungRepository.khoaTheoId(nguoiDungId);
        Map<Long, Integer> gom = new LinkedHashMap<>();
        gioTam.forEach(d -> gom.merge(d.bienTheId(), d.soLuong(), Integer::sum));

        int soDong = gioHangRepository.soDong(nguoiDungId);
        for (Map.Entry<Long, Integer> d : gom.entrySet()) {
            Optional<Integer> ton = gioHangRepository.tonKhoDangBan(d.getKey());
            if (ton.isEmpty() || ton.get() == 0) {
                continue;
            }
            Optional<Integer> daCo = gioHangRepository.soLuongTrongGio(nguoiDungId, d.getKey());
            if (daCo.isEmpty() && soDong >= TOI_DA_DONG) {
                continue;
            }
            int moi = Math.min(daCo.orElse(0) + d.getValue(), Math.min(ton.get(), TOI_DA_SO_LUONG));
            gioHangRepository.datSoLuong(nguoiDungId, d.getKey(), moi);
            if (daCo.isEmpty()) {
                soDong++;
            }
        }
        return xem(nguoiDungId);
    }

    private int tonKhoDangBan(Long bienTheId) {
        return gioHangRepository.tonKhoDangBan(bienTheId)
                .orElseThrow(() -> ApiException.khongTimThay("Phiên bản sản phẩm không còn bán"));
    }

    private static void kiemTraSoLuong(int ton, int soLuong) {
        if (ton == 0) {
            throw ApiException.xungDot("Sản phẩm đã hết hàng");
        }
        if (soLuong > ton) {
            throw ApiException.xungDot("Chỉ còn " + ton + " sản phẩm trong kho");
        }
        if (soLuong > TOI_DA_SO_LUONG) {
            throw ApiException.yeuCauKhongHopLe("Mỗi sản phẩm tối đa " + TOI_DA_SO_LUONG + " cái");
        }
    }
}
