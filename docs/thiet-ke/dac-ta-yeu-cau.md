# Đặc tả yêu cầu – Backend TechGear

Phiên bản: 1.0 (Phase 1 – chờ duyệt) · Cập nhật: 15/11/2026 · Phụ trách: Trần Hoàng Long (CSDL + backend)

## 1. Mục tiêu

Xây dựng lại từ đầu backend (REST API) và CSDL cho website thương mại điện tử **TechGear** bán phụ kiện công nghệ
(tai nghe, loa, sạc, cáp, pin dự phòng, chuột, bàn phím, ốp lưng...). Frontend React do thành viên khác phụ trách và
gọi backend qua HTTP/JSON.

Thành công khi:
- Đủ các use case trong `docs/so-do-use-case/` (9 sơ đồ) có API tương ứng, chạy được trên MySQL.
- Frontend hiện có (`source/frontend`) chạy được với backend mới mà không phải sửa (giữ hợp đồng API – mục 8).
- Mọi API có request Postman kèm kiểm thử tự động; logic tính toán có unit test; tất cả xanh trên CSDL sạch.
- Có tài liệu: đặc tả này, từ điển dữ liệu, ERD, sơ đồ kiến trúc/gói, sơ đồ hoạt động – tuần tự – trạng thái, đặc tả API.

## 2. Tác nhân

| Tác nhân | Mô tả |
|---|---|
| Khách vãng lai | Chưa đăng nhập: xem, tìm kiếm sản phẩm; giỏ hàng (lưu ở trình duyệt); đặt hàng; tra cứu đơn; đăng ký, đăng nhập, quên mật khẩu |
| Khách hàng | Đã đăng nhập, kế thừa khách vãng lai; thêm: thông tin cá nhân, sổ địa chỉ, giỏ hàng trên server, đơn hàng của tôi, đánh giá, yêu thích |
| Quản trị viên | Quản lý danh mục, thương hiệu, sản phẩm, kho, đơn hàng, mã giảm giá, banner, cấu hình, người dùng, đánh giá, thống kê |
| Cổng thanh toán PayOS | Hệ thống ngoài: nhận chuyển khoản / VietQR, gọi lại backend (webhook), trả trạng thái giao dịch |
| Dịch vụ Email (SMTP) | Hệ thống ngoài: gửi liên kết đặt lại mật khẩu |

## 3. Bản đồ module

| Mã module | Trách nhiệm | Phụ thuộc | Use case |
|---|---|---|---|
| `chung` | Lỗi thống nhất, phân trang, cấu hình, bảo mật JWT, upload ảnh, giới hạn số lần | – | – |
| `taikhoan` | Đăng ký, đăng nhập, hồ sơ, mật khẩu, sổ địa chỉ, khóa/mở tài khoản | chung | 04, 09 |
| `danhmuc` | Cây danh mục | chung | 02, 06 |
| `thuonghieu` | Thương hiệu | chung | 02, 06 |
| `sanpham` | Sản phẩm, thông số, tùy chọn, biến thể, ảnh, tìm kiếm | danhmuc, thuonghieu | 02, 06 |
| `cuahang` | Banner, cấu hình cửa hàng (phí vận chuyển, liên hệ) | chung | 02, 08 |
| `giohang` | Giỏ hàng trên server, gộp giỏ, yêu thích | taikhoan, sanpham | 03, 05 |
| `khuyenmai` | Mã giảm giá | chung | 03, 08 |
| `donhang` | Đặt hàng, tồn kho, đơn của tôi, hủy, tra cứu, quản trị đơn và kho | taikhoan, sanpham, khuyenmai, cuahang | 03, 05, 07 |
| `thanhtoan` | PayOS (tạo link, webhook, trang kết quả), COD | donhang | 03, 07 |
| `danhgia` | Viết, xem, duyệt đánh giá | taikhoan, sanpham, donhang | 02, 05, 09 |
| `thongke` | Doanh thu, đơn theo trạng thái, sản phẩm bán chạy | donhang | 09 |

