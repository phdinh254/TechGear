-- Nhóm 6 – đánh giá sản phẩm (xem docs/thiet-ke/tu-dien-du-lieu.md)

CREATE TABLE danh_gia (
    id            BIGINT        AUTO_INCREMENT PRIMARY KEY,
    san_pham_id   BIGINT        NOT NULL,
    nguoi_dung_id BIGINT        NOT NULL,
    don_hang_id   BIGINT        NOT NULL,
    so_sao        SMALLINT      NOT NULL CHECK (so_sao BETWEEN 1 AND 5),
    noi_dung      VARCHAR(2000),
    trang_thai    VARCHAR(20)   NOT NULL DEFAULT 'cho_duyet' CHECK (trang_thai IN ('cho_duyet', 'da_duyet', 'da_an')),
    created_at    DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    UNIQUE (nguoi_dung_id, san_pham_id, don_hang_id),
    FOREIGN KEY (san_pham_id) REFERENCES san_pham (id) ON DELETE CASCADE,
    FOREIGN KEY (nguoi_dung_id) REFERENCES nguoi_dung (id) ON DELETE CASCADE,
    FOREIGN KEY (don_hang_id) REFERENCES don_hang (id) ON DELETE CASCADE
);
CREATE INDEX ix_danh_gia_san_pham ON danh_gia (san_pham_id, trang_thai);
