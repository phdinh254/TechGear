package com.techgear.service.admin;

import com.techgear.dto.admin.AdminDanhMucRequest;
import com.techgear.dto.admin.AdminDanhMucResponse;
import com.techgear.entity.DanhMuc;
import com.techgear.exception.ApiException;
import com.techgear.repository.DanhMucRepository;
import com.techgear.repository.SanPhamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.Collections;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDanhMucServiceTest {

    @Mock
    DanhMucRepository danhMucRepository;
    @Mock
    SanPhamRepository sanPhamRepository;
    @InjectMocks
    AdminDanhMucService service;

    private static DanhMuc danhMuc(Long id, String ten, String slug, DanhMuc cha) {
        DanhMuc d = new DanhMuc();
        d.setId(id);
        d.setTen(ten);
        d.setSlug(slug);
        d.setDanhMucCha(cha);
        return d;
    }

    private static AdminDanhMucRequest req(String ten, Long chaId) {
        return new AdminDanhMucRequest(ten, chaId, null, null, 0, null);
    }

    private static void assertLoi(HttpStatus status, String message, Runnable r) {
        ApiException ex = assertThrows(ApiException.class, r::run);
        assertEquals(status, ex.getStatus());
        assertEquals(message, ex.getMessage());
    }

    @Test
    void taoTrungTenBiTuChoi() {
        when(danhMucRepository.existsBySlug("tai-nghe")).thenReturn(true);

        assertLoi(HttpStatus.CONFLICT, "Danh mục đã tồn tại", () -> service.tao(req("Tai nghe", null)));
        verify(danhMucRepository, never()).save(any());
    }

    @Test
    void taoGiaTriMacDinhVaSlug() {
        when(danhMucRepository.save(any(DanhMuc.class))).thenAnswer(i -> i.getArgument(0));

        AdminDanhMucResponse res = service.tao(req(" Tai nghe ", null));

        assertEquals("Tai nghe", res.ten());
        assertEquals("tai-nghe", res.slug());
        assertEquals(true, res.hienThi());
    }

    @Test
    void chonChaKhongPhaiGocBiTuChoi() {
        DanhMuc goc = danhMuc(1L, "Âm thanh", "am-thanh", null);
        DanhMuc con = danhMuc(2L, "Loa", "loa", goc);
        when(danhMucRepository.findById(2L)).thenReturn(Optional.of(con));

        assertLoi(HttpStatus.BAD_REQUEST, "Chỉ chọn danh mục cấp gốc làm danh mục cha",
                () -> service.tao(req("Loa mini", 2L)));
    }

    @Test
    void danhMucCoConKhongTheThanhCon() {
        DanhMuc cha = danhMuc(1L, "Âm thanh", "am-thanh", null);
        DanhMuc dm = danhMuc(5L, "Phụ kiện", "phu-kien", null);
        when(danhMucRepository.findById(5L)).thenReturn(Optional.of(dm));
        when(danhMucRepository.findById(1L)).thenReturn(Optional.of(cha));
        when(danhMucRepository.countByDanhMucChaId(5L)).thenReturn(2L);

        assertLoi(HttpStatus.BAD_REQUEST, "Danh mục đang có danh mục con nên không thể làm danh mục con",
                () -> service.capNhat(5L, req("Phụ kiện", 1L)));
    }

    @Test
    void khongTheLaChaCuaChinhNo() {
        DanhMuc dm = danhMuc(5L, "Phụ kiện", "phu-kien", null);
        when(danhMucRepository.findById(5L)).thenReturn(Optional.of(dm));

        ApiException ex = assertThrows(ApiException.class, () -> service.capNhat(5L, req("Phụ kiện", 5L)));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void suaTrungTenDanhMucKhacBiTuChoi() {
        DanhMuc dm = danhMuc(5L, "Phụ kiện", "phu-kien", null);
        when(danhMucRepository.findById(5L)).thenReturn(Optional.of(dm));
        when(danhMucRepository.existsBySlugAndIdNot("tai-nghe", 5L)).thenReturn(true);

        assertLoi(HttpStatus.CONFLICT, "Tên danh mục trùng với danh mục khác",
                () -> service.capNhat(5L, req("Tai nghe", null)));
    }

    @Test
    void suaTenGiuSlugCu() {
        DanhMuc dm = danhMuc(5L, "Tai nghe", "tai-nghe-2", null);
        when(danhMucRepository.findById(5L)).thenReturn(Optional.of(dm));

        AdminDanhMucResponse res = service.capNhat(5L, req("Tai nghe", null));

        assertEquals("tai-nghe-2", res.slug());
        verify(danhMucRepository, never()).demSlugKeCaDaXoa(any(), org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void xoaDanhMucConConBiChan() {
        DanhMuc dm = danhMuc(1L, "Âm thanh", "am-thanh", null);
        when(danhMucRepository.findById(1L)).thenReturn(Optional.of(dm));
        when(danhMucRepository.countByDanhMucChaId(1L)).thenReturn(1L);

        assertLoi(HttpStatus.CONFLICT, "Không thể xóa: danh mục còn danh mục con", () -> service.xoa(1L));
        verify(danhMucRepository, never()).delete(any());
    }

    @Test
    void xoaDanhMucConSanPhamBiChan() {
        DanhMuc dm = danhMuc(1L, "Âm thanh", "am-thanh", null);
        when(danhMucRepository.findById(1L)).thenReturn(Optional.of(dm));
        when(sanPhamRepository.countByDanhMucId(1L)).thenReturn(3L);

        assertLoi(HttpStatus.CONFLICT, "Không thể xóa: danh mục còn sản phẩm", () -> service.xoa(1L));
        verify(danhMucRepository, never()).delete(any());
    }

    @Test
    void xoaDanhMucTrongThiXoaMem() {
        DanhMuc dm = danhMuc(1L, "Âm thanh", "am-thanh", null);
        when(danhMucRepository.findById(1L)).thenReturn(Optional.of(dm));

        service.xoa(1L);

        verify(danhMucRepository).delete(dm);
    }

    @Test
    void layCayGomCaDanhMucAnVaDemSanPham() {
        DanhMuc goc = danhMuc(1L, "Âm thanh", "am-thanh", null);
        DanhMuc conAn = danhMuc(2L, "Loa", "loa", goc);
        conAn.setHienThi(false);
        when(danhMucRepository.findAll(Sort.by("thuTu"))).thenReturn(List.of(goc, conAn));
        when(sanPhamRepository.demTheoDanhMuc()).thenReturn(
                Collections.singletonList(new Object[]{2L, 4L}));

        List<AdminDanhMucResponse> cay = service.layCay();

        assertEquals(1, cay.size());
        assertEquals(0, cay.get(0).soSanPham());
        assertEquals(1, cay.get(0).danhMucCons().size());
        AdminDanhMucResponse con = cay.get(0).danhMucCons().get(0);
        assertEquals(false, con.hienThi());
        assertEquals(4, con.soSanPham());
        assertEquals(1L, con.danhMucChaId());
    }
}
