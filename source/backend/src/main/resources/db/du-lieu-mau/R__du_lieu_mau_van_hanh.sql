-- Dữ liệu mẫu vận hành – chỉ profile dev: khách hàng, đơn hàng (đủ trạng thái, COD/VNPay), thanh toán, lịch sử đơn/kho,
-- đánh giá, giỏ hàng, yêu thích. Chạy sau R__du_lieu_mau_san_pham.sql (Flyway chạy file R__ theo thứ tự tên).
-- Chỉ nạp khi chưa có đơn hàng nào; muốn nạp lại từ đầu: docker compose down -v.
-- Khách mẫu: mật khẩu chung Khach@123; ngo.long@example.com bị khóa.
-- Mã giảm giá đọc từ R__du_lieu_mau_khuyen_mai.sql (khung giờ tính theo now() nên đơn mẫu luôn áp được).
-- Đơn tính tiền theo đúng quy tắc backend (giá biến thể, mã giảm giá, phí vận chuyển trong cai_dat) và
-- trừ/hoàn kho nối tiếp từ dòng 'khoi_tao'; cuối file tự kiểm, sai thì dừng khởi động (RAISE EXCEPTION).

INSERT INTO nguoi_dung (ho_ten, email, mat_khau, so_dien_thoai, gioi_tinh, ngay_sinh, created_at)
SELECT v.ho_ten, v.email, '$2a$12$JZZeyeYy71xijX5IowHhU.GUbCQe.OvmO9g38yyCeM6PlEGC/3Zp2', v.sdt, v.gt, v.ns::date,
       now() - make_interval(days => v.ngay)
FROM (VALUES ('Nguyễn Thị Lan', 'nguyen.lan@example.com', '0912345601', 'nu', '1998-03-12', 120),
             ('Trần Quang Minh', 'tran.minh@example.com', '0912345602', 'nam', '1995-07-08', 118),
             ('Lê Thu Hương', 'le.huong@example.com', '0912345603', 'nu', '2001-11-20', 115),
             ('Phạm Anh Đức', 'pham.duc@example.com', '0912345604', 'nam', '1993-01-30', 112),
             ('Võ Ngọc Mai', 'vo.mai@example.com', '0912345605', 'nu', '2000-05-05', 110),
             ('Đặng Minh Khoa', 'dang.khoa@example.com', '0912345606', 'nam', '1997-09-14', 105),
             ('Bùi Phương Thảo', 'bui.thao@example.com', '0912345607', 'nu', '1999-12-01', 100),
             ('Hoàng Văn Tuấn', 'hoang.tuan@example.com', '0912345608', 'nam', '1990-04-22', 96),
             ('Đỗ Bảo Ngọc', 'do.ngoc@example.com', '0912345609', 'nu', '2002-08-18', 90),
             ('Ngô Thành Long', 'ngo.long@example.com', '0912345610', 'nam', '1996-02-10', 80)
     ) v(ho_ten, email, sdt, gt, ns, ngay)
ON CONFLICT ((lower(email))) DO NOTHING;

INSERT INTO dia_chi (nguoi_dung_id, ten_nguoi_nhan, so_dien_thoai, tinh_thanh, phuong_xa, dia_chi_chi_tiet, la_mac_dinh)
SELECT n.id, n.ho_ten, n.so_dien_thoai, v.tinh, v.phuong, v.chi_tiet, TRUE
FROM (VALUES ('nguyen.lan@example.com', 'Thành phố Hà Nội', 'Phường Cầu Giấy', '25 Trần Thái Tông'),
             ('tran.minh@example.com', 'Thành phố Hồ Chí Minh', 'Phường Bến Thành', '88 Lê Lợi'),
             ('le.huong@example.com', 'Thành phố Đà Nẵng', 'Phường Hải Châu', '15 Bạch Đằng'),
             ('pham.duc@example.com', 'Thành phố Cần Thơ', 'Phường Ninh Kiều', '42 Hai Bà Trưng'),
             ('vo.mai@example.com', 'Thành phố Hồ Chí Minh', 'Phường Tân Định', '120 Hai Bà Trưng'),
             ('dang.khoa@example.com', 'Thành phố Hải Phòng', 'Phường Hồng Bàng', '7 Điện Biên Phủ'),
             ('bui.thao@example.com', 'Thành phố Huế', 'Phường Thuận Hóa', '30 Lê Lợi'),
             ('hoang.tuan@example.com', 'Thành phố Hà Nội', 'Phường Ba Đình', '12 Phan Đình Phùng'),
             ('do.ngoc@example.com', 'Thành phố Hồ Chí Minh', 'Phường Thủ Đức', '5 Võ Văn Ngân'),
             ('ngo.long@example.com', 'Thành phố Hà Nội', 'Phường Hoàn Kiếm', '9 Hàng Bài')
     ) v(email, tinh, phuong, chi_tiet)
JOIN nguoi_dung n ON n.email = v.email
WHERE NOT EXISTS (SELECT 1 FROM dia_chi d WHERE d.nguoi_dung_id = n.id);

