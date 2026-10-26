package com.techgear.service;

import com.techgear.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageServiceTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 0};

    @TempDir
    Path thuMucTam;

    @Test
    void luuAnhPngVaXoaDuoc() {
        FileStorageService service = new FileStorageService(thuMucTam.toString());
        String duongDan = service.luuAnh(new MockMultipartFile("file", "a.png", "image/png", PNG), "san-pham");

        assertTrue(duongDan.matches("/uploads/san-pham/[0-9a-f]{32}\\.png"));
        Path file = thuMucTam.resolve(duongDan.substring("/uploads/".length()));
        assertTrue(Files.exists(file));

        service.xoa(duongDan);
        assertFalse(Files.exists(file));
    }

    @Test
    void tuChoiFileKhongPhaiAnhDuDoiDuoiJpg() {
        FileStorageService service = new FileStorageService(thuMucTam.toString());
        MockMultipartFile gia = new MockMultipartFile("file", "virus.jpg", "image/jpeg", "<?php echo 1;".getBytes());
        assertThrows(ApiException.class, () -> service.luuAnh(gia, "san-pham"));
    }

    @Test
    void tuChoiThuMucLa() {
        FileStorageService service = new FileStorageService(thuMucTam.toString());
        assertThrows(ApiException.class,
                () -> service.luuAnh(new MockMultipartFile("file", "a.png", "image/png", PNG), "../etc"));
    }

    @Test
    void xoaBoQuaDuongDanThoatRaNgoai() throws Exception {
        Path ngoai = Files.writeString(thuMucTam.getParent().resolve("ngoai-" + System.nanoTime() + ".txt"), "x");
        FileStorageService service = new FileStorageService(thuMucTam.toString());
        service.xoa("/uploads/../" + ngoai.getFileName());
        assertTrue(Files.exists(ngoai));
        Files.delete(ngoai);
    }
}