Thứ tự xây: chung → taikhoan → danhmuc, thuonghieu, sanpham, cuahang → giohang → khuyenmai, donhang → thanhtoan →
danhgia → phần quản trị của từng module → thongke.

## 4. Yêu cầu chức năng và quy tắc nghiệp vụ

### 4.1 Tài khoản (`taikhoan`)
- Đăng ký: họ tên, email, mật khẩu (≥ 8 ký tự, có chữ và số), số điện thoại (tùy chọn). Email so sánh không phân biệt
  hoa thường, lưu chữ thường; trùng → 409. Đăng ký xong trả token như đăng nhập.
- Đăng nhập: sai email/mật khẩu → 401 cùng một thông báo (không lộ email nào tồn tại); tài khoản bị khóa → 403.
  Quá 5 lần sai trong 15 phút cho một email + IP → 429.
- Token JWT (HS256) hạn 24 giờ. Token bị từ chối khi: tài khoản bị khóa, hoặc phát hành trước lần đổi/đặt lại mật khẩu gần nhất.
- Quên mật khẩu: luôn trả cùng một thông báo dù email có tồn tại hay không; gửi liên kết `FRONTEND_URL/dat-lai-mat-khau?ma=...`
  hiệu lực 15 phút, dùng một lần; chỉ lưu giá trị băm của mã; mỗi email tối đa 1 yêu cầu/60 giây. Chưa cấu hình SMTP thì ghi liên kết ra log.
- Hồ sơ: xem/sửa họ tên, số điện thoại, giới tính, ngày sinh; đổi ảnh đại diện (ảnh cũ chỉ xóa sau khi lưu ảnh mới thành công).
- Đổi mật khẩu: cần mật khẩu hiện tại; token cũ hết hiệu lực, trả token mới.
- Sổ địa chỉ: tối đa 10; địa chỉ đầu tiên tự là mặc định; luôn đúng một địa chỉ mặc định (CSDL ràng buộc);
  xóa địa chỉ mặc định thì địa chỉ mới nhất còn lại thành mặc định; thao tác trên địa chỉ người khác → 404.
- Đăng xuất: phía frontend xóa token (JWT không lưu phiên ở server).

### 4.2 Danh mục, thương hiệu, sản phẩm (`danhmuc`, `thuonghieu`, `sanpham`, `cuahang`)
- Danh mục 2 cấp (cha – con), có thứ tự, ẩn/hiện. Lọc theo danh mục cha gồm cả sản phẩm của danh mục con.
- Sản phẩm có tùy chọn (màu sắc, dung lượng, phiên bản, khác) và **biến thể** = một tổ hợp giá trị tùy chọn, mỗi biến thể có
  SKU, giá bán, giá gốc (tùy chọn), tồn kho, ngưỡng cảnh báo. **Giá nằm ở biến thể**; giá hiển thị của sản phẩm
  (`giaBan`, `giaGoc`) = giá của biến thể đang bán rẻ nhất, hệ thống tự cập nhật khi biến thể thay đổi.
- Ảnh sản phẩm có thứ tự, một ảnh chính; ảnh có thể gắn với một giá trị màu (đổi màu thì đổi ảnh).
- Tìm kiếm: từ khóa (không phân biệt hoa thường và dấu: "sac" tìm ra "Sạc"), danh mục, nhiều thương hiệu, khoảng giá
  (so với giá từng biến thể đang bán), sắp xếp (mới nhất, giá tăng, giá giảm, phổ biến), phân trang (1–48/trang, mặc định 12).
  Kết quả ổn định: cùng điều kiện thì hai trang liền nhau không trùng sản phẩm.
- Chỉ hiển thị sản phẩm/biến thể/danh mục/thương hiệu đang bật và chưa xóa. Xem chi tiết tăng lượt xem.
- Banner hiển thị khi đang bật và trong khung giờ (mốc để trống = không giới hạn), theo thứ tự.
- Cấu hình cửa hàng công khai: tên cửa hàng, hotline, email liên hệ, phí vận chuyển, ngưỡng miễn phí vận chuyển (không chứa bí mật).

