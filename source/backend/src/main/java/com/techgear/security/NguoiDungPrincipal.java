package com.techgear.security;

import com.techgear.entity.NguoiDung;
import com.techgear.entity.enums.TrangThaiTaiKhoan;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Thông tin người dùng đang đăng nhập, lưu trong SecurityContext.
 * Vai trò quan_tri -> ROLE_QUAN_TRI, khach_hang -> ROLE_KHACH_HANG.
 */
@Getter
public class NguoiDungPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final String hoTen;
    private final boolean hoatDong;
    private final List<GrantedAuthority> authorities;

    public NguoiDungPrincipal(NguoiDung nd) {
        this.id = nd.getId();
        this.email = nd.getEmail();
        this.password = nd.getPassword();
        this.hoTen = nd.getHoTen();
        this.hoatDong = nd.getTrangThai() == TrangThaiTaiKhoan.hoat_dong;
        this.authorities = List.of(new SimpleGrantedAuthority(
                "ROLE_" + nd.getVaiTro().name().toUpperCase(Locale.ROOT)));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return hoatDong;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return hoatDong;
    }
}
