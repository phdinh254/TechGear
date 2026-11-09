package com.techgear.service;

import com.techgear.dto.CapNhatHoSoRequest;
import com.techgear.dto.DiaChiRequest;
import com.techgear.dto.DiaChiResponse;
import com.techgear.dto.DoiMatKhauRequest;
import com.techgear.dto.NguoiDungResponse;
import com.techgear.entity.DiaChi;
import com.techgear.entity.NguoiDung;
import com.techgear.exception.ApiException;
import com.techgear.repository.DiaChiRepository;
import com.techgear.repository.NguoiDungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaiKhoanService {

    private final NguoiDungRepository nguoiDungRepository;
    private final DiaChiRepository diaChiRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public NguoiDungResponse capNhatHoSo(Long nguoiDungId, CapNhatHoSoRequest request) {
        NguoiDung nguoiDung = timNguoiDung(nguoiDungId);
        nguoiDung.setHoTen(request.hoTen().trim());
        nguoiDung.setSoDienThoai(chuanHoaRong(request.soDienThoai()));
        nguoiDung.setGioiTinh(request.gioiTinh());
        nguoiDung.setNgaySinh(request.ngaySinh());
        nguoiDung.setAnhDaiDien(chuanHoaRong(request.anhDaiDien()));
        return NguoiDungResponse.tu(nguoiDung);
    }

    @Transactional
    public void doiMatKhau(Long nguoiDungId, DoiMatKhauRequest request) {
        NguoiDung nguoiDung = timNguoiDung(nguoiDungId);
        if (!passwordEncoder.matches(request.matKhauHienTai(), nguoiDung.getPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Mật khẩu hiện tại không đúng");
        }
        if (passwordEncoder.matches(request.matKhauMoi(), nguoiDung.getPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Mật khẩu mới phải khác mật khẩu hiện tại");
        }
        nguoiDung.setPassword(passwordEncoder.encode(request.matKhauMoi()));
    }

    @Transactional(readOnly = true)
    public List<DiaChiResponse> layDiaChi(Long nguoiDungId) {
        return diaChiRepository.findByNguoiDungIdOrderByLaMacDinhDescIdDesc(nguoiDungId).stream()
                .map(DiaChiResponse::tu)
                .toList();
    }

    @Transactional
    public DiaChiResponse taoDiaChi(Long nguoiDungId, DiaChiRequest request) {
        DiaChi diaChi = new DiaChi();
        diaChi.setNguoiDung(timNguoiDung(nguoiDungId));
        ganDiaChi(diaChi, request);
        if (Boolean.TRUE.equals(diaChi.getLaMacDinh())) {
            diaChiRepository.boMacDinhCuaNguoiDung(nguoiDungId);
        }
        return DiaChiResponse.tu(diaChiRepository.save(diaChi));
    }

    @Transactional
    public DiaChiResponse capNhatDiaChi(Long nguoiDungId, Long diaChiId, DiaChiRequest request) {
        DiaChi diaChi = timDiaChi(nguoiDungId, diaChiId);
        ganDiaChi(diaChi, request);
        if (Boolean.TRUE.equals(diaChi.getLaMacDinh())) {
            diaChiRepository.boMacDinhKhac(nguoiDungId, diaChiId);
        }
        return DiaChiResponse.tu(diaChi);
    }

    @Transactional
    public DiaChiResponse datMacDinh(Long nguoiDungId, Long diaChiId) {
        DiaChi diaChi = timDiaChi(nguoiDungId, diaChiId);
        diaChiRepository.boMacDinhKhac(nguoiDungId, diaChiId);
        diaChi.setLaMacDinh(true);
        return DiaChiResponse.tu(diaChi);
    }

    @Transactional
    public void xoaDiaChi(Long nguoiDungId, Long diaChiId) {
        diaChiRepository.delete(timDiaChi(nguoiDungId, diaChiId));
    }

    private NguoiDung timNguoiDung(Long id) {
        return nguoiDungRepository.findById(id)
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy người dùng"));
    }

    private DiaChi timDiaChi(Long nguoiDungId, Long diaChiId) {
        return diaChiRepository.findByIdAndNguoiDungId(diaChiId, nguoiDungId)
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy địa chỉ"));
    }

    private void ganDiaChi(DiaChi diaChi, DiaChiRequest request) {
        diaChi.setTenNguoiNhan(request.tenNguoiNhan().trim());
        diaChi.setSoDienThoai(request.soDienThoai().trim());
        diaChi.setTinhThanh(request.tinhThanh().trim());
        diaChi.setPhuongXa(request.phuongXa().trim());
        diaChi.setDiaChiChiTiet(request.diaChiChiTiet().trim());
        diaChi.setLaMacDinh(Boolean.TRUE.equals(request.laMacDinh()));
    }

    private String chuanHoaRong(String giaTri) {
        return StringUtils.hasText(giaTri) ? giaTri.trim() : null;
    }
}
