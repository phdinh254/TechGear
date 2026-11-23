# Từ điển dữ liệu – CSDL TechGear (MySQL 8.4)

Phiên bản: 1.0 (Phase 1 – chờ duyệt) · 23 bảng · CSDL `techgear` · Quản lý bằng Flyway.
22/11/2026: chuyển từ PostgreSQL 17 sang MySQL 8.4 – cấu trúc bảng, cột, ràng buộc giữ nguyên, chỉ đổi kiểu dữ liệu theo MySQL (mục cuối).

## Quy ước chung
- Tên bảng/cột: tiếng Việt không dấu, `snake_case`, số ít.
- Khóa chính: `id BIGINT AUTO_INCREMENT` (trừ bảng nối dùng khóa kép). Khóa ngoại khai báo mức bảng (`FOREIGN KEY`), InnoDB tự tạo chỉ mục.
- Thời gian: `DATETIME(6)` lưu giờ Việt Nam (UTC+7, kết nối đặt `time_zone = '+07:00'`); `created_at` mặc định `CURRENT_TIMESTAMP(6)`; `updated_at` do ứng dụng ghi.
- Tiền: `BIGINT` (đồng, không lẻ) + `CHECK (>= 0)`. Số lượng: `INTEGER` + `CHECK`.
- Trạng thái/loại: `VARCHAR` + `CHECK (... IN (...))` (giá trị viết thường, khớp enum Java).
- Xóa mềm (`deleted_at`): chỉ ở bảng mà đơn hàng/lịch sử còn tham chiếu (danh mục, thương hiệu, sản phẩm, biến thể, mã giảm giá).
  Ràng buộc duy nhất của các bảng này là **chỉ mục duy nhất trên biểu thức** `(IF(deleted_at IS NULL, cot, NULL))`: dòng đã xóa cho NULL
  nên không đụng nhau (xóa xong tạo lại cùng tên được). Ghi "UQ (chưa xóa)" ở các bảng dưới.
- Bảng mã `utf8mb4`, collation `utf8mb4_0900_ai_ci`: so sánh không phân biệt hoa thường và dấu (`đ` = `d`) – dùng cho tìm kiếm
  không dấu, email và mã giảm giá không phân biệt hoa thường. Riêng `ma_dat_lai_mat_khau.ma_bam` dùng `ascii_bin` (so khớp chính xác).
- `BOOLEAN` của MySQL là `TINYINT(1)`; `INTEGER` là `INT`.
- Viết tắt: PK khóa chính, FK khóa ngoại, UQ duy nhất, CK kiểm tra, NN not null.

## Nhóm 1 – Tài khoản

### `nguoi_dung` – người dùng (khách hàng, quản trị viên)
| Cột | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | |
| ho_ten | VARCHAR(100) | NN | Họ và tên |
| email | VARCHAR(150) | NN, UQ (collation không phân biệt hoa thường) | Email đăng nhập, lưu chữ thường |
| mat_khau | VARCHAR(100) | NN | Băm BCrypt |
| so_dien_thoai | VARCHAR(20) | | |
| gioi_tinh | VARCHAR(10) | CK `nam`,`nu`,`khac` | |
| ngay_sinh | DATE | | |
| anh_dai_dien | VARCHAR(255) | | Đường dẫn ảnh |
| vai_tro | VARCHAR(20) | NN, CK `quan_tri`,`khach_hang`, mặc định `khach_hang` | |
| trang_thai | VARCHAR(20) | NN, CK `hoat_dong`,`bi_khoa`, mặc định `hoat_dong` | |
| doi_mat_khau_luc | DATETIME(6) | | Lần đổi/đặt lại mật khẩu gần nhất – token phát hành trước mốc này bị từ chối |
| created_at, updated_at | DATETIME(6) | | |

