-- Dữ liệu mẫu chỉ cho profile dev (Flyway chạy lại khi file đổi nội dung; câu lệnh không tạo trùng khi chạy lại).
-- Tài khoản: admin@techgear.vn / Admin@123 (quản trị), khach@techgear.vn / Khach@123 (khách hàng)

INSERT INTO nguoi_dung (ho_ten, email, mat_khau, so_dien_thoai, vai_tro)
VALUES ('Quản trị viên', 'admin@techgear.vn', '$2a$12$RIBO5dFY.4A2c9ZDQRN/oe4zLez.luB9urq.0clEoP2NxZz8Y2fWG', '0900000001', 'quan_tri'),
       ('Nguyễn Văn Khách', 'khach@techgear.vn', '$2a$12$JZZeyeYy71xijX5IowHhU.GUbCQe.OvmO9g38yyCeM6PlEGC/3Zp2', '0900000002', 'khach_hang')
ON CONFLICT ((lower(email))) DO NOTHING;

INSERT INTO dia_chi (nguoi_dung_id, ten_nguoi_nhan, so_dien_thoai, tinh_thanh, phuong_xa, dia_chi_chi_tiet, la_mac_dinh)
SELECT n.id, n.ho_ten, n.so_dien_thoai, 'Thành phố Hồ Chí Minh', 'Phường Sài Gòn', '12 Nguyễn Huệ', TRUE
FROM nguoi_dung n
WHERE n.email = 'khach@techgear.vn'
  AND NOT EXISTS (SELECT 1 FROM dia_chi d WHERE d.nguoi_dung_id = n.id);
