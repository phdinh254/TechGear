-- Dữ liệu mẫu danh mục, thương hiệu, sản phẩm – chỉ profile dev, chỉ nạp khi chưa có sản phẩm nào
-- (Flyway chạy lại file khi đổi nội dung; muốn nạp lại từ đầu: docker compose down -v).
-- Ảnh là link Wikimedia Commons (cần internet khi xem); nguồn, giấy phép, tác giả: docs/nguon-anh-mau.md.

DO $$
BEGIN
IF EXISTS (SELECT 1 FROM san_pham) THEN
    RAISE NOTICE 'Bỏ qua dữ liệu mẫu sản phẩm: CSDL đã có sản phẩm. Muốn nạp lại dữ liệu mẫu đầy đủ: docker compose down -v';
    RETURN;
END IF;

INSERT INTO danh_muc (ten, slug, thu_tu)
VALUES ('Sạc & cáp', 'sac-cap', 1),
       ('Âm thanh', 'am-thanh', 2),
       ('Phụ kiện máy tính', 'phu-kien-may-tinh', 3),
       ('Ốp lưng & bảo vệ', 'op-lung-bao-ve', 4)
ON CONFLICT (slug) WHERE deleted_at IS NULL DO NOTHING;

INSERT INTO danh_muc (danh_muc_cha_id, ten, slug, thu_tu)
SELECT cha.id, v.ten, v.slug, v.thu_tu
FROM (VALUES ('sac-cap', 'Củ sạc', 'cu-sac', 1),
             ('sac-cap', 'Cáp sạc', 'cap-sac', 2),
             ('sac-cap', 'Sạc dự phòng', 'sac-du-phong', 3),
             ('am-thanh', 'Tai nghe', 'tai-nghe', 1),
             ('am-thanh', 'Loa bluetooth', 'loa', 2),
             ('phu-kien-may-tinh', 'Chuột', 'chuot', 1),
             ('phu-kien-may-tinh', 'Bàn phím', 'ban-phim', 2),
             ('op-lung-bao-ve', 'Ốp lưng', 'op-lung', 1),
             ('op-lung-bao-ve', 'Kính cường lực', 'cuong-luc', 2)) v(cha, ten, slug, thu_tu)
JOIN danh_muc cha ON cha.slug = v.cha AND cha.deleted_at IS NULL
ON CONFLICT (slug) WHERE deleted_at IS NULL DO NOTHING;

INSERT INTO thuong_hieu (ten, slug)
VALUES ('Anker', 'anker'), ('Apple', 'apple'), ('Baseus', 'baseus'), ('JBL', 'jbl'), ('Logitech', 'logitech'),
       ('Samsung', 'samsung'), ('Sony', 'sony'), ('Ugreen', 'ugreen'), ('Xiaomi', 'xiaomi')
ON CONFLICT (slug) WHERE deleted_at IS NULL DO NOTHING;

-- ngay = tạo cách đây bao nhiêu ngày (để sắp xếp "mới nhất" có thứ tự)
INSERT INTO san_pham (danh_muc_id, thuong_hieu_id, ten, slug, mo_ta_ngan, mo_ta, bao_hanh_thang, noi_bat, luot_xem, created_at)
SELECT dm.id, th.id, v.ten, v.slug, v.mo_ta_ngan, v.mo_ta, v.bh, v.noi_bat, v.luot_xem, now() - make_interval(days => v.ngay)
FROM (VALUES
    ('cu-sac', 'anker', 'Củ sạc Anker Nano 65W GaN', 'cu-sac-anker-nano-65w',
     'Sạc nhanh 65W cho laptop, điện thoại; nhỏ gọn nhờ công nghệ GaN.',
     '<p>Củ sạc 1 cổng USB-C 65W, hỗ trợ PD 3.0 và PPS.</p>', 18, TRUE, 320, 30),
    ('cu-sac', 'apple', 'Củ sạc Apple USB-C 20W', 'cu-sac-apple-usb-c-20w',
     'Củ sạc chính hãng Apple, sạc nhanh iPhone.', '<p>Sạc nhanh iPhone 50% trong khoảng 30 phút.</p>', 12, FALSE, 210, 28),
    ('cu-sac', 'baseus', 'Củ sạc Baseus GaN5 Pro 100W', 'cu-sac-baseus-gan5-pro-100w',
     '3 cổng (2 USB-C, 1 USB-A), tổng công suất 100W.', '<p>Sạc cùng lúc laptop, máy tính bảng và điện thoại.</p>', 12, TRUE, 150, 20),
    ('cap-sac', 'ugreen', 'Cáp USB-C to USB-C Ugreen 100W', 'cap-usb-c-ugreen-100w',
     'Cáp bện dù, hỗ trợ sạc nhanh 100W.', '<p>Chip E-Marker, truyền dữ liệu 480Mbps.</p>', 18, TRUE, 280, 25),
    ('cap-sac', 'anker', 'Cáp Lightning Anker PowerLine III', 'cap-lightning-anker-powerline-iii',
     'Chuẩn MFi, độ bền 25.000 lần gập.', '<p>Cáp USB-C to Lightning dài 1m.</p>', 24, FALSE, 90, 40),
    ('sac-du-phong', 'anker', 'Sạc dự phòng Anker PowerCore 10000mAh', 'sac-du-phong-anker-powercore-10000',
     'Nhỏ gọn, sạc nhanh 20W.', '<p>Dung lượng 10.000mAh, 1 cổng USB-C vào/ra và 1 cổng USB-A.</p>', 18, TRUE, 260, 18),
    ('sac-du-phong', 'xiaomi', 'Sạc dự phòng Xiaomi 20000mAh 22.5W', 'sac-du-phong-xiaomi-20000',
     'Dung lượng lớn, sạc nhanh 22.5W.', '<p>3 cổng ra, sạc được 3 thiết bị cùng lúc.</p>', 6, FALSE, 120, 35),
    ('tai-nghe', 'sony', 'Tai nghe Sony WH-1000XM5', 'tai-nghe-sony-wh-1000xm5',
     'Chống ồn chủ động hàng đầu, pin 30 giờ.', '<p>8 micro chống ồn, kết nối Bluetooth 5.2 đa điểm.</p>', 12, TRUE, 540, 10),
    ('tai-nghe', 'apple', 'Tai nghe Apple AirPods Pro 2', 'tai-nghe-apple-airpods-pro-2',
     'Chống ồn chủ động, hộp sạc USB-C.', '<p>Chip H2, âm thanh không gian cá nhân hóa.</p>', 12, TRUE, 610, 12),
    ('tai-nghe', 'samsung', 'Tai nghe Samsung Galaxy Buds3', 'tai-nghe-samsung-galaxy-buds3',
     'Thiết kế mở, âm thanh AKG.', '<p>Chống ồn chủ động, pin 30 giờ cùng hộp sạc.</p>', 12, FALSE, 180, 8),
    ('loa', 'jbl', 'Loa JBL Flip 6', 'loa-jbl-flip-6',
     'Chống nước IP67, pin 12 giờ.', '<p>Âm thanh JBL Original Pro Sound, kết nối PartyBoost.</p>', 12, TRUE, 390, 15),
    ('loa', 'sony', 'Loa Sony SRS-XB13', 'loa-sony-srs-xb13',
     'Nhỏ gọn, chống nước IP67.', '<p>Extra Bass, pin 16 giờ.</p>', 12, FALSE, 75, 45),
    ('chuot', 'logitech', 'Chuột Logitech MX Master 3S', 'chuot-logitech-mx-master-3s',
     'Click yên tĩnh, cảm biến 8000 DPI.', '<p>Cuộn MagSpeed, kết nối 3 thiết bị.</p>', 12, TRUE, 430, 14),
    ('chuot', 'logitech', 'Chuột gaming Logitech G304', 'chuot-logitech-g304',
     'Không dây Lightspeed, pin 250 giờ.', '<p>Cảm biến HERO 12K, nặng 99g.</p>', 24, FALSE, 260, 50),
    ('ban-phim', 'logitech', 'Bàn phím cơ Logitech MX Mechanical Mini', 'ban-phim-logitech-mx-mechanical-mini',
     'Bàn phím cơ không dây, đèn nền thông minh.', '<p>Bố cục 75%, kết nối Bluetooth và Logi Bolt.</p>', 12, FALSE, 140, 6),
    ('op-lung', 'ugreen', 'Ốp lưng trong suốt Ugreen cho iPhone 16 Pro', 'op-lung-ugreen-iphone-16-pro',
     'Chống ố vàng, viền chống sốc.', '<p>Hỗ trợ sạc MagSafe.</p>', 0, FALSE, 60, 3),
    ('cuong-luc', 'baseus', 'Kính cường lực Baseus iPhone 16', 'kinh-cuong-luc-baseus-iphone-16',
     'Độ cứng 9H, kèm khung dán.', '<p>Phủ lớp chống bám vân tay.</p>', 0, FALSE, 40, 2)
) v(dm, th, ten, slug, mo_ta_ngan, mo_ta, bh, noi_bat, luot_xem, ngay)
JOIN danh_muc dm ON dm.slug = v.dm AND dm.deleted_at IS NULL
LEFT JOIN thuong_hieu th ON th.slug = v.th AND th.deleted_at IS NULL;

