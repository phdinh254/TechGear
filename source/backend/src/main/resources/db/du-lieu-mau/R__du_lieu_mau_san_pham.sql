-- Dữ liệu mẫu danh mục, thương hiệu, sản phẩm – chỉ profile dev, chỉ nạp khi chưa có sản phẩm nào
-- (Flyway chạy lại file khi đổi nội dung; muốn nạp lại từ đầu: docker compose down -v).
-- Ảnh là link Wikimedia Commons (cần internet khi xem); nguồn, giấy phép, tác giả: docs/nguon-anh-mau.md.

-- Thủ tục tạm: cần rẽ nhánh (bỏ qua khi đã có sản phẩm), gọi xong thì xóa.
DROP PROCEDURE IF EXISTS nap_du_lieu_mau_san_pham;
DELIMITER //
CREATE PROCEDURE nap_du_lieu_mau_san_pham()
nap: BEGIN
-- CSDL đã có sản phẩm: bỏ qua. Muốn nạp lại dữ liệu mẫu đầy đủ: docker compose down -v
IF EXISTS (SELECT 1 FROM san_pham) THEN
    LEAVE nap;
END IF;

INSERT INTO danh_muc (ten, slug, thu_tu)
VALUES ('Sạc & cáp', 'sac-cap', 1),
       ('Âm thanh', 'am-thanh', 2),
       ('Phụ kiện máy tính', 'phu-kien-may-tinh', 3),
       ('Ốp lưng & bảo vệ', 'op-lung-bao-ve', 4)
ON DUPLICATE KEY UPDATE danh_muc.id = danh_muc.id;

INSERT INTO danh_muc (danh_muc_cha_id, ten, slug, thu_tu)
SELECT cha.id, v.ten, v.slug, v.thu_tu
FROM (VALUES ROW('sac-cap', 'Củ sạc', 'cu-sac', 1),
             ROW('sac-cap', 'Cáp sạc', 'cap-sac', 2),
             ROW('sac-cap', 'Sạc dự phòng', 'sac-du-phong', 3),
             ROW('am-thanh', 'Tai nghe', 'tai-nghe', 1),
             ROW('am-thanh', 'Loa bluetooth', 'loa', 2),
             ROW('phu-kien-may-tinh', 'Chuột', 'chuot', 1),
             ROW('phu-kien-may-tinh', 'Bàn phím', 'ban-phim', 2),
             ROW('op-lung-bao-ve', 'Ốp lưng', 'op-lung', 1),
             ROW('op-lung-bao-ve', 'Kính cường lực', 'cuong-luc', 2)) v(cha, ten, slug, thu_tu)
JOIN danh_muc cha ON cha.slug = v.cha AND cha.deleted_at IS NULL
ON DUPLICATE KEY UPDATE danh_muc.id = danh_muc.id;

INSERT INTO thuong_hieu (ten, slug)
VALUES ('Anker', 'anker'), ('Apple', 'apple'), ('Baseus', 'baseus'), ('JBL', 'jbl'), ('Logitech', 'logitech'),
       ('Samsung', 'samsung'), ('Sony', 'sony'), ('Ugreen', 'ugreen'), ('Xiaomi', 'xiaomi')
ON DUPLICATE KEY UPDATE thuong_hieu.id = thuong_hieu.id;

-- ngay = tạo cách đây bao nhiêu ngày (để sắp xếp "mới nhất" có thứ tự)
INSERT INTO san_pham (danh_muc_id, thuong_hieu_id, ten, slug, mo_ta_ngan, mo_ta, bao_hanh_thang, noi_bat, luot_xem, created_at)
SELECT dm.id, th.id, v.ten, v.slug, v.mo_ta_ngan, v.mo_ta, v.bh, v.noi_bat, v.luot_xem, now() - INTERVAL v.ngay DAY
FROM (VALUES
    ROW('cu-sac', 'anker', 'Củ sạc Anker Nano 65W GaN', 'cu-sac-anker-nano-65w',
     'Sạc nhanh 65W cho laptop, điện thoại; nhỏ gọn nhờ công nghệ GaN.',
     '<p>Củ sạc 1 cổng USB-C 65W, hỗ trợ PD 3.0 và PPS.</p>', 18, TRUE, 320, 30),
    ROW('cu-sac', 'apple', 'Củ sạc Apple USB-C 20W', 'cu-sac-apple-usb-c-20w',
     'Củ sạc chính hãng Apple, sạc nhanh iPhone.', '<p>Sạc nhanh iPhone 50% trong khoảng 30 phút.</p>', 12, FALSE, 210, 28),
    ROW('cu-sac', 'baseus', 'Củ sạc Baseus GaN5 Pro 100W', 'cu-sac-baseus-gan5-pro-100w',
     '3 cổng (2 USB-C, 1 USB-A), tổng công suất 100W.', '<p>Sạc cùng lúc laptop, máy tính bảng và điện thoại.</p>', 12, TRUE, 150, 20),
    ROW('cap-sac', 'ugreen', 'Cáp USB-C to USB-C Ugreen 100W', 'cap-usb-c-ugreen-100w',
     'Cáp bện dù, hỗ trợ sạc nhanh 100W.', '<p>Chip E-Marker, truyền dữ liệu 480Mbps.</p>', 18, TRUE, 280, 25),
    ROW('cap-sac', 'anker', 'Cáp Lightning Anker PowerLine III', 'cap-lightning-anker-powerline-iii',
     'Chuẩn MFi, độ bền 25.000 lần gập.', '<p>Cáp USB-C to Lightning dài 1m.</p>', 24, FALSE, 90, 40),
    ROW('sac-du-phong', 'anker', 'Sạc dự phòng Anker PowerCore 10000mAh', 'sac-du-phong-anker-powercore-10000',
     'Nhỏ gọn, sạc nhanh 20W.', '<p>Dung lượng 10.000mAh, 1 cổng USB-C vào/ra và 1 cổng USB-A.</p>', 18, TRUE, 260, 18),
    ROW('sac-du-phong', 'xiaomi', 'Sạc dự phòng Xiaomi 20000mAh 22.5W', 'sac-du-phong-xiaomi-20000',
     'Dung lượng lớn, sạc nhanh 22.5W.', '<p>3 cổng ra, sạc được 3 thiết bị cùng lúc.</p>', 6, FALSE, 120, 35),
    ROW('tai-nghe', 'sony', 'Tai nghe Sony WH-1000XM5', 'tai-nghe-sony-wh-1000xm5',
     'Chống ồn chủ động hàng đầu, pin 30 giờ.', '<p>8 micro chống ồn, kết nối Bluetooth 5.2 đa điểm.</p>', 12, TRUE, 540, 10),
    ROW('tai-nghe', 'apple', 'Tai nghe Apple AirPods Pro 2', 'tai-nghe-apple-airpods-pro-2',
     'Chống ồn chủ động, hộp sạc USB-C.', '<p>Chip H2, âm thanh không gian cá nhân hóa.</p>', 12, TRUE, 610, 12),
    ROW('tai-nghe', 'samsung', 'Tai nghe Samsung Galaxy Buds3', 'tai-nghe-samsung-galaxy-buds3',
     'Thiết kế mở, âm thanh AKG.', '<p>Chống ồn chủ động, pin 30 giờ cùng hộp sạc.</p>', 12, FALSE, 180, 8),
    ROW('loa', 'jbl', 'Loa JBL Flip 6', 'loa-jbl-flip-6',
     'Chống nước IP67, pin 12 giờ.', '<p>Âm thanh JBL Original Pro Sound, kết nối PartyBoost.</p>', 12, TRUE, 390, 15),
    ROW('loa', 'sony', 'Loa Sony SRS-XB13', 'loa-sony-srs-xb13',
     'Nhỏ gọn, chống nước IP67.', '<p>Extra Bass, pin 16 giờ.</p>', 12, FALSE, 75, 45),
    ROW('chuot', 'logitech', 'Chuột Logitech MX Master 3S', 'chuot-logitech-mx-master-3s',
     'Click yên tĩnh, cảm biến 8000 DPI.', '<p>Cuộn MagSpeed, kết nối 3 thiết bị.</p>', 12, TRUE, 430, 14),
    ROW('chuot', 'logitech', 'Chuột gaming Logitech G304', 'chuot-logitech-g304',
     'Không dây Lightspeed, pin 250 giờ.', '<p>Cảm biến HERO 12K, nặng 99g.</p>', 24, FALSE, 260, 50),
    ROW('ban-phim', 'logitech', 'Bàn phím cơ Logitech MX Mechanical Mini', 'ban-phim-logitech-mx-mechanical-mini',
     'Bàn phím cơ không dây, đèn nền thông minh.', '<p>Bố cục 75%, kết nối Bluetooth và Logi Bolt.</p>', 12, FALSE, 140, 6),
    ROW('op-lung', 'ugreen', 'Ốp lưng trong suốt Ugreen cho iPhone 16 Pro', 'op-lung-ugreen-iphone-16-pro',
     'Chống ố vàng, viền chống sốc.', '<p>Hỗ trợ sạc MagSafe.</p>', 0, FALSE, 60, 3),
    ROW('cuong-luc', 'baseus', 'Kính cường lực Baseus iPhone 16', 'kinh-cuong-luc-baseus-iphone-16',
     'Độ cứng 9H, kèm khung dán.', '<p>Phủ lớp chống bám vân tay.</p>', 0, FALSE, 40, 2)
) v(dm, th, ten, slug, mo_ta_ngan, mo_ta, bh, noi_bat, luot_xem, ngay)
JOIN danh_muc dm ON dm.slug = v.dm AND dm.deleted_at IS NULL
LEFT JOIN thuong_hieu th ON th.slug = v.th AND th.deleted_at IS NULL;

