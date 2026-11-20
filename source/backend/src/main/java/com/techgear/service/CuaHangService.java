package com.techgear.service;

import com.techgear.dto.BannerResponse;
import com.techgear.repository.BannerRepository;
import com.techgear.repository.CaiDatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Thông tin công khai của cửa hàng: banner trang chủ và các cài đặt được phép hiển thị. */
@Service
@RequiredArgsConstructor
public class CuaHangService {

    /**
     * Chỉ các khóa này được trả ra API công khai. Cài đặt mới thêm vào bảng cai_dat
     * mặc định là nội bộ cho tới khi được đưa vào danh sách này.
     */
    static final Set<String> KHOA_CONG_KHAI = Set.of(
            "ten_cua_hang", "hotline", "email_lien_he", "phi_van_chuyen", "mien_phi_van_chuyen_tu");

    private final BannerRepository bannerRepository;
    private final CaiDatRepository caiDatRepository;

    @Transactional(readOnly = true)
    public List<BannerResponse> layBannerDangHienThi() {
        return bannerRepository.findDangHienThi(LocalDateTime.now()).stream()
                .map(BannerResponse::tu)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, String> layCaiDatCongKhai() {
        Map<String, String> ketQua = new LinkedHashMap<>();
        caiDatRepository.findByKhoaIn(KHOA_CONG_KHAI).stream()
                .sorted((a, b) -> a.getKhoa().compareTo(b.getKhoa()))
                .forEach(caiDat -> ketQua.put(caiDat.getKhoa(), caiDat.getGiaTri()));
        return ketQua;
    }
}
