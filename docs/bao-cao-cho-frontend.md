# Báo cáo backend cho người làm frontend

Người viết: Trần Hoàng Long (backend + CSDL) · Cập nhật: 15/11/2026 · Nhánh: `hoanglong`

Tài liệu này tóm tắt những gì backend mới đã có, chỗ nào khác backend cũ, và việc frontend cần làm. Mỗi phase backend
xong sẽ thêm một mục mới ở cuối. Chi tiết từng API (gửi lên, trả về, mã lỗi) nằm ở
[`thiet-ke/dac-ta-api.md`](thiet-ke/dac-ta-api.md). Tài liệu này không lặp lại các bảng trong đó.

## 1. Chạy backend để thử

```bash
docker compose up -d                 # ở gốc repo: PostgreSQL 17, cổng 5433
cd source/backend && mvn spring-boot:run
```
- API: `http://localhost:8080/api`, trùng `VITE_API_URL` mặc định của frontend nên không phải sửa `.env`.
- Swagger UI để gọi thử: `http://localhost:8080/swagger-ui.html`. Đăng nhập lấy token, bấm **Authorize** rồi dán token vào.
- Tài khoản mẫu: `admin@techgear.vn` / `Admin@123` (quản trị), `khach@techgear.vn` / `Khach@123` (khách hàng).
- CORS chỉ cho phép `http://localhost:5173`. Vite tự đổi sang cổng khác khi 5173 bận (VD 5174) thì mọi lệnh gọi bị chặn.
  Gặp vậy thì tắt tiến trình đang chiếm 5173, hoặc chạy backend với `FRONTEND_URL=http://localhost:5174`.
- Chưa cấu hình email thì liên kết đặt lại mật khẩu được in ra log của backend (dòng `Chưa cấu hình MAIL_USERNAME …`).

## 2. Tình trạng

| Phần | Backend | Frontend dùng được chưa |
|---|---|---|
| Tài khoản: đăng ký, đăng nhập, `/auth/toi` | Xong (Phase 3) | Được, giữ nguyên hợp đồng cũ |
| Hồ sơ, đổi mật khẩu, ảnh đại diện, quên/đặt lại mật khẩu, sổ địa chỉ | Xong (Phase 3) | API mới, frontend chưa có giao diện |
| Danh mục, thương hiệu, sản phẩm | Phase 4, chưa làm | Hiện chưa chạy: trang chủ và danh sách sản phẩm sẽ báo lỗi cho tới khi xong Phase 4 |
| Giỏ hàng server, đặt hàng, VNPay, đánh giá, quản trị | Phase 5–9 | Chưa |

## 3. Quy ước chung (áp dụng mọi API)

- Mọi lỗi có dạng `{ thoiGian, trangThai, thongBao, loi? }`. `thongBao` là câu tiếng Việt hiển thị được ngay.
  Hàm `layThongBaoLoi` trong `axiosClient.js` đã đọc đúng trường này.
- Lỗi dữ liệu sai (400) có thêm `loi`: đối tượng `{ tenTruong: "thông báo" }`. Khi đó `thongBao` chỉ là câu chung
  "Dữ liệu không hợp lệ", nên form nào cũng nên hiện `loi` cạnh từng ô như `RegisterPage` đang làm.
- Giá trị liệt kê viết thường: `vaiTro` là `quan_tri` | `khach_hang`, `gioiTinh` là `nam` | `nu` | `khac`.
- Trường không có giá trị thì **không có trong JSON** (không trả `null`). VD người dùng chưa nhập số điện thoại thì
  không có `soDienThoai`. Cách `nguoiDung.soDienThoai || '—'` hiện tại vẫn đúng.
- Ngày dạng `yyyy-MM-dd`, thời gian dạng ISO-8601 có múi giờ.

## 4. Phase 3 – Tài khoản

Sơ đồ luồng: [đăng ký](thiet-ke/04_hd_dang_ky.drawio.png), [đăng nhập](thiet-ke/05_hd_dang_nhap.drawio.png),
[quên mật khẩu](thiet-ke/06_hd_quen_mat_khau.drawio.png), [tuần tự đăng nhập + JWT](thiet-ke/07_td_dang_nhap_jwt.drawio.png).

### 4.1 Khác backend cũ, có ảnh hưởng tới frontend hiện tại

| Thay đổi | Ảnh hưởng | Cần làm |
|---|---|---|
| Mật khẩu phải từ 8 đến 72 ký tự, có cả chữ và số | Form đăng ký ghi "ít nhất 6 ký tự", `minLength: 6`. Nhập 6–7 ký tự thì backend trả 400 và hiện lỗi dưới ô mật khẩu | Sửa nhãn thành "8–72 ký tự, có chữ và số", `minLength: 8` |
| Đăng nhập sai 5 lần trong 15 phút (cùng email + IP) thì trả 429 | Trang đăng nhập hiện `thongBao` "Bạn thao tác quá nhiều lần, vui lòng thử lại sau ít phút" | Không bắt buộc sửa |
| Tài khoản bị khóa: đăng nhập trả 403 "Tài khoản đã bị khóa", token đang dùng trả 401 | Tự hiện thông báo; token bị xóa nhờ interceptor | Không bắt buộc sửa |
| Đổi hoặc đặt lại mật khẩu làm **mọi token cũ** hết hiệu lực (401) | Thiết bị khác đang đăng nhập sẽ bị đăng xuất ở lần gọi API kế tiếp | Xem 4.3 |
| Email không phân biệt hoa thường, lưu chữ thường | `nguoiDung.email` trả về luôn là chữ thường | Không |
| Các API công khai (đăng ký, đăng nhập, quên/đặt lại mật khẩu, sau này là xem sản phẩm) bỏ qua header `Authorization` | Token cũ hết hạn còn trong `localStorage` không làm hỏng đăng nhập hay xem sản phẩm nữa | Không |

