-- Dữ liệu mẫu danh mục, thương hiệu, sản phẩm – chỉ profile dev, chỉ nạp khi chưa có sản phẩm nào
-- (Flyway chạy lại file khi đổi nội dung; muốn nạp lại từ đầu: docker compose down -v).
-- Chưa có ảnh: frontend hiện "Chưa có ảnh". Dữ liệu mẫu đầy đủ (ảnh, banner) làm ở Phase 10.

DO $$
BEGIN
IF EXISTS (SELECT 1 FROM san_pham) THEN
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

-- Giá hiển thị = biến thể đang bán rẻ nhất
UPDATE san_pham sp
SET gia_ban = r.gia_ban, gia_goc = r.gia_goc
FROM (SELECT DISTINCT ON (san_pham_id) san_pham_id, gia_ban, gia_goc
      FROM bien_the WHERE hien_thi AND deleted_at IS NULL
      ORDER BY san_pham_id, gia_ban, id) r
WHERE r.san_pham_id = sp.id;
END
$$;
