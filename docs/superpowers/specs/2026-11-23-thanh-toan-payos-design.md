# Chuyển thanh toán trực tuyến từ VNPay sang PayOS – Thiết kế

Ngày: 2026-11-23 · Trạng thái: đã duyệt (phương án tài liệu 1 – sửa hết, vẽ lại sơ đồ tuần tự)

## 1. Mục đích
Thay toàn bộ cổng thanh toán trực tuyến VNPay bằng PayOS (chuyển khoản / quét VietQR) trên cả backend, CSDL, frontend,
kiểm thử và tài liệu. Hiện backend đã có luồng VNPay (Phase 7) nhưng frontend chưa có luồng thanh toán online (nút VNPay
ghi "Sắp ra mắt"), nên phần frontend là làm mới.

## 2. Mục tiêu / Không làm / Điều cần tránh
**Mục tiêu**
- Khách chọn PayOS khi đặt hàng → được chuyển sang trang thanh toán PayOS → quay về trang kết quả của TechGear.
- Đơn chuyển "Đã thanh toán" khi PayOS xác nhận (webhook hoặc backend tự hỏi PayOS ở trang kết quả).
- Giữ nguyên quy tắc nghiệp vụ cũ: đơn online chưa trả không xác nhận được; tự hủy sau 35 phút (link hết hạn phút 30);
  đơn 0đ không chọn thanh toán online; hoàn tiền là thủ công.
- Không còn "VNPay" trong code, cấu hình, dữ liệu mẫu, Postman, README, tài liệu thiết kế, sơ đồ.

**Không làm**
- Hoàn tiền tự động qua API PayOS, đối soát, hóa đơn điện tử.
- Sửa file báo cáo `docs/BC_HDV*` (không đưa lên git).
- Đăng ký tài khoản / khóa PayOS thật (nhóm tự làm; PayOS không có sandbox).

**Điều cần tránh**
- Tin tham số trên returnUrl (PayOS không ký) để đánh dấu đã thanh toán.
- Sửa migration đã chạy (V5, V6).
- Ghi nhận thanh toán khi số tiền không khớp hoặc giao dịch đã kết thúc (chống ghi hai lần).

## 3. Ràng buộc từ PayOS (tài liệu payos.vn/docs, đọc 2026-11-23)
- Gốc API `https://api-merchant.payos.vn`, header `x-client-id`, `x-api-key`. Không có sandbox.
- Tạo link: `POST /v2/payment-requests` `{ orderCode (số nguyên), amount, description, cancelUrl, returnUrl, expiredAt (Unix giây), items?, signature }`.
  `signature` = HMAC-SHA256 (khóa checksum, hex) của `amount=..&cancelUrl=..&description=..&orderCode=..&returnUrl=..`.
  `description` tối đa 9 ký tự với tài khoản ngân hàng không liên kết qua PayOS (xem 5.1).
  Trả `{ code: "00", desc, data: { checkoutUrl, paymentLinkId, qrCode, status, ... }, signature }`.
- Xem link: `GET /v2/payment-requests/{orderCode}` → `data.status` ∈ `PENDING | PROCESSING | PAID | CANCELLED` (+ `EXPIRED`), `amount`, `amountPaid`.
- Webhook: POST JSON `{ code, desc, success, data: {...}, signature }`; chữ ký = HMAC-SHA256 của các trường trong `data`
  xếp theo tên, `k=v` nối `&`, null → chuỗi rỗng. Trả 2xx là xác nhận. Đăng ký URL ở my.payos.vn hoặc `POST /confirm-webhook`
  (PayOS gửi thử một webhook mẫu khi xác nhận).
- returnUrl/cancelUrl nhận `?code&id&cancel&status&orderCode` – **không có chữ ký**.