### `ma_dat_lai_mat_khau` – mã đặt lại mật khẩu
| Cột | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | |
| nguoi_dung_id | BIGINT | NN, FK → nguoi_dung (CASCADE) | |
| ma_bam | CHAR(64) | NN, UQ | SHA-256 của mã gửi qua email (không lưu mã gốc) |
| het_han_luc | DATETIME(6) | NN | created_at + 15 phút |
| da_su_dung | BOOLEAN | NN, mặc định FALSE | |
| created_at | DATETIME(6) | NN | |

### `dia_chi` – sổ địa chỉ
| Cột | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | |
| nguoi_dung_id | BIGINT | NN, FK → nguoi_dung (CASCADE) | |
| ten_nguoi_nhan | VARCHAR(100) | NN | |
| so_dien_thoai | VARCHAR(20) | NN | |
| tinh_thanh | VARCHAR(100) | NN | Tỉnh/thành phố |
| phuong_xa | VARCHAR(100) | NN | Phường/xã/đặc khu |
| dia_chi_chi_tiet | VARCHAR(255) | NN | Số nhà, đường |
| la_mac_dinh | BOOLEAN | NN, mặc định FALSE; UQ trên `IF(la_mac_dinh, nguoi_dung_id, NULL)` | Tối đa một địa chỉ mặc định / người |
| created_at, updated_at | DATETIME(6) | | |

## Nhóm 2 – Danh mục, thương hiệu, sản phẩm

### `danh_muc` – danh mục (2 cấp)
| Cột | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | |
| danh_muc_cha_id | BIGINT | FK → danh_muc | NULL = cấp gốc |
| ten | VARCHAR(100) | NN | |
| slug | VARCHAR(120) | NN, UQ (chưa xóa) | |
| mo_ta | TEXT | | |
| hinh_anh | VARCHAR(255) | | |
| thu_tu | INTEGER | NN, mặc định 0 | |
| hien_thi | BOOLEAN | NN, mặc định TRUE | |
| created_at, updated_at, deleted_at | DATETIME(6) | | |

### `thuong_hieu` – thương hiệu
| Cột | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | |
| ten | VARCHAR(100) | NN | |
| slug | VARCHAR(120) | NN, UQ (chưa xóa) | |
| logo | VARCHAR(255) | | |
| mo_ta | TEXT | | |
| hien_thi | BOOLEAN | NN, mặc định TRUE | |
| created_at, updated_at, deleted_at | DATETIME(6) | | |

### `san_pham` – thông tin chung của sản phẩm
| Cột | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | |
| danh_muc_id | BIGINT | NN, FK → danh_muc | |
| thuong_hieu_id | BIGINT | FK → thuong_hieu | |
| ten | VARCHAR(255) | NN | |
| slug | VARCHAR(255) | NN, UQ (chưa xóa) | |
| mo_ta_ngan | VARCHAR(500) | | |
| mo_ta | TEXT | | Mô tả chi tiết (HTML) |
| gia_ban | BIGINT | NN, CK ≥ 0, mặc định 0 | Giá hiển thị = giá biến thể đang bán rẻ nhất (ứng dụng tự cập nhật) |
| gia_goc | BIGINT | CK ≥ 0 | Giá gốc của biến thể đó (gạch ngang) |
| bao_hanh_thang | INTEGER | NN, CK ≥ 0, mặc định 0 | |
| noi_bat | BOOLEAN | NN, mặc định FALSE | |
| hien_thi | BOOLEAN | NN, mặc định TRUE | Đang bán / ngừng bán |
| luot_xem | INTEGER | NN, CK ≥ 0, mặc định 0 | |
| created_at, updated_at, deleted_at | DATETIME(6) | | |

Chỉ mục: `danh_muc_id`, `thuong_hieu_id`, `gia_ban`.

### `thong_so` – thông số kỹ thuật
| Cột | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | |
| san_pham_id | BIGINT | NN, FK → san_pham (CASCADE) | |
| ten | VARCHAR(100) | NN | VD: Công suất |
| gia_tri | VARCHAR(255) | NN | VD: 65W |
| thu_tu | INTEGER | NN, mặc định 0 | |

