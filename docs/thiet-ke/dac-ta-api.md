# Đặc tả API – Backend TechGear

Phiên bản: 1.0 (Phase 1 – khung, chờ duyệt) · Dữ liệu vào/ra chi tiết của từng API được bổ sung khi làm module tương ứng.

## 1. Quy ước chung
- Gốc: `http://localhost:8080/api` · JSON UTF-8 · tên trường camelCase tiếng Việt không dấu.
- Xác thực: header `Authorization: Bearer <token>` (JWT HS256, hạn 24 giờ). `/api/admin/**` chỉ vai trò `quan_tri`.
- Tiền: số nguyên (đồng). Thời gian: ISO-8601 có múi giờ, VD `2026-11-15T09:30:00+07:00`. Ngày: `yyyy-MM-dd`.
- Giá trị liệt kê viết thường như CSDL, VD `vaiTro`: `quan_tri` | `khach_hang`; `trangThai` đơn: `cho_xac_nhan` | `da_xac_nhan` | `dang_giao` | `hoan_thanh` | `da_huy`.
- Phân trang: tham số `trang` (từ 0), `kichThuoc`; kết quả:
  ```json
  { "noiDung": [ ... ], "trang": 0, "kichThuoc": 12, "tongSoPhanTu": 57, "tongSoTrang": 5 }
  ```
- Swagger UI: `http://localhost:8080/swagger-ui.html` · OpenAPI JSON: `/v3/api-docs`.
- Kiểm thử: collection Postman `postman/TechGear.postman_collection.json`.

### Định dạng lỗi (mọi lỗi, kể cả 401/403)
```json
{ "thoiGian": "2026-11-15T09:30:00+07:00", "trangThai": 400, "thongBao": "Dữ liệu không hợp lệ",
  "loi": { "email": "Email không đúng định dạng" } }
```
`loi` chỉ có khi lỗi kiểm tra dữ liệu theo từng trường.

| Mã | Khi nào |
|---|---|
| 400 | Dữ liệu vào không hợp lệ, sai quy tắc nghiệp vụ (VD: mã giảm giá chưa đủ điều kiện) |
| 401 | Chưa đăng nhập, token sai/hết hạn/đã thu hồi, sai email hoặc mật khẩu |
| 403 | Không đủ quyền, tài khoản bị khóa |
| 404 | Không tìm thấy (kể cả tài nguyên của người khác) |
| 409 | Xung đột: email trùng, không đủ tồn kho, xóa danh mục còn sản phẩm, chuyển trạng thái đơn không hợp lệ |
| 413 / 415 | Ảnh quá 5MB / không phải ảnh |
| 429 | Vượt giới hạn số lần (đăng nhập, quên mật khẩu, tra cứu đơn) |

## 2. Hợp đồng frontend hiện có (giữ nguyên)
Frontend `source/frontend` đang gọi các API dưới đây và đọc đúng các trường liệt kê – backend phải trả đủ (có thể thêm trường, không bớt/đổi tên).

| API | Trường frontend đọc |
|---|---|
| `POST /auth/dang-ky`, `POST /auth/dang-nhap` | `token`, `nguoiDung { id, hoTen, email, soDienThoai, vaiTro }` |
| `GET /auth/toi` | `{ id, hoTen, email, soDienThoai, vaiTro }` |
| `GET /danh-muc` | `[{ id, ten, slug, danhMucCons: [{ id, ten, slug }] }]` |
| `GET /thuong-hieu` | `[{ id, ten, slug }]` |
| `GET /san-pham`, `GET /san-pham/noi-bat` | trang/mảng `{ id, ten, slug, giaBan, giaGoc, anhChinh, tenThuongHieu, conHang }` |
| `GET /san-pham/{slug}` | `{ id, ten, slug, moTaNgan, moTa, giaBan, giaGoc, baoHanhThang, danhMuc { ten, slug }, thuongHieu { ten }, hinhAnhs [{ duongDan }], thongSos [{ ten, giaTri }], tuyChons [{ id, ten, giaTris [{ id, giaTri, maMau }] }], bienThes [{ id, sku, giaBan, giaGoc, soLuongTon, giaTriIds }] }` |
| Mọi lỗi | `thongBao` |

## 3. Danh sách API
Cột "Phase" là phase trong kế hoạch sẽ làm API đó. Quyền: **CK** công khai · **ĐN** đã đăng nhập · **QT** quản trị.

