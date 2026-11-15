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

#### Chi tiết API tài khoản

Kiểu dữ liệu dùng chung:
```text
NguoiDung  { id, hoTen, email, soDienThoai, gioiTinh: "nam"|"nu"|"khac"|null, ngaySinh: "yyyy-MM-dd"|null,
             anhDaiDien: "/uploads/anh-dai-dien/..."|null, vaiTro: "quan_tri"|"khach_hang" }
XacThuc    { token, nguoiDung: NguoiDung }
DiaChi     { id, tenNguoiNhan, soDienThoai, tinhThanh, phuongXa, diaChiChiTiet, laMacDinh }
ThongBao   { thongBao }
```
Quy tắc kiểm tra: mật khẩu 8–72 ký tự (tối đa 72 byte), có cả chữ và số; số điện thoại `0` + 9 chữ số; email đúng định dạng,
so sánh không phân biệt hoa thường và lưu chữ thường. Sai quy tắc → 400 kèm `loi` theo từng trường.

| API | Gửi lên | Trả về | Lỗi riêng |
|---|---|---|---|
| `POST /auth/dang-ky` | `{ hoTen, email, matKhau, soDienThoai? }` (trường thừa như `nhapLai` bị bỏ qua; `soDienThoai` rỗng = không có) | 201 `XacThuc` | 409 "Email đã được sử dụng" |
| `POST /auth/dang-nhap` | `{ email, matKhau }` | 200 `XacThuc` | 401 "Email hoặc mật khẩu không đúng" (một thông báo cho mọi trường hợp sai); 403 "Tài khoản đã bị khóa" (chỉ khi mật khẩu đúng); 429 sau 5 lần sai / 15 phút cho cùng email + IP |
| `GET /auth/toi` | – | 200 `NguoiDung` | 401 |
| `POST /auth/quen-mat-khau` | `{ email }` | 200 `ThongBao` "Nếu email đã đăng ký, liên kết đặt lại mật khẩu đã được gửi" (giống nhau dù email có tồn tại hay không) | 429 khi cùng email gửi lại trong 60 giây hoặc một IP gửi quá 10 lần / 15 phút |
| `POST /auth/dat-lai-mat-khau` | `{ ma, matKhauMoi }` | 200 `ThongBao` "Đặt lại mật khẩu thành công, vui lòng đăng nhập" | 400 "Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn" |
| `GET /tai-khoan` | – | 200 `NguoiDung` | |
| `PUT /tai-khoan` | `{ hoTen, soDienThoai?, gioiTinh?, ngaySinh? }` (ghi đè cả 4 trường; `ngaySinh` phải trong quá khứ) | 200 `NguoiDung` | |
| `PUT /tai-khoan/mat-khau` | `{ matKhauHienTai, matKhauMoi }` | 200 `XacThuc` (token mới; mọi token cũ hết hiệu lực) | 400 "Mật khẩu hiện tại không đúng" |
| `POST /tai-khoan/anh-dai-dien` | multipart `file` (JPG/PNG/WEBP/GIF ≤ 5MB) | 200 `NguoiDung` | 413, 415 |
| `GET /tai-khoan/dia-chi` | – | 200 `DiaChi[]` (mặc định trước, rồi mới nhất trước) | |
| `POST /tai-khoan/dia-chi` | `{ tenNguoiNhan, soDienThoai, tinhThanh, phuongXa, diaChiChiTiet, laMacDinh? }` | 201 `DiaChi` (địa chỉ đầu tiên luôn là mặc định) | 400 "Chỉ lưu tối đa 10 địa chỉ" |
| `PUT /tai-khoan/dia-chi/{id}` | như trên (`laMacDinh: true` → thành mặc định; `false` không bỏ được mặc định) | 200 `DiaChi` | 404 "Không tìm thấy địa chỉ" (kể cả địa chỉ của người khác) |
| `DELETE /tai-khoan/dia-chi/{id}` | – | 204 (xóa địa chỉ mặc định → địa chỉ mới nhất còn lại thành mặc định) | 404 |
| `PATCH /tai-khoan/dia-chi/{id}/mac-dinh` | – | 200 `DiaChi` | 404 |

Token bị từ chối (401 "Phiên đăng nhập không hợp lệ hoặc đã hết hạn") khi: sai chữ ký, hết hạn, tài khoản không còn,
tài khoản bị khóa, hoặc phát hành trước lần đổi/đặt lại mật khẩu gần nhất (so theo giây).
Liên kết đặt lại mật khẩu: `FRONTEND_URL/dat-lai-mat-khau?ma=<mã>` – mã ngẫu nhiên 256 bit, CSDL chỉ lưu SHA-256,
hiệu lực 15 phút, dùng một lần; đặt lại thành công thì mọi mã khác của tài khoản cũng hết hiệu lực.
Email gửi sau khi giao dịch lưu thành công, không chặn phản hồi; chưa cấu hình `MAIL_USERNAME` thì ghi liên kết ra log.

