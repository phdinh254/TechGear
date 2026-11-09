package com.techgear.service;

import com.techgear.dto.DonHangResponse;
import com.techgear.dto.PageResponse;
import com.techgear.entity.*;
import com.techgear.entity.enums.LoaiBienDongKho;
import com.techgear.entity.enums.TrangThaiDonHang;
import com.techgear.exception.ApiException;
import com.techgear.repository.BienTheSanPhamRepository;
import com.techgear.repository.DonHangRepository;
import com.techgear.repository.LichSuTonKhoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class DonHangKhachHangService {

    private final DonHangRepository donHangRepository;
    private final BienTheSanPhamRepository bienTheRepository;
    private final LichSuTonKhoRepository lichSuTonKhoRepository;

    @Transactional(readOnly = true)
    public PageResponse<DonHangResponse> layDanhSach(Long nguoiDungId, int trang, int kichThuoc) {
        Page<DonHangResponse> page = donHangRepository
                .findByNguoiDungIdOrderByCreatedAtDesc(nguoiDungId, PageRequest.of(trang, kichThuoc))
                .map(order -> DonHangResponse.tu(order, false));
        return PageResponse.tu(page);
    }

    @Transactional(readOnly = true)
    public DonHangResponse layChiTiet(Long nguoiDungId, String maDonHang) {
        return DonHangResponse.tu(timCuaNguoiDung(nguoiDungId, maDonHang), true);
    }

    @Transactional
    public DonHangResponse huy(Long nguoiDungId, String maDonHang, String lyDo) {
        DonHang donHang = donHangRepository.findCuaNguoiDungForUpdate(maDonHang, nguoiDungId)
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy đơn hàng"));
        if (donHang.getTrangThai() != TrangThaiDonHang.cho_xac_nhan) {
            throw new ApiException(HttpStatus.CONFLICT, "Chỉ có thể hủy đơn đang chờ xác nhận");
        }

        NguoiDung nguoiDung = donHang.getNguoiDung();
        for (ChiTietDonHang chiTiet : donHang.getChiTiets()) {
            if (chiTiet.getBienThe() == null) continue;
            BienTheSanPham bienThe = bienTheRepository.findByIdForUpdate(chiTiet.getBienThe().getId())
                    .orElse(null);
            if (bienThe == null) continue;
            bienThe.setSoLuongTon(bienThe.getSoLuongTon() + chiTiet.getSoLuong());
            LichSuTonKho lichSuTon = new LichSuTonKho();
            lichSuTon.setBienThe(bienThe);
            lichSuTon.setChiTietDonHang(chiTiet);
            lichSuTon.setLoai(LoaiBienDongKho.huy_don);
            lichSuTon.setSoLuongThayDoi(chiTiet.getSoLuong());
            lichSuTon.setSoLuongSau(bienThe.getSoLuongTon());
            lichSuTon.setGhiChu("Hoàn kho do khách hủy đơn " + donHang.getMaDonHang());
            lichSuTon.setNguoiThucHien(nguoiDung);
            lichSuTonKhoRepository.save(lichSuTon);
        }
        donHang.setTrangThai(TrangThaiDonHang.da_huy);
        if (donHang.getMaGiamGia() != null && donHang.getMaGiamGia().getDaDung() > 0) {
            donHang.getMaGiamGia().setDaDung(donHang.getMaGiamGia().getDaDung() - 1);
        }
        LichSuDonHang lichSu = new LichSuDonHang();
        lichSu.setDonHang(donHang);
        lichSu.setTrangThai(TrangThaiDonHang.da_huy);
        lichSu.setGhiChu(StringUtils.hasText(lyDo) ? lyDo.trim() : "Khách hàng hủy đơn");
        lichSu.setNguoiThucHien(nguoiDung);
        donHang.getLichSus().add(lichSu);
        return DonHangResponse.tu(donHang, true);
    }

    private DonHang timCuaNguoiDung(Long nguoiDungId, String maDonHang) {
        return donHangRepository.findByMaDonHangAndNguoiDungId(maDonHang, nguoiDungId)
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy đơn hàng"));
    }
}
