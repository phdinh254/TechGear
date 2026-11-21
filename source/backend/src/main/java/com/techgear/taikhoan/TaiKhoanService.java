package com.techgear.taikhoan;

import com.techgear.chung.baomat.GioiHanSoLan;
import com.techgear.chung.baomat.JwtService;
import com.techgear.chung.loi.ApiException;
import com.techgear.chung.upload.LuuAnhService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
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
    private final LuuAnhService luuAnhService;
    private final Clock clock;
    /** Băm giả: email không tồn tại vẫn so khớp BCrypt để thời gian phản hồi không lộ email nào đã đăng ký. */
    private final String bamGia;

    public TaiKhoanService(NguoiDungRepository nguoiDungRepository, PasswordEncoder passwordEncoder,
                           JwtService jwtService, GioiHanSoLan gioiHanSoLan, LuuAnhService luuAnhService,
                           Clock clock) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.gioiHanSoLan = gioiHanSoLan;
        this.luuAnhService = luuAnhService;
        this.clock = clock;
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
        java.time.Instant lan = gioiHanSoLan.giuCho(khoa, TOI_DA_DANG_NHAP_SAI, CUA_SO_DANG_NHAP);

        Optional<NguoiDung> timThay = nguoiDungRepository.findByEmail(email);
        boolean dungMatKhau = passwordEncoder.matches(req.matKhau(), timThay.map(NguoiDung::getMatKhau).orElse(bamGia));
        if (timThay.isEmpty() || !dungMatKhau) {
            throw ApiException.chuaXacThuc("Email hoặc mật khẩu không đúng");
        }
        NguoiDung n = timThay.get();
        if (n.getTrangThai() == NguoiDung.TrangThai.bi_khoa) {
            gioiHanSoLan.traLai(khoa, lan); // chỉ đếm lần sai mật khẩu
            throw ApiException.khongCoQuyen("Tài khoản đã bị khóa");
        }
        gioiHanSoLan.xoa(khoa);
        return xacThuc(n);
    }

    @Transactional(readOnly = true)
    public NguoiDungResponse layThongTin(Long nguoiDungId) {
        return NguoiDungResponse.tu(tim(nguoiDungId));
    }

    @Transactional
    public NguoiDungResponse capNhatHoSo(Long nguoiDungId, CapNhatHoSoRequest req) {
        NguoiDung n = tim(nguoiDungId);
        n.setHoTen(req.hoTen().trim());
        n.setSoDienThoai(rongThanhNull(req.soDienThoai()));
        n.setGioiTinh(req.gioiTinh());
        n.setNgaySinh(req.ngaySinh());
        return NguoiDungResponse.tu(n);
    }

    /** Đổi mật khẩu: mọi token phát hành trước đó hết hiệu lực, trả token mới. */
    @Transactional
    public XacThucResponse doiMatKhau(Long nguoiDungId, DoiMatKhauRequest req) {
        NguoiDung n = tim(nguoiDungId);
        if (!passwordEncoder.matches(req.matKhauHienTai(), n.getMatKhau())) {
            throw ApiException.yeuCauKhongHopLe("Mật khẩu hiện tại không đúng");
        }
        datMatKhau(n, req.matKhauMoi());
        return xacThuc(n);
    }

    /** Ảnh cũ chỉ xóa sau khi ảnh mới đã lưu và CSDL đã ghi nhận. */
    @Transactional
    public NguoiDungResponse doiAnhDaiDien(Long nguoiDungId, MultipartFile file) {
        NguoiDung n = tim(nguoiDungId);
        String anhCu = n.getAnhDaiDien();
        n.setAnhDaiDien(luuAnhService.luu(file, "anh-dai-dien"));
        nguoiDungRepository.flush();
        luuAnhService.xoa(anhCu);
        return NguoiDungResponse.tu(n);
    }

    /** Gán mật khẩu mới và mốc thu hồi token – ghi mốc trước khi phát hành token mới. */
    void datMatKhau(NguoiDung n, String matKhauMoi) {
        n.setMatKhau(passwordEncoder.encode(matKhauMoi));
        n.setDoiMatKhauLuc(OffsetDateTime.now(clock));
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