INSERT INTO thong_so (san_pham_id, ten, gia_tri, thu_tu)
SELECT sp.id, v.ten, v.gia_tri, v.thu_tu
FROM (VALUES
    ROW('cu-sac-anker-nano-65w', 'Công suất', '65W', 1), ROW('cu-sac-anker-nano-65w', 'Cổng', '1 x USB-C', 2),
    ROW('cu-sac-apple-usb-c-20w', 'Công suất', '20W', 1), ROW('cu-sac-apple-usb-c-20w', 'Cổng', '1 x USB-C', 2),
    ROW('cu-sac-baseus-gan5-pro-100w', 'Công suất', '100W', 1), ROW('cu-sac-baseus-gan5-pro-100w', 'Cổng', '2 x USB-C, 1 x USB-A', 2),
    ROW('cap-usb-c-ugreen-100w', 'Công suất', '100W', 1), ROW('cap-usb-c-ugreen-100w', 'Chất liệu', 'Bện dù', 2),
    ROW('cap-lightning-anker-powerline-iii', 'Chiều dài', '1m', 1), ROW('cap-lightning-anker-powerline-iii', 'Chuẩn', 'MFi', 2),
    ROW('sac-du-phong-anker-powercore-10000', 'Dung lượng', '10.000mAh', 1), ROW('sac-du-phong-anker-powercore-10000', 'Công suất', '20W', 2),
    ROW('sac-du-phong-xiaomi-20000', 'Dung lượng', '20.000mAh', 1), ROW('sac-du-phong-xiaomi-20000', 'Công suất', '22.5W', 2),
    ROW('tai-nghe-sony-wh-1000xm5', 'Kết nối', 'Bluetooth 5.2', 1), ROW('tai-nghe-sony-wh-1000xm5', 'Thời lượng pin', '30 giờ', 2),
    ROW('tai-nghe-apple-airpods-pro-2', 'Chip', 'Apple H2', 1), ROW('tai-nghe-apple-airpods-pro-2', 'Thời lượng pin', '6 giờ (30 giờ kèm hộp)', 2),
    ROW('tai-nghe-samsung-galaxy-buds3', 'Kết nối', 'Bluetooth 5.4', 1), ROW('tai-nghe-samsung-galaxy-buds3', 'Thời lượng pin', '30 giờ kèm hộp', 2),
    ROW('loa-jbl-flip-6', 'Công suất', '30W', 1), ROW('loa-jbl-flip-6', 'Chống nước', 'IP67', 2),
    ROW('loa-sony-srs-xb13', 'Thời lượng pin', '16 giờ', 1), ROW('loa-sony-srs-xb13', 'Chống nước', 'IP67', 2),
    ROW('chuot-logitech-mx-master-3s', 'DPI', '8000', 1), ROW('chuot-logitech-mx-master-3s', 'Kết nối', 'Bluetooth, Logi Bolt', 2),
    ROW('chuot-logitech-g304', 'DPI', '12000', 1), ROW('chuot-logitech-g304', 'Trọng lượng', '99g', 2),
    ROW('ban-phim-logitech-mx-mechanical-mini', 'Bố cục', '75%', 1), ROW('ban-phim-logitech-mx-mechanical-mini', 'Thời lượng pin', '15 ngày (bật đèn)', 2),
    ROW('op-lung-ugreen-iphone-16-pro', 'Chất liệu', 'TPU + PC', 1),
    ROW('kinh-cuong-luc-baseus-iphone-16', 'Độ cứng', '9H', 1)
) v(sp, ten, gia_tri, thu_tu)
JOIN san_pham sp ON sp.slug = v.sp AND sp.deleted_at IS NULL;

INSERT INTO tuy_chon (san_pham_id, ten, loai)
SELECT sp.id, v.ten, v.loai
FROM (VALUES
    ROW('cu-sac-anker-nano-65w', 'Màu sắc', 'mau_sac'),
    ROW('cu-sac-baseus-gan5-pro-100w', 'Màu sắc', 'mau_sac'),
    ROW('cap-usb-c-ugreen-100w', 'Chiều dài', 'khac'),
    ROW('sac-du-phong-anker-powercore-10000', 'Màu sắc', 'mau_sac'),
    ROW('tai-nghe-sony-wh-1000xm5', 'Màu sắc', 'mau_sac'),
    ROW('tai-nghe-samsung-galaxy-buds3', 'Màu sắc', 'mau_sac'),
    ROW('loa-jbl-flip-6', 'Màu sắc', 'mau_sac'),
    ROW('chuot-logitech-mx-master-3s', 'Màu sắc', 'mau_sac'),
    ROW('ban-phim-logitech-mx-mechanical-mini', 'Loại switch', 'phien_ban'),
    ROW('op-lung-ugreen-iphone-16-pro', 'Dòng máy', 'phien_ban')
) v(sp, ten, loai)
JOIN san_pham sp ON sp.slug = v.sp AND sp.deleted_at IS NULL;