### `tuy_chon` – tùy chọn của sản phẩm
| Cột | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | |
| san_pham_id | BIGINT | NN, FK → san_pham (CASCADE) | |
| ten | VARCHAR(50) | NN; UQ `(san_pham_id, ten)` | VD: Màu sắc |
| loai | VARCHAR(20) | NN, CK `mau_sac`,`dung_luong`,`phien_ban`,`khac` | |
| thu_tu | INTEGER | NN, mặc định 0 | |

### `gia_tri_tuy_chon` – giá trị của tùy chọn
| Cột | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | |
| tuy_chon_id | BIGINT | NN, FK → tuy_chon (CASCADE) | |
| gia_tri | VARCHAR(50) | NN; UQ `(tuy_chon_id, gia_tri)` | VD: Đen |
| ma_mau | CHAR(7) | CK dạng `#RRGGBB` | Chỉ với màu sắc |
| thu_tu | INTEGER | NN, mặc định 0 | |

### `bien_the` – biến thể (SKU) – đơn vị bán và tồn kho
| Cột | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | |
| san_pham_id | BIGINT | NN, FK → san_pham | |
| sku | VARCHAR(100) | NN, UQ (chưa xóa) | |
| gia_ban | BIGINT | NN, CK ≥ 0 | |
| gia_goc | BIGINT | CK `gia_goc IS NULL OR gia_goc >= gia_ban` | |
| so_luong_ton | INTEGER | NN, CK ≥ 0, mặc định 0 | CSDL chặn tồn kho âm |
| nguong_canh_bao | INTEGER | NN, CK ≥ 0, mặc định 5 | Sắp hết khi tồn ≤ ngưỡng |
| hien_thi | BOOLEAN | NN, mặc định TRUE | |
| created_at, updated_at, deleted_at | DATETIME(6) | | |

### `bien_the_gia_tri` – giá trị tùy chọn tạo nên biến thể (N–N)
| Cột | Kiểu | Ràng buộc |
|---|---|---|
| bien_the_id | BIGINT | PK, FK → bien_the (CASCADE) |
| gia_tri_tuy_chon_id | BIGINT | PK, FK → gia_tri_tuy_chon (CASCADE) |

### `hinh_anh_san_pham` – ảnh sản phẩm
| Cột | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | |
| san_pham_id | BIGINT | NN, FK → san_pham (CASCADE) | |
| gia_tri_tuy_chon_id | BIGINT | FK → gia_tri_tuy_chon (SET NULL) | Ảnh theo màu; NULL = ảnh chung |
| duong_dan | VARCHAR(255) | NN | |
| mo_ta | VARCHAR(255) | | Văn bản thay thế |
| thu_tu | INTEGER | NN, mặc định 0 | |
| la_anh_chinh | BOOLEAN | NN, mặc định FALSE; UQ trên `IF(la_anh_chinh, san_pham_id, NULL)` | Tối đa một ảnh chính |

## Nhóm 3 – Cửa hàng, khuyến mãi

### `banner`
| Cột | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | |
| tieu_de | VARCHAR(255) | | |
| hinh_anh | VARCHAR(255) | NN | |
| lien_ket | VARCHAR(255) | | |
| thu_tu | INTEGER | NN, mặc định 0 | |
| hien_thi | BOOLEAN | NN, mặc định TRUE | |
| bat_dau_luc, ket_thuc_luc | DATETIME(6) | CK `ket_thuc_luc > bat_dau_luc` khi cả hai có | NULL = không giới hạn |
| created_at, updated_at | DATETIME(6) | | |

### `cai_dat` – cấu hình cửa hàng (khóa – giá trị, công khai)
| Cột | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| khoa | VARCHAR(100) | PK | `ten_cua_hang`, `hotline`, `email_lien_he`, `phi_van_chuyen`, `mien_phi_van_chuyen_tu` |
| gia_tri | TEXT | NN | |
| mo_ta | VARCHAR(255) | | |
| updated_at | DATETIME(6) | | |

