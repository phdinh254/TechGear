-- =====================================================================
--  CƠ SỞ DỮ LIỆU: WEBSITE THƯƠNG MẠI ĐIỆN TỬ BÁN PHỤ KIỆN CÔNG NGHỆ
--  Tên dự án   : TechGear
--  Sinh viên   : Trần Phú Dinh - CNTT - Đại học Trà Vinh
--  Công nghệ   : ReactJS (frontend) - Java Spring Boot (backend) - MySQL
--  Hệ QTCSDL   : MySQL 8.0.16+ / MariaDB 10.4+ (XAMPP)  (InnoDB, utf8mb4)
--  Quy ước     : tên bảng, tên cột tiếng Việt không dấu, snake_case;
--                các cột kỹ thuật dùng tên tiếng Anh: id, password,
--                created_at, updated_at, deleted_at (xóa mềm)
--  Xác thực    : Spring Security + JWT (không lưu phiên trên máy chủ)
--  Ghi chú     : Hibernate dùng chế độ ddl-auto = none, toàn bộ bảng
--                được tạo bằng file này (không để JPA tự sinh bảng).
-- =====================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS `techgear`;
CREATE DATABASE `techgear`
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `techgear`;

-- =====================================================================
-- 1. NHÓM NGƯỜI DÙNG
-- =====================================================================

