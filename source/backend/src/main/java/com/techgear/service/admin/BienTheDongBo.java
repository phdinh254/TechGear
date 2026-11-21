package com.techgear.service.admin;

import com.techgear.dto.admin.AdminSanPhamRequest;
import com.techgear.entity.BienTheSanPham;
import com.techgear.entity.GiaTriTuyChon;
import com.techgear.entity.SanPham;
import com.techgear.entity.TuyChon;
import com.techgear.exception.ApiException;
import com.techgear.util.SkuUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Kiểm tra và đồng bộ tùy chọn / biến thể của sản phẩm từ request quản trị.
 * Biến thể là tích Descartes các giá trị tùy chọn; khi sửa thì giữ id và tồn kho của tổ hợp còn tồn tại,
 * tạo biến thể cho tổ hợp mới và trả về biến thể bị bỏ để service xóa mềm.
 */
@Component
public class BienTheDongBo {

    private static final int TOI_DA_BIEN_THE = 50;

    /**
     * @param moi   biến thể mới, chưa gắn vào {@code sp.getBienThes()}
     * @param biXoa biến thể đã gỡ khỏi {@code sp.getBienThes()}, SKU đã được giải phóng
     */
    public record KetQua(List<BienTheSanPham> moi, List<BienTheSanPham> biXoa) {
    }

    // ---------------- kiểm tra ----------------

    public void kiemTra(AdminSanPhamRequest req) {
        List<AdminSanPhamRequest.TuyChon> tuyChons = tuyChons(req);
        List<AdminSanPhamRequest.BienThe> bienThes = req.bienThes() == null ? List.of() : req.bienThes();

        // 1. Tên tùy chọn không trùng
        Map<String, String> tenTuyChon = new HashMap<>();
        for (AdminSanPhamRequest.TuyChon tc : tuyChons) {
            String daCo = tenTuyChon.putIfAbsent(chuan(tc.ten()), tc.ten().trim());
            if (daCo != null) {
                throw ApiException.yeuCauKhongHopLe("Tên tùy chọn bị trùng: " + daCo);
            }
        }

        // 2. Giá trị trong mỗi tùy chọn không trùng
        List<Set<String>> giaTriTheoTuyChon = new ArrayList<>();
        for (AdminSanPhamRequest.TuyChon tc : tuyChons) {
            Set<String> giaTris = new HashSet<>();
            for (AdminSanPhamRequest.GiaTri gt : tc.giaTris()) {
                if (!giaTris.add(chuan(gt.giaTri()))) {
                    throw ApiException.yeuCauKhongHopLe(
                            "Giá trị \"" + gt.giaTri().trim() + "\" bị trùng trong tùy chọn \"" + tc.ten().trim() + "\"");
                }
            }
            giaTriTheoTuyChon.add(giaTris);
        }

        // 3. Số tổ hợp không vượt 50
        long soToHop = 1;
        for (AdminSanPhamRequest.TuyChon tc : tuyChons) {
            soToHop = Math.min(soToHop * tc.giaTris().size(), TOI_DA_BIEN_THE + 1L);
        }
        if (soToHop > TOI_DA_BIEN_THE) {
            throw ApiException.yeuCauKhongHopLe("Tối đa 50 biến thể");
        }

        // 4. Không có tùy chọn thì đúng 1 biến thể không gắn giá trị
        if (tuyChons.isEmpty() && (bienThes.size() != 1 || !bienThes.get(0).giaTris().isEmpty())) {
            throw ApiException.yeuCauKhongHopLe("Sản phẩm không có tùy chọn phải có đúng 1 biến thể");
        }

        // 5. Mỗi biến thể có đủ giá trị, đúng tùy chọn theo vị trí
        for (AdminSanPhamRequest.BienThe bt : bienThes) {
            if (bt.giaTris().size() != tuyChons.size()) {
                throw ApiException.yeuCauKhongHopLe("Mỗi biến thể phải có đúng một giá trị cho từng tùy chọn");
            }
            for (int i = 0; i < tuyChons.size(); i++) {
                String giaTri = bt.giaTris().get(i);
                if (giaTri == null || !giaTriTheoTuyChon.get(i).contains(chuan(giaTri))) {
                    throw ApiException.yeuCauKhongHopLe("Giá trị \"" + (giaTri == null ? "" : giaTri.trim())
                            + "\" không thuộc tùy chọn \"" + tuyChons.get(i).ten().trim() + "\"");
                }
            }
        }

        // 6. Không trùng tổ hợp
        Set<List<String>> khoas = new HashSet<>();
        for (AdminSanPhamRequest.BienThe bt : bienThes) {
            if (!khoas.add(bt.giaTris().stream().map(BienTheDongBo::chuan).toList())) {
                throw ApiException.yeuCauKhongHopLe("Có tổ hợp biến thể bị trùng");
            }
        }

        // 7. Đủ mọi tổ hợp
        if (bienThes.size() != soToHop) {
            throw ApiException.yeuCauKhongHopLe("Danh sách biến thể phải gồm đủ " + soToHop + " tổ hợp");
        }

        // 8. SKU không trùng trong request
        Set<String> skus = new HashSet<>();
        for (AdminSanPhamRequest.BienThe bt : bienThes) {
            String sku = chuanSku(bt.sku());
            if (!skus.add(sku)) {
                throw ApiException.xungDot("SKU " + sku + " bị trùng");
            }
        }
    }

