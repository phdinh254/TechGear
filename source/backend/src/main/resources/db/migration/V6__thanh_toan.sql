-- Nhóm 5 – giao dịch thanh toán (VNPay); một đơn có thể nhiều lần thử (xem docs/thiet-ke/tu-dien-du-lieu.md)

CREATE TABLE thanh_toan (
    id               BIGINT      AUTO_INCREMENT PRIMARY KEY,
    don_hang_id      BIGINT      NOT NULL,
    phuong_thuc      VARCHAR(10) NOT NULL CHECK (phuong_thuc IN ('cod', 'vnpay')),
    so_tien          BIGINT      NOT NULL CHECK (so_tien >= 0),
    ma_tham_chieu    VARCHAR(50) NOT NULL UNIQUE,
    ma_giao_dich     VARCHAR(50) UNIQUE,
    trang_thai       VARCHAR(20) NOT NULL DEFAULT 'cho_xu_ly' CHECK (trang_thai IN ('cho_xu_ly', 'thanh_cong', 'that_bai')),
    du_lieu_phan_hoi JSON,
    thanh_toan_luc   DATETIME(6),
    created_at       DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    FOREIGN KEY (don_hang_id) REFERENCES don_hang (id) ON DELETE CASCADE
);
