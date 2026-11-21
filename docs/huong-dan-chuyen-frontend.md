# Hướng dẫn chuyển frontend sang backend mới

Dành cho người làm frontend (nhánh `main`, cập nhật 21/11/2026). Backend mới (nhánh `hoanglong`) viết lại toàn bộ
`source/backend` trên PostgreSQL. Backend cũ trên `main` (gói `config/`, `controller/`, `dto/`, `service/`) sẽ bị thay thế
khi gộp nhánh, nên frontend cần gọi theo API mới.

- Đặc tả đầy đủ: `docs/thiet-ke/dac-ta-api.md`. Swagger khi chạy backend: <http://localhost:8080/swagger-ui.html>.
- Request và response mẫu cho từng API: bộ Postman `postman/TechGear.postman_collection.json`.
- Định dạng lỗi giữ nguyên: `{ thoiGian, trangThai, thongBao, loi? }`. Hàm `layThongBaoLoi` dùng được như cũ.
  Lỗi kiểm tra dữ liệu nằm trong `loi` theo tên ô, ví dụ `loi.soDienThoai`.
- Tiền là số nguyên VND (`long`), không còn `BigDecimal`. Thời gian có múi giờ (`2026-11-21T10:00:00+07:00`).

Frontend trên `main` gọi 57 API: 40 API đúng đường dẫn, 17 API sai đường dẫn. Ngoài ra có vài API đúng đường dẫn nhưng khác
trường JSON (xem mục 2). Dưới đây xếp theo file cần sửa, các thay đổi bắt buộc đặt lên đầu.

## 1. Bảng đổi đường dẫn (17 lời gọi)

| File | Đang gọi | Đổi thành | Ghi chú |
|---|---|---|---|
| `api/cartApi.js` | `POST /gio-hang/items` | `POST /gio-hang` | Body giữ nguyên `{ bienTheId, soLuong }` |
| | `PUT /gio-hang/items/{bienTheId}` | `PUT /gio-hang/{bienTheId}` | Body `{ soLuong }` |
| | `DELETE /gio-hang/items/{bienTheId}` | `DELETE /gio-hang/{bienTheId}` | |
| | `DELETE /gio-hang` | giữ nguyên | API mới thêm, trả giỏ rỗng |
| | `POST /gio-hang/gop` `{ items }` | `POST /gio-hang/gop` gửi **mảng** `[{ bienTheId, soLuong }]` | Đường dẫn đúng, body khác |
| `api/checkoutApi.js` | `POST /dat-hang/tinh-tien` | `POST /don-hang/tinh-thu` | Xem mục 3 |
| | `POST /dat-hang` | Đã đăng nhập: `POST /don-hang`. Khách vãng lai: `POST /don-hang/vang-lai` | Xem mục 3 |
| `api/customerApi.js` | `GET /don-hang-cua-toi` | `GET /don-hang?trang=&kichThuoc=` | Thêm được `trangThai=` để lọc |
| | `GET /don-hang-cua-toi/{ma}` | `GET /don-hang/{ma}` | |
| | `PATCH /don-hang-cua-toi/{ma}/huy` | `POST /don-hang/{ma}/huy` | Body `{ lyDo? }` giữ nguyên |
| | `GET /yeu-thich/{sanPhamId}` | bỏ, dùng danh sách `GET /yeu-thich` | Hiện chưa trang nào gọi |
| | `POST /yeu-thich/{sanPhamId}` | `PUT /yeu-thich/{sanPhamId}` | Trả **204 không có body** |
| `api/storeApi.js` | `POST /tra-cuu-don-hang` `{ maDonHang, lienHe }` | `POST /don-hang/tra-cuu` `{ maDonHang, email }` | Chỉ tra bằng email, không tra bằng SĐT |
| `admin/api/adminApi.js` | `GET /admin/ton-kho` | `GET /admin/kho?tuKhoa=&trangThai=&trang=&kichThuoc=` | API mới thêm |
| | `POST /admin/ton-kho/{id}/nhap-kho` | `POST /admin/bien-the/{id}/nhap-kho` | Body `{ soLuong, ghiChu? }` giữ nguyên |
| | `POST /admin/ton-kho/{id}/dieu-chinh` `{ soLuongThucTe, lyDo }` | `POST /admin/bien-the/{id}/dieu-chinh` `{ soLuongMoi, ghiChu }` | `ghiChu` bắt buộc (lý do) |
| | `GET /admin/ton-kho/{id}/lich-su?trang=` | `GET /admin/kho/lich-su?bienTheId={id}&trang=` | |
| | `PATCH /admin/san-pham/{id}/hien-thi` | giữ nguyên | API mới thêm, body `{ hienThi }` |

## 2. Khác trường JSON ở API đúng đường dẫn

