package com.techgear.donhang;

import com.techgear.chung.PageResponse;
import com.techgear.chung.baomat.GioiHanSoLan;
import com.techgear.chung.loi.ApiException;
import com.techgear.cuahang.CaiDatService;
import com.techgear.giohang.DongGioRequest;
import com.techgear.khuyenmai.KhuyenMaiService;
import com.techgear.taikhoan.NguoiDung;
import com.techgear.taikhoan.NguoiDungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Đặt hàng: mọi số tiền tính ở server từ giá hiện tại; khóa biến thể (theo id) rồi mã giảm giá, trừ kho, ghi lịch sử
 * trong một giao dịch. Hủy đơn: khóa đơn, hoàn kho (theo id biến thể), trả lượt mã.
 */
@Service
@RequiredArgsConstructor
public class DonHangService {

    static final String KY_TU_MA = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final DateTimeFormatter NGAY_MA = DateTimeFormatter.ofPattern("yyMMdd");
    private static final SecureRandom NGAU_NHIEN = new SecureRandom();
    private static final int GIOI_HAN_IP = 10;
    private static final Duration CUA_SO_IP = Duration.ofMinutes(15);
    static final Duration HUY_VNPAY_SAU = Duration.ofMinutes(35);

    private final DonHangRepository donHangRepository;
    private final KhuyenMaiService khuyenMaiService;
    private final CaiDatService caiDatService;
    private final NguoiDungRepository nguoiDungRepository;
    private final GioiHanSoLan gioiHanSoLan;
    private final Clock clock;
    private final TransactionTemplate giaoDich;

    private record BangGia(List<DonHangResponse.Dong> dongs, KhuyenMaiService.KetQua giam, long tamTinh,
                           long tienGiamGia, long phiVanChuyen, long tongTien) {
    }

    @Transactional(readOnly = true)
    public DonHangResponse.TinhThu tinhThu(YeuCauDonHang.TinhThu req) {
        BangGia g = tinh(req.dongs(), req.maGiamGia(), null, chuanHoaEmail(req.email()), false);
        return new DonHangResponse.TinhThu(g.dongs(), g.giam() == null ? null : g.giam().ma(), g.tamTinh(),
                g.tienGiamGia(), g.phiVanChuyen(), g.tongTien());
    }

    /** Khách hàng đã đăng nhập: email lấy từ tài khoản; các biến thể vừa đặt bị xóa khỏi giỏ server. */
    @Transactional
    public DonHangResponse.ChiTiet datHang(Long nguoiDungId, DatHangRequest req) {
        String email = nguoiDungRepository.findById(nguoiDungId).map(NguoiDung::getEmail)
                .orElseThrow(() -> ApiException.chuaXacThuc("Phiên đăng nhập không hợp lệ hoặc đã hết hạn"));
        return dat(nguoiDungId, email, req);
    }