### 4.3 Giỏ hàng & yêu thích (`giohang`)
- Khách vãng lai: giỏ lưu ở trình duyệt (frontend). Khách hàng: giỏ lưu ở server.
- Thêm cùng biến thể thì cộng dồn; số lượng 1 ≤ n ≤ tồn kho; chỉ biến thể đang bán.
- Đăng nhập: frontend gửi giỏ tạm để **gộp** vào giỏ server (cộng dồn, cắt theo tồn kho, bỏ dòng không còn bán).
- Yêu thích: thêm/bỏ/xem danh sách; thêm lại sản phẩm đã có không lỗi.

### 4.4 Mã giảm giá (`khuyenmai`)
- Loại: phần trăm (1–100, có thể giới hạn giảm tối đa) hoặc số tiền cố định.
- Điều kiện: đang bật, trong khung giờ, tạm tính ≥ đơn tối thiểu, chưa hết tổng lượt, khách chưa vượt lượt/người
  (khách vãng lai tính theo email). Đơn đã hủy không tính lượt.
- Tiền giảm làm tròn xuống đến đồng, không vượt tạm tính.

### 4.5 Đặt hàng & đơn hàng (`donhang`)
- Đặt hàng (vãng lai hoặc khách hàng): danh sách (biến thể, số lượng), thông tin người nhận (chọn từ sổ địa chỉ hoặc nhập),
  mã giảm giá (tùy chọn), phương thức thanh toán COD hoặc PayOS, ghi chú. Có API **tính thử** trả tạm tính – giảm giá – phí ship – tổng.
- **Mọi số tiền tính ở server** từ giá hiện tại; tổng = tạm tính − giảm giá + phí vận chuyển. Phí vận chuyển theo cấu hình,
  miễn phí khi (tạm tính − giảm giá) ≥ ngưỡng.
- Tồn kho trừ ngay khi đặt, trong cùng giao dịch, có khóa dòng; không đủ hàng → 409 nêu rõ biến thể. Ghi lịch sử tồn kho.
- Lưu ảnh chụp (snapshot) tên, SKU, phân loại, ảnh, đơn giá của từng dòng và địa chỉ giao hàng tại thời điểm đặt.
- Mã đơn dạng `DHyyMMdd` + 6 ký tự ngẫu nhiên (không đoán được). Khách hàng đặt xong thì xóa các dòng tương ứng khỏi giỏ server.
- Trạng thái đơn: `cho_xac_nhan → da_xac_nhan → dang_giao → hoan_thanh`; hủy được từ `cho_xac_nhan` hoặc `da_xac_nhan`.
  Mỗi lần đổi trạng thái ghi lịch sử (ai, lúc nào, ghi chú).
- Khách hàng: xem danh sách đơn của mình (lọc theo trạng thái), chi tiết + lịch sử trạng thái, hủy đơn khi còn `cho_xac_nhan`.
  Đơn của người khác → 404.
- Tra cứu đơn (vãng lai): mã đơn + email đặt hàng; sai → 404; giới hạn số lần.
- Hủy đơn (khách hoặc quản trị): hoàn tồn kho (ghi lịch sử), trả lại lượt mã giảm giá.
- Đơn PayOS chưa thanh toán sau 30 phút thì hệ thống tự hủy (hoàn kho).

### 4.6 Thanh toán (`thanhtoan`)
- COD: trạng thái thanh toán chuyển `da_thanh_toan` khi đơn `hoan_thanh`.
- PayOS (chuyển khoản / VietQR): tạo link thanh toán cho đơn PayOS chưa thanh toán; kết quả ghi nhận qua **webhook** (kiểm chữ ký
  HMAC-SHA256, mã giao dịch, số tiền) hoặc khi khách về trang kết quả – backend tự hỏi PayOS, không tin tham số trên URL;
  mỗi giao dịch chỉ ghi một lần. Profile dev giả lập PayOS khi chưa có khóa thật.
- Hoàn tiền PayOS (đơn đã thanh toán bị hủy): quản trị đánh dấu `da_hoan_tien` thủ công (không gọi API hoàn tiền).

