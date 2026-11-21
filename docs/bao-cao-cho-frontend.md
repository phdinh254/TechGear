# Báo cáo backend cho người làm frontend

Người viết: Trần Hoàng Long (backend + CSDL) · Cập nhật: 21/11/2026 (Phase 10) · Nhánh: `hoanglong`

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
  Dữ liệu mẫu đầy đủ (sản phẩm có ảnh, đơn hàng, đánh giá...): xem mục 11, cần xóa CSDL dev cũ một lần.
- CORS chỉ cho phép `http://localhost:5173`. Vite tự đổi sang cổng khác khi 5173 bận (VD 5174) thì mọi lệnh gọi bị chặn.
  Gặp vậy thì tắt tiến trình đang chiếm 5173, hoặc chạy backend với `FRONTEND_URL=http://localhost:5174`.
- Chưa cấu hình email thì (profile dev) liên kết đặt lại mật khẩu được in ra log của backend (dòng `Chưa cấu hình MAIL_USERNAME …`).

## 2. Tình trạng

| Phần | Backend | Frontend dùng được chưa |
|---|---|---|
| Tài khoản: đăng ký, đăng nhập, `/auth/toi` | Xong (Phase 3) | Được, giữ nguyên hợp đồng cũ |
| Hồ sơ, đổi mật khẩu, ảnh đại diện, quên/đặt lại mật khẩu, sổ địa chỉ | Xong (Phase 3) | API mới, frontend chưa có giao diện |
| Danh mục, thương hiệu, tìm kiếm, nổi bật, chi tiết sản phẩm | Xong (Phase 4) | Được, giữ nguyên hợp đồng cũ: trang chủ, danh sách, chi tiết chạy lại |
| Banner, cấu hình cửa hàng | Xong (Phase 4) | API mới, frontend chưa có giao diện |
| Giỏ hàng trên server, gộp giỏ khi đăng nhập, yêu thích | Xong (Phase 5) | API mới, frontend chưa dùng (giỏ hiện lưu `localStorage`, vẫn chạy bình thường) |
| Tính thử, đặt hàng (đăng nhập + vãng lai), mã giảm giá, đơn của tôi, hủy, tra cứu | Xong (Phase 6) | API mới; nút "Đặt hàng" ở trang giỏ đang tắt, có thể nối ngay |
| Thanh toán VNPay (sandbox) | Xong (Phase 7) | API mới; frontend cần thêm route `/thanh-toan/ket-qua` |
| Đánh giá: viết, xem đánh giá đã duyệt, điểm trung bình ở chi tiết sản phẩm | Xong (Phase 8) | API mới; trường mới ở chi tiết sản phẩm không làm hỏng trang hiện tại |
| Trang quản trị: danh mục, thương hiệu, sản phẩm, kho, đơn hàng, mã giảm giá, banner, cài đặt, người dùng, đánh giá, thống kê | Xong (Phase 9) | API mới; frontend chưa có trang quản trị (`laQuanTri` trong `AuthContext` đã có sẵn để ẩn/hiện menu) |
| Dữ liệu mẫu đầy đủ: ảnh, banner, khách, đơn đủ trạng thái, đánh giá | Xong (Phase 10) | Có ngay khi chạy backend trên CSDL trống (mục 11) |

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

## 5. Phase 4 – Danh mục, thương hiệu, sản phẩm, cửa hàng

Sơ đồ luồng: [tìm kiếm, lọc](thiet-ke/08_hd_tim_kiem_san_pham.drawio.png), [xem chi tiết, chọn biến thể](thiet-ke/09_hd_xem_chi_tiet_san_pham.drawio.png).
Hình dạng dữ liệu: mục "Chi tiết API sản phẩm, cửa hàng" trong [`dac-ta-api.md`](thiet-ke/dac-ta-api.md).
Dữ liệu mẫu dev có 13 danh mục (4 cha, 9 con), 9 thương hiệu, 17 sản phẩm có màu sắc/phiên bản, 8 sản phẩm nổi bật.
**Chưa có ảnh** (thẻ sản phẩm hiện "Chưa có ảnh") và chưa có banner; ảnh mẫu bổ sung ở Phase 10.

### 5.1 Frontend hiện tại chạy được, không phải sửa

