package com.techgear.service;

import com.techgear.dto.DiaChiRequest;
import com.techgear.dto.DiaChiResponse;
import com.techgear.entity.DiaChi;
import com.techgear.exception.ApiException;
import com.techgear.repository.DiaChiRepository;
import com.techgear.repository.NguoiDungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Sổ địa chỉ. Quy tắc: luôn có đúng một địa chỉ mặc định (nếu sổ không rỗng);
 * địa chỉ đầu tiên tự thành mặc định; xóa địa chỉ mặc định thì địa chỉ mới nhất còn lại thay thế.
 */
@Service
@RequiredArgsConstructor
public class DiaChiService {

    static final int TOI_DA = 10;

    private final DiaChiRepository diaChiRepository;
    private final NguoiDungRepository nguoiDungRepository;

    @Transactional(readOnly = true)
    public List<DiaChiResponse> layDanhSach(Long nguoiDungId) {
        return diaChiRepository.findByNguoiDungIdOrderByLaMacDinhDescIdDesc(nguoiDungId).stream()
                .map(DiaChiResponse::tu).toList();
    }

    @Transactional
    public DiaChiResponse tao(Long nguoiDungId, DiaChiRequest req) {
        long soLuong = diaChiRepository.countByNguoiDungId(nguoiDungId);
        if (soLuong >= TOI_DA) {
            throw ApiException.yeuCauKhongHopLe("Sổ địa chỉ tối đa " + TOI_DA + " địa chỉ");
        }
        DiaChi dc = new DiaChi();
        dc.setNguoiDung(nguoiDungRepository.getReferenceById(nguoiDungId));
        ganDuLieu(dc, req);
        boolean macDinh = soLuong == 0 || Boolean.TRUE.equals(req.laMacDinh());
        if (macDinh) {
            boMacDinhCacDiaChiKhac(nguoiDungId, null);
        }
        dc.setLaMacDinh(macDinh);
        return DiaChiResponse.tu(diaChiRepository.save(dc));
    }

    @Transactional
    public DiaChiResponse capNhat(Long nguoiDungId, Long id, DiaChiRequest req) {
        DiaChi dc = timCuaNguoiDung(nguoiDungId, id);
        ganDuLieu(dc, req);
        // Chỉ cho phép BẬT mặc định; muốn đổi mặc định thì chọn địa chỉ khác làm mặc định
        if (Boolean.TRUE.equals(req.laMacDinh()) && !Boolean.TRUE.equals(dc.getLaMacDinh())) {
            boMacDinhCacDiaChiKhac(nguoiDungId, dc.getId());
            dc.setLaMacDinh(true);
        }
        return DiaChiResponse.tu(dc);
    }

    @Transactional
    public DiaChiResponse datMacDinh(Long nguoiDungId, Long id) {
        DiaChi dc = timCuaNguoiDung(nguoiDungId, id);
        boMacDinhCacDiaChiKhac(nguoiDungId, dc.getId());
        dc.setLaMacDinh(true);
        return DiaChiResponse.tu(dc);
    }

    @Transactional
    public void xoa(Long nguoiDungId, Long id) {
        DiaChi dc = timCuaNguoiDung(nguoiDungId, id);
        boolean laMacDinh = Boolean.TRUE.equals(dc.getLaMacDinh());
        diaChiRepository.delete(dc);
        if (laMacDinh) {
            diaChiRepository.findByNguoiDungIdOrderByLaMacDinhDescIdDesc(nguoiDungId).stream()
                    .filter(d -> !d.getId().equals(id))
                    .findFirst()
                    .ifPresent(d -> d.setLaMacDinh(true));
        }
    }

    private void boMacDinhCacDiaChiKhac(Long nguoiDungId, Long giuLaiId) {
        diaChiRepository.findByNguoiDungIdOrderByLaMacDinhDescIdDesc(nguoiDungId).stream()
                .filter(d -> Boolean.TRUE.equals(d.getLaMacDinh()) && !d.getId().equals(giuLaiId))
                .forEach(d -> d.setLaMacDinh(false));
    }

    /** Địa chỉ của người khác trả 404 (không tiết lộ là địa chỉ đó có tồn tại). */
    private DiaChi timCuaNguoiDung(Long nguoiDungId, Long id) {
        return diaChiRepository.findByIdAndNguoiDungId(id, nguoiDungId)
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy địa chỉ"));
    }

    private void ganDuLieu(DiaChi dc, DiaChiRequest req) {
        dc.setTenNguoiNhan(req.tenNguoiNhan().trim());
        dc.setSoDienThoai(req.soDienThoai().trim());
        dc.setTinhThanh(req.tinhThanh().trim());
        dc.setPhuongXa(req.phuongXa().trim());
        dc.setDiaChiChiTiet(req.diaChiChiTiet().trim());
    }
}