### 4.2 API mới, frontend chưa có giao diện

Hình dạng dữ liệu và mã lỗi của từng API: xem mục "Chi tiết API tài khoản" trong
[`dac-ta-api.md`](thiet-ke/dac-ta-api.md). Tất cả cần đăng nhập, trừ hai API quên/đặt lại mật khẩu.

| Màn hình gợi ý | API | Ghi chú cho giao diện |
|---|---|---|
| Trang tài khoản: sửa hồ sơ | `GET`, `PUT /tai-khoan` | `PUT` ghi đè cả 4 trường `hoTen, soDienThoai, gioiTinh, ngaySinh`: gửi lại giá trị cũ của trường không sửa, gửi rỗng/không gửi là xóa. Sau khi lưu nên cập nhật `nguoiDung` trong `AuthContext` bằng dữ liệu trả về |
| Đổi mật khẩu | `PUT /tai-khoan/mat-khau` | Trả `{ token, nguoiDung }` giống đăng nhập: **phải lưu token mới** (gọi `luuPhien`), nếu không lần gọi sau sẽ bị 401 |
| Ảnh đại diện | `POST /tai-khoan/anh-dai-dien` | `multipart/form-data`, trường `file`, JPG/PNG/WEBP/GIF ≤ 5MB (413 quá lớn, 415 sai loại). Phải bỏ header `Content-Type: application/json` mặc định của `axiosClient` cho request này (truyền `FormData` và để trình duyệt tự đặt). `anhDaiDien` trả về là đường dẫn tương đối `/uploads/anh-dai-dien/…`, nằm ở gốc backend (`http://localhost:8080/uploads/…`), **không** nằm dưới `/api` |
| Quên mật khẩu (trang công khai) | `POST /auth/quen-mat-khau` `{ email }` | Luôn trả cùng một câu dù email có hay không: chỉ cần hiện `thongBao`. Gửi lại cùng email trong 60 giây thì 429 |
| Đặt lại mật khẩu: **route `/dat-lai-mat-khau`** | `POST /auth/dat-lai-mat-khau` `{ ma, matKhauMoi }` | Email gửi liên kết `http://localhost:5173/dat-lai-mat-khau?ma=<mã>`. Trang này đọc `ma` từ query string, cho nhập mật khẩu mới + nhập lại, thành công thì chuyển sang `/dang-nhap`. Mã sai, hết hạn (15 phút) hoặc đã dùng thì 400. **Hiện frontend chưa có route này nên liên kết trong email mở ra trang 404** |
| Sổ địa chỉ | `GET`, `POST /tai-khoan/dia-chi`; `PUT`, `DELETE /tai-khoan/dia-chi/{id}`; `PATCH /tai-khoan/dia-chi/{id}/mac-dinh` | Tối đa 10 địa chỉ. Địa chỉ đầu tiên tự thành mặc định; luôn có đúng một mặc định. Danh sách trả về đã xếp mặc định lên đầu. Xóa địa chỉ mặc định thì địa chỉ mới nhất còn lại thành mặc định, nên sau khi xóa hãy tải lại danh sách. Phase 6 (đặt hàng) sẽ dùng sổ địa chỉ này |

### 4.3 Hai chỗ nên sửa ở frontend hiện tại

1. **Token hết hiệu lực nhưng giao diện vẫn hiện "đã đăng nhập".** Interceptor 401 trong `axiosClient.js` xóa token khỏi
   `localStorage` nhưng không xóa `nguoiDung` trong `AuthContext`. Đến khi tải lại trang, header vẫn hiện tên người dùng,
   còn các API cần đăng nhập đều trả 401. Cách sửa gọn: interceptor phát một sự kiện (VD
   `window.dispatchEvent(new Event('techgear:dang-xuat'))`), `AuthProvider` nghe sự kiện đó và gọi `dangXuat()`.
2. **Nhãn mật khẩu ở form đăng ký** (xem 4.1).

## 5. Kiểm thử phía backend

Mọi API ở trên đã có kiểm thử tự động (JUnit trên PostgreSQL thật) và bộ Postman
`postman/TechGear.postman_collection.json`. Có thể import bộ Postman cùng `postman/TechGear.local.postman_environment.json`
để xem request và response mẫu của từng API. Thư mục "01 Tài khoản" có 29 request.

## 6. Liên hệ

Backend trả sai hình dạng dữ liệu hoặc thiếu trường frontend cần: báo kèm request (URL, body) và response nhận được.
Phần frontend do bạn quyết định. Backend không sửa gì trong `source/frontend`.
