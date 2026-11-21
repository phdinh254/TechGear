-- Mã giảm giá mẫu – chỉ profile dev (Flyway chạy lại khi file đổi nội dung; câu lệnh không tạo trùng).
-- GIAM10: 10% tối đa 100.000đ, đơn từ 200.000đ · GIAM50K: 50.000đ, đơn từ 500.000đ
-- MOILAN1: 20.000đ, mỗi khách 1 lượt · HETHAN: đã hết hạn

INSERT INTO ma_giam_gia (ma, mo_ta, loai_giam, gia_tri_giam, don_toi_thieu, giam_toi_da, luot_dung_moi_nguoi,
                         bat_dau_luc, ket_thuc_luc)
-- Khung giờ tính theo lúc nạp (đơn mẫu đặt trong 90 ngày qua phải áp được mã; dữ liệu mẫu không hết hạn theo năm)
VALUES ('GIAM10', 'Giảm 10% tối đa 100.000đ cho đơn từ 200.000đ', 'phan_tram', 10, 200000, 100000, NULL,
        now() - interval '1 year', now() + interval '5 years'),
       ('GIAM50K', 'Giảm 50.000đ cho đơn từ 500.000đ', 'so_tien', 50000, 500000, NULL, NULL,
        now() - interval '1 year', now() + interval '5 years'),
       ('MOILAN1', 'Giảm 20.000đ, mỗi khách dùng 1 lần', 'so_tien', 20000, 0, NULL, 1,
        now() - interval '1 year', now() + interval '5 years'),
       ('HETHAN', 'Mã đã hết hạn (để thử)', 'so_tien', 10000, 0, NULL, NULL,
        now() - interval '2 years', now() - interval '1 year')
ON CONFLICT ((upper(ma))) WHERE deleted_at IS NULL DO NOTHING;
