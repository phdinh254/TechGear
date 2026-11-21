package com.techgear.chung.loi;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class XuLyLoiToanCucTest {

    record DangKyThu(@NotBlank(message = "Vui lòng nhập họ tên") String hoTen,
                     @Email(message = "Email không đúng định dạng") String email) {
    }

    @RestController
    static class ControllerThu {
        @GetMapping("/thu/khong-tim-thay")
        void khongTimThay() {
            throw ApiException.khongTimThay("Không tìm thấy sản phẩm");
        }

        @GetMapping("/thu/xung-dot")
        void xungDot() {
            throw ApiException.xungDot("Email đã được sử dụng");
        }

        @GetMapping("/thu/vi-pham-csdl")
        void viPhamCsdl() {
            throw new DataIntegrityViolationException("duplicate key value violates unique constraint");
        }

        @GetMapping("/thu/so")
        int so(@RequestParam int trang) {
            return trang;
        }

        @GetMapping("/thu/trang-thai-418")
        void trangThai418() {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.I_AM_A_TEAPOT);
        }

        @GetMapping("/thu/loi-he-thong")
        void loiHeThong() {
            throw new IllegalStateException("chi tiết nội bộ không được lộ ra");
        }

        @PostMapping("/thu/dang-ky")
        void dangKy(@Valid @RequestBody DangKyThu req) {
        }
    }

    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new ControllerThu())
            .setControllerAdvice(new XuLyLoiToanCuc()).build();

    @Test
    void apiExceptionTraDungMaVaThongBao() throws Exception {
        mvc.perform(get("/thu/khong-tim-thay"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.trangThai").value(404))
                .andExpect(jsonPath("$.thongBao").value("Không tìm thấy sản phẩm"))
                .andExpect(jsonPath("$.thoiGian", startsWith("20")))
                .andExpect(jsonPath("$.loi").doesNotExist());
        mvc.perform(get("/thu/xung-dot")).andExpect(status().isConflict());
    }

    @Test
    void loiKiemTraDuLieuLietKeTungTruong() throws Exception {
        mvc.perform(post("/thu/dang-ky").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hoTen\":\"\",\"email\":\"sai\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thongBao").value("Dữ liệu không hợp lệ"))
                .andExpect(jsonPath("$.loi.hoTen").value("Vui lòng nhập họ tên"))
                .andExpect(jsonPath("$.loi.email").value("Email không đúng định dạng"));
    }

    @Test
    void jsonHongVaThamSoSaiKieuLa400() throws Exception {
        mvc.perform(post("/thu/dang-ky").contentType(MediaType.APPLICATION_JSON).content("{hong"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thongBao").value("Dữ liệu gửi lên không đúng định dạng"));
        mvc.perform(get("/thu/so").param("trang", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thongBao").value("Tham số 'trang' không hợp lệ"));
        mvc.perform(get("/thu/so"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.thongBao").value("Thiếu tham số 'trang'"));
    }

    @Test
    void saiPhuongThucLa405VaSaiKieuNoiDungLa415() throws Exception {
        mvc.perform(delete("/thu/so")).andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.thongBao").value("Phương thức DELETE không được hỗ trợ"));
        mvc.perform(post("/thu/dang-ky").contentType(MediaType.TEXT_PLAIN).content("x"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void loiCoSanMaCuaSpringGiuDungMaKhongThanh500() throws Exception {
        mvc.perform(get("/thu/so").param("trang", "1").accept(MediaType.APPLICATION_XML))
                .andExpect(status().isNotAcceptable());
        mvc.perform(get("/thu/trang-thai-418"))
                .andExpect(status().isIAmATeapot())
                .andExpect(jsonPath("$.trangThai").value(418))
                .andExpect(jsonPath("$.thongBao").value("Yêu cầu không thể xử lý"));
    }

    @Test
    void viPhamRangBuocCsdlLa409KhongLoChiTiet() throws Exception {
        mvc.perform(get("/thu/vi-pham-csdl"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.thongBao").value("Dữ liệu bị trùng hoặc đang được sử dụng"));
    }

    @Test
    void loiKhongLuongTruocLa500KhongLoChiTietNoiBo() throws Exception {
        mvc.perform(get("/thu/loi-he-thong"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.thongBao").value("Đã xảy ra lỗi hệ thống, vui lòng thử lại sau"));
    }
}