INSERT INTO gia_tri_tuy_chon (tuy_chon_id, gia_tri, ma_mau, thu_tu)
SELECT tc.id, v.gia_tri, v.ma_mau, v.thu_tu
FROM (VALUES
    ROW('cu-sac-anker-nano-65w', 'Màu sắc', 'Đen', '#1F2937', 1), ROW('cu-sac-anker-nano-65w', 'Màu sắc', 'Trắng', '#F9FAFB', 2),
    ROW('cu-sac-baseus-gan5-pro-100w', 'Màu sắc', 'Đen', '#1F2937', 1), ROW('cu-sac-baseus-gan5-pro-100w', 'Màu sắc', 'Trắng', '#F9FAFB', 2),
    ROW('cap-usb-c-ugreen-100w', 'Chiều dài', '1m', NULL, 1), ROW('cap-usb-c-ugreen-100w', 'Chiều dài', '2m', NULL, 2),
    ROW('sac-du-phong-anker-powercore-10000', 'Màu sắc', 'Đen', '#1F2937', 1), ROW('sac-du-phong-anker-powercore-10000', 'Màu sắc', 'Xanh', '#2563EB', 2),
    ROW('tai-nghe-sony-wh-1000xm5', 'Màu sắc', 'Đen', '#111827', 1), ROW('tai-nghe-sony-wh-1000xm5', 'Màu sắc', 'Bạc', '#D1D5DB', 2),
    ROW('tai-nghe-samsung-galaxy-buds3', 'Màu sắc', 'Trắng', '#F9FAFB', 1), ROW('tai-nghe-samsung-galaxy-buds3', 'Màu sắc', 'Bạc', '#9CA3AF', 2),
    ROW('loa-jbl-flip-6', 'Màu sắc', 'Đen', '#111827', 1), ROW('loa-jbl-flip-6', 'Màu sắc', 'Xanh', '#1D4ED8', 2),
    ROW('loa-jbl-flip-6', 'Màu sắc', 'Đỏ', '#DC2626', 3),
    ROW('chuot-logitech-mx-master-3s', 'Màu sắc', 'Đen', '#1F2937', 1), ROW('chuot-logitech-mx-master-3s', 'Màu sắc', 'Xám nhạt', '#E5E7EB', 2),
    ROW('ban-phim-logitech-mx-mechanical-mini', 'Loại switch', 'Tactile', NULL, 1),
    ROW('ban-phim-logitech-mx-mechanical-mini', 'Loại switch', 'Linear', NULL, 2),
    ROW('op-lung-ugreen-iphone-16-pro', 'Dòng máy', 'iPhone 16 Pro', NULL, 1),
    ROW('op-lung-ugreen-iphone-16-pro', 'Dòng máy', 'iPhone 16 Pro Max', NULL, 2)
) v(sp, tuy_chon, gia_tri, ma_mau, thu_tu)
JOIN san_pham sp ON sp.slug = v.sp AND sp.deleted_at IS NULL
JOIN tuy_chon tc ON tc.san_pham_id = sp.id AND tc.ten = v.tuy_chon;

-- Biến thể; (tuy_chon, gia_tri) NULL = sản phẩm không có tùy chọn
CREATE TEMPORARY TABLE v_bien_the
SELECT * FROM (VALUES
    ROW('cu-sac-anker-nano-65w', 'ANK-NANO65-DEN', 790000, 990000, 25, 'Màu sắc', 'Đen'),
    ROW('cu-sac-anker-nano-65w', 'ANK-NANO65-TRANG', 790000, 990000, 18, 'Màu sắc', 'Trắng'),
    ROW('cu-sac-apple-usb-c-20w', 'APL-20W', 490000, 550000, 40, NULL, NULL),
    ROW('cu-sac-baseus-gan5-pro-100w', 'BAS-GAN5-100-DEN', 1090000, NULL, 12, 'Màu sắc', 'Đen'),
    ROW('cu-sac-baseus-gan5-pro-100w', 'BAS-GAN5-100-TRANG', 1090000, NULL, 0, 'Màu sắc', 'Trắng'),
    ROW('cap-usb-c-ugreen-100w', 'UGR-CC100-1M', 159000, NULL, 60, 'Chiều dài', '1m'),
    ROW('cap-usb-c-ugreen-100w', 'UGR-CC100-2M', 199000, NULL, 45, 'Chiều dài', '2m'),
    ROW('cap-lightning-anker-powerline-iii', 'ANK-PL3-1M', 290000, 350000, 30, NULL, NULL),
    ROW('sac-du-phong-anker-powercore-10000', 'ANK-PC10K-DEN', 590000, 690000, 20, 'Màu sắc', 'Đen'),
    ROW('sac-du-phong-anker-powercore-10000', 'ANK-PC10K-XANH', 590000, 690000, 7, 'Màu sắc', 'Xanh'),
    ROW('sac-du-phong-xiaomi-20000', 'XMI-PB20K', 650000, NULL, 0, NULL, NULL),
    ROW('tai-nghe-sony-wh-1000xm5', 'SNY-XM5-DEN', 7990000, 8990000, 6, 'Màu sắc', 'Đen'),
    ROW('tai-nghe-sony-wh-1000xm5', 'SNY-XM5-BAC', 7990000, 8990000, 4, 'Màu sắc', 'Bạc'),
    ROW('tai-nghe-apple-airpods-pro-2', 'APL-APP2', 5490000, 6190000, 15, NULL, NULL),
    ROW('tai-nghe-samsung-galaxy-buds3', 'SSG-BUDS3-TRANG', 3490000, NULL, 10, 'Màu sắc', 'Trắng'),
    ROW('tai-nghe-samsung-galaxy-buds3', 'SSG-BUDS3-BAC', 3490000, NULL, 10, 'Màu sắc', 'Bạc'),
    ROW('loa-jbl-flip-6', 'JBL-FLIP6-DEN', 2690000, 2990000, 8, 'Màu sắc', 'Đen'),
    ROW('loa-jbl-flip-6', 'JBL-FLIP6-XANH', 2690000, 2990000, 5, 'Màu sắc', 'Xanh'),
    ROW('loa-jbl-flip-6', 'JBL-FLIP6-DO', 2690000, 2990000, 3, 'Màu sắc', 'Đỏ'),
    ROW('loa-sony-srs-xb13', 'SNY-XB13', 990000, NULL, 12, NULL, NULL),
    ROW('chuot-logitech-mx-master-3s', 'LOG-MX3S-DEN', 2290000, 2490000, 9, 'Màu sắc', 'Đen'),
    ROW('chuot-logitech-mx-master-3s', 'LOG-MX3S-XAM', 2290000, 2490000, 6, 'Màu sắc', 'Xám nhạt'),
    ROW('chuot-logitech-g304', 'LOG-G304', 790000, NULL, 30, NULL, NULL),
    ROW('ban-phim-logitech-mx-mechanical-mini', 'LOG-MXMINI-TAC', 3290000, NULL, 5, 'Loại switch', 'Tactile'),
    ROW('ban-phim-logitech-mx-mechanical-mini', 'LOG-MXMINI-LIN', 3290000, NULL, 5, 'Loại switch', 'Linear'),
    ROW('op-lung-ugreen-iphone-16-pro', 'UGR-OP16P', 190000, NULL, 50, 'Dòng máy', 'iPhone 16 Pro'),
    ROW('op-lung-ugreen-iphone-16-pro', 'UGR-OP16PM', 210000, NULL, 50, 'Dòng máy', 'iPhone 16 Pro Max'),
    ROW('kinh-cuong-luc-baseus-iphone-16', 'BAS-KCL16', 150000, NULL, 80, NULL, NULL)
) v(sp, sku, gia_ban, gia_goc, ton, tuy_chon, gia_tri);
INSERT INTO bien_the (san_pham_id, sku, gia_ban, gia_goc, so_luong_ton)
SELECT sp.id, v.sku, v.gia_ban, v.gia_goc, v.ton
FROM v_bien_the v JOIN san_pham sp ON sp.slug = v.sp AND sp.deleted_at IS NULL;
INSERT INTO bien_the_gia_tri (bien_the_id, gia_tri_tuy_chon_id)
SELECT bt.id, gt.id
FROM v_bien_the v
JOIN bien_the bt ON bt.sku = v.sku AND bt.deleted_at IS NULL
JOIN tuy_chon tc ON tc.san_pham_id = bt.san_pham_id AND tc.ten = v.tuy_chon
JOIN gia_tri_tuy_chon gt ON gt.tuy_chon_id = tc.id AND gt.gia_tri = v.gia_tri;
DROP TEMPORARY TABLE v_bien_the;

