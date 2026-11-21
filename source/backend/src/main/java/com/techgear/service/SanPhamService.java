package com.techgear.service;

import com.techgear.dto.DanhMucResponse;
import com.techgear.dto.PageResponse;
import com.techgear.dto.SanPhamChiTietResponse;
import com.techgear.dto.SanPhamTomTatResponse;
import com.techgear.dto.ThuongHieuResponse;
import com.techgear.entity.BienTheSanPham;
import com.techgear.entity.DanhMuc;
import com.techgear.entity.GiaTriTuyChon;
import com.techgear.entity.HinhAnhSanPham;
import com.techgear.entity.SanPham;
import com.techgear.entity.ThuongHieu;
import com.techgear.exception.ApiException;
import com.techgear.repository.SanPhamRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
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
        if (giaTu != null && giaDen != null && giaTu.compareTo(giaDen) > 0) {
            throw ApiException.yeuCauKhongHopLe("Giá từ không được lớn hơn giá đến");
        }
        List<Long> danhMucIds = StringUtils.hasText(danhMucSlug)
                ? danhMucService.layIdKemDanhMucCon(danhMucSlug) : null;

        Specification<SanPham> locTheoDieuKien = (root, query, cb) -> {
            List<Predicate> dk = new ArrayList<>();
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
            if (giaTu != null || giaDen != null) {
                dk.add(locTheoGiaBienThe(root, query, cb, giaTu, giaDen));
            }
            return cb.and(dk.toArray(new Predicate[0]));
        };
        Specification<SanPham> spec = dieuKienDangBan().and(locTheoDieuKien);

        PageRequest pageable = PageRequest.of(Math.max(trang, 0),
                Math.min(Math.max(kichThuoc, 1), KICH_THUOC_TOI_DA), taoSapXep(sapXep));
        Page<SanPhamTomTatResponse> page = sanPhamRepository.findAll(spec, pageable).map(this::taoTomTat);
        return PageResponse.tu(page);
    }

    @Transactional(readOnly = true)
    public List<SanPhamTomTatResponse> layNoiBat() {
        Specification<SanPham> noiBat = (root, query, cb) -> cb.isTrue(root.get("noiBat"));
        return sanPhamRepository.findAll(dieuKienDangBan().and(noiBat),
                        PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "id")))
                .getContent().stream().map(this::taoTomTat).toList();
    }

    /** Sản phẩm đang bán: sản phẩm, danh mục và danh mục cha (nếu có) đều hiển thị. */
    static Specification<SanPham> dieuKienDangBan() {
        return (root, query, cb) -> {
            Join<SanPham, DanhMuc> danhMuc = root.join("danhMuc");
            Join<DanhMuc, DanhMuc> cha = danhMuc.join("danhMucCha", JoinType.LEFT);
            return cb.and(cb.isTrue(root.get("hienThi")), cb.isTrue(danhMuc.get("hienThi")),
                    cb.or(cb.isNull(cha.get("id")), cb.isTrue(cha.get("hienThi"))));
        };
    }

    /** Lấy chi tiết sản phẩm theo slug và tăng lượt xem. */
    @Transactional
    public SanPhamChiTietResponse layChiTiet(String slug) {
        SanPham sp = sanPhamRepository.findBySlugAndHienThiTrue(slug)
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy sản phẩm"));
        if (!sp.dangBan()) {
            throw ApiException.khongTimThay("Không tìm thấy sản phẩm");
        }
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
                danhMuc, sp.getThuongHieu() == null ? null : ThuongHieuResponse.tu(sp.getThuongHieu()),
                hinhAnhs, thongSos, tuyChons, bienThes);
    }

    public SanPhamTomTatResponse taoTomTat(SanPham sp) {
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

    /**
     * Sản phẩm khớp khoảng giá khi có ít nhất một biến thể đang hiển thị có giá (giá riêng của biến thể,
     * NULL thì lấy giá sản phẩm) nằm trong khoảng. Sản phẩm chưa có biến thể hiển thị thì xét giá sản phẩm.
     */
    private Predicate locTheoGiaBienThe(Root<SanPham> root, CriteriaQuery<?> query, CriteriaBuilder cb,
                                        BigDecimal giaTu, BigDecimal giaDen) {
        Subquery<Long> bienTheKhopGia = query.subquery(Long.class);
        Root<BienTheSanPham> bt = bienTheKhopGia.from(BienTheSanPham.class);
        Expression<BigDecimal> giaHieuLuc = cb.coalesce(bt.<BigDecimal>get("giaBan"), root.<BigDecimal>get("giaBan"));
        List<Predicate> dkBienThe = new ArrayList<>();
        dkBienThe.add(cb.equal(bt.get("sanPham"), root));
        dkBienThe.add(cb.isTrue(bt.get("hienThi")));
        dkBienThe.addAll(trongKhoang(cb, giaHieuLuc, giaTu, giaDen));
        bienTheKhopGia.select(bt.get("id")).where(dkBienThe.toArray(new Predicate[0]));

        Subquery<Long> coBienThe = query.subquery(Long.class);
        Root<BienTheSanPham> bt2 = coBienThe.from(BienTheSanPham.class);
        coBienThe.select(bt2.get("id")).where(cb.equal(bt2.get("sanPham"), root), cb.isTrue(bt2.get("hienThi")));

        List<Predicate> dkGiaSanPham = new ArrayList<>(trongKhoang(cb, root.get("giaBan"), giaTu, giaDen));
        dkGiaSanPham.add(cb.not(cb.exists(coBienThe)));
        return cb.or(cb.exists(bienTheKhopGia), cb.and(dkGiaSanPham.toArray(new Predicate[0])));
    }

    private static List<Predicate> trongKhoang(CriteriaBuilder cb, Expression<BigDecimal> gia,
                                               BigDecimal giaTu, BigDecimal giaDen) {
        List<Predicate> dk = new ArrayList<>();
        if (giaTu != null) dk.add(cb.greaterThanOrEqualTo(gia, giaTu));
        if (giaDen != null) dk.add(cb.lessThanOrEqualTo(gia, giaDen));
        return dk;
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