## 4. Phương án đã cân nhắc
| Phương án | Ưu | Nhược | Chọn |
|---|---|---|---|
| A. Chỉ webhook cập nhật CSDL (giống IPN VNPay) | Đúng mô hình cũ | Chạy localhost không nhận webhook → demo không bao giờ "đã thanh toán" nếu không có tunnel | |
| **B. Webhook + trang kết quả gọi backend, backend tự hỏi PayOS (`GET /v2/payment-requests/{orderCode}`) rồi cập nhật** | Chạy được ở localhost; không tin tham số trình duyệt; webhook vẫn là đường chính khi triển khai | Thêm một lời gọi API PayOS mỗi lần mở trang kết quả | ✔ |
| C. Dùng SDK `vn.payos:payos-java` | Ít code ký | Thêm phụ thuộc, khó giả lập trong test, SDK đổi API theo phiên bản | |

## 5. Thiết kế
### 5.1 Backend (gói `thanhtoan`)
- `PayOsProperties` (`app.payos.client-id`, `api-key`, `checksum-key`, `api-url`, `return-url`, `cancel-url`); thiếu khóa → 503 "Chưa cấu hình PayOS".
- `ChuKyPayOs`: ký chuỗi tạo link; ký/kiểm `data` webhook (xếp khóa, null → rỗng, so sánh thời gian hằng).
- `PayOsClient` (interface) + `PayOsClientHttp` (Spring `RestClient`): `taoLink(...)`, `layThongTin(orderCode)`. Test thay bằng bean giả.
- API:
  - `POST /api/thanh-toan/payos/{maDonHang}` → `{ duongDan: checkoutUrl }`. Kiểm như cũ (đơn PayOS, chưa trả, còn chờ, chưa quá 30 phút,
    ≤ 99 lần thử). `orderCode = id đơn × 100 + lần thử`, lưu vào `thanh_toan.ma_tham_chieu`. `expiredAt` = lúc đặt + 30 phút.
    `description` = `"TG" + orderCode`, dài hơn 9 ký tự thì lấy 9 ký tự cuối. Lỗi PayOS → 502 "Không tạo được liên kết thanh toán PayOS".
  - `POST /api/thanh-toan/payos/webhook` (công khai): chữ ký sai → bỏ qua; không thấy giao dịch → bỏ qua (webhook mẫu khi xác nhận URL);
    số tiền khác → bỏ qua; giao dịch đã kết thúc → bỏ qua; `data.code == "00"` → thành công. Luôn trả 200 `{ success }` (lỗi bất ngờ trả 500 để PayOS gửi lại).
  - `GET /api/thanh-toan/payos/ket-qua?orderCode=` (công khai): giao dịch còn `cho_xu_ly` thì hỏi PayOS; `PAID` + đủ tiền → ghi thành công;
    `CANCELLED`/`EXPIRED` → ghi thất bại; trả `{ hopLe, thanhCong, trangThai (cho_xu_ly|thanh_cong|that_bai), maDonHang, trangThaiThanhToan, thongBao }`.
  - Ghi nhận thành công dùng chung một hàm (khóa dòng giao dịch, kiểm còn chờ) cho cả webhook và trang kết quả.
- Đổi tên còn lại: `phuongThucThanhToan` `cod | payos`; `huyDonPayOsQuaHan`; thông báo "Đơn PayOS chưa thanh toán, chưa xác nhận được",
  "Đơn 0đ không cần thanh toán PayOS, …", "Quá 30 phút chưa thanh toán PayOS, hệ thống tự hủy"; ghi chú "Đã nhận tiền PayOS sau khi đơn bị hủy – cần hoàn tiền".
- Cấu hình: `PAYOS_CLIENT_ID`, `PAYOS_API_KEY`, `PAYOS_CHECKSUM_KEY`, `PAYOS_RETURN_URL`, `PAYOS_CANCEL_URL` (mặc định `${FRONTEND_URL}/thanh-toan/ket-qua`).
  Profile dev có khóa giả để thử webhook bằng Postman.