DO $$
DECLARE
    ky_tu      CONSTANT TEXT := 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'; -- như DonHangService.KY_TU_MA
    quan_tri   BIGINT := (SELECT id FROM nguoi_dung WHERE email = 'admin@techgear.vn');
    phi        BIGINT := (SELECT gia_tri::bigint FROM cai_dat WHERE khoa = 'phi_van_chuyen');
    mien_phi   BIGINT := (SELECT gia_tri::bigint FROM cai_dat WHERE khoa = 'mien_phi_van_chuyen_tu');
    d          RECORD;
    dong       RECORD;
    mgg        RECORD;
    mgg_id     BIGINT;
    khach      RECORD;
    don_ids    BIGINT[] := '{}';
    don_id     BIGINT;
    ma_don     TEXT;
    t          TIMESTAMPTZ; -- lúc đặt
    t_huy      TIMESTAMPTZ;
    t_cuoi     TIMESTAMPTZ;
    tam        BIGINT;
    giam       BIGINT;
    phi_ship   BIGINT;
    tong       BIGINT;
    sau        INTEGER;
    nguoi_dat  BIGINT;
    ten_nhan   TEXT;
    sdt        TEXT;
    dia_chi_gh TEXT;
    tt_tt      TEXT;
    loi        TEXT;
BEGIN
IF EXISTS (SELECT 1 FROM don_hang) THEN
    RETURN;
END IF;
-- CSDL tạo trước Phase 10: R__du_lieu_mau_san_pham.sql đã bỏ qua sản phẩm mới mà đơn mẫu cần → không chạy (không làm hỏng khởi động)
IF NOT EXISTS (SELECT 1 FROM bien_the WHERE sku = 'KEY-K8-RED') THEN
    RAISE NOTICE 'Bỏ qua đơn hàng mẫu: CSDL thiếu sản phẩm mẫu mới. Muốn có đủ dữ liệu mẫu: docker compose down -v';
    RETURN;
END IF;

-- Chỉnh khách mẫu trong khối này để file chạy lại không ghi đè thao tác của quản trị
UPDATE nguoi_dung SET trang_thai = 'bi_khoa', updated_at = now() - make_interval(days => 50)
WHERE email = 'ngo.long@example.com';
INSERT INTO dia_chi (nguoi_dung_id, ten_nguoi_nhan, so_dien_thoai, tinh_thanh, phuong_xa, dia_chi_chi_tiet, la_mac_dinh)
SELECT n.id, 'Nguyễn Văn Khách (công ty)', '0900000003', 'Thành phố Hồ Chí Minh', 'Phường Bến Nghé',
       'Tầng 5, 2 Hải Triều', FALSE
FROM nguoi_dung n
WHERE n.email = 'khach@techgear.vn';

CREATE TEMP TABLE dong_don (sku TEXT, so_luong INT, bien_the_id BIGINT, san_pham_id BIGINT, ten TEXT,
                            phan_loai TEXT, hinh_anh TEXT, don_gia BIGINT) ON COMMIT DROP;