### 4.7 Đánh giá (`danhgia`)
- Chỉ khách có đơn `hoan_thanh` chứa sản phẩm; mỗi (khách, sản phẩm, đơn) một lần; 1–5 sao, nội dung ≤ 2000 ký tự.
- Đánh giá mới ở trạng thái chờ duyệt; chỉ đánh giá đã duyệt hiện công khai và tính vào điểm trung bình (làm tròn 1 chữ số).
- Quản trị: duyệt, ẩn, xóa.

### 4.8 Quản trị
- Danh mục, thương hiệu, sản phẩm (kèm thông số, tùy chọn, biến thể, ảnh): thêm/sửa/xóa. Slug tự sinh từ tên tiếng Việt,
  không trùng. Xóa là xóa mềm; không xóa danh mục còn danh mục con hoặc sản phẩm (409).
- Kho: nhập kho, điều chỉnh (có lý do), xem lịch sử nhập – xuất, danh sách biến thể sắp hết (tồn ≤ ngưỡng).
- Đơn hàng: tìm (mã đơn, tên, SĐT, email), lọc (trạng thái, thanh toán, khoảng ngày), xem chi tiết, chuyển trạng thái hợp lệ,
  cập nhật trạng thái thanh toán, ghi chú nội bộ, hủy.
- Mã giảm giá, banner: thêm/sửa/xóa/bật-tắt. Cấu hình cửa hàng: cập nhật phí vận chuyển, thông tin liên hệ.
- Người dùng: xem danh sách (tìm, lọc vai trò/trạng thái), khóa/mở (không tự khóa chính mình).
- Đánh giá: lọc theo trạng thái, duyệt/ẩn/xóa.
- Thống kê: doanh thu theo ngày/tháng trong khoảng thời gian (chỉ đơn `hoan_thanh`), số đơn theo trạng thái,
  top sản phẩm bán chạy (theo số lượng trong đơn `hoan_thanh`).

## 5. Yêu cầu phi chức năng
- **Bảo mật:** BCrypt cường độ 12; JWT secret bắt buộc từ biến môi trường (thiếu thì không khởi động, trừ profile `dev`);
  `/api/admin/**` chỉ quản trị viên; CORS theo danh sách origin cấu hình; truy vấn tham số hóa; upload chỉ nhận ảnh
  JPG/PNG/WEBP/GIF ≤ 5MB kiểm theo nội dung file, chặn path traversal; lỗi không lộ chi tiết nội bộ; giới hạn số lần ở
  đăng nhập, quên mật khẩu, tra cứu đơn.
- **Dữ liệu:** MySQL 8.4 (utf8mb4, InnoDB); lược đồ do Flyway quản lý; ràng buộc CHECK/UNIQUE/FK ở CSDL cho các bất biến quan trọng
  (tồn kho ≥ 0, tổng tiền đúng công thức, một địa chỉ mặc định...). Thời gian lưu `DATETIME(6)` theo giờ Việt Nam, trả về ISO-8601 múi giờ +07:00.
  Tiền là số nguyên đồng (`BIGINT`).
- **Hiệu năng:** không có truy vấn N+1 ở danh sách; trang danh sách sản phẩm ≤ 2 truy vấn chính + truy vấn theo lô.
- **Ngôn ngữ:** thông báo lỗi, comment tiếng Việt; tên bảng/cột/trường JSON tiếng Việt không dấu kiểu camelCase/snake_case.

## 6. Công nghệ, lệnh, cấu trúc
| Thành phần | Phiên bản |
|---|---|
| Java / Maven | 21 / 3.9 |
| Spring Boot | 4.1.2 (Web MVC, Data JPA, Security + OAuth2 Resource Server, Validation, Mail) |
| MySQL / Flyway | 8.4 / 12.4 |
| springdoc-openapi | 3.2.0 |
| Kiểm thử | JUnit 5, Testcontainers 2.0.5, Postman + Newman |

| Việc | Lệnh |
|---|---|
| Chạy CSDL | `docker compose up -d` (ở gốc repo) |
| Chạy backend | `cd source/backend && mvn spring-boot:run` (profile `dev` có dữ liệu mẫu) |
| Unit test | `mvn test` |
| Test tích hợp (cần Docker) | `mvn verify` |
| Test API | `npx newman run postman/TechGear.postman_collection.json -e postman/TechGear.local.postman_environment.json` |

