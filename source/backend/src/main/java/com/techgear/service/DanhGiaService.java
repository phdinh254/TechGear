package com.techgear.service;

import com.techgear.dto.DanhGiaResponse;
import com.techgear.dto.DanhGiaRequest;
import com.techgear.dto.DanhGiaSanPhamResponse;
import com.techgear.dto.PageResponse;
import com.techgear.entity.SanPham;
import com.techgear.entity.DanhGia;
import com.techgear.entity.DonHang;
import com.techgear.entity.NguoiDung;
import com.techgear.entity.enums.TrangThaiDonHang;
import com.techgear.exception.ApiException;
import com.techgear.repository.DanhGiaRepository;
import com.techgear.repository.SanPhamRepository;
import com.techgear.repository.DonHangRepository;
import com.techgear.repository.NguoiDungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class DanhGiaService {

    private final SanPhamRepository sanPhamRepository;
    private final DanhGiaRepository danhGiaRepository;
    private final DonHangRepository donHangRepository;
    private final NguoiDungRepository nguoiDungRepository;

    @Transactional(readOnly = true)
    public DanhGiaSanPhamResponse layTheoSanPham(String slug, int trang, int kichThuoc) {
        SanPham sanPham = sanPhamRepository.findBySlugAndHienThiTrue(slug)
                .filter(SanPham::dangBan)
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy sản phẩm"));

        Page<DanhGiaResponse> danhSach = danhGiaRepository
                .findBySanPhamIdAndDaDuyetTrueOrderByCreatedAtDesc(
                        sanPham.getId(), PageRequest.of(trang, kichThuoc))
                .map(DanhGiaResponse::tu);
        Double diemTrungBinh = danhGiaRepository.tinhDiemTrungBinhDaDuyet(sanPham.getId());

        return new DanhGiaSanPhamResponse(
                diemTrungBinh == null ? 0 : Math.round(diemTrungBinh * 10.0) / 10.0,
                danhSach.getTotalElements(),
                PageResponse.tu(danhSach)
        );
    }

    @Transactional
    public DanhGiaResponse tao(Long nguoiDungId, DanhGiaRequest request) {
        DonHang donHang = donHangRepository.findByIdAndNguoiDungId(request.donHangId(), nguoiDungId)
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy đơn hàng"));
        if (donHang.getTrangThai() != TrangThaiDonHang.hoan_thanh) {
            throw new ApiException(HttpStatus.CONFLICT, "Chỉ có thể đánh giá đơn hàng đã hoàn thành");
        }
        boolean daMua = donHang.getChiTiets().stream()
                .anyMatch(item -> item.getSanPham() != null && item.getSanPham().getId().equals(request.sanPhamId()));
        if (!daMua) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Sản phẩm không thuộc đơn hàng này");
        }
        if (danhGiaRepository.existsByNguoiDungIdAndSanPhamIdAndDonHangId(
                nguoiDungId, request.sanPhamId(), request.donHangId())) {
            throw new ApiException(HttpStatus.CONFLICT, "Bạn đã đánh giá sản phẩm này trong đơn hàng");
        }
        NguoiDung nguoiDung = nguoiDungRepository.findById(nguoiDungId)
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy người dùng"));
        SanPham sanPham = sanPhamRepository.findById(request.sanPhamId())
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy sản phẩm"));
        DanhGia danhGia = new DanhGia();
        danhGia.setNguoiDung(nguoiDung);
        danhGia.setSanPham(sanPham);
        danhGia.setDonHang(donHang);
        danhGia.setSoSao(request.soSao());
        danhGia.setNoiDung(StringUtils.hasText(request.noiDung()) ? request.noiDung().trim() : null);
        danhGia.setDaDuyet(false);
        return DanhGiaResponse.tu(danhGiaRepository.saveAndFlush(danhGia));
    }
}
