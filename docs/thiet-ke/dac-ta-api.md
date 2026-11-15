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
| POST | `/don-hang` | ĐN | Đặt hàng của khách hàng đã đăng nhập |
| POST | `/don-hang/vang-lai` | CK | Đặt hàng của khách vãng lai |
| GET | `/don-hang` | ĐN | Đơn của tôi (lọc `trangThai`, phân trang) |
| GET | `/don-hang/{maDonHang}` | ĐN | Chi tiết + lịch sử trạng thái |
| POST | `/don-hang/{maDonHang}/huy` | ĐN | Hủy khi còn `cho_xac_nhan` |
| POST | `/don-hang/tra-cuu` | CK | Tra cứu bằng `{ maDonHang, email }` (429 khi quá số lần) |

Đặt hàng tách hai API vì đường dẫn công khai bỏ qua token (frontend luôn gắn token cũ): một API "công khai nhưng có token thì
nhận" sẽ làm khách vãng lai còn token hết hạn bị 401.

#### Chi tiết API đặt hàng, đơn hàng

```text
DongDat        { bienTheId, soLuong }                                    (1–50 dòng, số lượng 1–999, trùng biến thể thì cộng)
NguoiNhan      { tenNguoiNhan, soDienThoai, tinhThanh, phuongXa, diaChiChiTiet }
TinhThu        { dongs: DongTinh[], maGiamGia?, tamTinh, tienGiamGia, phiVanChuyen, tongTien }
DongTinh       { bienTheId, sanPhamId, sanPhamSlug, tenSanPham, sku, phanLoai?, hinhAnh?, donGia, soLuong, thanhTien }
DonHangTomTat  { maDonHang, trangThai, trangThaiThanhToan, phuongThucThanhToan, tongTien, soLuongSanPham,
                 tenSanPhamDau, hinhAnhDau?, ngayDat }
DonHang        { maDonHang, trangThai, trangThaiThanhToan, phuongThucThanhToan, tenNguoiNhan, email, soDienThoai,
                 diaChiGiaoHang, ghiChu?, maGiamGia?, tamTinh, tienGiamGia, phiVanChuyen, tongTien, ngayDat, coTheHuy,
                 dongs: DongTinh[] (sanPhamId/bienTheId/sanPhamSlug có thể thiếu nếu sản phẩm đã bị xóa hẳn),
                 lichSu: [{ trangThai, ghiChu?, thoiGian }] }
```
Giá trị liệt kê: `trangThai` `cho_xac_nhan` | `da_xac_nhan` | `dang_giao` | `hoan_thanh` | `da_huy`;
`trangThaiThanhToan` `chua_thanh_toan` | `da_thanh_toan` | `da_hoan_tien`; `phuongThucThanhToan` `cod` | `vnpay`.

Cách tính (mọi số tiền tính ở server từ giá hiện tại, không nhận giá từ frontend):
- `tamTinh` = Σ đơn giá biến thể × số lượng. `tienGiamGia`: mã phần trăm = làm tròn xuống (`tamTinh` × % / 100), không vượt
  `giamToiDa` nếu có; mã số tiền = giá trị mã; cả hai không vượt `tamTinh`.
- `phiVanChuyen` = cài đặt `phi_van_chuyen`, bằng 0 khi `tamTinh − tienGiamGia` ≥ `mien_phi_van_chuyen_tu`.
- `tongTien` = `tamTinh − tienGiamGia + phiVanChuyen` (CSDL cũng kiểm công thức này).

Mã giảm giá (không phân biệt hoa thường) hợp lệ khi: tồn tại, đang bật, trong khung giờ, `tamTinh` ≥ đơn tối thiểu, chưa hết
tổng lượt, khách chưa hết lượt của mình (đếm đơn chưa hủy theo tài khoản **hoặc** email). Sai thì 400 với một trong các thông báo:
"Mã giảm giá không tồn tại", "Mã giảm giá đã ngừng áp dụng", "Mã giảm giá chưa đến thời gian sử dụng", "Mã giảm giá đã hết hạn",
"Đơn hàng tối thiểu 500.000đ để dùng mã này", "Mã giảm giá đã hết lượt sử dụng", "Bạn đã dùng hết lượt của mã giảm giá này".