### 3.2 Danh mục, thương hiệu, sản phẩm, cửa hàng – Phase 4
| PT | Đường dẫn | Quyền | Mô tả |
|---|---|---|---|
| GET | `/danh-muc` | CK | Cây danh mục đang hiển thị |
| GET | `/thuong-hieu` | CK | Thương hiệu đang hiển thị |
| GET | `/san-pham` | CK | Tìm kiếm: `tuKhoa`, `danhMuc` (slug), `thuongHieu` (nhiều slug, phân cách dấu phẩy), `giaTu`, `giaDen`, `sapXep` (`moi_nhat` \| `gia_tang` \| `gia_giam` \| `pho_bien`), `trang`, `kichThuoc` (1–48) |
| GET | `/san-pham/noi-bat` | CK | 8 sản phẩm nổi bật |
| GET | `/san-pham/{slug}` | CK | Chi tiết (tăng lượt xem) |
| GET | `/banner` | CK | Banner đang hiển thị |
| GET | `/cai-dat` | CK | Cấu hình cửa hàng `{ khoa: giaTri }` |

Điểm đánh giá (`diemTrungBinh`, `soDanhGia` trong chi tiết) và `GET /san-pham/{slug}/danh-gia` chuyển sang Phase 8:
bảng `danh_gia` có khóa ngoại tới `don_hang` (Phase 6) nên chưa tạo được ở Phase 4.

#### Chi tiết API sản phẩm, cửa hàng

```text
DanhMuc         { id, ten, slug, moTa?, hinhAnh?, danhMucCons: DanhMuc[] }   (cấp con có danhMucCons rỗng)
ThuongHieu      { id, ten, slug, logo? }
SanPhamTomTat   { id, ten, slug, giaBan, giaGoc?, anhChinh?, tenThuongHieu?, conHang }
SanPhamChiTiet  { id, ten, slug, moTaNgan?, moTa?, giaBan, giaGoc?, baoHanhThang, conHang,
                  danhMuc { id, ten, slug }, thuongHieu? { id, ten, slug },
                  hinhAnhs [{ id, duongDan, moTa?, giaTriTuyChonId?, laAnhChinh }],
                  thongSos [{ ten, giaTri }],
                  tuyChons [{ id, ten, loai, giaTris [{ id, giaTri, maMau? }] }],
                  bienThes [{ id, sku, giaBan, giaGoc?, soLuongTon, giaTriIds: number[] }] }
Banner          { id, tieuDe?, hinhAnh, lienKet? }
```
`?` = không có trong JSON khi trống. Mảng luôn có (có thể rỗng).

Sản phẩm được hiển thị khi: đang bật, chưa xóa, danh mục (và danh mục cha nếu có) đang bật và chưa xóa, có ít nhất một biến thể
đang bật và chưa xóa. Thương hiệu bị ẩn chỉ biến mất khỏi danh sách thương hiệu, sản phẩm của nó vẫn hiển thị.
`giaBan`/`giaGoc` của sản phẩm = giá biến thể đang bán rẻ nhất. `conHang` = có biến thể đang bán còn tồn kho.
`anhChinh` = ảnh chính, không có thì ảnh đầu tiên theo thứ tự.

| API | Tham số / trả về | Lỗi |
|---|---|---|
| `GET /danh-muc` | Cây 2 cấp, sắp theo `thuTu` rồi `id`. Danh mục cha bị ẩn thì cả nhánh bị ẩn | |
| `GET /thuong-hieu` | `ThuongHieu[]` sắp theo tên | |
| `GET /san-pham` | `tuKhoa` (≤ 100 ký tự; không phân biệt hoa thường và dấu, nhiều từ thì sản phẩm phải chứa đủ các từ; tìm trong tên sản phẩm và tên thương hiệu), `danhMuc` (slug; slug danh mục cha gồm cả danh mục con), `thuongHieu` (slug, nhiều slug cách nhau dấu phẩy), `giaTu`, `giaDen` (≥ 0; có biến thể đang bán với giá trong khoảng), `sapXep` (`moi_nhat` mặc định \| `gia_tang` \| `gia_giam` \| `pho_bien` = lượt xem), `trang` (≥ 0), `kichThuoc` (1–48, mặc định 12) → trang `SanPhamTomTat`. Cùng giá trị sắp xếp thì xếp theo `id` giảm dần nên hai trang liền nhau không trùng | 400 khi tham số sai kiểu, ngoài khoảng, `giaTu > giaDen` hoặc `sapXep` lạ (`loi` theo tham số hoặc `thongBao` "Tham số 'x' không hợp lệ") |
| `GET /san-pham/noi-bat` | Tối đa 8 `SanPhamTomTat` được đánh dấu nổi bật, nhiều lượt xem trước | |
| `GET /san-pham/{slug}` | `SanPhamChiTiet`; chỉ gồm biến thể đang bán; ảnh chính đứng đầu `hinhAnhs`; mỗi lần gọi tăng `luot_xem` 1 | 404 "Không tìm thấy sản phẩm" |
| `GET /banner` | `Banner[]` đang bật và trong khung giờ (mốc trống = không giới hạn), sắp theo `thuTu` | |
| `GET /cai-dat` | `{ ten_cua_hang, hotline, email_lien_he, phi_van_chuyen, mien_phi_van_chuyen_tu }` (giá trị dạng chuỗi) | |

