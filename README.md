# TechGear – Website thương mại điện tử bán phụ kiện công nghệ

Nhóm sinh viên thực hiện:

- Trần Phú Dinh - 110123007 - DA23TTA
- Khưu Gia Bảo - 110123004 - DA23TTA
- Trần Hoàng Long - 110123025 - DA23TTA

| Thành phần | Công nghệ |
|---|---|
| Frontend | ReactJS 18 + Vite 5, React Router 6, Axios |
| Backend | Java 21, Spring Boot 4.1 (Web MVC, Data JPA, Security + OAuth2 Resource Server/JWT, Validation, Mail), Flyway, springdoc-openapi (Swagger UI) |
| CSDL | PostgreSQL 17 (chạy bằng Docker Compose), lược đồ quản lý bằng Flyway |

## Cấu trúc thư mục

```
TechGear/
├── database/
│   └── DB_TechGear.sql           Lược đồ + dữ liệu mẫu PostgreSQL 17 (xuất bằng pg_dump)
├── docs/
│   ├── so-do-use-case/           9 sơ đồ use case (draw.io)
│   ├── thiet-ke/                 Đặc tả, từ điển dữ liệu, đặc tả API, sơ đồ thiết kế
│   └── nguon-anh-mau.md          Nguồn, tác giả, giấy phép ảnh dữ liệu mẫu
├── postman/                      Bộ kiểm thử API (Postman/Newman)
└── source/
    ├── backend/                  Spring Boot REST API (cổng 8080)
    └── frontend/                 React (cổng 5173)
        └── src/
            ├── api/              axiosClient (tự gắn token), các hàm gọi API
            ├── components/       Header, Footer, ProductCard, ProtectedRoute...
            ├── context/          AuthContext (đăng nhập), CartContext (giỏ hàng localStorage)
            ├── pages/            Trang chủ, danh sách, chi tiết, giỏ hàng, đăng nhập, đăng ký...
            └── utils/            Định dạng tiền VNĐ, đường dẫn ảnh
```

## Sơ đồ thiết kế