| API | Frontend đang đọc / gửi | Backend mới |
|---|---|---|
| Mọi API giỏ hàng (trả `GioHang`) | `response.dong` | `response.dongs` (mỗi dòng: `bienTheId, sanPhamId, sanPhamSlug, ten, phanLoai?, anh?, sku, giaBan, giaGoc?, soLuong, tonKho, conBan`). Không có `id` dòng, dùng `bienTheId` làm key |
| `GET /san-pham/{slug}/danh-gia` | `{ diemTrungBinh, tongDanhGia, danhSach: trang }`, mỗi dòng `tenNguoiDung, ngayDanhGia, anhDaiDien` | Trả thẳng một trang `{ noiDung, trang, kichThuoc, tongSoPhanTu, tongSoTrang }`, mỗi dòng `{ id, hoTen, soSao, noiDung?, ngayTao }`. Điểm trung bình và số đánh giá lấy từ chi tiết sản phẩm: `diemTrungBinh`, `soDanhGia` |
| `POST /danh-gia` | `{ donHangId, sanPhamId, soSao, noiDung }` | `{ maDonHang, sanPhamId, soSao, noiDung? }`. Danh sách sản phẩm còn được đánh giá: `GET /danh-gia/co-the-viet` |
| `GET /admin/danh-muc` | Cây, đọc `goc.danhMucCons` | **Danh sách phẳng** `[{ id, ten, slug, moTa?, hinhAnh?, thuTu, hienThi, danhMucChaId?, soDanhMucCon, soSanPham }]`, frontend tự ghép cây theo `danhMucChaId` (chỉ 2 cấp) |
| `GET /admin/san-pham` | Lọc `trangThai=dang_ban\|dang_an\|het_hang`; đọc `thuongHieuTen`, `danhMucTen`, `giaGoc` | Lọc `hienThi=true\|false` (hết hàng: dùng `GET /admin/kho?trangThai=het_hang`); trường `tenThuongHieu`, `tenDanhMuc`; không có `giaGoc` |
| `GET /admin/thuong-hieu` | – | Giống cũ, có thêm `slug`, `soSanPham` |

## 3. Từng màn hình

### Giỏ hàng – `context/CartContext.jsx`
- Đổi `response.dong` thành `response.dongs` (2 chỗ).
- `cartApi.gop` gửi mảng: `axiosClient.post('/gio-hang/gop', items)`.
- Sau khi đặt hàng thành công, backend **tự xóa khỏi giỏ** các dòng đã mua. Gọi `xoaHet()` sau đó vẫn được, không gây lỗi.

### Thanh toán – `pages/CheckoutPage.jsx`
- Tính tiền: `POST /don-hang/tinh-thu` với `{ dongs: [{ bienTheId, soLuong }], maGiamGia?, email? }`.
  Response: `{ dongs, maGiamGia?, tamTinh, tienGiamGia, phiVanChuyen, tongTien }`, **không có `moTaGiamGia`**.
  Nên gửi `email` (đã đăng nhập thì gửi email tài khoản, vì API này không đọc token) để số tiền giảm khớp lúc đặt đơn với
  mã giảm giá giới hạn lượt dùng theo khách.
- Đặt hàng: đổi `items` thành `dongs`. Bỏ `diaChiGiaoHang` ghép chuỗi, gửi riêng 3 ô `tinhThanh`, `phuongXa`,
  `diaChiChiTiet` (form đã có sẵn). `phuongThucThanhToan`: `cod` hoặc `vnpay`.
  - Đã đăng nhập: `POST /don-hang`, `email` không bắt buộc.
  - Chưa đăng nhập: `POST /don-hang/vang-lai`, **bắt buộc `email`**. Mỗi IP đặt tối đa 10 đơn trong 15 phút (429).
- Response 201 là chi tiết đơn (giống `GET /don-hang/{ma}`), có `maDonHang` và `tongTien`, nên phần hiện kết quả giữ nguyên.
- VNPay: response **không còn `thanhToanUrl`**. Sau khi đặt đơn `vnpay`, gọi `POST /thanh-toan/vnpay/{maDonHang}`
  (không cần body) để lấy `{ duongDan }`, rồi `window.location = duongDan`. Thanh toán xong, VNPay chuyển khách về trang
  `/thanh-toan/ket-qua` của frontend (cần tạo trang này). Trang đó gửi nguyên query string sang
  `GET /thanh-toan/vnpay/ket-qua?...` để nhận `{ hopLe, thanhCong, maDonHang, trangThaiThanhToan, thongBao }`.
  Trạng thái đơn được cập nhật qua IPN, không phụ thuộc trang này. Đơn VNPay tổng 0đ bị từ chối (400), cần chọn COD.

