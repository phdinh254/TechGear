-- Nhóm 1 – Tài khoản: người dùng, mã đặt lại mật khẩu, sổ địa chỉ (xem docs/thiet-ke/tu-dien-du-lieu.md)

CREATE TABLE nguoi_dung (
    id               BIGINT       AUTO_INCREMENT PRIMARY KEY,
    ho_ten           VARCHAR(100) NOT NULL,
    -- collation _ci: email không phân biệt hoa thường
    email            VARCHAR(150) NOT NULL UNIQUE,
    mat_khau         VARCHAR(100) NOT NULL,
    so_dien_thoai    VARCHAR(20),
    gioi_tinh        VARCHAR(10)  CHECK (gioi_tinh IN ('nam', 'nu', 'khac')),
    ngay_sinh        DATE,
    anh_dai_dien     VARCHAR(255),
    vai_tro          VARCHAR(20)  NOT NULL DEFAULT 'khach_hang' CHECK (vai_tro IN ('quan_tri', 'khach_hang')),
    trang_thai       VARCHAR(20)  NOT NULL DEFAULT 'hoat_dong' CHECK (trang_thai IN ('hoat_dong', 'bi_khoa')),
    doi_mat_khau_luc DATETIME(6),
    created_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);

CREATE TABLE ma_dat_lai_mat_khau (
    id            BIGINT      AUTO_INCREMENT PRIMARY KEY,
    nguoi_dung_id BIGINT      NOT NULL,
    -- mã băm so khớp chính xác từng ký tự
    ma_bam        CHAR(64)    CHARACTER SET ascii COLLATE ascii_bin NOT NULL UNIQUE,
    het_han_luc   DATETIME(6) NOT NULL,
    da_su_dung    BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at    DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    FOREIGN KEY (nguoi_dung_id) REFERENCES nguoi_dung (id) ON DELETE CASCADE
);

CREATE TABLE dia_chi (
    id               BIGINT       AUTO_INCREMENT PRIMARY KEY,
    nguoi_dung_id    BIGINT       NOT NULL,
    ten_nguoi_nhan   VARCHAR(100) NOT NULL,
    so_dien_thoai    VARCHAR(20)  NOT NULL,
    tinh_thanh       VARCHAR(100) NOT NULL,
    phuong_xa        VARCHAR(100) NOT NULL,
    dia_chi_chi_tiet VARCHAR(255) NOT NULL,
    la_mac_dinh      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    FOREIGN KEY (nguoi_dung_id) REFERENCES nguoi_dung (id) ON DELETE CASCADE
);
-- Mỗi người tối đa một địa chỉ mặc định (dòng không mặc định cho NULL nên không đụng nhau)
CREATE UNIQUE INDEX uq_dia_chi_mac_dinh ON dia_chi ((IF(la_mac_dinh, nguoi_dung_id, NULL)));