| API | Gửi lên | Trả về | Lỗi riêng |
|---|---|---|---|
| `POST /don-hang/tinh-thu` | `{ dongs, maGiamGia?, email? }` (`email` để kiểm lượt mỗi người) | 200 `TinhThu` (không lưu, không giữ hàng) | Như đặt hàng |
| `POST /don-hang` | `{ dongs, ...NguoiNhan, maGiamGia?, phuongThucThanhToan, ghiChu? }`; email lấy từ tài khoản | 201 `DonHang`; các biến thể vừa đặt bị xóa khỏi giỏ server | 404 "Phiên bản sản phẩm không còn bán"; 409 "Sản phẩm X (phân loại) chỉ còn N trong kho" / "... đã hết hàng"; 400 mã giảm giá |
| `POST /don-hang/vang-lai` | như trên + `email` (bắt buộc) | 201 `DonHang` | như trên; 429 khi một IP đặt quá 10 đơn / 15 phút |
| `GET /don-hang` | `trangThai?`, `trang`, `kichThuoc` (1–50, mặc định 10) | 200 trang `DonHangTomTat`, mới nhất trước | |
| `GET /don-hang/{maDonHang}` | – | 200 `DonHang` | 404 "Không tìm thấy đơn hàng" (kể cả đơn người khác) |
| `POST /don-hang/{maDonHang}/huy` | `{ lyDo? }` | 200 `DonHang`: hoàn tồn kho, trả lượt mã giảm giá | 404; 409 "Chỉ hủy được đơn đang chờ xác nhận" |
| `POST /don-hang/tra-cuu` | `{ maDonHang, email }` | 200 `DonHang` (`coTheHuy` = false) | 404 "Không tìm thấy đơn hàng"; 429 sau 10 lần sai / 15 phút mỗi IP |

Mã đơn: `DH` + `yyMMdd` (giờ Việt Nam) + 6 ký tự ngẫu nhiên trong `ABCDEFGHJKLMNPQRSTUVWXYZ23456789`. Đặt hàng khóa dòng các
biến thể theo thứ tự id (tránh khóa chéo) và dòng mã giảm giá, trừ kho trong cùng giao dịch, ghi `lich_su_ton_kho` và
`lich_su_don_hang`. Hai người đặt cùng lúc món chỉ còn 1 thì đúng một người thành công. Đơn VNPay: tạo URL thanh toán và tự hủy
sau 30 phút làm ở Phase 7.

### 3.5 Thanh toán (`thanhtoan`) – Phase 7
| PT | Đường dẫn | Quyền | Mô tả |
|---|---|---|---|
| POST | `/thanh-toan/vnpay/{maDonHang}` | CK | Tạo URL thanh toán VNPay |
| GET | `/thanh-toan/vnpay/ket-qua` | CK | Return URL: đọc kết quả để hiển thị (không đổi trạng thái) |
| GET | `/thanh-toan/vnpay/ipn` | VNPay | IPN: kiểm chữ ký, cập nhật thanh toán; trả `{ RspCode, Message }` theo chuẩn VNPay |

#### Chi tiết thanh toán VNPay

Theo tài liệu VNPay chính thức, phiên bản 2.1.0: <https://sandbox.vnpayment.vn/apis/docs/thanh-toan-pay/pay.html>.

- Chữ ký `vnp_SecureHash` = HMAC-SHA512 (khóa `VNPAY_HASH_SECRET`) của chuỗi `ten=giá_trị_mã_hóa` nối bằng `&`, các tham số
  `vnp_*` xếp tăng dần theo tên, giá trị mã hóa URL kiểu `URLEncoder` (US-ASCII, dấu cách thành `+`); khi kiểm bỏ
  `vnp_SecureHash`, `vnp_SecureHashType`. Như mẫu Java của VNPay.