-- stt, đặt cách đây (ngày, phút), email khách, trạng thái cuối, thanh toán, mã giảm giá, dòng "SKU*số lượng;...",
-- kiểu: NULL bình thường | khach_huy | qt_huy (sau khi xác nhận) | tu_huy (VNPay quá hạn) | hoan_tien (VNPay đã trả, quản trị hủy)
--       | lan2 (VNPay lần 1 thất bại, lần 2 thành công); ghi chú hủy; ghi chú của khách
FOR d IN
    SELECT * FROM (VALUES
        (1, 91, 200, 'nguyen.lan@example.com', 'hoan_thanh', 'cod', NULL, 'LOG-K380*1;UGR-AC3A*2', NULL, NULL, NULL),
        (2, 89, 320, 'tran.minh@example.com', 'hoan_thanh', 'vnpay', 'GIAM10', 'SSG-PB10K*1;UGR-AC3A*1', NULL, NULL, NULL),
        (3, 87, 150, 'le.huong@example.com', 'hoan_thanh', 'cod', NULL, 'LOG-M331-DO*1', NULL, NULL, 'Giao giờ hành chính'),
        (4, 86, 480, 'pham.duc@example.com', 'hoan_thanh', 'vnpay', NULL, 'SNY-CH510*1;LOG-M331-DEN*1', NULL, NULL, NULL),
        (5, 84, 260, 'phan.hong@example.com', 'hoan_thanh', 'cod', NULL, 'SSG-25W*2', NULL, NULL, NULL),
        (6, 78, 90, 'vo.mai@example.com', 'da_huy', 'cod', NULL, 'JBL-CHG4-XANH*1', 'khach_huy', 'Đổi ý, muốn chọn màu khác', NULL),
        (7, 76, 400, 'dang.khoa@example.com', 'hoan_thanh', 'cod', 'MOILAN1', 'SSG-S22U-TPU*1;UGR-AC3A*1', NULL, NULL, NULL),
        (8, 74, 210, 'bui.thao@example.com', 'hoan_thanh', 'vnpay', NULL, 'SSG-BUDS2P-TRANG*1', NULL, NULL, NULL),
        (9, 72, 330, 'hoang.tuan@example.com', 'hoan_thanh', 'cod', 'GIAM50K', 'JBL-CHG4-DEN*1', NULL, NULL, 'Gọi trước khi giao'),
        (10, 69, 120, 'nguyen.lan@example.com', 'hoan_thanh', 'vnpay', NULL, 'BAS-CC60-2M*2;SSG-25W*1', NULL, NULL, NULL),
        (11, 64, 500, 'do.ngoc@example.com', 'da_huy', 'vnpay', NULL, 'XMI-PB10K-PRO*1', 'tu_huy', NULL, NULL),
        (12, 63, 280, 'tran.minh@example.com', 'hoan_thanh', 'cod', NULL, 'XMI-PB10K-PRO*1;BAS-CC60-1M*1', NULL, NULL, NULL),
        (13, 61, 190, 'khach@techgear.vn', 'hoan_thanh', 'vnpay', 'GIAM10', 'SSG-KCL-A04E*1;SSG-BUDS2P-DEN*1', NULL, NULL, NULL),
        (14, 59, 360, 'le.huong@example.com', 'hoan_thanh', 'cod', NULL, 'JBL-GO2-XANH*1', NULL, NULL, NULL),
        (15, 57, 100, 'ngo.long@example.com', 'da_huy', 'cod', NULL, 'JBL-GO2-DEN*3', 'qt_huy',
         'Gọi 3 lần không liên lạc được khách', NULL),
        (16, 54, 230, 'pham.duc@example.com', 'hoan_thanh', 'vnpay', NULL, 'UGR-PRIV16*2;LOG-M331-DEN*1', NULL, NULL, NULL),
        (17, 51, 420, 'vo.mai@example.com', 'hoan_thanh', 'cod', 'MOILAN1', 'JBL-GO2-DEN*1', NULL, NULL, NULL),
        (18, 49, 140, 'ly.huy@example.com', 'hoan_thanh', 'vnpay', NULL, 'LOG-G304*1', NULL, NULL, NULL),
        (19, 44, 300, 'bui.thao@example.com', 'hoan_thanh', 'cod', NULL, 'SNY-XB13*1;UGR-AC3A*1', NULL, NULL, NULL),
        (20, 42, 250, 'hoang.tuan@example.com', 'da_huy', 'vnpay', NULL, 'LOG-K380*1', 'hoan_tien',
         'Khách đổi ý sau khi thanh toán, đã hoàn tiền qua VNPay', NULL),
        (21, 39, 180, 'nguyen.lan@example.com', 'hoan_thanh', 'cod', NULL, 'ANK-PL3-1M*1;SSG-25W*1', NULL, NULL, NULL),
        (22, 36, 440, 'dang.khoa@example.com', 'hoan_thanh', 'vnpay', 'GIAM10', 'LOG-G304*1;LOG-M331-DO*1', NULL, NULL, NULL),
        (23, 33, 160, 'do.ngoc@example.com', 'hoan_thanh', 'cod', NULL, 'JBL-CHG4-DEN*1', NULL, NULL, NULL),
        (24, 29, 350, 'tran.minh@example.com', 'hoan_thanh', 'vnpay', NULL, 'ANK-NANO65-DEN*1;UGR-AC3A*1', NULL, NULL, NULL),
        (25, 27, 220, 'khach@techgear.vn', 'hoan_thanh', 'cod', 'MOILAN1', 'APL-20W*1', NULL, NULL, NULL),
        (26, 24, 310, 'le.huong@example.com', 'hoan_thanh', 'vnpay', NULL, 'UGR-CC100-1M*2', 'lan2', NULL, NULL),
        (27, 21, 130, 'phan.hong@example.com', 'da_huy', 'cod', NULL, 'SSG-PB10K*1', 'qt_huy', 'Khách gọi điện xin hủy', NULL),
        (28, 19, 270, 'pham.duc@example.com', 'hoan_thanh', 'cod', NULL, 'BAS-GAN5-100-DEN*1', NULL, NULL, NULL),
        (29, 17, 390, 'vo.mai@example.com', 'hoan_thanh', 'vnpay', 'GIAM50K', 'ANK-PC10K-XANH*1;ANK-NANO65-TRANG*1', NULL, NULL, NULL),
        (30, 15, 110, 'bui.thao@example.com', 'hoan_thanh', 'cod', NULL, 'ANK-PC10K-DEN*1', NULL, NULL, NULL),
        (31, 13, 340, 'hoang.tuan@example.com', 'hoan_thanh', 'vnpay', NULL, 'LOG-MX3S-DEN*1', NULL, NULL, NULL),
        (32, 11, 200, 'nguyen.lan@example.com', 'hoan_thanh', 'vnpay', 'GIAM10', 'APL-APP2*1', NULL, NULL, NULL),
        (33, 9, 450, 'dang.khoa@example.com', 'hoan_thanh', 'cod', NULL, 'SNY-XM5-DEN*1', NULL, NULL, NULL),
        (34, 7, 170, 'do.ngoc@example.com', 'hoan_thanh', 'cod', NULL, 'SSG-BUDS3-TRANG*1', NULL, NULL, NULL),
        (35, 6, 290, 'tran.minh@example.com', 'da_huy', 'cod', NULL, 'APL-SC17*1', 'khach_huy', NULL, NULL),
        (36, 5, 380, 'khach@techgear.vn', 'hoan_thanh', 'vnpay', NULL, 'LOG-MXMINI-TAC*1', NULL, NULL, NULL),
        (37, 3, 240, 'le.huong@example.com', 'dang_giao', 'cod', NULL, 'LOG-M705*1;UGR-CAR30*1', NULL, NULL, NULL),
        (38, 2, 330, 'pham.duc@example.com', 'dang_giao', 'vnpay', NULL, 'UGR-OP16P*1;APL-SC17*1', NULL, NULL, NULL),
        (39, 1, 400, 'vo.mai@example.com', 'da_xac_nhan', 'cod', 'GIAM10', 'SNY-XM5-BAC*1', NULL, NULL, NULL),
        (40, 1, 150, 'ly.huy@example.com', 'da_xac_nhan', 'vnpay', NULL, 'LOG-MX3S-XAM*1', NULL, NULL, NULL),
        (41, 0, 180, 'bui.thao@example.com', 'cho_xac_nhan', 'cod', NULL, 'KEY-K8-RED*1', NULL, NULL, NULL),
        (42, 0, 60, 'hoang.tuan@example.com', 'cho_xac_nhan', 'vnpay', NULL, 'KEY-K8-BROWN*1;LOG-M331-DEN*1', NULL, NULL, NULL),
        (43, 0, 30, 'nguyen.lan@example.com', 'cho_xac_nhan', 'cod', 'MOILAN1', 'UGR-CC100-2M*1', NULL, NULL, NULL)
    ) v(stt, ngay, phut, email, trang_thai, pttt, ma_giam, dong, kieu, ghi_chu_huy, ghi_chu)
    ORDER BY ngay DESC, phut DESC -- theo thời gian để lịch sử kho nối tiếp đúng
