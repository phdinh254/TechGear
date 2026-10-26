# TechGear – Website thương mại điện tử bán phụ kiện công nghệ

Nhóm sinh viên thực hiện:

- Trần Phú Dinh - 110123007 - DA23TTA
- Khưu Gia Bảo - 110123004 - DA23TTA
- Trần Hoàng Long - 110123025 - DA23TTA

| Thành phần | Công nghệ |
|---|---|
| Frontend | ReactJS 18 + Vite 5, React Router 6, Axios |
| Backend | Java 17, Spring Boot 3.3.5 (Web, Data JPA, Security, Validation, Mail), JWT (jjwt 0.12.6), Lombok, springdoc-openapi (Swagger UI) |
| CSDL | MySQL 8.0.16+ hoặc MariaDB 10.4+ (XAMPP) |

## Cấu trúc thư mục

```
TechGear/
├── database/
│   ├── DB_TechGear.sql          Script tạo CSDL techgear + dữ liệu mẫu
│   └── DB_TechGear_ban_cu.sql   Bản cũ (đề tài thời trang/Laravel), giữ lại để đối chiếu
├── docs/
│   ├── so-do-use-case/           9 sơ đồ use case (PlantUML)
│   ├── thiet-ke/                 Sơ đồ thiết kế (draw.io) + đặc tả API
│   └── TechGear.dbml             Lược đồ CSDL dạng dbdiagram.io
└── source/
    ├── backend/                  Spring Boot REST API (cổng 8080)
    │   └── src/main/java/com/techgear/
    │       ├── config/           SecurityConfig (JWT, CORS, phân quyền), WebConfig (ảnh upload), OpenApiConfig, AsyncConfig
    │       ├── controller/       REST controller
    │       ├── dto/              Dữ liệu vào/ra API (Java record)
    │       ├── entity/           24 entity JPA ánh xạ đúng các bảng (+ enums)
    │       ├── event/            Sự kiện nghiệp vụ (VD: yêu cầu đặt lại mật khẩu → gửi email)
    │       ├── exception/        ApiException, ApiError, GlobalExceptionHandler (lỗi trả JSON tiếng Việt)
    │       ├── repository/       Spring Data JPA repository
    │       ├── security/         JwtService, JwtAuthFilter, JsonAuthHandlers (401/403), UserDetailsService
    │       ├── service/          Xử lý nghiệp vụ (tài khoản, mật khẩu, sổ địa chỉ, email, lưu ảnh...)
    │       └── util/             SlugUtil (tạo slug từ tiếng Việt)
    └── frontend/                 React (cổng 5173)
        └── src/
            ├── api/              axiosClient (tự gắn token), các hàm gọi API
            ├── components/       Header, Footer, ProductCard, ProtectedRoute...
            ├── context/          AuthContext (đăng nhập), CartContext (giỏ hàng localStorage)
            ├── pages/            Trang chủ, danh sách, chi tiết, giỏ hàng, đăng nhập, đăng ký...
            └── utils/            Định dạng tiền VNĐ, đường dẫn ảnh
```

## Tài liệu thiết kế (`docs/thiet-ke/`)