- URL thanh toán: `vnp_Amount` = tổng tiền × 100, `vnp_CurrCode` VND, `vnp_Locale` vn, `vnp_OrderType` other,
  `vnp_OrderInfo` "Thanh toan don hang <mã>" (không dấu), `vnp_TxnRef` = mã tham chiếu mới mỗi lần tạo (mã đơn + 2 chữ số),
  `vnp_CreateDate`/`vnp_ExpireDate` `yyyyMMddHHmmss` giờ GMT+7; liên kết hết hạn lúc đặt hàng + 30 phút.
- Thanh toán thành công khi **cả** `vnp_ResponseCode` và `vnp_TransactionStatus` = "00".
- Chỉ IPN cập nhật CSDL; Return URL chỉ kiểm chữ ký và đọc trạng thái để hiển thị.
- Đơn VNPay chưa thanh toán: liên kết hết hạn sau 30 phút; hệ thống tự hủy đơn (hoàn kho, trả lượt mã) sau **35 phút**
  (5 phút cho giao dịch đang dở trên VNPay).

| API | Gửi lên | Trả về | Lỗi riêng |
|---|---|---|---|
| `POST /thanh-toan/vnpay/{maDonHang}` | – (mã đơn khó đoán; trả hộ đơn người khác không gây hại) | 200 `{ duongDan }`: frontend chuyển trình duyệt tới đây | 404 "Không tìm thấy đơn hàng"; 409 "Đơn hàng không thanh toán qua VNPay" / "Đơn hàng đã được thanh toán" / "Đơn hàng không còn chờ thanh toán" / "Đơn hàng đã quá hạn thanh toán"; 503 "Chưa cấu hình VNPay" |
| `GET /thanh-toan/vnpay/ket-qua?<toàn bộ tham số VNPay trả về>` | Frontend chuyển nguyên query string từ trang `VNPAY_RETURN_URL` | 200 `{ hopLe, thanhCong, maDonHang?, trangThaiThanhToan?, thongBao }`: `hopLe` = chữ ký đúng; `thanhCong` theo mã VNPay; `trangThaiThanhToan` đọc từ CSDL (có thể còn `chua_thanh_toan` vài giây nếu IPN đến sau) | |
| `GET /thanh-toan/vnpay/ipn` | VNPay gọi | 200 `{ RspCode, Message }`: `00` Confirm Success · `01` Order not found · `02` Order already confirmed · `04` Invalid amount · `97` Invalid signature · `99` Unknow error | Kiểm theo thứ tự: chữ ký → tìm giao dịch theo `vnp_TxnRef` → số tiền → còn chờ xử lý → cập nhật. Gửi lại IPN đã xử lý trả `02`, không ghi lần hai |

### 3.6 Đánh giá (`danhgia`) – Phase 8
| PT | Đường dẫn | Quyền | Mô tả |
|---|---|---|---|
| GET | `/danh-gia/co-the-viet` | ĐN | Các (đơn, sản phẩm) đã hoàn thành chưa đánh giá |
| POST | `/danh-gia` | ĐN | Viết đánh giá `{ maDonHang, sanPhamId, soSao, noiDung? }` |
| GET | `/san-pham/{slug}/danh-gia` | CK | Đánh giá đã duyệt của sản phẩm, phân trang (dời từ Phase 4) |

Chi tiết sản phẩm (`GET /san-pham/{slug}`) có thêm `soDanhGia` và `diemTrungBinh` (làm tròn 1 chữ số; không có khi chưa có
đánh giá đã duyệt). Duyệt / ẩn / xóa đánh giá ở Phase 9.

#### Chi tiết API đánh giá

```text
CoTheViet  { maDonHang, sanPhamId, sanPhamSlug?, tenSanPham, hinhAnh?, ngayDat }
DanhGia    { id, sanPhamId, soSao, noiDung?, trangThai: "cho_duyet"|"da_duyet"|"da_an", ngayTao }
DanhGiaCK  { id, hoTen, soSao, noiDung?, ngayTao }                        (công khai, chỉ đã duyệt)
```

