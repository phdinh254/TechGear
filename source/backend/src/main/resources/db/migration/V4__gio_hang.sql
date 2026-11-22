-- Nhóm 4 – Giỏ hàng, yêu thích (xem docs/thiet-ke/tu-dien-du-lieu.md)

CREATE TABLE gio_hang (
    nguoi_dung_id BIGINT      NOT NULL,
    bien_the_id   BIGINT      NOT NULL,
    so_luong      INT         NOT NULL CHECK (so_luong > 0),
    created_at    DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (nguoi_dung_id, bien_the_id),
    FOREIGN KEY (nguoi_dung_id) REFERENCES nguoi_dung (id) ON DELETE CASCADE,
    FOREIGN KEY (bien_the_id) REFERENCES bien_the (id) ON DELETE CASCADE
);

CREATE TABLE yeu_thich (
    nguoi_dung_id BIGINT      NOT NULL,
    san_pham_id   BIGINT      NOT NULL,
    created_at    DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (nguoi_dung_id, san_pham_id),
    FOREIGN KEY (nguoi_dung_id) REFERENCES nguoi_dung (id) ON DELETE CASCADE,
    FOREIGN KEY (san_pham_id) REFERENCES san_pham (id) ON DELETE CASCADE
);