### 3.1 Tài khoản (`taikhoan`) – Phase 3
| PT | Đường dẫn | Quyền | Mô tả |
|---|---|---|---|
| POST | `/auth/dang-ky` | CK | Đăng ký, trả token |
| POST | `/auth/dang-nhap` | CK | Đăng nhập, trả token (429 khi quá số lần) |
| GET | `/auth/toi` | ĐN | Tài khoản hiện tại |
| POST | `/auth/quen-mat-khau` | CK | Gửi liên kết đặt lại mật khẩu |
| POST | `/auth/dat-lai-mat-khau` | CK | Đặt mật khẩu mới bằng mã trong liên kết |
| GET, PUT | `/tai-khoan` | ĐN | Xem / sửa hồ sơ |
| PUT | `/tai-khoan/mat-khau` | ĐN | Đổi mật khẩu (trả token mới) |
| POST | `/tai-khoan/anh-dai-dien` | ĐN | Đổi ảnh đại diện (multipart `file`) |
| GET, POST | `/tai-khoan/dia-chi` | ĐN | Danh sách / thêm địa chỉ |
| PUT, DELETE | `/tai-khoan/dia-chi/{id}` | ĐN | Sửa / xóa địa chỉ |
| PATCH | `/tai-khoan/dia-chi/{id}/mac-dinh` | ĐN | Đặt mặc định |

### 3.2 Danh mục, thương hiệu, sản phẩm, cửa hàng – Phase 4
| PT | Đường dẫn | Quyền | Mô tả |
|---|---|---|---|
| GET | `/danh-muc` | CK | Cây danh mục đang hiển thị |
| GET | `/thuong-hieu` | CK | Thương hiệu đang hiển thị |
| GET | `/san-pham` | CK | Tìm kiếm: `tuKhoa`, `danhMuc` (slug), `thuongHieu` (nhiều slug, phân cách dấu phẩy), `giaTu`, `giaDen`, `sapXep` (`moi_nhat` \| `gia_tang` \| `gia_giam` \| `pho_bien`), `trang`, `kichThuoc` (1–48) |
| GET | `/san-pham/noi-bat` | CK | 8 sản phẩm nổi bật |
| GET | `/san-pham/{slug}` | CK | Chi tiết (tăng lượt xem), kèm `diemTrungBinh`, `soDanhGia` |
| GET | `/san-pham/{slug}/danh-gia` | CK | Đánh giá đã duyệt, phân trang |
| GET | `/banner` | CK | Banner đang hiển thị |
| GET | `/cai-dat` | CK | Cấu hình cửa hàng `{ khoa: giaTri }` |

### 3.3 Giỏ hàng & yêu thích (`giohang`) – Phase 5
| PT | Đường dẫn | Quyền | Mô tả |
|---|---|---|---|
| GET | `/gio-hang` | ĐN | Giỏ hàng (giá, tồn kho hiện tại, tạm tính) |
| POST | `/gio-hang` | ĐN | Thêm `{ bienTheId, soLuong }` (cộng dồn) |
| PUT, DELETE | `/gio-hang/{bienTheId}` | ĐN | Sửa số lượng / xóa dòng |
| POST | `/gio-hang/gop` | ĐN | Gộp giỏ tạm của trình duyệt `[{ bienTheId, soLuong }]` |
| GET | `/yeu-thich` | ĐN | Danh sách yêu thích |
| PUT, DELETE | `/yeu-thich/{sanPhamId}` | ĐN | Thêm / bỏ yêu thích |

### 3.4 Đặt hàng & đơn hàng (`donhang`, `khuyenmai`) – Phase 6
| PT | Đường dẫn | Quyền | Mô tả |
|---|---|---|---|
| POST | `/don-hang/tinh-thu` | CK | Tạm tính, giảm giá, phí ship, tổng (không lưu) |
| POST | `/don-hang` | CK/ĐN | Đặt hàng (vãng lai hoặc đã đăng nhập) |
| GET | `/don-hang` | ĐN | Đơn của tôi (lọc `trangThai`, phân trang) |
| GET | `/don-hang/{maDonHang}` | ĐN | Chi tiết + lịch sử trạng thái |
| POST | `/don-hang/{maDonHang}/huy` | ĐN | Hủy khi còn `cho_xac_nhan` |
| POST | `/don-hang/tra-cuu` | CK | Tra cứu bằng `{ maDonHang, email }` (429 khi quá số lần) |