### 5.2 CSDL
- `V8__thanh_toan_payos.sql`: bỏ CHECK `don_hang_chk_4`, `thanh_toan_chk_1` (tên do MySQL đặt theo thứ tự cột – cố định khi tạo từ V5/V6),
  `UPDATE … 'vnpay' → 'payos'`, `ma_tham_chieu` của giao dịch cũ đổi sang dạng số `id đơn × 100 + lần`, tạo lại CHECK `IN ('cod','payos')` có tên rõ ràng.
- Dữ liệu mẫu `R__du_lieu_mau_van_hanh.sql`: `payos`, `ma_tham_chieu` dạng số, `du_lieu_phan_hoi` theo dạng webhook PayOS.
- Xuất lại `database/DB_TechGear.sql`.

### 5.3 Frontend
- Trang đặt hàng: lựa chọn "PayOS – Chuyển khoản / quét mã QR" (bỏ "Sắp ra mắt"). Đặt đơn PayOS xong gọi tạo link rồi `window.location` sang `checkoutUrl`;
  tạo link lỗi thì về trang đơn với thông báo và nút thanh toán lại.
- Trang mới `/thanh-toan/ket-qua` (đọc `orderCode`, gọi `ket-qua`): thành công / đang xử lý (nút kiểm tra lại) / thất bại hoặc đã hủy (nút thanh toán lại nếu đơn còn chờ).
- Chi tiết đơn của khách (và tra cứu đơn): đơn PayOS chưa trả, còn chờ xác nhận → nút "Thanh toán ngay".
- Nhãn `OrderView` và admin: "Thanh toán qua PayOS", phương thức "PayOS".

### 5.4 Kiểm thử
- Unit: `ChuKyPayOsTest` (chuỗi ký tạo link, chữ ký webhook, null → rỗng, chữ ký sai).
- Tích hợp: `ThanhToanPayOsIT` thay `ThanhToanVnPayIT` (PayOsClient giả): tạo link, lần thử, quá hạn, webhook đúng/sai chữ ký/sai tiền/lặp,
  trang kết quả PAID/CANCELLED, đơn đã hủy mà tiền về. Sửa `DatHangIT`, `AdminDonHangIT`, `KiemThuTichHop`.
- Postman: thư mục Thanh toán theo API mới (webhook ký bằng khóa dev). Thử tay trên trình duyệt với PayOS giả lập ở dev.

### 5.5 Tài liệu
README, `.env.example`, `docker-compose.yml`, `docs/thiet-ke/dac-ta-api.md`, `tu-dien-du-lieu.md`, `dac-ta-yeu-cau.md`, `docs/bao-cao-cho-frontend.md`,
`docs/BAO_CAO_TIEN_DO.md`, `docs/so-do-use-case/*`, các sơ đồ thiết kế (đổi nhãn) và vẽ lại sơ đồ tuần tự thanh toán thành `16_td_thanh_toan_payos`
(drawio + svg) theo luồng: tạo link → PayOS → webhook / trang kết quả hỏi lại PayOS.

## 6. Để người làm tự quyết
Tên lớp/biến chi tiết, bố cục trang kết quả, cách giả lập PayOS khi chạy dev không có khóa thật (khóa giả → tạo link trả 502 là chấp nhận được,
hoặc chế độ giả lập trả link tới chính trang kết quả).

**Đã quyết khi lập kế hoạch:** profile dev bật `app.payos.gia-lap=true` khi chưa đặt khóa thật: `PayOsClient` giả lập trả `checkoutUrl`
là chính trang kết quả (`…/thanh-toan/ket-qua?orderCode=…`) và báo `PAID` khi hỏi lại, để chạy thử trọn luồng trên máy cá nhân.
Đặt `PAYOS_CLIENT_ID/API_KEY/CHECKSUM_KEY` thì tự dùng PayOS thật. Môi trường khác không bao giờ giả lập.
