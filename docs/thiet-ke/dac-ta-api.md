# Đặc tả API – TechGear Backend

Tài liệu tương tác (thử gọi API trực tiếp): chạy backend rồi mở `http://localhost:8080/swagger-ui.html`.
Tài liệu này là bản tóm tắt để đưa vào báo cáo; khi có khác biệt thì Swagger (sinh từ code) là chuẩn.

## 1. Quy ước chung

| Mục | Quy ước |
|---|---|
| Địa chỉ gốc | `http://localhost:8080/api` |
| Định dạng | JSON, UTF-8. Tiền tệ là số nguyên VNĐ. Thời gian dạng ISO-8601 (`2026-10-26T22:50:45`), múi giờ Asia/Ho_Chi_Minh |
| Xác thực | JWT trong header `Authorization: Bearer <token>`, nhận được khi đăng ký / đăng nhập. Token hết hạn sau 24 giờ |
| Phân quyền | **Công khai** – không cần token · **Khách hàng** – đã đăng nhập (mọi vai trò) · **Quản trị** – vai trò `quan_tri`, các đường dẫn `/api/admin/**` |
| Phân trang | Tham số `trang` (bắt đầu từ 0), `kichThuoc`. Kết quả: `{ noiDung, trang, kichThuoc, tongSoPhanTu, tongSoTrang }` |
| Đặt tên | Đường dẫn tiếng Việt không dấu, gạch nối (`/san-pham/noi-bat`); trường JSON camelCase tiếng Việt (`giaBan`, `soLuongTon`) |

### Định dạng lỗi

Mọi lỗi đều trả cùng một dạng:

```json
{
  "thoiGian": "2026-10-26T22:50:45.559",
  "trangThai": 400,
  "thongBao": "Dữ liệu không hợp lệ",
  "loi": { "email": "Email không hợp lệ" }
}
```

`loi` chỉ có khi lỗi kiểm tra dữ liệu (cho biết trường nào sai).

| Mã | Ý nghĩa |
|---|---|
| 400 | Dữ liệu không hợp lệ, sai định dạng, thiếu tham số |
| 401 | Chưa đăng nhập / token sai hoặc hết hạn |
| 403 | Không đủ quyền, hoặc tài khoản bị khóa |
| 404 | Không tìm thấy dữ liệu hoặc đường dẫn |
| 405 | Sai phương thức HTTP |
| 409 | Trùng dữ liệu (email, tên...) hoặc xung đột nghiệp vụ |
| 413 | File tải lên quá 5MB |
| 500 | Lỗi hệ thống |

## 2. Danh sách API

> Cột **Phase** cho biết API thuộc giai đoạn nào trong kế hoạch hoàn thiện backend; các mục chưa làm sẽ được bổ sung khi tới phase đó.

### 2.1. Xác thực & tài khoản

| Phương thức | Đường dẫn | Quyền | Mô tả | Phase |
|---|---|---|---|---|
| POST | `/auth/dang-ky` | Công khai | Đăng ký, trả về token | có sẵn |
| POST | `/auth/dang-nhap` | Công khai | Đăng nhập, trả về token | có sẵn |
| GET | `/auth/toi` | Khách hàng | Thông tin tài khoản hiện tại (giống `GET /tai-khoan`) | có sẵn |
| POST | `/auth/quen-mat-khau` | Công khai | Gửi liên kết đặt lại mật khẩu qua email | 2 |
| POST | `/auth/dat-lai-mat-khau` | Công khai | Đặt mật khẩu mới bằng mã trong liên kết | 2 |
| GET | `/tai-khoan` | Khách hàng | Thông tin tài khoản | 2 |
| PUT | `/tai-khoan` | Khách hàng | Cập nhật họ tên, số điện thoại, giới tính, ngày sinh | 2 |
| PUT | `/tai-khoan/mat-khau` | Khách hàng | Đổi mật khẩu (cần mật khẩu hiện tại) | 2 |
| POST | `/tai-khoan/anh-dai-dien` | Khách hàng | Đổi ảnh đại diện (multipart, trường `file`) | 2 |
| GET | `/tai-khoan/dia-chi` | Khách hàng | Sổ địa chỉ (mặc định đứng đầu) | 2 |
| POST | `/tai-khoan/dia-chi` | Khách hàng | Thêm địa chỉ | 2 |
| PUT | `/tai-khoan/dia-chi/{id}` | Khách hàng | Sửa địa chỉ | 2 |
| PATCH | `/tai-khoan/dia-chi/{id}/mac-dinh` | Khách hàng | Đặt làm địa chỉ mặc định | 2 |
| DELETE | `/tai-khoan/dia-chi/{id}` | Khách hàng | Xóa địa chỉ | 2 |

**Dữ liệu chính**

| API | Request | Response |
|---|---|---|
| Đăng ký | `{ hoTen, email, matKhau (6–100 ký tự), soDienThoai? }` | `{ token, thoiHanMs, nguoiDung }` |
| Đăng nhập | `{ email, matKhau }` | như trên |
| Quên mật khẩu | `{ email }` | `{ thongBao }` – luôn cùng một thông báo |
| Đặt lại mật khẩu | `{ ma, matKhauMoi }` | `{ thongBao }` |
| Cập nhật hồ sơ | `{ hoTen, soDienThoai?, gioiTinh? (nam/nu/khac), ngaySinh? (yyyy-MM-dd) }` | `nguoiDung` |
| Đổi mật khẩu | `{ matKhauCu, matKhauMoi }` | `{ thongBao }` |
| Địa chỉ | `{ tenNguoiNhan, soDienThoai, tinhThanh, phuongXa, diaChiChiTiet, laMacDinh? }` | `{ id, ..., laMacDinh, diaChiDayDu }` |