### Đơn của tôi – `pages/OrdersPage.jsx`, `pages/OrderDetailPage.jsx`, `components/OrderView.jsx`
- Danh sách (`GET /don-hang`) trả bản tóm tắt: `{ maDonHang, trangThai, trangThaiThanhToan, phuongThucThanhToan, tongTien,
  soLuongSanPham, tenSanPhamDau, hinhAnhDau?, ngayDat }`. **Không có `id`**, dùng `maDonHang` làm key.
- Chi tiết (`GET /don-hang/{ma}`): đổi `chiTiets` thành `dongs`, `lichSus` thành `lichSu`. Mỗi dòng không có `id`, dùng
  `bienTheId` làm key. Có thêm `coTheHuy` (dùng để ẩn hoặc hiện nút hủy), `maGiamGia?`, `email?`.
- Hủy: `POST /don-hang/{ma}/huy`, response là chi tiết đơn mới.
- Đánh giá trong `OrderDetailPage`: gửi `maDonHang` thay cho `donHangId` (`donHang.id` không còn).

### Tra cứu đơn – `pages/OrderLookupPage.jsx`
- Ô "Email hoặc số điện thoại" đổi thành "Email", gửi `{ maDonHang, email }`. Response là chi tiết đơn như trên, dùng chung
  `OrderView`. Mỗi IP tra sai tối đa 10 lần trong 15 phút, quá thì 429.

### Yêu thích – `context/WishlistContext.jsx`
- Thêm: `PUT /yeu-thich/{id}`. Bỏ: `DELETE /yeu-thich/{id}`. Cả hai trả **204 không có body**, nên `res.yeuThich` luôn
  `undefined` và sản phẩm sẽ bị gỡ khỏi danh sách. Sửa thành: trạng thái mới = `!dangThich`.
- `GET /yeu-thich` trả mảng sản phẩm tóm tắt (`id, ten, slug, giaBan, giaGoc?, anhChinh?, tenThuongHieu?, conHang`), giữ nguyên.

### Chi tiết sản phẩm – `pages/ProductDetailPage.jsx`
- Đánh giá: xem mục 2. Phân trang dùng `noiDung` và `tongSoTrang` như danh sách sản phẩm.

### Quản trị danh mục – `admin/pages/DanhMucPage.jsx`, `SanPhamListPage.jsx`
- `layCay()` giờ trả danh sách phẳng, cần ghép cây:
  `goc = ds.filter(d => !d.danhMucChaId).map(g => ({ ...g, danhMucCons: ds.filter(c => c.danhMucChaId === g.id) }))`.
- Gửi lên giữ nguyên (`ten, moTa?, hinhAnh?, thuTu?, hienThi?, danhMucChaId?`), có thể gửi thêm `slug`.

### Quản trị sản phẩm – `admin/pages/SanPhamListPage.jsx`
- Lọc: thay `trangThai` bằng `hienThi` (`true`, `false`, bỏ trống = tất cả). Đọc `tenThuongHieu` thay cho `thuongHieuTen`.
- Đổi nhanh hiển thị: `PATCH /admin/san-pham/{id}/hien-thi` `{ hienThi }` giữ nguyên.

### Quản trị sản phẩm – `admin/pages/SanPhamFormPage.jsx`, `components/VariantEditor.jsx`, `components/ProductImages.jsx`
Đây là phần phải sửa nhiều nhất. Backend cũ lưu một lần cả sản phẩm, ảnh, tùy chọn và biến thể. Backend mới tách
thành các API con, và **mỗi API con trả lại toàn bộ sản phẩm** (`SanPhamQT`) để cập nhật form.

```text
SanPhamQT { id, ten, slug, danhMucId, thuongHieuId?, moTaNgan?, moTa?, baoHanhThang, noiBat, hienThi, giaBan, giaGoc?,
            thongSos [{ ten, giaTri }], tuyChons [{ id, ten, loai, giaTris [{ id, giaTri, maMau? }] }],
            hinhAnhs [{ id, duongDan, moTa?, giaTriTuyChonId?, laAnhChinh, thuTu }],
            bienThes [{ id, sku, giaBan, giaGoc?, soLuongTon, nguongCanhBao, hienThi, giaTriIds }] }
```

Khác dữ liệu so với bản cũ:
- Biến thể dùng `giaTriIds` (id giá trị tùy chọn) thay cho `giaTris` (tên). `dangBan` đổi thành `hienThi`.
  `tonKhoBanDau` đổi thành `soLuongTon` (chỉ gửi khi tạo).
- Ảnh dùng `giaTriTuyChonId` thay cho cặp `tuyChon`, `giaTri`. Có thêm `thuTu`.
- Sản phẩm không nhận `giaBan`, `giaGoc`: giá hiển thị tự lấy theo biến thể đang bán rẻ nhất.