Đã gọi thử đúng các request mà `HomePage`, `ProductListPage`, `ProductDetailPage` gửi (có header `Origin` của Vite và token
cũ trong `localStorage`): trả đủ các trường frontend đọc, CORS cho phép, token cũ không làm lỗi. Mình chưa mở giao diện
trên trình duyệt (trình duyệt tự động chưa kết nối), nhờ bạn mở thử một lượt.

### 5.2 Hành vi cần biết

| Chủ đề | Backend làm gì |
|---|---|
| Từ khóa | Không phân biệt hoa thường và dấu ("sac" ra "Sạc"). Nhiều từ thì sản phẩm phải chứa đủ các từ. Tìm trong tên sản phẩm **và** tên thương hiệu ("anker sac") |
| Danh mục | `danhMuc=<slug cha>` gồm cả sản phẩm của danh mục con |
| Thương hiệu | Lọc được nhiều thương hiệu: `thuongHieu=anker,ugreen`. Ô chọn một thương hiệu hiện tại vẫn đúng |
| Khoảng giá | So với giá **từng biến thể** đang bán. Sản phẩm 200k–800k khớp khoảng 700k–900k nhưng thẻ vẫn hiện giá thấp nhất 200k |
| Sắp xếp | `moi_nhat` (mặc định), `gia_tang`, `gia_giam`, `pho_bien` (lượt xem). Bằng nhau thì theo id nên hai trang liền nhau không trùng |
| Phân trang | `kichThuoc` 1–48 (frontend đang gửi 12). Tham số sai (VD `kichThuoc=100`, `giaTu=abc`, `giaTu > giaDen`) trả 400 kèm `thongBao`, trang danh sách sẽ hiện thông báo đó |
| Sản phẩm bị ẩn | Sản phẩm tắt, đã xóa, nằm trong danh mục bị ẩn, hoặc không còn biến thể đang bán: không có trong danh sách, chi tiết trả 404 "Không tìm thấy sản phẩm" |
| `conHang` | Có ít nhất một biến thể đang bán còn tồn kho. Trang chi tiết cũng có `conHang` |
| Lượt xem | Mỗi lần gọi chi tiết tăng 1 (dùng cho "Phổ biến" và nổi bật). React StrictMode ở chế độ dev gọi `useEffect` 2 lần nên mỗi lần mở trang tăng 2, bản build không bị |

### 5.3 Trường mới có thể dùng thêm (không bắt buộc)

| Trường | Dùng để |
|---|---|
| `hinhAnhs[].giaTriTuyChonId` | Ảnh gắn với một màu: khi khách chọn màu, chuyển ảnh đang xem sang ảnh có `giaTriTuyChonId` bằng id màu đó |
| `hinhAnhs[].laAnhChinh` | Ảnh chính (luôn đứng đầu mảng) |
| `tuyChons[].loai` | `mau_sac` \| `dung_luong` \| `phien_ban` \| `khac` – VD chỉ hiện ô màu khi `mau_sac` |
| `thuongHieu.slug`, `danhMuc.id` trong chi tiết | Tạo liên kết "Xem thêm sản phẩm của thương hiệu" |
| `GET /banner` | `[{ id, tieuDe?, hinhAnh, lienKet? }]` đã lọc theo khung giờ, cho slider trang chủ (ảnh ghép bằng `duongDanAnh`) |
| `GET /cai-dat` | `{ ten_cua_hang, hotline, email_lien_he, phi_van_chuyen, mien_phi_van_chuyen_tu }` (chuỗi) cho footer và trang giỏ hàng |

### 5.4 Chưa có ở Phase 4

- Điểm đánh giá (`diemTrungBinh`, `soDanhGia`) và danh sách đánh giá: chuyển sang Phase 8 (cần bảng đơn hàng trước).
- `moTa` là HTML, frontend đang hiện bằng `dangerouslySetInnerHTML`. Dữ liệu mẫu an toàn; backend sẽ lọc HTML khi quản trị
  lưu mô tả ở Phase 9.
- Giỏ hàng `localStorage` lưu sẵn `giaBan`, `tonKho` lúc bấm thêm, nên có thể cũ khi giá/tồn kho đổi. Phase 5 có giỏ hàng
  trên server trả giá và tồn kho hiện tại.

## 6. Phase 5 – Giỏ hàng và yêu thích

