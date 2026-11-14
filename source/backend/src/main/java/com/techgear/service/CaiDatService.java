package com.techgear.service;

import com.techgear.entity.CaiDat;
import com.techgear.repository.CaiDatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class CaiDatService {

    private final CaiDatRepository caiDatRepository;

    /** Toàn bộ cài đặt cửa hàng dạng khoa → giá trị (bảng cai_dat chỉ chứa thông tin công khai, không lưu bí mật). */
    @Transactional(readOnly = true)
    public Map<String, String> layTatCa() {
        Map<String, String> kq = new TreeMap<>();
        for (CaiDat cd : caiDatRepository.findAll()) {
            kq.put(cd.getKhoa(), cd.getGiaTri());
        }
        return kq;
    }
}
