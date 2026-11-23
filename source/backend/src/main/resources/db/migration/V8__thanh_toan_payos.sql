-- Chuyển thanh toán trực tuyến VNPay → PayOS (docs/superpowers/specs/2026-11-23-thanh-toan-payos-design.md).
-- CHECK của phương thức thanh toán được khai báo ngay trên cột ở V5/V6 nên MySQL tự đặt tên theo thứ tự CHECK trong bảng:
-- don_hang_chk_4 (phuong_thuc_thanh_toan) và thanh_toan_chk_1 (phuong_thuc) – cố định với mọi CSDL tạo từ V5/V6.
-- Tạo lại với tên rõ ràng để lần sau sửa không phụ thuộc thứ tự.

ALTER TABLE don_hang DROP CHECK don_hang_chk_4;
ALTER TABLE thanh_toan DROP CHECK thanh_toan_chk_1;

UPDATE don_hang SET phuong_thuc_thanh_toan = 'payos' WHERE phuong_thuc_thanh_toan = 'vnpay';
UPDATE thanh_toan SET phuong_thuc = 'payos' WHERE phuong_thuc = 'vnpay';

-- Mã tham chiếu VNPay = mã đơn + 2 số lần thử → orderCode PayOS = id đơn × 100 + lần thử (PayOS bắt buộc là số)
UPDATE thanh_toan tt JOIN don_hang dh ON dh.id = tt.don_hang_id
SET tt.ma_tham_chieu = CAST(dh.id * 100 + CAST(RIGHT(tt.ma_tham_chieu, 2) AS UNSIGNED) AS CHAR)
WHERE tt.ma_tham_chieu NOT REGEXP '^[0-9]+$';

ALTER TABLE don_hang
    ADD CONSTRAINT ck_don_hang_phuong_thuc CHECK (phuong_thuc_thanh_toan IN ('cod', 'payos'));
ALTER TABLE thanh_toan
    ADD CONSTRAINT ck_thanh_toan_phuong_thuc CHECK (phuong_thuc IN ('cod', 'payos'));
