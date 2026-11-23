# Trang thanh toán nhúng PayOS – Thiết kế (ngắn)

**Mục đích:** khách thanh toán PayOS ngay trên TechGear thay vì bị chuyển sang `pay.payos.vn`, giao diện liền mạch với web.

**Mục tiêu**
- Trang mới `/thanh-toan/:maDonHang`: khung PayOS nhúng (thư viện `@payos/payos-checkout`, `embedded: true`) bên trái; bên phải mã đơn, tổng tiền, đồng hồ đếm ngược tới hạn 30 phút, hướng dẫn 3 bước, nút "Xem đơn hàng". Dưới khung: "Không hiện mã QR? Mở trang PayOS ↗" (mở `duongDan` gốc). Mobile: khung trên, tóm tắt dưới.
- Mọi nút thanh toán (đặt hàng PayOS, "Thanh toán ngay" ở chi tiết đơn / tra cứu đơn) dẫn tới trang này.
- `onSuccess` / `onCancel` của khung → chuyển tới `/thanh-toan/ket-qua/:ma?orderCode=…` (trang kết quả hiện có tự hỏi lại PayOS – tín hiệu từ khung chỉ dùng cho giao diện).
- Tải lại trang không tạo giao dịch mới: nhớ `{orderCode, duongDan}` theo mã đơn trong `sessionStorage`; khi tải lại hỏi `ket-qua`: `cho_xu_ly` → dùng lại link; `thanh_cong` → sang trang kết quả; khác → tạo link mới.
- Hết giờ đếm ngược: gỡ khung, báo "Đơn đã quá hạn thanh toán", nút xem đơn.

**Không làm:** đổi sang COD / hủy đơn từ trang này; liệt kê sản phẩm (khách vãng lai không xem được chi tiết đơn); thay đổi luồng xác nhận ở backend.

**Ràng buộc:** `CHECKOUT_URL` phải dạng `https://pay.payos.vn/web/<id 32 hex>` (thư viện kiểm); khung cao 100% ô chứa nên ô cần chiều cao cố định. Chưa rõ PayOS có yêu cầu đăng ký tên miền để nhúng → thử thật trước (spike) với khóa thật, chặn thì dừng báo lại.

**Cách tiếp cận đã cân nhắc**
- A. Trang riêng `/thanh-toan/:ma` – **đã chọn**: một trang cho mọi nút, chịu được tải lại / quay lại, gửi link được.
- B. Popup trên trang đặt hàng / chi tiết đơn – ít việc hơn nhưng tải lại là mất, chật trên điện thoại.
- (Tự vẽ QR từ dữ liệu PayOS trả về – người dùng chọn nhúng khung PayOS thay vì cách này.)

**Backend (quyết định của Claude):** `POST /api/thanh-toan/payos/{ma}` trả thêm `orderCode`, `maDonHang`, `tongTien`, `hetHan` (ISO-8601) cạnh `duongDan`; IT cập nhật; Postman và đặc tả API mục 3.5 cập nhật.

**Để lại cho người làm:** chiều cao khung (khoảng 560–640px desktop), câu chữ hướng dẫn, kiểu đồng hồ.