LOOP
    t := now() - make_interval(days => d.ngay, mins => d.phut);

    -- Người đặt: tài khoản (địa chỉ mặc định) hoặc khách vãng lai
    SELECT n.id, n.trang_thai, dc.ten_nguoi_nhan, dc.so_dien_thoai,
           dc.dia_chi_chi_tiet || ', ' || dc.phuong_xa || ', ' || dc.tinh_thanh AS dia_chi
    INTO khach
    FROM nguoi_dung n JOIN dia_chi dc ON dc.nguoi_dung_id = n.id AND dc.la_mac_dinh
    WHERE n.email = d.email;
    IF FOUND THEN
        nguoi_dat := khach.id; ten_nhan := khach.ten_nguoi_nhan; sdt := khach.so_dien_thoai; dia_chi_gh := khach.dia_chi;
    ELSE
        nguoi_dat := NULL;
        SELECT g.ten, g.sdt, g.dia_chi INTO ten_nhan, sdt, dia_chi_gh
        FROM (VALUES ('phan.hong@example.com', 'Phan Thị Hồng', '0987654321', '18 Nguyễn Trãi, Phường Thanh Xuân, Thành phố Hà Nội'),
                     ('ly.huy@example.com', 'Lý Gia Huy', '0987654322', '66 Cách Mạng Tháng Tám, Phường Bàn Cờ, Thành phố Hồ Chí Minh')
             ) g(email, ten, sdt, dia_chi)
        WHERE g.email = d.email;
        IF NOT FOUND THEN
            RAISE EXCEPTION 'Đơn mẫu %: không có khách %', d.stt, d.email;
        END IF;
    END IF;

    -- Dòng đơn: giá, tên, phân loại, ảnh lấy như lúc đặt hàng thật (HienThi.PHAN_LOAI, ANH_BIEN_THE)
    TRUNCATE dong_don;
    INSERT INTO dong_don
    SELECT split_part(x, '*', 1), split_part(x, '*', 2)::int, bt.id, bt.san_pham_id, sp.ten,
           (SELECT string_agg(gt.gia_tri, ' / ' ORDER BY tc.thu_tu, tc.id)
            FROM bien_the_gia_tri btg
            JOIN gia_tri_tuy_chon gt ON gt.id = btg.gia_tri_tuy_chon_id
            JOIN tuy_chon tc ON tc.id = gt.tuy_chon_id
            WHERE btg.bien_the_id = bt.id),
           (SELECT ha.duong_dan FROM hinh_anh_san_pham ha WHERE ha.san_pham_id = bt.san_pham_id
            ORDER BY ha.la_anh_chinh DESC, ha.thu_tu, ha.id LIMIT 1),
           bt.gia_ban
    FROM unnest(string_to_array(d.dong, ';')) x
    LEFT JOIN bien_the bt ON bt.sku = split_part(x, '*', 1) AND bt.deleted_at IS NULL
    LEFT JOIN san_pham sp ON sp.id = bt.san_pham_id;
    SELECT string_agg(sku, ', ') INTO loi FROM dong_don WHERE bien_the_id IS NULL;
    IF loi IS NOT NULL THEN
        RAISE EXCEPTION 'Đơn mẫu %: không có SKU %', d.stt, loi;
    END IF;
    SELECT string_agg(dd.sku, ', ') INTO loi
    FROM dong_don dd JOIN bien_the bt ON bt.id = dd.bien_the_id
    WHERE NOT EXISTS (SELECT 1 FROM lich_su_ton_kho ls
                      WHERE ls.bien_the_id = bt.id AND ls.loai = 'khoi_tao' AND ls.created_at < t);
    IF loi IS NOT NULL THEN
        RAISE EXCEPTION 'Đơn mẫu %: % chưa nhập kho trước lúc đặt', d.stt, loi;
    END IF;
    tam := (SELECT sum(don_gia * so_luong) FROM dong_don);

    -- Mã giảm giá: kiểm như MaGiamGia.loiKhiApDung, tính như MaGiamGia.tienGiam
    giam := 0;
    mgg_id := NULL;
    IF d.ma_giam IS NOT NULL THEN
        SELECT * INTO mgg FROM ma_giam_gia WHERE upper(ma) = d.ma_giam AND deleted_at IS NULL;
        IF NOT FOUND OR NOT mgg.kich_hoat OR t < mgg.bat_dau_luc OR t >= mgg.ket_thuc_luc OR tam < mgg.don_toi_thieu
           OR (mgg.tong_luot_dung IS NOT NULL AND mgg.da_dung >= mgg.tong_luot_dung)
           OR (mgg.luot_dung_moi_nguoi IS NOT NULL AND mgg.luot_dung_moi_nguoi <=
               (SELECT count(*) FROM don_hang dh WHERE dh.ma_giam_gia_id = mgg.id AND dh.trang_thai <> 'da_huy'
                AND (dh.nguoi_dung_id = nguoi_dat OR (nguoi_dat IS NULL AND lower(dh.email) = d.email)))) THEN
            RAISE EXCEPTION 'Đơn mẫu %: không áp được mã %', d.stt, d.ma_giam;
        END IF;
        giam := CASE WHEN mgg.loai_giam = 'phan_tram'
                     THEN tam / 100 * mgg.gia_tri_giam + tam % 100 * mgg.gia_tri_giam / 100
                     ELSE mgg.gia_tri_giam END;
        giam := least(giam, coalesce(mgg.giam_toi_da, giam), tam);
        mgg_id := mgg.id;
    END IF;
    phi_ship := CASE WHEN tam - giam >= mien_phi THEN 0 ELSE phi END;
    tong := tam - giam + phi_ship;

    -- Mốc thời gian theo trạng thái cuối
    t_huy := CASE d.kieu WHEN 'khach_huy' THEN t + interval '2 hours'
                         WHEN 'qt_huy' THEN t + interval '5 hours'
                         WHEN 'hoan_tien' THEN t + interval '6 hours'
                         WHEN 'tu_huy' THEN t + interval '35 minutes' END;
    t_cuoi := CASE d.trang_thai WHEN 'cho_xac_nhan' THEN t + interval '3 minutes'
                                WHEN 'da_xac_nhan' THEN t + interval '2 hours'
                                WHEN 'dang_giao' THEN t + interval '1 day'
                                WHEN 'hoan_thanh' THEN t + interval '3 days'
                                ELSE t_huy END;
    IF t_cuoi >= now() THEN
        RAISE EXCEPTION 'Đơn mẫu %: mốc cuối nằm ở tương lai', d.stt;
    END IF;
    tt_tt := CASE WHEN d.kieu = 'hoan_tien' THEN 'da_hoan_tien'
                  WHEN d.pttt = 'vnpay' AND d.kieu IS DISTINCT FROM 'tu_huy' THEN 'da_thanh_toan'
                  WHEN d.pttt = 'cod' AND d.trang_thai = 'hoan_thanh' THEN 'da_thanh_toan'
                  ELSE 'chua_thanh_toan' END;

    ma_don := 'DH' || to_char(t AT TIME ZONE 'Asia/Ho_Chi_Minh', 'YYMMDD')
              || (SELECT string_agg(substr(ky_tu, (d.stt * 7 + i * 13) % 32 + 1, 1), '' ORDER BY i)
                  FROM generate_series(1, 6) i);

    INSERT INTO don_hang (ma_don_hang, nguoi_dung_id, ma_giam_gia_id, ten_nguoi_nhan, email, so_dien_thoai,
                          dia_chi_giao_hang, ghi_chu, tam_tinh, tien_giam_gia, phi_van_chuyen, tong_tien,
                          phuong_thuc_thanh_toan, trang_thai_thanh_toan, trang_thai, created_at, updated_at)
    VALUES (ma_don, nguoi_dat, mgg_id, ten_nhan, d.email, sdt, dia_chi_gh, d.ghi_chu, tam, giam, phi_ship, tong,
            d.pttt, tt_tt, d.trang_thai, t, t_cuoi)
    RETURNING id INTO don_id;
    don_ids[d.stt] := don_id;

    FOR dong IN SELECT * FROM dong_don LOOP
        INSERT INTO chi_tiet_don_hang (don_hang_id, san_pham_id, bien_the_id, ten_san_pham, sku, phan_loai, hinh_anh,
                                       don_gia, so_luong, thanh_tien)
        VALUES (don_id, dong.san_pham_id, dong.bien_the_id, dong.ten, dong.sku, dong.phan_loai, dong.hinh_anh,
                dong.don_gia, dong.so_luong, dong.don_gia * dong.so_luong);
        UPDATE bien_the SET so_luong_ton = so_luong_ton - dong.so_luong, updated_at = t
        WHERE id = dong.bien_the_id RETURNING so_luong_ton INTO sau;
        INSERT INTO lich_su_ton_kho (bien_the_id, don_hang_id, loai, so_luong_thay_doi, so_luong_sau, nguoi_thuc_hien_id, created_at)
        VALUES (dong.bien_the_id, don_id, 'ban_hang', -dong.so_luong, sau, nguoi_dat, t);
    END LOOP;
    IF mgg_id IS NOT NULL THEN
        UPDATE ma_giam_gia SET da_dung = da_dung + 1 WHERE id = mgg_id;
    END IF;

    -- Lịch sử trạng thái đơn
    INSERT INTO lich_su_don_hang (don_hang_id, trang_thai, ghi_chu, nguoi_thuc_hien_id, created_at)
    VALUES (don_id, 'cho_xac_nhan', 'Đặt hàng', nguoi_dat, t);
    IF d.trang_thai IN ('da_xac_nhan', 'dang_giao', 'hoan_thanh') OR d.kieu IN ('qt_huy', 'hoan_tien') THEN
        INSERT INTO lich_su_don_hang (don_hang_id, trang_thai, ghi_chu, nguoi_thuc_hien_id, created_at)
        VALUES (don_id, 'da_xac_nhan', NULL, quan_tri, t + interval '2 hours');
    END IF;
    IF d.trang_thai IN ('dang_giao', 'hoan_thanh') THEN
        INSERT INTO lich_su_don_hang (don_hang_id, trang_thai, ghi_chu, nguoi_thuc_hien_id, created_at)
        VALUES (don_id, 'dang_giao', 'Bàn giao cho đơn vị vận chuyển', quan_tri, t + interval '1 day');
    END IF;
    IF d.trang_thai = 'hoan_thanh' THEN
        INSERT INTO lich_su_don_hang (don_hang_id, trang_thai, ghi_chu, nguoi_thuc_hien_id, created_at)
        VALUES (don_id, 'hoan_thanh', 'Giao hàng thành công', quan_tri, t + interval '3 days');
    END IF;

    -- Hủy: hoàn kho, trả lượt mã (như DonHangService.huy)
    IF d.trang_thai = 'da_huy' THEN
        FOR dong IN SELECT * FROM dong_don LOOP
            UPDATE bien_the SET so_luong_ton = so_luong_ton + dong.so_luong, updated_at = t_huy
            WHERE id = dong.bien_the_id RETURNING so_luong_ton INTO sau;
            INSERT INTO lich_su_ton_kho (bien_the_id, don_hang_id, loai, so_luong_thay_doi, so_luong_sau, nguoi_thuc_hien_id, created_at)
            VALUES (dong.bien_the_id, don_id, 'huy_don', dong.so_luong, sau,
                    CASE d.kieu WHEN 'khach_huy' THEN nguoi_dat WHEN 'tu_huy' THEN NULL ELSE quan_tri END, t_huy);
        END LOOP;
        IF mgg_id IS NOT NULL THEN
            UPDATE ma_giam_gia SET da_dung = da_dung - 1 WHERE id = mgg_id;
        END IF;
        INSERT INTO lich_su_don_hang (don_hang_id, trang_thai, ghi_chu, nguoi_thuc_hien_id, created_at)
        VALUES (don_id, 'da_huy',
                coalesce(d.ghi_chu_huy, CASE d.kieu WHEN 'khach_huy' THEN 'Khách hủy đơn'
                                                    WHEN 'tu_huy' THEN 'Quá 30 phút chưa thanh toán VNPay, hệ thống tự hủy'
                                                    ELSE 'Quản trị hủy đơn' END),
                CASE d.kieu WHEN 'khach_huy' THEN nguoi_dat WHEN 'tu_huy' THEN NULL ELSE quan_tri END, t_huy);
    END IF;
    IF d.trang_thai = 'hoan_thanh' AND d.pttt = 'cod' THEN
        UPDATE don_hang SET ghi_chu_quan_tri = 'Đã thu tiền COD' WHERE id = don_id;
    END IF;

    -- Giao dịch VNPay: mã tham chiếu = mã đơn + 2 chữ số lần thử (như ThanhToanService)
    IF d.pttt = 'vnpay' THEN
        IF d.kieu = 'lan2' THEN
            INSERT INTO thanh_toan (don_hang_id, phuong_thuc, so_tien, ma_tham_chieu, trang_thai, du_lieu_phan_hoi,
                                    thanh_toan_luc, created_at, updated_at)
            VALUES (don_id, 'vnpay', tong, ma_don || '01', 'that_bai',
                    jsonb_build_object('vnp_Amount', (tong * 100)::text, 'vnp_BankCode', 'NCB',
                                       'vnp_ResponseCode', '24', 'vnp_TransactionStatus', '02', 'vnp_TxnRef', ma_don || '01'),
                    t + interval '2 minutes', t + interval '1 minute', t + interval '2 minutes');
        END IF;
        INSERT INTO thanh_toan (don_hang_id, phuong_thuc, so_tien, ma_tham_chieu, ma_giao_dich, trang_thai,
                                du_lieu_phan_hoi, thanh_toan_luc, created_at, updated_at)
        SELECT don_id, 'vnpay', tong, ma_don || CASE WHEN d.kieu = 'lan2' THEN '02' ELSE '01' END,
               CASE WHEN d.kieu = 'tu_huy' THEN NULL ELSE (14000000 + d.stt * 7919)::text END,
               CASE WHEN d.kieu = 'tu_huy' THEN 'cho_xu_ly' ELSE 'thanh_cong' END,
               CASE WHEN d.kieu = 'tu_huy' THEN NULL
                    ELSE jsonb_build_object('vnp_Amount', (tong * 100)::text, 'vnp_BankCode', 'NCB', 'vnp_CardType', 'ATM',
                                            'vnp_PayDate', to_char((t + interval '3 minutes') AT TIME ZONE 'Asia/Ho_Chi_Minh',
                                                                   'YYYYMMDDHH24MISS'),
                                            'vnp_ResponseCode', '00', 'vnp_TransactionNo', (14000000 + d.stt * 7919)::text,
                                            'vnp_TransactionStatus', '00',
                                            'vnp_TxnRef', ma_don || CASE WHEN d.kieu = 'lan2' THEN '02' ELSE '01' END) END,
               CASE WHEN d.kieu = 'tu_huy' THEN NULL ELSE t + interval '3 minutes' END,
               t + interval '2 minutes', t + interval '3 minutes';
    END IF;