INSERT INTO thong_so (san_pham_id, ten, gia_tri, thu_tu)
SELECT sp.id, v.ten, v.gia_tri, v.thu_tu
FROM (VALUES
    ('cu-sac-anker-nano-65w', 'Công suất', '65W', 1), ('cu-sac-anker-nano-65w', 'Cổng', '1 x USB-C', 2),
    ('cu-sac-apple-usb-c-20w', 'Công suất', '20W', 1), ('cu-sac-apple-usb-c-20w', 'Cổng', '1 x USB-C', 2),
    ('cu-sac-baseus-gan5-pro-100w', 'Công suất', '100W', 1), ('cu-sac-baseus-gan5-pro-100w', 'Cổng', '2 x USB-C, 1 x USB-A', 2),
    ('cap-usb-c-ugreen-100w', 'Công suất', '100W', 1), ('cap-usb-c-ugreen-100w', 'Chất liệu', 'Bện dù', 2),
    ('cap-lightning-anker-powerline-iii', 'Chiều dài', '1m', 1), ('cap-lightning-anker-powerline-iii', 'Chuẩn', 'MFi', 2),
    ('sac-du-phong-anker-powercore-10000', 'Dung lượng', '10.000mAh', 1), ('sac-du-phong-anker-powercore-10000', 'Công suất', '20W', 2),
    ('sac-du-phong-xiaomi-20000', 'Dung lượng', '20.000mAh', 1), ('sac-du-phong-xiaomi-20000', 'Công suất', '22.5W', 2),
    ('tai-nghe-sony-wh-1000xm5', 'Kết nối', 'Bluetooth 5.2', 1), ('tai-nghe-sony-wh-1000xm5', 'Thời lượng pin', '30 giờ', 2),
    ('tai-nghe-apple-airpods-pro-2', 'Chip', 'Apple H2', 1), ('tai-nghe-apple-airpods-pro-2', 'Thời lượng pin', '6 giờ (30 giờ kèm hộp)', 2),
    ('tai-nghe-samsung-galaxy-buds3', 'Kết nối', 'Bluetooth 5.4', 1), ('tai-nghe-samsung-galaxy-buds3', 'Thời lượng pin', '30 giờ kèm hộp', 2),
    ('loa-jbl-flip-6', 'Công suất', '30W', 1), ('loa-jbl-flip-6', 'Chống nước', 'IP67', 2),
    ('loa-sony-srs-xb13', 'Thời lượng pin', '16 giờ', 1), ('loa-sony-srs-xb13', 'Chống nước', 'IP67', 2),
    ('chuot-logitech-mx-master-3s', 'DPI', '8000', 1), ('chuot-logitech-mx-master-3s', 'Kết nối', 'Bluetooth, Logi Bolt', 2),
    ('chuot-logitech-g304', 'DPI', '12000', 1), ('chuot-logitech-g304', 'Trọng lượng', '99g', 2),
    ('ban-phim-logitech-mx-mechanical-mini', 'Bố cục', '75%', 1), ('ban-phim-logitech-mx-mechanical-mini', 'Thời lượng pin', '15 ngày (bật đèn)', 2),
    ('op-lung-ugreen-iphone-16-pro', 'Chất liệu', 'TPU + PC', 1),
    ('kinh-cuong-luc-baseus-iphone-16', 'Độ cứng', '9H', 1)
) v(sp, ten, gia_tri, thu_tu)
JOIN san_pham sp ON sp.slug = v.sp AND sp.deleted_at IS NULL;

INSERT INTO tuy_chon (san_pham_id, ten, loai)
SELECT sp.id, v.ten, v.loai
FROM (VALUES
    ('cu-sac-anker-nano-65w', 'Màu sắc', 'mau_sac'),
    ('cu-sac-baseus-gan5-pro-100w', 'Màu sắc', 'mau_sac'),
    ('cap-usb-c-ugreen-100w', 'Chiều dài', 'khac'),
    ('sac-du-phong-anker-powercore-10000', 'Màu sắc', 'mau_sac'),
    ('tai-nghe-sony-wh-1000xm5', 'Màu sắc', 'mau_sac'),
    ('tai-nghe-samsung-galaxy-buds3', 'Màu sắc', 'mau_sac'),
    ('loa-jbl-flip-6', 'Màu sắc', 'mau_sac'),
    ('chuot-logitech-mx-master-3s', 'Màu sắc', 'mau_sac'),
    ('ban-phim-logitech-mx-mechanical-mini', 'Loại switch', 'phien_ban'),
    ('op-lung-ugreen-iphone-16-pro', 'Dòng máy', 'phien_ban')
) v(sp, ten, loai)
JOIN san_pham sp ON sp.slug = v.sp AND sp.deleted_at IS NULL;

