package com.techgear.service;

import com.techgear.dto.GioHangItemRequest;
import com.techgear.dto.GioHangResponse;
import com.techgear.entity.BienTheSanPham;
import com.techgear.entity.ChiTietGioHang;
import com.techgear.entity.GiaTriTuyChon;
import com.techgear.entity.GioHang;
import com.techgear.entity.HinhAnhSanPham;
import com.techgear.entity.NguoiDung;
import com.techgear.entity.SanPham;
import com.techgear.exception.ApiException;
import com.techgear.repository.BienTheSanPhamRepository;
import com.techgear.repository.ChiTietGioHangRepository;
import com.techgear.repository.GioHangRepository;
import com.techgear.repository.NguoiDungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GioHangService {

    private final GioHangRepository gioHangRepository;
    private final ChiTietGioHangRepository chiTietRepository;
    private final BienTheSanPhamRepository bienTheRepository;
    private final NguoiDungRepository nguoiDungRepository;

    @Transactional(readOnly = true)
    public GioHangResponse layGioHang(Long nguoiDungId) {
        return gioHangRepository.findByNguoiDungId(nguoiDungId)
                .map(this::sangResponse)
                .orElseGet(() -> new GioHangResponse(List.of(), 0, BigDecimal.ZERO));
    }

    @Transactional
    public GioHangResponse them(Long nguoiDungId, GioHangItemRequest request) {
        GioHang gioHang = layHoacTaoGioHang(nguoiDungId);
        BienTheSanPham bienThe = timBienTheCoTheMua(request.bienTheId());
        ChiTietGioHang chiTiet = chiTietRepository
                .findByGioHangIdAndBienTheId(gioHang.getId(), bienThe.getId())
                .orElseGet(() -> taoChiTiet(gioHang, bienThe));
        int soLuongMoi = Math.min(chiTiet.getSoLuong() + request.soLuong(), bienThe.getSoLuongTon());
        chiTiet.setSoLuong(soLuongMoi);
        chiTietRepository.save(chiTiet);
        return sangResponse(gioHangRepository.findById(gioHang.getId()).orElseThrow());
    }

    @Transactional
    public GioHangResponse capNhat(Long nguoiDungId, Long bienTheId, int soLuong) {
        GioHang gioHang = timGioHang(nguoiDungId);
        ChiTietGioHang chiTiet = timChiTiet(gioHang.getId(), bienTheId);
        BienTheSanPham bienThe = timBienTheCoTheMua(bienTheId);
        chiTiet.setSoLuong(Math.min(soLuong, bienThe.getSoLuongTon()));
        return sangResponse(gioHang);
    }

    @Transactional
    public GioHangResponse xoa(Long nguoiDungId, Long bienTheId) {
        GioHang gioHang = timGioHang(nguoiDungId);
        ChiTietGioHang chiTiet = timChiTiet(gioHang.getId(), bienTheId);
        gioHang.getChiTiets().remove(chiTiet);
        return sangResponse(gioHang);
    }

    @Transactional
    public void xoaTatCa(Long nguoiDungId) {
        gioHangRepository.findByNguoiDungId(nguoiDungId).ifPresent(gioHang -> {
            gioHang.getChiTiets().clear();
            gioHangRepository.save(gioHang);
        });
    }

    @Transactional
    public GioHangResponse gop(Long nguoiDungId, List<GioHangItemRequest> items) {
        GioHang gioHang = layHoacTaoGioHang(nguoiDungId);
        for (GioHangItemRequest request : items) {
            BienTheSanPham bienThe = timBienTheCoTheMua(request.bienTheId());
            ChiTietGioHang chiTiet = chiTietRepository
                    .findByGioHangIdAndBienTheId(gioHang.getId(), bienThe.getId())
                    .orElseGet(() -> taoChiTiet(gioHang, bienThe));
            chiTiet.setSoLuong(Math.min(chiTiet.getSoLuong() + request.soLuong(), bienThe.getSoLuongTon()));
            chiTietRepository.save(chiTiet);
        }
        return sangResponse(gioHangRepository.findById(gioHang.getId()).orElseThrow());
    }

    private GioHang layHoacTaoGioHang(Long nguoiDungId) {
        return gioHangRepository.findByNguoiDungId(nguoiDungId).orElseGet(() -> {
            NguoiDung nguoiDung = nguoiDungRepository.findById(nguoiDungId)
                    .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy người dùng"));
            GioHang gioHang = new GioHang();
            gioHang.setNguoiDung(nguoiDung);
            return gioHangRepository.save(gioHang);
        });
    }

    private GioHang timGioHang(Long nguoiDungId) {
        return gioHangRepository.findByNguoiDungId(nguoiDungId)
                .orElseThrow(() -> ApiException.khongTimThay("Giỏ hàng đang trống"));
    }

    private ChiTietGioHang timChiTiet(Long gioHangId, Long bienTheId) {
        return chiTietRepository.findByGioHangIdAndBienTheId(gioHangId, bienTheId)
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy sản phẩm trong giỏ"));
    }

    private BienTheSanPham timBienTheCoTheMua(Long bienTheId) {
        BienTheSanPham bienThe = bienTheRepository.findById(bienTheId)
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy biến thể sản phẩm"));
        SanPham sanPham = bienThe.getSanPham();
        if (!Boolean.TRUE.equals(bienThe.getHienThi()) || !sanPham.dangBan()) {
            throw ApiException.khongTimThay("Sản phẩm không còn được bán");
        }
        if (bienThe.getSoLuongTon() <= 0) {
            throw new ApiException(HttpStatus.CONFLICT, "Sản phẩm đã hết hàng");
        }
        return bienThe;
    }

    private ChiTietGioHang taoChiTiet(GioHang gioHang, BienTheSanPham bienThe) {
        ChiTietGioHang chiTiet = new ChiTietGioHang();
        chiTiet.setGioHang(gioHang);
        chiTiet.setBienThe(bienThe);
        chiTiet.setSoLuong(0);
        gioHang.getChiTiets().add(chiTiet);
        return chiTiet;
    }

    private GioHangResponse sangResponse(GioHang gioHang) {
        List<GioHangResponse.DongGioHang> dong = gioHang.getChiTiets().stream()
                .map(this::sangDong)
                .toList();
        int tongSoLuong = dong.stream().mapToInt(GioHangResponse.DongGioHang::soLuong).sum();
        BigDecimal tamTinh = dong.stream()
                .map(item -> item.giaBan().multiply(BigDecimal.valueOf(item.soLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new GioHangResponse(dong, tongSoLuong, tamTinh);
    }

    private GioHangResponse.DongGioHang sangDong(ChiTietGioHang chiTiet) {
        BienTheSanPham bienThe = chiTiet.getBienThe();
        SanPham sanPham = bienThe.getSanPham();
        String phanLoai = bienThe.getGiaTris().stream()
                .sorted(Comparator.comparing(gt -> gt.getTuyChon().getThuTu()))
                .map(GiaTriTuyChon::getGiaTri)
                .reduce((a, b) -> a + " / " + b)
                .orElse("");
        String anh = sanPham.getHinhAnhs().stream()
                .filter(item -> Boolean.TRUE.equals(item.getLaAnhChinh()))
                .map(HinhAnhSanPham::getDuongDan)
                .findFirst()
                .orElse(sanPham.getHinhAnhs().isEmpty() ? null : sanPham.getHinhAnhs().get(0).getDuongDan());
        boolean conBan = sanPham.dangBan()
                && Boolean.TRUE.equals(bienThe.getHienThi()) && bienThe.getSoLuongTon() > 0;
        return new GioHangResponse.DongGioHang(
                chiTiet.getId(), bienThe.getId(), sanPham.getSlug(), sanPham.getTen(), phanLoai,
                bienThe.layGiaBan(), anh, bienThe.getSoLuongTon(), chiTiet.getSoLuong(), conBan
        );
    }
}