### `ma_giam_gia`
| Cột | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | |
| ma | VARCHAR(50) | NN, UQ (chưa xóa) | Khách nhập không phân biệt hoa thường (collation) |
| mo_ta | VARCHAR(255) | | |
| loai_giam | VARCHAR(20) | NN, CK `phan_tram`,`so_tien` | |
| gia_tri_giam | BIGINT | NN, CK > 0; CK `loai_giam <> 'phan_tram' OR gia_tri_giam <= 100` | |
| don_toi_thieu | BIGINT | NN, CK ≥ 0, mặc định 0 | |
| giam_toi_da | BIGINT | CK > 0 | Trần tiền giảm khi giảm % |
| tong_luot_dung | INTEGER | CK > 0 | NULL = không giới hạn |
| luot_dung_moi_nguoi | INTEGER | CK > 0 | NULL = không giới hạn |
| da_dung | INTEGER | NN, CK ≥ 0, mặc định 0; CK `tong_luot_dung IS NULL OR da_dung <= tong_luot_dung` | |
| bat_dau_luc, ket_thuc_luc | DATETIME(6) | NN; CK `ket_thuc_luc > bat_dau_luc` | |
| kich_hoat | BOOLEAN | NN, mặc định TRUE | |
| created_at, updated_at, deleted_at | DATETIME(6) | | |

## Nhóm 4 – Giỏ hàng, yêu thích

### `gio_hang` – dòng giỏ hàng của khách hàng
| Cột | Kiểu | Ràng buộc |
|---|---|---|
| nguoi_dung_id | BIGINT | PK, FK → nguoi_dung (CASCADE) |
| bien_the_id | BIGINT | PK, FK → bien_the (CASCADE) |
| so_luong | INTEGER | NN, CK > 0 |
| created_at, updated_at | DATETIME(6) | |

### `yeu_thich`
| Cột | Kiểu | Ràng buộc |
|---|---|---|
| nguoi_dung_id | BIGINT | PK, FK → nguoi_dung (CASCADE) |
| san_pham_id | BIGINT | PK, FK → san_pham (CASCADE) |
| created_at | DATETIME(6) | NN |

## Nhóm 5 – Đơn hàng, thanh toán, kho

### `don_hang`
| Cột | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | |
| ma_don_hang | VARCHAR(20) | NN, UQ | `DHyyMMdd` + 6 ký tự ngẫu nhiên |
| nguoi_dung_id | BIGINT | FK → nguoi_dung (SET NULL) | NULL = khách vãng lai |
| ma_giam_gia_id | BIGINT | FK → ma_giam_gia | |
| ten_nguoi_nhan | VARCHAR(100) | NN | |
| email | VARCHAR(150) | NN | Chữ thường; dùng tra cứu đơn |
| so_dien_thoai | VARCHAR(20) | NN | |
| dia_chi_giao_hang | VARCHAR(500) | NN | Ảnh chụp địa chỉ lúc đặt |
| ghi_chu | VARCHAR(500) | | Của khách |
| ghi_chu_quan_tri | VARCHAR(500) | | Nội bộ |
| tam_tinh | BIGINT | NN, CK ≥ 0 | |
| tien_giam_gia | BIGINT | NN, CK ≥ 0, mặc định 0; CK `tien_giam_gia <= tam_tinh` | |
| phi_van_chuyen | BIGINT | NN, CK ≥ 0, mặc định 0 | |
| tong_tien | BIGINT | NN; CK `tong_tien = tam_tinh - tien_giam_gia + phi_van_chuyen` | |
| phuong_thuc_thanh_toan | VARCHAR(10) | NN, CK `cod`,`payos` (ràng buộc `ck_don_hang_phuong_thuc`, V8) | |
| trang_thai_thanh_toan | VARCHAR(20) | NN, CK `chua_thanh_toan`,`da_thanh_toan`,`da_hoan_tien`, mặc định `chua_thanh_toan` | |
| trang_thai | VARCHAR(20) | NN, CK `cho_xac_nhan`,`da_xac_nhan`,`dang_giao`,`hoan_thanh`,`da_huy`, mặc định `cho_xac_nhan` | |
| created_at, updated_at | DATETIME(6) | | created_at = ngày đặt |