INSERT INTO gia_tri_tuy_chon (tuy_chon_id, gia_tri, ma_mau, thu_tu)
SELECT tc.id, v.gia_tri, v.ma_mau, v.thu_tu
FROM (VALUES
    ('cu-sac-anker-nano-65w', 'Màu sắc', 'Đen', '#1F2937', 1), ('cu-sac-anker-nano-65w', 'Màu sắc', 'Trắng', '#F9FAFB', 2),
    ('cu-sac-baseus-gan5-pro-100w', 'Màu sắc', 'Đen', '#1F2937', 1), ('cu-sac-baseus-gan5-pro-100w', 'Màu sắc', 'Trắng', '#F9FAFB', 2),
    ('cap-usb-c-ugreen-100w', 'Chiều dài', '1m', NULL, 1), ('cap-usb-c-ugreen-100w', 'Chiều dài', '2m', NULL, 2),
    ('sac-du-phong-anker-powercore-10000', 'Màu sắc', 'Đen', '#1F2937', 1), ('sac-du-phong-anker-powercore-10000', 'Màu sắc', 'Xanh', '#2563EB', 2),
    ('tai-nghe-sony-wh-1000xm5', 'Màu sắc', 'Đen', '#111827', 1), ('tai-nghe-sony-wh-1000xm5', 'Màu sắc', 'Bạc', '#D1D5DB', 2),
    ('tai-nghe-samsung-galaxy-buds3', 'Màu sắc', 'Trắng', '#F9FAFB', 1), ('tai-nghe-samsung-galaxy-buds3', 'Màu sắc', 'Bạc', '#9CA3AF', 2),
    ('loa-jbl-flip-6', 'Màu sắc', 'Đen', '#111827', 1), ('loa-jbl-flip-6', 'Màu sắc', 'Xanh', '#1D4ED8', 2),
    ('loa-jbl-flip-6', 'Màu sắc', 'Đỏ', '#DC2626', 3),
    ('chuot-logitech-mx-master-3s', 'Màu sắc', 'Đen', '#1F2937', 1), ('chuot-logitech-mx-master-3s', 'Màu sắc', 'Xám nhạt', '#E5E7EB', 2),
    ('ban-phim-logitech-mx-mechanical-mini', 'Loại switch', 'Tactile', NULL, 1),
    ('ban-phim-logitech-mx-mechanical-mini', 'Loại switch', 'Linear', NULL, 2),
    ('op-lung-ugreen-iphone-16-pro', 'Dòng máy', 'iPhone 16 Pro', NULL, 1),
    ('op-lung-ugreen-iphone-16-pro', 'Dòng máy', 'iPhone 16 Pro Max', NULL, 2)
) v(sp, tuy_chon, gia_tri, ma_mau, thu_tu)
JOIN san_pham sp ON sp.slug = v.sp AND sp.deleted_at IS NULL
JOIN tuy_chon tc ON tc.san_pham_id = sp.id AND tc.ten = v.tuy_chon;

-- Biến thể; (tuy_chon, gia_tri) NULL = sản phẩm không có tùy chọn
WITH v(sp, sku, gia_ban, gia_goc, ton, tuy_chon, gia_tri) AS (VALUES
    ('cu-sac-anker-nano-65w', 'ANK-NANO65-DEN', 790000, 990000, 25, 'Màu sắc', 'Đen'),
    ('cu-sac-anker-nano-65w', 'ANK-NANO65-TRANG', 790000, 990000, 18, 'Màu sắc', 'Trắng'),
    ('cu-sac-apple-usb-c-20w', 'APL-20W', 490000, 550000, 40, NULL, NULL),
    ('cu-sac-baseus-gan5-pro-100w', 'BAS-GAN5-100-DEN', 1090000, NULL, 12, 'Màu sắc', 'Đen'),
    ('cu-sac-baseus-gan5-pro-100w', 'BAS-GAN5-100-TRANG', 1090000, NULL, 0, 'Màu sắc', 'Trắng'),
    ('cap-usb-c-ugreen-100w', 'UGR-CC100-1M', 159000, NULL, 60, 'Chiều dài', '1m'),
    ('cap-usb-c-ugreen-100w', 'UGR-CC100-2M', 199000, NULL, 45, 'Chiều dài', '2m'),
    ('cap-lightning-anker-powerline-iii', 'ANK-PL3-1M', 290000, 350000, 30, NULL, NULL),
    ('sac-du-phong-anker-powercore-10000', 'ANK-PC10K-DEN', 590000, 690000, 20, 'Màu sắc', 'Đen'),
    ('sac-du-phong-anker-powercore-10000', 'ANK-PC10K-XANH', 590000, 690000, 7, 'Màu sắc', 'Xanh'),
    ('sac-du-phong-xiaomi-20000', 'XMI-PB20K', 650000, NULL, 0, NULL, NULL),
    ('tai-nghe-sony-wh-1000xm5', 'SNY-XM5-DEN', 7990000, 8990000, 6, 'Màu sắc', 'Đen'),
    ('tai-nghe-sony-wh-1000xm5', 'SNY-XM5-BAC', 7990000, 8990000, 4, 'Màu sắc', 'Bạc'),
    ('tai-nghe-apple-airpods-pro-2', 'APL-APP2', 5490000, 6190000, 15, NULL, NULL),
    ('tai-nghe-samsung-galaxy-buds3', 'SSG-BUDS3-TRANG', 3490000, NULL, 10, 'Màu sắc', 'Trắng'),
    ('tai-nghe-samsung-galaxy-buds3', 'SSG-BUDS3-BAC', 3490000, NULL, 10, 'Màu sắc', 'Bạc'),
    ('loa-jbl-flip-6', 'JBL-FLIP6-DEN', 2690000, 2990000, 8, 'Màu sắc', 'Đen'),
    ('loa-jbl-flip-6', 'JBL-FLIP6-XANH', 2690000, 2990000, 5, 'Màu sắc', 'Xanh'),
    ('loa-jbl-flip-6', 'JBL-FLIP6-DO', 2690000, 2990000, 3, 'Màu sắc', 'Đỏ'),
    ('loa-sony-srs-xb13', 'SNY-XB13', 990000, NULL, 12, NULL, NULL),
    ('chuot-logitech-mx-master-3s', 'LOG-MX3S-DEN', 2290000, 2490000, 9, 'Màu sắc', 'Đen'),
    ('chuot-logitech-mx-master-3s', 'LOG-MX3S-XAM', 2290000, 2490000, 6, 'Màu sắc', 'Xám nhạt'),
    ('chuot-logitech-g304', 'LOG-G304', 790000, NULL, 30, NULL, NULL),
    ('ban-phim-logitech-mx-mechanical-mini', 'LOG-MXMINI-TAC', 3290000, NULL, 5, 'Loại switch', 'Tactile'),
    ('ban-phim-logitech-mx-mechanical-mini', 'LOG-MXMINI-LIN', 3290000, NULL, 5, 'Loại switch', 'Linear'),
    ('op-lung-ugreen-iphone-16-pro', 'UGR-OP16P', 190000, NULL, 50, 'Dòng máy', 'iPhone 16 Pro'),
    ('op-lung-ugreen-iphone-16-pro', 'UGR-OP16PM', 210000, NULL, 50, 'Dòng máy', 'iPhone 16 Pro Max'),
    ('kinh-cuong-luc-baseus-iphone-16', 'BAS-KCL16', 150000, NULL, 80, NULL, NULL)
), them AS (
    INSERT INTO bien_the (san_pham_id, sku, gia_ban, gia_goc, so_luong_ton)
    SELECT sp.id, v.sku, v.gia_ban, v.gia_goc, v.ton
    FROM v JOIN san_pham sp ON sp.slug = v.sp AND sp.deleted_at IS NULL
    RETURNING id, san_pham_id, sku
)
INSERT INTO bien_the_gia_tri (bien_the_id, gia_tri_tuy_chon_id)
SELECT them.id, gt.id
FROM them
JOIN v ON v.sku = them.sku
JOIN tuy_chon tc ON tc.san_pham_id = them.san_pham_id AND tc.ten = v.tuy_chon
JOIN gia_tri_tuy_chon gt ON gt.tuy_chon_id = tc.id AND gt.gia_tri = v.gia_tri;