Sơ đồ luồng: [thêm vào giỏ](thiet-ke/10_hd_them_vao_gio.drawio.png), [gộp giỏ khi đăng nhập](thiet-ke/11_hd_gop_gio_hang.drawio.png).
Hình dạng dữ liệu, mã lỗi: mục "Chi tiết API giỏ hàng, yêu thích" trong [`dac-ta-api.md`](thiet-ke/dac-ta-api.md).
Tất cả cần đăng nhập. Giỏ hiện tại trong `localStorage` vẫn chạy như cũ; dùng giỏ server hay không là do bạn quyết định.

### 6.1 Gợi ý cách nối vào `CartContext`

| Lúc | Gọi | Ghi chú |
|---|---|---|
| Chưa đăng nhập | Không gọi | Giữ giỏ `localStorage` như hiện tại |
| Vừa đăng nhập / đăng ký | `POST /gio-hang/gop` với `[{ bienTheId, soLuong }]` lấy từ giỏ `localStorage` | Trả về cả giỏ đã gộp: đặt thẳng vào state rồi xóa `techgear_cart`. Không báo lỗi từng dòng: dòng ngừng bán hoặc hết hàng bị bỏ, số lượng bị cắt theo tồn kho |
| Tải lại trang khi đã đăng nhập | `GET /gio-hang` | |
| Thêm / sửa / xóa khi đã đăng nhập | `POST /gio-hang`, `PUT /gio-hang/{bienTheId}`, `DELETE /gio-hang/{bienTheId}` | Mỗi API trả về **cả giỏ**, không phải gọi lại `GET` |
| Đăng xuất | Không gọi | Giỏ vẫn nằm trên server, đăng nhập lại là thấy |

Dòng giỏ server dùng **cùng tên trường** với dòng giỏ `localStorage`: `bienTheId, sanPhamSlug, ten, phanLoai, giaBan, anh,
soLuong, tonKho`. Có thêm `sanPhamId, sku, giaGoc, conBan`. Giỏ có sẵn `tongSoLuong` và `tamTinh`, không phải tự cộng.
Khác giỏ `localStorage`: `giaBan`, `tonKho` luôn là giá trị hiện tại, nên giá trong giỏ không bị cũ.

### 6.2 Hành vi cần biết

| Tình huống | Backend trả |
|---|---|
| Thêm vượt tồn kho (tính cả số đã có trong giỏ) | 409 "Chỉ còn N sản phẩm trong kho"; tồn kho 0 thì 409 "Sản phẩm đã hết hàng" |
| Phiên bản đã ngừng bán | Thêm/sửa: 404 "Phiên bản sản phẩm không còn bán". Dòng đã có trong giỏ vẫn hiện với `conBan: false` (nên làm mờ, kèm nút xóa) và không tính vào `tamTinh` |
| Giỏ đủ 50 dòng, thêm phiên bản mới | 400 "Giỏ hàng tối đa 50 sản phẩm" |
| Số lượng ngoài 1–999 | 400, `loi.soLuong` = "Số lượng từ 1 đến 999" |
| Thứ tự dòng | Dòng thêm sau đứng trước; sửa số lượng không đổi thứ tự |

Tồn kho trong giỏ chỉ để báo sớm. Đặt hàng (Phase 6) mới là chỗ kiểm tra và trừ kho thật.

### 6.3 Yêu thích

- `PUT /yeu-thich/{sanPhamId}` thêm, `DELETE /yeu-thich/{sanPhamId}` bỏ; cả hai trả 204, gọi lặp lại không lỗi
  (nút trái tim bấm nhanh hai lần không sao). `sanPhamId` là `id` trong chi tiết sản phẩm hoặc thẻ sản phẩm.
- `GET /yeu-thich` trả mảng thẻ sản phẩm giống `GET /san-pham/noi-bat`, dùng lại được `ProductCard`. Sản phẩm đã bị ẩn tự
  biến mất khỏi danh sách.
- Tối đa 100 sản phẩm (400). Sản phẩm không tồn tại hoặc đang ẩn: 404 "Không tìm thấy sản phẩm".
- Chưa có API "sản phẩm này đã được yêu thích chưa". Muốn tô màu trái tim ở trang chi tiết thì lấy `GET /yeu-thich` một lần
  rồi so `id`. Cần API riêng thì báo mình.


## 7. Phase 6 – Đặt hàng và đơn hàng

