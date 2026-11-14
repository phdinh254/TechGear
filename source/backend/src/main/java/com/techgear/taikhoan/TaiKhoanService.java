package com.techgear.taikhoan;

import com.techgear.chung.baomat.GioiHanSoLan;
import com.techgear.chung.baomat.JwtService;
import com.techgear.chung.loi.ApiException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Locale;
import java.util.Optional;

@Service
public class TaiKhoanService {

    static final int TOI_DA_DANG_NHAP_SAI = 5;
    static final Duration CUA_SO_DANG_NHAP = Duration.ofMinutes(15);

    private final NguoiDungRepository nguoiDungRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GioiHanSoLan gioiHanSoLan;
    /** Băm giả: email không tồn tại vẫn so khớp BCrypt để thời gian phản hồi không lộ email nào đã đăng ký. */
    private final String bamGia;

    public TaiKhoanService(NguoiDungRepository nguoiDungRepository, PasswordEncoder passwordEncoder,
                           JwtService jwtService, GioiHanSoLan gioiHanSoLan) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.gioiHanSoLan = gioiHanSoLan;
        this.bamGia = passwordEncoder.encode("mat-khau-gia-0123456789");
    }

    @Transactional
    public XacThucResponse dangKy(DangKyRequest req) {
        String email = chuanHoaEmail(req.email());
        if (nguoiDungRepository.existsByEmail(email)) {
            throw ApiException.xungDot("Email đã được sử dụng");
        }
        NguoiDung n = new NguoiDung();
        n.setHoTen(req.hoTen().trim());
        n.setEmail(email);
        n.setMatKhau(passwordEncoder.encode(req.matKhau()));
        n.setSoDienThoai(rongThanhNull(req.soDienThoai()));
        return xacThuc(nguoiDungRepository.save(n));
    }

    /** Không mở giao dịch: so khớp BCrypt mất ~0,3 giây, không giữ kết nối CSDL trong lúc đó. */
    public XacThucResponse dangNhap(DangNhapRequest req, String ip) {
        String email = chuanHoaEmail(req.email());
        String khoa = "dang-nhap:" + email + "|" + ip;
        gioiHanSoLan.kiemTra(khoa, TOI_DA_DANG_NHAP_SAI, CUA_SO_DANG_NHAP);

        Optional<NguoiDung> timThay = nguoiDungRepository.findByEmail(email);
        boolean dungMatKhau = passwordEncoder.matches(req.matKhau(), timThay.map(NguoiDung::getMatKhau).orElse(bamGia));
        if (timThay.isEmpty() || !dungMatKhau) {
            gioiHanSoLan.ghiNhan(khoa);
            throw ApiException.chuaXacThuc("Email hoặc mật khẩu không đúng");
        }
        NguoiDung n = timThay.get();
        if (n.getTrangThai() == NguoiDung.TrangThai.bi_khoa) {
            throw ApiException.khongCoQuyen("Tài khoản đã bị khóa");
        }
        gioiHanSoLan.xoa(khoa);
        return xacThuc(n);
    }

    @Transactional(readOnly = true)
    public NguoiDungResponse layThongTin(Long nguoiDungId) {
        return NguoiDungResponse.tu(tim(nguoiDungId));
    }

    private NguoiDung tim(Long nguoiDungId) {
        return nguoiDungRepository.findById(nguoiDungId)
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy tài khoản"));
    }

    private XacThucResponse xacThuc(NguoiDung n) {
        return new XacThucResponse(jwtService.taoToken(n.getId(), n.getVaiTro().name()), NguoiDungResponse.tu(n));
    }

    static String chuanHoaEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    static String rongThanhNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
