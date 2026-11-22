-- Nhóm 2 – Danh mục, thương hiệu, sản phẩm; Nhóm 3 – banner, cài đặt (xem docs/thiet-ke/tu-dien-du-lieu.md)
-- InnoDB tự tạo chỉ mục cho cột khóa ngoại.
-- Slug/SKU chỉ duy nhất giữa các dòng chưa xóa mềm: chỉ mục duy nhất trên biểu thức, dòng đã xóa cho NULL.

CREATE TABLE danh_muc (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    danh_muc_cha_id BIGINT,
    ten             VARCHAR(100) NOT NULL,
    slug            VARCHAR(120) NOT NULL,
    mo_ta           TEXT,
    hinh_anh        VARCHAR(255),
    thu_tu          INT          NOT NULL DEFAULT 0,
    hien_thi        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    deleted_at      DATETIME(6),
    FOREIGN KEY (danh_muc_cha_id) REFERENCES danh_muc (id)
);
CREATE UNIQUE INDEX uq_danh_muc_slug ON danh_muc ((IF(deleted_at IS NULL, slug, NULL)));

CREATE TABLE thuong_hieu (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    ten        VARCHAR(100) NOT NULL,
    slug       VARCHAR(120) NOT NULL,
    logo       VARCHAR(255),
    mo_ta      TEXT,
    hien_thi   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    deleted_at DATETIME(6)
);
CREATE UNIQUE INDEX uq_thuong_hieu_slug ON thuong_hieu ((IF(deleted_at IS NULL, slug, NULL)));

CREATE TABLE san_pham (
    id             BIGINT       AUTO_INCREMENT PRIMARY KEY,
    danh_muc_id    BIGINT       NOT NULL,
    thuong_hieu_id BIGINT,
    ten            VARCHAR(255) NOT NULL,
    slug           VARCHAR(255) NOT NULL,
    mo_ta_ngan     VARCHAR(500),
    mo_ta          TEXT,
    gia_ban        BIGINT       NOT NULL DEFAULT 0 CHECK (gia_ban >= 0),
    gia_goc        BIGINT       CHECK (gia_goc >= 0),
    bao_hanh_thang INT          NOT NULL DEFAULT 0 CHECK (bao_hanh_thang >= 0),
    noi_bat        BOOLEAN      NOT NULL DEFAULT FALSE,
    hien_thi       BOOLEAN      NOT NULL DEFAULT TRUE,
    luot_xem       INT          NOT NULL DEFAULT 0 CHECK (luot_xem >= 0),
    created_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    deleted_at     DATETIME(6),
    FOREIGN KEY (danh_muc_id) REFERENCES danh_muc (id),
    FOREIGN KEY (thuong_hieu_id) REFERENCES thuong_hieu (id)
);
CREATE UNIQUE INDEX uq_san_pham_slug ON san_pham ((IF(deleted_at IS NULL, slug, NULL)));
CREATE INDEX ix_san_pham_gia_ban ON san_pham (gia_ban);

CREATE TABLE thong_so (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    san_pham_id BIGINT       NOT NULL,
    ten         VARCHAR(100) NOT NULL,
    gia_tri     VARCHAR(255) NOT NULL,
    thu_tu      INT          NOT NULL DEFAULT 0,
    FOREIGN KEY (san_pham_id) REFERENCES san_pham (id) ON DELETE CASCADE
);

CREATE TABLE tuy_chon (
    id          BIGINT      AUTO_INCREMENT PRIMARY KEY,
    san_pham_id BIGINT      NOT NULL,
    ten         VARCHAR(50) NOT NULL,
    loai        VARCHAR(20) NOT NULL CHECK (loai IN ('mau_sac', 'dung_luong', 'phien_ban', 'khac')),
    thu_tu      INT         NOT NULL DEFAULT 0,
    UNIQUE (san_pham_id, ten),
    FOREIGN KEY (san_pham_id) REFERENCES san_pham (id) ON DELETE CASCADE
);