    // ---------------- đồng bộ ----------------

    public KetQua dongBo(SanPham sp, AdminSanPhamRequest req) {
        List<AdminSanPhamRequest.TuyChon> reqTuyChons = tuyChons(req);

        // Bước 1: so khớp và cập nhật tùy chọn / giá trị tại chỗ (đổi tên giữ nguyên entity).
        List<TuyChon> tuyChonCu = new ArrayList<>(sp.getTuyChons());
        List<TuyChon> khopTuyChon = khop(reqTuyChons, tuyChonCu,
                AdminSanPhamRequest.TuyChon::id, AdminSanPhamRequest.TuyChon::ten, TuyChon::getId, TuyChon::getTen,
                () -> ApiException.yeuCauKhongHopLe("Tùy chọn không thuộc sản phẩm"));

        Set<GiaTriTuyChon> giaTriGiuLai = Collections.newSetFromMap(new IdentityHashMap<>());
        List<GiaTriTuyChon> giaTriBo = new ArrayList<>();
        for (int i = 0; i < reqTuyChons.size(); i++) {
            AdminSanPhamRequest.TuyChon r = reqTuyChons.get(i);
            TuyChon tc = khopTuyChon.get(i);
            if (tc == null) {
                tc = new TuyChon();
                tc.setSanPham(sp);
                sp.getTuyChons().add(tc);
                khopTuyChon.set(i, tc);
            }
            tc.setTen(r.ten().trim());
            tc.setLoai(r.loai());
            tc.setThuTu(i);

            List<GiaTriTuyChon> giaTriCu = new ArrayList<>(tc.getGiaTris());
            List<GiaTriTuyChon> khopGiaTri = khop(r.giaTris(), giaTriCu,
                    AdminSanPhamRequest.GiaTri::id, AdminSanPhamRequest.GiaTri::giaTri,
                    GiaTriTuyChon::getId, GiaTriTuyChon::getGiaTri,
                    () -> ApiException.yeuCauKhongHopLe("Giá trị không thuộc tùy chọn"));
            for (int j = 0; j < r.giaTris().size(); j++) {
                AdminSanPhamRequest.GiaTri rg = r.giaTris().get(j);
                GiaTriTuyChon gt = khopGiaTri.get(j);
                if (gt == null) {
                    gt = new GiaTriTuyChon();
                    gt.setTuyChon(tc);
                    tc.getGiaTris().add(gt);
                }
                gt.setGiaTri(rg.giaTri().trim());
                gt.setMaMau(rg.maMau());
                gt.setThuTu(j);
                giaTriGiuLai.add(gt);
            }
            giaTriCu.stream().filter(gt -> !giaTriGiuLai.contains(gt)).forEach(giaTriBo::add);
        }
        Set<TuyChon> tuyChonGiuLai = Collections.newSetFromMap(new IdentityHashMap<>());
        tuyChonGiuLai.addAll(khopTuyChon);
        List<TuyChon> tuyChonBo = tuyChonCu.stream().filter(tc -> !tuyChonGiuLai.contains(tc)).toList();

        // Bước 2: khóa của biến thể hiện có, tính sau khi đổi tên nhưng trước khi gỡ giá trị / tùy chọn cũ.
        // Biến thể còn dính giá trị sắp bị gỡ thì không thể khớp với tổ hợp nào trong request.
        Map<String, BienTheSanPham> hienCo = new HashMap<>();
        for (BienTheSanPham bt : sp.getBienThes()) {
            if (giaTriGiuLai.containsAll(bt.getGiaTris())) {
                hienCo.putIfAbsent(khoa(bt), bt);
            }
        }

        Set<BienTheSanPham> daKhop = Collections.newSetFromMap(new IdentityHashMap<>());
        List<BienTheSanPham> moi = new ArrayList<>();
        for (AdminSanPhamRequest.BienThe r : req.bienThes()) {
            List<GiaTriTuyChon> giaTris = timGiaTri(r.giaTris(), khopTuyChon, giaTriGiuLai);
            BienTheSanPham bt = hienCo.remove(khoaGiaTri(giaTris));
            if (bt != null) {
                daKhop.add(bt);
            } else {
                bt = new BienTheSanPham();
                bt.setSanPham(sp);
                bt.getGiaTris().addAll(giaTris);
                bt.setSoLuongTon(r.tonKhoBanDau() != null ? r.tonKhoBanDau() : 0);
                moi.add(bt);
            }
            bt.setSku(chuanSku(r.sku()));
            bt.setGiaBan(r.giaBan());
            bt.setGiaGoc(r.giaGoc());
            bt.setNguongCanhBao(r.nguongCanhBao());
            bt.setHienThi(!Boolean.FALSE.equals(r.dangBan()));
        }

        List<BienTheSanPham> biXoa = new ArrayList<>();
        for (BienTheSanPham bt : new ArrayList<>(sp.getBienThes())) {
            if (!daKhop.contains(bt)) {
                // Chỉ để bộ nhớ nhất quán: việc giải phóng SKU thật do @SQLDelete trong SQL đảm nhiệm (authoritative).
                bt.setSku(SkuUtil.giaiPhong(bt.getSku(), bt.getId()));
                sp.getBienThes().remove(bt);
                biXoa.add(bt);
            }
        }

        // Gỡ giá trị / tùy chọn cũ không còn dùng (orphanRemoval xóa cứng), rồi sắp lại theo thứ tự request.
        for (GiaTriTuyChon gt : giaTriBo) {
            gt.getTuyChon().getGiaTris().remove(gt);
        }
        sp.getTuyChons().removeAll(tuyChonBo);
        sp.getTuyChons().sort(Comparator.comparing(TuyChon::getThuTu));
        for (TuyChon tc : sp.getTuyChons()) {
            tc.getGiaTris().sort(Comparator.comparing(GiaTriTuyChon::getThuTu));
        }

        return new KetQua(moi, biXoa);
    }