CREATE TABLE `nguoi_dung` (
  `id`                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Mã người dùng',
  `ho_ten`            VARCHAR(100)    NOT NULL COMMENT 'Họ và tên',
  `email`             VARCHAR(150)    NOT NULL COMMENT 'Email đăng nhập (duy nhất)',
  `password`          VARCHAR(255)    NOT NULL COMMENT 'Mật khẩu đã mã hóa BCrypt',
  `so_dien_thoai`     VARCHAR(20)     NULL     COMMENT 'Số điện thoại',
  `gioi_tinh`         ENUM('nam','nu','khac') NULL COMMENT 'Giới tính',
  `ngay_sinh`         DATE            NULL     COMMENT 'Ngày sinh',
  `anh_dai_dien`      VARCHAR(255)    NULL     COMMENT 'Đường dẫn ảnh đại diện',
  `vai_tro`           ENUM('quan_tri','khach_hang') NOT NULL DEFAULT 'khach_hang' COMMENT 'Vai trò',
  `trang_thai`        ENUM('hoat_dong','bi_khoa')   NOT NULL DEFAULT 'hoat_dong'  COMMENT 'Trạng thái tài khoản',
  `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo',
  `updated_at`        DATETIME        NULL     COMMENT 'Ngày cập nhật',
  `deleted_at`        DATETIME        NULL     COMMENT 'Ngày xóa mềm',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_nguoi_dung_email` (`email`),
  KEY `idx_nguoi_dung_vai_tro` (`vai_tro`, `trang_thai`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Tài khoản quản trị viên và khách hàng';

CREATE TABLE `ma_dat_lai_mat_khau` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Mã',
  `nguoi_dung_id` BIGINT UNSIGNED NOT NULL COMMENT 'Người dùng yêu cầu',
  `ma`            VARCHAR(100)    NOT NULL COMMENT 'Chuỗi mã ngẫu nhiên gửi qua email',
  `het_han_luc`   DATETIME        NOT NULL COMMENT 'Thời điểm hết hạn',
  `da_su_dung`    TINYINT(1)      NOT NULL DEFAULT 0 COMMENT 'Đã dùng (1) / Chưa (0)',
  `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ma_dat_lai` (`ma`),
  KEY `idx_mdlmk_nguoi_dung` (`nguoi_dung_id`),
  CONSTRAINT `fk_mdlmk_nguoi_dung` FOREIGN KEY (`nguoi_dung_id`)
    REFERENCES `nguoi_dung` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Mã đặt lại mật khẩu (chức năng quên mật khẩu)';

CREATE TABLE `dia_chi` (
  `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Mã địa chỉ',
  `nguoi_dung_id`    BIGINT UNSIGNED NOT NULL COMMENT 'Người dùng sở hữu',
  `ten_nguoi_nhan`   VARCHAR(100)    NOT NULL COMMENT 'Tên người nhận',
  `so_dien_thoai`    VARCHAR(20)     NOT NULL COMMENT 'Số điện thoại người nhận',
  `tinh_thanh`       VARCHAR(100)    NOT NULL COMMENT 'Tỉnh / thành phố',
  `phuong_xa`        VARCHAR(100)    NOT NULL COMMENT 'Phường / xã / đặc khu',
  `dia_chi_chi_tiet` VARCHAR(255)    NOT NULL COMMENT 'Số nhà, tên đường, ấp/khóm',
  `la_mac_dinh`      TINYINT(1)      NOT NULL DEFAULT 0 COMMENT 'Địa chỉ mặc định (1 = có)',
  `created_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo',
  `updated_at`       DATETIME        NULL COMMENT 'Ngày cập nhật',
  PRIMARY KEY (`id`),
  KEY `idx_dia_chi_nguoi_dung` (`nguoi_dung_id`),
  CONSTRAINT `fk_dia_chi_nguoi_dung` FOREIGN KEY (`nguoi_dung_id`)
    REFERENCES `nguoi_dung` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Sổ địa chỉ giao hàng của khách hàng';

-- =====================================================================
-- 2. NHÓM DANH MỤC – THƯƠNG HIỆU
-- =====================================================================

CREATE TABLE `danh_muc` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Mã danh mục',
  `danh_muc_cha_id` BIGINT UNSIGNED NULL     COMMENT 'Danh mục cha (NULL = cấp gốc)',
  `ten`             VARCHAR(100)    NOT NULL COMMENT 'Tên danh mục',
  `slug`            VARCHAR(120)    NOT NULL COMMENT 'Đường dẫn thân thiện (duy nhất)',
  `mo_ta`           TEXT            NULL     COMMENT 'Mô tả',
  `hinh_anh`        VARCHAR(255)    NULL     COMMENT 'Ảnh đại diện danh mục',
  `thu_tu`          INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT 'Thứ tự hiển thị',
  `hien_thi`        TINYINT(1)      NOT NULL DEFAULT 1 COMMENT 'Hiển thị (1) / Ẩn (0)',
  `created_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo',
  `updated_at`      DATETIME        NULL COMMENT 'Ngày cập nhật',
  `deleted_at`      DATETIME        NULL COMMENT 'Ngày xóa mềm',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_danh_muc_slug` (`slug`),
  KEY `idx_danh_muc_cha` (`danh_muc_cha_id`),
  CONSTRAINT `fk_danh_muc_cha` FOREIGN KEY (`danh_muc_cha_id`)
    REFERENCES `danh_muc` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Danh mục sản phẩm, hỗ trợ cấp cha - con';

CREATE TABLE `thuong_hieu` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Mã thương hiệu',
  `ten`        VARCHAR(100)    NOT NULL COMMENT 'Tên thương hiệu',
  `slug`       VARCHAR(120)    NOT NULL COMMENT 'Đường dẫn thân thiện (duy nhất)',
  `logo`       VARCHAR(255)    NULL     COMMENT 'Logo thương hiệu',
  `mo_ta`      TEXT            NULL     COMMENT 'Mô tả',
  `hien_thi`   TINYINT(1)      NOT NULL DEFAULT 1 COMMENT 'Hiển thị (1) / Ẩn (0)',
  `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo',
  `updated_at` DATETIME        NULL COMMENT 'Ngày cập nhật',
  `deleted_at` DATETIME        NULL COMMENT 'Ngày xóa mềm',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_thuong_hieu_slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Thương hiệu phụ kiện công nghệ';

-- =====================================================================
-- 3. NHÓM SẢN PHẨM – BIẾN THỂ
--    Sản phẩm (Sạc dự phòng) -> Tùy chọn (Màu sắc, Dung lượng)
--    -> Giá trị tùy chọn (Đen, Trắng / 10000mAh, 20000mAh)
--    -> Biến thể (Đen - 20000mAh): mỗi biến thể có SKU, giá, tồn kho riêng
--    Thông số kỹ thuật (công suất, cổng kết nối...) lưu ở thuoc_tinh_san_pham
-- =====================================================================

CREATE TABLE `san_pham` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Mã sản phẩm',
  `danh_muc_id`    BIGINT UNSIGNED NOT NULL COMMENT 'Danh mục',
  `thuong_hieu_id` BIGINT UNSIGNED NULL     COMMENT 'Thương hiệu',
  `ten`            VARCHAR(255)    NOT NULL COMMENT 'Tên sản phẩm',
  `slug`           VARCHAR(255)    NOT NULL COMMENT 'Đường dẫn thân thiện (duy nhất)',
  `mo_ta_ngan`     VARCHAR(500)    NULL     COMMENT 'Mô tả ngắn',
  `mo_ta`          LONGTEXT        NULL     COMMENT 'Mô tả chi tiết (HTML)',
  `gia_ban`        DECIMAL(12,0) UNSIGNED NOT NULL COMMENT 'Giá bán mặc định (VNĐ)',
  `gia_goc`        DECIMAL(12,0) UNSIGNED NULL     COMMENT 'Giá gốc trước giảm, để hiển thị gạch ngang',
  `bao_hanh_thang` INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT 'Thời gian bảo hành (tháng), 0 = không bảo hành',
  `noi_bat`        TINYINT(1)      NOT NULL DEFAULT 0 COMMENT 'Sản phẩm nổi bật',
  `hien_thi`       TINYINT(1)      NOT NULL DEFAULT 1 COMMENT 'Đang bán (1) / Ngừng bán (0)',
  `luot_xem`       INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT 'Số lượt xem',
  `created_at`     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo',
  `updated_at`     DATETIME        NULL COMMENT 'Ngày cập nhật',
  `deleted_at`     DATETIME        NULL COMMENT 'Ngày xóa mềm',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_san_pham_slug` (`slug`),
  KEY `idx_san_pham_danh_muc` (`danh_muc_id`),
  KEY `idx_san_pham_thuong_hieu` (`thuong_hieu_id`),
  KEY `idx_san_pham_gia` (`gia_ban`),
  CONSTRAINT `fk_san_pham_danh_muc` FOREIGN KEY (`danh_muc_id`)
    REFERENCES `danh_muc` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_san_pham_thuong_hieu` FOREIGN KEY (`thuong_hieu_id`)
    REFERENCES `thuong_hieu` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Thông tin chung của sản phẩm';

CREATE TABLE `thuoc_tinh_san_pham` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Mã thuộc tính',
  `san_pham_id` BIGINT UNSIGNED NOT NULL COMMENT 'Sản phẩm',
  `ten`         VARCHAR(100)    NOT NULL COMMENT 'Tên thông số: Công suất, Cổng kết nối, Pin, Chuẩn Bluetooth...',
  `gia_tri`     VARCHAR(255)    NOT NULL COMMENT 'Giá trị: 65W, USB-C x2 + USB-A, 30 giờ, 5.3...',
  `thu_tu`      INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT 'Thứ tự hiển thị',
  `created_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo',
  `updated_at`  DATETIME        NULL COMMENT 'Ngày cập nhật',
  PRIMARY KEY (`id`),
  KEY `idx_thuoc_tinh_san_pham` (`san_pham_id`),
  CONSTRAINT `fk_thuoc_tinh_san_pham` FOREIGN KEY (`san_pham_id`)
    REFERENCES `san_pham` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Thông số kỹ thuật sản phẩm (không ảnh hưởng giá, tồn kho)';

CREATE TABLE `tuy_chon` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Mã tùy chọn',
  `san_pham_id` BIGINT UNSIGNED NOT NULL COMMENT 'Sản phẩm',
  `ten`         VARCHAR(50)     NOT NULL COMMENT 'Tên hiển thị: Màu sắc, Dung lượng, Phiên bản, Độ dài',
  `loai`        ENUM('mau_sac','dung_luong','phien_ban','khac') NOT NULL DEFAULT 'khac' COMMENT 'Loại tùy chọn (dùng để lọc)',
  `thu_tu`      INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT 'Thứ tự hiển thị',
  `created_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo',
  `updated_at`  DATETIME        NULL COMMENT 'Ngày cập nhật',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_tuy_chon_san_pham_ten` (`san_pham_id`, `ten`),
  CONSTRAINT `fk_tuy_chon_san_pham` FOREIGN KEY (`san_pham_id`)
    REFERENCES `san_pham` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Các tùy chọn của sản phẩm (Màu sắc, Dung lượng, Phiên bản)';

CREATE TABLE `gia_tri_tuy_chon` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Mã giá trị',
  `tuy_chon_id` BIGINT UNSIGNED NOT NULL COMMENT 'Tùy chọn',
  `gia_tri`     VARCHAR(50)     NOT NULL COMMENT 'Giá trị: Đen, Trắng, 10000mAh, 1m, Red switch',
  `ma_mau`      CHAR(7)         NULL     COMMENT 'Mã màu HEX (#000000) nếu là màu sắc',
  `thu_tu`      INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT 'Thứ tự hiển thị',
  `created_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo',
  `updated_at`  DATETIME        NULL COMMENT 'Ngày cập nhật',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_gia_tri_tuy_chon` (`tuy_chon_id`, `gia_tri`),
  KEY `idx_gia_tri` (`gia_tri`),
  CONSTRAINT `fk_gia_tri_tuy_chon` FOREIGN KEY (`tuy_chon_id`)
    REFERENCES `tuy_chon` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Các giá trị của một tùy chọn';

CREATE TABLE `bien_the_san_pham` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Mã biến thể',
  `san_pham_id`     BIGINT UNSIGNED NOT NULL COMMENT 'Sản phẩm',
  `sku`             VARCHAR(100)    NOT NULL COMMENT 'Mã kho (duy nhất), VD: TG-SDP01-20000',
  `gia_ban`         DECIMAL(12,0) UNSIGNED NULL COMMENT 'Giá riêng; NULL = dùng giá sản phẩm',
  `gia_goc`         DECIMAL(12,0) UNSIGNED NULL COMMENT 'Giá gốc riêng; NULL = dùng giá gốc sản phẩm',
  `so_luong_ton`    INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT 'Số lượng tồn kho',
  `nguong_canh_bao` INT UNSIGNED    NOT NULL DEFAULT 5 COMMENT 'Cảnh báo khi tồn kho <= ngưỡng',
  `hien_thi`        TINYINT(1)      NOT NULL DEFAULT 1 COMMENT 'Đang bán (1) / Ngừng bán (0)',
  `created_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo',
  `updated_at`      DATETIME        NULL COMMENT 'Ngày cập nhật',
  `deleted_at`      DATETIME        NULL COMMENT 'Ngày xóa mềm',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_bien_the_sku` (`sku`),
  KEY `idx_bien_the_san_pham` (`san_pham_id`),
  CONSTRAINT `fk_bien_the_san_pham` FOREIGN KEY (`san_pham_id`)
    REFERENCES `san_pham` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Biến thể sản phẩm - đơn vị quản lý giá và tồn kho. Sản phẩm không có tùy chọn vẫn có 1 biến thể mặc định';

CREATE TABLE `bien_the_gia_tri` (
  `bien_the_id`         BIGINT UNSIGNED NOT NULL COMMENT 'Biến thể',
  `gia_tri_tuy_chon_id` BIGINT UNSIGNED NOT NULL COMMENT 'Giá trị tùy chọn',
  PRIMARY KEY (`bien_the_id`, `gia_tri_tuy_chon_id`),
  KEY `idx_bien_the_gia_tri` (`gia_tri_tuy_chon_id`),
  CONSTRAINT `fk_btgt_bien_the` FOREIGN KEY (`bien_the_id`)
    REFERENCES `bien_the_san_pham` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_btgt_gia_tri` FOREIGN KEY (`gia_tri_tuy_chon_id`)
    REFERENCES `gia_tri_tuy_chon` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Bảng trung gian: biến thể gồm những giá trị nào (Đen + 20000mAh)';

CREATE TABLE `hinh_anh_san_pham` (
  `id`                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Mã hình ảnh',
  `san_pham_id`         BIGINT UNSIGNED NOT NULL COMMENT 'Sản phẩm',
  `gia_tri_tuy_chon_id` BIGINT UNSIGNED NULL     COMMENT 'Ảnh theo màu (NULL = ảnh chung)',
  `duong_dan`           VARCHAR(255)    NOT NULL COMMENT 'Đường dẫn file ảnh',
  `mo_ta`               VARCHAR(255)    NULL     COMMENT 'Văn bản thay thế (alt)',
  `thu_tu`              INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT 'Thứ tự hiển thị',
  `la_anh_chinh`        TINYINT(1)      NOT NULL DEFAULT 0 COMMENT 'Ảnh đại diện (1 = có)',
  `created_at`          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo',
  `updated_at`          DATETIME        NULL COMMENT 'Ngày cập nhật',
  PRIMARY KEY (`id`),
  KEY `idx_hinh_anh_san_pham` (`san_pham_id`, `thu_tu`),
  KEY `idx_hinh_anh_gia_tri` (`gia_tri_tuy_chon_id`),
  CONSTRAINT `fk_hinh_anh_san_pham` FOREIGN KEY (`san_pham_id`)
    REFERENCES `san_pham` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_hinh_anh_gia_tri` FOREIGN KEY (`gia_tri_tuy_chon_id`)
    REFERENCES `gia_tri_tuy_chon` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Hình ảnh sản phẩm, có thể gắn theo màu';

-- =====================================================================
-- 4. NHÓM KHUYẾN MÃI – GIAO DIỆN
-- =====================================================================

CREATE TABLE `ma_giam_gia` (
  `id`                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Mã',
  `ma`                  VARCHAR(50)     NOT NULL COMMENT 'Mã giảm giá khách nhập (duy nhất)',
  `mo_ta`               VARCHAR(255)    NULL     COMMENT 'Mô tả chương trình',
  `loai_giam`           ENUM('phan_tram','so_tien') NOT NULL COMMENT 'Giảm theo % hoặc số tiền',
  `gia_tri_giam`        DECIMAL(12,0) UNSIGNED NOT NULL COMMENT 'Số % hoặc số tiền được giảm',
  `don_toi_thieu`       DECIMAL(12,0) UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Giá trị đơn tối thiểu',
  `giam_toi_da`         DECIMAL(12,0) UNSIGNED NULL     COMMENT 'Số tiền giảm tối đa (khi giảm %)',
  `tong_luot_dung`      INT UNSIGNED    NULL     COMMENT 'Tổng lượt dùng tối đa (NULL = không giới hạn)',
  `luot_dung_moi_nguoi` INT UNSIGNED    NULL     COMMENT 'Lượt dùng tối đa mỗi khách (NULL = không giới hạn)',
  `da_dung`             INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT 'Số lượt đã dùng',
  `bat_dau_luc`         DATETIME        NOT NULL COMMENT 'Thời điểm bắt đầu',
  `ket_thuc_luc`        DATETIME        NOT NULL COMMENT 'Thời điểm kết thúc',
  `kich_hoat`           TINYINT(1)      NOT NULL DEFAULT 1 COMMENT 'Đang kích hoạt',
  `created_at`          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo',
  `updated_at`          DATETIME        NULL COMMENT 'Ngày cập nhật',
  `deleted_at`          DATETIME        NULL COMMENT 'Ngày xóa mềm',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ma_giam_gia` (`ma`),
  CONSTRAINT `ck_ma_giam_gia_thoi_gian` CHECK (`ket_thuc_luc` > `bat_dau_luc`),
  CONSTRAINT `ck_ma_giam_gia_phan_tram` CHECK (`loai_giam` <> 'phan_tram' OR `gia_tri_giam` BETWEEN 1 AND 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Mã giảm giá (coupon)';

CREATE TABLE `banner` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Mã banner',
  `tieu_de`      VARCHAR(255)    NULL     COMMENT 'Tiêu đề',
  `hinh_anh`     VARCHAR(255)    NOT NULL COMMENT 'Đường dẫn ảnh',
  `lien_ket`     VARCHAR(255)    NULL     COMMENT 'Liên kết khi bấm vào',
  `thu_tu`       INT UNSIGNED    NOT NULL DEFAULT 0 COMMENT 'Thứ tự hiển thị',
  `hien_thi`     TINYINT(1)      NOT NULL DEFAULT 1 COMMENT 'Hiển thị (1) / Ẩn (0)',
  `bat_dau_luc`  DATETIME        NULL     COMMENT 'Bắt đầu hiển thị (NULL = ngay)',
  `ket_thuc_luc` DATETIME        NULL     COMMENT 'Kết thúc hiển thị (NULL = không hạn)',
  `created_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo',
  `updated_at`   DATETIME        NULL COMMENT 'Ngày cập nhật',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Banner quảng cáo trang chủ';

CREATE TABLE `cai_dat` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Mã cài đặt',
  `khoa`       VARCHAR(100)    NOT NULL COMMENT 'Khóa: ten_cua_hang, hotline, phi_van_chuyen...',
  `gia_tri`    TEXT            NULL     COMMENT 'Giá trị',
  `kieu`       ENUM('chuoi','so','logic','json') NOT NULL DEFAULT 'chuoi' COMMENT 'Kiểu dữ liệu của giá trị',
  `mo_ta`      VARCHAR(255)    NULL     COMMENT 'Mô tả',
  `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo',
  `updated_at` DATETIME        NULL COMMENT 'Ngày cập nhật',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_cai_dat_khoa` (`khoa`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Cấu hình chung của cửa hàng';

-- =====================================================================
-- 5. NHÓM GIỎ HÀNG – YÊU THÍCH
--    Giỏ hàng chỉ lưu cho khách đã đăng nhập. Khách vãng lai lưu giỏ
--    ở localStorage phía React, khi đăng nhập thì gộp vào giỏ trên server.
-- =====================================================================

CREATE TABLE `gio_hang` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Mã giỏ hàng',
  `nguoi_dung_id` BIGINT UNSIGNED NOT NULL COMMENT 'Khách hàng sở hữu giỏ',
  `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo',
  `updated_at`    DATETIME        NULL COMMENT 'Ngày cập nhật',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_gio_hang_nguoi_dung` (`nguoi_dung_id`),
  CONSTRAINT `fk_gio_hang_nguoi_dung` FOREIGN KEY (`nguoi_dung_id`)
    REFERENCES `nguoi_dung` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Giỏ hàng: mỗi khách hàng có một giỏ';

CREATE TABLE `chi_tiet_gio_hang` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Mã dòng',
  `gio_hang_id` BIGINT UNSIGNED NOT NULL COMMENT 'Giỏ hàng',
  `bien_the_id` BIGINT UNSIGNED NOT NULL COMMENT 'Biến thể được chọn',
  `so_luong`    INT UNSIGNED    NOT NULL DEFAULT 1 COMMENT 'Số lượng',
  `created_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo',
  `updated_at`  DATETIME        NULL COMMENT 'Ngày cập nhật',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ctgh_bien_the` (`gio_hang_id`, `bien_the_id`),
  KEY `idx_ctgh_bien_the` (`bien_the_id`),
  CONSTRAINT `fk_ctgh_gio_hang` FOREIGN KEY (`gio_hang_id`)
    REFERENCES `gio_hang` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ctgh_bien_the` FOREIGN KEY (`bien_the_id`)
    REFERENCES `bien_the_san_pham` (`id`) ON DELETE CASCADE,
  CONSTRAINT `ck_ctgh_so_luong` CHECK (`so_luong` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Sản phẩm trong giỏ - giá luôn lấy theo giá hiện tại';

CREATE TABLE `yeu_thich` (
  `nguoi_dung_id` BIGINT UNSIGNED NOT NULL COMMENT 'Khách hàng',
  `san_pham_id`   BIGINT UNSIGNED NOT NULL COMMENT 'Sản phẩm yêu thích',
  `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày thêm',
  PRIMARY KEY (`nguoi_dung_id`, `san_pham_id`),
  KEY `idx_yeu_thich_san_pham` (`san_pham_id`),
  CONSTRAINT `fk_yeu_thich_nguoi_dung` FOREIGN KEY (`nguoi_dung_id`)
    REFERENCES `nguoi_dung` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_yeu_thich_san_pham` FOREIGN KEY (`san_pham_id`)
    REFERENCES `san_pham` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Danh sách sản phẩm yêu thích';

-- =====================================================================
-- 6. NHÓM ĐƠN HÀNG – THANH TOÁN – KHO – ĐÁNH GIÁ
-- =====================================================================

CREATE TABLE `don_hang` (
  `id`                     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Mã đơn hàng (nội bộ)',
  `ma_don_hang`            VARCHAR(30)     NOT NULL COMMENT 'Mã đơn hiển thị, VD: DH20261014001',
  `nguoi_dung_id`          BIGINT UNSIGNED NULL     COMMENT 'Khách đặt (NULL = khách vãng lai)',
  `ma_giam_gia_id`         BIGINT UNSIGNED NULL     COMMENT 'Mã giảm giá đã áp dụng',
  `ten_nguoi_nhan`         VARCHAR(100)    NOT NULL COMMENT 'Tên người nhận',
  `email`                  VARCHAR(150)    NOT NULL COMMENT 'Email nhận thông báo',
  `so_dien_thoai`          VARCHAR(20)     NOT NULL COMMENT 'Số điện thoại người nhận',
  `dia_chi_giao_hang`      VARCHAR(500)    NOT NULL COMMENT 'Địa chỉ giao hàng đầy đủ (lưu lại lúc đặt)',
  `ghi_chu`                VARCHAR(500)    NULL     COMMENT 'Ghi chú của khách',
  `ghi_chu_quan_tri`       VARCHAR(500)    NULL     COMMENT 'Ghi chú nội bộ của quản trị',
  `tam_tinh`               DECIMAL(12,0) UNSIGNED NOT NULL COMMENT 'Tổng tiền hàng',
  `tien_giam_gia`          DECIMAL(12,0) UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Số tiền được giảm',
  `phi_van_chuyen`         DECIMAL(12,0) UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Phí vận chuyển',
  `tong_tien`              DECIMAL(12,0) UNSIGNED NOT NULL COMMENT 'Khách phải trả = tạm tính - giảm giá + phí ship',
  `phuong_thuc_thanh_toan` ENUM('cod','vnpay') NOT NULL DEFAULT 'cod' COMMENT 'Phương thức thanh toán',
  `trang_thai_thanh_toan`  ENUM('chua_thanh_toan','da_thanh_toan','da_hoan_tien') NOT NULL DEFAULT 'chua_thanh_toan' COMMENT 'Trạng thái thanh toán',
  `trang_thai`             ENUM('cho_xac_nhan','da_xac_nhan','dang_giao','hoan_thanh','da_huy') NOT NULL DEFAULT 'cho_xac_nhan' COMMENT 'Trạng thái đơn hàng',
  `created_at`             DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày đặt',
  `updated_at`             DATETIME        NULL COMMENT 'Ngày cập nhật',
  `deleted_at`             DATETIME        NULL COMMENT 'Ngày xóa mềm',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ma_don_hang` (`ma_don_hang`),
  KEY `idx_don_hang_nguoi_dung` (`nguoi_dung_id`, `created_at`),
  KEY `idx_don_hang_trang_thai` (`trang_thai`, `created_at`),
  KEY `idx_don_hang_ma_giam_gia` (`ma_giam_gia_id`),
  CONSTRAINT `fk_don_hang_nguoi_dung` FOREIGN KEY (`nguoi_dung_id`)
    REFERENCES `nguoi_dung` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_don_hang_ma_giam_gia` FOREIGN KEY (`ma_giam_gia_id`)
    REFERENCES `ma_giam_gia` (`id`) ON DELETE SET NULL,
  CONSTRAINT `ck_don_hang_giam_gia` CHECK (`tien_giam_gia` <= `tam_tinh`),
  CONSTRAINT `ck_don_hang_tong_tien` CHECK (`tong_tien` = `tam_tinh` - `tien_giam_gia` + `phi_van_chuyen`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Đơn đặt hàng';

CREATE TABLE `chi_tiet_don_hang` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Mã dòng',
  `don_hang_id`  BIGINT UNSIGNED NOT NULL COMMENT 'Đơn hàng',
  `san_pham_id`  BIGINT UNSIGNED NULL     COMMENT 'Sản phẩm (NULL nếu sản phẩm đã bị xóa hẳn)',
  `bien_the_id`  BIGINT UNSIGNED NULL     COMMENT 'Biến thể (NULL nếu biến thể đã bị xóa hẳn)',
  `ten_san_pham` VARCHAR(255)    NOT NULL COMMENT 'Tên sản phẩm lúc mua',
  `sku`          VARCHAR(100)    NOT NULL COMMENT 'SKU lúc mua',
  `phan_loai`    VARCHAR(100)    NULL     COMMENT 'Phân loại lúc mua, VD: Đen / 20000mAh',
  `hinh_anh`     VARCHAR(255)    NULL     COMMENT 'Ảnh lúc mua',
  `don_gia`      DECIMAL(12,0) UNSIGNED NOT NULL COMMENT 'Đơn giá lúc mua',
  `so_luong`     INT UNSIGNED    NOT NULL COMMENT 'Số lượng',
  `thanh_tien`   DECIMAL(12,0) UNSIGNED NOT NULL COMMENT 'Thành tiền = đơn giá x số lượng',
  `created_at`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo',
  `updated_at`   DATETIME        NULL COMMENT 'Ngày cập nhật',
  PRIMARY KEY (`id`),
  KEY `idx_ctdh_don_hang` (`don_hang_id`),
  KEY `idx_ctdh_san_pham` (`san_pham_id`),
  KEY `idx_ctdh_bien_the` (`bien_the_id`),
  CONSTRAINT `fk_ctdh_don_hang` FOREIGN KEY (`don_hang_id`)
    REFERENCES `don_hang` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ctdh_san_pham` FOREIGN KEY (`san_pham_id`)
    REFERENCES `san_pham` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_ctdh_bien_the` FOREIGN KEY (`bien_the_id`)
    REFERENCES `bien_the_san_pham` (`id`) ON DELETE SET NULL,
  CONSTRAINT `ck_ctdh_so_luong` CHECK (`so_luong` > 0),
  CONSTRAINT `ck_ctdh_thanh_tien` CHECK (`thanh_tien` = `don_gia` * `so_luong`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Chi tiết đơn hàng - lưu lại thông tin tại thời điểm mua';

CREATE TABLE `lich_su_don_hang` (
  `id`                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Mã',
  `don_hang_id`        BIGINT UNSIGNED NOT NULL COMMENT 'Đơn hàng',
  `trang_thai`         ENUM('cho_xac_nhan','da_xac_nhan','dang_giao','hoan_thanh','da_huy') NOT NULL COMMENT 'Trạng thái mới',
  `ghi_chu`            VARCHAR(500)    NULL     COMMENT 'Ghi chú / lý do',
  `nguoi_thuc_hien_id` BIGINT UNSIGNED NULL     COMMENT 'Người đổi trạng thái (NULL = hệ thống)',
  `created_at`         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời điểm',
  PRIMARY KEY (`id`),
  KEY `idx_lsdh_don_hang` (`don_hang_id`, `created_at`),
  KEY `idx_lsdh_nguoi_thuc_hien` (`nguoi_thuc_hien_id`),
  CONSTRAINT `fk_lsdh_don_hang` FOREIGN KEY (`don_hang_id`)
    REFERENCES `don_hang` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_lsdh_nguoi_thuc_hien` FOREIGN KEY (`nguoi_thuc_hien_id`)
    REFERENCES `nguoi_dung` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Lịch sử thay đổi trạng thái đơn hàng';

CREATE TABLE `thanh_toan` (
  `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Mã giao dịch nội bộ',
  `don_hang_id`      BIGINT UNSIGNED NOT NULL COMMENT 'Đơn hàng (1 đơn có thể có nhiều lần thanh toán)',
  `phuong_thuc`      ENUM('cod','vnpay') NOT NULL COMMENT 'Phương thức',
  `so_tien`          DECIMAL(12,0) UNSIGNED NOT NULL COMMENT 'Số tiền',
  `ma_giao_dich`     VARCHAR(100)    NULL     COMMENT 'Mã giao dịch của cổng thanh toán',
  `trang_thai`       ENUM('cho_xu_ly','thanh_cong','that_bai','da_hoan_tien') NOT NULL DEFAULT 'cho_xu_ly' COMMENT 'Trạng thái giao dịch',
  `du_lieu_phan_hoi` JSON            NULL     COMMENT 'Dữ liệu cổng thanh toán trả về',
  `thanh_toan_luc`   DATETIME        NULL     COMMENT 'Thời điểm thanh toán thành công',
  `created_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo',
  `updated_at`       DATETIME        NULL COMMENT 'Ngày cập nhật',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_thanh_toan_ma_gd` (`ma_giao_dich`),
  KEY `idx_thanh_toan_don_hang` (`don_hang_id`),
  CONSTRAINT `fk_thanh_toan_don_hang` FOREIGN KEY (`don_hang_id`)
    REFERENCES `don_hang` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Các lần thanh toán của đơn hàng';

CREATE TABLE `lich_su_ton_kho` (
  `id`                   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Mã',
  `bien_the_id`          BIGINT UNSIGNED NOT NULL COMMENT 'Biến thể',
  `chi_tiet_don_hang_id` BIGINT UNSIGNED NULL     COMMENT 'Dòng đơn hàng liên quan (nếu có)',
  `loai`                 ENUM('khoi_tao','nhap_kho','ban_hang','huy_don','dieu_chinh') NOT NULL COMMENT 'Loại biến động',
  `so_luong_thay_doi`    INT             NOT NULL COMMENT 'Dương = tăng, âm = giảm',
  `so_luong_sau`         INT UNSIGNED    NOT NULL COMMENT 'Tồn kho sau khi thay đổi',
  `ghi_chu`              VARCHAR(255)    NULL     COMMENT 'Ghi chú',
  `nguoi_thuc_hien_id`   BIGINT UNSIGNED NULL     COMMENT 'Người thực hiện (NULL = hệ thống)',
  `created_at`           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời điểm',
  PRIMARY KEY (`id`),
  KEY `idx_lstk_bien_the` (`bien_the_id`, `created_at`),
  KEY `idx_lstk_ctdh` (`chi_tiet_don_hang_id`),
  KEY `idx_lstk_nguoi_thuc_hien` (`nguoi_thuc_hien_id`),
  CONSTRAINT `fk_lstk_bien_the` FOREIGN KEY (`bien_the_id`)
    REFERENCES `bien_the_san_pham` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_lstk_ctdh` FOREIGN KEY (`chi_tiet_don_hang_id`)
    REFERENCES `chi_tiet_don_hang` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_lstk_nguoi_thuc_hien` FOREIGN KEY (`nguoi_thuc_hien_id`)
    REFERENCES `nguoi_dung` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Nhật ký nhập - xuất kho theo biến thể';

CREATE TABLE `danh_gia` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Mã đánh giá',
  `san_pham_id`   BIGINT UNSIGNED NOT NULL COMMENT 'Sản phẩm được đánh giá',
  `nguoi_dung_id` BIGINT UNSIGNED NOT NULL COMMENT 'Khách đánh giá',
  `don_hang_id`   BIGINT UNSIGNED NOT NULL COMMENT 'Đơn hàng đã mua (chỉ khách đã mua mới được đánh giá)',
  `so_sao`        TINYINT UNSIGNED NOT NULL COMMENT 'Số sao từ 1 đến 5',
  `noi_dung`      TEXT            NULL     COMMENT 'Nội dung nhận xét',
  `da_duyet`      TINYINT(1)      NOT NULL DEFAULT 0 COMMENT 'Đã được quản trị duyệt',
  `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Ngày tạo',
  `updated_at`    DATETIME        NULL COMMENT 'Ngày cập nhật',
  `deleted_at`    DATETIME        NULL COMMENT 'Ngày xóa mềm',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_danh_gia` (`nguoi_dung_id`, `san_pham_id`, `don_hang_id`),
  KEY `idx_danh_gia_san_pham` (`san_pham_id`, `da_duyet`),
  KEY `idx_danh_gia_don_hang` (`don_hang_id`),
  CONSTRAINT `fk_danh_gia_san_pham` FOREIGN KEY (`san_pham_id`)
    REFERENCES `san_pham` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_danh_gia_nguoi_dung` FOREIGN KEY (`nguoi_dung_id`)
    REFERENCES `nguoi_dung` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_danh_gia_don_hang` FOREIGN KEY (`don_hang_id`)
    REFERENCES `don_hang` (`id`) ON DELETE CASCADE,
  CONSTRAINT `ck_danh_gia_so_sao` CHECK (`so_sao` BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Đánh giá sản phẩm của khách đã mua';

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- 7. DỮ LIỆU MẪU
--    LƯU Ý: dữ liệu dưới đây chỉ để minh họa khi chạy thử hệ thống.
--    Tên sản phẩm, giá bán và thông số là giả định, KHÔNG phải số liệu
--    chính thức của các hãng.
--    Tài khoản mẫu (mật khẩu đã mã hóa BCrypt):
--      admin@techgear.vn      / Admin@123   (quản trị)
--      khachhang@techgear.vn  / Khach@123   (khách hàng)
-- =====================================================================

INSERT INTO `nguoi_dung` (`id`, `ho_ten`, `email`, `password`, `so_dien_thoai`, `vai_tro`) VALUES
(1, 'Quản trị viên', 'admin@techgear.vn',     '$2a$10$n0hBhTr3qAkhV66kr1lu4ubJN5akvHtMvKvA0MeiqD3mye0kNOVkW', '0900000001', 'quan_tri'),
(2, 'Khách hàng mẫu', 'khachhang@techgear.vn', '$2a$10$9ozo64DaCt/XoPH7WpvbF.PeXoTrQDk4Z3VLk64Kg45c7nopJbqqe', '0900000002', 'khach_hang');

INSERT INTO `danh_muc` (`id`, `danh_muc_cha_id`, `ten`, `slug`, `thu_tu`) VALUES
(1,  NULL, 'Âm thanh',            'am-thanh',            1),
(2,  1,    'Tai nghe',            'tai-nghe',            1),
(3,  1,    'Loa',                 'loa',                 2),
(4,  NULL, 'Sạc & Cáp',           'sac-va-cap',          2),
(5,  4,    'Củ sạc',              'cu-sac',              1),
(6,  4,    'Cáp sạc',             'cap-sac',             2),
(7,  4,    'Sạc dự phòng',        'sac-du-phong',        3),
(8,  NULL, 'Phụ kiện máy tính',   'phu-kien-may-tinh',   3),
(9,  8,    'Chuột',               'chuot',               1),
(10, 8,    'Bàn phím',            'ban-phim',            2),
(11, NULL, 'Ốp lưng & Bảo vệ',    'op-lung-va-bao-ve',   4);

INSERT INTO `thuong_hieu` (`id`, `ten`, `slug`) VALUES
(1, 'Anker',    'anker'),
(2, 'Baseus',   'baseus'),
(3, 'Logitech', 'logitech'),
(4, 'Ugreen',   'ugreen'),
(5, 'Xiaomi',   'xiaomi'),
(6, 'JBL',      'jbl');

INSERT INTO `san_pham` (`id`, `danh_muc_id`, `thuong_hieu_id`, `ten`, `slug`, `mo_ta_ngan`, `gia_ban`, `gia_goc`, `bao_hanh_thang`, `noi_bat`) VALUES
(1, 2,  1, 'Tai nghe Bluetooth chống ồn chủ động', 'tai-nghe-bluetooth-chong-on-chu-dong', 'Tai nghe true wireless, chống ồn chủ động, hộp sạc nhỏ gọn.', 1290000, 1590000, 12, 1),
(2, 3,  6, 'Loa Bluetooth di động chống nước',     'loa-bluetooth-di-dong-chong-nuoc',     'Loa nhỏ gọn, chống nước, phù hợp mang đi du lịch.',          990000,  NULL,    12, 1),
(3, 5,  2, 'Củ sạc nhanh GaN 65W 3 cổng',          'cu-sac-nhanh-gan-65w-3-cong',          'Sạc nhanh cho điện thoại và laptop, công nghệ GaN.',          690000,  850000,  18, 1),
(4, 6,  4, 'Cáp sạc USB-C to USB-C 100W',          'cap-sac-usb-c-to-usb-c-100w',          'Cáp bọc dù, hỗ trợ sạc nhanh tối đa 100W.',                   159000,  NULL,    6,  0),
(5, 7,  5, 'Sạc dự phòng sạc nhanh',               'sac-du-phong-sac-nhanh',               'Pin dự phòng nhiều dung lượng, hỗ trợ sạc nhanh.',            349000,  NULL,    12, 1),
(6, 9,  3, 'Chuột không dây công thái học',        'chuot-khong-day-cong-thai-hoc',        'Thiết kế ôm tay, kết nối Bluetooth hoặc đầu thu USB.',        1990000, NULL,    12, 0),
(7, 10, 3, 'Bàn phím cơ không dây',                'ban-phim-co-khong-day',                'Bàn phím cơ kết nối đa thiết bị, có đèn nền.',                1590000, NULL,    12, 0),
(8, 11, 4, 'Ốp lưng silicon cho điện thoại',       'op-lung-silicon-cho-dien-thoai',       'Ốp lưng mềm, chống trầy xước và chống sốc nhẹ.',              129000,  NULL,    0,  0);

INSERT INTO `thuoc_tinh_san_pham` (`san_pham_id`, `ten`, `gia_tri`, `thu_tu`) VALUES
(1, 'Kết nối',          'Bluetooth',                  1),
(1, 'Chống ồn',         'Chống ồn chủ động (ANC)',    2),
(2, 'Kết nối',          'Bluetooth',                  1),
(2, 'Chống nước',       'Có',                         2),
(3, 'Công suất tối đa', '65W',                        1),
(3, 'Cổng sạc',         '2 x USB-C, 1 x USB-A',       2),
(4, 'Công suất tối đa', '100W',                       1),
(4, 'Chất liệu',        'Bọc dù',                     2),
(5, 'Cổng sạc',         'USB-C, USB-A',               1),
(6, 'Kết nối',          'Bluetooth, đầu thu USB',     1),
(7, 'Kết nối',          'Bluetooth, đầu thu USB',     1),
(7, 'Đèn nền',          'Có',                         2),
(8, 'Chất liệu',        'Silicon',                    1);

INSERT INTO `tuy_chon` (`id`, `san_pham_id`, `ten`, `loai`, `thu_tu`) VALUES
(1, 1, 'Màu sắc',   'mau_sac',    1),
(2, 2, 'Màu sắc',   'mau_sac',    1),
(3, 3, 'Màu sắc',   'mau_sac',    1),
(4, 4, 'Độ dài',    'khac',       1),
(5, 5, 'Dung lượng','dung_luong', 1),
(6, 6, 'Màu sắc',   'mau_sac',    1),
(7, 7, 'Phiên bản', 'phien_ban',  1);

INSERT INTO `gia_tri_tuy_chon` (`id`, `tuy_chon_id`, `gia_tri`, `ma_mau`, `thu_tu`) VALUES
(1,  1, 'Đen',          '#000000', 1),
(2,  1, 'Trắng',        '#FFFFFF', 2),
(3,  2, 'Đen',          '#000000', 1),
(4,  2, 'Xanh dương',   '#1E40AF', 2),
(5,  3, 'Đen',          '#000000', 1),
(6,  3, 'Trắng',        '#FFFFFF', 2),
(7,  4, '1m',           NULL,      1),
(8,  4, '2m',           NULL,      2),
(9,  5, '10000mAh',     NULL,      1),
(10, 5, '20000mAh',     NULL,      2),
(11, 6, 'Đen',          '#000000', 1),
(12, 6, 'Xám',          '#6B7280', 2),
(13, 7, 'Red switch',   NULL,      1),
(14, 7, 'Brown switch', NULL,      2);

INSERT INTO `bien_the_san_pham` (`id`, `san_pham_id`, `sku`, `gia_ban`, `so_luong_ton`) VALUES
(1,  1, 'TG-TN01-DEN',    NULL,   30),
(2,  1, 'TG-TN01-TRANG',  NULL,   20),
(3,  2, 'TG-LOA01-DEN',   NULL,   15),
(4,  2, 'TG-LOA01-XANH',  NULL,   10),
(5,  3, 'TG-CS01-DEN',    NULL,   50),
(6,  3, 'TG-CS01-TRANG',  NULL,   40),
(7,  4, 'TG-CAP01-1M',    NULL,   100),
(8,  4, 'TG-CAP01-2M',    199000, 80),
(9,  5, 'TG-SDP01-10000', NULL,   60),
(10, 5, 'TG-SDP01-20000', 499000, 45),
(11, 6, 'TG-CH01-DEN',    NULL,   25),
(12, 6, 'TG-CH01-XAM',    NULL,   3),
(13, 7, 'TG-BP01-RED',    NULL,   12),
(14, 7, 'TG-BP01-BROWN',  NULL,   8),
(15, 8, 'TG-OL01',        NULL,   200);

INSERT INTO `bien_the_gia_tri` (`bien_the_id`, `gia_tri_tuy_chon_id`) VALUES
(1, 1), (2, 2), (3, 3), (4, 4), (5, 5), (6, 6), (7, 7),
(8, 8), (9, 9), (10, 10), (11, 11), (12, 12), (13, 13), (14, 14);
-- Biến thể 15 (ốp lưng) là biến thể mặc định, không gắn giá trị tùy chọn.

INSERT INTO `lich_su_ton_kho` (`bien_the_id`, `loai`, `so_luong_thay_doi`, `so_luong_sau`, `ghi_chu`, `nguoi_thuc_hien_id`)
SELECT `id`, 'khoi_tao', `so_luong_ton`, `so_luong_ton`, 'Khởi tạo dữ liệu mẫu', 1 FROM `bien_the_san_pham`;

INSERT INTO `ma_giam_gia` (`ma`, `mo_ta`, `loai_giam`, `gia_tri_giam`, `don_toi_thieu`, `giam_toi_da`, `tong_luot_dung`, `luot_dung_moi_nguoi`, `bat_dau_luc`, `ket_thuc_luc`) VALUES
('WELCOME10', 'Giảm 10% cho đơn từ 300.000đ, tối đa 100.000đ', 'phan_tram', 10,    300000, 100000, 1000, 1,    '2026-01-01 00:00:00', '2026-12-31 23:59:59'),
('GIAM50K',   'Giảm 50.000đ cho đơn từ 500.000đ',              'so_tien',   50000, 500000, NULL,   500,  NULL, '2026-01-01 00:00:00', '2026-12-31 23:59:59');

INSERT INTO `cai_dat` (`khoa`, `gia_tri`, `kieu`, `mo_ta`) VALUES
('ten_cua_hang',            'TechGear',           'chuoi', 'Tên cửa hàng'),
('hotline',                 '0900000000',         'chuoi', 'Số điện thoại hỗ trợ (mẫu)'),
('email_lien_he',           'hotro@techgear.vn',  'chuoi', 'Email liên hệ (mẫu)'),
('phi_van_chuyen',          '30000',              'so',    'Phí vận chuyển mặc định (VNĐ)'),
('mien_phi_van_chuyen_tu',  '500000',             'so',    'Miễn phí vận chuyển cho đơn từ (VNĐ)');