| API | Gửi lên | Trả về | Lỗi riêng |
|---|---|---|---|
| `GET /danh-gia/co-the-viet` | – | 200 `CoTheViet[]`: dòng của đơn đã hoàn thành, sản phẩm còn tồn tại, chưa đánh giá; đơn mới trước | |
| `POST /danh-gia` | `{ maDonHang, sanPhamId, soSao (1–5), noiDung? (≤ 2000) }` | 201 `DanhGia` (`cho_duyet`, chưa hiện công khai) | 404 "Không tìm thấy đơn hàng" (kể cả đơn người khác) / "Sản phẩm không có trong đơn hàng"; 409 "Chỉ đánh giá được đơn đã hoàn thành" / "Bạn đã đánh giá sản phẩm này trong đơn hàng này" |
| `GET /san-pham/{slug}/danh-gia` | `trang`, `kichThuoc` (1–50, mặc định 10) | 200 trang `DanhGiaCK`, mới nhất trước | 404 "Không tìm thấy sản phẩm" |

### 3.7 Quản trị – Phase 9 (tất cả quyền QT)
| PT | Đường dẫn | Mô tả |
|---|---|---|
| POST | `/admin/upload?thuMuc=` | Tải ảnh (`san-pham` \| `banner` \| `thuong-hieu` \| `danh-muc`) |
| GET, POST · PUT, DELETE | `/admin/danh-muc` · `/admin/danh-muc/{id}` | Danh mục |
| GET, POST · PUT, DELETE | `/admin/thuong-hieu` · `/admin/thuong-hieu/{id}` | Thương hiệu |
| GET, POST · GET, PUT, DELETE | `/admin/san-pham` · `/admin/san-pham/{id}` | Sản phẩm (kèm thông số, tùy chọn, ảnh) |
| POST · PUT, DELETE | `/admin/san-pham/{id}/tuy-chon` · `/admin/tuy-chon/{id}` | Tùy chọn và giá trị |
| POST · PUT, DELETE | `/admin/san-pham/{id}/hinh-anh` · `/admin/hinh-anh/{id}` | Ảnh sản phẩm |
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

#### Quy ước chung của API quản trị
- Mọi `/api/admin/**`: chưa đăng nhập 401, không phải quản trị 403 "Bạn không có quyền thực hiện thao tác này".
- Xóa danh mục, thương hiệu, sản phẩm, biến thể, mã giảm giá là **xóa mềm** (`deleted_at`): đơn cũ vẫn tham chiếu được; slug/SKU/mã
  đã xóa được dùng lại. Danh sách quản trị gồm cả mục đang ẩn, không gồm mục đã xóa. Không tìm thấy (hoặc đã xóa) → 404.
- `slug` để trống thì tự sinh từ tên (bỏ dấu, `đ` → `d`, chữ thường, ký tự khác chữ số thành `-`; trùng thì thêm `-2`, `-3`…).
  Slug tự nhập phải dạng `a-z0-9-` và không trùng (409 "Slug đã được dùng").

#### Chi tiết API quản trị: danh mục, thương hiệu, sản phẩm (9.1)

