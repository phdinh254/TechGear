-- Dữ liệu mẫu vận hành – chỉ profile dev: khách hàng, đơn hàng (đủ trạng thái, COD/PayOS), thanh toán, lịch sử đơn/kho,
-- đánh giá, giỏ hàng, yêu thích. Chạy sau R__du_lieu_mau_san_pham.sql (Flyway chạy file R__ theo thứ tự tên).
-- Chỉ nạp khi chưa có đơn hàng nào; muốn nạp lại từ đầu: docker compose down -v.
-- Khách mẫu: mật khẩu chung Khach@123; ngo.long@example.com bị khóa.
-- Mã giảm giá đọc từ R__du_lieu_mau_khuyen_mai.sql (khung giờ tính theo now() nên đơn mẫu luôn áp được).
-- Đơn tính tiền theo đúng quy tắc backend (giá biến thể, mã giảm giá, phí vận chuyển trong cai_dat) và
-- trừ/hoàn kho nối tiếp từ dòng 'khoi_tao'; cuối thủ tục tự kiểm, sai thì dừng khởi động (SIGNAL).

INSERT INTO nguoi_dung (ho_ten, email, mat_khau, so_dien_thoai, gioi_tinh, ngay_sinh, created_at)
SELECT v.ho_ten, v.email, '$2a$12$JZZeyeYy71xijX5IowHhU.GUbCQe.OvmO9g38yyCeM6PlEGC/3Zp2', v.sdt, v.gt, CAST(v.ns AS DATE),
       now() - INTERVAL v.ngay DAY
FROM (VALUES ROW('Nguyễn Thị Lan', 'nguyen.lan@example.com', '0912345601', 'nu', '1998-03-12', 120),
             ROW('Trần Quang Minh', 'tran.minh@example.com', '0912345602', 'nam', '1995-07-08', 118),
             ROW('Lê Thu Hương', 'le.huong@example.com', '0912345603', 'nu', '2001-11-20', 115),
             ROW('Phạm Anh Đức', 'pham.duc@example.com', '0912345604', 'nam', '1993-01-30', 112),
             ROW('Võ Ngọc Mai', 'vo.mai@example.com', '0912345605', 'nu', '2000-05-05', 110),
             ROW('Đặng Minh Khoa', 'dang.khoa@example.com', '0912345606', 'nam', '1997-09-14', 105),
             ROW('Bùi Phương Thảo', 'bui.thao@example.com', '0912345607', 'nu', '1999-12-01', 100),
             ROW('Hoàng Văn Tuấn', 'hoang.tuan@example.com', '0912345608', 'nam', '1990-04-22', 96),
             ROW('Đỗ Bảo Ngọc', 'do.ngoc@example.com', '0912345609', 'nu', '2002-08-18', 90),
             ROW('Ngô Thành Long', 'ngo.long@example.com', '0912345610', 'nam', '1996-02-10', 80)
     ) v(ho_ten, email, sdt, gt, ns, ngay)
ON DUPLICATE KEY UPDATE nguoi_dung.id = nguoi_dung.id; -- email đã có: bỏ qua

INSERT INTO dia_chi (nguoi_dung_id, ten_nguoi_nhan, so_dien_thoai, tinh_thanh, phuong_xa, dia_chi_chi_tiet, la_mac_dinh)
SELECT n.id, n.ho_ten, n.so_dien_thoai, v.tinh, v.phuong, v.chi_tiet, TRUE
FROM (VALUES ROW('nguyen.lan@example.com', 'Thành phố Hà Nội', 'Phường Cầu Giấy', '25 Trần Thái Tông'),
             ROW('tran.minh@example.com', 'Thành phố Hồ Chí Minh', 'Phường Bến Thành', '88 Lê Lợi'),
             ROW('le.huong@example.com', 'Thành phố Đà Nẵng', 'Phường Hải Châu', '15 Bạch Đằng'),
             ROW('pham.duc@example.com', 'Thành phố Cần Thơ', 'Phường Ninh Kiều', '42 Hai Bà Trưng'),
             ROW('vo.mai@example.com', 'Thành phố Hồ Chí Minh', 'Phường Tân Định', '120 Hai Bà Trưng'),
             ROW('dang.khoa@example.com', 'Thành phố Hải Phòng', 'Phường Hồng Bàng', '7 Điện Biên Phủ'),
             ROW('bui.thao@example.com', 'Thành phố Huế', 'Phường Thuận Hóa', '30 Lê Lợi'),
             ROW('hoang.tuan@example.com', 'Thành phố Hà Nội', 'Phường Ba Đình', '12 Phan Đình Phùng'),
             ROW('do.ngoc@example.com', 'Thành phố Hồ Chí Minh', 'Phường Thủ Đức', '5 Võ Văn Ngân'),
             ROW('ngo.long@example.com', 'Thành phố Hà Nội', 'Phường Hoàn Kiếm', '9 Hàng Bài')
     ) v(email, tinh, phuong, chi_tiet)
JOIN nguoi_dung n ON n.email = v.email
WHERE NOT EXISTS (SELECT 1 FROM dia_chi d WHERE d.nguoi_dung_id = n.id);

