package com.techgear.service;

import com.techgear.dto.DanhMucResponse;
import com.techgear.entity.DanhMuc;
import com.techgear.exception.ApiException;
import com.techgear.repository.DanhMucRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DanhMucService {

    private final DanhMucRepository danhMucRepository;

    /** Trả về cây danh mục (cha chứa danh sách con) gồm các danh mục đang hiển thị. */
    @Transactional(readOnly = true)
    public List<DanhMucResponse> layCayDanhMuc() {
        List<DanhMuc> tatCa = danhMucRepository.findByHienThiTrueOrderByThuTuAsc();
        Map<Long, DanhMucResponse> theoId = new LinkedHashMap<>();
        for (DanhMuc dm : tatCa) {
            theoId.put(dm.getId(), new DanhMucResponse(dm.getId(), dm.getTen(), dm.getSlug(), dm.getHinhAnh()));
        }
        List<DanhMucResponse> goc = new ArrayList<>();
        for (DanhMuc dm : tatCa) {
            DanhMucResponse node = theoId.get(dm.getId());
            DanhMucResponse cha = dm.getDanhMucCha() == null ? null : theoId.get(dm.getDanhMucCha().getId());
            if (cha != null) {
                cha.danhMucCons().add(node);
            } else if (dm.getDanhMucCha() == null) {
                goc.add(node);
            }
            // Danh mục có cha đang bị ẩn thì cũng không hiển thị.
        }
        return goc;
    }

    /** Lấy id của danh mục theo slug và tất cả danh mục con cháu (để lọc sản phẩm). */
    @Transactional(readOnly = true)
    public List<Long> layIdKemDanhMucCon(String slug) {
        DanhMuc dm = danhMucRepository.findBySlug(slug)
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy danh mục: " + slug));
        List<Long> ketQua = new ArrayList<>();
        gomId(dm, ketQua);
        return ketQua;
    }

    private void gomId(DanhMuc dm, List<Long> ketQua) {
        ketQua.add(dm.getId());
        for (DanhMuc con : dm.getDanhMucCons()) {
            gomId(con, ketQua);
        }
    }
}
