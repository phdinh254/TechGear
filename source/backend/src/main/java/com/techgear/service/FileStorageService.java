package com.techgear.service;

import com.techgear.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

/**
 * Lưu ảnh tải lên vào thư mục app.upload.dir, trả về đường dẫn web dạng /uploads/{thuMuc}/{ten}.
 * Loại file được xác định bằng chữ ký byte đầu file (không tin phần mở rộng hay Content-Type client gửi).
 */
@Slf4j
@Service
public class FileStorageService {

    public static final String TIEN_TO_WEB = "/uploads/";
    private static final Set<String> THU_MUC_HOP_LE =
            Set.of("san-pham", "banner", "thuong-hieu", "danh-muc", "anh-dai-dien");

    private final Path goc;

    public FileStorageService(@Value("${app.upload.dir}") String uploadDir) {
        this.goc = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    /** Lưu ảnh JPG/PNG/WEBP/GIF, trả về đường dẫn web của ảnh. */
    public String luuAnh(MultipartFile file, String thuMuc) {
        if (!THU_MUC_HOP_LE.contains(thuMuc)) {
            throw ApiException.yeuCauKhongHopLe("Thư mục lưu ảnh không hợp lệ");
        }
        if (file == null || file.isEmpty()) {
            throw ApiException.yeuCauKhongHopLe("Vui lòng chọn ảnh");
        }
        String duoi = nhanDangAnh(file);
        if (duoi == null) {
            throw ApiException.yeuCauKhongHopLe("Chỉ chấp nhận ảnh JPG, PNG, WEBP hoặc GIF");
        }
        String ten = UUID.randomUUID().toString().replace("-", "") + "." + duoi;
        Path dich = goc.resolve(thuMuc).resolve(ten).normalize();
        try {
            Files.createDirectories(dich.getParent());
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, dich, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            log.error("Không lưu được ảnh {}", dich, ex);
            throw new IllegalStateException("Không lưu được ảnh", ex);
        }
        return TIEN_TO_WEB + thuMuc + "/" + ten;
    }

    /** Xóa ảnh theo đường dẫn web; bỏ qua nếu không phải ảnh do hệ thống lưu hoặc file không còn. */
    public void xoa(String duongDanWeb) {
        if (duongDanWeb == null || !duongDanWeb.startsWith(TIEN_TO_WEB)) {
            return;
        }
        Path file = goc.resolve(duongDanWeb.substring(TIEN_TO_WEB.length())).normalize();
        if (!file.startsWith(goc)) {
            return; // chặn đường dẫn kiểu ../../ thoát ra ngoài thư mục upload
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException ex) {
            log.warn("Không xóa được ảnh {}", file, ex);
        }
    }

    /**
     * Thay ảnh trong một giao dịch: ảnh cũ chỉ bị xóa khi giao dịch commit thành công,
     * ảnh mới bị xóa nếu giao dịch rollback (tránh mất ảnh cũ hoặc để lại file rác).
     * Gọi ngoài giao dịch thì xóa ảnh cũ ngay.
     */
    public void donDepSauGiaoDich(String anhCu, String anhMoi) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            xoa(anhCu);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_COMMITTED) {
                    xoa(anhCu);
                } else {
                    xoa(anhMoi);
                }
            }
        });
    }

    /** Trả về phần mở rộng theo chữ ký file, hoặc null nếu không phải ảnh được hỗ trợ. */
    private static String nhanDangAnh(MultipartFile file) {
        byte[] dau = new byte[12];
        int doc;
        try (InputStream in = file.getInputStream()) {
            doc = in.readNBytes(dau, 0, dau.length);
        } catch (IOException ex) {
            return null;
        }
        if (doc >= 3 && (dau[0] & 0xFF) == 0xFF && (dau[1] & 0xFF) == 0xD8 && (dau[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (doc >= 8 && Arrays.equals(Arrays.copyOf(dau, 8),
                new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'})) {
            return "png";
        }
        if (doc >= 6 && dau[0] == 'G' && dau[1] == 'I' && dau[2] == 'F' && dau[3] == '8') {
            return "gif";
        }
        if (doc >= 12 && dau[0] == 'R' && dau[1] == 'I' && dau[2] == 'F' && dau[3] == 'F'
                && dau[8] == 'W' && dau[9] == 'E' && dau[10] == 'B' && dau[11] == 'P') {
            return "webp";
        }
        return null;
    }
}
