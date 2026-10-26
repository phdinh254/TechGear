package com.techgear.service;

import com.techgear.dto.CapNhatHoSoRequest;
import com.techgear.dto.DoiMatKhauRequest;
import com.techgear.dto.NguoiDungResponse;
import com.techgear.dto.ThongBaoResponse;
import com.techgear.entity.NguoiDung;
import com.techgear.exception.ApiException;
import com.techgear.repository.NguoiDungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/** Hồ sơ tài khoản của người dùng đang đăng nhập. */
@Service
@RequiredArgsConstructor
public class TaiKhoanService {

    private final NguoiDungRepository nguoiDungRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public NguoiDungResponse layThongTin(Long nguoiDungId) {
        return NguoiDungResponse.tu(timTheoId(nguoiDungId));
    }

    @Transactional
    public NguoiDungResponse capNhatHoSo(Long nguoiDungId, CapNhatHoSoRequest req) {
        NguoiDung nd = timTheoId(nguoiDungId);
        nd.setHoTen(req.hoTen().trim());
        nd.setSoDienThoai(StringUtils.hasText(req.soDienThoai()) ? req.soDienThoai().trim() : null);
        nd.setGioiTinh(req.gioiTinh());
        nd.setNgaySinh(req.ngaySinh());
        return NguoiDungResponse.tu(nd);
    }

    @Transactional
    public ThongBaoResponse doiMatKhau(Long nguoiDungId, DoiMatKhauRequest req) {
        NguoiDung nd = timTheoId(nguoiDungId);
        if (!passwordEncoder.matches(req.matKhauCu(), nd.getPassword())) {
            throw ApiException.yeuCauKhongHopLe("Mật khẩu hiện tại không đúng");
        }
        if (req.matKhauMoi().equals(req.matKhauCu())) {
            throw ApiException.yeuCauKhongHopLe("Mật khẩu mới phải khác mật khẩu hiện tại");
        }
        nd.setPassword(passwordEncoder.encode(req.matKhauMoi()));
        return new ThongBaoResponse("Đổi mật khẩu thành công");
    }

    @Transactional
    public NguoiDungResponse capNhatAnhDaiDien(Long nguoiDungId, MultipartFile file) {
        NguoiDung nd = timTheoId(nguoiDungId);
        String anhMoi = fileStorageService.luuAnh(file, "anh-dai-dien");
        fileStorageService.donDepSauGiaoDich(nd.getAnhDaiDien(), anhMoi);
        nd.setAnhDaiDien(anhMoi);
        return NguoiDungResponse.tu(nd);
    }

    private NguoiDung timTheoId(Long id) {
        return nguoiDungRepository.findById(id)
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy người dùng"));
    }
}