-- ===== Phase 10: bổ sung sản phẩm, ảnh (Wikimedia Commons – nguồn: docs/nguon-anh-mau.md), banner, tồn kho ban đầu =====

INSERT INTO thuong_hieu (ten, slug) VALUES ('Keychron', 'keychron')
ON DUPLICATE KEY UPDATE thuong_hieu.id = thuong_hieu.id;

INSERT INTO san_pham (danh_muc_id, thuong_hieu_id, ten, slug, mo_ta_ngan, mo_ta, bao_hanh_thang, noi_bat, luot_xem, hien_thi, created_at)
SELECT dm.id, th.id, v.ten, v.slug, v.mo_ta_ngan, v.mo_ta, v.bh, v.noi_bat, v.luot_xem, v.hien_thi, now() - INTERVAL v.ngay DAY
FROM (VALUES
    ROW('cu-sac-samsung-25w', 'cu-sac', 'samsung', 'Củ sạc nhanh Samsung 25W',
     'Sạc siêu nhanh 25W cho điện thoại Galaxy.', '<p>Chuẩn USB PD 3.0 PPS, 1 cổng USB-C.</p>', 12, FALSE, 170, 85, TRUE),
    ROW('tau-sac-o-to-ugreen-30w', 'cu-sac', 'ugreen', 'Tẩu sạc ô tô Ugreen 30W',
     '2 cổng USB-C và USB-A, sạc nhanh trên xe.', '<p>Tổng công suất 30W, vỏ hợp kim nhôm tản nhiệt tốt.</p>', 18, FALSE, 65, 4, TRUE),
    ROW('cap-usb-c-baseus-60w', 'cap-sac', 'baseus', 'Cáp USB-C to USB-C Baseus 60W',
     'Sạc nhanh 60W, vỏ bện chống đứt.', '<p>Hỗ trợ USB PD 60W, truyền dữ liệu 480Mbps.</p>', 12, FALSE, 95, 70, TRUE),
    ROW('cap-usb-a-usb-c-ugreen-3a', 'cap-sac', 'ugreen', 'Cáp USB-A to USB-C Ugreen 3A',
     'Dùng với củ sạc USB-A, dòng 3A.', '<p>Đầu nối mạ niken chống oxy hóa, dài 1m.</p>', 18, FALSE, 140, 95, TRUE),
    ROW('sac-du-phong-samsung-eb-p1100c', 'sac-du-phong', 'samsung', 'Sạc dự phòng Samsung EB-P1100C 10000mAh',
     'Mỏng nhẹ, vỏ kim loại, sạc nhanh 15W.', '<p>1 cổng USB-C và 1 cổng USB-A, sạc 2 thiết bị cùng lúc.</p>', 6, FALSE, 110, 90, TRUE),
    ROW('sac-du-phong-xiaomi-pro-10000', 'sac-du-phong', 'xiaomi', 'Sạc dự phòng Xiaomi Mi Power Bank Pro 10000mAh',
     'Vỏ nhôm nguyên khối, sạc nhanh 18W.', '<p>Hỗ trợ Quick Charge 3.0 và USB PD.</p>', 6, FALSE, 85, 65, TRUE),
    ROW('tai-nghe-sony-wh-ch510', 'tai-nghe', 'sony', 'Tai nghe Sony WH-CH510',
     'Nhẹ 132g, pin 35 giờ.', '<p>Bluetooth 5.0, sạc USB-C. Đã ngừng kinh doanh.</p>', 12, FALSE, 230, 100, FALSE),
    ROW('tai-nghe-samsung-galaxy-buds2-pro', 'tai-nghe', 'samsung', 'Tai nghe Samsung Galaxy Buds2 Pro',
     'Chống ồn thông minh, âm thanh 24-bit Hi-Fi.', '<p>Chống nước IPX7, kết nối liền mạch với thiết bị Galaxy.</p>', 12, TRUE, 350, 75, TRUE),
    ROW('loa-jbl-charge-4', 'loa', 'jbl', 'Loa JBL Charge 4',
     'Pin 20 giờ, kiêm sạc dự phòng.', '<p>Chống nước IPX7, công suất 30W.</p>', 12, FALSE, 260, 80, TRUE),
    ROW('loa-jbl-go-2', 'loa', 'jbl', 'Loa JBL Go 2',
     'Siêu nhỏ gọn, chống nước IPX7.', '<p>Pin 5 giờ, có micro đàm thoại.</p>', 6, FALSE, 190, 60, TRUE),
    ROW('chuot-logitech-m331-silent-plus', 'chuot', 'logitech', 'Chuột không dây Logitech M331 Silent Plus',
     'Giảm 90% tiếng click, pin 24 tháng.', '<p>Đầu thu USB nano, cảm biến 1000 DPI.</p>', 12, FALSE, 300, 88, TRUE),
    ROW('chuot-logitech-m705-marathon', 'chuot', 'logitech', 'Chuột không dây Logitech M705 Marathon',
     'Pin tới 3 năm, cuộn siêu nhanh.', '<p>Thiết kế công thái học cho tay phải, 7 nút tùy chỉnh.</p>', 12, FALSE, 120, 5, TRUE),
    ROW('ban-phim-logitech-k380', 'ban-phim', 'logitech', 'Bàn phím Bluetooth Logitech K380',
     'Kết nối 3 thiết bị, chuyển đổi bằng 1 phím.', '<p>Nhỏ gọn, pin 2 năm, hỗ trợ Windows, macOS, iOS, Android.</p>', 12, TRUE, 280, 92, TRUE),
    ROW('ban-phim-co-keychron-k8', 'ban-phim', 'keychron', 'Bàn phím cơ Keychron K8',
     'Bố cục TKL 87 phím, Bluetooth 5.1.', '<p>Hot-swap, tương thích macOS và Windows.</p>', 12, TRUE, 410, 1, TRUE),
    ROW('op-lung-apple-silicone-iphone-17', 'op-lung', 'apple', 'Ốp lưng Silicone MagSafe Apple cho iPhone 17',
     'Chính hãng Apple, lớp lót microfiber.', '<p>Nam châm MagSafe tích hợp, ôm sát thân máy.</p>', 0, FALSE, 150, 7, TRUE),
    ROW('op-lung-samsung-galaxy-s22-ultra-tpu', 'op-lung', 'samsung', 'Ốp lưng TPU trong suốt Samsung Galaxy S22 Ultra',
     'Dẻo, mỏng, giữ nguyên thiết kế máy.', '<p>Gờ bảo vệ camera và màn hình.</p>', 0, FALSE, 45, 98, TRUE),
    ROW('mieng-dan-chong-nhin-trom-iphone', 'cuong-luc', 'ugreen', 'Kính cường lực chống nhìn trộm Ugreen iPhone 16',
     'Góc nhìn 60°, bảo vệ quyền riêng tư.', '<p>Độ cứng 9H, phủ lớp chống bám vân tay.</p>', 0, FALSE, 70, 55, TRUE),
    ROW('kinh-cuong-luc-samsung-galaxy-a04e', 'cuong-luc', 'samsung', 'Kính cường lực Samsung Galaxy A04e',
     'Full màn hình, độ cứng 9H.', '<p>Viền đen tràn cạnh, kèm khăn lau.</p>', 0, FALSE, 30, 62, TRUE)
) v(slug, dm, th, ten, mo_ta_ngan, mo_ta, bh, noi_bat, luot_xem, ngay, hien_thi)
JOIN danh_muc dm ON dm.slug = v.dm AND dm.deleted_at IS NULL
LEFT JOIN thuong_hieu th ON th.slug = v.th AND th.deleted_at IS NULL;