Chỉ mục: `nguoi_dung_id`, `(trang_thai, created_at)`, `email`.

### `chi_tiet_don_hang` – dòng đơn hàng (ảnh chụp lúc mua)
| Cột | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | |
| don_hang_id | BIGINT | NN, FK → don_hang (CASCADE) | |
| san_pham_id | BIGINT | FK → san_pham (SET NULL) | |
| bien_the_id | BIGINT | FK → bien_the (SET NULL) | |
| ten_san_pham | VARCHAR(255) | NN | |
| sku | VARCHAR(100) | NN | |
| phan_loai | VARCHAR(150) | | VD: Đen / 20000mAh |
| hinh_anh | VARCHAR(255) | | |
| don_gia | BIGINT | NN, CK ≥ 0 | |
| so_luong | INTEGER | NN, CK > 0 | |
| thanh_tien | BIGINT | NN; CK `thanh_tien = don_gia * so_luong` | |

### `lich_su_don_hang` – lịch sử trạng thái
| Cột | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | |
| don_hang_id | BIGINT | NN, FK → don_hang (CASCADE) | |
| trang_thai | VARCHAR(20) | NN, CK như `don_hang.trang_thai` | Trạng thái mới |
| ghi_chu | VARCHAR(500) | | Lý do |
| nguoi_thuc_hien_id | BIGINT | FK → nguoi_dung (SET NULL) | NULL = khách vãng lai/hệ thống |
| created_at | DATETIME(6) | NN | |

### `thanh_toan` – giao dịch thanh toán
| Cột | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | |
| don_hang_id | BIGINT | NN, FK → don_hang (CASCADE) | Một đơn có thể nhiều lần thử |
| phuong_thuc | VARCHAR(10) | NN, CK `cod`,`payos` (ràng buộc `ck_thanh_toan_phuong_thuc`, V8) | |
| so_tien | BIGINT | NN, CK ≥ 0 | |
| ma_tham_chieu | VARCHAR(50) | NN, UQ | `orderCode` gửi PayOS = id đơn × 100 + lần thử (chuỗi số) |
| ma_giao_dich | VARCHAR(50) | UQ | Mã giao dịch ngân hàng PayOS trả về (`data.reference` của webhook) |
| trang_thai | VARCHAR(20) | NN, CK `cho_xu_ly`,`thanh_cong`,`that_bai`, mặc định `cho_xu_ly` | |
| du_lieu_phan_hoi | JSON | | Toàn bộ body webhook PayOS, hoặc thông tin link khi trang kết quả hỏi lại PayOS |
| thanh_toan_luc | DATETIME(6) | | |
| created_at, updated_at | DATETIME(6) | | |

### `lich_su_ton_kho` – nhật ký biến động kho
| Cột | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | |
| bien_the_id | BIGINT | NN, FK → bien_the | |
| don_hang_id | BIGINT | FK → don_hang (SET NULL) | Khi do bán/hủy đơn |
| loai | VARCHAR(20) | NN, CK `khoi_tao`,`nhap_kho`,`ban_hang`,`huy_don`,`dieu_chinh` | |
| so_luong_thay_doi | INTEGER | NN, CK ≠ 0 | Dương tăng, âm giảm |
| so_luong_sau | INTEGER | NN, CK ≥ 0 | |
| ghi_chu | VARCHAR(255) | | Bắt buộc với `dieu_chinh` (ứng dụng kiểm) |
| nguoi_thuc_hien_id | BIGINT | FK → nguoi_dung (SET NULL) | |
| created_at | DATETIME(6) | NN | |

## Nhóm 6 – Đánh giá

