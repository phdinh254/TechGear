# TechGear – Website thương mại điện tử bán phụ kiện công nghệ

Nhóm sinh viên thực hiện:

- Trần Phú Dinh - 110123007 - DA23TTA
- Khưu Gia Bảo - 110123004 - DA23TTA
- Trần Hoàng Long - 110123025 - DA23TTA

| Thành phần | Công nghệ |
|---|---|
| Frontend | ReactJS 18 + Vite 5, React Router 6, Axios |
| Backend | Java 17, Spring Boot 3.3.5 (Web, Data JPA, Security, Validation), JWT (jjwt 0.12.6), Lombok |
| CSDL | MySQL 8.0.16+ hoặc MariaDB 10.4+ (XAMPP) |

## Cấu trúc thư mục

```
TechGear/
├── database/
│   ├── DB_TechGear.sql          Script tạo CSDL techgear + dữ liệu mẫu
│   └── DB_TechGear_ban_cu.sql   Bản cũ (đề tài thời trang/Laravel), giữ lại để đối chiếu
├── docs/                         Tài liệu báo cáo
└── source/
    ├── backend/                  Spring Boot REST API (cổng 8080)
    │   └── src/main/java/com/techgear/
    │       ├── config/           SecurityConfig (JWT, CORS, phân quyền), WebConfig (ảnh upload)
    │       ├── controller/       REST controller
    │       ├── dto/              Dữ liệu vào/ra API (Java record)
    │       ├── entity/           24 entity JPA ánh xạ đúng các bảng (+ enums)
    │       ├── exception/        ApiException, GlobalExceptionHandler (lỗi trả JSON tiếng Việt)
    │       ├── repository/       Spring Data JPA repository
    │       ├── security/         JwtService, JwtAuthFilter, UserDetailsService
    │       ├── service/          Xử lý nghiệp vụ
    │       └── util/             SlugUtil (tạo slug từ tiếng Việt)
    └── frontend/                 React (cổng 5173)
        └── src/
            ├── api/              axiosClient (tự gắn token), các hàm gọi API
            ├── components/       Header, Footer, ProductCard, ProtectedRoute...
            ├── context/          AuthContext (đăng nhập), CartContext (giỏ hàng localStorage)
            ├── pages/            Trang chủ, danh sách, chi tiết, giỏ hàng, đăng nhập, đăng ký...
            └── utils/            Định dạng tiền VNĐ, đường dẫn ảnh
```

## Cách chạy

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
Yêu cầu: **JDK 17 trở lên**, Maven (IntelliJ IDEA có sẵn Maven).

1. IntelliJ IDEA → **Open** → chọn thư mục `source/backend` (nhận diện `pom.xml`).
2. Đợi Maven tải thư viện. Nếu IntelliJ hỏi bật *Annotation Processing* cho Lombok → chọn **Enable**.
3. Kiểm tra `src/main/resources/application.properties` (mặc định user `root`, mật khẩu rỗng).
4. Chạy `TechGearApplication`. Hoặc dòng lệnh: `mvn spring-boot:run`.

Kiểm tra nhanh: mở `http://localhost:8080/api/danh-muc` sẽ thấy JSON danh mục.

### 3. Frontend
Yêu cầu: Node.js 18 trở lên.

```bash
cd source/frontend
npm install
npm run dev
```
Mở `http://localhost:5173`. Địa chỉ backend cấu hình trong file `.env` (`VITE_API_URL`).

## Danh sách API hiện có

| Phương thức | Đường dẫn | Quyền | Mô tả |
|---|---|---|---|
| POST | `/api/auth/dang-ky` | Công khai | Đăng ký, trả về token |
| POST | `/api/auth/dang-nhap` | Công khai | Đăng nhập, trả về token |
| GET | `/api/auth/toi` | Đã đăng nhập | Thông tin tài khoản hiện tại |
| GET | `/api/danh-muc` | Công khai | Cây danh mục (cha – con) |
| GET | `/api/thuong-hieu` | Công khai | Thương hiệu đang hiển thị |
| GET | `/api/san-pham` | Công khai | Tìm kiếm, lọc, sắp xếp, phân trang |
| GET | `/api/san-pham/noi-bat` | Công khai | 8 sản phẩm nổi bật |
| GET | `/api/san-pham/{slug}` | Công khai | Chi tiết sản phẩm (tùy chọn, biến thể, thông số) |
| GET/POST/PUT/DELETE | `/api/admin/thuong-hieu` | Quản trị | Quản lý thương hiệu (mẫu CRUD cho admin) |

Tham số `/api/san-pham`: `tuKhoa`, `danhMuc` (slug, gồm cả danh mục con), `thuongHieu` (slug), `giaTu`, `giaDen`, `sapXep` (`moi_nhat` | `gia_tang` | `gia_giam` | `pho_bien`), `trang` (bắt đầu từ 0), `kichThuoc` (tối đa 48).

Gửi token: header `Authorization: Bearer <token>`. Lỗi trả về dạng `{ "trangThai": 400, "thongBao": "...", "loi": { ... } }`.

## Thay đổi so với file CSDL ban đầu

- Đổi tên đề tài, công nghệ, tên CSDL `shop_thoi_trang` → `techgear`.
- Bỏ các cột riêng của Laravel (`remember_token`, `email_verified_at`); thêm bảng `ma_dat_lai_mat_khau` cho chức năng quên mật khẩu.
- `tuy_chon.loai`: `mau_sac, kich_co, khac` → `mau_sac, dung_luong, phien_ban, khac`.
- Thêm `san_pham.bao_hanh_thang` (thời gian bảo hành).
- `gio_hang`: chỉ lưu giỏ của khách đã đăng nhập (bỏ `ma_phien`, `het_han_luc`); khách vãng lai lưu giỏ ở localStorage của React.
- `don_hang`: thêm ràng buộc `tien_giam_gia <= tam_tinh`.
- Cột thời gian dùng `DATETIME` với `created_at` mặc định `CURRENT_TIMESTAMP`.
- Thêm dữ liệu mẫu: 2 tài khoản, 11 danh mục, 6 thương hiệu, 8 sản phẩm, 15 biến thể, 2 mã giảm giá, cài đặt cửa hàng.

## Việc tiếp theo (gợi ý)

1. API giỏ hàng trên server + gộp giỏ localStorage khi đăng nhập.
2. Đặt hàng: kiểm tra tồn kho, áp mã giảm giá, tính phí vận chuyển, ghi `lich_su_ton_kho`.
3. Thanh toán VNPay (môi trường sandbox).
4. Trang quản trị: sản phẩm (kèm biến thể, upload ảnh), đơn hàng, mã giảm giá, banner, thống kê.
5. Đánh giá sản phẩm, danh sách yêu thích, sổ địa chỉ.
