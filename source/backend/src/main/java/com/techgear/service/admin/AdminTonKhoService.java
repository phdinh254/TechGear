package com.techgear.service.admin;

import com.techgear.dto.PageResponse;
import com.techgear.dto.admin.AdminTonKhoResponse;
import com.techgear.dto.admin.DieuChinhKhoRequest;
import com.techgear.dto.admin.LichSuTonKhoResponse;
import com.techgear.dto.admin.NhapKhoRequest;
import com.techgear.entity.BienTheSanPham;
import com.techgear.entity.GiaTriTuyChon;
import com.techgear.entity.LichSuTonKho;
import com.techgear.entity.enums.LoaiBienDongKho;
import com.techgear.exception.ApiException;
import com.techgear.repository.BienTheSanPhamRepository;
import com.techgear.repository.LichSuTonKhoRepository;
import com.techgear.repository.NguoiDungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminTonKhoService {

    private static final int KICH_THUOC_TOI_DA = 50;
    private static final List<String> TRANG_THAI = List.of("con_hang", "sap_het", "het_hang");

    private final BienTheSanPhamRepository bienTheSanPhamRepository;
    private final LichSuTonKhoRepository lichSuTonKhoRepository;
    private final NguoiDungRepository nguoiDungRepository;

    @Transactional(readOnly = true)
    public PageResponse<AdminTonKhoResponse> danhSach(String tuKhoa, String trangThai, int trang, int kichThuoc) {
        if (StringUtils.hasText(trangThai) && !TRANG_THAI.contains(trangThai)) {
            throw ApiException.yeuCauKhongHopLe("Trạng thái không hợp lệ");
        }
        String mau = StringUtils.hasText(tuKhoa) ? "%" + tuKhoa.trim().toLowerCase(Locale.ROOT) + "%" : null;
        String tt = StringUtils.hasText(trangThai) ? trangThai : null;
        return PageResponse.tu(bienTheSanPhamRepository.timChoQuanTri(mau, tt, taoPageable(trang, kichThuoc))
                .map(this::sangResponse));
    }

    @Transactional
    public AdminTonKhoResponse nhapKho(Long bienTheId, NhapKhoRequest req, Long adminId) {
        BienTheSanPham bienThe = layDeGhi(bienTheId);
        bienThe.setSoLuongTon(bienThe.getSoLuongTon() + req.soLuong());
        ghiLichSu(bienThe, LoaiBienDongKho.nhap_kho, req.soLuong(),
                StringUtils.hasText(req.ghiChu()) ? req.ghiChu().trim() : null, adminId);
        return sangResponse(bienThe);
    }

    @Transactional
    public AdminTonKhoResponse dieuChinh(Long bienTheId, DieuChinhKhoRequest req, Long adminId) {
        BienTheSanPham bienThe = layDeGhi(bienTheId);
        int chenhLech = req.soLuongThucTe() - bienThe.getSoLuongTon();
        if (chenhLech == 0) {
            throw ApiException.yeuCauKhongHopLe("Số lượng thực tế bằng tồn kho hiện tại, không cần điều chỉnh");
        }
        bienThe.setSoLuongTon(req.soLuongThucTe());
        ghiLichSu(bienThe, LoaiBienDongKho.dieu_chinh, chenhLech, req.lyDo().trim(), adminId);
        return sangResponse(bienThe);
    }

    @Transactional(readOnly = true)
    public PageResponse<LichSuTonKhoResponse> lichSu(Long bienTheId, int trang, int kichThuoc) {
        return PageResponse.tu(lichSuTonKhoRepository
                .findByBienTheIdOrderByCreatedAtDescIdDesc(bienTheId, taoPageable(trang, kichThuoc))
                .map(ls -> new LichSuTonKhoResponse(ls.getId(), ls.getCreatedAt(), ls.getLoai().name(),
                        ls.getSoLuongThayDoi(), ls.getSoLuongSau(), ls.getGhiChu(),
                        ls.getNguoiThucHien() != null ? ls.getNguoiThucHien().getHoTen() : null,
                        ls.getChiTietDonHang() != null ? ls.getChiTietDonHang().getDonHang().getMaDonHang() : null)));
    }

    public static String trangThai(BienTheSanPham bt) {
        int ton = bt.getSoLuongTon();
        if (ton <= 0) {
            return "het_hang";
        }
        return ton <= bt.getNguongCanhBao() ? "sap_het" : "con_hang";
    }

    private static PageRequest taoPageable(int trang, int kichThuoc) {
        return PageRequest.of(Math.max(trang, 0), Math.min(Math.max(kichThuoc, 1), KICH_THUOC_TOI_DA));
    }

    private BienTheSanPham layDeGhi(Long bienTheId) {
        return bienTheSanPhamRepository.findByIdForUpdate(bienTheId)
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy biến thể sản phẩm"));
    }

    private void ghiLichSu(BienTheSanPham bienThe, LoaiBienDongKho loai, int thayDoi, String ghiChu, Long adminId) {
        LichSuTonKho ls = new LichSuTonKho();
        ls.setBienThe(bienThe);
        ls.setLoai(loai);
        ls.setSoLuongThayDoi(thayDoi);
        ls.setSoLuongSau(bienThe.getSoLuongTon());
        ls.setGhiChu(ghiChu);
        ls.setNguoiThucHien(nguoiDungRepository.getReferenceById(adminId));
        lichSuTonKhoRepository.save(ls);
    }

    private AdminTonKhoResponse sangResponse(BienTheSanPham bt) {
        String phanLoai = bt.getGiaTris().stream()
                .sorted(Comparator.comparing(gt -> gt.getTuyChon().getThuTu()))
                .map(GiaTriTuyChon::getGiaTri)
                .reduce((a, b) -> a + " / " + b)
                .orElse("");
        return new AdminTonKhoResponse(bt.getId(), bt.getSanPham().getId(), bt.getSanPham().getTen(), phanLoai,
                bt.getSku(), bt.getSoLuongTon(), bt.getNguongCanhBao(), trangThai(bt),
                bt.getSanPham().dangBan() && Boolean.TRUE.equals(bt.getHienThi()));
    }
}