END LOOP;

-- Đánh giá trên đơn đã hoàn thành (stt đơn, SKU đã mua, số sao, nội dung, trạng thái)
INSERT INTO danh_gia (san_pham_id, nguoi_dung_id, don_hang_id, so_sao, noi_dung, trang_thai, created_at, updated_at)
SELECT ct.san_pham_id, dh.nguoi_dung_id, dh.id, v.sao, v.noi_dung, v.tt,
       dh.updated_at + make_interval(days => v.stt % 3 + 1), dh.updated_at + make_interval(days => v.stt % 3 + 1)
FROM (VALUES (1, 'LOG-K380', 5, 'Gõ êm, kết nối nhanh với cả laptop và điện thoại.', 'da_duyet'),
             (1, 'UGR-AC3A', 4, 'Cáp chắc chắn, sạc ổn định.', 'da_duyet'),
             (2, 'SSG-PB10K', 5, 'Pin trâu, vỏ kim loại cầm rất chắc tay.', 'da_duyet'),
             (3, 'LOG-M331-DO', 5, 'Click êm thật sự, dùng trong thư viện không làm phiền ai.', 'da_duyet'),
             (4, 'SNY-CH510', 4, 'Nhẹ, đeo lâu không đau tai, bass hơi ít.', 'da_duyet'),
             (7, 'SSG-S22U-TPU', 3, 'Ốp ổn nhưng sau một tháng hơi ngả màu.', 'da_duyet'),
             (8, 'SSG-BUDS2P-TRANG', 5, 'Chống ồn tốt, kết nối với điện thoại Samsung rất nhanh.', 'da_duyet'),
             (9, 'JBL-CHG4-DEN', 5, 'Âm thanh to, pin dùng cả buổi dã ngoại.', 'da_duyet'),
             (10, 'BAS-CC60-2M', 4, 'Cáp dài 2m tiện, sạc laptop được.', 'da_duyet'),
             (12, 'XMI-PB10K-PRO', 4, 'Sạc nhanh, hơi nặng.', 'da_duyet'),
             (13, 'SSG-BUDS2P-DEN', 4, 'Tai nghe tốt, giao hàng nhanh.', 'da_duyet'),
             (13, 'SSG-KCL-A04E', 2, 'Dán bị bọt khí ở góc, phải dán lại.', 'da_duyet'),
             (14, 'JBL-GO2-XANH', 5, 'Nhỏ xinh, âm lượng bất ngờ.', 'da_duyet'),
             (16, 'UGR-PRIV16', 4, 'Chống nhìn trộm tốt, màn hình hơi tối hơn.', 'da_duyet'),
             (17, 'JBL-GO2-DEN', 1, 'Mua giá rẻ hơn liên hệ zalo 09xx!!!', 'da_an'),
             (19, 'SNY-XB13', 4, 'Loa nhỏ mà bass khá.', 'da_duyet'),
             (21, 'ANK-PL3-1M', 5, 'Cáp chuẩn MFi, không báo lỗi.', 'da_duyet'),
             (22, 'LOG-G304', 5, 'Chuột nhẹ, chơi game mượt.', 'da_duyet'),
             (23, 'JBL-CHG4-DEN', 4, 'Âm hay, giá hơi cao.', 'da_duyet'),
             (24, 'ANK-NANO65-DEN', 5, 'Nhỏ gọn mà sạc được laptop 65W.', 'da_duyet'),
             (25, 'APL-20W', 5, 'Hàng chính hãng, sạc iPhone nhanh.', 'da_duyet'),
             (26, 'UGR-CC100-1M', 4, 'Cáp bện dù chắc.', 'cho_duyet'),
             (29, 'ANK-PC10K-XANH', 5, 'Màu xanh đẹp, sạc nhanh.', 'cho_duyet'),
             (31, 'LOG-MX3S-DEN', 5, 'Chuột tốt nhất mình từng dùng, cuộn MagSpeed rất đã.', 'da_duyet'),
             (32, 'APL-APP2', 5, 'Chống ồn xuất sắc.', 'da_duyet'),
             (33, 'SNY-XM5-DEN', 5, 'Đáng tiền, chống ồn số 1.', 'cho_duyet'),
             (34, 'SSG-BUDS3-TRANG', 3, 'Thiết kế mở nên chống ồn không bằng loại nhét tai.', 'cho_duyet')
     ) v(stt, sku, sao, noi_dung, tt)