```
source/backend/src/main/java/com/techgear/
├── chung/        lỗi (ApiException, xử lý lỗi), phân trang, cấu hình (bảo mật, CORS, OpenAPI), upload, giới hạn số lần
├── taikhoan/     controller, service, repository, entity, dto của module tài khoản
├── danhmuc/ thuonghieu/ sanpham/ cuahang/ giohang/ khuyenmai/ donhang/ thanhtoan/ danhgia/ thongke/   (cùng cấu trúc)
source/backend/src/main/resources/db/migration/   migration Flyway (V1__..., V2__...)
source/backend/src/main/resources/db/du-lieu-mau/ dữ liệu mẫu (chỉ profile dev)
postman/          collection + environment
docs/thiet-ke/    tài liệu thiết kế (file này, từ điển dữ liệu, sơ đồ, đặc tả API)
```

Phong cách code (ví dụ):
```java
@Transactional
public DiaChiResponse tao(Long nguoiDungId, DiaChiRequest req) {
    if (diaChiRepository.countByNguoiDungId(nguoiDungId) >= TOI_DA_DIA_CHI) {
        throw ApiException.yeuCauKhongHopLe("Chỉ lưu tối đa " + TOI_DA_DIA_CHI + " địa chỉ");
    }
    ...
}
```

## 7. Chiến lược kiểm thử
| Mức | Công cụ | Phạm vi |
|---|---|---|
| Unit | JUnit 5 + Mockito (`mvn test`) | Logic: tính tiền, mã giảm giá, chuyển trạng thái đơn, chữ ký PayOS, slug, kiểm tra ảnh |
| Tích hợp | Testcontainers MySQL (`mvn verify`) | Khởi động với Flyway thật + `ddl-auto=validate`; đặt hàng đồng thời không bán quá tồn kho |
| API | Postman + Newman | Mọi API: mã trạng thái, hình dạng JSON, quy tắc nghiệp vụ, 401/403/404/409/429, hợp đồng frontend |

## 8. Hợp đồng API phải giữ cho frontend hiện có
`POST /api/auth/dang-ky`, `POST /api/auth/dang-nhap` → `{ token, nguoiDung }`; `GET /api/auth/toi`; `GET /api/danh-muc` (cây,
`danhMucCons`); `GET /api/thuong-hieu`; `GET /api/san-pham` → `{ noiDung, trang, kichThuoc, tongSoPhanTu, tongSoTrang }`;
`GET /api/san-pham/noi-bat`; `GET /api/san-pham/{slug}`. Lỗi `{ thoiGian, trangThai, thongBao, loi }`; `vaiTro`: `quan_tri` | `khach_hang`.
Chi tiết trường: `docs/thiet-ke/dac-ta-api.md`.

## 9. Ranh giới
- **Luôn:** viết test trước code logic; chạy `mvn test` + Newman trước mỗi commit; kiểm đầu vào ở controller (Bean Validation);
  giữ hợp đồng API mục 8; thông báo lỗi tiếng Việt.
- **Hỏi trước:** đổi lược đồ đã duyệt; thêm thư viện ngoài danh sách mục 6; đổi hợp đồng API đã công bố; đổi cấu hình bảo mật/CORS.
- **Không bao giờ:** sửa/xóa `source/frontend`; commit bí mật (JWT secret, mật khẩu SMTP, khóa PayOS); push trước khi kiểm tra toàn bộ;
  bỏ/tắt test đang lỗi để qua.

## 10. Câu hỏi cần chốt
1. Giá đặt ở **biến thể**, giá hiển thị của sản phẩm là giá biến thể rẻ nhất (tự cập nhật) – đồng ý?
2. Đơn PayOS chưa thanh toán sau **30 phút** tự hủy và hoàn kho – đồng ý thời gian này?
3. Doanh thu thống kê chỉ tính đơn **hoàn thành** – đồng ý?
4. Mật khẩu tối thiểu 8 ký tự, có chữ và số – đồng ý?
