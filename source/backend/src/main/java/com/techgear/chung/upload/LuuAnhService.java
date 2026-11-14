package com.techgear.chung.upload;

import com.techgear.chung.loi.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

/** Lưu ảnh upload vào thư mục cấu hình, phục vụ tại /uploads/**. Kiểu ảnh nhận diện theo nội dung file. */
@Slf4j
@Service
public class LuuAnhService {

    public static final String TIEN_TO = "/uploads/";
    static final Set<String> THU_MUC_HOP_LE = Set.of("san-pham", "banner", "thuong-hieu", "danh-muc", "anh-dai-dien");

    private final Path goc;

    public LuuAnhService(@Value("${app.upload.thu-muc}") String thuMuc) {
        this.goc = thuMucGoc(thuMuc);
    }

    static Path thuMucGoc(String thuMuc) {
        return Paths.get(thuMuc).toAbsolutePath().normalize();
    }

    /** @return đường dẫn công khai, VD /uploads/san-pham/2f1c...png */
    public String luu(MultipartFile file, String thuMuc) {
        if (!THU_MUC_HOP_LE.contains(thuMuc)) {
            throw ApiException.yeuCauKhongHopLe("Thư mục lưu ảnh không hợp lệ");
        }
        if (file == null || file.isEmpty()) {
            throw ApiException.yeuCauKhongHopLe("Vui lòng chọn ảnh");
        }
        String duoi = nhanDienAnh(file);
        String ten = UUID.randomUUID() + "." + duoi;
        try {
            Path dich = goc.resolve(thuMuc).resolve(ten).normalize();
            Files.createDirectories(dich.getParent());
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, dich);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Không lưu được ảnh", e);
        }
        return TIEN_TO + thuMuc + "/" + ten;
    }

    /** Xóa ảnh theo đường dẫn công khai; bỏ qua đường dẫn nằm ngoài thư mục upload hoặc file đã không còn. */
    public void xoa(String duongDan) {
        if (duongDan == null || !duongDan.startsWith(TIEN_TO)) {
            return;
        }
        Path file = goc.resolve(duongDan.substring(TIEN_TO.length())).normalize();
        if (!file.startsWith(goc) || file.equals(goc)) {
            log.warn("Từ chối xóa ngoài thư mục upload: {}", duongDan);
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.warn("Không xóa được ảnh {}: {}", duongDan, e.getMessage());
        }
    }

    private static String nhanDienAnh(MultipartFile file) {
        byte[] dau;
        try (InputStream in = file.getInputStream()) {
            dau = in.readNBytes(12);
        } catch (IOException e) {
            throw new UncheckedIOException("Không đọc được ảnh", e);
        }
        if (batDau(dau, 0xFF, 0xD8, 0xFF)) {
            return "jpg";
        }
        if (batDau(dau, 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A)) {
            return "png";
        }
        if (batDau(dau, 'G', 'I', 'F', '8')) {
            return "gif";
        }
        if (batDau(dau, 'R', 'I', 'F', 'F') && dau.length >= 12
                && Arrays.equals(Arrays.copyOfRange(dau, 8, 12), new byte[]{'W', 'E', 'B', 'P'})) {
            return "webp";
        }
        throw new ApiException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Chỉ chấp nhận ảnh JPG, PNG, WEBP hoặc GIF");
    }

    private static boolean batDau(byte[] dau, int... mau) {
        if (dau.length < mau.length) {
            return false;
        }
        for (int i = 0; i < mau.length; i++) {
            if ((dau[i] & 0xFF) != mau[i]) {
                return false;
            }
        }
        return true;
    }
}
