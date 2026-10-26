package com.techgear.controller;

import com.techgear.dto.DiaChiRequest;
import com.techgear.dto.DiaChiResponse;
import com.techgear.security.NguoiDungPrincipal;
import com.techgear.service.DiaChiService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Tài khoản - Sổ địa chỉ")
@RestController
@RequestMapping("/api/tai-khoan/dia-chi")
@RequiredArgsConstructor
public class DiaChiController {

    private final DiaChiService diaChiService;

    @GetMapping
    public List<DiaChiResponse> layDanhSach(@AuthenticationPrincipal NguoiDungPrincipal nd) {
        return diaChiService.layDanhSach(nd.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DiaChiResponse tao(@AuthenticationPrincipal NguoiDungPrincipal nd, @Valid @RequestBody DiaChiRequest req) {
        return diaChiService.tao(nd.getId(), req);
    }

    @PutMapping("/{id}")
    public DiaChiResponse capNhat(@AuthenticationPrincipal NguoiDungPrincipal nd, @PathVariable Long id,
                                  @Valid @RequestBody DiaChiRequest req) {
        return diaChiService.capNhat(nd.getId(), id, req);
    }

    @PatchMapping("/{id}/mac-dinh")
    public DiaChiResponse datMacDinh(@AuthenticationPrincipal NguoiDungPrincipal nd, @PathVariable Long id) {
        return diaChiService.datMacDinh(nd.getId(), id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void xoa(@AuthenticationPrincipal NguoiDungPrincipal nd, @PathVariable Long id) {
        diaChiService.xoa(nd.getId(), id);
    }
}
