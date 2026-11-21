# TECHGEAR ADMIN – Đợt 1: Nền tảng cửa hàng

Ngày: 21/11/2026 · Trạng thái: đã duyệt

## 1. Mục đích

TECHGEAR ADMIN là công cụ **vận hành** cửa hàng, không chỉ là nơi sửa dữ liệu: mọi thao tác của quản trị viên phải tác động ngay lên website khách hàng (thêm sản phẩm → sản phẩm xuất hiện ở cửa hàng; sửa giá → giá trên web đổi; nhập kho → khách mua được).

Toàn bộ khu quản trị chia 4 đợt theo sự phụ thuộc nghiệp vụ (do người thực hiện đề xuất):

| Đợt | Nội dung |
|---|---|
| **1 – Nền tảng cửa hàng** | Danh mục → Thương hiệu → Sản phẩm → Biến thể → Tồn kho *(tài liệu này)* |
| 2 – Quy trình bán hàng | Đơn hàng → cập nhật trạng thái → lịch sử tồn kho |
| 3 – Chức năng bổ trợ | Mã giảm giá → Banner → Cấu hình cửa hàng |
| 4 – Quản lý và kiểm soát | Người dùng → Duyệt đánh giá → Thống kê (dashboard) |

Đợt 1 bảo đảm quản trị viên tự tạo và quản lý được sản phẩm để website có dữ liệu bán hàng, không phải sửa CSDL bằng phpMyAdmin.

## 2. Mục tiêu / Không làm / Điều cần tránh

**Mục tiêu**
- Khung giao diện quản trị dùng chung cho cả 4 đợt (menu trái, vùng nội dung, bảo vệ quyền).
- CRUD danh mục (cây cha – con), thương hiệu (API đã có, làm giao diện).
- Tạo / sửa sản phẩm đầy đủ: thông tin chung, giá, bảo hành, nổi bật, hiển thị, hình ảnh, thông số kỹ thuật, tùy chọn và biến thể.
- Biến thể sinh tự động từ tổ hợp tùy chọn (**phương án A – đã chốt**).
- Tồn kho: xem toàn bộ biến thể, cảnh báo sắp hết, **Nhập kho** và **Điều chỉnh** có ghi lý do, xem lịch sử biến động.

**Không làm trong Đợt 1**
- Đơn hàng, mã giảm giá, banner, cấu hình, người dùng, đánh giá, thống kê (các đợt sau). Menu hiển thị các mục này ở trạng thái "đợt sau", không bấm được.
- Trình soạn thảo WYSIWYG cho mô tả: dùng ô văn bản cho phép HTML cơ bản (frontend đã lọc HTML an toàn bằng `lamSachHtml`).
- Nhập/xuất Excel, thao tác hàng loạt, phân quyền nhiều cấp quản trị (chỉ một vai trò `quan_tri`).
- Ứng dụng React riêng cho admin, trang server-render.

**Điều cần tránh (coi là thất bại dù chạy được)**
- Sửa thẳng con số tồn kho mà không ghi `lich_su_ton_kho`.
- Thay đổi tùy chọn làm hỏng đơn hàng cũ hoặc biến thể đang có trong giỏ của khách.
- Admin thao tác xong mà website khách không phản ánh (dữ liệu lệch).
- Khách hàng thường vào được trang/API quản trị.

## 3. Ràng buộc

- Giữ nguyên công nghệ: React 18 + Vite 5 + React Router 6 + Axios; Spring Boot 3.3.5, Spring Data JPA, Spring Security + JWT; MySQL/MariaDB (XAMPP). Không thêm Docker, không thêm thư viện UI lớn.
- **Không đổi cấu trúc CSDL**: 24 bảng hiện có đã đủ (`danh_muc`, `thuong_hieu`, `san_pham`, `thuoc_tinh_san_pham`, `tuy_chon`, `gia_tri_tuy_chon`, `bien_the_san_pham`, `bien_the_gia_tri`, `hinh_anh_san_pham`, `lich_su_ton_kho`).
- API quản trị đặt dưới `/api/admin/**` (đã chặn cho vai trò `QUAN_TRI` trong `SecurityConfig`). Theo quy ước hiện có: đường dẫn tiếng Việt không dấu, JSON camelCase tiếng Việt, DTO là Java `record` có `@Valid`, lỗi qua `ApiException` → `GlobalExceptionHandler`, xóa mềm bằng `@SQLDelete`.
- Mô hình dữ liệu đang dùng phải giữ nguyên:
  - Sản phẩm không có tùy chọn vẫn có **một biến thể mặc định** không gắn giá trị (ví dụ `TG-OL01`).
  - Giá biến thể `NULL` = **lấy giá sản phẩm**; chỉ điền khi khác giá sản phẩm.
  - Mọi biến thể có bản ghi `lich_su_ton_kho` loại `khoi_tao`.