INSERT INTO thong_so (san_pham_id, ten, gia_tri, thu_tu)
SELECT sp.id, v.ten, v.gia_tri, v.thu_tu
FROM (VALUES
    ROW('cu-sac-samsung-25w', 'Công suất', '25W', 1),
    ROW('cu-sac-samsung-25w', 'Cổng', '1 x USB-C', 2),
    ROW('tau-sac-o-to-ugreen-30w', 'Công suất', '30W', 1),
    ROW('tau-sac-o-to-ugreen-30w', 'Cổng', '1 x USB-C, 1 x USB-A', 2),
    ROW('cap-usb-c-baseus-60w', 'Công suất', '60W', 1),
    ROW('cap-usb-c-baseus-60w', 'Chất liệu', 'Bện dù', 2),
    ROW('cap-usb-a-usb-c-ugreen-3a', 'Dòng điện', '3A', 1),
    ROW('cap-usb-a-usb-c-ugreen-3a', 'Chiều dài', '1m', 2),
    ROW('sac-du-phong-samsung-eb-p1100c', 'Dung lượng', '10.000mAh', 1),
    ROW('sac-du-phong-samsung-eb-p1100c', 'Công suất', '15W', 2),
    ROW('sac-du-phong-xiaomi-pro-10000', 'Dung lượng', '10.000mAh', 1),
    ROW('sac-du-phong-xiaomi-pro-10000', 'Công suất', '18W', 2),
    ROW('tai-nghe-sony-wh-ch510', 'Kết nối', 'Bluetooth 5.0', 1),
    ROW('tai-nghe-sony-wh-ch510', 'Thời lượng pin', '35 giờ', 2),
    ROW('tai-nghe-samsung-galaxy-buds2-pro', 'Kết nối', 'Bluetooth 5.3', 1),
    ROW('tai-nghe-samsung-galaxy-buds2-pro', 'Chống nước', 'IPX7', 2),
    ROW('loa-jbl-charge-4', 'Công suất', '30W', 1),
    ROW('loa-jbl-charge-4', 'Thời lượng pin', '20 giờ', 2),
    ROW('loa-jbl-go-2', 'Công suất', '3W', 1),
    ROW('loa-jbl-go-2', 'Chống nước', 'IPX7', 2),
    ROW('chuot-logitech-m331-silent-plus', 'DPI', '1000', 1),
    ROW('chuot-logitech-m331-silent-plus', 'Kết nối', 'USB nano 2.4GHz', 2),
    ROW('chuot-logitech-m705-marathon', 'DPI', '1000', 1),
    ROW('chuot-logitech-m705-marathon', 'Thời lượng pin', '3 năm', 2),
    ROW('ban-phim-logitech-k380', 'Kết nối', 'Bluetooth', 1),
    ROW('ban-phim-logitech-k380', 'Thời lượng pin', '2 năm', 2),
    ROW('ban-phim-co-keychron-k8', 'Bố cục', 'TKL 87 phím', 1),
    ROW('ban-phim-co-keychron-k8', 'Kết nối', 'Bluetooth 5.1, USB-C', 2),
    ROW('op-lung-apple-silicone-iphone-17', 'Chất liệu', 'Silicone', 1),
    ROW('op-lung-samsung-galaxy-s22-ultra-tpu', 'Chất liệu', 'TPU', 1),
    ROW('mieng-dan-chong-nhin-trom-iphone', 'Độ cứng', '9H', 1),
    ROW('kinh-cuong-luc-samsung-galaxy-a04e', 'Độ cứng', '9H', 1)
) v(sp, ten, gia_tri, thu_tu)
JOIN san_pham sp ON sp.slug = v.sp AND sp.deleted_at IS NULL;

INSERT INTO tuy_chon (san_pham_id, ten, loai)
SELECT sp.id, v.ten, v.loai
FROM (VALUES
    ROW('cap-usb-c-baseus-60w', 'Chiều dài', 'khac'),
    ROW('tai-nghe-samsung-galaxy-buds2-pro', 'Màu sắc', 'mau_sac'),
    ROW('loa-jbl-charge-4', 'Màu sắc', 'mau_sac'),
    ROW('loa-jbl-go-2', 'Màu sắc', 'mau_sac'),
    ROW('chuot-logitech-m331-silent-plus', 'Màu sắc', 'mau_sac'),
    ROW('ban-phim-co-keychron-k8', 'Loại switch', 'phien_ban')
) v(sp, ten, loai)
JOIN san_pham sp ON sp.slug = v.sp AND sp.deleted_at IS NULL;

INSERT INTO gia_tri_tuy_chon (tuy_chon_id, gia_tri, ma_mau, thu_tu)
SELECT tc.id, v.gia_tri, v.ma_mau, v.thu_tu
FROM (VALUES
    ROW('cap-usb-c-baseus-60w', 'Chiều dài', '1m', NULL, 1),
    ROW('cap-usb-c-baseus-60w', 'Chiều dài', '2m', NULL, 2),
    ROW('tai-nghe-samsung-galaxy-buds2-pro', 'Màu sắc', 'Trắng', '#F9FAFB', 1),
    ROW('tai-nghe-samsung-galaxy-buds2-pro', 'Màu sắc', 'Đen', '#111827', 2),
    ROW('loa-jbl-charge-4', 'Màu sắc', 'Đen', '#111827', 1),
    ROW('loa-jbl-charge-4', 'Màu sắc', 'Xanh', '#1D4ED8', 2),
    ROW('loa-jbl-go-2', 'Màu sắc', 'Xanh', '#1E40AF', 1),
    ROW('loa-jbl-go-2', 'Màu sắc', 'Đen', '#111827', 2),
    ROW('loa-jbl-go-2', 'Màu sắc', 'Đỏ', '#DC2626', 3),
    ROW('chuot-logitech-m331-silent-plus', 'Màu sắc', 'Đen', '#1F2937', 1),
    ROW('chuot-logitech-m331-silent-plus', 'Màu sắc', 'Đỏ', '#DC2626', 2),
    ROW('ban-phim-co-keychron-k8', 'Loại switch', 'Red', NULL, 1),
    ROW('ban-phim-co-keychron-k8', 'Loại switch', 'Brown', NULL, 2)
) v(sp, tuy_chon, gia_tri, ma_mau, thu_tu)
JOIN san_pham sp ON sp.slug = v.sp AND sp.deleted_at IS NULL
JOIN tuy_chon tc ON tc.san_pham_id = sp.id AND tc.ten = v.tuy_chon;