JOIN don_hang dh ON dh.id = don_ids[v.stt] AND dh.trang_thai = 'hoan_thanh' AND dh.nguoi_dung_id IS NOT NULL
JOIN chi_tiet_don_hang ct ON ct.don_hang_id = dh.id AND ct.sku = v.sku;
IF (SELECT count(*) FROM danh_gia) <> 27 THEN
    RAISE EXCEPTION 'Đánh giá mẫu: chỉ nạp được % / 27 (stt/SKU không khớp đơn hoàn thành)', (SELECT count(*) FROM danh_gia);
END IF;

-- Giỏ hàng, yêu thích của khach@techgear.vn
INSERT INTO gio_hang (nguoi_dung_id, bien_the_id, so_luong)
SELECT n.id, bt.id, v.sl
FROM (VALUES ('LOG-M705', 1), ('SSG-25W', 2)) v(sku, sl)
JOIN bien_the bt ON bt.sku = v.sku AND bt.deleted_at IS NULL
JOIN nguoi_dung n ON n.email = 'khach@techgear.vn';
INSERT INTO yeu_thich (nguoi_dung_id, san_pham_id)
SELECT n.id, sp.id
FROM (VALUES ('tai-nghe-apple-airpods-pro-2'), ('ban-phim-co-keychron-k8'), ('tai-nghe-sony-wh-1000xm5')) v(slug)
JOIN san_pham sp ON sp.slug = v.slug AND sp.deleted_at IS NULL
JOIN nguoi_dung n ON n.email = 'khach@techgear.vn';