-- ===== Phase 10: bổ sung sản phẩm, ảnh (Wikimedia Commons – nguồn: docs/nguon-anh-mau.md), banner, tồn kho ban đầu =====

INSERT INTO thuong_hieu (ten, slug) VALUES ('Keychron', 'keychron')
ON CONFLICT (slug) WHERE deleted_at IS NULL DO NOTHING;

INSERT INTO san_pham (danh_muc_id, thuong_hieu_id, ten, slug, mo_ta_ngan, mo_ta, bao_hanh_thang, noi_bat, luot_xem, hien_thi, created_at)
SELECT dm.id, th.id, v.ten, v.slug, v.mo_ta_ngan, v.mo_ta, v.bh, v.noi_bat, v.luot_xem, v.hien_thi, now() - make_interval(days => v.ngay)
FROM (VALUES
    ('cu-sac-samsung-25w', 'cu-sac', 'samsung', 'Củ sạc nhanh Samsung 25W',
     'Sạc siêu nhanh 25W cho điện thoại Galaxy.', '<p>Chuẩn USB PD 3.0 PPS, 1 cổng USB-C.</p>', 12, FALSE, 170, 85, TRUE),
    ('tau-sac-o-to-ugreen-30w', 'cu-sac', 'ugreen', 'Tẩu sạc ô tô Ugreen 30W',
     '2 cổng USB-C và USB-A, sạc nhanh trên xe.', '<p>Tổng công suất 30W, vỏ hợp kim nhôm tản nhiệt tốt.</p>', 18, FALSE, 65, 4, TRUE),
    ('cap-usb-c-baseus-60w', 'cap-sac', 'baseus', 'Cáp USB-C to USB-C Baseus 60W',
     'Sạc nhanh 60W, vỏ bện chống đứt.', '<p>Hỗ trợ USB PD 60W, truyền dữ liệu 480Mbps.</p>', 12, FALSE, 95, 70, TRUE),
    ('cap-usb-a-usb-c-ugreen-3a', 'cap-sac', 'ugreen', 'Cáp USB-A to USB-C Ugreen 3A',
     'Dùng với củ sạc USB-A, dòng 3A.', '<p>Đầu nối mạ niken chống oxy hóa, dài 1m.</p>', 18, FALSE, 140, 95, TRUE),
    ('sac-du-phong-samsung-eb-p1100c', 'sac-du-phong', 'samsung', 'Sạc dự phòng Samsung EB-P1100C 10000mAh',
     'Mỏng nhẹ, vỏ kim loại, sạc nhanh 15W.', '<p>1 cổng USB-C và 1 cổng USB-A, sạc 2 thiết bị cùng lúc.</p>', 6, FALSE, 110, 90, TRUE),
    ('sac-du-phong-xiaomi-pro-10000', 'sac-du-phong', 'xiaomi', 'Sạc dự phòng Xiaomi Mi Power Bank Pro 10000mAh',
     'Vỏ nhôm nguyên khối, sạc nhanh 18W.', '<p>Hỗ trợ Quick Charge 3.0 và USB PD.</p>', 6, FALSE, 85, 65, TRUE),
    ('tai-nghe-sony-wh-ch510', 'tai-nghe', 'sony', 'Tai nghe Sony WH-CH510',
     'Nhẹ 132g, pin 35 giờ.', '<p>Bluetooth 5.0, sạc USB-C. Đã ngừng kinh doanh.</p>', 12, FALSE, 230, 100, FALSE),
    ('tai-nghe-samsung-galaxy-buds2-pro', 'tai-nghe', 'samsung', 'Tai nghe Samsung Galaxy Buds2 Pro',
     'Chống ồn thông minh, âm thanh 24-bit Hi-Fi.', '<p>Chống nước IPX7, kết nối liền mạch với thiết bị Galaxy.</p>', 12, TRUE, 350, 75, TRUE),
    ('loa-jbl-charge-4', 'loa', 'jbl', 'Loa JBL Charge 4',
     'Pin 20 giờ, kiêm sạc dự phòng.', '<p>Chống nước IPX7, công suất 30W.</p>', 12, FALSE, 260, 80, TRUE),
    ('loa-jbl-go-2', 'loa', 'jbl', 'Loa JBL Go 2',
     'Siêu nhỏ gọn, chống nước IPX7.', '<p>Pin 5 giờ, có micro đàm thoại.</p>', 6, FALSE, 190, 60, TRUE),
    ('chuot-logitech-m331-silent-plus', 'chuot', 'logitech', 'Chuột không dây Logitech M331 Silent Plus',
     'Giảm 90% tiếng click, pin 24 tháng.', '<p>Đầu thu USB nano, cảm biến 1000 DPI.</p>', 12, FALSE, 300, 88, TRUE),
    ('chuot-logitech-m705-marathon', 'chuot', 'logitech', 'Chuột không dây Logitech M705 Marathon',
     'Pin tới 3 năm, cuộn siêu nhanh.', '<p>Thiết kế công thái học cho tay phải, 7 nút tùy chỉnh.</p>', 12, FALSE, 120, 5, TRUE),
    ('ban-phim-logitech-k380', 'ban-phim', 'logitech', 'Bàn phím Bluetooth Logitech K380',
     'Kết nối 3 thiết bị, chuyển đổi bằng 1 phím.', '<p>Nhỏ gọn, pin 2 năm, hỗ trợ Windows, macOS, iOS, Android.</p>', 12, TRUE, 280, 92, TRUE),
    ('ban-phim-co-keychron-k8', 'ban-phim', 'keychron', 'Bàn phím cơ Keychron K8',
     'Bố cục TKL 87 phím, Bluetooth 5.1.', '<p>Hot-swap, tương thích macOS và Windows.</p>', 12, TRUE, 410, 1, TRUE),
    ('op-lung-apple-silicone-iphone-17', 'op-lung', 'apple', 'Ốp lưng Silicone MagSafe Apple cho iPhone 17',
     'Chính hãng Apple, lớp lót microfiber.', '<p>Nam châm MagSafe tích hợp, ôm sát thân máy.</p>', 0, FALSE, 150, 7, TRUE),
    ('op-lung-samsung-galaxy-s22-ultra-tpu', 'op-lung', 'samsung', 'Ốp lưng TPU trong suốt Samsung Galaxy S22 Ultra',
     'Dẻo, mỏng, giữ nguyên thiết kế máy.', '<p>Gờ bảo vệ camera và màn hình.</p>', 0, FALSE, 45, 98, TRUE),
    ('mieng-dan-chong-nhin-trom-iphone', 'cuong-luc', 'ugreen', 'Kính cường lực chống nhìn trộm Ugreen iPhone 16',
     'Góc nhìn 60°, bảo vệ quyền riêng tư.', '<p>Độ cứng 9H, phủ lớp chống bám vân tay.</p>', 0, FALSE, 70, 55, TRUE),
    ('kinh-cuong-luc-samsung-galaxy-a04e', 'cuong-luc', 'samsung', 'Kính cường lực Samsung Galaxy A04e',
     'Full màn hình, độ cứng 9H.', '<p>Viền đen tràn cạnh, kèm khăn lau.</p>', 0, FALSE, 30, 62, TRUE)
) v(slug, dm, th, ten, mo_ta_ngan, mo_ta, bh, noi_bat, luot_xem, ngay, hien_thi)
JOIN danh_muc dm ON dm.slug = v.dm AND dm.deleted_at IS NULL
LEFT JOIN thuong_hieu th ON th.slug = v.th AND th.deleted_at IS NULL;