-- Biến thể mới; gia_tri NULL = sản phẩm không có tùy chọn (mỗi sản phẩm mới có tối đa 1 tùy chọn)
CREATE TEMPORARY TABLE v_bien_the
SELECT * FROM (VALUES
    ROW('cu-sac-samsung-25w', 'SSG-25W', 390000, 450000, 40, NULL),
    ROW('tau-sac-o-to-ugreen-30w', 'UGR-CAR30', 290000, NULL, 25, NULL),
    ROW('cap-usb-c-baseus-60w', 'BAS-CC60-1M', 129000, NULL, 60, '1m'),
    ROW('cap-usb-c-baseus-60w', 'BAS-CC60-2M', 159000, NULL, 40, '2m'),
    ROW('cap-usb-a-usb-c-ugreen-3a', 'UGR-AC3A', 99000, 129000, 80, NULL),
    ROW('sac-du-phong-samsung-eb-p1100c', 'SSG-PB10K', 690000, 790000, 20, NULL),
    ROW('sac-du-phong-xiaomi-pro-10000', 'XMI-PB10K-PRO', 550000, NULL, 25, NULL),
    ROW('tai-nghe-sony-wh-ch510', 'SNY-CH510', 990000, 1290000, 10, NULL),
    ROW('tai-nghe-samsung-galaxy-buds2-pro', 'SSG-BUDS2P-TRANG', 3990000, 4490000, 10, 'Trắng'),
    ROW('tai-nghe-samsung-galaxy-buds2-pro', 'SSG-BUDS2P-DEN', 3990000, 4490000, 6, 'Đen'),
    ROW('loa-jbl-charge-4', 'JBL-CHG4-DEN', 2490000, 2990000, 15, 'Đen'),
    ROW('loa-jbl-charge-4', 'JBL-CHG4-XANH', 2490000, 2990000, 8, 'Xanh'),
    ROW('loa-jbl-go-2', 'JBL-GO2-XANH', 690000, 790000, 6, 'Xanh'),
    ROW('loa-jbl-go-2', 'JBL-GO2-DEN', 690000, 790000, 15, 'Đen'),
    ROW('loa-jbl-go-2', 'JBL-GO2-DO', 690000, 790000, 2, 'Đỏ'),
    ROW('chuot-logitech-m331-silent-plus', 'LOG-M331-DEN', 390000, 450000, 50, 'Đen'),
    ROW('chuot-logitech-m331-silent-plus', 'LOG-M331-DO', 390000, 450000, 20, 'Đỏ'),
    ROW('chuot-logitech-m705-marathon', 'LOG-M705', 990000, NULL, 15, NULL),
    ROW('ban-phim-logitech-k380', 'LOG-K380', 790000, 890000, 30, NULL),
    ROW('ban-phim-co-keychron-k8', 'KEY-K8-RED', 2190000, NULL, 12, 'Red'),
    ROW('ban-phim-co-keychron-k8', 'KEY-K8-BROWN', 2190000, NULL, 10, 'Brown'),
    ROW('op-lung-apple-silicone-iphone-17', 'APL-SC17', 1190000, 1290000, 20, NULL),
    ROW('op-lung-samsung-galaxy-s22-ultra-tpu', 'SSG-S22U-TPU', 250000, NULL, 30, NULL),
    ROW('mieng-dan-chong-nhin-trom-iphone', 'UGR-PRIV16', 290000, NULL, 45, NULL),
    ROW('kinh-cuong-luc-samsung-galaxy-a04e', 'SSG-KCL-A04E', 120000, NULL, 60, NULL)
) v(sp, sku, gia_ban, gia_goc, ton, gia_tri);
INSERT INTO bien_the (san_pham_id, sku, gia_ban, gia_goc, so_luong_ton, created_at)
SELECT sp.id, v.sku, v.gia_ban, v.gia_goc, v.ton, sp.created_at
FROM v_bien_the v JOIN san_pham sp ON sp.slug = v.sp AND sp.deleted_at IS NULL;
INSERT INTO bien_the_gia_tri (bien_the_id, gia_tri_tuy_chon_id)
SELECT bt.id, gt.id
FROM v_bien_the v
JOIN bien_the bt ON bt.sku = v.sku AND bt.deleted_at IS NULL
JOIN tuy_chon tc ON tc.san_pham_id = bt.san_pham_id
JOIN gia_tri_tuy_chon gt ON gt.tuy_chon_id = tc.id AND gt.gia_tri = v.gia_tri;
DROP TEMPORARY TABLE v_bien_the;