### 3.3 Giỏ hàng & yêu thích (`giohang`) – Phase 5
| PT | Đường dẫn | Quyền | Mô tả |
|---|---|---|---|
| GET | `/gio-hang` | ĐN | Giỏ hàng (giá, tồn kho hiện tại, tạm tính) |
| POST | `/gio-hang` | ĐN | Thêm `{ bienTheId, soLuong }` (cộng dồn) |
| PUT, DELETE | `/gio-hang/{bienTheId}` | ĐN | Sửa số lượng / xóa dòng |
| POST | `/gio-hang/gop` | ĐN | Gộp giỏ tạm của trình duyệt `[{ bienTheId, soLuong }]` |
| GET | `/yeu-thich` | ĐN | Danh sách yêu thích |
| PUT, DELETE | `/yeu-thich/{sanPhamId}` | ĐN | Thêm / bỏ yêu thích |

#### Chi tiết API giỏ hàng, yêu thích

```text
DongGioHang  { bienTheId, sanPhamId, sanPhamSlug, ten, phanLoai?, anh?, sku, giaBan, giaGoc?, soLuong, tonKho, conBan }
GioHang      { dongs: DongGioHang[], tongSoLuong, tamTinh }
```
Tên trường dòng giỏ trùng dòng giỏ `localStorage` của frontend (`bienTheId, sanPhamSlug, ten, phanLoai, giaBan, anh,
soLuong, tonKho`). `giaBan`, `tonKho` là giá trị **hiện tại**. `phanLoai` = các giá trị tùy chọn nối bằng " / " (VD "Đen").
`anh` = ảnh gắn với màu của biến thể, không có thì ảnh chính. `conBan = false` khi biến thể/sản phẩm/danh mục đã tắt hoặc xóa:
dòng vẫn hiện để khách biết, không tính vào `tongSoLuong`, `tamTinh`. `tamTinh` = Σ `giaBan × soLuong` các dòng còn bán.
Dòng mới thêm đứng đầu. Mỗi giỏ tối đa 50 dòng; số lượng mỗi dòng 1–999 và không vượt tồn kho lúc thêm/sửa
(đặt hàng ở Phase 6 kiểm tra tồn kho lần cuối).

| API | Gửi lên | Trả về | Lỗi riêng |
|---|---|---|---|
| `GET /gio-hang` | – | 200 `GioHang` | |
| `POST /gio-hang` | `{ bienTheId, soLuong }` (cộng dồn nếu đã có) | 200 `GioHang` | 404 "Phiên bản sản phẩm không còn bán"; 409 "Sản phẩm đã hết hàng" / "Chỉ còn N sản phẩm trong kho" (tính cả số đã có trong giỏ); 400 "Giỏ hàng tối đa 50 sản phẩm" |
| `PUT /gio-hang/{bienTheId}` | `{ soLuong }` (đặt lại số lượng) | 200 `GioHang` | 404 "Sản phẩm không có trong giỏ hàng" / "Phiên bản sản phẩm không còn bán"; 409 như trên |
| `DELETE /gio-hang/{bienTheId}` | – | 200 `GioHang` (không có dòng đó cũng 200) | |
| `POST /gio-hang/gop` | `[{ bienTheId, soLuong }]` (≤ 100 phần tử): giỏ tạm của trình duyệt khi đăng nhập | 200 `GioHang` | Không báo lỗi từng dòng: cộng dồn, cắt theo tồn kho, bỏ dòng không còn bán/hết hàng/vượt 50 dòng |
| `GET /yeu-thich` | – | 200 `SanPhamTomTat[]`, yêu thích sau đứng trước; sản phẩm đã ẩn không hiện | |
| `PUT /yeu-thich/{sanPhamId}` | – | 204 (đã có cũng 204) | 404 "Không tìm thấy sản phẩm"; 400 "Danh sách yêu thích tối đa 100 sản phẩm" |
| `DELETE /yeu-thich/{sanPhamId}` | – | 204 (không có cũng 204) | |

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
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | `jdbc:postgresql://localhost:5433/techgear`, `techgear`, `techgear` | Kết nối CSDL (khớp `docker-compose.yml`) |
| `JWT_SECRET` | khóa dev (chỉ profile `dev`; thiếu ở môi trường khác thì không khởi động) | Khóa ký JWT, ≥ 32 byte |
| `FRONTEND_URL` | `http://localhost:5173` | Liên kết trong email |
| `CORS_ALLOWED_ORIGINS` | = `FRONTEND_URL` | Origin được phép, phân cách dấu phẩy |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` | Gmail SMTP, rỗng | Chưa có `MAIL_USERNAME` thì ghi liên kết ra log |
| `VNPAY_TMN_CODE`, `VNPAY_HASH_SECRET`, `VNPAY_PAY_URL`, `VNPAY_RETURN_URL` | rỗng, sandbox | Cấu hình VNPay (Phase 7) |
| `UPLOAD_DIR` | `uploads` | Thư mục lưu ảnh |
