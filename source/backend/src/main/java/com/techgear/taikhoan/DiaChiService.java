package com.techgear.taikhoan;

import com.techgear.chung.loi.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Sổ địa chỉ: tối đa 10 địa chỉ, luôn đúng một địa chỉ mặc định khi sổ không rỗng.
 * Mọi thao tác ghi khóa dòng người dùng trước để hai yêu cầu đồng thời không phá các quy tắc này.
 */
@Service
@RequiredArgsConstructor
public class DiaChiService {

    static final int TOI_DA_DIA_CHI = 10;

    private final DiaChiRepository diaChiRepository;
    private final NguoiDungRepository nguoiDungRepository;

    @Transactional(readOnly = true)
    public List<DiaChiResponse> danhSach(Long nguoiDungId) {
        return diaChiRepository.findByNguoiDungIdOrderByLaMacDinhDescIdDesc(nguoiDungId).stream()
                .map(DiaChiResponse::tu).toList();
    }

    @Transactional
    public DiaChiResponse tao(Long nguoiDungId, DiaChiRequest req) {
        nguoiDungRepository.khoaTheoId(nguoiDungId);
        long soLuong = diaChiRepository.countByNguoiDungId(nguoiDungId);
        if (soLuong >= TOI_DA_DIA_CHI) {
            throw ApiException.yeuCauKhongHopLe("Chỉ lưu tối đa " + TOI_DA_DIA_CHI + " địa chỉ");
        }
        boolean macDinh = soLuong == 0 || Boolean.TRUE.equals(req.laMacDinh());
        if (macDinh) {
            boMacDinhHienTai(nguoiDungId);
        }
        DiaChi d = new DiaChi();
        d.setNguoiDungId(nguoiDungId);
        ganDuLieu(d, req);
        d.setLaMacDinh(macDinh);
        return DiaChiResponse.tu(diaChiRepository.save(d));
    }

    @Transactional
    public DiaChiResponse sua(Long nguoiDungId, Long id, DiaChiRequest req) {
        DiaChi d = khoaVaTim(nguoiDungId, id);
        ganDuLieu(d, req);
        if (Boolean.TRUE.equals(req.laMacDinh())) {
            lamMacDinh(d);
        }
        return DiaChiResponse.tu(d);
    }

    @Transactional
    public DiaChiResponse datMacDinh(Long nguoiDungId, Long id) {
        DiaChi d = khoaVaTim(nguoiDungId, id);
        lamMacDinh(d);
        return DiaChiResponse.tu(d);
    }

    /** Xóa địa chỉ mặc định thì địa chỉ mới nhất còn lại thành mặc định. */
    @Transactional
    public void xoa(Long nguoiDungId, Long id) {
        DiaChi d = khoaVaTim(nguoiDungId, id);
        diaChiRepository.delete(d);
        diaChiRepository.flush(); // Hibernate xóa sau cùng khi flush – xóa trước rồi mới đặt mặc định mới
        if (d.isLaMacDinh()) {
            diaChiRepository.findFirstByNguoiDungIdOrderByIdDesc(nguoiDungId).ifPresent(m -> m.setLaMacDinh(true));
        }
    }

    /** Địa chỉ của người khác trả 404 như không tồn tại. */
    private DiaChi khoaVaTim(Long nguoiDungId, Long id) {
        nguoiDungRepository.khoaTheoId(nguoiDungId);
        return diaChiRepository.findByIdAndNguoiDungId(id, nguoiDungId)
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy địa chỉ"));
    }

    private void lamMacDinh(DiaChi d) {
        if (!d.isLaMacDinh()) {
            boMacDinhHienTai(d.getNguoiDungId());
            d.setLaMacDinh(true);
        }
    }

    /** Ghi xuống CSDL ngay: chỉ mục duy nhất một phần chỉ cho một địa chỉ mặc định tại mọi thời điểm. */
    private void boMacDinhHienTai(Long nguoiDungId) {
        diaChiRepository.findByNguoiDungIdAndLaMacDinhTrue(nguoiDungId).ifPresent(c -> {
            c.setLaMacDinh(false);
            diaChiRepository.flush();
        });
    }

    private static void ganDuLieu(DiaChi d, DiaChiRequest req) {
        d.setTenNguoiNhan(req.tenNguoiNhan().trim());
        d.setSoDienThoai(req.soDienThoai());
        d.setTinhThanh(req.tinhThanh().trim());
        d.setPhuongXa(req.phuongXa().trim());
        d.setDiaChiChiTiet(req.diaChiChiTiet().trim());
    }
}
