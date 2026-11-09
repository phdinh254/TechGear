package com.techgear.service;

import com.techgear.dto.MaGiamGiaResponse;
import com.techgear.repository.MaGiamGiaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaGiamGiaService {

    private final MaGiamGiaRepository maGiamGiaRepository;

    @Transactional(readOnly = true)
    public List<MaGiamGiaResponse> layMaDangApDung() {
        return maGiamGiaRepository.timMaDangApDung(LocalDateTime.now(), PageRequest.of(0, 3))
                .stream().map(MaGiamGiaResponse::tu).toList();
    }
}