`nguoiDung` = `{ id, hoTen, email, soDienThoai, gioiTinh, ngaySinh, anhDaiDien, vaiTro, ngayTao }`.

**Quy tắc nghiệp vụ**

- *Quên mật khẩu*: liên kết `FRONTEND_URL/dat-lai-mat-khau?ma=...` hiệu lực 15 phút, dùng một lần; yêu cầu mới vô hiệu mã cũ;
  mỗi tài khoản phải cách 60 giây giữa hai lần yêu cầu (yêu cầu sớm hơn bị bỏ qua lặng lẽ). Tài khoản bị khóa không nhận mã.
  CSDL chỉ lưu bản băm SHA-256 của mã. Email gửi bất đồng bộ sau khi lưu mã; chưa cấu hình SMTP thì liên kết được ghi ra log backend.
- *Sổ địa chỉ*: tối đa 10 địa chỉ; địa chỉ đầu tiên tự là mặc định; luôn có đúng một địa chỉ mặc định; xóa địa chỉ mặc định thì
  địa chỉ mới nhất còn lại thay thế; thao tác trên địa chỉ của người khác trả 404.
- *Ảnh đại diện*: ảnh cũ chỉ bị xóa sau khi lưu thành công ảnh mới.
- *Giới hạn đã biết*: JWT không bị thu hồi khi đổi / đặt lại mật khẩu (token cũ còn hiệu lực tới khi hết hạn 24 giờ).

### 2.2. Danh mục, thương hiệu, sản phẩm (công khai)

| Phương thức | Đường dẫn | Quyền | Mô tả | Phase |
|---|---|---|---|---|
| GET | `/danh-muc` | Công khai | Cây danh mục cha – con đang hiển thị | có sẵn |
| GET | `/thuong-hieu` | Công khai | Thương hiệu đang hiển thị | có sẵn |
| GET | `/san-pham` | Công khai | Tìm kiếm, lọc, sắp xếp, phân trang | có sẵn |
| GET | `/san-pham/noi-bat` | Công khai | 8 sản phẩm nổi bật | có sẵn |
| GET | `/san-pham/{slug}` | Công khai | Chi tiết sản phẩm: tùy chọn, biến thể, thông số, ảnh | có sẵn |

Tham số `GET /san-pham`: `tuKhoa`, `danhMuc` (slug, gồm cả danh mục con), `thuongHieu` (slug), `giaTu`, `giaDen`,
`sapXep` (`moi_nhat` | `gia_tang` | `gia_giam` | `pho_bien`), `trang`, `kichThuoc` (tối đa 48).

### 2.3. Quản trị

| Phương thức | Đường dẫn | Quyền | Mô tả | Phase |
|---|---|---|---|---|
| GET | `/admin/thuong-hieu` | Quản trị | Tất cả thương hiệu (kể cả đang ẩn) | có sẵn |
| POST | `/admin/thuong-hieu` | Quản trị | Thêm thương hiệu (slug tự sinh, không trùng) | có sẵn |
| PUT | `/admin/thuong-hieu/{id}` | Quản trị | Sửa thương hiệu (giữ slug cũ nếu tên không đổi) | có sẵn |
| DELETE | `/admin/thuong-hieu/{id}` | Quản trị | Xóa mềm thương hiệu | có sẵn |
| POST | `/admin/upload?thuMuc=` | Quản trị | Tải ảnh (multipart, trường `file`), trả `{ duongDan }` | 1 |

Upload ảnh: chỉ nhận JPG/PNG/WEBP/GIF (kiểm tra theo nội dung file), tối đa 5MB; `thuMuc` là
`san-pham` | `banner` | `thuong-hieu` | `danh-muc`. Ảnh truy cập tại `http://localhost:8080{duongDan}`.

## 3. Biến môi trường

| Biến | Mặc định | Ý nghĩa |
|---|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | MySQL XAMPP `localhost:3306/techgear`, `root`, rỗng | Kết nối CSDL |
| `JWT_SECRET` | khóa dev có sẵn | Khóa ký JWT (Base64, ≥ 256 bit) – **bắt buộc đổi khi triển khai** |
| `JWT_EXPIRATION_MS` | `86400000` (24 giờ) | Thời hạn token |
| `FRONTEND_URL` | `http://localhost:5173` | Địa chỉ frontend (CORS, link trong email) |
| `CORS_ALLOWED_ORIGINS` | = `FRONTEND_URL` | Danh sách origin, phân cách dấu phẩy |
| `UPLOAD_DIR` | `uploads` | Thư mục lưu ảnh |
| `MAIL_HOST`, `MAIL_PORT` | `smtp.gmail.com`, `587` | Máy chủ SMTP |
| `MAIL_USERNAME`, `MAIL_PASSWORD` | rỗng | Tài khoản SMTP (Gmail dùng App Password). **Để trống thì email chỉ ghi ra log** |
| `MAIL_FROM` | `TechGear <no-reply@techgear.vn>` | Người gửi |
| `RESET_PASSWORD_EXPIRATION_MINUTES` | `15` | Thời hạn liên kết đặt lại mật khẩu (phút) |
