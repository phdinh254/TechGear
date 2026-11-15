-- Nhóm 4 – Giỏ hàng, yêu thích (xem docs/thiet-ke/tu-dien-du-lieu.md)

CREATE TABLE gio_hang (
    nguoi_dung_id BIGINT      NOT NULL REFERENCES nguoi_dung (id) ON DELETE CASCADE,
    bien_the_id   BIGINT      NOT NULL REFERENCES bien_the (id) ON DELETE CASCADE,
    so_luong      INTEGER     NOT NULL CHECK (so_luong > 0),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (nguoi_dung_id, bien_the_id)
);
CREATE INDEX ix_gio_hang_bien_the ON gio_hang (bien_the_id);

CREATE TABLE yeu_thich (
    nguoi_dung_id BIGINT      NOT NULL REFERENCES nguoi_dung (id) ON DELETE CASCADE,
    san_pham_id   BIGINT      NOT NULL REFERENCES san_pham (id) ON DELETE CASCADE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (nguoi_dung_id, san_pham_id)
);
CREATE INDEX ix_yeu_thich_san_pham ON yeu_thich (san_pham_id);