-- Thủ tục tạm (cần biến, vòng lặp, rẽ nhánh), gọi xong thì xóa.
-- Biến cục bộ đặt tên khác mọi cột vì trong câu SQL của thủ tục MySQL, biến cùng tên được ưu tiên hơn cột.
DROP PROCEDURE IF EXISTS nap_du_lieu_mau_van_hanh;
DELIMITER //
CREATE PROCEDURE nap_du_lieu_mau_van_hanh()
nap: BEGIN
    DECLARE ky_tu VARCHAR(32) DEFAULT 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'; -- như DonHangService.KY_TU_MA
    DECLARE quan_tri, phi, mien_phi BIGINT;
    -- Đơn mẫu đang xét
    DECLARE d_stt, d_ngay, d_phut INT;
    DECLARE d_email VARCHAR(150);
    DECLARE d_trang_thai, d_pttt, d_ma_giam, d_kieu VARCHAR(50);
    DECLARE d_dong, d_ghi_chu_huy, d_ghi_chu VARCHAR(500);
    -- Mã giảm giá của đơn
    DECLARE mgg_id, m_don_toi_thieu, m_gia_tri, m_giam_toi_da BIGINT;
    DECLARE m_tong_luot, m_da_dung, m_moi_nguoi INT;
    DECLARE m_kich_hoat BOOLEAN;
    DECLARE m_bat_dau, m_ket_thuc DATETIME(6);
    DECLARE m_loai VARCHAR(20);
    -- Đơn đang tạo
    DECLARE v_don_id, nguoi_dat, tam, giam, phi_ship, tong BIGINT;
    DECLARE ma_don VARCHAR(20);
    DECLARE t, t_huy, t_cuoi DATETIME(6); -- t: lúc đặt
    DECLARE ten_nhan VARCHAR(100);
    DECLARE v_sdt VARCHAR(20);
    DECLARE dia_chi_gh VARCHAR(500);
    DECLARE tt_tt VARCHAR(20);
    DECLARE loi TEXT;
    DECLARE het BOOLEAN DEFAULT FALSE;
    -- Theo thời gian để lịch sử kho nối tiếp đúng
    DECLARE don_cur CURSOR FOR
        SELECT stt, ngay, phut, email, trang_thai, pttt, ma_giam, dong, kieu, ghi_chu_huy, ghi_chu
        FROM don_mau ORDER BY ngay DESC, phut DESC;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET het = TRUE;

    IF EXISTS (SELECT 1 FROM don_hang) THEN
        LEAVE nap;
    END IF;
    -- CSDL tạo trước Phase 10: R__du_lieu_mau_san_pham.sql đã bỏ qua sản phẩm mới mà đơn mẫu cần → không chạy
    -- (không làm hỏng khởi động). Muốn có đủ dữ liệu mẫu: docker compose down -v
    IF NOT EXISTS (SELECT 1 FROM bien_the WHERE sku = 'KEY-K8-RED') THEN
        LEAVE nap;
    END IF;
    SET quan_tri = (SELECT id FROM nguoi_dung WHERE email = 'admin@techgear.vn');
    SET phi = (SELECT CAST(gia_tri AS SIGNED) FROM cai_dat WHERE khoa = 'phi_van_chuyen');
    SET mien_phi = (SELECT CAST(gia_tri AS SIGNED) FROM cai_dat WHERE khoa = 'mien_phi_van_chuyen_tu');

    -- Chỉnh khách mẫu trong thủ tục để file chạy lại không ghi đè thao tác của quản trị
    UPDATE nguoi_dung SET trang_thai = 'bi_khoa', updated_at = now() - INTERVAL 50 DAY
    WHERE email = 'ngo.long@example.com';
    INSERT INTO dia_chi (nguoi_dung_id, ten_nguoi_nhan, so_dien_thoai, tinh_thanh, phuong_xa, dia_chi_chi_tiet, la_mac_dinh)
    SELECT n.id, 'Nguyễn Văn Khách (công ty)', '0900000003', 'Thành phố Hồ Chí Minh', 'Phường Bến Nghé',
           'Tầng 5, 2 Hải Triều', FALSE
    FROM nguoi_dung n
    WHERE n.email = 'khach@techgear.vn';

    DROP TEMPORARY TABLE IF EXISTS don_mau, dong_don, khach_vang_lai, don_da_tao;
    CREATE TEMPORARY TABLE dong_don (sku VARCHAR(100), so_luong INT, bien_the_id BIGINT, san_pham_id BIGINT,
                                     ten VARCHAR(255), phan_loai VARCHAR(150), hinh_anh VARCHAR(255), don_gia BIGINT);
    -- stt đơn mẫu -> id đơn đã tạo (để gắn đánh giá)
    CREATE TEMPORARY TABLE don_da_tao (stt INT PRIMARY KEY, don_id BIGINT NOT NULL);
    CREATE TEMPORARY TABLE khach_vang_lai (email VARCHAR(150), ten VARCHAR(100), sdt VARCHAR(20), dia_chi VARCHAR(500));
    INSERT INTO khach_vang_lai VALUES
        ('phan.hong@example.com', 'Phan Thị Hồng', '0987654321', '18 Nguyễn Trãi, Phường Thanh Xuân, Thành phố Hà Nội'),
        ('ly.huy@example.com', 'Lý Gia Huy', '0987654322', '66 Cách Mạng Tháng Tám, Phường Bàn Cờ, Thành phố Hồ Chí Minh');

    -- stt, đặt cách đây (ngày, phút), email khách, trạng thái cuối, thanh toán, mã giảm giá, dòng "SKU*số lượng;...",
    -- kiểu: NULL bình thường | khach_huy | qt_huy (sau khi xác nhận) | tu_huy (PayOS quá hạn) | hoan_tien (PayOS đã trả, quản trị hủy)
    --       | lan2 (PayOS lần 1 thất bại, lần 2 thành công); ghi chú hủy; ghi chú của khách
    CREATE TEMPORARY TABLE don_mau (stt INT PRIMARY KEY, ngay INT, phut INT, email VARCHAR(150), trang_thai VARCHAR(20),
                                    pttt VARCHAR(10), ma_giam VARCHAR(50), dong VARCHAR(500), kieu VARCHAR(20),
                                    ghi_chu_huy VARCHAR(500), ghi_chu VARCHAR(500));
    INSERT INTO don_mau VALUES
        (1, 91, 200, 'nguyen.lan@example.com', 'hoan_thanh', 'cod', NULL, 'LOG-K380*1;UGR-AC3A*2', NULL, NULL, NULL),
        (2, 89, 320, 'tran.minh@example.com', 'hoan_thanh', 'payos', 'GIAM10', 'SSG-PB10K*1;UGR-AC3A*1', NULL, NULL, NULL),
        (3, 87, 150, 'le.huong@example.com', 'hoan_thanh', 'cod', NULL, 'LOG-M331-DO*1', NULL, NULL, 'Giao giờ hành chính'),
        (4, 86, 480, 'pham.duc@example.com', 'hoan_thanh', 'payos', NULL, 'SNY-CH510*1;LOG-M331-DEN*1', NULL, NULL, NULL),
        (5, 84, 260, 'phan.hong@example.com', 'hoan_thanh', 'cod', NULL, 'SSG-25W*2', NULL, NULL, NULL),
        (6, 78, 90, 'vo.mai@example.com', 'da_huy', 'cod', NULL, 'JBL-CHG4-XANH*1', 'khach_huy', 'Đổi ý, muốn chọn màu khác', NULL),
        (7, 76, 400, 'dang.khoa@example.com', 'hoan_thanh', 'cod', 'MOILAN1', 'SSG-S22U-TPU*1;UGR-AC3A*1', NULL, NULL, NULL),
        (8, 74, 210, 'bui.thao@example.com', 'hoan_thanh', 'payos', NULL, 'SSG-BUDS2P-TRANG*1', NULL, NULL, NULL),
        (9, 72, 330, 'hoang.tuan@example.com', 'hoan_thanh', 'cod', 'GIAM50K', 'JBL-CHG4-DEN*1', NULL, NULL, 'Gọi trước khi giao'),
        (10, 69, 120, 'nguyen.lan@example.com', 'hoan_thanh', 'payos', NULL, 'BAS-CC60-2M*2;SSG-25W*1', NULL, NULL, NULL),
        (11, 64, 500, 'do.ngoc@example.com', 'da_huy', 'payos', NULL, 'XMI-PB10K-PRO*1', 'tu_huy', NULL, NULL),
        (12, 63, 280, 'tran.minh@example.com', 'hoan_thanh', 'cod', NULL, 'XMI-PB10K-PRO*1;BAS-CC60-1M*1', NULL, NULL, NULL),
        (13, 61, 190, 'khach@techgear.vn', 'hoan_thanh', 'payos', 'GIAM10', 'SSG-KCL-A04E*1;SSG-BUDS2P-DEN*1', NULL, NULL, NULL),
        (14, 59, 360, 'le.huong@example.com', 'hoan_thanh', 'cod', NULL, 'JBL-GO2-XANH*1', NULL, NULL, NULL),
        (15, 57, 100, 'ngo.long@example.com', 'da_huy', 'cod', NULL, 'JBL-GO2-DEN*3', 'qt_huy',
         'Gọi 3 lần không liên lạc được khách', NULL),
        (16, 54, 230, 'pham.duc@example.com', 'hoan_thanh', 'payos', NULL, 'UGR-PRIV16*2;LOG-M331-DEN*1', NULL, NULL, NULL),
        (17, 51, 420, 'vo.mai@example.com', 'hoan_thanh', 'cod', 'MOILAN1', 'JBL-GO2-DEN*1', NULL, NULL, NULL),
        (18, 49, 140, 'ly.huy@example.com', 'hoan_thanh', 'payos', NULL, 'LOG-G304*1', NULL, NULL, NULL),
        (19, 44, 300, 'bui.thao@example.com', 'hoan_thanh', 'cod', NULL, 'SNY-XB13*1;UGR-AC3A*1', NULL, NULL, NULL),
        (20, 42, 250, 'hoang.tuan@example.com', 'da_huy', 'payos', NULL, 'LOG-K380*1', 'hoan_tien',
         'Khách đổi ý sau khi thanh toán, đã hoàn tiền chuyển khoản', NULL),
        (21, 39, 180, 'nguyen.lan@example.com', 'hoan_thanh', 'cod', NULL, 'ANK-PL3-1M*1;SSG-25W*1', NULL, NULL, NULL),
        (22, 36, 440, 'dang.khoa@example.com', 'hoan_thanh', 'payos', 'GIAM10', 'LOG-G304*1;LOG-M331-DO*1', NULL, NULL, NULL),
        (23, 33, 160, 'do.ngoc@example.com', 'hoan_thanh', 'cod', NULL, 'JBL-CHG4-DEN*1', NULL, NULL, NULL),
        (24, 29, 350, 'tran.minh@example.com', 'hoan_thanh', 'payos', NULL, 'ANK-NANO65-DEN*1;UGR-AC3A*1', NULL, NULL, NULL),
        (25, 27, 220, 'khach@techgear.vn', 'hoan_thanh', 'cod', 'MOILAN1', 'APL-20W*1', NULL, NULL, NULL),
        (26, 24, 310, 'le.huong@example.com', 'hoan_thanh', 'payos', NULL, 'UGR-CC100-1M*2', 'lan2', NULL, NULL),
        (27, 21, 130, 'phan.hong@example.com', 'da_huy', 'cod', NULL, 'SSG-PB10K*1', 'qt_huy', 'Khách gọi điện xin hủy', NULL),
        (28, 19, 270, 'pham.duc@example.com', 'hoan_thanh', 'cod', NULL, 'BAS-GAN5-100-DEN*1', NULL, NULL, NULL),
        (29, 17, 390, 'vo.mai@example.com', 'hoan_thanh', 'payos', 'GIAM50K', 'ANK-PC10K-XANH*1;ANK-NANO65-TRANG*1', NULL, NULL, NULL),
        (30, 15, 110, 'bui.thao@example.com', 'hoan_thanh', 'cod', NULL, 'ANK-PC10K-DEN*1', NULL, NULL, NULL),
        (31, 13, 340, 'hoang.tuan@example.com', 'hoan_thanh', 'payos', NULL, 'LOG-MX3S-DEN*1', NULL, NULL, NULL),
        (32, 11, 200, 'nguyen.lan@example.com', 'hoan_thanh', 'payos', 'GIAM10', 'APL-APP2*1', NULL, NULL, NULL),
        (33, 9, 450, 'dang.khoa@example.com', 'hoan_thanh', 'cod', NULL, 'SNY-XM5-DEN*1', NULL, NULL, NULL),
        (34, 7, 170, 'do.ngoc@example.com', 'hoan_thanh', 'cod', NULL, 'SSG-BUDS3-TRANG*1', NULL, NULL, NULL),
        (35, 6, 290, 'tran.minh@example.com', 'da_huy', 'cod', NULL, 'APL-SC17*1', 'khach_huy', NULL, NULL),
        (36, 5, 380, 'khach@techgear.vn', 'hoan_thanh', 'payos', NULL, 'LOG-MXMINI-TAC*1', NULL, NULL, NULL),
        (37, 3, 240, 'le.huong@example.com', 'dang_giao', 'cod', NULL, 'LOG-M705*1;UGR-CAR30*1', NULL, NULL, NULL),
        (38, 2, 330, 'pham.duc@example.com', 'dang_giao', 'payos', NULL, 'UGR-OP16P*1;APL-SC17*1', NULL, NULL, NULL),
        (39, 1, 400, 'vo.mai@example.com', 'da_xac_nhan', 'cod', 'GIAM10', 'SNY-XM5-BAC*1', NULL, NULL, NULL),
        (40, 1, 150, 'ly.huy@example.com', 'da_xac_nhan', 'payos', NULL, 'LOG-MX3S-XAM*1', NULL, NULL, NULL),
        (41, 0, 180, 'bui.thao@example.com', 'cho_xac_nhan', 'cod', NULL, 'KEY-K8-RED*1', NULL, NULL, NULL),
        (42, 0, 60, 'hoang.tuan@example.com', 'cho_xac_nhan', 'payos', NULL, 'KEY-K8-BROWN*1;LOG-M331-DEN*1', NULL, NULL, NULL),
        (43, 0, 30, 'nguyen.lan@example.com', 'cho_xac_nhan', 'cod', 'MOILAN1', 'UGR-CC100-2M*1', NULL, NULL, NULL);

    OPEN don_cur;
    moi_don: LOOP
        FETCH don_cur INTO d_stt, d_ngay, d_phut, d_email, d_trang_thai, d_pttt, d_ma_giam, d_dong, d_kieu,
                           d_ghi_chu_huy, d_ghi_chu;
        IF het THEN
            LEAVE moi_don;
        END IF;
        SET t = now() - INTERVAL d_ngay DAY - INTERVAL d_phut MINUTE;

        -- Người đặt: tài khoản (địa chỉ mặc định) hoặc khách vãng lai
        SET nguoi_dat = (SELECT n.id FROM nguoi_dung n JOIN dia_chi dc ON dc.nguoi_dung_id = n.id AND dc.la_mac_dinh
                         WHERE n.email = d_email);
        IF nguoi_dat IS NOT NULL THEN
            SELECT dc.ten_nguoi_nhan, dc.so_dien_thoai, CONCAT(dc.dia_chi_chi_tiet, ', ', dc.phuong_xa, ', ', dc.tinh_thanh)
            INTO ten_nhan, v_sdt, dia_chi_gh
            FROM dia_chi dc WHERE dc.nguoi_dung_id = nguoi_dat AND dc.la_mac_dinh;
        ELSEIF EXISTS (SELECT 1 FROM khach_vang_lai WHERE email = d_email) THEN
            SELECT ten, sdt, dia_chi INTO ten_nhan, v_sdt, dia_chi_gh FROM khach_vang_lai WHERE email = d_email;
        ELSE
            SET loi = CONCAT('Đơn mẫu ', d_stt, ': không có khách ', d_email);
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = loi;
        END IF;

        -- Dòng đơn: giá, tên, phân loại, ảnh lấy như lúc đặt hàng thật (HienThi.PHAN_LOAI, ANH_BIEN_THE)
        DELETE FROM dong_don;
        INSERT INTO dong_don
        SELECT SUBSTRING_INDEX(j.x, '*', 1), CAST(SUBSTRING_INDEX(j.x, '*', -1) AS SIGNED), bt.id, bt.san_pham_id, sp.ten,
               (SELECT GROUP_CONCAT(gt.gia_tri ORDER BY tc.thu_tu, tc.id SEPARATOR ' / ')
                FROM bien_the_gia_tri btg
                JOIN gia_tri_tuy_chon gt ON gt.id = btg.gia_tri_tuy_chon_id
                JOIN tuy_chon tc ON tc.id = gt.tuy_chon_id
                WHERE btg.bien_the_id = bt.id),
               (SELECT ha.duong_dan FROM hinh_anh_san_pham ha WHERE ha.san_pham_id = bt.san_pham_id
                ORDER BY ha.la_anh_chinh DESC, ha.thu_tu, ha.id LIMIT 1),
               bt.gia_ban
        -- "A*1;B*2" thành các dòng qua mảng JSON ["A*1","B*2"]
        FROM JSON_TABLE(CONCAT('["', REPLACE(d_dong, ';', '","'), '"]'), '$[*]' COLUMNS (x VARCHAR(100) PATH '$')) j
        LEFT JOIN bien_the bt ON bt.sku = SUBSTRING_INDEX(j.x, '*', 1) AND bt.deleted_at IS NULL
        LEFT JOIN san_pham sp ON sp.id = bt.san_pham_id;
        SELECT GROUP_CONCAT(sku SEPARATOR ', ') INTO loi FROM dong_don WHERE bien_the_id IS NULL;
        IF loi IS NOT NULL THEN
            SET loi = CONCAT('Đơn mẫu ', d_stt, ': không có SKU ', loi);
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = loi;
        END IF;
        SELECT GROUP_CONCAT(dd.sku SEPARATOR ', ') INTO loi
        FROM dong_don dd JOIN bien_the bt ON bt.id = dd.bien_the_id
        WHERE NOT EXISTS (SELECT 1 FROM lich_su_ton_kho ls
                          WHERE ls.bien_the_id = bt.id AND ls.loai = 'khoi_tao' AND ls.created_at < t);
        IF loi IS NOT NULL THEN
            SET loi = LEFT(CONCAT('Đơn mẫu ', d_stt, ': ', loi, ' chưa nhập kho trước lúc đặt'), 128);
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = loi;
        END IF;
        SET tam = (SELECT sum(don_gia * so_luong) FROM dong_don);

        -- Mã giảm giá: kiểm như MaGiamGia.loiKhiApDung, tính như MaGiamGia.tienGiam (DIV: chia nguyên như Java)
        SET giam = 0;
        SET mgg_id = NULL;
        IF d_ma_giam IS NOT NULL THEN
            SET mgg_id = (SELECT id FROM ma_giam_gia WHERE ma = d_ma_giam AND deleted_at IS NULL);
            IF mgg_id IS NULL THEN
                SET loi = CONCAT('Đơn mẫu ', d_stt, ': không có mã ', d_ma_giam);
                SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = loi;
            END IF;
            SELECT kich_hoat, bat_dau_luc, ket_thuc_luc, don_toi_thieu, tong_luot_dung, da_dung, luot_dung_moi_nguoi,
                   loai_giam, gia_tri_giam, giam_toi_da
            INTO m_kich_hoat, m_bat_dau, m_ket_thuc, m_don_toi_thieu, m_tong_luot, m_da_dung, m_moi_nguoi,
                 m_loai, m_gia_tri, m_giam_toi_da
            FROM ma_giam_gia WHERE id = mgg_id;
            IF NOT m_kich_hoat OR t < m_bat_dau OR t >= m_ket_thuc OR tam < m_don_toi_thieu
               OR (m_tong_luot IS NOT NULL AND m_da_dung >= m_tong_luot)
               OR (m_moi_nguoi IS NOT NULL AND m_moi_nguoi <=
                   (SELECT count(*) FROM don_hang dh WHERE dh.ma_giam_gia_id = mgg_id AND dh.trang_thai <> 'da_huy'
                    AND (dh.nguoi_dung_id = nguoi_dat OR (nguoi_dat IS NULL AND dh.email = d_email)))) THEN
                SET loi = CONCAT('Đơn mẫu ', d_stt, ': không áp được mã ', d_ma_giam);
                SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = loi;
            END IF;
            SET giam = CASE WHEN m_loai = 'phan_tram'
                            THEN tam DIV 100 * m_gia_tri + tam % 100 * m_gia_tri DIV 100
                            ELSE m_gia_tri END;
            SET giam = least(giam, coalesce(m_giam_toi_da, giam), tam);
        END IF;
        SET phi_ship = CASE WHEN tam - giam >= mien_phi THEN 0 ELSE phi END;
        SET tong = tam - giam + phi_ship;

        -- Mốc thời gian theo trạng thái cuối
        SET t_huy = CASE d_kieu WHEN 'khach_huy' THEN t + INTERVAL 2 HOUR
                                WHEN 'qt_huy' THEN t + INTERVAL 5 HOUR
                                WHEN 'hoan_tien' THEN t + INTERVAL 6 HOUR
                                WHEN 'tu_huy' THEN t + INTERVAL 35 MINUTE END;
        SET t_cuoi = CASE d_trang_thai WHEN 'cho_xac_nhan' THEN t + INTERVAL 3 MINUTE
                                       WHEN 'da_xac_nhan' THEN t + INTERVAL 2 HOUR
                                       WHEN 'dang_giao' THEN t + INTERVAL 1 DAY
                                       WHEN 'hoan_thanh' THEN t + INTERVAL 3 DAY
                                       ELSE t_huy END;
        IF t_cuoi >= now() THEN
            SET loi = CONCAT('Đơn mẫu ', d_stt, ': mốc cuối nằm ở tương lai');
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = loi;
        END IF;
        SET tt_tt = CASE WHEN d_kieu = 'hoan_tien' THEN 'da_hoan_tien'
                         WHEN d_pttt = 'payos' AND NOT (d_kieu <=> 'tu_huy') THEN 'da_thanh_toan'
                         WHEN d_pttt = 'cod' AND d_trang_thai = 'hoan_thanh' THEN 'da_thanh_toan'
                         ELSE 'chua_thanh_toan' END;

        -- t đã là giờ Việt Nam
        SET ma_don = CONCAT('DH', DATE_FORMAT(t, '%y%m%d'),
                            (WITH RECURSIVE s (i) AS (SELECT 1 UNION ALL SELECT i + 1 FROM s WHERE i < 6)
                             SELECT GROUP_CONCAT(SUBSTR(ky_tu, (d_stt * 7 + i * 13) % 32 + 1, 1) ORDER BY i SEPARATOR '')
                             FROM s));

        INSERT INTO don_hang (ma_don_hang, nguoi_dung_id, ma_giam_gia_id, ten_nguoi_nhan, email, so_dien_thoai,
                              dia_chi_giao_hang, ghi_chu, tam_tinh, tien_giam_gia, phi_van_chuyen, tong_tien,
                              phuong_thuc_thanh_toan, trang_thai_thanh_toan, trang_thai, created_at, updated_at)
        VALUES (ma_don, nguoi_dat, mgg_id, ten_nhan, d_email, v_sdt, dia_chi_gh, d_ghi_chu, tam, giam, phi_ship, tong,
                d_pttt, tt_tt, d_trang_thai, t, t_cuoi);
        SET v_don_id = LAST_INSERT_ID();
        INSERT INTO don_da_tao VALUES (d_stt, v_don_id);

        INSERT INTO chi_tiet_don_hang (don_hang_id, san_pham_id, bien_the_id, ten_san_pham, sku, phan_loai, hinh_anh,
                                       don_gia, so_luong, thanh_tien)
        SELECT v_don_id, san_pham_id, bien_the_id, ten, sku, phan_loai, hinh_anh, don_gia, so_luong, don_gia * so_luong
        FROM dong_don;
        -- Trừ kho rồi ghi lịch sử kèm tồn kho sau (mỗi SKU một dòng trong đơn; sai thì bước tự kiểm 1 báo)
        UPDATE bien_the bt JOIN dong_don dd ON dd.bien_the_id = bt.id
        SET bt.so_luong_ton = bt.so_luong_ton - dd.so_luong, bt.updated_at = t;
        INSERT INTO lich_su_ton_kho (bien_the_id, don_hang_id, loai, so_luong_thay_doi, so_luong_sau, nguoi_thuc_hien_id, created_at)
        SELECT dd.bien_the_id, v_don_id, 'ban_hang', -dd.so_luong, bt.so_luong_ton, nguoi_dat, t
        FROM dong_don dd JOIN bien_the bt ON bt.id = dd.bien_the_id;
        IF mgg_id IS NOT NULL THEN
            UPDATE ma_giam_gia SET da_dung = da_dung + 1 WHERE id = mgg_id;
        END IF;

        -- Lịch sử trạng thái đơn
        INSERT INTO lich_su_don_hang (don_hang_id, trang_thai, ghi_chu, nguoi_thuc_hien_id, created_at)
        VALUES (v_don_id, 'cho_xac_nhan', 'Đặt hàng', nguoi_dat, t);
        IF d_trang_thai IN ('da_xac_nhan', 'dang_giao', 'hoan_thanh') OR d_kieu IN ('qt_huy', 'hoan_tien') THEN
            INSERT INTO lich_su_don_hang (don_hang_id, trang_thai, ghi_chu, nguoi_thuc_hien_id, created_at)
            VALUES (v_don_id, 'da_xac_nhan', NULL, quan_tri, t + INTERVAL 2 HOUR);
        END IF;
        IF d_trang_thai IN ('dang_giao', 'hoan_thanh') THEN
            INSERT INTO lich_su_don_hang (don_hang_id, trang_thai, ghi_chu, nguoi_thuc_hien_id, created_at)
            VALUES (v_don_id, 'dang_giao', 'Bàn giao cho đơn vị vận chuyển', quan_tri, t + INTERVAL 1 DAY);
        END IF;
        IF d_trang_thai = 'hoan_thanh' THEN
            INSERT INTO lich_su_don_hang (don_hang_id, trang_thai, ghi_chu, nguoi_thuc_hien_id, created_at)
            VALUES (v_don_id, 'hoan_thanh', 'Giao hàng thành công', quan_tri, t + INTERVAL 3 DAY);
        END IF;

        -- Hủy: hoàn kho, trả lượt mã (như DonHangService.huy)
        IF d_trang_thai = 'da_huy' THEN
            UPDATE bien_the bt JOIN dong_don dd ON dd.bien_the_id = bt.id
            SET bt.so_luong_ton = bt.so_luong_ton + dd.so_luong, bt.updated_at = t_huy;
            INSERT INTO lich_su_ton_kho (bien_the_id, don_hang_id, loai, so_luong_thay_doi, so_luong_sau, nguoi_thuc_hien_id, created_at)
            SELECT dd.bien_the_id, v_don_id, 'huy_don', dd.so_luong, bt.so_luong_ton,
                   CASE d_kieu WHEN 'khach_huy' THEN nguoi_dat WHEN 'tu_huy' THEN NULL ELSE quan_tri END, t_huy
            FROM dong_don dd JOIN bien_the bt ON bt.id = dd.bien_the_id;
            IF mgg_id IS NOT NULL THEN
                UPDATE ma_giam_gia SET da_dung = da_dung - 1 WHERE id = mgg_id;
            END IF;
            INSERT INTO lich_su_don_hang (don_hang_id, trang_thai, ghi_chu, nguoi_thuc_hien_id, created_at)
            VALUES (v_don_id, 'da_huy',
                    coalesce(d_ghi_chu_huy, CASE d_kieu WHEN 'khach_huy' THEN 'Khách hủy đơn'
                                                        WHEN 'tu_huy' THEN 'Quá 30 phút chưa thanh toán PayOS, hệ thống tự hủy'
                                                        ELSE 'Quản trị hủy đơn' END),
                    CASE d_kieu WHEN 'khach_huy' THEN nguoi_dat WHEN 'tu_huy' THEN NULL ELSE quan_tri END, t_huy);
        END IF;
        IF d_trang_thai = 'hoan_thanh' AND d_pttt = 'cod' THEN
            UPDATE don_hang SET ghi_chu_quan_tri = 'Đã thu tiền COD' WHERE id = v_don_id;
        END IF;

        -- Giao dịch PayOS: orderCode = id đơn × 100 + lần thử (như ThanhToanService); dữ liệu phản hồi dạng webhook PayOS
        IF d_pttt = 'payos' THEN
            IF d_kieu = 'lan2' THEN
                INSERT INTO thanh_toan (don_hang_id, phuong_thuc, so_tien, ma_tham_chieu, trang_thai, du_lieu_phan_hoi,
                                        thanh_toan_luc, created_at, updated_at)
                VALUES (v_don_id, 'payos', tong, CAST(v_don_id * 100 + 1 AS CHAR), 'that_bai',
                        JSON_OBJECT('orderCode', v_don_id * 100 + 1, 'amount', tong, 'amountPaid', 0, 'status', 'CANCELLED'),
                        NULL, t + INTERVAL 1 MINUTE, t + INTERVAL 2 MINUTE);
            END IF;
            INSERT INTO thanh_toan (don_hang_id, phuong_thuc, so_tien, ma_tham_chieu, ma_giao_dich, trang_thai,
                                    du_lieu_phan_hoi, thanh_toan_luc, created_at, updated_at)
            SELECT v_don_id, 'payos', tong, CAST(v_don_id * 100 + CASE WHEN d_kieu = 'lan2' THEN 2 ELSE 1 END AS CHAR),
                   CASE WHEN d_kieu = 'tu_huy' THEN NULL ELSE CONCAT('FT', 26000000 + d_stt * 7919) END,
                   CASE WHEN d_kieu = 'tu_huy' THEN 'cho_xu_ly' ELSE 'thanh_cong' END,
                   CASE WHEN d_kieu = 'tu_huy' THEN NULL
                        ELSE JSON_OBJECT('code', '00', 'desc', 'success', 'success', TRUE,
                                         'data', JSON_OBJECT(
                                             'orderCode', v_don_id * 100 + CASE WHEN d_kieu = 'lan2' THEN 2 ELSE 1 END,
                                             'amount', tong, 'description', CONCAT('TG', v_don_id * 100 + CASE WHEN d_kieu = 'lan2' THEN 2 ELSE 1 END),
                                             'reference', CONCAT('FT', 26000000 + d_stt * 7919),
                                             'transactionDateTime', DATE_FORMAT(t + INTERVAL 3 MINUTE, '%Y-%m-%d %H:%i:%s'),
                                             'currency', 'VND', 'code', '00', 'desc', 'Thành công')) END,
                   CASE WHEN d_kieu = 'tu_huy' THEN NULL ELSE t + INTERVAL 3 MINUTE END,
                   t + INTERVAL 2 MINUTE, t + INTERVAL 3 MINUTE;
        END IF;
    END LOOP;
    CLOSE don_cur;

    -- Đánh giá trên đơn đã hoàn thành (stt đơn, SKU đã mua, số sao, nội dung, trạng thái)
    INSERT INTO danh_gia (san_pham_id, nguoi_dung_id, don_hang_id, so_sao, noi_dung, trang_thai, created_at, updated_at)
    SELECT ct.san_pham_id, dh.nguoi_dung_id, dh.id, v.sao, v.noi_dung, v.tt,
           dh.updated_at + INTERVAL (v.stt % 3 + 1) DAY, dh.updated_at + INTERVAL (v.stt % 3 + 1) DAY
    FROM (VALUES ROW(1, 'LOG-K380', 5, 'Gõ êm, kết nối nhanh với cả laptop và điện thoại.', 'da_duyet'),
                 ROW(1, 'UGR-AC3A', 4, 'Cáp chắc chắn, sạc ổn định.', 'da_duyet'),
                 ROW(2, 'SSG-PB10K', 5, 'Pin trâu, vỏ kim loại cầm rất chắc tay.', 'da_duyet'),
                 ROW(3, 'LOG-M331-DO', 5, 'Click êm thật sự, dùng trong thư viện không làm phiền ai.', 'da_duyet'),
                 ROW(4, 'SNY-CH510', 4, 'Nhẹ, đeo lâu không đau tai, bass hơi ít.', 'da_duyet'),
                 ROW(7, 'SSG-S22U-TPU', 3, 'Ốp ổn nhưng sau một tháng hơi ngả màu.', 'da_duyet'),
                 ROW(8, 'SSG-BUDS2P-TRANG', 5, 'Chống ồn tốt, kết nối với điện thoại Samsung rất nhanh.', 'da_duyet'),
                 ROW(9, 'JBL-CHG4-DEN', 5, 'Âm thanh to, pin dùng cả buổi dã ngoại.', 'da_duyet'),
                 ROW(10, 'BAS-CC60-2M', 4, 'Cáp dài 2m tiện, sạc laptop được.', 'da_duyet'),
                 ROW(12, 'XMI-PB10K-PRO', 4, 'Sạc nhanh, hơi nặng.', 'da_duyet'),
                 ROW(13, 'SSG-BUDS2P-DEN', 4, 'Tai nghe tốt, giao hàng nhanh.', 'da_duyet'),
                 ROW(13, 'SSG-KCL-A04E', 2, 'Dán bị bọt khí ở góc, phải dán lại.', 'da_duyet'),
                 ROW(14, 'JBL-GO2-XANH', 5, 'Nhỏ xinh, âm lượng bất ngờ.', 'da_duyet'),
                 ROW(16, 'UGR-PRIV16', 4, 'Chống nhìn trộm tốt, màn hình hơi tối hơn.', 'da_duyet'),
                 ROW(17, 'JBL-GO2-DEN', 1, 'Mua giá rẻ hơn liên hệ zalo 09xx!!!', 'da_an'),
                 ROW(19, 'SNY-XB13', 4, 'Loa nhỏ mà bass khá.', 'da_duyet'),
                 ROW(21, 'ANK-PL3-1M', 5, 'Cáp chuẩn MFi, không báo lỗi.', 'da_duyet'),
                 ROW(22, 'LOG-G304', 5, 'Chuột nhẹ, chơi game mượt.', 'da_duyet'),
                 ROW(23, 'JBL-CHG4-DEN', 4, 'Âm hay, giá hơi cao.', 'da_duyet'),
                 ROW(24, 'ANK-NANO65-DEN', 5, 'Nhỏ gọn mà sạc được laptop 65W.', 'da_duyet'),
                 ROW(25, 'APL-20W', 5, 'Hàng chính hãng, sạc iPhone nhanh.', 'da_duyet'),
                 ROW(26, 'UGR-CC100-1M', 4, 'Cáp bện dù chắc.', 'cho_duyet'),
                 ROW(29, 'ANK-PC10K-XANH', 5, 'Màu xanh đẹp, sạc nhanh.', 'cho_duyet'),
                 ROW(31, 'LOG-MX3S-DEN', 5, 'Chuột tốt nhất mình từng dùng, cuộn MagSpeed rất đã.', 'da_duyet'),
                 ROW(32, 'APL-APP2', 5, 'Chống ồn xuất sắc.', 'da_duyet'),
                 ROW(33, 'SNY-XM5-DEN', 5, 'Đáng tiền, chống ồn số 1.', 'cho_duyet'),
                 ROW(34, 'SSG-BUDS3-TRANG', 3, 'Thiết kế mở nên chống ồn không bằng loại nhét tai.', 'cho_duyet')
         ) v(stt, sku, sao, noi_dung, tt)
    JOIN don_da_tao dt ON dt.stt = v.stt
    JOIN don_hang dh ON dh.id = dt.don_id AND dh.trang_thai = 'hoan_thanh' AND dh.nguoi_dung_id IS NOT NULL
    JOIN chi_tiet_don_hang ct ON ct.don_hang_id = dh.id AND ct.sku = v.sku;
    IF (SELECT count(*) FROM danh_gia) <> 27 THEN
        SET loi = CONCAT('Đánh giá mẫu: chỉ nạp được ', (SELECT count(*) FROM danh_gia), ' / 27 (stt/SKU không khớp đơn hoàn thành)');
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = loi;
    END IF;

    -- Giỏ hàng, yêu thích của khach@techgear.vn
    INSERT INTO gio_hang (nguoi_dung_id, bien_the_id, so_luong)
    SELECT n.id, bt.id, v.sl
    FROM (VALUES ROW('LOG-M705', 1), ROW('SSG-25W', 2)) v(sku, sl)
    JOIN bien_the bt ON bt.sku = v.sku AND bt.deleted_at IS NULL
    JOIN nguoi_dung n ON n.email = 'khach@techgear.vn';
    INSERT INTO yeu_thich (nguoi_dung_id, san_pham_id)
    SELECT n.id, sp.id
    FROM (VALUES ROW('tai-nghe-apple-airpods-pro-2'), ROW('ban-phim-co-keychron-k8'), ROW('tai-nghe-sony-wh-1000xm5')) v(slug)
    JOIN san_pham sp ON sp.slug = v.slug AND sp.deleted_at IS NULL
    JOIN nguoi_dung n ON n.email = 'khach@techgear.vn';

    DROP TEMPORARY TABLE don_mau, dong_don, khach_vang_lai, don_da_tao;

    -- ===== Tự kiểm: dữ liệu sai thì dừng (backend không khởi động, lỗi ghi rõ chỗ sai, tối đa 128 ký tự) =====
    -- 1. Lịch sử kho nối tiếp và khớp tồn kho hiện tại
    SELECT GROUP_CONCAT(DISTINCT bt.sku SEPARATOR ', ') INTO loi
    FROM bien_the bt
    JOIN (SELECT bien_the_id, so_luong_sau, so_luong_thay_doi,
                 lag(so_luong_sau) OVER w AS truoc, row_number() OVER w AS thu_tu_dong,
                 count(*) OVER (PARTITION BY bien_the_id) AS so_dong
          FROM lich_su_ton_kho WINDOW w AS (PARTITION BY bien_the_id ORDER BY created_at, id)) ls ON ls.bien_the_id = bt.id
    WHERE ls.so_luong_sau <> coalesce(ls.truoc, 0) + ls.so_luong_thay_doi
       OR (ls.thu_tu_dong = ls.so_dong AND ls.so_luong_sau <> bt.so_luong_ton);
    IF loi IS NOT NULL THEN
        SET loi = LEFT(CONCAT('Lịch sử kho không khớp tồn kho: ', loi), 128);
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = loi;
    END IF;
    -- 2. Tạm tính = tổng dòng (tổng tiền đã có CHECK trong bảng)
    SELECT GROUP_CONCAT(dh.ma_don_hang SEPARATOR ', ') INTO loi
    FROM don_hang dh
    WHERE dh.tam_tinh <> (SELECT sum(thanh_tien) FROM chi_tiet_don_hang ct WHERE ct.don_hang_id = dh.id);
    IF loi IS NOT NULL THEN
        SET loi = LEFT(CONCAT('Tạm tính lệch tổng dòng: ', loi), 128);
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = loi;
    END IF;
    -- 3. Đơn đã thanh toán/hoàn tiền qua PayOS phải có giao dịch thành công khớp số tiền; COD hoàn thành đã thu tiền
    SELECT GROUP_CONCAT(dh.ma_don_hang SEPARATOR ', ') INTO loi
    FROM don_hang dh
    WHERE (dh.phuong_thuc_thanh_toan = 'payos' AND dh.trang_thai_thanh_toan <> 'chua_thanh_toan'
           AND NOT EXISTS (SELECT 1 FROM thanh_toan tt WHERE tt.don_hang_id = dh.id AND tt.trang_thai = 'thanh_cong'
                           AND tt.so_tien = dh.tong_tien))
       OR (dh.phuong_thuc_thanh_toan = 'cod' AND dh.trang_thai = 'hoan_thanh' AND dh.trang_thai_thanh_toan <> 'da_thanh_toan')
       OR (dh.phuong_thuc_thanh_toan = 'payos' AND dh.trang_thai IN ('da_xac_nhan', 'dang_giao', 'hoan_thanh')
           AND dh.trang_thai_thanh_toan <> 'da_thanh_toan');
    IF loi IS NOT NULL THEN
        SET loi = LEFT(CONCAT('Trạng thái thanh toán sai: ', loi), 128);
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = loi;
    END IF;
    -- 4. Lượt dùng mã = số đơn chưa hủy dùng mã
    SELECT GROUP_CONCAT(m.ma SEPARATOR ', ') INTO loi
    FROM ma_giam_gia m
    WHERE m.da_dung <> (SELECT count(*) FROM don_hang dh WHERE dh.ma_giam_gia_id = m.id AND dh.trang_thai <> 'da_huy');
    IF loi IS NOT NULL THEN
        SET loi = LEFT(CONCAT('Lượt dùng mã giảm giá lệch: ', loi), 128);
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = loi;
    END IF;
    -- 5. Lịch sử đơn kết thúc đúng trạng thái hiện tại
    SELECT GROUP_CONCAT(dh.ma_don_hang SEPARATOR ', ') INTO loi
    FROM don_hang dh
    WHERE dh.trang_thai <> (SELECT ls.trang_thai FROM lich_su_don_hang ls WHERE ls.don_hang_id = dh.id
                            ORDER BY ls.created_at DESC, ls.id DESC LIMIT 1);
    IF loi IS NOT NULL THEN
        SET loi = LEFT(CONCAT('Lịch sử đơn không khớp trạng thái: ', loi), 128);
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = loi;
    END IF;
END //
DELIMITER ;
CALL nap_du_lieu_mau_van_hanh();
DROP PROCEDURE nap_du_lieu_mau_van_hanh;
