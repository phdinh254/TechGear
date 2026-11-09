package com.techgear.service;

import com.techgear.dto.DatHangRequest;
import com.techgear.dto.DatHangResponse;
import com.techgear.dto.GioHangItemRequest;
import com.techgear.dto.TinhTienRequest;
import com.techgear.dto.TinhTienResponse;
import com.techgear.entity.*;
import com.techgear.entity.enums.*;
import com.techgear.exception.ApiException;
import com.techgear.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DatHangService {

    private static final String PHI_VAN_CHUYEN = "phi_van_chuyen";
    private static final String MIEN_PHI_VAN_CHUYEN_TU = "mien_phi_van_chuyen_tu";

    private final NguoiDungRepository nguoiDungRepository;
    private final GioHangRepository gioHangRepository;
    private final BienTheSanPhamRepository bienTheRepository;
    private final MaGiamGiaRepository maGiamGiaRepository;
    private final CaiDatRepository caiDatRepository;
    private final DonHangRepository donHangRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final LichSuTonKhoRepository lichSuTonKhoRepository;

    @Transactional(readOnly = true)
    public TinhTienResponse tinhTien(Long nguoiDungId, TinhTienRequest request) {
        List<ItemDatHang> items = layItems(nguoiDungId, request.items(), false);
        return tinhTien(items, request.maGiamGia(), nguoiDungId).response();
    }

    @Transactional
    public DatHangResponse datHang(Long nguoiDungId, DatHangRequest request) {
        if (request.phuongThucThanhToan() == PhuongThucThanhToan.vnpay) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "VNPay chưa được cấu hình. Vui lòng chọn thanh toán khi nhận hàng");
        }

        NguoiDung nguoiDung = nguoiDungId == null ? null : nguoiDungRepository.findById(nguoiDungId)
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy người dùng"));
        List<ItemDatHang> items = layItems(nguoiDungId, request.items(), true);
        KetQuaTinhTien ketQua = tinhTien(items, request.maGiamGia(), nguoiDungId);

        DonHang donHang = new DonHang();
        donHang.setMaDonHang(taoMaDonHang());
        donHang.setNguoiDung(nguoiDung);
        donHang.setMaGiamGia(ketQua.maGiamGia());
        donHang.setTenNguoiNhan(request.tenNguoiNhan().trim());
        donHang.setEmail(request.email().trim().toLowerCase(Locale.ROOT));
        donHang.setSoDienThoai(request.soDienThoai().trim());
        donHang.setDiaChiGiaoHang(request.diaChiGiaoHang().trim());
        donHang.setGhiChu(chuanHoaRong(request.ghiChu()));
        donHang.setTamTinh(ketQua.response().tamTinh());
        donHang.setTienGiamGia(ketQua.response().tienGiamGia());
        donHang.setPhiVanChuyen(ketQua.response().phiVanChuyen());
        donHang.setTongTien(ketQua.response().tongTien());
        donHang.setPhuongThucThanhToan(request.phuongThucThanhToan());

        for (ItemDatHang item : items) {
            ChiTietDonHang chiTiet = taoChiTiet(donHang, item);
            donHang.getChiTiets().add(chiTiet);
        }
        LichSuDonHang lichSu = new LichSuDonHang();
        lichSu.setDonHang(donHang);
        lichSu.setTrangThai(TrangThaiDonHang.cho_xac_nhan);
        lichSu.setGhiChu("Khách hàng tạo đơn");
        lichSu.setNguoiThucHien(nguoiDung);
        donHang.getLichSus().add(lichSu);

        donHangRepository.saveAndFlush(donHang);
        truTonKho(donHang, items, nguoiDung);

        if (ketQua.maGiamGia() != null) {
            ketQua.maGiamGia().setDaDung(ketQua.maGiamGia().getDaDung() + 1);
        }

        ThanhToan thanhToan = new ThanhToan();
        thanhToan.setDonHang(donHang);
        thanhToan.setPhuongThuc(PhuongThucThanhToan.cod);
        thanhToan.setSoTien(donHang.getTongTien());
        thanhToan.setTrangThai(TrangThaiGiaoDich.cho_xu_ly);
        thanhToanRepository.save(thanhToan);

        if (nguoiDungId != null) {
            gioHangRepository.findByNguoiDungId(nguoiDungId).ifPresent(gioHang -> gioHang.getChiTiets().clear());
        }

        return new DatHangResponse(donHang.getId(), donHang.getMaDonHang(), donHang.getTongTien(),
                donHang.getPhuongThucThanhToan().name(), donHang.getTrangThai().name(), null);
    }

    private List<ItemDatHang> layItems(Long nguoiDungId, List<GioHangItemRequest> requestItems, boolean khoa) {
        List<GioHangItemRequest> nguon;
        if (nguoiDungId != null) {
            GioHang gioHang = gioHangRepository.findByNguoiDungId(nguoiDungId)
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Giỏ hàng đang trống"));
            nguon = gioHang.getChiTiets().stream()
                    .map(item -> new GioHangItemRequest(item.getBienThe().getId(), item.getSoLuong()))
                    .toList();
        } else {
            nguon = requestItems == null ? List.of() : requestItems;
        }
        if (nguon.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Giỏ hàng đang trống");
        }

        Map<Long, Integer> soLuongTheoBienThe = new TreeMap<>();
        nguon.forEach(item -> soLuongTheoBienThe.merge(item.bienTheId(), item.soLuong(), Integer::sum));
        List<ItemDatHang> ketQua = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : soLuongTheoBienThe.entrySet()) {
            BienTheSanPham bienThe = khoa
                    ? bienTheRepository.findByIdForUpdate(entry.getKey()).orElseThrow(
                    () -> ApiException.khongTimThay("Không tìm thấy biến thể sản phẩm"))
                    : bienTheRepository.findById(entry.getKey()).orElseThrow(
                    () -> ApiException.khongTimThay("Không tìm thấy biến thể sản phẩm"));
            if (!Boolean.TRUE.equals(bienThe.getHienThi())
                    || !Boolean.TRUE.equals(bienThe.getSanPham().getHienThi())) {
                throw new ApiException(HttpStatus.CONFLICT, "Sản phẩm không còn được bán: " + bienThe.getSku());
            }
            if (entry.getValue() > bienThe.getSoLuongTon()) {
                throw new ApiException(HttpStatus.CONFLICT,
                        "Sản phẩm " + bienThe.getSku() + " chỉ còn " + bienThe.getSoLuongTon());
            }
            ketQua.add(new ItemDatHang(bienThe, entry.getValue()));
        }
        return ketQua;
    }

    private KetQuaTinhTien tinhTien(List<ItemDatHang> items, String maNhap, Long nguoiDungId) {
        BigDecimal tamTinh = items.stream()
                .map(item -> item.bienThe().layGiaBan().multiply(BigDecimal.valueOf(item.soLuong())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        MaGiamGia maGiamGia = null;
        BigDecimal tienGiam = BigDecimal.ZERO;
        if (StringUtils.hasText(maNhap)) {
            maGiamGia = kiemTraMaGiamGia(maNhap.trim(), tamTinh, nguoiDungId);
            tienGiam = tinhGiamGia(maGiamGia, tamTinh);
        }
        BigDecimal phiVanChuyenMacDinh = layCaiDatSo(PHI_VAN_CHUYEN, new BigDecimal("30000"));
        BigDecimal nguongMienPhi = layCaiDatSo(MIEN_PHI_VAN_CHUYEN_TU, new BigDecimal("500000"));
        BigDecimal phiVanChuyen = tamTinh.compareTo(nguongMienPhi) >= 0 ? BigDecimal.ZERO : phiVanChuyenMacDinh;
        BigDecimal tongTien = tamTinh.subtract(tienGiam).add(phiVanChuyen);
        TinhTienResponse response = new TinhTienResponse(tamTinh, tienGiam, phiVanChuyen, tongTien,
                maGiamGia == null ? null : maGiamGia.getMa(), maGiamGia == null ? null : maGiamGia.getMoTa());
        return new KetQuaTinhTien(response, maGiamGia);
    }

    private MaGiamGia kiemTraMaGiamGia(String ma, BigDecimal tamTinh, Long nguoiDungId) {
        MaGiamGia coupon = maGiamGiaRepository.findByMaIgnoreCase(ma)
                .orElseThrow(() -> ApiException.khongTimThay("Mã giảm giá không tồn tại"));
        LocalDateTime hienTai = LocalDateTime.now();
        if (!Boolean.TRUE.equals(coupon.getKichHoat())
                || hienTai.isBefore(coupon.getBatDauLuc()) || hienTai.isAfter(coupon.getKetThucLuc())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Mã giảm giá chưa có hiệu lực hoặc đã hết hạn");
        }
        if (tamTinh.compareTo(coupon.getDonToiThieu()) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Đơn hàng chưa đạt giá trị tối thiểu của mã giảm giá");
        }
        if (coupon.getTongLuotDung() != null && coupon.getDaDung() >= coupon.getTongLuotDung()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Mã giảm giá đã hết lượt sử dụng");
        }
        if (nguoiDungId != null && coupon.getLuotDungMoiNguoi() != null) {
            long daDung = donHangRepository.countByNguoiDungIdAndMaGiamGiaIdAndTrangThaiNot(
                    nguoiDungId, coupon.getId(), TrangThaiDonHang.da_huy);
            if (daDung >= coupon.getLuotDungMoiNguoi()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Bạn đã hết lượt sử dụng mã giảm giá này");
            }
        }
        return coupon;
    }

    private BigDecimal tinhGiamGia(MaGiamGia coupon, BigDecimal tamTinh) {
        BigDecimal tienGiam = coupon.getLoaiGiam() == LoaiGiam.phan_tram
                ? tamTinh.multiply(coupon.getGiaTriGiam()).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP)
                : coupon.getGiaTriGiam();
        if (coupon.getGiamToiDa() != null) tienGiam = tienGiam.min(coupon.getGiamToiDa());
        return tienGiam.min(tamTinh);
    }

    private BigDecimal layCaiDatSo(String khoa, BigDecimal macDinh) {
        return caiDatRepository.findByKhoa(khoa)
                .filter(item -> StringUtils.hasText(item.getGiaTri()))
                .map(item -> {
                    try {
                        return new BigDecimal(item.getGiaTri());
                    } catch (NumberFormatException ex) {
                        return macDinh;
                    }
                })
                .orElse(macDinh);
    }

    private ChiTietDonHang taoChiTiet(DonHang donHang, ItemDatHang item) {
        BienTheSanPham bienThe = item.bienThe();
        SanPham sanPham = bienThe.getSanPham();
        ChiTietDonHang chiTiet = new ChiTietDonHang();
        chiTiet.setDonHang(donHang);
        chiTiet.setSanPham(sanPham);
        chiTiet.setBienThe(bienThe);
        chiTiet.setTenSanPham(sanPham.getTen());
        chiTiet.setSku(bienThe.getSku());
        chiTiet.setPhanLoai(bienThe.getGiaTris().stream()
                .sorted(Comparator.comparing(gt -> gt.getTuyChon().getThuTu()))
                .map(GiaTriTuyChon::getGiaTri).reduce((a, b) -> a + " / " + b).orElse(""));
        chiTiet.setHinhAnh(sanPham.getHinhAnhs().stream()
                .filter(image -> Boolean.TRUE.equals(image.getLaAnhChinh()))
                .map(HinhAnhSanPham::getDuongDan).findFirst()
                .orElse(sanPham.getHinhAnhs().isEmpty() ? null : sanPham.getHinhAnhs().get(0).getDuongDan()));
        chiTiet.setDonGia(bienThe.layGiaBan());
        chiTiet.setSoLuong(item.soLuong());
        chiTiet.setThanhTien(chiTiet.getDonGia().multiply(BigDecimal.valueOf(item.soLuong())));
        return chiTiet;
    }

    private void truTonKho(DonHang donHang, List<ItemDatHang> items, NguoiDung nguoiDung) {
        Map<Long, ChiTietDonHang> chiTietTheoBienThe = new HashMap<>();
        donHang.getChiTiets().forEach(item -> chiTietTheoBienThe.put(item.getBienThe().getId(), item));
        for (ItemDatHang item : items) {
            BienTheSanPham bienThe = item.bienThe();
            bienThe.setSoLuongTon(bienThe.getSoLuongTon() - item.soLuong());
            LichSuTonKho lichSu = new LichSuTonKho();
            lichSu.setBienThe(bienThe);
            lichSu.setChiTietDonHang(chiTietTheoBienThe.get(bienThe.getId()));
            lichSu.setLoai(LoaiBienDongKho.ban_hang);
            lichSu.setSoLuongThayDoi(-item.soLuong());
            lichSu.setSoLuongSau(bienThe.getSoLuongTon());
            lichSu.setGhiChu("Xuất kho cho đơn " + donHang.getMaDonHang());
            lichSu.setNguoiThucHien(nguoiDung);
            lichSuTonKhoRepository.save(lichSu);
        }
    }

    private String taoMaDonHang() {
        String thoiGian = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "DH" + thoiGian + UUID.randomUUID().toString().substring(0, 4).toUpperCase(Locale.ROOT);
    }

    private String chuanHoaRong(String giaTri) {
        return StringUtils.hasText(giaTri) ? giaTri.trim() : null;
    }

    private record ItemDatHang(BienTheSanPham bienThe, int soLuong) {
    }

    private record KetQuaTinhTien(TinhTienResponse response, MaGiamGia maGiamGia) {
    }
}