CREATE TABLE gia_tri_tuy_chon (
    id          BIGINT      AUTO_INCREMENT PRIMARY KEY,
    tuy_chon_id BIGINT      NOT NULL,
    gia_tri     VARCHAR(50) NOT NULL,
    ma_mau      CHAR(7)     CHECK (REGEXP_LIKE(ma_mau, '^#[0-9A-Fa-f]{6}$')),
    thu_tu      INT         NOT NULL DEFAULT 0,
    UNIQUE (tuy_chon_id, gia_tri),
    FOREIGN KEY (tuy_chon_id) REFERENCES tuy_chon (id) ON DELETE CASCADE
);

CREATE TABLE bien_the (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    san_pham_id     BIGINT       NOT NULL,
    sku             VARCHAR(100) NOT NULL,
    gia_ban         BIGINT       NOT NULL CHECK (gia_ban >= 0),
    gia_goc         BIGINT,
    so_luong_ton    INT          NOT NULL DEFAULT 0 CHECK (so_luong_ton >= 0),
    nguong_canh_bao INT          NOT NULL DEFAULT 5 CHECK (nguong_canh_bao >= 0),
    hien_thi        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    deleted_at      DATETIME(6),
    CHECK (gia_goc IS NULL OR gia_goc >= gia_ban),
    FOREIGN KEY (san_pham_id) REFERENCES san_pham (id)
);
CREATE UNIQUE INDEX uq_bien_the_sku ON bien_the ((IF(deleted_at IS NULL, sku, NULL)));

CREATE TABLE bien_the_gia_tri (
    bien_the_id         BIGINT NOT NULL,
    gia_tri_tuy_chon_id BIGINT NOT NULL,
    PRIMARY KEY (bien_the_id, gia_tri_tuy_chon_id),
    FOREIGN KEY (bien_the_id) REFERENCES bien_the (id) ON DELETE CASCADE,
    FOREIGN KEY (gia_tri_tuy_chon_id) REFERENCES gia_tri_tuy_chon (id) ON DELETE CASCADE
);

CREATE TABLE hinh_anh_san_pham (
    id                  BIGINT       AUTO_INCREMENT PRIMARY KEY,
    san_pham_id         BIGINT       NOT NULL,
    gia_tri_tuy_chon_id BIGINT,
    duong_dan           VARCHAR(255) NOT NULL,
    mo_ta               VARCHAR(255),
    thu_tu              INT          NOT NULL DEFAULT 0,
    la_anh_chinh        BOOLEAN      NOT NULL DEFAULT FALSE,
    FOREIGN KEY (san_pham_id) REFERENCES san_pham (id) ON DELETE CASCADE,
    FOREIGN KEY (gia_tri_tuy_chon_id) REFERENCES gia_tri_tuy_chon (id) ON DELETE SET NULL
);
-- Tối đa một ảnh chính mỗi sản phẩm
CREATE UNIQUE INDEX uq_hinh_anh_chinh ON hinh_anh_san_pham ((IF(la_anh_chinh, san_pham_id, NULL)));

CREATE TABLE banner (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    tieu_de      VARCHAR(255),
    hinh_anh     VARCHAR(255) NOT NULL,
    lien_ket     VARCHAR(255),
    thu_tu       INT          NOT NULL DEFAULT 0,
    hien_thi     BOOLEAN      NOT NULL DEFAULT TRUE,
    bat_dau_luc  DATETIME(6),
    ket_thuc_luc DATETIME(6),
    created_at   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at   DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CHECK (bat_dau_luc IS NULL OR ket_thuc_luc IS NULL OR ket_thuc_luc > bat_dau_luc)
);

-- Cấu hình công khai (API /cai-dat trả nguyên bảng): KHÔNG lưu bí mật ở đây
CREATE TABLE cai_dat (
    khoa       VARCHAR(100) PRIMARY KEY,
    gia_tri    TEXT         NOT NULL,
    mo_ta      VARCHAR(255),
    updated_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);
INSERT INTO cai_dat (khoa, gia_tri, mo_ta)
VALUES ('ten_cua_hang', 'TechGear', 'Tên cửa hàng'),
       ('hotline', '1900 1234', 'Số điện thoại hỗ trợ'),
       ('email_lien_he', 'hotro@techgear.vn', 'Email liên hệ'),
       ('phi_van_chuyen', '30000', 'Phí vận chuyển mỗi đơn (đồng)'),
       ('mien_phi_van_chuyen_tu', '500000', 'Đơn từ mức này (đồng) được miễn phí vận chuyển');