-- Ảnh sản phẩm (ảnh số 1 là ảnh chính)
INSERT INTO hinh_anh_san_pham (san_pham_id, duong_dan, mo_ta, thu_tu, la_anh_chinh)
SELECT sp.id, v.duong_dan, sp.ten, v.thu_tu, v.thu_tu = 1
FROM (VALUES
    ROW('cu-sac-anker-nano-65w', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4a/Silicon_vs_GaN_30W_USB-C_chargers.jpg/960px-Silicon_vs_GaN_30W_USB-C_chargers.jpg'),
    ROW('cu-sac-apple-usb-c-20w', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6b/Apple_USB-C_%28USB_Type_C%29_87W_Power_Adapter_for_MacBook_Pro_%2848979218053%29.jpg/960px-Apple_USB-C_%28USB_Type_C%29_87W_Power_Adapter_for_MacBook_Pro_%2848979218053%29.jpg'),
    ROW('cu-sac-baseus-gan5-pro-100w', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0c/Baseus_USB_chargers-1.jpg/960px-Baseus_USB_chargers-1.jpg'),
    ROW('cu-sac-baseus-gan5-pro-100w', 2, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/3d/Baseus_USB_chargers-0.jpg/960px-Baseus_USB_chargers-0.jpg'),
    ROW('cap-usb-c-ugreen-100w', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c3/USB-C_cable_2017_A.jpg/960px-USB-C_cable_2017_A.jpg'),
    ROW('cap-lightning-anker-powerline-iii', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/df/Lightning_connector.jpg/960px-Lightning_connector.jpg'),
    ROW('sac-du-phong-anker-powercore-10000', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/ee/Anker_power_bank_and_cable.jpg/960px-Anker_power_bank_and_cable.jpg'),
    ROW('sac-du-phong-anker-powercore-10000', 2, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4e/Anker_power_bank_lit.jpg/960px-Anker_power_bank_lit.jpg'),
    ROW('sac-du-phong-xiaomi-20000', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5b/Xiaomi_PowerBank.jpg/960px-Xiaomi_PowerBank.jpg'),
    ROW('tai-nghe-sony-wh-1000xm5', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b2/Sony_Headphones_%2840476165073%29.jpg/960px-Sony_Headphones_%2840476165073%29.jpg'),
    ROW('tai-nghe-apple-airpods-pro-2', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/2f/AirPods_Pro_%282nd_generation%29.jpg/960px-AirPods_Pro_%282nd_generation%29.jpg'),
    ROW('tai-nghe-apple-airpods-pro-2', 2, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4c/AirPods_Pro_case.jpg/960px-AirPods_Pro_case.jpg'),
    ROW('tai-nghe-apple-airpods-pro-2', 3, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/97/AirPods_Pro_2.jpg/960px-AirPods_Pro_2.jpg'),
    ROW('tai-nghe-samsung-galaxy-buds3', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a4/Samsung_Galaxy_Buds.jpg/960px-Samsung_Galaxy_Buds.jpg'),
    ROW('loa-jbl-flip-6', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f9/JBL_Flip_bluetooth_speaker.jpg/960px-JBL_Flip_bluetooth_speaker.jpg'),
    ROW('loa-jbl-flip-6', 2, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a2/JBL_Flip_4.jpg/960px-JBL_Flip_4.jpg'),
    ROW('loa-sony-srs-xb13', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9a/SONY_SRS-XB2_01.JPG/960px-SONY_SRS-XB2_01.JPG'),
    ROW('chuot-logitech-mx-master-3s', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/69/Logitech_MX_Master_3S_HS13.jpg/960px-Logitech_MX_Master_3S_HS13.jpg'),
    ROW('chuot-logitech-mx-master-3s', 2, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/21/Logitech_MX_Master_3S_HS10.jpg/960px-Logitech_MX_Master_3S_HS10.jpg'),
    ROW('chuot-logitech-mx-master-3s', 3, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/90/Logitech_MX_Master_3S_HS01.jpg/960px-Logitech_MX_Master_3S_HS01.jpg'),
    ROW('chuot-logitech-g304', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c9/Logitech%28G703%29_mouse.jpg/960px-Logitech%28G703%29_mouse.jpg'),
    ROW('ban-phim-logitech-mx-mechanical-mini', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7f/Beautiful_Mechanical_Keyboard.jpg/960px-Beautiful_Mechanical_Keyboard.jpg'),
    ROW('op-lung-ugreen-iphone-16-pro', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f3/IPhone_case_wikipedia.jpg/960px-IPhone_case_wikipedia.jpg'),
    ROW('kinh-cuong-luc-baseus-iphone-16', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c4/White_iPhone_4S_with_screen_protector_vs_iPhone_5S.jpg/960px-White_iPhone_4S_with_screen_protector_vs_iPhone_5S.jpg'),
    ROW('cu-sac-samsung-25w', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f5/Samsung_Charger.jpg/960px-Samsung_Charger.jpg'),
    ROW('tau-sac-o-to-ugreen-30w', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/92/Ugreen_car_charger_USB-C_USB-A_30W_25845.jpg/960px-Ugreen_car_charger_USB-C_USB-A_30W_25845.jpg'),
    ROW('cap-usb-c-baseus-60w', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5c/Two_USB-C_connectors_being_held.jpg/960px-Two_USB-C_connectors_being_held.jpg'),
    ROW('cap-usb-a-usb-c-ugreen-3a', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5f/2026-08-22_UGreen-USB-C-Kabel_HOF1918_RAW-Export.png/960px-2026-08-22_UGreen-USB-C-Kabel_HOF1918_RAW-Export.png'),
    ROW('sac-du-phong-samsung-eb-p1100c', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e9/SAMSUNG_BATTERY_PACK_%28POWER_BANK%29_EB-P1100C.jpg/960px-SAMSUNG_BATTERY_PACK_%28POWER_BANK%29_EB-P1100C.jpg'),
    ROW('sac-du-phong-samsung-eb-p1100c', 2, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1e/SAMSUNG_BATTERY_PACK_%28POWER_BANK%29_EB-P1100C_%282%29.jpg/960px-SAMSUNG_BATTERY_PACK_%28POWER_BANK%29_EB-P1100C_%282%29.jpg'),
    ROW('sac-du-phong-xiaomi-pro-10000', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e8/MiPowerBankPro10000mAhQC30Xiaomi20240820000.jpg/960px-MiPowerBankPro10000mAhQC30Xiaomi20240820000.jpg'),
    ROW('sac-du-phong-xiaomi-pro-10000', 2, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/50/MiPowerBankPro10000mAhQC30Xiaomi20240820001.jpg/960px-MiPowerBankPro10000mAhQC30Xiaomi20240820001.jpg'),
    ROW('tai-nghe-sony-wh-ch510', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/01/Sony_WH-CH510_Bluetooth_Over-Ear_Headphone.jpg/960px-Sony_WH-CH510_Bluetooth_Over-Ear_Headphone.jpg'),
    ROW('tai-nghe-samsung-galaxy-buds2-pro', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9e/Samsung_Galaxy_Buds_FE_and_Samsung_Galaxy_Buds2_Pro.jpg/960px-Samsung_Galaxy_Buds_FE_and_Samsung_Galaxy_Buds2_Pro.jpg'),
    ROW('loa-jbl-charge-4', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/33/JBL_Charge_4_1v2.jpg/960px-JBL_Charge_4_1v2.jpg'),
    ROW('loa-jbl-charge-4', 2, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/ef/JBL_Charge_4_2v2.jpg/960px-JBL_Charge_4_2v2.jpg'),
    ROW('loa-jbl-go-2', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9c/JBL_GO2_Bluetooth_speaker.jpeg/960px-JBL_GO2_Bluetooth_speaker.jpeg'),
    ROW('loa-jbl-go-2', 2, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/32/JBL_GO2_Bluetooth_speaker_00_10_27_681000_%28turned_upright%29.jpeg/960px-JBL_GO2_Bluetooth_speaker_00_10_27_681000_%28turned_upright%29.jpeg'),
    ROW('chuot-logitech-m331-silent-plus', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/39/Logitech_M331_Silent_Mouse.jpg/960px-Logitech_M331_Silent_Mouse.jpg'),
    ROW('chuot-logitech-m705-marathon', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8a/2023_Mysz_komputerowa_Logitech_M705.jpg/960px-2023_Mysz_komputerowa_Logitech_M705.jpg'),
    ROW('ban-phim-logitech-k380', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e6/Logitech_K380_CJK_Keyboard.jpg/960px-Logitech_K380_CJK_Keyboard.jpg'),
    ROW('ban-phim-co-keychron-k8', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/fb/Keychron_K8_Non-Backlight_Wireless_Mechanical_Keyboard.jpg/960px-Keychron_K8_Non-Backlight_Wireless_Mechanical_Keyboard.jpg'),
    ROW('ban-phim-co-keychron-k8', 2, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8d/Keychron_K8_Non-Backlight_Wireless_Mechanical_Keyboard_-_52771896827.jpg/960px-Keychron_K8_Non-Backlight_Wireless_Mechanical_Keyboard_-_52771896827.jpg'),
    ROW('op-lung-apple-silicone-iphone-17', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/07/Apple_iPhone_17_Silicone_Case_%28Black%29.jpg/960px-Apple_iPhone_17_Silicone_Case_%28Black%29.jpg'),
    ROW('op-lung-apple-silicone-iphone-17', 2, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b7/Apple_iPhone_17_Silicone_Case_%28Black%29_rear.jpg/960px-Apple_iPhone_17_Silicone_Case_%28Black%29_rear.jpg'),
    ROW('op-lung-samsung-galaxy-s22-ultra-tpu', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/78/SAMSUNG_Galaxy_S22_Ultra_TPU_CASE.jpg/960px-SAMSUNG_Galaxy_S22_Ultra_TPU_CASE.jpg'),
    ROW('op-lung-samsung-galaxy-s22-ultra-tpu', 2, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4e/SAMSUNG_Galaxy_S22_Ultra_TPU_CASE_%282%29.jpg/960px-SAMSUNG_Galaxy_S22_Ultra_TPU_CASE_%282%29.jpg'),
    ROW('mieng-dan-chong-nhin-trom-iphone', 1, 'https://upload.wikimedia.org/wikipedia/commons/f/f0/%D9%85%D8%AD%D8%A7%D9%81%D8%B8_%D8%B5%D9%81%D8%AD%D9%87_%D9%86%D9%85%D8%A7%DB%8C%D8%B4_%D8%AD%D8%B1%DB%8C%D9%85_%D8%B4%D8%AE%D8%B5%DB%8C.jpg'),
    ROW('kinh-cuong-luc-samsung-galaxy-a04e', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6a/Samsung_Galaxy_A04e_front_with_screen_protector.jpg/960px-Samsung_Galaxy_A04e_front_with_screen_protector.jpg')
) v(sp, thu_tu, duong_dan)
JOIN san_pham sp ON sp.slug = v.sp AND sp.deleted_at IS NULL;

UPDATE thuong_hieu th
JOIN (VALUES
    ROW('anker', 'https://upload.wikimedia.org/wikipedia/commons/a/a4/Anker_Innovations_Co._Ltd._Logo.png'),
    ROW('apple', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/fa/Apple_logo_black.svg/960px-Apple_logo_black.svg.png'),
    ROW('jbl', 'https://upload.wikimedia.org/wikipedia/commons/d/d2/Jbl-logo.jpg'),
    ROW('logitech', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/17/Logitech_logo.svg/960px-Logitech_logo.svg.png'),
    ROW('samsung', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b4/Samsung_wordmark.svg/960px-Samsung_wordmark.svg.png'),
    ROW('sony', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/ca/Sony_logo.svg/960px-Sony_logo.svg.png'),
    ROW('ugreen', 'https://upload.wikimedia.org/wikipedia/commons/7/75/UGREEN_wordmark.png'),
    ROW('xiaomi', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/ae/Xiaomi_logo_%282021-%29.svg/960px-Xiaomi_logo_%282021-%29.svg.png'),
    ROW('keychron', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/69/Keychron_logo.svg/960px-Keychron_logo.svg.png')
) v(slug, logo) ON th.slug = v.slug
SET th.logo = v.logo
WHERE th.deleted_at IS NULL;

UPDATE danh_muc dm
JOIN (VALUES
    ROW('sac-cap', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/53/Charging_a_smartphone_with_a_USB_power_bank.jpg/960px-Charging_a_smartphone_with_a_USB_power_bank.jpg'),
    ROW('am-thanh', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f6/Headphones_on_desk.jpg/960px-Headphones_on_desk.jpg'),
    ROW('phu-kien-may-tinh', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c6/Black_and_white_computer_desk_%28Unsplash%29.jpg/960px-Black_and_white_computer_desk_%28Unsplash%29.jpg'),
    ROW('op-lung-bao-ve', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a1/Apple_iPhone_17_Silicone_Case_%28Black%29_unboxing.jpg/960px-Apple_iPhone_17_Silicone_Case_%28Black%29_unboxing.jpg')
) v(slug, hinh_anh) ON dm.slug = v.slug
SET dm.hinh_anh = v.hinh_anh
WHERE dm.deleted_at IS NULL;

-- Banner: 3 đang chạy, 1 đã hết hạn (không hiện ở trang chủ, vẫn thấy trong trang quản trị)
INSERT INTO banner (tieu_de, hinh_anh, lien_ket, thu_tu, bat_dau_luc, ket_thuc_luc)
VALUES
    ('Âm thanh đỉnh cao – giảm đến 20%',
     'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/55/Top_view_of_headphones_hanging_from_a_desk_%28Unsplash%29.jpg/1920px-Top_view_of_headphones_hanging_from_a_desk_%28Unsplash%29.jpg',
     '/san-pham?danhMuc=am-thanh', 1, now() - INTERVAL 10 DAY, now() + INTERVAL 60 DAY),
    ('Phụ kiện chính hãng, bảo hành tận nơi',
     'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/11/Xiaomi_Store_Portugal.jpg/1920px-Xiaomi_Store_Portugal.jpg',
     '/san-pham', 2, now() - INTERVAL 30 DAY, NULL),
    ('Bàn phím cơ mới về – Keychron K8',
     'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7b/Mechanical_Keyboard_Exhibition.jpg/1920px-Mechanical_Keyboard_Exhibition.jpg',
     '/san-pham/ban-phim-co-keychron-k8', 3, now() - INTERVAL 1 DAY, now() + INTERVAL 30 DAY),
    ('Sale tai nghe tháng trước (đã kết thúc)',
     'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6b/Desk-music-headphones-earphones_%2824243083451%29.jpg/1920px-Desk-music-headphones-earphones_%2824243083451%29.jpg',
     '/san-pham?danhMuc=tai-nghe', 4, now() - INTERVAL 60 DAY, now() + INTERVAL -30 DAY);

-- Biến thể tạo cùng lúc với sản phẩm (khớp mốc 'khoi_tao' bên dưới)
UPDATE bien_the bt JOIN san_pham sp ON sp.id = bt.san_pham_id
SET bt.created_at = sp.created_at, bt.updated_at = sp.created_at;

-- Tồn kho ban đầu của mọi biến thể (đơn mẫu ở R__du_lieu_mau_van_hanh.sql trừ tiếp từ mốc này)
INSERT INTO lich_su_ton_kho (bien_the_id, loai, so_luong_thay_doi, so_luong_sau, ghi_chu, nguoi_thuc_hien_id, created_at)
SELECT bt.id, 'khoi_tao', bt.so_luong_ton, bt.so_luong_ton, 'Tồn kho ban đầu',
       (SELECT id FROM nguoi_dung WHERE email = 'admin@techgear.vn'), sp.created_at
FROM bien_the bt
JOIN san_pham sp ON sp.id = bt.san_pham_id
WHERE bt.so_luong_ton > 0;

-- Giá hiển thị = biến thể đang bán rẻ nhất
UPDATE san_pham sp
JOIN (SELECT san_pham_id, gia_ban, gia_goc,
             row_number() OVER (PARTITION BY san_pham_id ORDER BY gia_ban, id) AS thu_hang
      FROM bien_the WHERE hien_thi AND deleted_at IS NULL) r ON r.san_pham_id = sp.id AND r.thu_hang = 1
SET sp.gia_ban = r.gia_ban, sp.gia_goc = r.gia_goc;
END //
DELIMITER ;
CALL nap_du_lieu_mau_san_pham();
DROP PROCEDURE nap_du_lieu_mau_san_pham;
