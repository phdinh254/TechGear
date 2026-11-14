package com.techgear.service;

import com.techgear.dto.DanhGiaResponse;
import com.techgear.dto.PageResponse;
import com.techgear.entity.SanPham;
import com.techgear.exception.ApiException;
import com.techgear.repository.DanhGiaRepository;
import com.techgear.repository.SanPhamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DanhGiaService {

    private static final int KICH_THUOC_TOI_DA = 50;

    private final DanhGiaRepository danhGiaRepository;
    private final SanPhamRepository sanPhamRepository;

    /** Đánh giá đã duyệt của sản phẩm đang bán, mới nhất trước. */
    @Transactional(readOnly = true)
    public PageResponse<DanhGiaResponse> layTheoSanPham(String slug, int trang, int kichThuoc) {
        SanPham sp = sanPhamRepository.findBySlugAndHienThiTrue(slug)
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy sản phẩm"));
        PageRequest pageable = PageRequest.of(Math.max(trang, 0),
                Math.min(Math.max(kichThuoc, 1), KICH_THUOC_TOI_DA),
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")));
        return PageResponse.tu(danhGiaRepository.findBySanPhamIdAndDaDuyetTrue(sp.getId(), pageable)
                .map(DanhGiaResponse::tu));
    }
}
