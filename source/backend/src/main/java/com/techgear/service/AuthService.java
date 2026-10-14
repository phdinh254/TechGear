package com.techgear.service;

import com.techgear.dto.AuthResponse;
import com.techgear.dto.DangKyRequest;
import com.techgear.dto.DangNhapRequest;
import com.techgear.dto.NguoiDungResponse;
import com.techgear.entity.NguoiDung;
import com.techgear.entity.enums.TrangThaiTaiKhoan;
import com.techgear.entity.enums.VaiTro;
import com.techgear.exception.ApiException;
import com.techgear.repository.NguoiDungRepository;
import com.techgear.security.JwtService;
import com.techgear.security.NguoiDungPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final NguoiDungRepository nguoiDungRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse dangKy(DangKyRequest req) {
        String email = req.email().trim().toLowerCase(Locale.ROOT);
        if (nguoiDungRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email đã được sử dụng");
        }
        NguoiDung nd = new NguoiDung();
        nd.setHoTen(req.hoTen().trim());
        nd.setEmail(email);
        nd.setPassword(passwordEncoder.encode(req.matKhau()));
        nd.setSoDienThoai(StringUtils.hasText(req.soDienThoai()) ? req.soDienThoai().trim() : null);
        nd.setVaiTro(VaiTro.khach_hang);
        nd.setTrangThai(TrangThaiTaiKhoan.hoat_dong);
        nguoiDungRepository.save(nd);
        return taoPhanHoi(nd);
    }

    @Transactional(readOnly = true)
    public AuthResponse dangNhap(DangNhapRequest req) {
        String email = req.email().trim().toLowerCase(Locale.ROOT);
        NguoiDung nd = nguoiDungRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(req.matKhau(), u.getPassword()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng"));
        if (nd.getTrangThai() == TrangThaiTaiKhoan.bi_khoa) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Tài khoản đã bị khóa");
        }
        return taoPhanHoi(nd);
    }

    @Transactional(readOnly = true)
    public NguoiDungResponse layThongTin(Long nguoiDungId) {
        return nguoiDungRepository.findById(nguoiDungId)
                .map(NguoiDungResponse::tu)
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy người dùng"));
    }

    private AuthResponse taoPhanHoi(NguoiDung nd) {
        String token = jwtService.taoToken(new NguoiDungPrincipal(nd));
        return new AuthResponse(token, jwtService.getThoiHanMs(), NguoiDungResponse.tu(nd));
    }
}