-- ===== Tự kiểm: dữ liệu sai thì dừng (backend không khởi động, lỗi ghi rõ chỗ sai) =====
-- 1. Lịch sử kho nối tiếp và khớp tồn kho hiện tại
SELECT string_agg(DISTINCT bt.sku, ', ') INTO loi
FROM bien_the bt
JOIN (SELECT bien_the_id, so_luong_sau, so_luong_thay_doi,
             lag(so_luong_sau) OVER w AS truoc, row_number() OVER w AS stt,
             count(*) OVER (PARTITION BY bien_the_id) AS so_dong
      FROM lich_su_ton_kho WINDOW w AS (PARTITION BY bien_the_id ORDER BY created_at, id)) ls ON ls.bien_the_id = bt.id
WHERE ls.so_luong_sau <> coalesce(ls.truoc, 0) + ls.so_luong_thay_doi
   OR (ls.stt = ls.so_dong AND ls.so_luong_sau <> bt.so_luong_ton);
IF loi IS NOT NULL THEN
    RAISE EXCEPTION 'Lịch sử kho không khớp tồn kho: %', loi;
END IF;
-- 2. Tạm tính = tổng dòng (tổng tiền đã có CHECK trong bảng)
SELECT string_agg(dh.ma_don_hang, ', ') INTO loi
FROM don_hang dh
WHERE dh.tam_tinh <> (SELECT sum(thanh_tien) FROM chi_tiet_don_hang ct WHERE ct.don_hang_id = dh.id);
IF loi IS NOT NULL THEN
    RAISE EXCEPTION 'Tạm tính lệch tổng dòng: %', loi;