    // ---------------- khóa ----------------

    /** Khóa từ các phần "tenTuyChon=giaTri" đã xếp theo thứ tự tùy chọn. Biến thể mặc định có khóa "". */
    public static String khoa(List<String> giaTris) {
        return String.join("|", giaTris);
    }

    /** Khóa của biến thể hiện có: giá trị xếp theo thứ tự tùy chọn của chúng. */
    public static String khoa(BienTheSanPham bt) {
        List<GiaTriTuyChon> giaTris = new ArrayList<>(bt.getGiaTris());
        giaTris.sort(Comparator.comparing((GiaTriTuyChon gt) -> gt.getTuyChon().getThuTu())
                .thenComparing(gt -> gt.getTuyChon().getTen()));
        return khoaGiaTri(giaTris);
    }

    private static String khoaGiaTri(List<GiaTriTuyChon> giaTris) {
        return khoa(giaTris.stream().map(gt -> gt.getTuyChon().getTen() + "=" + gt.getGiaTri()).toList());
    }

    // ---------------- tiện ích ----------------

    /**
     * So khớp từng phần tử request với entity cũ: trước theo id (không có -> lỗi), sau đó phần tử không id
     * khớp theo tên với entity chưa được dùng. Trả về danh sách cùng thứ tự request, null = cần tạo mới.
     */
    private static <R, E> List<E> khop(List<R> yeuCau, List<E> cu,
                                       Function<R, Long> idYeuCau,
                                       Function<R, String> tenYeuCau,
                                       Function<E, Long> idCu,
                                       Function<E, String> tenCu,
                                       Supplier<ApiException> khongThuoc) {
        List<E> ketQua = new ArrayList<>(Collections.nCopies(yeuCau.size(), null));
        Set<E> daDung = Collections.newSetFromMap(new IdentityHashMap<>());
        for (int i = 0; i < yeuCau.size(); i++) {
            Long id = idYeuCau.apply(yeuCau.get(i));
            if (id == null) {
                continue;
            }
            E e = cu.stream().filter(x -> id.equals(idCu.apply(x))).findFirst().orElseThrow(khongThuoc);
            if (!daDung.add(e)) {
                throw khongThuoc.get();
            }
            ketQua.set(i, e);
        }
        for (int i = 0; i < yeuCau.size(); i++) {
            R r = yeuCau.get(i);
            if (idYeuCau.apply(r) != null) {
                continue;
            }
            String ten = chuan(tenYeuCau.apply(r));
            for (E e : cu) {
                if (!daDung.contains(e) && Objects.equals(ten, chuan(tenCu.apply(e)))) {
                    daDung.add(e);
                    ketQua.set(i, e);
                    break;
                }
            }
        }
        return ketQua;
    }

