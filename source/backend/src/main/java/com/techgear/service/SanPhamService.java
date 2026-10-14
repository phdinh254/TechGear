package com.techgear.service;

import com.techgear.dto.DanhMucResponse;
import com.techgear.dto.PageResponse;
import com.techgear.dto.SanPhamChiTietResponse;
import com.techgear.dto.SanPhamTomTatResponse;
import com.techgear.dto.ThuongHieuResponse;
import com.techgear.entity.DanhMuc;
import com.techgear.entity.GiaTriTuyChon;
import com.techgear.entity.HinhAnhSanPham;
import com.techgear.entity.SanPham;
import com.techgear.entity.ThuongHieu;
import com.techgear.exception.ApiException;
import com.techgear.repository.SanPhamRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SanPhamService {

    private static final int KICH_THUOC_TOI_DA = 48;

    private final SanPhamRepository sanPhamRepository;
    private final DanhMucService danhMucService;

    /**
     * Tìm kiếm, lọc và phân trang sản phẩm.
     *
     * @param sapXep moi_nhat | gia_tang | gia_giam | pho_bien
     */
    @Transactional(readOnly = true)
    public PageResponse<SanPhamTomTatResponse> timKiem(String tuKhoa, String danhMucSlug, String thuongHieuSlug,
                                                       BigDecimal giaTu, BigDecimal giaDen,
                                                       String sapXep, int trang, int kichThuoc) {
        List<Long> danhMucIds = StringUtils.hasText(danhMucSlug)
                ? danhMucService.layIdKemDanhMucCon(danhMucSlug) : null;

        Specification<SanPham> spec = (root, query, cb) -> {
            List<Predicate> dk = new ArrayList<>();
            dk.add(cb.isTrue(root.get("hienThi")));
            if (StringUtils.hasText(tuKhoa)) {
                dk.add(cb.like(cb.lower(root.get("ten")), "%" + tuKhoa.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (danhMucIds != null) {
                dk.add(root.get("danhMuc").get("id").in(danhMucIds));
            }
            if (StringUtils.hasText(thuongHieuSlug)) {
                Join<SanPham, ThuongHieu> th = root.join("thuongHieu");
                dk.add(cb.equal(th.get("slug"), thuongHieuSlug));
            }
            if (giaTu != null) {
                dk.add(cb.greaterThanOrEqualTo(root.get("giaBan"), giaTu));
            }
            if (giaDen != null) {
                dk.add(cb.lessThanOrEqualTo(root.get("giaBan"), giaDen));
            }
            return cb.and(dk.toArray(new Predicate[0]));
        };

        PageRequest pageable = PageRequest.of(Math.max(trang, 0),
                Math.min(Math.max(kichThuoc, 1), KICH_THUOC_TOI_DA), taoSapXep(sapXep));
        Page<SanPhamTomTatResponse> page = sanPhamRepository.findAll(spec, pageable).map(this::sangTomTat);
        return PageResponse.tu(page);
    }

    @Transactional(readOnly = true)
    public List<SanPhamTomTatResponse> layNoiBat() {
        return sanPhamRepository.findTop8ByHienThiTrueAndNoiBatTrueOrderByIdDesc().stream()
                .map(this::sangTomTat).toList();
    }

    /** Lấy chi tiết sản phẩm theo slug và tăng lượt xem. */
    @Transactional
    public SanPhamChiTietResponse layChiTiet(String slug) {
        SanPham sp = sanPhamRepository.findBySlugAndHienThiTrue(slug)
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy sản phẩm"));
        sanPhamRepository.tangLuotXem(sp.getId());

        DanhMuc dm = sp.getDanhMuc();
        DanhMucResponse danhMuc = new DanhMucResponse(dm.getId(), dm.getTen(), dm.getSlug(), dm.getHinhAnh());

        List<SanPhamChiTietResponse.HinhAnh> hinhAnhs = sp.getHinhAnhs().stream()
                .map(ha -> new SanPhamChiTietResponse.HinhAnh(ha.getId(), ha.getDuongDan(), ha.getMoTa(),
                        ha.getGiaTriTuyChon() == null ? null : ha.getGiaTriTuyChon().getId(),
                        Boolean.TRUE.equals(ha.getLaAnhChinh())))
                .toList();

        List<SanPhamChiTietResponse.ThongSo> thongSos = sp.getThuocTinhs().stream()
                .map(tt -> new SanPhamChiTietResponse.ThongSo(tt.getTen(), tt.getGiaTri()))
                .toList();

        List<SanPhamChiTietResponse.TuyChon> tuyChons = sp.getTuyChons().stream()
                .map(tc -> new SanPhamChiTietResponse.TuyChon(tc.getId(), tc.getTen(), tc.getLoai().name(),
                        tc.getGiaTris().stream()
                                .map(gt -> new SanPhamChiTietResponse.GiaTri(gt.getId(), gt.getGiaTri(), gt.getMaMau()))
                                .toList()))
                .toList();

        List<SanPhamChiTietResponse.BienThe> bienThes = sp.getBienThes().stream()
                .filter(bt -> Boolean.TRUE.equals(bt.getHienThi()))
                .map(bt -> new SanPhamChiTietResponse.BienThe(bt.getId(), bt.getSku(),
                        bt.layGiaBan(), bt.layGiaGoc(), bt.getSoLuongTon(),
                        bt.getGiaTris().stream().map(GiaTriTuyChon::getId).sorted().toList()))
                .toList();

        return new SanPhamChiTietResponse(sp.getId(), sp.getTen(), sp.getSlug(), sp.getMoTaNgan(), sp.getMoTa(),
                sp.getGiaBan(), sp.getGiaGoc(), sp.getBaoHanhThang(), sp.getLuotXem() + 1,
                danhMuc, ThuongHieuResponse.tu(sp.getThuongHieu()),
                hinhAnhs, thongSos, tuyChons, bienThes);
    }

    private SanPhamTomTatResponse sangTomTat(SanPham sp) {
        String anhChinh = sp.getHinhAnhs().stream()
                .filter(ha -> Boolean.TRUE.equals(ha.getLaAnhChinh()))
                .map(HinhAnhSanPham::getDuongDan)
                .findFirst()
                .orElse(sp.getHinhAnhs().isEmpty() ? null : sp.getHinhAnhs().get(0).getDuongDan());
        boolean conHang = sp.getBienThes().stream()
                .anyMatch(bt -> Boolean.TRUE.equals(bt.getHienThi()) && bt.getSoLuongTon() > 0);
        return new SanPhamTomTatResponse(sp.getId(), sp.getTen(), sp.getSlug(), sp.getMoTaNgan(),
                sp.getGiaBan(), sp.getGiaGoc(), anhChinh,
                sp.getThuongHieu() == null ? null : sp.getThuongHieu().getTen(), conHang);
    }

    private Sort taoSapXep(String sapXep) {
        if (sapXep == null) {
            return Sort.by(Sort.Direction.DESC, "id");
        }
        return switch (sapXep) {
            case "gia_tang" -> Sort.by(Sort.Direction.ASC, "giaBan");
            case "gia_giam" -> Sort.by(Sort.Direction.DESC, "giaBan");
            case "pho_bien" -> Sort.by(Sort.Direction.DESC, "luotXem");
            default -> Sort.by(Sort.Direction.DESC, "id");
        };
    }
}
