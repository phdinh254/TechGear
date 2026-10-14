# Sơ đồ use case - Website TechGear

| File | Nội dung |
|---|---|
| 01_use_case_tong_quat | Sơ đồ use case tổng quát (toàn hệ thống) |
| 02_uc_xem_tim_kiem_san_pham | Phân rã: xem trang chủ, tìm kiếm/lọc, xem chi tiết sản phẩm |
| 03_uc_gio_hang_dat_hang | Phân rã: giỏ hàng, đặt hàng, thanh toán (COD, VNPay) |
| 04_uc_tai_khoan | Phân rã: đăng ký, đăng nhập, quên mật khẩu, thông tin cá nhân, sổ địa chỉ |
| 05_uc_don_hang_danh_gia | Phân rã: đơn hàng của tôi, đánh giá, yêu thích |
| 06_uc_qt_san_pham | Phân rã (quản trị): danh mục, thương hiệu, sản phẩm |
| 07_uc_qt_kho_don_hang | Phân rã (quản trị): tồn kho, đơn hàng |
| 08_uc_qt_khuyen_mai_giao_dien | Phân rã (quản trị): mã giảm giá, banner, cấu hình cửa hàng |
| 09_uc_qt_nguoi_dung_thong_ke | Phân rã (quản trị): người dùng, đánh giá, thống kê |

Mỗi sơ đồ có 3 file: `.puml` (mã nguồn PlantUML, sửa được), `.png` (chèn vào Word), `.svg` (ảnh vector, phóng to không vỡ).

## Tác nhân (Actor)

- **Khách vãng lai**: người truy cập chưa đăng nhập; xem, tìm kiếm sản phẩm, quản lý giỏ hàng, đặt hàng, đăng ký, đăng nhập.
- **Khách hàng**: khách đã đăng nhập; kế thừa chức năng của khách vãng lai và có thêm quản lý tài khoản, đơn hàng, đánh giá, yêu thích.
- **Quản trị viên**: quản lý toàn bộ dữ liệu cửa hàng.
- **Cổng thanh toán VNPay** (hệ thống ngoài): xử lý thanh toán trực tuyến.
- **Dịch vụ Email** (hệ thống ngoài): gửi mã đặt lại mật khẩu.

## Quy ước quan hệ

- `<<include>>`: use case cơ sở luôn gọi use case được include (VD: Đặt hàng luôn gồm Nhập thông tin giao hàng, Kiểm tra tồn kho).
- `<<extend>>`: use case mở rộng, chỉ xảy ra trong một số trường hợp (VD: Áp dụng mã giảm giá, Thanh toán qua VNPay mở rộng Đặt hàng).
- Mũi tên tam giác rỗng: tổng quát hóa (Khách hàng kế thừa Khách vãng lai).

## Sửa sơ đồ

Mở file `.puml` bằng plugin PlantUML (VS Code / IntelliJ) hoặc dán vào https://www.plantuml.com/plantuml để xem và xuất lại ảnh.