Nằm trong `docs/thiet-ke/`, mỗi sơ đồ có `.drawio` (mở bằng [draw.io](https://www.drawio.com/) để sửa), `.drawio.png` và `.svg`.

| Tài liệu | Nội dung |
|---|---|
| [`dac-ta-yeu-cau.md`](docs/thiet-ke/dac-ta-yeu-cau.md) | Đặc tả yêu cầu: tác nhân, module, quy tắc nghiệp vụ, phi chức năng, kiểm thử |
| [`tu-dien-du-lieu.md`](docs/thiet-ke/tu-dien-du-lieu.md) | Từ điển dữ liệu 23 bảng PostgreSQL |
| [`dac-ta-api.md`](docs/thiet-ke/dac-ta-api.md) | Đặc tả API: quy ước, định dạng lỗi, danh sách API |
| [`bao-cao-cho-frontend.md`](docs/bao-cao-cho-frontend.md) | Báo cáo cho người làm frontend: API đã có, thay đổi so với bản cũ, việc frontend cần làm |

### 1. Kiến trúc tổng thể

[![Kiến trúc tổng thể](docs/thiet-ke/02_kien_truc_tong_the.drawio.png)](docs/thiet-ke/02_kien_truc_tong_the.drawio.png)

### 2. Sơ đồ thực thể – liên kết (ERD)

23 bảng, 31 khóa ngoại, gom theo 6 nhóm nghiệp vụ.

[![ERD](docs/thiet-ke/01_erd.drawio.png)](docs/thiet-ke/01_erd.drawio.png)

### 3. Sơ đồ gói backend

12 module (`com.techgear.<module>`) và quan hệ phụ thuộc.

[![Sơ đồ gói](docs/thiet-ke/03_so_do_goi.drawio.png)](docs/thiet-ke/03_so_do_goi.drawio.png)

### 4. Tài khoản: hoạt động và tuần tự

| Đăng ký | Đăng nhập | Quên mật khẩu |
|:---:|:---:|:---:|
| [![Đăng ký](docs/thiet-ke/04_hd_dang_ky.drawio.png)](docs/thiet-ke/04_hd_dang_ky.drawio.png) | [![Đăng nhập](docs/thiet-ke/05_hd_dang_nhap.drawio.png)](docs/thiet-ke/05_hd_dang_nhap.drawio.png) | [![Quên mật khẩu](docs/thiet-ke/06_hd_quen_mat_khau.drawio.png)](docs/thiet-ke/06_hd_quen_mat_khau.drawio.png) |

Tuần tự đăng nhập và kiểm tra JWT:

[![Tuần tự đăng nhập JWT](docs/thiet-ke/07_td_dang_nhap_jwt.drawio.png)](docs/thiet-ke/07_td_dang_nhap_jwt.drawio.png)

### 5. Sản phẩm: tìm kiếm và xem chi tiết

| Tìm kiếm, lọc sản phẩm | Xem chi tiết, chọn biến thể |
|:---:|:---:|
| [![Tìm kiếm sản phẩm](docs/thiet-ke/08_hd_tim_kiem_san_pham.drawio.png)](docs/thiet-ke/08_hd_tim_kiem_san_pham.drawio.png) | [![Xem chi tiết sản phẩm](docs/thiet-ke/09_hd_xem_chi_tiet_san_pham.drawio.png)](docs/thiet-ke/09_hd_xem_chi_tiet_san_pham.drawio.png) |

### 6. Giỏ hàng

| Thêm vào giỏ | Gộp giỏ khi đăng nhập |
|:---:|:---:|
| [![Thêm vào giỏ](docs/thiet-ke/10_hd_them_vao_gio.drawio.png)](docs/thiet-ke/10_hd_them_vao_gio.drawio.png) | [![Gộp giỏ hàng](docs/thiet-ke/11_hd_gop_gio_hang.drawio.png)](docs/thiet-ke/11_hd_gop_gio_hang.drawio.png) |

### 7. Đặt hàng và đơn hàng

| Đặt hàng | Hủy đơn |
|:---:|:---:|
| [![Đặt hàng](docs/thiet-ke/12_hd_dat_hang.drawio.png)](docs/thiet-ke/12_hd_dat_hang.drawio.png) | [![Hủy đơn](docs/thiet-ke/13_hd_huy_don.drawio.png)](docs/thiet-ke/13_hd_huy_don.drawio.png) |

Tuần tự đặt hàng (khóa biến thể và mã giảm giá trong một giao dịch):

[![Tuần tự đặt hàng](docs/thiet-ke/14_td_dat_hang.drawio.png)](docs/thiet-ke/14_td_dat_hang.drawio.png)

Trạng thái đơn hàng:

[![Trạng thái đơn hàng](docs/thiet-ke/15_tt_don_hang.drawio.png)](docs/thiet-ke/15_tt_don_hang.drawio.png)

### 8. Thanh toán VNPay

[![Tuần tự thanh toán VNPay](docs/thiet-ke/16_td_thanh_toan_vnpay.drawio.png)](docs/thiet-ke/16_td_thanh_toan_vnpay.drawio.png)

Trạng thái thanh toán của đơn:

[![Trạng thái thanh toán](docs/thiet-ke/17_tt_thanh_toan.drawio.png)](docs/thiet-ke/17_tt_thanh_toan.drawio.png)

### 9. Đánh giá sản phẩm

[![Viết đánh giá](docs/thiet-ke/18_hd_viet_danh_gia.drawio.png)](docs/thiet-ke/18_hd_viet_danh_gia.drawio.png)

### 10. Quản trị

| Xử lý đơn hàng | Thêm sản phẩm |
|:---:|:---:|
| [![Quản trị xử lý đơn hàng](docs/thiet-ke/19_hd_qt_xu_ly_don_hang.drawio.png)](docs/thiet-ke/19_hd_qt_xu_ly_don_hang.drawio.png) | [![Quản trị thêm sản phẩm](docs/thiet-ke/20_hd_qt_them_san_pham.drawio.png)](docs/thiet-ke/20_hd_qt_them_san_pham.drawio.png) |

## Sơ đồ use case

Nằm trong `docs/so-do-use-case/`, vẽ bằng draw.io (`.drawio`, `.drawio.png`, `.svg`). Tác nhân và quy ước quan hệ: xem [`docs/so-do-use-case/README.md`](docs/so-do-use-case/README.md).

### Use case tổng quát

[![Use case tổng quát](docs/so-do-use-case/01_use_case_tong_quat.drawio.png)](docs/so-do-use-case/01_use_case_tong_quat.drawio.png)

### Phía khách hàng

| Xem, tìm kiếm sản phẩm | Giỏ hàng, đặt hàng, thanh toán |
|:---:|:---:|
| [![Xem, tìm kiếm sản phẩm](docs/so-do-use-case/02_uc_xem_tim_kiem_san_pham.drawio.png)](docs/so-do-use-case/02_uc_xem_tim_kiem_san_pham.drawio.png) | [![Giỏ hàng, đặt hàng](docs/so-do-use-case/03_uc_gio_hang_dat_hang.drawio.png)](docs/so-do-use-case/03_uc_gio_hang_dat_hang.drawio.png) |
| **Tài khoản** | **Đơn hàng, đánh giá, yêu thích** |
| [![Tài khoản](docs/so-do-use-case/04_uc_tai_khoan.drawio.png)](docs/so-do-use-case/04_uc_tai_khoan.drawio.png) | [![Đơn hàng, đánh giá](docs/so-do-use-case/05_uc_don_hang_danh_gia.drawio.png)](docs/so-do-use-case/05_uc_don_hang_danh_gia.drawio.png) |

### Phía quản trị

| Danh mục, thương hiệu, sản phẩm | Tồn kho, đơn hàng |
|:---:|:---:|
| [![Quản trị sản phẩm](docs/so-do-use-case/06_uc_qt_san_pham.drawio.png)](docs/so-do-use-case/06_uc_qt_san_pham.drawio.png) | [![Quản trị kho, đơn hàng](docs/so-do-use-case/07_uc_qt_kho_don_hang.drawio.png)](docs/so-do-use-case/07_uc_qt_kho_don_hang.drawio.png) |
| **Mã giảm giá, banner, cấu hình** | **Người dùng, đánh giá, thống kê** |
| [![Quản trị khuyến mãi](docs/so-do-use-case/08_uc_qt_khuyen_mai_giao_dien.drawio.png)](docs/so-do-use-case/08_uc_qt_khuyen_mai_giao_dien.drawio.png) | [![Quản trị người dùng, thống kê](docs/so-do-use-case/09_uc_qt_nguoi_dung_thong_ke.drawio.png)](docs/so-do-use-case/09_uc_qt_nguoi_dung_thong_ke.drawio.png) |

## Cách chạy

### Yêu cầu

| Công cụ | Phiên bản | Ghi chú |
|---|---|---|
| JDK | 21 | VD: Eclipse Temurin 21 |
| Maven | 3.9+ | IntelliJ IDEA có sẵn Maven |
| Node.js | 18 trở lên | |
| Docker Desktop | mới nhất | Chạy PostgreSQL 17 bằng Docker Compose |

### 1. Cơ sở dữ liệu

```bash
docker compose up -d        # PostgreSQL 17 ở cổng 5433 (đổi bằng biến TECHGEAR_DB_PORT)
docker compose down -v      # xóa sạch dữ liệu khi cần làm lại từ đầu
```
Lược đồ CSDL do Flyway trong backend tự tạo khi khởi động (`source/backend/src/main/resources/db/migration`).

### 2. Backend

```bash
cd source/backend
mvn spring-boot:run         # tự bật profile dev (kết nối CSDL ở trên, khóa JWT dùng cho máy cá nhân)
```
- Swagger UI: `http://localhost:8080/swagger-ui.html` (bấm **Authorize**, dán token nhận được khi đăng nhập).
- Dữ liệu mẫu (chỉ profile dev, nạp khi CSDL chưa có sản phẩm/đơn hàng; muốn nạp lại: `docker compose down -v`): 35 sản phẩm
  có ảnh, 4 banner, 12 tài khoản, 43 đơn trong 90 ngày qua (đủ trạng thái, COD/VNPay), 27 đánh giá. Chi tiết:
  [`docs/bao-cao-cho-frontend.md`](docs/bao-cao-cho-frontend.md) mục 11. Ảnh là link Wikimedia Commons (cần internet),
  nguồn và giấy phép: [`docs/nguon-anh-mau.md`](docs/nguon-anh-mau.md).
- Tài khoản mẫu: `admin@techgear.vn` / `Admin@123` (quản trị), `khach@techgear.vn` / `Khach@123` (khách hàng),
  10 khách `*@example.com` / `Khach@123` (`ngo.long@example.com` bị khóa).
- Mã giảm giá mẫu: `GIAM10`, `GIAM50K`, `MOILAN1` (mỗi khách 1 lần), `HETHAN` (đã hết hạn).
- Không chạy backend mà chỉ cần CSDL (VD để chấm/nộp bài): nạp `database/DB_TechGear.sql` vào CSDL trống, cách nạp ghi ở đầu file.
- VNPay: dev dùng mã website và khóa giả (đủ để test IPN bằng Postman). Thanh toán thật trên sandbox: đặt `VNPAY_TMN_CODE`, `VNPAY_HASH_SECRET` do sandbox.vnpayment.vn cấp.
- Chưa cấu hình `MAIL_USERNAME` thì ở profile dev liên kết đặt lại mật khẩu được ghi ra log của backend thay vì gửi email (môi trường khác chỉ cảnh báo, không ghi liên kết).
- Biến môi trường khi triển khai: xem `source/backend/.env.example` (bắt buộc `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`).

Kiểm thử:

| Lệnh | Nội dung |
|---|---|
| `mvn test` | Unit test |
| `mvn verify` | Unit test + kiểm thử tích hợp trên PostgreSQL thật (Testcontainers, cần Docker) |
| `npx -y newman@6 run postman/TechGear.postman_collection.json -e postman/TechGear.local.postman_environment.json` | Kiểm thử API bằng bộ Postman (chạy ở gốc repo, backend đang chạy). Có thể import 2 file này vào Postman để gọi thử |

### 3. Frontend

```bash
cd source/frontend
npm install
cp .env.example .env
npm run dev
```
Mở `http://localhost:5173`. Địa chỉ backend cấu hình trong file `.env` (`VITE_API_URL`).

## Danh sách API

Đặc tả đầy đủ (dữ liệu gửi lên, trả về, mã lỗi): `docs/thiet-ke/dac-ta-api.md`. Backend mới giữ nguyên các API mà
frontend hiện đang gọi (`/api/auth/dang-ky`, `/api/auth/dang-nhap`, `/api/auth/toi`, `/api/danh-muc`, `/api/thuong-hieu`,
`/api/san-pham`, `/api/san-pham/noi-bat`, `/api/san-pham/{slug}`). Đã có: tài khoản (`/api/auth/**`, `/api/tai-khoan/**`), sản phẩm và cửa hàng (`/api/danh-muc`, `/api/thuong-hieu`,
`/api/san-pham/**`, `/api/banner`, `/api/cai-dat`), giỏ hàng và yêu thích (`/api/gio-hang/**`, `/api/yeu-thich/**`), đơn hàng (`/api/don-hang/**`), thanh toán VNPay (`/api/thanh-toan/vnpay/**`), đánh giá (`/api/danh-gia/**`, `/api/san-pham/{slug}/danh-gia`), quản trị (`/api/admin/**`).

## Tiến độ viết lại backend

| Giai đoạn | Nội dung | Trạng thái |
|---|---|---|
| 0. Dọn sạch | Xóa backend, script CSDL MySQL và tài liệu thiết kế cũ | Xong |
| 1. Thiết kế | Đặc tả yêu cầu, ERD PostgreSQL, kiến trúc, danh sách API | Xong |
| 2. Nền tảng | Spring Boot 4, Docker Compose, Flyway, bảo mật JWT, xử lý lỗi, Swagger, upload | Xong |
| 3. Tài khoản | Đăng ký, đăng nhập, hồ sơ, mật khẩu, sổ địa chỉ | Xong |
| 4. Sản phẩm | Danh mục, thương hiệu, tìm kiếm/lọc, chi tiết, banner, cài đặt | Xong |
| 5. Giỏ hàng | Giỏ hàng trên server, gộp giỏ, yêu thích | Xong |
| 6. Đặt hàng | Mã giảm giá, phí vận chuyển, tồn kho, đơn hàng, hủy, tra cứu | Xong |
| 7. Thanh toán | VNPay (sandbox) | Xong |
| 8. Đánh giá | Đánh giá sản phẩm đã mua, chờ duyệt | Xong |
| 9. Quản trị | Sản phẩm, kho, đơn hàng, khuyến mãi, người dùng, thống kê | Xong |
| 10. Dữ liệu mẫu | Dữ liệu mẫu đầy đủ + file `database/DB_TechGear.sql` | Xong |
| 11. Bàn giao | Kiểm thử toàn bộ, tài liệu, review | Xong |
