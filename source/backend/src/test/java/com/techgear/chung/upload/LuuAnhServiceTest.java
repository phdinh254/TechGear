package com.techgear.chung.upload;

import com.techgear.chung.loi.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LuuAnhServiceTest {

    static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
    static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0};
    static final byte[] WEBP = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P', 0};

    @TempDir
    Path goc;

    LuuAnhService service() {
        return new LuuAnhService(goc.toString());
    }

    @Test
    void luuTheoKieuThatCuaNoiDungKhongTheoTenFile() throws Exception {
        String duongDan = service().luu(new MockMultipartFile("file", "virus.exe", "application/octet-stream", PNG), "san-pham");

        assertTrue(duongDan.startsWith("/uploads/san-pham/") && duongDan.endsWith(".png"), duongDan);
        assertTrue(Files.exists(goc.resolve(duongDan.substring("/uploads/".length()))));
    }

    @Test
    void nhanJpgVaWebp() {
        assertTrue(service().luu(new MockMultipartFile("file", "a", "image/jpeg", JPG), "banner").endsWith(".jpg"));
        assertTrue(service().luu(new MockMultipartFile("file", "a", "image/webp", WEBP), "banner").endsWith(".webp"));
    }

    @Test
    void tuChoiFileKhongPhaiAnhDuDatTenPng() {
        ApiException e = assertThrows(ApiException.class, () -> service().luu(
                new MockMultipartFile("file", "anh.png", "image/png", "<script>alert(1)</script>".getBytes()), "san-pham"));
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, e.getTrangThai());
    }

    @Test
    void tuChoiThuMucNgoaiDanhSachVaFileRong() {
        ApiException e = assertThrows(ApiException.class,
                () -> service().luu(new MockMultipartFile("file", "a.png", "image/png", PNG), "../ngoai"));
        assertEquals(HttpStatus.BAD_REQUEST, e.getTrangThai());
        assertThrows(ApiException.class,
                () -> service().luu(new MockMultipartFile("file", "a.png", "image/png", new byte[0]), "san-pham"));
    }

    @Test
    void xoaChiTrongThuMucUploadVaKhongLoiKhiFileKhongCon() throws Exception {
        Path bimat = Files.writeString(goc.getParent().resolve("bi-mat-" + goc.getFileName() + ".txt"), "x");
        try {
            service().xoa("/uploads/../" + bimat.getFileName());
            assertTrue(Files.exists(bimat));

            String duongDan = service().luu(new MockMultipartFile("file", "a", "image/png", PNG), "san-pham");
            service().xoa(duongDan);
            assertFalse(Files.exists(goc.resolve(duongDan.substring("/uploads/".length()))));
            service().xoa(duongDan);
        } finally {
            Files.deleteIfExists(bimat);
        }
    }
}
