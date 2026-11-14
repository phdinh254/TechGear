# TechGear – Website thương mại điện tử bán phụ kiện công nghệ

Nhóm sinh viên thực hiện:

- Trần Phú Dinh - 110123007 - DA23TTA
- Khưu Gia Bảo - 110123004 - DA23TTA
- Trần Hoàng Long - 110123025 - DA23TTA

| Thành phần | Công nghệ |
|---|---|
| Frontend | ReactJS 18 + Vite 5, React Router 6, Axios |
| Backend | Java 21, Spring Boot 4.1 (Web MVC, Data JPA, Security + OAuth2 Resource Server/JWT, Validation, Mail), Flyway, springdoc-openapi (Swagger UI) – **đang viết lại** |
| CSDL | PostgreSQL 17 (chạy bằng Docker Compose) – **đang viết lại** |

## Cấu trúc thư mục

```
TechGear/
├── docs/
│   └── so-do-use-case/           9 sơ đồ use case (draw.io)
└── source/
    ├── backend/                  Spring Boot REST API (cổng 8080) – đang viết lại
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

### 1. Kiến trúc tổng thể

[![Kiến trúc tổng thể](docs/thiet-ke/02_kien_truc_tong_the.drawio.png)](docs/thiet-ke/02_kien_truc_tong_the.drawio.png)

### 2. Sơ đồ thực thể – liên kết (ERD)

23 bảng, 31 khóa ngoại, gom theo 6 nhóm nghiệp vụ.

[![ERD](docs/thiet-ke/01_erd.drawio.png)](docs/thiet-ke/01_erd.drawio.png)

### 3. Sơ đồ gói backend

12 module (`com.techgear.<module>`) và quan hệ phụ thuộc.

[![Sơ đồ gói](docs/thiet-ke/03_so_do_goi.drawio.png)](docs/thiet-ke/03_so_do_goi.drawio.png)

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

Đang viết lại. Backend mới giữ nguyên các API mà frontend hiện đang gọi (`/api/auth/dang-ky`, `/api/auth/dang-nhap`,
`/api/auth/toi`, `/api/danh-muc`, `/api/thuong-hieu`, `/api/san-pham`, `/api/san-pham/noi-bat`, `/api/san-pham/{slug}`).
Đặc tả đầy đủ sẽ nằm ở `docs/thiet-ke/dac-ta-api.md`.

## Tiến độ viết lại backend

| Giai đoạn | Nội dung | Trạng thái |
|---|---|---|
| 0. Dọn sạch | Xóa backend, script CSDL MySQL và tài liệu thiết kế cũ | Xong |
| 1. Thiết kế | Đặc tả yêu cầu, ERD PostgreSQL, kiến trúc, danh sách API | Xong |
| 2. Nền tảng | Spring Boot 4, Docker Compose, Flyway, bảo mật JWT, xử lý lỗi, Swagger, upload | Xong |
| 3. Tài khoản | Đăng ký, đăng nhập, hồ sơ, mật khẩu, sổ địa chỉ | Chưa |
| 4. Sản phẩm | Danh mục, thương hiệu, tìm kiếm/lọc, chi tiết, banner, cài đặt | Chưa |
| 5. Giỏ hàng | Giỏ hàng trên server, gộp giỏ, yêu thích | Chưa |
| 6. Đặt hàng | Mã giảm giá, phí vận chuyển, tồn kho, đơn hàng, hủy, tra cứu | Chưa |
| 7. Thanh toán | VNPay (sandbox) | Chưa |
| 8. Đánh giá | Đánh giá sản phẩm đã mua, chờ duyệt | Chưa |
| 9. Quản trị | Sản phẩm, kho, đơn hàng, khuyến mãi, người dùng, thống kê | Chưa |
| 10. Dữ liệu mẫu | Dữ liệu mẫu đầy đủ + file `database/DB_TechGear.sql` | Chưa |
| 11. Bàn giao | Kiểm thử toàn bộ, tài liệu, review | Chưa |