| API | Gửi lên | Trả về | Lỗi riêng |
|---|---|---|---|
| `GET /admin/danh-muc` | – | `[{ id, ten, slug, moTa?, hinhAnh?, thuTu, hienThi, danhMucChaId?, soDanhMucCon, soSanPham }]` theo `thuTu` | |
| `POST /admin/danh-muc` · `PUT /admin/danh-muc/{id}` | `{ ten, slug?, moTa?, hinhAnh?, thuTu?, hienThi?, danhMucChaId? }` | 201 / 200 danh mục | 400 "Danh mục cha phải là danh mục cấp 1" (2 cấp; danh mục đang có con không thành con được; không chọn chính nó) |
| `DELETE /admin/danh-muc/{id}` | – | 204 | 409 "Danh mục còn danh mục con" / "Danh mục còn sản phẩm" |
| `GET /admin/thuong-hieu` | – | `[{ id, ten, slug, logo?, moTa?, hienThi, soSanPham }]` theo tên | |
| `POST` · `PUT /admin/thuong-hieu/{id}` | `{ ten, slug?, logo?, moTa?, hienThi? }` | 201 / 200 | |
| `DELETE /admin/thuong-hieu/{id}` | – | 204 | 409 "Thương hiệu còn sản phẩm" |
| `GET /admin/san-pham` | `tuKhoa?` (không dấu), `danhMucId?`, `thuongHieuId?`, `hienThi?`, `trang`, `kichThuoc` (1–50, mặc định 20) | trang `{ id, ten, slug, anhChinh?, tenDanhMuc, tenThuongHieu?, giaBan, tongTon, soBienThe, hienThi, noiBat, luotXem, ngayTao }`, mới nhất trước | |
| `GET /admin/san-pham/{id}` | – | `SanPhamQT` (dưới) | 404 |
| `POST /admin/san-pham` · `PUT /admin/san-pham/{id}` | `{ ten, slug?, danhMucId, thuongHieuId?, moTaNgan?, moTa? (HTML), baoHanhThang?, noiBat?, hienThi?, thongSos?: [{ ten, giaTri }] }` – `thongSos` thay toàn bộ | 201 / 200 `SanPhamQT` | 400 danh mục/thương hiệu không tồn tại |
| `DELETE /admin/san-pham/{id}` | – | 204 (xóa mềm sản phẩm và các biến thể) | |
| `POST /admin/san-pham/{id}/tuy-chon` | `{ ten, loai, giaTris: [{ giaTri, maMau? }] }` | 201 `SanPhamQT` | 409 "Tùy chọn đã tồn tại" / "Sản phẩm đã có biến thể, xóa biến thể trước khi thêm tùy chọn" (biến thể cũ sẽ thiếu giá trị) |
| `PUT /admin/tuy-chon/{id}` | `{ ten, loai, giaTris: [{ id?, giaTri, maMau? }] }` – có `id` là sửa, không có là thêm, thiếu là xóa | 200 `SanPhamQT` | 409 "Giá trị \"Đen\" đang được dùng ở biến thể" |
| `DELETE /admin/tuy-chon/{id}` | – | 200 `SanPhamQT` | 409 như trên |
| `POST /admin/san-pham/{id}/hinh-anh` · `PUT /admin/hinh-anh/{id}` | `{ duongDan, moTa?, giaTriTuyChonId?, laAnhChinh?, thuTu? }` (`duongDan` lấy từ `POST /admin/upload`) | 201 / 200 `SanPhamQT` | 400 giá trị tùy chọn không thuộc sản phẩm |
| `DELETE /admin/hinh-anh/{id}` | – | 200 `SanPhamQT` | |
| `POST /admin/san-pham/{id}/bien-the` | `{ sku, giaBan, giaGoc?, soLuongTon?, nguongCanhBao?, hienThi?, giaTriIds: [] }` – đúng một giá trị cho mỗi tùy chọn | 201 `SanPhamQT` | 400 "Mỗi tùy chọn phải chọn đúng một giá trị"; 409 "SKU đã được dùng" / "Đã có biến thể với tổ hợp này" |
| `PUT /admin/bien-the/{id}` | `{ sku, giaBan, giaGoc?, nguongCanhBao?, hienThi? }` (tồn kho đổi qua API kho) | 200 `SanPhamQT` | như trên |
| `DELETE /admin/bien-the/{id}` | – | 200 `SanPhamQT` (xóa mềm) | |