Sơ đồ: [đặt hàng](thiet-ke/12_hd_dat_hang.drawio.png), [hủy đơn](thiet-ke/13_hd_huy_don.drawio.png), [tuần tự đặt hàng](thiet-ke/14_td_dat_hang.drawio.png), [trạng thái đơn](thiet-ke/15_tt_don_hang.drawio.png).
Hình dạng dữ liệu, cách tính tiền, thông báo lỗi mã giảm giá: mục "Chi tiết API đặt hàng, đơn hàng" trong
[`dac-ta-api.md`](thiet-ke/dac-ta-api.md). Mã giảm giá mẫu (dev): `GIAM10` (10%, tối đa 100.000đ, đơn từ 200.000đ),
`GIAM50K` (50.000đ, đơn từ 500.000đ), `MOILAN1` (20.000đ, mỗi khách 1 lần), `HETHAN` (đã hết hạn).

### 7.1 Trang thanh toán gợi ý

| Bước | Gọi | Ghi chú |
|---|---|---|
| Mở trang / đổi số lượng / nhập mã | `POST /don-hang/tinh-thu` `{ dongs: [{ bienTheId, soLuong }], maGiamGia?, email? }` | Không cần đăng nhập, không giữ hàng. Hiện `tamTinh`, `tienGiamGia`, `phiVanChuyen`, `tongTien` đúng như server sẽ tính. Mã sai trả 400 kèm `thongBao` (hiện ngay dưới ô mã) |
| Điền người nhận | `GET /tai-khoan/dia-chi` (đã đăng nhập) | Chọn địa chỉ thì điền sẵn các ô; API đặt hàng nhận thẳng các ô, không nhận id địa chỉ |
| Bấm Đặt hàng, **đã đăng nhập** | `POST /don-hang` | Email lấy từ tài khoản, không cần gửi |
| Bấm Đặt hàng, **chưa đăng nhập** | `POST /don-hang/vang-lai` | Bắt buộc `email` (để tra cứu đơn). Xong thì xóa giỏ `localStorage` |
| Sau khi đặt | Trả 201 + cả đơn | Hiện `maDonHang`. Khách đăng nhập: các biến thể vừa đặt đã tự bị xóa khỏi giỏ server, gọi lại `GET /gio-hang` |

Thân đặt hàng: `{ dongs, tenNguoiNhan, soDienThoai, tinhThanh, phuongXa, diaChiChiTiet, email?, maGiamGia?,
phuongThucThanhToan: "cod" | "vnpay", ghiChu? }`. Lỗi từng ô trả trong `loi` như các form khác.

### 7.2 Hành vi cần biết

| Tình huống | Backend trả |
|---|---|
| Giữa lúc tính thử và lúc đặt, giá đổi | Đơn dùng giá **lúc đặt**; tổng trong đơn trả về là con số cuối cùng (nên hiện lại cho khách) |
| Không đủ hàng | 409 "Sản phẩm Loa JBL Flip 6 (Đỏ) chỉ còn 2 trong kho" hoặc "... đã hết hàng"; không trừ kho dòng nào |
| Phiên bản ngừng bán | 404 "Phiên bản sản phẩm không còn bán" |
| Hai người cùng mua món cuối cùng | Đúng một người thành công, người kia nhận 409 hết hàng (đã có test tự động) |
| Khách vãng lai đặt quá 10 đơn / 15 phút cùng một IP | 429 |
| Đơn chọn `vnpay` | Đặt được, `trangThaiThanhToan` = `chua_thanh_toan`. Trang chuyển sang VNPay và tự hủy sau 30 phút làm ở Phase 7 |

### 7.3 Đơn của tôi, hủy, tra cứu

- `GET /don-hang?trangThai=&trang=&kichThuoc=` (mặc định 10/trang, mới nhất trước): mỗi đơn có `tenSanPhamDau`,
  `hinhAnhDau`, `soLuongSanPham` để làm thẻ đơn mà không phải gọi chi tiết.
- `GET /don-hang/{maDonHang}`: chi tiết + `lichSu` (dòng thời gian trạng thái). Đơn của người khác trả 404.
- `coTheHuy` = true thì hiện nút Hủy → `POST /don-hang/{maDonHang}/huy` `{ lyDo? }`. Chỉ hủy được khi đang chờ xác nhận
  (409 nếu không). Hủy xong hàng và lượt mã giảm giá được trả lại.
- Khách vãng lai: trang "Tra cứu đơn hàng" gọi `POST /don-hang/tra-cuu` `{ maDonHang, email }` (không phân biệt hoa thường).
  Sai 404; sai quá 10 lần / 15 phút mỗi IP thì 429. Khách vãng lai không tự hủy được (liên hệ cửa hàng).