Mỗi sơ đồ có 3 file: `.drawio` (mở bằng [draw.io](https://www.drawio.com/) để sửa), `.drawio.png` (chèn vào Word, vẫn mở lại được bằng draw.io) và `.svg` (ảnh vector).

| Sơ đồ | Nội dung |
|---|---|
| `01_erd` | Sơ đồ thực thể – liên kết: 24 bảng, 32 khóa ngoại, gom theo 6 nhóm nghiệp vụ |
| `02_kien_truc_tong_the` | Kiến trúc tổng thể: React ↔ các tầng Spring Boot ↔ MySQL, VNPay, SMTP |
| `03_so_do_lop_entity` | Sơ đồ lớp các entity JPA (thuộc tính, quan hệ, bội số, enum) |
| `04_hd_dang_ky` | Sơ đồ hoạt động: đăng ký |
| `05_hd_dang_nhap` | Sơ đồ hoạt động: đăng nhập |
| `06_hd_quen_mat_khau` | Sơ đồ hoạt động: quên / đặt lại mật khẩu |
| `07_tt_dang_nhap_jwt` | Sơ đồ tuần tự: đăng nhập nhận JWT và gọi API có token |

Đặc tả API đầy đủ (quy ước, mã lỗi, dữ liệu vào/ra, quy tắc nghiệp vụ, biến môi trường): [`docs/thiet-ke/dac-ta-api.md`](docs/thiet-ke/dac-ta-api.md).

## Cách chạy

### Yêu cầu

| Công cụ | Phiên bản | Ghi chú |
|---|---|---|
| JDK | 17 trở lên | VD: Eclipse Temurin 17 |
| Maven | 3.9+ | IntelliJ IDEA có sẵn Maven |
| Node.js | 18 trở lên | |
| XAMPP | MySQL/MariaDB 10.4+ | |

### 1. Cơ sở dữ liệu
1. Mở XAMPP, bật **MySQL**.
2. Vào phpMyAdmin → tab **Import** → chọn `database/DB_TechGear.sql` → **Go**.
   (Script tự xóa và tạo lại CSDL `techgear`.)

Tài khoản mẫu:

| Email | Mật khẩu | Vai trò |
|---|---|---|
| admin@techgear.vn | Admin@123 | Quản trị |
| khachhang@techgear.vn | Khach@123 | Khách hàng |

> Dữ liệu sản phẩm mẫu (tên, giá, thông số) chỉ để minh họa, không phải số liệu chính thức của hãng.

### 2. Backend
1. IntelliJ IDEA → **Open** → chọn thư mục `source/backend` (nhận diện `pom.xml`).
2. Đợi Maven tải thư viện. Nếu IntelliJ hỏi bật *Annotation Processing* cho Lombok → chọn **Enable**.
3. Chạy `TechGearApplication`. Hoặc dòng lệnh: `mvn spring-boot:run`.

Kiểm tra nhanh:
- `http://localhost:8080/api/danh-muc` → JSON danh mục.
- `http://localhost:8080/swagger-ui.html` → tài liệu API, thử gọi API trực tiếp (bấm **Authorize**, dán token nhận được khi đăng nhập).

Chạy kiểm thử: `mvn test`.

#### Cấu hình (biến môi trường)

Không cần khai báo gì khi chạy trên máy cá nhân với XAMPP mặc định. Khi cần thay đổi, đặt biến môi trường (đầy đủ trong [đặc tả API](docs/thiet-ke/dac-ta-api.md#3-biến-môi-trường)):

| Biến | Mặc định | Ý nghĩa |
|---|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | `localhost:3306/techgear`, `root`, rỗng | Kết nối CSDL |
| `JWT_SECRET` | khóa dev có sẵn | Khóa ký JWT – **bắt buộc đổi khi triển khai thật** |
| `FRONTEND_URL` | `http://localhost:5173` | Địa chỉ frontend (CORS, liên kết trong email) |
| `MAIL_USERNAME`, `MAIL_PASSWORD` | rỗng | Gmail + [App Password](https://myaccount.google.com/apppasswords) để gửi email thật |

> Chưa cấu hình `MAIL_USERNAME` thì hệ thống **không gửi email**, mà ghi liên kết đặt lại mật khẩu ra log của backend (tiện chạy thử).

### 3. Frontend

```bash
cd source/frontend
npm install
cp .env.example .env
npm run dev
```
Mở `http://localhost:5173`. Địa chỉ backend cấu hình trong file `.env` (`VITE_API_URL`).

## Danh sách API hiện có

Gốc: `http://localhost:8080/api`. Gửi token: header `Authorization: Bearer <token>`.
Lỗi trả về dạng `{ "thoiGian": "...", "trangThai": 400, "thongBao": "...", "loi": { ... } }`.

| Phương thức | Đường dẫn | Quyền | Mô tả |
|---|---|---|---|
| POST | `/auth/dang-ky` | Công khai | Đăng ký, trả về token |
| POST | `/auth/dang-nhap` | Công khai | Đăng nhập, trả về token |
| GET | `/auth/toi` | Đã đăng nhập | Thông tin tài khoản hiện tại |
| POST | `/auth/quen-mat-khau` | Công khai | Gửi liên kết đặt lại mật khẩu qua email (hiệu lực 15 phút) |
| POST | `/auth/dat-lai-mat-khau` | Công khai | Đặt mật khẩu mới bằng mã trong liên kết |
| GET / PUT | `/tai-khoan` | Đã đăng nhập | Xem / cập nhật hồ sơ (họ tên, SĐT, giới tính, ngày sinh) |
| PUT | `/tai-khoan/mat-khau` | Đã đăng nhập | Đổi mật khẩu |
| POST | `/tai-khoan/anh-dai-dien` | Đã đăng nhập | Đổi ảnh đại diện (multipart) |
| GET / POST | `/tai-khoan/dia-chi` | Đã đăng nhập | Sổ địa chỉ: danh sách / thêm |
| PUT / DELETE | `/tai-khoan/dia-chi/{id}` | Đã đăng nhập | Sửa / xóa địa chỉ |
| PATCH | `/tai-khoan/dia-chi/{id}/mac-dinh` | Đã đăng nhập | Đặt địa chỉ mặc định |
| GET | `/danh-muc` | Công khai | Cây danh mục (cha – con) |
| GET | `/thuong-hieu` | Công khai | Thương hiệu đang hiển thị |
| GET | `/san-pham` | Công khai | Tìm kiếm, lọc, sắp xếp, phân trang |
| GET | `/san-pham/noi-bat` | Công khai | 8 sản phẩm nổi bật |
| GET | `/san-pham/{slug}` | Công khai | Chi tiết sản phẩm (tùy chọn, biến thể, thông số) |
| GET/POST/PUT/DELETE | `/admin/thuong-hieu` | Quản trị | Quản lý thương hiệu |
| POST | `/admin/upload?thuMuc=` | Quản trị | Tải ảnh (JPG/PNG/WEBP/GIF, tối đa 5MB) |

Tham số `/san-pham`: `tuKhoa`, `danhMuc` (slug, gồm cả danh mục con), `thuongHieu` (slug), `giaTu`, `giaDen`, `sapXep` (`moi_nhat` | `gia_tang` | `gia_giam` | `pho_bien`), `trang` (bắt đầu từ 0), `kichThuoc` (tối đa 48).

## Thay đổi so với file CSDL ban đầu

- Đổi tên đề tài, công nghệ, tên CSDL `shop_thoi_trang` → `techgear`.
- Bỏ các cột riêng của Laravel (`remember_token`, `email_verified_at`); thêm bảng `ma_dat_lai_mat_khau` cho chức năng quên mật khẩu.
- `tuy_chon.loai`: `mau_sac, kich_co, khac` → `mau_sac, dung_luong, phien_ban, khac`.
- Thêm `san_pham.bao_hanh_thang` (thời gian bảo hành).
- `gio_hang`: chỉ lưu giỏ của khách đã đăng nhập (bỏ `ma_phien`, `het_han_luc`); khách vãng lai lưu giỏ ở localStorage của React.
- `don_hang`: thêm ràng buộc `tien_giam_gia <= tam_tinh`.
- Cột thời gian dùng `DATETIME` với `created_at` mặc định `CURRENT_TIMESTAMP`.
- Thêm dữ liệu mẫu: 2 tài khoản, 11 danh mục, 6 thương hiệu, 8 sản phẩm, 15 biến thể, 2 mã giảm giá, cài đặt cửa hàng.

## Tiến độ hoàn thiện backend

| Giai đoạn | Nội dung | Trạng thái |
|---|---|---|
| 1. Nền tảng | ERD, kiến trúc, sơ đồ lớp; chuẩn hóa lỗi; cấu hình biến môi trường; Swagger; upload ảnh | Xong |
| 2. Tài khoản | Hồ sơ, đổi mật khẩu, ảnh đại diện, quên / đặt lại mật khẩu, sổ địa chỉ | Xong |
| 3. Sản phẩm | Tìm kiếm theo giá biến thể, banner, cài đặt cửa hàng, đánh giá theo sản phẩm | Đang làm |
| 4. Giỏ hàng | Giỏ hàng trên server, gộp giỏ localStorage khi đăng nhập, yêu thích | Chưa |
| 5. Đặt hàng | Mã giảm giá, phí vận chuyển, tồn kho, đơn của tôi, hủy đơn, tra cứu đơn | Chưa |
| 6. Thanh toán | VNPay (sandbox) | Chưa |
| 7. Đánh giá | Đánh giá sản phẩm đã mua, chờ duyệt | Chưa |
| 8. Quản trị | Sản phẩm + biến thể, kho, đơn hàng, mã giảm giá, banner, người dùng, thống kê | Chưa |
| 9. Bàn giao | Kiểm thử tích hợp, hoàn thiện tài liệu | Chưa |