```text
SanPhamQT { id, ten, slug, danhMucId, thuongHieuId?, moTaNgan?, moTa?, baoHanhThang, noiBat, hienThi, giaBan, giaGoc?,
            thongSos [{ ten, giaTri }], tuyChons [{ id, ten, loai, giaTris [{ id, giaTri, maMau? }] }],
            hinhAnhs [{ id, duongDan, moTa?, giaTriTuyChonId?, laAnhChinh, thuTu }],
            bienThes [{ id, sku, giaBan, giaGoc?, soLuongTon, nguongCanhBao, hienThi, giaTriIds }] }   (biến thể chưa xóa, kể cả đang ẩn)
```
- `moTa` được lọc HTML khi lưu (jsoup, danh sách thẻ an toàn: đoạn, tiêu đề, danh sách, bảng, đậm/nghiêng, liên kết, ảnh http(s));
  `<script>`, thuộc tính `on*`, `javascript:` bị bỏ. Frontend hiện bằng `dangerouslySetInnerHTML` nên bắt buộc.
- `giaGoc` (nếu có) ≥ `giaBan`. Giá hiển thị của sản phẩm (`giaBan`, `giaGoc`) tự cập nhật = biến thể đang bán rẻ nhất
  sau mỗi lần thêm/sửa/xóa biến thể.
- Biến thể mới có `soLuongTon` > 0 thì ghi lịch sử kho loại `khoi_tao`. SKU lưu chữ hoa, không phân biệt hoa thường khi kiểm trùng.
- Thứ tự kiểm biến thể: giá (400) → SKU trùng (409) → tổ hợp giá trị (400/409).

#### Chi tiết API quản trị: kho (9.2)

| API | Gửi lên | Trả về | Lỗi riêng |
|---|---|---|---|
| `POST /admin/bien-the/{id}/nhap-kho` | `{ soLuong (1–100000), ghiChu? }` | 200 `{ bienTheId, soLuongTon }` + lịch sử `nhap_kho` | 404 |
| `POST /admin/bien-the/{id}/dieu-chinh` | `{ soLuongMoi (≥ 0), ghiChu }` (bắt buộc lý do) | 200 như trên + lịch sử `dieu_chinh` (chênh lệch) | 400 "Số lượng mới bằng tồn kho hiện tại" |
| `GET /admin/kho/lich-su` | `bienTheId?`, `loai?`, `trang`, `kichThuoc` (1–100, mặc định 20) | trang `{ id, bienTheId, sku, tenSanPham, loai, soLuongThayDoi, soLuongSau, ghiChu?, maDonHang?, nguoiThucHien?, thoiGian }`, mới nhất trước | |
| `GET /admin/kho/sap-het` | – | `[{ bienTheId, sanPhamId, tenSanPham, sku, phanLoai?, soLuongTon, nguongCanhBao }]` biến thể chưa xóa có tồn ≤ ngưỡng, ít hàng trước | |

Thay đổi tồn kho khóa dòng biến thể (cùng cơ chế với đặt hàng) nên nhập kho và đặt hàng cùng lúc không làm sai số.

## 4. Biến môi trường
| Biến | Mặc định (profile `dev`) | Ý nghĩa |
|---|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | `jdbc:postgresql://localhost:5433/techgear`, `techgear`, `techgear` | Kết nối CSDL (khớp `docker-compose.yml`) |
| `JWT_SECRET` | khóa dev (chỉ profile `dev`; thiếu ở môi trường khác thì không khởi động) | Khóa ký JWT, ≥ 32 byte |
| `FRONTEND_URL` | `http://localhost:5173` | Liên kết trong email |
| `CORS_ALLOWED_ORIGINS` | = `FRONTEND_URL` | Origin được phép, phân cách dấu phẩy |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` | Gmail SMTP, rỗng | Chưa có `MAIL_USERNAME` thì ghi liên kết ra log |
| `VNPAY_TMN_CODE`, `VNPAY_HASH_SECRET` | rỗng (dev: giá trị giả để test IPN bằng Postman) | Mã website và khóa bí mật VNPay sandbox (đăng ký tại sandbox.vnpayment.vn) |
| `VNPAY_PAY_URL` | `https://sandbox.vnpayment.vn/paymentv2/vpcpay.html` | Cổng thanh toán |
| `VNPAY_RETURN_URL` | `FRONTEND_URL/thanh-toan/ket-qua` | Trang frontend VNPay chuyển khách về |
| `UPLOAD_DIR` | `uploads` | Thư mục lưu ảnh |
