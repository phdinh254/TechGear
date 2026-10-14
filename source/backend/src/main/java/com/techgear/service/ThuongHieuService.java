package com.techgear.service;

import com.techgear.dto.ThuongHieuRequest;
import com.techgear.dto.ThuongHieuResponse;
import com.techgear.entity.ThuongHieu;
import com.techgear.exception.ApiException;
import com.techgear.repository.ThuongHieuRepository;
import com.techgear.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ThuongHieuService {

    private final ThuongHieuRepository thuongHieuRepository;

    @Transactional(readOnly = true)
    public List<ThuongHieuResponse> layDangHienThi() {
        return thuongHieuRepository.findByHienThiTrueOrderByTenAsc().stream()
                .map(ThuongHieuResponse::tu).toList();
    }

    @Transactional(readOnly = true)
    public List<ThuongHieuResponse> layTatCa() {
        return thuongHieuRepository.findAll(Sort.by("ten")).stream()
                .map(ThuongHieuResponse::tu).toList();
    }

    @Transactional
    public ThuongHieuResponse tao(ThuongHieuRequest req) {
        String slug = SlugUtil.taoSlug(req.ten());
        if (thuongHieuRepository.existsBySlug(slug)) {
            throw new ApiException(HttpStatus.CONFLICT, "Thương hiệu đã tồn tại");
        }
        ThuongHieu th = new ThuongHieu();
        ganDuLieu(th, req, slug);
        return ThuongHieuResponse.tu(thuongHieuRepository.save(th));
    }

    @Transactional
    public ThuongHieuResponse capNhat(Long id, ThuongHieuRequest req) {
        ThuongHieu th = timTheoId(id);
        String slug = SlugUtil.taoSlug(req.ten());
        if (thuongHieuRepository.existsBySlugAndIdNot(slug, id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Tên thương hiệu trùng với thương hiệu khác");
        }
        ganDuLieu(th, req, slug);
        return ThuongHieuResponse.tu(th);
    }

    /** Xóa mềm (cập nhật deleted_at). */
    @Transactional
    public void xoa(Long id) {
        thuongHieuRepository.delete(timTheoId(id));
    }

    private ThuongHieu timTheoId(Long id) {
        return thuongHieuRepository.findById(id)
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy thương hiệu"));
    }

    private void ganDuLieu(ThuongHieu th, ThuongHieuRequest req, String slug) {
        th.setTen(req.ten().trim());
        th.setSlug(slug);
        th.setLogo(req.logo());
        th.setMoTa(req.moTa());
        th.setHienThi(req.hienThi() == null || req.hienThi());
    }
}
