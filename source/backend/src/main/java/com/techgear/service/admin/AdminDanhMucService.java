package com.techgear.service.admin;

import com.techgear.dto.admin.AdminDanhMucRequest;
import com.techgear.dto.admin.AdminDanhMucResponse;
import com.techgear.entity.DanhMuc;
import com.techgear.exception.ApiException;
import com.techgear.repository.DanhMucRepository;
import com.techgear.repository.SanPhamRepository;
import com.techgear.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminDanhMucService {

    private final DanhMucRepository danhMucRepository;
    private final SanPhamRepository sanPhamRepository;

    /** Cây 2 cấp gồm cả danh mục ẩn, sắp theo thứ tự. */
    @Transactional(readOnly = true)
    public List<AdminDanhMucResponse> layCay() {
        Map<Long, Long> soSanPham = new HashMap<>();
        for (Object[] dong : sanPhamRepository.demTheoDanhMuc()) {
            soSanPham.put((Long) dong[0], (Long) dong[1]);
        }
        List<DanhMuc> tatCa = danhMucRepository.findAll(Sort.by("thuTu"));
        Map<Long, List<AdminDanhMucResponse>> conTheoCha = new HashMap<>();
        for (DanhMuc d : tatCa) {
            if (d.getDanhMucCha() != null) {
                conTheoCha.computeIfAbsent(d.getDanhMucCha().getId(), k -> new ArrayList<>())
                        .add(AdminDanhMucResponse.tu(d, soSanPham.getOrDefault(d.getId(), 0L), List.of()));
            }
        }
        return tatCa.stream()
                .filter(d -> d.getDanhMucCha() == null)
                .map(d -> AdminDanhMucResponse.tu(d, soSanPham.getOrDefault(d.getId(), 0L),
                        conTheoCha.getOrDefault(d.getId(), List.of())))
                .toList();
    }

    @Transactional
    public AdminDanhMucResponse tao(AdminDanhMucRequest req) {
        if (danhMucRepository.existsBySlug(SlugUtil.taoSlug(req.ten()))) {
            throw ApiException.xungDot("Danh mục đã tồn tại");
        }
        DanhMuc dm = new DanhMuc();
        DanhMuc cha = req.danhMucChaId() == null ? null : timCha(req.danhMucChaId());
        ganDuLieu(dm, req, taoSlugDuyNhat(req.ten(), 0), cha);
        return AdminDanhMucResponse.tu(danhMucRepository.save(dm), 0, List.of());
    }

    @Transactional
    public AdminDanhMucResponse capNhat(Long id, AdminDanhMucRequest req) {
        DanhMuc dm = timTheoId(id);
        String slugGoc = SlugUtil.taoSlug(req.ten());
        if (danhMucRepository.existsBySlugAndIdNot(slugGoc, id)) {
            throw ApiException.xungDot("Tên danh mục trùng với danh mục khác");
        }
        DanhMuc cha = null;
        if (req.danhMucChaId() != null) {
            if (req.danhMucChaId().equals(id)) {
                throw ApiException.yeuCauKhongHopLe("Danh mục không thể là cha của chính nó");
            }
            cha = timCha(req.danhMucChaId());
            if (danhMucRepository.countByDanhMucChaId(id) > 0) {
                throw ApiException.yeuCauKhongHopLe(
                        "Danh mục đang có danh mục con nên không thể làm danh mục con");
            }
        }
        // Giữ slug cũ nếu tên không đổi về mặt slug (tránh làm hỏng đường dẫn đã chia sẻ)
        String slug = SlugUtil.cungGoc(dm.getSlug(), slugGoc) ? dm.getSlug() : taoSlugDuyNhat(req.ten(), id);
        ganDuLieu(dm, req, slug, cha);
        return AdminDanhMucResponse.tu(dm, sanPhamRepository.countByDanhMucId(id), List.of());
    }

    /** Xóa mềm (cập nhật deleted_at), chỉ khi không còn danh mục con và sản phẩm. */
    @Transactional
    public void xoa(Long id) {
        DanhMuc dm = timTheoId(id);
        if (danhMucRepository.countByDanhMucChaId(id) > 0) {
            throw ApiException.xungDot("Không thể xóa: danh mục còn danh mục con");
        }
        if (sanPhamRepository.countByDanhMucId(id) > 0) {
            throw ApiException.xungDot("Không thể xóa: danh mục còn sản phẩm");
        }
        danhMucRepository.delete(dm);
    }

    /** Slug không trùng với bất kỳ danh mục nào, kể cả danh mục đã xóa mềm. */
    private String taoSlugDuyNhat(String ten, long boQuaId) {
        return SlugUtil.taoSlugDuyNhat(ten, s -> danhMucRepository.demSlugKeCaDaXoa(s, boQuaId) > 0);
    }

    private DanhMuc timTheoId(Long id) {
        return danhMucRepository.findById(id)
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy danh mục"));
    }

    private DanhMuc timCha(Long chaId) {
        DanhMuc cha = timTheoId(chaId);
        if (cha.getDanhMucCha() != null) {
            throw ApiException.yeuCauKhongHopLe("Chỉ chọn danh mục cấp gốc làm danh mục cha");
        }
        return cha;
    }

    private void ganDuLieu(DanhMuc dm, AdminDanhMucRequest req, String slug, DanhMuc cha) {
        dm.setTen(req.ten().trim());
        dm.setSlug(slug);
        dm.setDanhMucCha(cha);
        dm.setMoTa(req.moTa());
        dm.setHinhAnh(req.hinhAnh());
        dm.setThuTu(req.thuTu());
        dm.setHienThi(req.hienThi() == null || req.hienThi());
    }
}