    /** Khách vãng lai: bắt buộc email; mỗi IP tối đa 10 đơn / 15 phút (đơn giữ hàng trong kho). */
    @Transactional
    public DonHangResponse.ChiTiet datHangVangLai(DatHangRequest req, String ip) {
        String khoa = "dat-hang-vang-lai:" + ip;
        String email = chuanHoaEmail(req.email());
        if (email == null) {
            throw ApiException.yeuCauKhongHopLe("Vui lòng nhập email để nhận thông tin đơn hàng");
        }
        java.time.Instant lan = gioiHanSoLan.giuCho(khoa, GIOI_HAN_IP, CUA_SO_IP);
        try {
            return dat(null, email, req);
        } catch (RuntimeException e) {
            gioiHanSoLan.traLai(khoa, lan); // chỉ đếm đơn đặt được
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<DonHangResponse.TomTat> cuaToi(Long nguoiDungId, TrangThaiDonHang trangThai, int trang,
                                                       int kichThuoc) {
        DonHangRepository.TrangKetQua kq = donHangRepository.cuaNguoiDung(nguoiDungId, trangThai, trang, kichThuoc);
        int tongSoTrang = (int) ((kq.tongSoPhanTu() + kichThuoc - 1) / kichThuoc);
        return new PageResponse<>(kq.noiDung(), trang, kichThuoc, kq.tongSoPhanTu(), tongSoTrang);
    }

    @Transactional(readOnly = true)
    public DonHangResponse.ChiTiet chiTiet(Long nguoiDungId, String maDonHang) {
        Long id = donHangRepository.idCuaNguoiDung(maDonHang, nguoiDungId).orElseThrow(DonHangService::khongTimThay);
        return donHangRepository.chiTiet(id, true);
    }

    /** Khách hủy đơn của mình khi còn chờ xác nhận. */
    @Transactional
    public DonHangResponse.ChiTiet huyCuaKhach(Long nguoiDungId, String maDonHang, String lyDo) {
        DonHangRepository.DonKhoa don = donHangRepository.khoaDonCuaNguoiDung(maDonHang, nguoiDungId)
                .orElseThrow(DonHangService::khongTimThay);
        if (!TrangThaiDonHang.cho_xac_nhan.name().equals(don.trangThai())) {
            throw ApiException.xungDot("Chỉ hủy được đơn đang chờ xác nhận");
        }
        huy(don, nguoiDungId, lyDo == null || lyDo.isBlank() ? "Khách hủy đơn" : lyDo.trim());
        return donHangRepository.chiTiet(don.id(), true);
    }

    /** Tra cứu đơn bằng mã + email đặt hàng; mỗi IP sai tối đa 10 lần / 15 phút. */
    @Transactional(readOnly = true)
    public DonHangResponse.ChiTiet traCuu(YeuCauDonHang.TraCuu req, String ip) {
        String khoa = "tra-cuu-don:" + ip;
        java.time.Instant lan = gioiHanSoLan.giuCho(khoa, GIOI_HAN_IP, CUA_SO_IP);
        Long id = donHangRepository.idTheoEmail(req.maDonHang().trim(), req.email().trim()).orElse(null);
        if (id == null) {
            throw khongTimThay();
        }
        gioiHanSoLan.traLai(khoa, lan); // chỉ đếm lần tra sai
        return donHangRepository.chiTiet(id, false);
    }

    /**
     * Hủy các đơn VNPay chưa thanh toán sau 35 phút (liên kết hết hạn ở phút 30, thêm 5 phút cho giao dịch đang dở).
     * Mỗi đơn một giao dịch riêng để thứ tự khóa (biến thể → mã giảm giá) giống lúc đặt hàng, không khóa chéo.
     */
    public int huyDonVnPayQuaHan() {
        java.time.OffsetDateTime moc = java.time.OffsetDateTime.now(clock).minus(HUY_VNPAY_SAU);
        int soDon = 0;
        for (Long id : donHangRepository.donVnPayQuaHan(moc)) {
            Boolean daHuy = giaoDich.execute(t -> donHangRepository.khoaDonVnPayQuaHan(id, moc).map(don -> {
                huy(don, null, "Quá 30 phút chưa thanh toán VNPay, hệ thống tự hủy");
                return true;
            }).orElse(false));
            if (Boolean.TRUE.equals(daHuy)) {
                soDon++;
            }
        }
        return soDon;
    }

    /** Hủy đơn đã khóa: hoàn kho, trả lượt mã giảm giá, ghi lịch sử (dùng chung cho khách, quản trị, hết hạn VNPay). */
    void huy(DonHangRepository.DonKhoa don, Long nguoiThucHienId, String ghiChu) {
        donHangRepository.doiTrangThai(don.id(), TrangThaiDonHang.da_huy);
        for (DonHangRepository.DongHoan d : donHangRepository.dongHoanKho(don.id())) {
            int sau = donHangRepository.doiTonKho(d.bienTheId(), d.soLuong());
            donHangRepository.ghiLichSuKho(d.bienTheId(), don.id(), "huy_don", d.soLuong(), sau, nguoiThucHienId);
        }
        if (don.maGiamGiaId() != null) {
            khuyenMaiService.traLuot(don.maGiamGiaId());
        }
        donHangRepository.ghiLichSuDon(don.id(), TrangThaiDonHang.da_huy, ghiChu, nguoiThucHienId);
    }

    private DonHangResponse.ChiTiet dat(Long nguoiDungId, String email, DatHangRequest req) {
        BangGia g = tinh(req.dongs(), req.maGiamGia(), nguoiDungId, email, true);

        Map<String, Object> cot = new HashMap<>();
        cot.put("ma", maDonHangMoi());
        cot.put("nd", nguoiDungId);
        cot.put("maGiamGiaId", g.giam() == null ? null : g.giam().id());
        cot.put("tenNguoiNhan", req.tenNguoiNhan().trim());
        cot.put("email", email);
        cot.put("soDienThoai", req.soDienThoai());
        cot.put("diaChi", req.diaChiChiTiet().trim() + ", " + req.phuongXa().trim() + ", " + req.tinhThanh().trim());
        cot.put("ghiChu", req.ghiChu() == null || req.ghiChu().isBlank() ? null : req.ghiChu().trim());
        cot.put("tamTinh", g.tamTinh());
        cot.put("tienGiamGia", g.tienGiamGia());
        cot.put("phiVanChuyen", g.phiVanChuyen());
        cot.put("tongTien", g.tongTien());
        cot.put("phuongThuc", req.phuongThucThanhToan().name());
        Long donHangId = donHangRepository.themDonHang(cot);

        for (DonHangResponse.Dong d : g.dongs()) {
            donHangRepository.themDong(donHangId, d);
            int sau = donHangRepository.doiTonKho(d.bienTheId(), -d.soLuong());
            donHangRepository.ghiLichSuKho(d.bienTheId(), donHangId, "ban_hang", -d.soLuong(), sau, nguoiDungId);
        }
        donHangRepository.ghiLichSuDon(donHangId, TrangThaiDonHang.cho_xac_nhan, "Đặt hàng", nguoiDungId);
        if (nguoiDungId != null) {
            donHangRepository.xoaKhoiGio(nguoiDungId, g.dongs().stream().map(DonHangResponse.Dong::bienTheId).toList());
        }
        return donHangRepository.chiTiet(donHangId, nguoiDungId != null);
    }

    /** {@code datHang} = true: khóa biến thể và mã giảm giá, ghi nhận lượt dùng mã. */
    private BangGia tinh(List<DongGioRequest> yeuCau, String maGiamGia, Long nguoiDungId, String email,
                         boolean datHang) {
        Map<Long, Integer> gom = new LinkedHashMap<>();
        yeuCau.forEach(d -> gom.merge(d.bienTheId(), d.soLuong(), Integer::sum));
        Map<Long, DonHangRepository.DongKho> kho = donHangRepository.dongKho(gom.keySet(), datHang);

        List<DonHangResponse.Dong> dongs = new ArrayList<>();
        long tamTinh = 0;
        for (Map.Entry<Long, Integer> e : gom.entrySet()) {
            DonHangRepository.DongKho k = kho.get(e.getKey());
            if (k == null) {
                throw ApiException.khongTimThay("Phiên bản sản phẩm không còn bán");
            }
            int soLuong = e.getValue();
            if (k.soLuongTon() < soLuong) {
                String ten = k.tenSanPham() + (k.phanLoai() == null ? "" : " (" + k.phanLoai() + ")");
                throw ApiException.xungDot("Sản phẩm " + ten
                        + (k.soLuongTon() == 0 ? " đã hết hàng" : " chỉ còn " + k.soLuongTon() + " trong kho"));
            }
            long thanhTien = k.donGia() * soLuong;
            dongs.add(new DonHangResponse.Dong(k.bienTheId(), k.sanPhamId(), k.sanPhamSlug(), k.tenSanPham(), k.sku(),
                    k.phanLoai(), k.hinhAnh(), k.donGia(), soLuong, thanhTien));
            tamTinh += thanhTien;
        }

        KhuyenMaiService.KetQua giam = maGiamGia == null || maGiamGia.isBlank() ? null
                : khuyenMaiService.apDung(maGiamGia.trim(), tamTinh, nguoiDungId, email, datHang);
        long tienGiam = giam == null ? 0 : giam.tienGiam();
        long phi = tamTinh - tienGiam >= caiDatService.soTien(CaiDatService.MIEN_PHI_VAN_CHUYEN_TU) ? 0
                : caiDatService.soTien(CaiDatService.PHI_VAN_CHUYEN);
        return new BangGia(dongs, giam, tamTinh, tienGiam, phi, tamTinh - tienGiam + phi);
    }

    /** shortcut: kiểm trùng rồi mới chèn – trùng đồng thời (xác suất ~1/10^9 mỗi ngày) làm đơn lỗi 409, khách đặt lại. */
    private String maDonHangMoi() {
        String ma;
        do {
            ma = taoMaDonHang(LocalDate.now(clock), NGAU_NHIEN);
        } while (donHangRepository.maDaTonTai(ma));
        return ma;
    }

    static String taoMaDonHang(LocalDate ngay, Random ngauNhien) {
        StringBuilder ma = new StringBuilder("DH").append(ngay.format(NGAY_MA));
        for (int i = 0; i < 6; i++) {
            ma.append(KY_TU_MA.charAt(ngauNhien.nextInt(KY_TU_MA.length())));
        }
        return ma.toString();
    }

    private static String chuanHoaEmail(String email) {
        return email == null || email.isBlank() ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private static ApiException khongTimThay() {
        return ApiException.khongTimThay("Không tìm thấy đơn hàng");
    }
}