INSERT INTO thong_so (san_pham_id, ten, gia_tri, thu_tu)
SELECT sp.id, v.ten, v.gia_tri, v.thu_tu
FROM (VALUES
    ('cu-sac-samsung-25w', 'Công suất', '25W', 1),
    ('cu-sac-samsung-25w', 'Cổng', '1 x USB-C', 2),
    ('tau-sac-o-to-ugreen-30w', 'Công suất', '30W', 1),
    ('tau-sac-o-to-ugreen-30w', 'Cổng', '1 x USB-C, 1 x USB-A', 2),
    ('cap-usb-c-baseus-60w', 'Công suất', '60W', 1),
    ('cap-usb-c-baseus-60w', 'Chất liệu', 'Bện dù', 2),
    ('cap-usb-a-usb-c-ugreen-3a', 'Dòng điện', '3A', 1),
    ('cap-usb-a-usb-c-ugreen-3a', 'Chiều dài', '1m', 2),
    ('sac-du-phong-samsung-eb-p1100c', 'Dung lượng', '10.000mAh', 1),
    ('sac-du-phong-samsung-eb-p1100c', 'Công suất', '15W', 2),
    ('sac-du-phong-xiaomi-pro-10000', 'Dung lượng', '10.000mAh', 1),
    ('sac-du-phong-xiaomi-pro-10000', 'Công suất', '18W', 2),
    ('tai-nghe-sony-wh-ch510', 'Kết nối', 'Bluetooth 5.0', 1),
    ('tai-nghe-sony-wh-ch510', 'Thời lượng pin', '35 giờ', 2),
    ('tai-nghe-samsung-galaxy-buds2-pro', 'Kết nối', 'Bluetooth 5.3', 1),
    ('tai-nghe-samsung-galaxy-buds2-pro', 'Chống nước', 'IPX7', 2),
    ('loa-jbl-charge-4', 'Công suất', '30W', 1),
    ('loa-jbl-charge-4', 'Thời lượng pin', '20 giờ', 2),
    ('loa-jbl-go-2', 'Công suất', '3W', 1),
    ('loa-jbl-go-2', 'Chống nước', 'IPX7', 2),
    ('chuot-logitech-m331-silent-plus', 'DPI', '1000', 1),
    ('chuot-logitech-m331-silent-plus', 'Kết nối', 'USB nano 2.4GHz', 2),
    ('chuot-logitech-m705-marathon', 'DPI', '1000', 1),
    ('chuot-logitech-m705-marathon', 'Thời lượng pin', '3 năm', 2),
    ('ban-phim-logitech-k380', 'Kết nối', 'Bluetooth', 1),
    ('ban-phim-logitech-k380', 'Thời lượng pin', '2 năm', 2),
    ('ban-phim-co-keychron-k8', 'Bố cục', 'TKL 87 phím', 1),
    ('ban-phim-co-keychron-k8', 'Kết nối', 'Bluetooth 5.1, USB-C', 2),
    ('op-lung-apple-silicone-iphone-17', 'Chất liệu', 'Silicone', 1),
    ('op-lung-samsung-galaxy-s22-ultra-tpu', 'Chất liệu', 'TPU', 1),
    ('mieng-dan-chong-nhin-trom-iphone', 'Độ cứng', '9H', 1),
    ('kinh-cuong-luc-samsung-galaxy-a04e', 'Độ cứng', '9H', 1)
) v(sp, ten, gia_tri, thu_tu)
JOIN san_pham sp ON sp.slug = v.sp AND sp.deleted_at IS NULL;

INSERT INTO tuy_chon (san_pham_id, ten, loai)
SELECT sp.id, v.ten, v.loai
FROM (VALUES
    ('cap-usb-c-baseus-60w', 'Chiều dài', 'khac'),
    ('tai-nghe-samsung-galaxy-buds2-pro', 'Màu sắc', 'mau_sac'),
    ('loa-jbl-charge-4', 'Màu sắc', 'mau_sac'),
    ('loa-jbl-go-2', 'Màu sắc', 'mau_sac'),
    ('chuot-logitech-m331-silent-plus', 'Màu sắc', 'mau_sac'),
    ('ban-phim-co-keychron-k8', 'Loại switch', 'phien_ban')
) v(sp, ten, loai)
JOIN san_pham sp ON sp.slug = v.sp AND sp.deleted_at IS NULL;

INSERT INTO gia_tri_tuy_chon (tuy_chon_id, gia_tri, ma_mau, thu_tu)
SELECT tc.id, v.gia_tri, v.ma_mau, v.thu_tu
FROM (VALUES
    ('cap-usb-c-baseus-60w', 'Chiều dài', '1m', NULL, 1),
    ('cap-usb-c-baseus-60w', 'Chiều dài', '2m', NULL, 2),
    ('tai-nghe-samsung-galaxy-buds2-pro', 'Màu sắc', 'Trắng', '#F9FAFB', 1),
    ('tai-nghe-samsung-galaxy-buds2-pro', 'Màu sắc', 'Đen', '#111827', 2),
    ('loa-jbl-charge-4', 'Màu sắc', 'Đen', '#111827', 1),
    ('loa-jbl-charge-4', 'Màu sắc', 'Xanh', '#1D4ED8', 2),
    ('loa-jbl-go-2', 'Màu sắc', 'Xanh', '#1E40AF', 1),
    ('loa-jbl-go-2', 'Màu sắc', 'Đen', '#111827', 2),
    ('loa-jbl-go-2', 'Màu sắc', 'Đỏ', '#DC2626', 3),
    ('chuot-logitech-m331-silent-plus', 'Màu sắc', 'Đen', '#1F2937', 1),
    ('chuot-logitech-m331-silent-plus', 'Màu sắc', 'Đỏ', '#DC2626', 2),
    ('ban-phim-co-keychron-k8', 'Loại switch', 'Red', NULL, 1),
    ('ban-phim-co-keychron-k8', 'Loại switch', 'Brown', NULL, 2)
) v(sp, tuy_chon, gia_tri, ma_mau, thu_tu)
JOIN san_pham sp ON sp.slug = v.sp AND sp.deleted_at IS NULL
JOIN tuy_chon tc ON tc.san_pham_id = sp.id AND tc.ten = v.tuy_chon;