- Các ràng buộc duy nhất trong CSDL tính cả bản ghi đã xóa mềm: `san_pham.slug`, `bien_the_san_pham.sku`, `danh_muc.slug` → kiểm tra trùng phải tính cả bản ghi đã xóa (giống cách `ThuongHieuService` / `AuthService` đang làm).

## 4. Các phương án đã cân nhắc

### 4.1. Nơi đặt giao diện quản trị — **chọn: chung ứng dụng React, đường dẫn `/admin/*`**

| Phương án | Ưu | Nhược |
|---|---|---|
| **1. Chung ứng dụng React, `/admin/*`** *(đã chọn)* | Dùng lại `axiosClient`, đăng nhập, design tokens, toast, `Pagination`; một lệnh `npm run dev`; không sửa CORS; admin mở ngay trang khách để kiểm tra | Gói JS lớn hơn → khắc phục bằng `React.lazy` tải phần admin khi vào `/admin` |
| 2. Ứng dụng React riêng (`source/admin`) | Tách biệt, triển khai riêng | Dựng lại từ đầu, 2 dev server, thêm CORS/biến môi trường; tốn công với đồ án |
| 3. Trang Thymeleaf trong Spring Boot | Nhanh cho CRUD | Đi ngược định hướng hướng dịch vụ (backend chỉ cung cấp API) → loại |

### 4.2. Cách tạo biến thể — **chọn: A. tự sinh tổ hợp**

| Phương án | Ưu | Nhược |
|---|---|---|
| **A. Admin khai báo tùy chọn + giá trị, hệ thống sinh đủ tổ hợp** *(đã chốt)* | Không sót / trùng tổ hợp; nhanh khi nhiều tùy chọn; giống các sàn TMĐT | Giao diện và logic đồng bộ phức tạp hơn |
| B. Tạo từng biến thể thủ công | Làm nhanh hơn | Dễ trùng/sót tổ hợp, sai dữ liệu |

### 4.3. Thay đổi tồn kho — **chọn: chỉ qua Nhập kho / Điều chỉnh có lý do**
Không cho sửa thẳng `so_luong_ton` trong form sản phẩm. Lý do: `lich_su_ton_kho` phải luôn giải thích được mọi biến động (khởi tạo, nhập, bán, hủy đơn, điều chỉnh) — cần cho Đợt 2 (đơn hàng) và Đợt 4 (thống kê).

## 5. Thiết kế

### 5.1. Khung quản trị (frontend)

- Route `/admin/*` bọc trong `<ProtectedRoute chiQuanTri>` (component đã hỗ trợ `chiQuanTri`) và `AdminLayout` riêng (không dùng Header/Footer của cửa hàng). Tải lười bằng `React.lazy`.
- `AdminLayout`: menu trái cố định — Danh mục, Thương hiệu, Sản phẩm, Tồn kho; nhóm "Đợt sau" (Đơn hàng, Mã giảm giá, Banner, Cấu hình, Người dùng, Đánh giá, Tổng quan) ở trạng thái mờ; cuối menu: "Về cửa hàng", tên admin, Đăng xuất. Trên màn hình hẹp, menu thu gọn thành nút mở.
- `/admin` chuyển hướng tới `/admin/san-pham` (trang Tổng quan thuộc Đợt 4).
- Header cửa hàng: khi `laQuanTri`, menu tài khoản có thêm mục **"Trang quản trị"**.
- Dùng lại `tokens.css` (navy + electric blue). Thêm `styles/admin.css` cho bảng dữ liệu, thanh lọc, form 2 cột, modal xác nhận, nhãn trạng thái.
- Mã nguồn: `src/admin/` gồm `AdminLayout.jsx`, `pages/` (DanhMuc, ThuongHieu, SanPhamList, SanPhamForm, TonKho), `components/` (DataTable, ConfirmDialog, ImageUploader, VariantEditor, StockDialog), `api/adminApi.js`.

