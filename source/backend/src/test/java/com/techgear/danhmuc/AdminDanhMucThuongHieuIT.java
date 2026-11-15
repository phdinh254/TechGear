package com.techgear.danhmuc;

import com.techgear.KiemThuQuanTri;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminDanhMucThuongHieuIT extends KiemThuQuanTri {

    @Test
    void chiQuanTriDuocGoi() throws Exception {
        mvc.perform(get("/api/admin/danh-muc")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/danh-muc").header("Authorization", khach))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.thongBao").value("Bạn không có quyền thực hiện thao tác này"));
        mvc.perform(post("/api/admin/thuong-hieu").header("Authorization", khach)
                        .contentType("application/json").content("{\"ten\":\"X\"}"))
                .andExpect(status().isForbidden());
        qt(get("/api/admin/danh-muc"), null).andExpect(status().isOk());
    }

    @Test
    void danhMucHaiCapSlugTuSinhVaRangBuocXoa() throws Exception {
        long cha = id(qt(post("/api/admin/danh-muc"), "{\"ten\":\"Phụ kiện máy ảnh\",\"thuTu\":3}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("phu-kien-may-anh"))
                .andExpect(jsonPath("$.hienThi").value(true))
                .andExpect(jsonPath("$.danhMucChaId").doesNotExist()));
        qt(post("/api/admin/danh-muc"), "{\"ten\":\"Phụ kiện máy ảnh\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("phu-kien-may-anh-2"));
        long con = id(qt(post("/api/admin/danh-muc"),
                "{\"ten\":\"Chân máy\",\"danhMucChaId\":%d,\"hienThi\":false}".formatted(cha))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.danhMucChaId").value(cha)));
        qt(post("/api/admin/danh-muc"), "{\"ten\":\"Cháu\",\"danhMucChaId\":%d}".formatted(con))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thongBao").value("Danh mục cha phải là danh mục cấp 1"));
        long khac = id(qt(post("/api/admin/danh-muc"), "{\"ten\":\"Khác\"}"));
        qt(put("/api/admin/danh-muc/" + cha), "{\"ten\":\"Phụ kiện máy ảnh\",\"danhMucChaId\":%d}".formatted(khac))
                .andExpect(status().isBadRequest());
        qt(put("/api/admin/danh-muc/" + khac), "{\"ten\":\"Khác\",\"danhMucChaId\":%d}".formatted(khac))
                .andExpect(status().isBadRequest());
        qt(post("/api/admin/danh-muc"), "{\"ten\":\"Trùng\",\"slug\":\"phu-kien-may-anh\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.thongBao").value("Slug đã được dùng"));
        qt(post("/api/admin/danh-muc"), "{\"ten\":\"Sai\",\"slug\":\"Có Dấu\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.loi.slug").exists());

        qt(get("/api/admin/danh-muc"), null)
                .andExpect(jsonPath("$[?(@.id == %d)].soDanhMucCon".formatted(cha)).value(1))
                .andExpect(jsonPath("$[?(@.id == %d)].hienThi".formatted(con)).value(false));
        qt(delete("/api/admin/danh-muc/" + cha), null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.thongBao").value("Danh mục còn danh mục con"));
        qt(delete("/api/admin/danh-muc/" + con), null).andExpect(status().isNoContent());
        tao.sanPhamCoBan("SP", cha, null, 100_000);
        qt(delete("/api/admin/danh-muc/" + cha), null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.thongBao").value("Danh mục còn sản phẩm"));
        qt(delete("/api/admin/danh-muc/" + con), null).andExpect(status().isNotFound());
        qt(put("/api/admin/danh-muc/" + khac), "{\"ten\":\"Đổi tên\",\"slug\":\"doi-ten\",\"thuTu\":9}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("doi-ten"))
                .andExpect(jsonPath("$.thuTu").value(9));
    }

    @Test
    void thuongHieuThemSuaXoa() throws Exception {
        long th = id(qt(post("/api/admin/thuong-hieu"), "{\"ten\":\"Sennheiser\",\"logo\":\"/uploads/thuong-hieu/s.png\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("sennheiser"))
                .andExpect(jsonPath("$.soSanPham").value(0)));
        qt(put("/api/admin/thuong-hieu/" + th), "{\"ten\":\"Sennheiser Đức\",\"hienThi\":false}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ten").value("Sennheiser Đức"))
                .andExpect(jsonPath("$.slug").value("sennheiser-duc"))
                .andExpect(jsonPath("$.hienThi").value(false));
        long dm = tao.danhMuc("DM", "dm-th", null, 0, true);
        long sp = tao.sanPhamCoBan("Tai nghe", dm, th, 100_000);
        qt(get("/api/admin/thuong-hieu"), null).andExpect(jsonPath("$[?(@.id == %d)].soSanPham".formatted(th)).value(1));
        qt(delete("/api/admin/thuong-hieu/" + th), null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.thongBao").value("Thương hiệu còn sản phẩm"));
        tao.sua("UPDATE san_pham SET deleted_at = now() WHERE id = ?", sp);
        qt(delete("/api/admin/thuong-hieu/" + th), null).andExpect(status().isNoContent());
        qt(get("/api/admin/thuong-hieu"), null).andExpect(jsonPath("$[*].id", not(hasItem((int) th))));
        qt(post("/api/admin/thuong-hieu"), "{\"ten\":\"Sennheiser Đức\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("sennheiser-duc"));
    }
}