-- Biến thể mới; gia_tri NULL = sản phẩm không có tùy chọn (mỗi sản phẩm mới có tối đa 1 tùy chọn)
WITH v(sp, sku, gia_ban, gia_goc, ton, gia_tri) AS (VALUES
    ('cu-sac-samsung-25w', 'SSG-25W', 390000, 450000, 40, NULL),
    ('tau-sac-o-to-ugreen-30w', 'UGR-CAR30', 290000, NULL::bigint, 25, NULL),
    ('cap-usb-c-baseus-60w', 'BAS-CC60-1M', 129000, NULL::bigint, 60, '1m'),
    ('cap-usb-c-baseus-60w', 'BAS-CC60-2M', 159000, NULL::bigint, 40, '2m'),
    ('cap-usb-a-usb-c-ugreen-3a', 'UGR-AC3A', 99000, 129000, 80, NULL),
    ('sac-du-phong-samsung-eb-p1100c', 'SSG-PB10K', 690000, 790000, 20, NULL),
    ('sac-du-phong-xiaomi-pro-10000', 'XMI-PB10K-PRO', 550000, NULL::bigint, 25, NULL),
    ('tai-nghe-sony-wh-ch510', 'SNY-CH510', 990000, 1290000, 10, NULL),
    ('tai-nghe-samsung-galaxy-buds2-pro', 'SSG-BUDS2P-TRANG', 3990000, 4490000, 10, 'Trắng'),
    ('tai-nghe-samsung-galaxy-buds2-pro', 'SSG-BUDS2P-DEN', 3990000, 4490000, 6, 'Đen'),
    ('loa-jbl-charge-4', 'JBL-CHG4-DEN', 2490000, 2990000, 15, 'Đen'),
    ('loa-jbl-charge-4', 'JBL-CHG4-XANH', 2490000, 2990000, 8, 'Xanh'),
    ('loa-jbl-go-2', 'JBL-GO2-XANH', 690000, 790000, 6, 'Xanh'),
    ('loa-jbl-go-2', 'JBL-GO2-DEN', 690000, 790000, 15, 'Đen'),
    ('loa-jbl-go-2', 'JBL-GO2-DO', 690000, 790000, 2, 'Đỏ'),
    ('chuot-logitech-m331-silent-plus', 'LOG-M331-DEN', 390000, 450000, 50, 'Đen'),
    ('chuot-logitech-m331-silent-plus', 'LOG-M331-DO', 390000, 450000, 20, 'Đỏ'),
    ('chuot-logitech-m705-marathon', 'LOG-M705', 990000, NULL::bigint, 15, NULL),
    ('ban-phim-logitech-k380', 'LOG-K380', 790000, 890000, 30, NULL),
    ('ban-phim-co-keychron-k8', 'KEY-K8-RED', 2190000, NULL::bigint, 12, 'Red'),
    ('ban-phim-co-keychron-k8', 'KEY-K8-BROWN', 2190000, NULL::bigint, 10, 'Brown'),
    ('op-lung-apple-silicone-iphone-17', 'APL-SC17', 1190000, 1290000, 20, NULL),
    ('op-lung-samsung-galaxy-s22-ultra-tpu', 'SSG-S22U-TPU', 250000, NULL::bigint, 30, NULL),
    ('mieng-dan-chong-nhin-trom-iphone', 'UGR-PRIV16', 290000, NULL::bigint, 45, NULL),
    ('kinh-cuong-luc-samsung-galaxy-a04e', 'SSG-KCL-A04E', 120000, NULL::bigint, 60, NULL)
), them AS (
    INSERT INTO bien_the (san_pham_id, sku, gia_ban, gia_goc, so_luong_ton, created_at)
    SELECT sp.id, v.sku, v.gia_ban, v.gia_goc, v.ton, sp.created_at
    FROM v JOIN san_pham sp ON sp.slug = v.sp AND sp.deleted_at IS NULL
    RETURNING id, san_pham_id, sku
)
INSERT INTO bien_the_gia_tri (bien_the_id, gia_tri_tuy_chon_id)
SELECT them.id, gt.id
FROM them
JOIN v ON v.sku = them.sku
JOIN tuy_chon tc ON tc.san_pham_id = them.san_pham_id
JOIN gia_tri_tuy_chon gt ON gt.tuy_chon_id = tc.id AND gt.gia_tri = v.gia_tri;