```
┌───────────────┬───────────────────────────────────────────┐
│ TECHGEAR ADMIN│  Sản phẩm                  [+ Thêm sản phẩm]│
│ ▸ Danh mục    │  [Tìm tên/SKU…] [Danh mục ▾] [Trạng thái ▾]  │
│ ▸ Thương hiệu │  Ảnh │ Tên │ Danh mục │ Giá │ Tồn │ Hiển thị │⋯│
│ ▸ Sản phẩm    │  ▣   │ Tai nghe BT… │ Tai nghe │1.290.000│ 50 │●│✎│
│ ▸ Tồn kho     │                                    « 1 2 3 »  │
│ ─ Đợt sau ─   │                                              │
│ ↩ Về cửa hàng │                                              │
└───────────────┴───────────────────────────────────────────┘
```

### 5.2. Danh mục

- Giao diện: danh sách dạng cây 2 cấp (cha → con), cột thứ tự, hiển thị, số sản phẩm; form thêm/sửa: tên, danh mục cha (chỉ chọn danh mục cấp gốc → giữ cây 2 cấp như website đang hiển thị), mô tả, ảnh (tải lên thư mục `danh-muc`), thứ tự, hiển thị.
- Slug tự sinh từ tên (`SlugUtil`), giữ slug cũ nếu tên không đổi về mặt slug (cùng cách `ThuongHieuService`).
- Xóa: xóa mềm; **chặn** nếu còn danh mục con hoặc còn sản phẩm (chưa xóa) → 409 kèm thông báo.
- API: `GET/POST /api/admin/danh-muc`, `PUT/DELETE /api/admin/danh-muc/{id}`. `GET` trả cả danh mục đang ẩn, kèm số sản phẩm.
- Ẩn danh mục: cây danh mục trên website đã lọc `hien_thi` sẵn (`findByHienThiTrueOrderByThuTuAsc`), nhưng hiện sản phẩm thuộc danh mục ẩn vẫn hiện ở tìm kiếm/danh sách vì `SanPhamService` chỉ xét `san_pham.hien_thi`. Bổ sung điều kiện: sản phẩm chỉ hiển thị và mua được khi **cả sản phẩm và danh mục (và danh mục cha) đều đang hiển thị** — áp dụng cho danh sách, nổi bật, chi tiết, thêm vào giỏ, đặt hàng.

### 5.3. Thương hiệu

- API đã có (`/api/admin/thuong-hieu`). Làm giao diện: bảng (logo, tên, số sản phẩm, hiển thị), form thêm/sửa (tên, logo tải lên thư mục `thuong-hieu`, mô tả, hiển thị), xóa mềm có xác nhận.
- Bổ sung kiểm tra: chặn xóa thương hiệu còn sản phẩm, hoặc cho xóa và để sản phẩm "không thương hiệu" (CSDL `ON DELETE SET NULL`). **Quyết định của người xây dựng:** chặn xóa nếu còn sản phẩm, nhất quán với danh mục.

### 5.4. Sản phẩm

**Danh sách** (`/admin/san-pham`): tìm theo tên hoặc SKU, lọc danh mục / thương hiệu / trạng thái (đang bán, đang ẩn, hết hàng), phân trang; cột: ảnh chính, tên, danh mục, giá bán (và giá gốc nếu sale), tổng tồn, số biến thể, nổi bật, hiển thị; thao tác: sửa, ẩn/hiện nhanh, xóa (mềm), "Xem trên cửa hàng".

**Form tạo/sửa** (`/admin/san-pham/moi`, `/admin/san-pham/{id}`) — một trang, chia khối:
1. *Thông tin chung*: tên, danh mục (bắt buộc), thương hiệu (tùy chọn), mô tả ngắn, mô tả (HTML cơ bản), bảo hành (tháng), nổi bật, hiển thị.
2. *Giá*: giá bán (bắt buộc), giá gốc (tùy chọn; phải ≥ giá bán; có giá gốc > giá bán thì website hiện nhãn sale).
3. *Hình ảnh*: tải nhiều ảnh (`/api/admin/upload?thuMuc=san-pham`), kéo/chọn thứ tự, chọn **ảnh chính**, gắn ảnh với một giá trị tùy chọn (ví dụ ảnh màu Đen). Bắt buộc ≥ 1 ảnh khi bật hiển thị.
4. *Thông số kỹ thuật*: danh sách dòng tên – giá trị, thêm/xóa/sắp xếp.
5. *Tùy chọn & biến thể* (5.5).

Lưu toàn bộ trong **một giao dịch** bằng một request (`POST` tạo, `PUT` cập nhật) → không có trạng thái nửa vời. Slug tự sinh, duy nhất kể cả sản phẩm đã xóa.