- Ngày giờ (`ngayDat`, `lichSu[].thoiGian`) dạng ISO-8601 có `+07:00`.
- Trạng thái nên hiển thị: `cho_xac_nhan` Chờ xác nhận, `da_xac_nhan` Đã xác nhận, `dang_giao` Đang giao,
  `hoan_thanh` Hoàn thành, `da_huy` Đã hủy; thanh toán: `chua_thanh_toan`, `da_thanh_toan`, `da_hoan_tien`.

## 8. Phase 7 – Thanh toán VNPay

Sơ đồ: [tuần tự thanh toán](thiet-ke/16_td_thanh_toan_vnpay.drawio.png), [trạng thái thanh toán](thiet-ke/17_tt_thanh_toan.drawio.png).
Chi tiết: mục "Chi tiết thanh toán VNPay" trong [`dac-ta-api.md`](thiet-ke/dac-ta-api.md) (làm theo tài liệu VNPay 2.1.0).

### 8.1 Việc của frontend

| Bước | Làm gì |
|---|---|
| Sau khi đặt đơn `vnpay` (201), hoặc ở trang chi tiết đơn khi `phuongThucThanhToan = vnpay`, `trangThaiThanhToan = chua_thanh_toan`, `trangThai = cho_xac_nhan` | Gọi `POST /thanh-toan/vnpay/{maDonHang}` (không cần token) → nhận `{ duongDan }` → `window.location.href = duongDan` |
| Thêm route **`/thanh-toan/ket-qua`** (VNPay chuyển khách về đây kèm `?vnp_...`) | Gọi `GET /thanh-toan/vnpay/ket-qua` + **nguyên** `window.location.search`, hiện `thongBao`. `hopLe = false`: dữ liệu bị sửa, báo lỗi |
| `thanhCong = true` mà `trangThaiThanhToan` vẫn `chua_thanh_toan` | IPN của VNPay chưa tới (thường vài giây): hiện "Đang xác nhận thanh toán", gọi lại sau 2–3 giây hoặc mở chi tiết đơn |
| Thanh toán thất bại / khách hủy trên VNPay | Gọi lại `POST /thanh-toan/vnpay/{maDonHang}` để có liên kết mới (mỗi lần một mã giao dịch mới) |

### 8.2 Hành vi cần biết

- Liên kết thanh toán hết hạn 30 phút sau lúc đặt đơn. Quá 35 phút chưa trả, hệ thống **tự hủy đơn** (hoàn kho, trả lượt mã)
  và lịch sử đơn ghi "Quá 30 phút chưa thanh toán VNPay, hệ thống tự hủy". Nên hiện đồng hồ đếm ngược 30 phút.
- 409 khi tạo liên kết: "Đơn hàng không thanh toán qua VNPay", "Đơn hàng đã được thanh toán", "Đơn hàng không còn chờ thanh toán",
  "Đơn hàng đã quá hạn thanh toán". 503 "Chưa cấu hình VNPay" khi backend chưa có mã website VNPay.
- Chỉ IPN (VNPay gọi thẳng backend) mới đổi trạng thái thanh toán; trang kết quả chỉ đọc. Vì vậy sửa query string trên trình
  duyệt không làm đơn thành "đã thanh toán".

### 8.3 Thử trên máy

- Dev mặc định dùng mã website và khóa **giả**: trang VNPay sẽ báo lỗi. Muốn thanh toán thật trên sandbox: đăng ký tài khoản
  tại sandbox.vnpayment.vn, chạy backend với `VNPAY_TMN_CODE`, `VNPAY_HASH_SECRET` được cấp (thẻ test do VNPay cung cấp trong
  email đăng ký).
- IPN cần địa chỉ HTTPS công khai (khai báo trong cổng quản lý của VNPay), nên trên máy cá nhân VNPay không gọi được IPN về
  `localhost`. Để thử trọn luồng không cần VNPay: thư mục Postman "05 Thanh toán" giả lập IPN đã ký bằng khóa dev.

## 9. Phase 8 – Đánh giá sản phẩm

Sơ đồ: [viết đánh giá](thiet-ke/18_hd_viet_danh_gia.drawio.png).
Hình dạng dữ liệu: mục "Chi tiết API đánh giá" trong [`dac-ta-api.md`](thiet-ke/dac-ta-api.md).