-- Ảnh sản phẩm (ảnh số 1 là ảnh chính)
INSERT INTO hinh_anh_san_pham (san_pham_id, duong_dan, mo_ta, thu_tu, la_anh_chinh)
SELECT sp.id, v.duong_dan, sp.ten, v.thu_tu, v.thu_tu = 1
FROM (VALUES
    ('cu-sac-anker-nano-65w', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4a/Silicon_vs_GaN_30W_USB-C_chargers.jpg/960px-Silicon_vs_GaN_30W_USB-C_chargers.jpg'),
    ('cu-sac-apple-usb-c-20w', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6b/Apple_USB-C_%28USB_Type_C%29_87W_Power_Adapter_for_MacBook_Pro_%2848979218053%29.jpg/960px-Apple_USB-C_%28USB_Type_C%29_87W_Power_Adapter_for_MacBook_Pro_%2848979218053%29.jpg'),
    ('cu-sac-baseus-gan5-pro-100w', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/0c/Baseus_USB_chargers-1.jpg/960px-Baseus_USB_chargers-1.jpg'),
    ('cu-sac-baseus-gan5-pro-100w', 2, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/3d/Baseus_USB_chargers-0.jpg/960px-Baseus_USB_chargers-0.jpg'),
    ('cap-usb-c-ugreen-100w', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c3/USB-C_cable_2017_A.jpg/960px-USB-C_cable_2017_A.jpg'),
    ('cap-lightning-anker-powerline-iii', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/d/df/Lightning_connector.jpg/960px-Lightning_connector.jpg'),
    ('sac-du-phong-anker-powercore-10000', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/ee/Anker_power_bank_and_cable.jpg/960px-Anker_power_bank_and_cable.jpg'),
    ('sac-du-phong-anker-powercore-10000', 2, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4e/Anker_power_bank_lit.jpg/960px-Anker_power_bank_lit.jpg'),
    ('sac-du-phong-xiaomi-20000', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5b/Xiaomi_PowerBank.jpg/960px-Xiaomi_PowerBank.jpg'),
    ('tai-nghe-sony-wh-1000xm5', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b2/Sony_Headphones_%2840476165073%29.jpg/960px-Sony_Headphones_%2840476165073%29.jpg'),
    ('tai-nghe-apple-airpods-pro-2', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/2f/AirPods_Pro_%282nd_generation%29.jpg/960px-AirPods_Pro_%282nd_generation%29.jpg'),
    ('tai-nghe-apple-airpods-pro-2', 2, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4c/AirPods_Pro_case.jpg/960px-AirPods_Pro_case.jpg'),
    ('tai-nghe-apple-airpods-pro-2', 3, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/97/AirPods_Pro_2.jpg/960px-AirPods_Pro_2.jpg'),
    ('tai-nghe-samsung-galaxy-buds3', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a4/Samsung_Galaxy_Buds.jpg/960px-Samsung_Galaxy_Buds.jpg'),
    ('loa-jbl-flip-6', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f9/JBL_Flip_bluetooth_speaker.jpg/960px-JBL_Flip_bluetooth_speaker.jpg'),
    ('loa-jbl-flip-6', 2, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a2/JBL_Flip_4.jpg/960px-JBL_Flip_4.jpg'),
    ('loa-sony-srs-xb13', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9a/SONY_SRS-XB2_01.JPG/960px-SONY_SRS-XB2_01.JPG'),
    ('chuot-logitech-mx-master-3s', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/69/Logitech_MX_Master_3S_HS13.jpg/960px-Logitech_MX_Master_3S_HS13.jpg'),
    ('chuot-logitech-mx-master-3s', 2, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/2/21/Logitech_MX_Master_3S_HS10.jpg/960px-Logitech_MX_Master_3S_HS10.jpg'),
    ('chuot-logitech-mx-master-3s', 3, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/90/Logitech_MX_Master_3S_HS01.jpg/960px-Logitech_MX_Master_3S_HS01.jpg'),
    ('chuot-logitech-g304', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c9/Logitech%28G703%29_mouse.jpg/960px-Logitech%28G703%29_mouse.jpg'),
    ('ban-phim-logitech-mx-mechanical-mini', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7f/Beautiful_Mechanical_Keyboard.jpg/960px-Beautiful_Mechanical_Keyboard.jpg'),
    ('op-lung-ugreen-iphone-16-pro', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f3/IPhone_case_wikipedia.jpg/960px-IPhone_case_wikipedia.jpg'),
    ('kinh-cuong-luc-baseus-iphone-16', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c4/White_iPhone_4S_with_screen_protector_vs_iPhone_5S.jpg/960px-White_iPhone_4S_with_screen_protector_vs_iPhone_5S.jpg'),
    ('cu-sac-samsung-25w', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f5/Samsung_Charger.jpg/960px-Samsung_Charger.jpg'),
    ('tau-sac-o-to-ugreen-30w', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/92/Ugreen_car_charger_USB-C_USB-A_30W_25845.jpg/960px-Ugreen_car_charger_USB-C_USB-A_30W_25845.jpg'),
    ('cap-usb-c-baseus-60w', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5c/Two_USB-C_connectors_being_held.jpg/960px-Two_USB-C_connectors_being_held.jpg'),
    ('cap-usb-a-usb-c-ugreen-3a', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/5f/2026-08-22_UGreen-USB-C-Kabel_HOF1918_RAW-Export.png/960px-2026-08-22_UGreen-USB-C-Kabel_HOF1918_RAW-Export.png'),
    ('sac-du-phong-samsung-eb-p1100c', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e9/SAMSUNG_BATTERY_PACK_%28POWER_BANK%29_EB-P1100C.jpg/960px-SAMSUNG_BATTERY_PACK_%28POWER_BANK%29_EB-P1100C.jpg'),
    ('sac-du-phong-samsung-eb-p1100c', 2, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/1e/SAMSUNG_BATTERY_PACK_%28POWER_BANK%29_EB-P1100C_%282%29.jpg/960px-SAMSUNG_BATTERY_PACK_%28POWER_BANK%29_EB-P1100C_%282%29.jpg'),
    ('sac-du-phong-xiaomi-pro-10000', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e8/MiPowerBankPro10000mAhQC30Xiaomi20240820000.jpg/960px-MiPowerBankPro10000mAhQC30Xiaomi20240820000.jpg'),
    ('sac-du-phong-xiaomi-pro-10000', 2, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/50/MiPowerBankPro10000mAhQC30Xiaomi20240820001.jpg/960px-MiPowerBankPro10000mAhQC30Xiaomi20240820001.jpg'),
    ('tai-nghe-sony-wh-ch510', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/01/Sony_WH-CH510_Bluetooth_Over-Ear_Headphone.jpg/960px-Sony_WH-CH510_Bluetooth_Over-Ear_Headphone.jpg'),
    ('tai-nghe-samsung-galaxy-buds2-pro', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9e/Samsung_Galaxy_Buds_FE_and_Samsung_Galaxy_Buds2_Pro.jpg/960px-Samsung_Galaxy_Buds_FE_and_Samsung_Galaxy_Buds2_Pro.jpg'),
    ('loa-jbl-charge-4', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/33/JBL_Charge_4_1v2.jpg/960px-JBL_Charge_4_1v2.jpg'),
    ('loa-jbl-charge-4', 2, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/ef/JBL_Charge_4_2v2.jpg/960px-JBL_Charge_4_2v2.jpg'),
    ('loa-jbl-go-2', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/9/9c/JBL_GO2_Bluetooth_speaker.jpeg/960px-JBL_GO2_Bluetooth_speaker.jpeg'),
    ('loa-jbl-go-2', 2, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/32/JBL_GO2_Bluetooth_speaker_00_10_27_681000_%28turned_upright%29.jpeg/960px-JBL_GO2_Bluetooth_speaker_00_10_27_681000_%28turned_upright%29.jpeg'),
    ('chuot-logitech-m331-silent-plus', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/3/39/Logitech_M331_Silent_Mouse.jpg/960px-Logitech_M331_Silent_Mouse.jpg'),
    ('chuot-logitech-m705-marathon', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8a/2023_Mysz_komputerowa_Logitech_M705.jpg/960px-2023_Mysz_komputerowa_Logitech_M705.jpg'),
    ('ban-phim-logitech-k380', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/e/e6/Logitech_K380_CJK_Keyboard.jpg/960px-Logitech_K380_CJK_Keyboard.jpg'),
    ('ban-phim-co-keychron-k8', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/fb/Keychron_K8_Non-Backlight_Wireless_Mechanical_Keyboard.jpg/960px-Keychron_K8_Non-Backlight_Wireless_Mechanical_Keyboard.jpg'),
    ('ban-phim-co-keychron-k8', 2, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/8/8d/Keychron_K8_Non-Backlight_Wireless_Mechanical_Keyboard_-_52771896827.jpg/960px-Keychron_K8_Non-Backlight_Wireless_Mechanical_Keyboard_-_52771896827.jpg'),
    ('op-lung-apple-silicone-iphone-17', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/0/07/Apple_iPhone_17_Silicone_Case_%28Black%29.jpg/960px-Apple_iPhone_17_Silicone_Case_%28Black%29.jpg'),
    ('op-lung-apple-silicone-iphone-17', 2, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b7/Apple_iPhone_17_Silicone_Case_%28Black%29_rear.jpg/960px-Apple_iPhone_17_Silicone_Case_%28Black%29_rear.jpg'),
    ('op-lung-samsung-galaxy-s22-ultra-tpu', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/78/SAMSUNG_Galaxy_S22_Ultra_TPU_CASE.jpg/960px-SAMSUNG_Galaxy_S22_Ultra_TPU_CASE.jpg'),
    ('op-lung-samsung-galaxy-s22-ultra-tpu', 2, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/4/4e/SAMSUNG_Galaxy_S22_Ultra_TPU_CASE_%282%29.jpg/960px-SAMSUNG_Galaxy_S22_Ultra_TPU_CASE_%282%29.jpg'),
    ('mieng-dan-chong-nhin-trom-iphone', 1, 'https://upload.wikimedia.org/wikipedia/commons/f/f0/%D9%85%D8%AD%D8%A7%D9%81%D8%B8_%D8%B5%D9%81%D8%AD%D9%87_%D9%86%D9%85%D8%A7%DB%8C%D8%B4_%D8%AD%D8%B1%DB%8C%D9%85_%D8%B4%D8%AE%D8%B5%DB%8C.jpg'),
    ('kinh-cuong-luc-samsung-galaxy-a04e', 1, 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6a/Samsung_Galaxy_A04e_front_with_screen_protector.jpg/960px-Samsung_Galaxy_A04e_front_with_screen_protector.jpg')
) v(sp, thu_tu, duong_dan)
JOIN san_pham sp ON sp.slug = v.sp AND sp.deleted_at IS NULL;

UPDATE thuong_hieu th SET logo = v.logo
FROM (VALUES
    ('anker', 'https://upload.wikimedia.org/wikipedia/commons/a/a4/Anker_Innovations_Co._Ltd._Logo.png'),
    ('apple', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/fa/Apple_logo_black.svg/960px-Apple_logo_black.svg.png'),
    ('jbl', 'https://upload.wikimedia.org/wikipedia/commons/d/d2/Jbl-logo.jpg'),
    ('logitech', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/17/Logitech_logo.svg/960px-Logitech_logo.svg.png'),
    ('samsung', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/b/b4/Samsung_wordmark.svg/960px-Samsung_wordmark.svg.png'),
    ('sony', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/ca/Sony_logo.svg/960px-Sony_logo.svg.png'),
    ('ugreen', 'https://upload.wikimedia.org/wikipedia/commons/7/75/UGREEN_wordmark.png'),
    ('xiaomi', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/ae/Xiaomi_logo_%282021-%29.svg/960px-Xiaomi_logo_%282021-%29.svg.png'),
    ('keychron', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/69/Keychron_logo.svg/960px-Keychron_logo.svg.png')
) v(slug, logo)
WHERE th.slug = v.slug AND th.deleted_at IS NULL;

UPDATE danh_muc dm SET hinh_anh = v.hinh_anh
FROM (VALUES
    ('sac-cap', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/53/Charging_a_smartphone_with_a_USB_power_bank.jpg/960px-Charging_a_smartphone_with_a_USB_power_bank.jpg'),
    ('am-thanh', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/f/f6/Headphones_on_desk.jpg/960px-Headphones_on_desk.jpg'),
    ('phu-kien-may-tinh', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/c/c6/Black_and_white_computer_desk_%28Unsplash%29.jpg/960px-Black_and_white_computer_desk_%28Unsplash%29.jpg'),
    ('op-lung-bao-ve', 'https://thumb.wikimedia.org/wikipedia/commons/thumb/a/a1/Apple_iPhone_17_Silicone_Case_%28Black%29_unboxing.jpg/960px-Apple_iPhone_17_Silicone_Case_%28Black%29_unboxing.jpg')
) v(slug, hinh_anh)
WHERE dm.slug = v.slug AND dm.deleted_at IS NULL;

-- Banner: 3 đang chạy, 1 đã hết hạn (không hiện ở trang chủ, vẫn thấy trong trang quản trị)
INSERT INTO banner (tieu_de, hinh_anh, lien_ket, thu_tu, bat_dau_luc, ket_thuc_luc)
VALUES
    ('Âm thanh đỉnh cao – giảm đến 20%',
     'https://thumb.wikimedia.org/wikipedia/commons/thumb/5/55/Top_view_of_headphones_hanging_from_a_desk_%28Unsplash%29.jpg/1920px-Top_view_of_headphones_hanging_from_a_desk_%28Unsplash%29.jpg',
     '/san-pham?danhMuc=am-thanh', 1, now() - make_interval(days => 10), now() + make_interval(days => 60)),
    ('Phụ kiện chính hãng, bảo hành tận nơi',
     'https://thumb.wikimedia.org/wikipedia/commons/thumb/1/11/Xiaomi_Store_Portugal.jpg/1920px-Xiaomi_Store_Portugal.jpg',
     '/san-pham', 2, now() - make_interval(days => 30), NULL),
    ('Bàn phím cơ mới về – Keychron K8',
     'https://thumb.wikimedia.org/wikipedia/commons/thumb/7/7b/Mechanical_Keyboard_Exhibition.jpg/1920px-Mechanical_Keyboard_Exhibition.jpg',
     '/san-pham/ban-phim-co-keychron-k8', 3, now() - make_interval(days => 1), now() + make_interval(days => 30)),
    ('Sale tai nghe tháng trước (đã kết thúc)',
     'https://thumb.wikimedia.org/wikipedia/commons/thumb/6/6b/Desk-music-headphones-earphones_%2824243083451%29.jpg/1920px-Desk-music-headphones-earphones_%2824243083451%29.jpg',
     '/san-pham?danhMuc=tai-nghe', 4, now() - make_interval(days => 60), now() + make_interval(days => -30));

-- Biến thể tạo cùng lúc với sản phẩm (khớp mốc 'khoi_tao' bên dưới)
UPDATE bien_the bt SET created_at = sp.created_at, updated_at = sp.created_at
FROM san_pham sp
WHERE sp.id = bt.san_pham_id;

-- Tồn kho ban đầu của mọi biến thể (đơn mẫu ở R__du_lieu_mau_van_hanh.sql trừ tiếp từ mốc này)
INSERT INTO lich_su_ton_kho (bien_the_id, loai, so_luong_thay_doi, so_luong_sau, ghi_chu, nguoi_thuc_hien_id, created_at)
SELECT bt.id, 'khoi_tao', bt.so_luong_ton, bt.so_luong_ton, 'Tồn kho ban đầu',
       (SELECT id FROM nguoi_dung WHERE email = 'admin@techgear.vn'), sp.created_at
FROM bien_the bt
JOIN san_pham sp ON sp.id = bt.san_pham_id
WHERE bt.so_luong_ton > 0;

-- Giá hiển thị = biến thể đang bán rẻ nhất
UPDATE san_pham sp
SET gia_ban = r.gia_ban, gia_goc = r.gia_goc
FROM (SELECT DISTINCT ON (san_pham_id) san_pham_id, gia_ban, gia_goc
      FROM bien_the WHERE hien_thi AND deleted_at IS NULL
      ORDER BY san_pham_id, gia_ban, id) r
WHERE r.san_pham_id = sp.id;
END
$$;
