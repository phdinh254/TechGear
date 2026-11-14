package com.techgear.service;

import com.techgear.dto.BannerResponse;
import com.techgear.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;

    @Transactional(readOnly = true)
    public List<BannerResponse> layDangHien() {
        return bannerRepository.timDangHien(LocalDateTime.now()).stream().map(BannerResponse::tu).toList();
    }
}