| Màn hình gợi ý | API | Ghi chú |
|---|---|---|
| Trang chi tiết sản phẩm: dòng sao | `GET /san-pham/{slug}` có thêm `soDanhGia`, `diemTrungBinh` | `diemTrungBinh` làm tròn 1 chữ số, **không có** trong JSON khi chưa có đánh giá đã duyệt (hiện "Chưa có đánh giá") |
| Trang chi tiết: danh sách đánh giá | `GET /san-pham/{slug}/danh-gia?trang=&kichThuoc=` | Chỉ đánh giá đã duyệt, mới nhất trước, mặc định 10/trang. Mỗi đánh giá: `hoTen`, `soSao`, `noiDung?`, `ngayTao` |
| Tài khoản: "Đánh giá sản phẩm đã mua" | `GET /danh-gia/co-the-viet` | Các sản phẩm trong đơn **đã hoàn thành** chưa đánh giá: `maDonHang`, `sanPhamId`, `tenSanPham`, `hinhAnh?`, `sanPhamSlug` |
| Form đánh giá | `POST /danh-gia` `{ maDonHang, sanPhamId, soSao, noiDung? }` | 201: đánh giá ở trạng thái `cho_duyet`, chưa hiện công khai cho tới khi quản trị duyệt (Phase 9) – nên báo "Cảm ơn, đánh giá đang chờ duyệt" |

Lỗi: 404 "Không tìm thấy đơn hàng" / "Sản phẩm không có trong đơn hàng"; 409 "Chỉ đánh giá được đơn đã hoàn thành" /
"Bạn đã đánh giá sản phẩm này trong đơn hàng này"; 400 `loi.soSao` "Số sao từ 1 đến 5", `loi.noiDung` (tối đa 2000 ký tự).
`noiDung` là chữ thuần: hiển thị như văn bản (React tự thoát ký tự), không dùng `dangerouslySetInnerHTML`.

Hiện chưa có đơn nào "hoàn thành" trong dữ liệu mẫu (chỉ quản trị chuyển được, Phase 9), nên `co-the-viet` đang trả rỗng.

## 10. Phase 9 – Trang quản trị

Chi tiết từng API: các mục "Chi tiết API quản trị" (9.1 → 9.5) trong [`dac-ta-api.md`](thiet-ke/dac-ta-api.md).
Đăng nhập bằng `admin@techgear.vn` / `Admin@123` (dev). Mọi `/api/admin/**`: chưa đăng nhập 401, khách hàng 403.

### 10.1 Các trang gợi ý

| Trang | API chính | Ghi chú cho giao diện |
|---|---|---|
| Danh mục | `GET/POST/PUT/DELETE /admin/danh-muc` | Danh sách phẳng có `danhMucChaId` (tự dựng cây); chỉ 2 cấp. Xóa bị chặn (409) khi còn danh mục con hoặc sản phẩm: hiện `thongBao` |
| Thương hiệu | `/admin/thuong-hieu` | Logo: tải qua `POST /admin/upload?thuMuc=thuong-hieu` (multipart `file`) rồi gửi `duongDan` nhận được |
| Sản phẩm – danh sách | `GET /admin/san-pham?tuKhoa=&danhMucId=&thuongHieuId=&hienThi=` | Gồm cả sản phẩm đang ẩn; có `tongTon`, `soBienThe` |
| Sản phẩm – sửa | `GET /admin/san-pham/{id}` rồi các API con: tùy chọn, ảnh, biến thể | **Mọi API con trả lại cả sản phẩm** (`SanPhamQT`), đặt thẳng vào state. Thứ tự tạo: thông tin chung → tùy chọn → biến thể → ảnh. Sản phẩm chưa có biến thể đang bán thì không hiện ở trang khách |
| Kho | `POST /admin/bien-the/{id}/nhap-kho`, `/dieu-chinh` (bắt buộc lý do), `GET /admin/kho/lich-su`, `GET /admin/kho/sap-het` | Trang tổng quan nên có ô "Sắp hết hàng" |
| Đơn hàng | `GET /admin/don-hang` (tìm, lọc), `GET /admin/don-hang/{ma}`, `PATCH .../trang-thai`, `.../thanh-toan`, `.../ghi-chu` | Chi tiết có `trangThaiTiepTheo`: chỉ hiện các nút chuyển trạng thái trong danh sách này. `don.lichSu[].nguoiThucHien` chỉ có ở API quản trị |
| Mã giảm giá | `/admin/ma-giam-gia` | `batDauLuc`, `ketThucLuc` dạng ISO có múi giờ, VD `2026-11-20T00:00:00+07:00` |
| Banner | `/admin/banner` | `lienKet` phải bắt đầu bằng `/` hoặc `http(s)://` |
| Cài đặt | `GET/PUT /admin/cai-dat` | `PUT` nhận `{ "khoa": "giá trị" }`, chỉ khóa có sẵn |
| Người dùng | `GET /admin/nguoi-dung`, `PATCH .../{id}/trang-thai` | Khóa thì người đó bị đăng xuất ngay ở lần gọi API kế tiếp; không tự khóa được chính mình |
| Đánh giá | `GET /admin/danh-gia?trangThai=cho_duyet`, `PATCH /admin/danh-gia/{id}`, `DELETE` | Duyệt mới hiện công khai và tính điểm |
| Thống kê | `GET /admin/thong-ke/doanh-thu?tu=&den=&theo=ngay\|thang`, `/don-hang`, `/ban-chay` | Doanh thu trả đủ mọi ngày/tháng trong khoảng (0 nếu không có đơn) – vẽ biểu đồ trực tiếp |