END IF;
-- 3. Đơn đã thanh toán/hoàn tiền qua VNPay phải có giao dịch thành công khớp số tiền; COD hoàn thành đã thu tiền
SELECT string_agg(dh.ma_don_hang, ', ') INTO loi
FROM don_hang dh
WHERE (dh.phuong_thuc_thanh_toan = 'vnpay' AND dh.trang_thai_thanh_toan <> 'chua_thanh_toan'
       AND NOT EXISTS (SELECT 1 FROM thanh_toan tt WHERE tt.don_hang_id = dh.id AND tt.trang_thai = 'thanh_cong'
                       AND tt.so_tien = dh.tong_tien))
   OR (dh.phuong_thuc_thanh_toan = 'cod' AND dh.trang_thai = 'hoan_thanh' AND dh.trang_thai_thanh_toan <> 'da_thanh_toan')
   OR (dh.phuong_thuc_thanh_toan = 'vnpay' AND dh.trang_thai IN ('da_xac_nhan', 'dang_giao', 'hoan_thanh')
       AND dh.trang_thai_thanh_toan <> 'da_thanh_toan');
IF loi IS NOT NULL THEN
    RAISE EXCEPTION 'Trạng thái thanh toán sai: %', loi;
END IF;
-- 4. Lượt dùng mã = số đơn chưa hủy dùng mã
SELECT string_agg(m.ma, ', ') INTO loi
FROM ma_giam_gia m
WHERE m.da_dung <> (SELECT count(*) FROM don_hang dh WHERE dh.ma_giam_gia_id = m.id AND dh.trang_thai <> 'da_huy');
IF loi IS NOT NULL THEN
    RAISE EXCEPTION 'Lượt dùng mã giảm giá lệch: %', loi;
END IF;
-- 5. Lịch sử đơn kết thúc đúng trạng thái hiện tại
SELECT string_agg(dh.ma_don_hang, ', ') INTO loi
FROM don_hang dh
WHERE dh.trang_thai <> (SELECT ls.trang_thai FROM lich_su_don_hang ls WHERE ls.don_hang_id = dh.id
                        ORDER BY ls.created_at DESC, ls.id DESC LIMIT 1);
IF loi IS NOT NULL THEN
    RAISE EXCEPTION 'Lịch sử đơn không khớp trạng thái: %', loi;
END IF;
END
$$;
