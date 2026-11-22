-- Nhóm 3 – mã giảm giá; Nhóm 5 – đơn hàng, lịch sử đơn, lịch sử tồn kho (xem docs/thiet-ke/tu-dien-du-lieu.md)
-- Bảng thanh_toan tạo ở V6 (Phase 7 – VNPay).

CREATE TABLE ma_giam_gia (
    id                  BIGINT       AUTO_INCREMENT PRIMARY KEY,
    ma                  VARCHAR(50)  NOT NULL,
    mo_ta               VARCHAR(255),
    loai_giam           VARCHAR(20)  NOT NULL CHECK (loai_giam IN ('phan_tram', 'so_tien')),
    gia_tri_giam        BIGINT       NOT NULL CHECK (gia_tri_giam > 0),
    don_toi_thieu       BIGINT       NOT NULL DEFAULT 0 CHECK (don_toi_thieu >= 0),
    giam_toi_da         BIGINT       CHECK (giam_toi_da > 0),
    tong_luot_dung      INT          CHECK (tong_luot_dung > 0),
    luot_dung_moi_nguoi INT          CHECK (luot_dung_moi_nguoi > 0),
    da_dung             INT          NOT NULL DEFAULT 0 CHECK (da_dung >= 0),
    bat_dau_luc         DATETIME(6)  NOT NULL,
    ket_thuc_luc        DATETIME(6)  NOT NULL,
    kich_hoat           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at          DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    deleted_at          DATETIME(6),
    CHECK (loai_giam <> 'phan_tram' OR gia_tri_giam <= 100),
    CHECK (tong_luot_dung IS NULL OR da_dung <= tong_luot_dung),
    CHECK (ket_thuc_luc > bat_dau_luc)
);
-- Khách nhập mã không phân biệt hoa thường (collation _ci); chỉ duy nhất giữa các mã chưa xóa mềm
CREATE UNIQUE INDEX uq_ma_giam_gia_ma ON ma_giam_gia ((IF(deleted_at IS NULL, ma, NULL)));

CREATE TABLE don_hang (
    id                     BIGINT       AUTO_INCREMENT PRIMARY KEY,
    ma_don_hang            VARCHAR(20)  NOT NULL UNIQUE,
    nguoi_dung_id          BIGINT,
    ma_giam_gia_id         BIGINT,
    ten_nguoi_nhan         VARCHAR(100) NOT NULL,
    email                  VARCHAR(150) NOT NULL,
    so_dien_thoai          VARCHAR(20)  NOT NULL,
    dia_chi_giao_hang      VARCHAR(500) NOT NULL,
    ghi_chu                VARCHAR(500),
    ghi_chu_quan_tri       VARCHAR(500),
    tam_tinh               BIGINT       NOT NULL CHECK (tam_tinh >= 0),
    tien_giam_gia          BIGINT       NOT NULL DEFAULT 0 CHECK (tien_giam_gia >= 0),
    phi_van_chuyen         BIGINT       NOT NULL DEFAULT 0 CHECK (phi_van_chuyen >= 0),
    tong_tien              BIGINT       NOT NULL,
    phuong_thuc_thanh_toan VARCHAR(10)  NOT NULL CHECK (phuong_thuc_thanh_toan IN ('cod', 'vnpay')),
    trang_thai_thanh_toan  VARCHAR(20)  NOT NULL DEFAULT 'chua_thanh_toan'
        CHECK (trang_thai_thanh_toan IN ('chua_thanh_toan', 'da_thanh_toan', 'da_hoan_tien')),
    trang_thai             VARCHAR(20)  NOT NULL DEFAULT 'cho_xac_nhan'
        CHECK (trang_thai IN ('cho_xac_nhan', 'da_xac_nhan', 'dang_giao', 'hoan_thanh', 'da_huy')),
    created_at             DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at             DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CHECK (tien_giam_gia <= tam_tinh),
    CHECK (tong_tien = tam_tinh - tien_giam_gia + phi_van_chuyen),
    FOREIGN KEY (nguoi_dung_id) REFERENCES nguoi_dung (id) ON DELETE SET NULL,
    FOREIGN KEY (ma_giam_gia_id) REFERENCES ma_giam_gia (id)
);
CREATE INDEX ix_don_hang_trang_thai ON don_hang (trang_thai, created_at);
CREATE INDEX ix_don_hang_email ON don_hang (email);

CREATE TABLE chi_tiet_don_hang (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    don_hang_id  BIGINT       NOT NULL,
    san_pham_id  BIGINT,
    bien_the_id  BIGINT,
    ten_san_pham VARCHAR(255) NOT NULL,
    sku          VARCHAR(100) NOT NULL,
    phan_loai    VARCHAR(150),
    hinh_anh     VARCHAR(255),
    don_gia      BIGINT       NOT NULL CHECK (don_gia >= 0),
    so_luong     INT          NOT NULL CHECK (so_luong > 0),
    thanh_tien   BIGINT       NOT NULL,
    CHECK (thanh_tien = don_gia * so_luong),
    FOREIGN KEY (don_hang_id) REFERENCES don_hang (id) ON DELETE CASCADE,
    FOREIGN KEY (san_pham_id) REFERENCES san_pham (id) ON DELETE SET NULL,
    FOREIGN KEY (bien_the_id) REFERENCES bien_the (id) ON DELETE SET NULL
);

CREATE TABLE lich_su_don_hang (
    id                 BIGINT       AUTO_INCREMENT PRIMARY KEY,
    don_hang_id        BIGINT       NOT NULL,
    trang_thai         VARCHAR(20)  NOT NULL
        CHECK (trang_thai IN ('cho_xac_nhan', 'da_xac_nhan', 'dang_giao', 'hoan_thanh', 'da_huy')),
    ghi_chu            VARCHAR(500),
    nguoi_thuc_hien_id BIGINT,
    created_at         DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    FOREIGN KEY (don_hang_id) REFERENCES don_hang (id) ON DELETE CASCADE,
    FOREIGN KEY (nguoi_thuc_hien_id) REFERENCES nguoi_dung (id) ON DELETE SET NULL
);

CREATE TABLE lich_su_ton_kho (
    id                 BIGINT       AUTO_INCREMENT PRIMARY KEY,
    bien_the_id        BIGINT       NOT NULL,
    don_hang_id        BIGINT,
    loai               VARCHAR(20)  NOT NULL CHECK (loai IN ('khoi_tao', 'nhap_kho', 'ban_hang', 'huy_don', 'dieu_chinh')),
    so_luong_thay_doi  INT          NOT NULL CHECK (so_luong_thay_doi <> 0),
    so_luong_sau       INT          NOT NULL CHECK (so_luong_sau >= 0),
    ghi_chu            VARCHAR(255),
    nguoi_thuc_hien_id BIGINT,
    created_at         DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    FOREIGN KEY (bien_the_id) REFERENCES bien_the (id),
    FOREIGN KEY (don_hang_id) REFERENCES don_hang (id) ON DELETE SET NULL,
    FOREIGN KEY (nguoi_thuc_hien_id) REFERENCES nguoi_dung (id) ON DELETE SET NULL
);
CREATE INDEX ix_lich_su_ton_kho_bien_the ON lich_su_ton_kho (bien_the_id, created_at);