### 10.2 Hành vi cần biết

- Mô tả sản phẩm (HTML) được backend **lọc** khi lưu: `<script>`, `onclick`, `javascript:`… bị bỏ, nên trang khách hiện bằng
  `dangerouslySetInnerHTML` là an toàn. Trình soạn thảo nên chỉ dùng thẻ thường (đoạn, tiêu đề, danh sách, bảng, đậm, nghiêng,
  liên kết, ảnh).
- Slug để trống thì backend tự sinh từ tên không dấu; sửa tên mà để trống slug thì slug đổi theo (đổi đường dẫn trang khách).
  Muốn giữ đường dẫn cũ thì gửi lại `slug` hiện tại.
- Xóa danh mục, thương hiệu, sản phẩm, biến thể, mã giảm giá là xóa mềm: biến mất khỏi danh sách, đơn cũ vẫn xem được.
- SKU, mã giảm giá lưu chữ hoa. Giá hiển thị của sản phẩm tự cập nhật theo biến thể rẻ nhất, không cần gửi.
- Đơn VNPay chưa thanh toán không xác nhận được (chỉ hủy). Đơn COD chuyển "hoàn thành" thì tự thành "đã thanh toán".
  Hoàn tiền chỉ đánh dấu được sau khi đơn đã thanh toán bị hủy.

Sơ đồ: [quản trị xử lý đơn](thiet-ke/19_hd_qt_xu_ly_don_hang.drawio.png), [quản trị thêm sản phẩm](thiet-ke/20_hd_qt_them_san_pham.drawio.png).

## 11. Phase 10 – Dữ liệu mẫu

Chạy backend profile dev trên CSDL trống là có sẵn dữ liệu để dựng giao diện. **CSDL dev cũ phải xóa một lần**
(`docker compose down -v` rồi `docker compose up -d`), vì dữ liệu mẫu chỉ nạp khi chưa có sản phẩm hay đơn hàng nào.

| Có gì | Dùng để thử |
|---|---|
| 35 sản phẩm / 9 danh mục con (mỗi danh mục ≥ 3), 10 thương hiệu có logo (trừ Baseus), 1 sản phẩm ẩn (`tai-nghe-sony-wh-ch510` → 404) | Trang chủ, lọc, sắp xếp, phân trang, trang chi tiết |
| Ảnh: mọi sản phẩm có 1–3 ảnh, 4 danh mục cha có ảnh. `duongDan` là URL đầy đủ `https://...` (Wikimedia Commons) | `duongDanAnh()` trong `src/utils` đã giữ nguyên URL `http(s)` nên hiện đúng. Ảnh upload mới vẫn là `/uploads/...` |
| Biến thể hết hàng (`BAS-GAN5-100-TRANG`, `XMI-PB20K`) và sắp hết | Nút "Hết hàng", cảnh báo tồn kho ở trang quản trị |
| 4 banner: 3 đang chạy, 1 đã hết hạn (chỉ thấy ở trang quản trị) | Slider trang chủ |
| 10 khách `*@example.com` / `Khach@123`, trong đó `ngo.long@example.com` bị khóa (đăng nhập trả 403) | Trang quản trị người dùng |
| 43 đơn trong 90 ngày qua: đủ 5 trạng thái, COD và VNPay (đã trả, thất bại rồi trả lại, quá hạn tự hủy, hoàn tiền), có đơn vãng lai | Đơn của tôi, tra cứu, trang quản trị đơn, thống kê doanh thu theo ngày/tháng, bán chạy |
| 27 đánh giá (đã duyệt, chờ duyệt, đã ẩn) | Sao + đánh giá ở chi tiết sản phẩm, trang duyệt đánh giá |
| `khach@techgear.vn`: 3 đơn, 2 địa chỉ, giỏ hàng 2 dòng, 3 sản phẩm yêu thích | Đăng nhập là thấy ngay giỏ, yêu thích, lịch sử đơn |