API:
- `GET /api/admin/san-pham` (tìm, lọc, phân trang) · `GET /api/admin/san-pham/{id}` (đầy đủ để sửa)
- `POST /api/admin/san-pham` · `PUT /api/admin/san-pham/{id}`
- `PATCH /api/admin/san-pham/{id}/hien-thi` · `DELETE /api/admin/san-pham/{id}` (mềm)

### 5.5. Tùy chọn & biến thể (phương án A)

**Khai báo**: tối đa 3 tùy chọn; mỗi tùy chọn có tên, loại (`mau_sac` / `dung_luong` / `phien_ban` / `khac`) và danh sách giá trị (màu có thêm mã màu). Không có tùy chọn nào → một biến thể mặc định.

**Sinh tổ hợp**: mỗi lần thay đổi tùy chọn, giao diện tính lại tích Đề-các các giá trị và hiển thị bảng biến thể; mỗi dòng có: phân loại (ví dụ "Đen / 20000mAh"), SKU, giá bán riêng, giá gốc riêng, tồn kho ban đầu (chỉ với biến thể mới), ngưỡng cảnh báo, đang bán. Giới hạn tối đa **50 tổ hợp**.
- SKU gợi ý tự động: `TG-<mã sản phẩm rút gọn>-<giá trị không dấu>` (ví dụ `TG-SDP01-20000`), sửa được; kiểm tra trùng ở cả giao diện và backend (tính cả biến thể đã xóa).
- Giá riêng để trống = dùng giá sản phẩm (giữ đúng mô hình hiện có). Có ô "Áp dụng cho tất cả" để điền nhanh giá / ngưỡng.

**Đồng bộ khi sửa sản phẩm** (backend, trong cùng giao dịch), so khớp biến thể theo **tập giá trị** của nó:
- Tổ hợp đã tồn tại → giữ nguyên `id` (giỏ hàng, đơn hàng, lịch sử tồn kho vẫn trỏ đúng), cập nhật SKU / giá / ngưỡng / đang bán. **Không** đổi tồn kho ở đây.
- Tổ hợp mới → tạo biến thể, tồn kho = số nhập ban đầu, ghi `lich_su_ton_kho` loại `khoi_tao` (người thực hiện = admin).
- Tổ hợp biến mất (do bỏ giá trị / tùy chọn) → **xóa mềm** biến thể (`deleted_at`); biến thể đã xóa mềm không còn bán được, đơn hàng cũ vẫn giữ thông tin nhờ `chi_tiet_don_hang` lưu tên/SKU/phân loại tại thời điểm đặt. Xóa dòng giỏ hàng chứa biến thể đó.
- Đổi tên một giá trị (ví dụ "Xanh" → "Xanh dương") → sửa tại chỗ, biến thể giữ nguyên.
- Từ "không tùy chọn" chuyển sang có tùy chọn: biến thể mặc định bị xóa mềm, các biến thể mới được sinh (admin được cảnh báo trên giao diện nếu biến thể mặc định còn tồn kho).
- Giao diện cảnh báo trước khi lưu: "X biến thể sẽ ngừng bán (còn Y sản phẩm trong kho)".

### 5.6. Tồn kho

**Trang** `/admin/ton-kho`: bảng toàn bộ biến thể đang có (sản phẩm, phân loại, SKU, tồn, ngưỡng, trạng thái **Còn hàng / Sắp hết (≤ ngưỡng) / Hết hàng**); tìm theo tên/SKU; lọc theo trạng thái; mặc định sắp xếp "Sắp hết" lên đầu.

**Thao tác** (hộp thoại):
- **Nhập kho**: số lượng > 0, ghi chú (tùy chọn) → `so_luong_ton += n`, lịch sử loại `nhap_kho`.
- **Điều chỉnh**: nhập **số lượng thực tế** (≥ 0) và **lý do (bắt buộc)** → hệ thống tính chênh lệch, lịch sử loại `dieu_chinh` với `so_luong_thay_doi` = chênh lệch.
- **Lịch sử**: danh sách biến động của biến thể (thời gian, loại, thay đổi, tồn sau, ghi chú, người thực hiện / mã đơn hàng nếu có).

Backend khóa dòng biến thể bằng `findByIdForUpdate` (đã có) để không xung đột với đặt hàng đồng thời. Người thực hiện = admin đang đăng nhập.

