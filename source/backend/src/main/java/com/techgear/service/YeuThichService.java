package com.techgear.service;

import com.techgear.dto.SanPhamTomTatResponse;
import com.techgear.entity.NguoiDung;
import com.techgear.entity.SanPham;
import com.techgear.entity.YeuThich;
import com.techgear.entity.YeuThichId;
import com.techgear.exception.ApiException;
import com.techgear.repository.NguoiDungRepository;
import com.techgear.repository.SanPhamRepository;
import com.techgear.repository.YeuThichRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class YeuThichService {

    private final YeuThichRepository yeuThichRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final SanPhamRepository sanPhamRepository;
    private final SanPhamService sanPhamService;

    @Transactional(readOnly = true)
    public List<SanPhamTomTatResponse> layDanhSach(Long nguoiDungId) {
        return yeuThichRepository.findByNguoiDungIdOrderByCreatedAtDesc(nguoiDungId).stream()
                .map(YeuThich::getSanPham)
                .filter(SanPham::dangBan)
                .map(sanPhamService::taoTomTat)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean kiemTra(Long nguoiDungId, Long sanPhamId) {
        return yeuThichRepository.existsByNguoiDungIdAndSanPhamId(nguoiDungId, sanPhamId);
    }

    @Transactional
    public void them(Long nguoiDungId, Long sanPhamId) {
        YeuThichId id = new YeuThichId(nguoiDungId, sanPhamId);
        if (yeuThichRepository.existsById(id)) return;
        NguoiDung nguoiDung = nguoiDungRepository.findById(nguoiDungId)
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy người dùng"));
        SanPham sanPham = sanPhamRepository.findById(sanPhamId)
                .filter(SanPham::dangBan)
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy sản phẩm"));
        YeuThich yeuThich = new YeuThich();
        yeuThich.setId(id);
        yeuThich.setNguoiDung(nguoiDung);
        yeuThich.setSanPham(sanPham);
        yeuThichRepository.save(yeuThich);
    }

    @Transactional
    public void xoa(Long nguoiDungId, Long sanPhamId) {
        yeuThichRepository.deleteById(new YeuThichId(nguoiDungId, sanPhamId));
    }
}