Nguồn và giấy phép của từng ảnh: [`nguon-anh-mau.md`](nguon-anh-mau.md). Bản CSDL đầy đủ để nộp bài hoặc nạp tay:
`database/DB_TechGear.sql` (cách nạp ghi ở đầu file).

## 12. Phase 11 – Hành vi mới và hạn chế đã biết

Hành vi mới (sau review bảo mật và review code):
- `POST /don-hang` (và `/vang-lai`) chọn VNPay mà tổng tiền 0đ → 400 "Đơn 0đ không cần thanh toán VNPay, vui lòng chọn thanh
  toán khi nhận hàng". Gợi ý: ẩn lựa chọn VNPay khi `tinh-thu` trả `tongTien = 0`.
- Khách hoặc quản trị hủy đơn **đã thanh toán** (VNPay trả xong khi đơn còn chờ xác nhận) → đơn `da_huy`, vẫn
  `da_thanh_toan`, `ghiChuQuanTri` thêm "Đơn đã thanh toán bị hủy – cần hoàn tiền". Quản trị hoàn tiền xong thì
  `PATCH /admin/don-hang/{ma}/thanh-toan` `{ trangThaiThanhToan: "da_hoan_tien" }`.
- Giới hạn số lần (đăng nhập sai, quên mật khẩu, tra cứu đơn, đặt hàng vãng lai) tính cả các request gửi song song.

Hạn chế đã biết (cố ý giữ, ghi lại để không bất ngờ):
- `POST /don-hang/tinh-thu` là đường dẫn công khai nên backend bỏ qua token. Khách đã đăng nhập cần gửi kèm `email` của
  tài khoản thì kết quả kiểm lượt dùng mã "mỗi khách N lần" mới khớp lúc đặt hàng thật.
- `POST /thanh-toan/vnpay/{ma}` công khai (khách vãng lai cần trả tiền). Ai biết mã đơn (6 ký tự ngẫu nhiên) có thể tạo thêm
  liên kết thanh toán cho đơn đó, nhưng không xem được thông tin đơn và mỗi đơn tối đa 99 lần thử.
- Sửa tùy chọn mà **đổi chéo** tên hai giá trị trong một lần gửi (VD Đen ↔ Trắng) → 409; đổi qua một tên tạm rồi sửa tiếp.
- Bộ đếm giới hạn số lần lưu trong bộ nhớ một máy chủ (chạy nhiều máy chủ thì cần kho dùng chung).

## 13. Kiểm thử phía backend

Mọi API ở trên đã có kiểm thử tự động (JUnit trên PostgreSQL thật) và bộ Postman
`postman/TechGear.postman_collection.json`. Có thể import bộ Postman cùng `postman/TechGear.local.postman_environment.json`
để xem request và response mẫu của từng API: thư mục "01 Tài khoản" (29 request), "02 Sản phẩm" (16 request), "03 Giỏ hàng" (20 request), "04 Đơn hàng" (24 request), "05 Thanh toán" (15 request), "06 Đánh giá" (12 request), "07 Quản trị" (73 request, chạy một vòng từ tạo sản phẩm tới đơn hoàn thành, đánh giá được duyệt, thống kê).
Bộ Postman gọi đủ cả 95 API của backend (đối chiếu với Swagger `/v3/api-docs`).

## 14. Liên hệ

Backend trả sai hình dạng dữ liệu hoặc thiếu trường frontend cần: báo kèm request (URL, body) và response nhận được.
Phần frontend do bạn quyết định. Backend không sửa gì trong `source/frontend`.