Trình tự lưu gợi ý:
1. Tạo: `POST /admin/san-pham` `{ ten, slug?, danhMucId, thuongHieuId?, moTaNgan?, moTa?, baoHanhThang?, noiBat?, hienThi?,
   thongSos? }`. Nhận `id`. Sửa: `PUT /admin/san-pham/{id}` (body như trên, `thongSos` thay toàn bộ).
2. Tùy chọn: thêm bằng `POST /admin/san-pham/{id}/tuy-chon` `{ ten, loai, giaTris: [{ giaTri, maMau? }] }`. Sửa bằng
   `PUT /admin/tuy-chon/{tcId}`: giá trị có `id` thì sửa, không có `id` thì thêm, thiếu thì xóa. Xóa bằng
   `DELETE /admin/tuy-chon/{tcId}`. Phải thêm hết tùy chọn **trước** khi tạo biến thể. Khi đã có biến thể thì không thêm
   tùy chọn mới được (409).
3. Biến thể: lấy id giá trị từ `SanPhamQT.tuyChons` vừa trả về. Thêm bằng `POST /admin/san-pham/{id}/bien-the`
   `{ sku, giaBan, giaGoc?, soLuongTon?, nguongCanhBao?, hienThi?, giaTriIds }`. Sửa bằng `PUT /admin/bien-the/{btId}`
   (không đổi tồn kho, tồn kho đổi qua API kho). Xóa bằng `DELETE /admin/bien-the/{btId}`.
4. Ảnh: `POST /admin/upload?thuMuc=san-pham` để lấy `duongDan`, rồi `POST /admin/san-pham/{id}/hinh-anh`
   `{ duongDan, moTa?, giaTriTuyChonId?, laAnhChinh?, thuTu? }`. Sửa ảnh: `PUT /admin/hinh-anh/{anhId}`.
   Xóa ảnh: `DELETE /admin/hinh-anh/{anhId}`.
5. Lỗi từng bước trả về `thongBao` bằng tiếng Việt, ví dụ "SKU đã được dùng" hay "Đã có biến thể với tổ hợp này". Các bước
   không nằm chung một giao dịch: nếu một bước lỗi thì các bước trước đã được lưu. Nên mở lại form theo `SanPhamQT` mới nhất.

Khi tạo sản phẩm mới, cách đơn giản là tách làm hai màn: màn 1 lưu thông tin chung, màn 2 quản lý tùy chọn, biến thể
và ảnh, mỗi thao tác gọi ngay API con tương ứng.

### Quản trị tồn kho – `admin/pages/TonKhoPage.jsx`, `components/StockDialog.jsx`
- Danh sách: `GET /admin/kho`. Mỗi dòng: `{ bienTheId, sanPhamId, tenSanPham, sku, phanLoai?, soLuongTon, nguongCanhBao,
  trangThai, hienThi }`. `trangThai` vẫn là `con_hang`, `sap_het` hoặc `het_hang`. `dangBan` đổi thành `hienThi`.
  `tuKhoa` tìm theo tên sản phẩm (không dấu) hoặc SKU.
- Nhập kho và điều chỉnh trả `{ bienTheId, soLuongTon }`, không còn trả cả dòng tồn kho. Nên tải lại danh sách sau khi lưu.
- Điều chỉnh: gửi `{ soLuongMoi, ghiChu }` (lý do bắt buộc). Nếu số mới bằng số hiện tại thì trả 400.
- Lịch sử: `GET /admin/kho/lich-su?bienTheId=&trang=`. Các trường `thoiGian, loai, soLuongThayDoi, soLuongSau, ghiChu?,
  nguoiThucHien?, maDonHang?` giống bản cũ. Có thêm `loai` lọc theo `khoi_tao`, `nhap_kho`, `ban_hang`, `huy_don`
  hoặc `dieu_chinh`.

## 4. Không cần sửa

Các API sau đúng đường dẫn và đúng trường mà các trang trên `main` đang đọc:
- **Đăng nhập và tài khoản:** đăng nhập, đăng ký (`soDienThoai` không bắt buộc), quên và đặt lại mật khẩu, `/auth/toi`, hồ sơ,
  ảnh đại diện, đổi mật khẩu, địa chỉ (`diaChiDayDu` không có, trang đã tự ghép).
- **Sản phẩm:** danh mục, thương hiệu, danh sách, nổi bật, chi tiết sản phẩm.
- **Cửa hàng:** banner, cài đặt (các khóa `ten_cua_hang`, `hotline`, `email_lien_he`, `phi_van_chuyen`,
  `mien_phi_van_chuyen_tu`).
- **Quản trị:** thương hiệu, tải ảnh lên.

Backend mới còn có các API mà frontend chưa dùng: quản trị đơn hàng, mã giảm giá, banner, cài đặt, người dùng, duyệt đánh
giá, thống kê. Xem `docs/bao-cao-cho-frontend.md` và đặc tả.
