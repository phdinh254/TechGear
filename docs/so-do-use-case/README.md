# Sơ đồ use case - Website TechGear

| File | Nội dung |
|---|---|
| 01_use_case_tong_quat | Sơ đồ use case tổng quát (toàn hệ thống) |
| 02_uc_xem_tim_kiem_san_pham | Phân rã: xem trang chủ, tìm kiếm/lọc, xem chi tiết sản phẩm |
| 03_uc_gio_hang_dat_hang | Phân rã: giỏ hàng, đặt hàng, chọn phương thức thanh toán (COD, VNPay), tra cứu đơn hàng |
| 04_uc_tai_khoan | Phân rã: đăng ký, đăng nhập, quên mật khẩu, thông tin cá nhân, sổ địa chỉ, đăng xuất |
| 05_uc_don_hang_danh_gia | Phân rã: đơn hàng của tôi, đánh giá, yêu thích |
| 06_uc_qt_san_pham | Phân rã (quản trị): danh mục, thương hiệu, sản phẩm |
| 07_uc_qt_kho_don_hang | Phân rã (quản trị): tồn kho, đơn hàng |
| 08_uc_qt_khuyen_mai_giao_dien | Phân rã (quản trị): mã giảm giá, banner, cấu hình cửa hàng |
| 09_uc_qt_nguoi_dung_thong_ke | Phân rã (quản trị): người dùng, đánh giá, thống kê |

Mỗi sơ đồ có 3 file: `.drawio` (mở bằng [draw.io](https://www.drawio.com/) để sửa), `.drawio.png` (chèn vào Word, vẫn mở lại được bằng draw.io) và `.svg` (ảnh vector, phóng to không vỡ).

## Tác nhân (Actor)

- **Khách vãng lai**: người truy cập chưa đăng nhập; xem, tìm kiếm sản phẩm, quản lý giỏ hàng, đặt hàng, tra cứu đơn hàng, đăng ký, đăng nhập.
- **Khách hàng**: khách đã đăng nhập; kế thừa chức năng của khách vãng lai (mũi tên tổng quát hóa) và có thêm quản lý tài khoản, đơn hàng, đánh giá, yêu thích.
- **Quản trị viên**: quản lý toàn bộ dữ liệu cửa hàng.
- **Cổng thanh toán VNPay** (hệ thống ngoài): xử lý thanh toán trực tuyến.
- **Dịch vụ Email** (hệ thống ngoài): gửi liên kết đặt lại mật khẩu.

## Quy ước quan hệ

- `«include»`: use case cơ sở luôn thực hiện use case được include (VD: Đặt hàng luôn gồm Nhập thông tin giao hàng, Kiểm tra tồn kho, Chọn phương thức thanh toán).
- `«extend»`: use case mở rộng, chỉ xảy ra trong một số trường hợp (VD: Áp dụng mã giảm giá mở rộng Đặt hàng; Quên mật khẩu mở rộng Đăng nhập).
- Mũi tên tam giác rỗng: tổng quát hóa – giữa tác nhân (Khách hàng kế thừa Khách vãng lai) hoặc giữa use case
  (Thanh toán khi nhận hàng và Thanh toán qua VNPay là hai cách Chọn phương thức thanh toán).

## Thay đổi so với bản PlantUML trước đây

- Vẽ lại bằng draw.io, bỏ các ô ghi chú; quy tắc nghiệp vụ được thể hiện bằng quan hệ trên sơ đồ.
- Thêm **Tra cứu đơn hàng** (03, 01): khách vãng lai đặt hàng không cần tài khoản nên cần cách xem lại đơn bằng mã đơn và email.
- **Đặt hàng** `«include»` **Chọn phương thức thanh toán**; COD và VNPay là hai trường hợp tổng quát hóa (trước đây vẽ là `«extend»`).
- **Quên mật khẩu** `«extend»` **Đăng nhập** và `«include»` **Gửi liên kết đặt lại mật khẩu qua email**; thêm **Đổi ảnh đại diện** (04).
- Quản trị đơn hàng (07): **Hủy đơn hàng** `«include»` **Hoàn tồn kho và lượt dùng mã giảm giá**;
  **Cập nhật trạng thái đơn hàng** `«include»` **Ghi lịch sử trạng thái**; nhập / điều chỉnh kho `«include»` **Ghi nhật ký tồn kho**.
- Thêm **Xem banner khuyến mãi** (02), **Kiểm tra đơn hàng đã hoàn thành** khi đánh giá (05).