API:
- `GET /api/admin/ton-kho` (tìm, lọc trạng thái, phân trang)
- `POST /api/admin/ton-kho/{bienTheId}/nhap-kho` · `POST /api/admin/ton-kho/{bienTheId}/dieu-chinh`
- `GET /api/admin/ton-kho/{bienTheId}/lich-su`

### 5.7. Liên kết với website khách hàng (tiêu chí nghiệm thu)

| Thao tác admin | Kết quả phải thấy trên website |
|---|---|
| Tạo sản phẩm đang hiển thị | Xuất hiện ở danh sách, tìm kiếm, lọc theo danh mục/thương hiệu/giá; "Hàng mới về" trên trang chủ |
| Bật "nổi bật" | Xuất hiện ở "Sản phẩm nổi bật" |
| Giá gốc > giá bán | Hiện giá gạch ngang và nhãn −x% |
| Sửa giá / giá biến thể | Giá mới ở thẻ sản phẩm, trang chi tiết, giỏ hàng, tính tiền khi đặt |
| Ẩn sản phẩm / danh mục | Biến mất khỏi cửa hàng; trang chi tiết trả 404; không thêm vào giỏ được |
| Thêm giá trị tùy chọn | Trang chi tiết có lựa chọn mới, đúng giá/tồn của biến thể |
| Nhập kho cho biến thể hết hàng | Biến thể mua được; thẻ sản phẩm hết nhãn "Hết hàng" |
| Khách đặt hàng | Tồn kho ở trang admin giảm, lịch sử có dòng `ban_hang` kèm mã đơn |

### 5.8. Lỗi và kiểm tra dữ liệu

- Kiểm tra bằng `@Valid` trên DTO (thông báo tiếng Việt); lỗi nghiệp vụ trả 400/404/409 qua `ApiException`, giao diện hiển thị `thongBao` và lỗi theo trường (`loi`) ngay dưới ô nhập.
- Trùng tên danh mục / SKU / slug → 409. Giá gốc < giá bán → 400. Xóa danh mục/thương hiệu còn sản phẩm → 409.
- Gọi API admin bằng tài khoản khách → 403 (đã có `JsonAuthHandlers`); giao diện chuyển về trang chủ.

### 5.9. Kiểm thử

- Kiểm thử đơn vị (JUnit 5 + Mockito, theo mẫu các test hiện có) cho phần logic khó: sinh/đồng bộ biến thể (giữ id, tạo mới, xóa mềm, đổi tên giá trị, chuyển không-tùy-chọn → có-tùy-chọn), nhập kho / điều chỉnh (lịch sử đúng, chặn số âm, bắt buộc lý do), chặn xóa danh mục còn sản phẩm, slug/SKU trùng kể cả đã xóa.
- Chạy `mvn test` (toàn bộ test cũ vẫn đạt) và `npm run build`.
- Kiểm tra thủ công theo bảng 5.7 trên XAMPP.

## 6. Quyết định của người xây dựng (người thực hiện kiểm tra lại khi đọc)

1. Cây danh mục giới hạn 2 cấp trên giao diện admin (CSDL hỗ trợ nhiều cấp nhưng website đang hiển thị 2 cấp).
2. Chặn xóa thương hiệu còn sản phẩm (thay vì để sản phẩm mất thương hiệu).
3. Tối đa 3 tùy chọn và 50 biến thể mỗi sản phẩm.
4. Tồn kho ban đầu chỉ nhập khi tạo biến thể mới; sau đó chỉ qua Nhập kho / Điều chỉnh.
5. Biến thể biến mất khi sửa tùy chọn → xóa mềm và gỡ khỏi giỏ hàng của khách.
6. Bắt buộc ít nhất 1 ảnh khi sản phẩm ở trạng thái hiển thị.
7. Mô tả dùng ô văn bản HTML cơ bản, không dùng trình soạn thảo WYSIWYG.
8. Trang Tổng quan để sang Đợt 4; `/admin` mở thẳng trang Sản phẩm.
9. Ẩn danh mục thì ẩn luôn các sản phẩm thuộc danh mục đó (và danh mục con) khỏi cửa hàng, không chỉ ẩn khỏi menu.

## 7. Phần để lại cho người xây dựng

Bố cục chi tiết từng màn hình, tên component, cấu trúc DTO cụ thể, cách chia controller/service (gợi ý: `AdminDanhMucController`, `AdminSanPhamController`, `AdminTonKhoController` + service tương ứng), thứ tự các bước triển khai trong kế hoạch.
