package com.techgear.sanpham;

import com.techgear.chung.PageResponse;
import com.techgear.chung.loi.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SanPhamService {

    static final int SO_SAN_PHAM_NOI_BAT = 8;
    private static final int TOI_DA_TU_KHOA = 5;

    private final SanPhamRepository sanPhamRepository;

    @Transactional(readOnly = true)
    public PageResponse<SanPhamTomTat> tim(String tuKhoa, String danhMuc, List<String> thuongHieus, Long giaTu,
                                           Long giaDen, SapXep sapXep, int trang, int kichThuoc) {
        if (giaTu != null && giaDen != null && giaTu > giaDen) {
            throw ApiException.yeuCauKhongHopLe("Giá từ phải nhỏ hơn hoặc bằng giá đến");
        }
        List<String> tuKhoas = tuKhoa == null ? List.of()
                : Arrays.stream(tuKhoa.trim().split("\\s+")).filter(t -> !t.isEmpty()).limit(TOI_DA_TU_KHOA).toList();
        List<String> slugs = thuongHieus == null ? List.of()
                : thuongHieus.stream().map(String::trim).filter(s -> !s.isEmpty()).toList();
        String slugDanhMuc = danhMuc == null || danhMuc.isBlank() ? null : danhMuc.trim();

        SanPhamRepository.TrangKetQua kq = sanPhamRepository.tim(
                new SanPhamRepository.DieuKien(tuKhoas, slugDanhMuc, slugs, giaTu, giaDen), sapXep, trang, kichThuoc);
        int tongSoTrang = (int) ((kq.tongSoPhanTu() + kichThuoc - 1) / kichThuoc);
        return new PageResponse<>(kq.noiDung(), trang, kichThuoc, kq.tongSoPhanTu(), tongSoTrang);
    }

    @Transactional(readOnly = true)
    public List<SanPhamTomTat> noiBat() {
        return sanPhamRepository.noiBat(SO_SAN_PHAM_NOI_BAT);
    }

    /** Thẻ của các sản phẩm đang hiển thị, giữ thứ tự {@code ids}; sản phẩm đã ẩn bị bỏ qua. */
    @Transactional(readOnly = true)
    public List<SanPhamTomTat> tomTatTheoThuTu(List<Long> ids) {
        Map<Long, SanPhamTomTat> theoId = sanPhamRepository.theoIds(ids).stream()
                .collect(Collectors.toMap(SanPhamTomTat::id, Function.identity()));
        return ids.stream().map(theoId::get).filter(Objects::nonNull).toList();
    }

    @Transactional(readOnly = true)
    public boolean dangHienThi(Long sanPhamId) {
        return sanPhamRepository.dangHienThi(sanPhamId);
    }

    /** Chi tiết sản phẩm đang hiển thị; mỗi lần xem tăng lượt xem 1. */
    @Transactional
    public SanPhamChiTiet chiTiet(String slug) {
        SanPhamRepository.ThongTinChung sp = sanPhamRepository.timTheoSlug(slug)
                .orElseThrow(() -> ApiException.khongTimThay("Không tìm thấy sản phẩm"));
        sanPhamRepository.tangLuotXem(sp.id());

        List<SanPhamChiTiet.BienThe> bienThes = sanPhamRepository.bienThes(sp.id());
        return new SanPhamChiTiet(sp.id(), sp.ten(), sp.slug(), sp.moTaNgan(), sp.moTa(), sp.giaBan(), sp.giaGoc(),
                sp.baoHanhThang(), bienThes.stream().anyMatch(b -> b.soLuongTon() > 0),
                new SanPhamChiTiet.ThamChieu(sp.dmId(), sp.dmTen(), sp.dmSlug()),
                sp.thId() == null ? null : new SanPhamChiTiet.ThamChieu(sp.thId(), sp.thTen(), sp.thSlug()),
                sanPhamRepository.hinhAnhs(sp.id()), sanPhamRepository.thongSos(sp.id()),
                sanPhamRepository.tuyChons(sp.id()), bienThes);
    }
}