### `danh_gia`
| Cột | Kiểu | Ràng buộc | Mô tả |
|---|---|---|---|
| id | BIGINT | PK | |
| san_pham_id | BIGINT | NN, FK → san_pham (CASCADE) | |
| nguoi_dung_id | BIGINT | NN, FK → nguoi_dung (CASCADE) | |
| don_hang_id | BIGINT | NN, FK → don_hang (CASCADE) | Đơn đã hoàn thành chứa sản phẩm |
| so_sao | SMALLINT | NN, CK 1–5 | |
| noi_dung | VARCHAR(2000) | | |
| trang_thai | VARCHAR(20) | NN, CK `cho_duyet`,`da_duyet`,`da_an`, mặc định `cho_duyet` | |
| created_at, updated_at | DATETIME(6) | | |

UQ `(nguoi_dung_id, san_pham_id, don_hang_id)`; chỉ mục `(san_pham_id, trang_thai)`.

## Thay đổi so với lược đồ MySQL cũ
| Thay đổi | Lý do |
|---|---|
| Giá chỉ nằm ở `bien_the` (bắt buộc); `san_pham.gia_ban/gia_goc` là giá hiển thị tự cập nhật | Bỏ logic "giá biến thể NULL thì lấy giá sản phẩm" (dễ sai khi lọc/sắp xếp) |
| `gio_hang` + `chi_tiet_gio_hang` gộp thành một bảng `gio_hang` khóa kép | Mỗi khách đúng một giỏ, không cần bảng cha |
| `cai_dat`: khóa là PK, bỏ cột `kieu`, `id` | Tập khóa cố định, ứng dụng biết kiểu |
| `danh_gia`: `da_duyet` + `deleted_at` → `trang_thai` (`cho_duyet`/`da_duyet`/`da_an`) | Khớp use case Duyệt / Ẩn đánh giá |
| `ma_dat_lai_mat_khau.ma` → `ma_bam` (SHA-256) | Lộ CSDL không lộ mã đặt lại |
| Thêm `nguoi_dung.doi_mat_khau_luc` | Thu hồi token khi đổi/đặt lại mật khẩu |
| Thêm `thanh_toan.ma_tham_chieu` | Mã gửi PayOS (`orderCode`), tách khỏi mã giao dịch PayOS trả về |
| Bỏ `deleted_at` ở `nguoi_dung`, `danh_gia` | Không có use case xóa tài khoản; đánh giá dùng trạng thái |
| Duy nhất giữa các dòng chưa xóa mềm, một địa chỉ mặc định, một ảnh chính – ràng buộc ở CSDL | Chỉ mục duy nhất trên biểu thức (MySQL 8) |
| Tiền `BIGINT`, thời gian `DATETIME(6)` (giờ Việt Nam), `thuoc_tinh_san_pham` → `thong_so`, `bien_the_san_pham` → `bien_the`, `password` → `mat_khau` | Tiền không lẻ, tên ngắn gọn thống nhất |

## Chuyển từ PostgreSQL 17 sang MySQL 8.4 (22/11/2026)
| PostgreSQL | MySQL | Ghi chú |
|---|---|---|
| `GENERATED BY DEFAULT AS IDENTITY` | `AUTO_INCREMENT` | Lấy id vừa thêm qua `KeyHolder` (không có `RETURNING`) |
| `TIMESTAMPTZ` | `DATETIME(6)` giờ Việt Nam | Kết nối cố định `time_zone = '+07:00'`, Hibernate lưu `OffsetDateTime` theo giờ VN |
| `JSONB` | `JSON` | |
| Chỉ mục duy nhất một phần `WHERE ...` | Chỉ mục duy nhất trên biểu thức `IF(..., cot, NULL)` | Cùng ý nghĩa |
| `unaccent`, `lower()`/`upper()` | Collation `utf8mb4_0900_ai_ci` | Không phân biệt hoa thường và dấu |
| CHECK regex `~` | `REGEXP_LIKE` | |
| Mức cô lập mặc định READ COMMITTED | Kết nối đặt READ COMMITTED (InnoDB mặc định REPEATABLE READ) | Giữ đúng hành vi khóa dòng khi đặt hàng đồng thời |
