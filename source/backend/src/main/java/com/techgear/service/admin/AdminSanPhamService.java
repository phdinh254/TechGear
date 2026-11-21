package com.techgear.service.admin;

import com.techgear.dto.PageResponse;
import com.techgear.dto.admin.AdminSanPhamChiTietResponse;
import com.techgear.dto.admin.AdminSanPhamRequest;
import com.techgear.dto.admin.AdminSanPhamTomTatResponse;
import com.techgear.entity.BienTheSanPham;
import com.techgear.entity.DanhMuc;
import com.techgear.entity.GiaTriTuyChon;
import com.techgear.entity.HinhAnhSanPham;
import com.techgear.entity.LichSuTonKho;
import com.techgear.entity.SanPham;
import com.techgear.entity.ThuocTinhSanPham;
import com.techgear.entity.ThuongHieu;
import com.techgear.entity.TuyChon;
import com.techgear.entity.enums.LoaiBienDongKho;
import com.techgear.exception.ApiException;
import com.techgear.repository.BienTheSanPhamRepository;
import com.techgear.repository.ChiTietGioHangRepository;
import com.techgear.repository.DanhMucRepository;
import com.techgear.repository.LichSuTonKhoRepository;
import com.techgear.repository.NguoiDungRepository;
import com.techgear.repository.SanPhamRepository;
import com.techgear.repository.ThuongHieuRepository;
import com.techgear.repository.YeuThichRepository;
import com.techgear.util.SlugUtil;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminSanPhamService {

    private static final int KICH_THUOC_TOI_DA = 50;
    private static final List<String> TRANG_THAI = List.of("dang_ban", "dang_an", "het_hang");
    private static final String LOI_THIEU_ANH = "Sản phẩm đang hiển thị phải có ít nhất 1 ảnh";

    private final SanPhamRepository sanPhamRepository;
    private final BienTheSanPhamRepository bienTheSanPhamRepository;
    private final DanhMucRepository danhMucRepository;
    private final ThuongHieuRepository thuongHieuRepository;
    private final ChiTietGioHangRepository chiTietGioHangRepository;
    private final YeuThichRepository yeuThichRepository;
    private final LichSuTonKhoRepository lichSuTonKhoRepository;
    private final NguoiDungRepository nguoiDungRepository;
    private final BienTheDongBo bienTheDongBo;

    // ---------------- đọc ----------------

    /**
     * Danh sách sản phẩm cho quản trị, gồm cả sản phẩm đang ẩn.
     *
     * @param tuKhoa     tìm theo tên sản phẩm hoặc SKU biến thể
     * @param danhMucId  lọc theo danh mục (gồm cả danh mục con)
     * @param trangThai  dang_ban | dang_an | het_hang (không còn biến thể đang bán nào có tồn kho)
     */
    @Transactional(readOnly = true)
    public PageResponse<AdminSanPhamTomTatResponse> timKiem(String tuKhoa, Long danhMucId, Long thuongHieuId,
                                                            String trangThai, int trang, int kichThuoc) {
        if (StringUtils.hasText(trangThai) && !TRANG_THAI.contains(trangThai)) {
            throw ApiException.yeuCauKhongHopLe("Trạng thái không hợp lệ");
        }
        Specification<SanPham> spec = (root, query, cb) -> {
            List<Predicate> dk = new ArrayList<>();
            if (StringUtils.hasText(tuKhoa)) {
                String mau = "%" + tuKhoa.trim().toLowerCase(Locale.ROOT) + "%";
                Subquery<Long> theoSku = query.subquery(Long.class);
                Root<BienTheSanPham> b = theoSku.from(BienTheSanPham.class);
                theoSku.select(b.get("id")).where(cb.equal(b.get("sanPham"), root), cb.like(cb.lower(b.get("sku")), mau));
                dk.add(cb.or(cb.like(cb.lower(root.get("ten")), mau), cb.exists(theoSku)));
            }
            if (danhMucId != null) {
                Join<SanPham, DanhMuc> dm = root.join("danhMuc");
                Join<DanhMuc, DanhMuc> cha = dm.join("danhMucCha", JoinType.LEFT);
                dk.add(cb.or(cb.equal(dm.get("id"), danhMucId), cb.equal(cha.get("id"), danhMucId)));
            }
            if (thuongHieuId != null) {
                dk.add(cb.equal(root.get("thuongHieu").get("id"), thuongHieuId));
            }
            if (StringUtils.hasText(trangThai)) {
                switch (trangThai) {
                    case "dang_ban" -> dk.add(cb.isTrue(root.get("hienThi")));
                    case "dang_an" -> dk.add(cb.isFalse(root.get("hienThi")));
                    case "het_hang" -> {
                        Subquery<Long> conHang = query.subquery(Long.class);
                        Root<BienTheSanPham> b = conHang.from(BienTheSanPham.class);
                        conHang.select(b.get("id")).where(cb.equal(b.get("sanPham"), root),
                                cb.isTrue(b.get("hienThi")), cb.gt(b.get("soLuongTon"), 0));
                        dk.add(cb.not(cb.exists(conHang)));
                    }
                    default -> throw new IllegalStateException(trangThai);
                }
            }
            return cb.and(dk.toArray(new Predicate[0]));
        };
        PageRequest pageable = PageRequest.of(Math.max(trang, 0),
                Math.min(Math.max(kichThuoc, 1), KICH_THUOC_TOI_DA), Sort.by(Sort.Direction.DESC, "id"));
        return PageResponse.tu(sanPhamRepository.findAll(spec, pageable).map(this::taoTomTat));
    }

    @Transactional(readOnly = true)
    public AdminSanPhamChiTietResponse layChiTiet(Long id) {
        return taoChiTiet(timTheoId(id));
    }

    // ---------------- ghi ----------------

    @Transactional
    public AdminSanPhamChiTietResponse tao(AdminSanPhamRequest req, Long adminId) {
        SanPham sp = new SanPham();
        luu(sp, req, adminId);
        return taoChiTiet(sp);
    }

    @Transactional
    public AdminSanPhamChiTietResponse capNhat(Long id, AdminSanPhamRequest req, Long adminId) {
        SanPham sp = timTheoId(id);
        luu(sp, req, adminId);
        return taoChiTiet(sp);
    }

    @Transactional
    public void doiHienThi(Long id, boolean hienThi) {
        SanPham sp = timTheoId(id);
        if (hienThi && sp.getHinhAnhs().isEmpty()) {
            throw ApiException.yeuCauKhongHopLe(LOI_THIEU_ANH);
        }
        sp.setHienThi(hienThi);
    }

    /**
     * Xóa mềm sản phẩm và biến thể (giải phóng SKU), gỡ khỏi giỏ hàng và yêu thích.
     * Không dùng repository.delete: cascade sẽ xóa cứng ảnh, thông số, tùy chọn.
     */
    @Transactional
    public void xoa(Long id) {
        timTheoId(id);
        // Thứ tự quan trọng: subquery giỏ hàng chỉ thấy biến thể chưa bị xóa mềm.
        chiTietGioHangRepository.xoaTheoSanPham(id);
        yeuThichRepository.xoaTheoSanPham(id);
        bienTheSanPhamRepository.xoaMemTheoSanPham(id);
        sanPhamRepository.xoaMem(id);
    }

    /** Dùng chung cho tạo và sửa: kiểm tra toàn bộ request trước khi đổi entity. */
    private void luu(SanPham sp, AdminSanPhamRequest req, Long adminId) {
        // 1. Kiểm tra
        bienTheDongBo.kiemTra(req);
        kiemTraGia(req);
        List<AdminSanPhamRequest.HinhAnh> hinhAnhs = req.hinhAnhs() == null ? List.of() : req.hinhAnhs();
        kiemTraHinhAnh(req, hinhAnhs);
        DanhMuc danhMuc = danhMucRepository.findById(req.danhMucId())
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy danh mục"));
        ThuongHieu thuongHieu = req.thuongHieuId() == null ? null : thuongHieuRepository.findById(req.thuongHieuId())
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy thương hiệu"));
        for (AdminSanPhamRequest.BienThe bt : req.bienThes()) {
            String sku = chuanSku(bt.sku());
            boolean daDung = bienTheSanPhamRepository.timSanPhamIdTheoSkuKeCaDaXoa(sku).stream()
                    .anyMatch(spId -> !spId.equals(sp.getId()));
            if (daDung) {
                throw ApiException.xungDot("SKU " + sku + " đã được dùng cho sản phẩm khác");
            }
        }

        // 2. Trường đơn + slug (giữ slug cũ nếu tên không đổi về mặt slug)
        String slug = SlugUtil.cungGoc(sp.getSlug(), SlugUtil.taoSlug(req.ten())) ? sp.getSlug()
                : taoSlugDuyNhat(req.ten(), sp.getId() == null ? 0 : sp.getId());
        sp.setTen(req.ten().trim());
        sp.setSlug(slug);
        sp.setDanhMuc(danhMuc);
        sp.setThuongHieu(thuongHieu);
        sp.setMoTaNgan(req.moTaNgan());
        sp.setMoTa(req.moTa());
        sp.setGiaBan(req.giaBan());
        sp.setGiaGoc(req.giaGoc());
        sp.setBaoHanhThang(req.baoHanhThang());
        sp.setNoiBat(Boolean.TRUE.equals(req.noiBat()));
        sp.setHienThi(!Boolean.FALSE.equals(req.hienThi()));

        // 3. Tùy chọn / biến thể
        BienTheDongBo.KetQua kq = bienTheDongBo.dongBo(sp, req);

        // 4. Ảnh + thông số: thay toàn bộ theo request. Ảnh theo giá trị tùy chọn chỉ được gắn giá trị
        //    sau lần flush ở bước 6: Hibernate cascade hinhAnhs trước tuyChons, giá trị mới lúc này còn transient.
        sp.getHinhAnhs().clear();
        Map<HinhAnhSanPham, GiaTriTuyChon> anhTheoGiaTri = new IdentityHashMap<>();
        boolean coAnhChinh = hinhAnhs.stream().anyMatch(AdminSanPhamRequest.HinhAnh::laAnhChinh);
        for (int i = 0; i < hinhAnhs.size(); i++) {
            AdminSanPhamRequest.HinhAnh r = hinhAnhs.get(i);
            HinhAnhSanPham ha = new HinhAnhSanPham();
            ha.setSanPham(sp);
            if (r.tuyChon() != null) {
                anhTheoGiaTri.put(ha, timGiaTri(sp, r.tuyChon(), r.giaTri()));
            }
            ha.setDuongDan(r.duongDan().trim());
            ha.setMoTa(r.moTa());
            ha.setThuTu(i);
            ha.setLaAnhChinh(coAnhChinh ? r.laAnhChinh() : i == 0);
            sp.getHinhAnhs().add(ha);
        }
        sp.getThuocTinhs().clear();
        List<AdminSanPhamRequest.ThongSo> thongSos = req.thongSos() == null ? List.of() : req.thongSos();
        for (int i = 0; i < thongSos.size(); i++) {
            ThuocTinhSanPham tt = new ThuocTinhSanPham();
            tt.setSanPham(sp);
            tt.setTen(thongSos.get(i).ten().trim());
            tt.setGiaTri(thongSos.get(i).giaTri().trim());
            tt.setThuTu(i);
            sp.getThuocTinhs().add(tt);
        }

        // 5. Biến thể bị xóa: gỡ khỏi giỏ hàng
        List<Long> idBiXoa = kq.biXoa().stream().map(BienTheSanPham::getId).filter(Objects::nonNull).toList();
        if (!idBiXoa.isEmpty()) {
            chiTietGioHangRepository.xoaTheoBienThe(idBiXoa);
        }

        // 6. Ghi xóa mềm + SKU đã giải phóng TRƯỚC khi chèn biến thể mới (có thể dùng lại SKU cũ)
        //    Sản phẩm đã có thì chỉ flush, không save (= merge): merge sao chép tùy chọn / giá trị mới thành
        //    instance khác, biến thể mới và ảnh vẫn trỏ vào instance cũ chưa lưu.
        if (sp.getId() == null) {
            sanPhamRepository.save(sp);
        }
        sanPhamRepository.flush();
        anhTheoGiaTri.forEach(HinhAnhSanPham::setGiaTriTuyChon);

        // 7. Chèn biến thể mới (kèm cập nhật ảnh theo giá trị)
        sp.getBienThes().addAll(kq.moi());
        sanPhamRepository.flush();

        // 8. Lịch sử tồn kho khởi tạo
        for (BienTheSanPham bt : kq.moi()) {
            LichSuTonKho ls = new LichSuTonKho();
            ls.setBienThe(bt);
            ls.setLoai(LoaiBienDongKho.khoi_tao);
            ls.setSoLuongThayDoi(bt.getSoLuongTon());
            ls.setSoLuongSau(bt.getSoLuongTon());
            ls.setGhiChu("Khởi tạo biến thể");
            ls.setNguoiThucHien(nguoiDungRepository.getReferenceById(adminId));
            lichSuTonKhoRepository.save(ls);
        }
    }

    // ---------------- kiểm tra ----------------

    private static void kiemTraGia(AdminSanPhamRequest req) {
        if (req.giaGoc() != null && req.giaGoc().compareTo(req.giaBan()) < 0) {
            throw ApiException.yeuCauKhongHopLe("Giá gốc phải lớn hơn hoặc bằng giá bán");
        }
        for (AdminSanPhamRequest.BienThe bt : req.bienThes()) {
            BigDecimal giaBan = bt.giaBan() != null ? bt.giaBan() : req.giaBan();
            BigDecimal giaGoc = bt.giaGoc() != null ? bt.giaGoc() : req.giaGoc();
            if (giaGoc != null && giaGoc.compareTo(giaBan) < 0) {
                throw ApiException.yeuCauKhongHopLe(
                        "Giá gốc của biến thể " + chuanSku(bt.sku()) + " phải lớn hơn hoặc bằng giá bán");
            }
        }
    }

    private static void kiemTraHinhAnh(AdminSanPhamRequest req, List<AdminSanPhamRequest.HinhAnh> hinhAnhs) {
        if (!Boolean.FALSE.equals(req.hienThi()) && hinhAnhs.isEmpty()) {
            throw ApiException.yeuCauKhongHopLe(LOI_THIEU_ANH);
        }
        if (hinhAnhs.stream().filter(AdminSanPhamRequest.HinhAnh::laAnhChinh).count() > 1) {
            throw ApiException.yeuCauKhongHopLe("Chỉ chọn một ảnh chính");
        }
        List<AdminSanPhamRequest.TuyChon> tuyChons = req.tuyChons() == null ? List.of() : req.tuyChons();
        for (AdminSanPhamRequest.HinhAnh ha : hinhAnhs) {
            if (ha.tuyChon() == null && ha.giaTri() == null) {
                continue;
            }
            boolean hopLe = ha.tuyChon() != null && ha.giaTri() != null && tuyChons.stream()
                    .filter(tc -> chuan(tc.ten()).equals(chuan(ha.tuyChon())))
                    .flatMap(tc -> tc.giaTris().stream())
                    .anyMatch(gt -> chuan(gt.giaTri()).equals(chuan(ha.giaTri())));
            if (!hopLe) {
                throw ApiException.yeuCauKhongHopLe("Ảnh gắn với giá trị tùy chọn không tồn tại: "
                        + ha.tuyChon() + " / " + ha.giaTri());
            }
        }
    }

    // ---------------- tiện ích ----------------

    private SanPham timTheoId(Long id) {
        return sanPhamRepository.findById(id)
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy sản phẩm"));
    }

    /** Slug không trùng với bất kỳ sản phẩm nào, kể cả sản phẩm đã xóa mềm. */
    private String taoSlugDuyNhat(String ten, long boQuaId) {
        return SlugUtil.taoSlugDuyNhat(ten, s -> sanPhamRepository.demSlugKeCaDaXoa(s, boQuaId) > 0);
    }

    /** Giá trị tùy chọn trên entity (sau đồng bộ) theo tên tùy chọn + tên giá trị; đã kiểm tra tồn tại. */
    private static GiaTriTuyChon timGiaTri(SanPham sp, String tuyChon, String giaTri) {
        return sp.getTuyChons().stream()
                .filter(tc -> chuan(tc.getTen()).equals(chuan(tuyChon)))
                .flatMap(tc -> tc.getGiaTris().stream())
                .filter(gt -> chuan(gt.getGiaTri()).equals(chuan(giaTri)))
                .findFirst()
                .orElseThrow(() -> ApiException.yeuCauKhongHopLe(
                        "Ảnh gắn với giá trị tùy chọn không tồn tại: " + tuyChon + " / " + giaTri));
    }

    private static String chuan(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    private static String chuanSku(String sku) {
        return sku.trim().toUpperCase(Locale.ROOT);
    }

    private AdminSanPhamTomTatResponse taoTomTat(SanPham sp) {
        String anhChinh = sp.getHinhAnhs().stream()
                .filter(ha -> Boolean.TRUE.equals(ha.getLaAnhChinh()))
                .map(HinhAnhSanPham::getDuongDan)
                .findFirst()
                .orElse(sp.getHinhAnhs().isEmpty() ? null : sp.getHinhAnhs().get(0).getDuongDan());
        int tongTon = sp.getBienThes().stream().filter(bt -> Boolean.TRUE.equals(bt.getHienThi()))
                .mapToInt(BienTheSanPham::getSoLuongTon).sum();
        return new AdminSanPhamTomTatResponse(sp.getId(), sp.getTen(), sp.getSlug(), anhChinh,
                sp.getDanhMuc().getTen(), sp.getThuongHieu() == null ? null : sp.getThuongHieu().getTen(),
                sp.getGiaBan(), sp.getGiaGoc(), tongTon, sp.getBienThes().size(), sp.getNoiBat(), sp.getHienThi());
    }

    private static AdminSanPhamChiTietResponse taoChiTiet(SanPham sp) {
        List<AdminSanPhamChiTietResponse.HinhAnh> hinhAnhs = sp.getHinhAnhs().stream()
                .map(ha -> {
                    GiaTriTuyChon gt = ha.getGiaTriTuyChon();
                    return new AdminSanPhamChiTietResponse.HinhAnh(ha.getId(), ha.getDuongDan(), ha.getMoTa(),
                            Boolean.TRUE.equals(ha.getLaAnhChinh()),
                            gt == null ? null : gt.getTuyChon().getTen(), gt == null ? null : gt.getGiaTri());
                })
                .toList();
        List<AdminSanPhamChiTietResponse.ThongSo> thongSos = sp.getThuocTinhs().stream()
                .map(tt -> new AdminSanPhamChiTietResponse.ThongSo(tt.getTen(), tt.getGiaTri()))
                .toList();
        List<AdminSanPhamChiTietResponse.TuyChon> tuyChons = sp.getTuyChons().stream()
                .map(tc -> new AdminSanPhamChiTietResponse.TuyChon(tc.getId(), tc.getTen(), tc.getLoai(),
                        tc.getGiaTris().stream()
                                .map(gt -> new AdminSanPhamChiTietResponse.GiaTri(gt.getId(), gt.getGiaTri(),
                                        gt.getMaMau()))
                                .toList()))
                .toList();
        List<AdminSanPhamChiTietResponse.BienThe> bienThes = sp.getBienThes().stream()
                .map(bt -> new AdminSanPhamChiTietResponse.BienThe(bt.getId(),
                        bt.getGiaTris().stream()
                                .sorted(Comparator.comparing((GiaTriTuyChon gt) -> gt.getTuyChon().getThuTu()))
                                .map(GiaTriTuyChon::getGiaTri)
                                .toList(),
                        bt.getSku(), bt.getGiaBan(), bt.getGiaGoc(), bt.getSoLuongTon(), bt.getNguongCanhBao(),
                        Boolean.TRUE.equals(bt.getHienThi())))
                .toList();
        return new AdminSanPhamChiTietResponse(sp.getId(), sp.getTen(), sp.getSlug(),
                sp.getDanhMuc() == null ? null : sp.getDanhMuc().getId(),
                sp.getThuongHieu() == null ? null : sp.getThuongHieu().getId(),
                sp.getMoTaNgan(), sp.getMoTa(), sp.getGiaBan(), sp.getGiaGoc(), sp.getBaoHanhThang(),
                sp.getNoiBat(), sp.getHienThi(), hinhAnhs, thongSos, tuyChons, bienThes);
    }
}