### 3.5 Thanh toán (`thanhtoan`) – Phase 7
| PT | Đường dẫn | Quyền | Mô tả |
|---|---|---|---|
| POST | `/thanh-toan/vnpay/{maDonHang}` | CK/ĐN | Tạo URL thanh toán VNPay |
| GET | `/thanh-toan/vnpay/ket-qua` | CK | Return URL: đọc kết quả để hiển thị (không đổi trạng thái) |
| GET | `/thanh-toan/vnpay/ipn` | VNPay | IPN: kiểm chữ ký, cập nhật thanh toán; trả `{ RspCode, Message }` theo chuẩn VNPay |

### 3.6 Đánh giá (`danhgia`) – Phase 8
| PT | Đường dẫn | Quyền | Mô tả |
|---|---|---|---|
| GET | `/danh-gia/co-the-viet` | ĐN | Các (đơn, sản phẩm) đã hoàn thành chưa đánh giá |
| POST | `/danh-gia` | ĐN | Viết đánh giá `{ donHangId, sanPhamId, soSao, noiDung }` |

### 3.7 Quản trị – Phase 9 (tất cả quyền QT)
| PT | Đường dẫn | Mô tả |
|---|---|---|
| POST | `/admin/upload?thuMuc=` | Tải ảnh (`san-pham` \| `banner` \| `thuong-hieu` \| `danh-muc`) |
| GET, POST · PUT, DELETE | `/admin/danh-muc` · `/admin/danh-muc/{id}` | Danh mục |
| GET, POST · PUT, DELETE | `/admin/thuong-hieu` · `/admin/thuong-hieu/{id}` | Thương hiệu |
| GET, POST · GET, PUT, DELETE | `/admin/san-pham` · `/admin/san-pham/{id}` | Sản phẩm (kèm thông số, tùy chọn, ảnh) |
| POST · PUT, DELETE | `/admin/san-pham/{id}/bien-the` · `/admin/bien-the/{id}` | Biến thể |
| POST | `/admin/bien-the/{id}/nhap-kho` · `/admin/bien-the/{id}/dieu-chinh` | Nhập kho / điều chỉnh tồn kho |
| GET | `/admin/kho/lich-su` · `/admin/kho/sap-het` | Lịch sử kho / biến thể sắp hết |
| GET · GET | `/admin/don-hang` · `/admin/don-hang/{maDonHang}` | Tìm, lọc đơn / chi tiết |
| PATCH | `/admin/don-hang/{maDonHang}/trang-thai` | Chuyển trạng thái (kèm ghi chú) |
| PATCH | `/admin/don-hang/{maDonHang}/thanh-toan` | Cập nhật trạng thái thanh toán |
| PATCH | `/admin/don-hang/{maDonHang}/ghi-chu` | Ghi chú nội bộ |
| GET, POST · PUT, DELETE | `/admin/ma-giam-gia` · `/admin/ma-giam-gia/{id}` | Mã giảm giá |
| GET, POST · PUT, DELETE | `/admin/banner` · `/admin/banner/{id}` | Banner |
| GET, PUT | `/admin/cai-dat` | Cấu hình cửa hàng |
| GET · PATCH | `/admin/nguoi-dung` · `/admin/nguoi-dung/{id}/trang-thai` | Người dùng / khóa – mở |
| GET · PATCH, DELETE | `/admin/danh-gia` · `/admin/danh-gia/{id}` | Đánh giá / duyệt – ẩn, xóa |
| GET | `/admin/thong-ke/doanh-thu?tu=&den=&theo=ngay\|thang` | Doanh thu |
| GET | `/admin/thong-ke/don-hang` · `/admin/thong-ke/ban-chay` | Đơn theo trạng thái / sản phẩm bán chạy |

## 4. Biến môi trường
| Biến | Mặc định (profile `dev`) | Ý nghĩa |
|---|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | `jdbc:postgresql://localhost:5432/techgear`, `techgear`, `techgear` | Kết nối CSDL (khớp `docker-compose.yml`) |
| `JWT_SECRET` | khóa dev (chỉ profile `dev`; thiếu ở môi trường khác thì không khởi động) | Khóa ký JWT, ≥ 32 byte |
| `FRONTEND_URL` | `http://localhost:5173` | Liên kết trong email |
| `CORS_ALLOWED_ORIGINS` | = `FRONTEND_URL` | Origin được phép, phân cách dấu phẩy |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` | Gmail SMTP, rỗng | Chưa có `MAIL_USERNAME` thì ghi liên kết ra log |
| `VNPAY_TMN_CODE`, `VNPAY_HASH_SECRET`, `VNPAY_PAY_URL`, `VNPAY_RETURN_URL` | rỗng, sandbox | Cấu hình VNPay (Phase 7) |
| `UPLOAD_DIR` | `uploads` | Thư mục lưu ảnh |