    /** Ánh xạ giá trị dạng chuỗi của một biến thể sang entity được giữ lại, theo vị trí tùy chọn. */
    private static List<GiaTriTuyChon> timGiaTri(List<String> giaTris, List<TuyChon> tuyChons,
                                                 Set<GiaTriTuyChon> giuLai) {
        if (giaTris.size() != tuyChons.size()) {
            throw ApiException.yeuCauKhongHopLe("Mỗi biến thể phải có đúng một giá trị cho từng tùy chọn");
        }
        List<GiaTriTuyChon> ketQua = new ArrayList<>();
        for (int i = 0; i < tuyChons.size(); i++) {
            TuyChon tc = tuyChons.get(i);
            String giaTri = giaTris.get(i);
            ketQua.add(tc.getGiaTris().stream()
                    .filter(gt -> giuLai.contains(gt) && Objects.equals(chuan(giaTri), chuan(gt.getGiaTri())))
                    .findFirst()
                    .orElseThrow(() -> ApiException.yeuCauKhongHopLe("Giá trị \"" + (giaTri == null ? "" : giaTri.trim())
                            + "\" không thuộc tùy chọn \"" + tc.getTen() + "\"")));
        }
        return ketQua;
    }

    private static List<AdminSanPhamRequest.TuyChon> tuyChons(AdminSanPhamRequest req) {
        return req.tuyChons() == null ? List.of() : req.tuyChons();
    }

    private static String chuan(String s) {
        return s == null ? null : s.trim().toLowerCase(Locale.ROOT);
    }

    private static String chuanSku(String sku) {
        return sku.trim().toUpperCase(Locale.ROOT);
    }
}
