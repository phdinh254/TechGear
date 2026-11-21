# Ghi chú tiến độ hoàn thiện báo cáo BC_HDV

- File gốc: `docs/BC_HDV.docx` (giữ nguyên, không chỉnh sửa).
- Bản hoàn thiện: `docs/BC_HDV_HoanThien.docx` (63 trang) và `docs/BC_HDV_HoanThien.pdf` (bản xuất từ Microsoft Word để kiểm tra bố cục).
- Nội dung được đối chiếu với mã nguồn tại commit `6598f06` (backend `source/backend`, frontend `source/frontend`, CSDL `database/DB_TechGear.sql`).

## 1. Hiện trạng file gốc

File gốc là **mẫu báo cáo của Khoa**: có bìa chính, bìa phụ, trang Lời cam đoan, Lời cảm ơn, hai trang Nhận xét, các tiêu đề Mục lục, Danh mục chữ viết tắt, Danh mục bảng biểu, Danh mục hình ảnh, Tóm tắt đồ án, và 5 chương chỉ có tiêu đề kèm câu hướng dẫn của mẫu. Chưa có nội dung chuyên môn, chưa có bảng hay hình nào.

## 2. Những phần đã hoàn thiện

| Phần | Trạng thái |
|---|---|
| Bìa chính, bìa phụ, Nhận xét GVHD/GV chấm | Giữ nguyên theo mẫu |
| Lời cam đoan | Đã viết |
| Lời cảm ơn | Đã viết **bản nháp** (đánh dấu vàng để nhóm sửa theo ý mình) |
| Mục lục | Trường TOC tự động (Heading 1–3), đã cập nhật số trang |
| Danh mục chữ viết tắt | Bảng 23 từ viết tắt |
| Danh mục bảng biểu / hình ảnh | Trường tự động lấy từ chú thích (26 bảng, 27 hình) |
| Tóm tắt đồ án | Đã viết |
| Chương 1 – Tổng quan | Đã viết: bối cảnh, lý do, mục tiêu, đối tượng, phạm vi, phương pháp, bố cục |
| Chương 2 – Cơ sở lý thuyết | Đã viết: SOA, REST, mô hình tách frontend–backend, Spring Boot, JPA, Security/JWT, React, Vite, Router, Axios, MySQL, bảng tổng hợp công nghệ |
| Chương 3 – Hiện thực hóa nghiên cứu | Đã viết: phân tích yêu cầu (tác nhân, yêu cầu chức năng có cột trạng thái, phi chức năng, quy tắc nghiệp vụ), 9 sơ đồ use case, 3 đặc tả use case, kiến trúc, CSDL (ERD, 24 bảng, chi tiết 4 bảng chính, ràng buộc), sơ đồ lớp, thiết kế API (4 bảng API), hiện thực các chức năng chính kèm sơ đồ hoạt động/tuần tự |
| Chương 4 – Kết quả nghiên cứu | Đã viết: môi trường, 17 trang giao diện, kết quả kiểm thử đơn vị, kịch bản kiểm thử chức năng, tổng hợp mức độ hoàn thành. **Còn thiếu ảnh chụp màn hình** |
| Chương 5 – Kết luận và hướng phát triển | Đã viết: kết luận, hạn chế, hướng phát triển |
| Tài liệu tham khảo | 14 tài liệu (tài liệu chính thức của công nghệ, RFC 7519, luận án của Fielding, OWASP) |
| Phụ lục | Hướng dẫn cài đặt, biến môi trường, tài khoản mẫu |

## 3. Định dạng đã áp dụng

- Giữ quy định của mẫu Khoa: khổ A4; lề trái 3 cm, phải 2 cm, trên/dưới 2 cm (bìa giữ lề và khung viền của mẫu); chương đánh số tự động "CHƯƠNG n:".
- Nội dung: Times New Roman 13pt, màu đen, căn đều hai bên, giãn dòng 1.5, cách đoạn trước/sau 6pt. Dòng đầu thụt 1 cm (first line), từ dòng thứ hai trở đi sát lề trái.
- Không in đậm tùy ý: chữ đậm chỉ dùng cho tiêu đề chương/mục, các tiêu đề của mẫu (Lời cam đoan, Mục lục, Danh mục…), dòng tiêu đề bảng, tiêu đề Phụ lục A/B/C và chữ ký. Chú thích bảng/hình in nghiêng (theo style Caption của mẫu); đoạn văn, gạch đầu dòng, ô bảng, dòng nhóm trong bảng, nhãn "Từ khóa:" và ghi chú [CẦN BỔ SUNG] đều không in đậm.
- Gạch đầu dòng và danh sách đánh số: ký hiệu đặt ở vị trí thụt 1 cm như dòng đầu đoạn văn, các dòng tiếp theo sát lề trái.
- Heading 1 (chương): 16pt, in đậm, viết hoa, căn giữa, luôn sang trang mới. Heading 2 (1.1.) sát lề trái; Heading 3 (1.1.1.) thụt vào 0,5 cm so với Heading 2. Các tiêu đề đánh số tự động, luôn đi kèm đoạn văn phía sau (không bị tách khỏi nội dung).
- Bảng: chú thích "Bảng x.y." đặt trên bảng; dòng tiêu đề in đậm, nền xám, lặp lại khi bảng sang trang; bảng vừa đúng bề rộng vùng chữ; hàng không bị tách đôi giữa hai trang.
- Hình: căn giữa, giữ tỷ lệ, chú thích "Hình x.y." (in nghiêng) đặt dưới hình. Số bảng/hình dùng trường SEQ nên tự cập nhật khi thêm hay bớt.
- Mã nguồn: font Consolas, nền xám nhạt; tên lớp, API, bảng CSDL viết bằng font monospace.
- Không dùng header (đã bỏ dòng tên đề tài và đường kẻ phía trên tên chương). Footer: GVHD – SVTH và số trang; phần đầu (Lời cam đoan → Tóm tắt) đánh số La Mã, phần nội dung đánh số 1, 2, 3… từ Chương 1; bìa không có số trang.

## 4. Nguyên tắc nội dung

- Tên đề tài dùng thống nhất đúng như trên bìa: “Xây dựng website bán phụ kiện công nghệ”; “TechGear” chỉ dùng làm tên hệ thống.
- Chỉ ghi "Đã triển khai" cho chức năng có đủ API backend **và** giao diện frontend trong mã nguồn.
- Phân loại rõ: **Đã triển khai** (toàn bộ phân hệ khách hàng), **Đang phát triển** (VNPay: API trả 503, giao diện ghi "Sắp ra mắt"; đăng nhập Google: chỉ có nút; quản trị: mới có API thương hiệu và tải ảnh, chưa có giao diện), **Dự kiến** (các chức năng quản trị còn lại).
- Sơ đồ kiến trúc và use case quản trị là thiết kế mục tiêu; báo cáo đã ghi chú các thành phần chưa hiện thực (VnPayService, trang /admin).
- Không tự tạo số liệu. Kết quả kiểm thử đơn vị (10 lớp, 37 ca, 0 lỗi) lấy từ `source/backend/target/surefire-reports` (lần chạy ngày 20/11/2026); số lượng dữ liệu mẫu lấy từ script SQL.

## 5. Thông tin còn thiếu – cần nhóm xác nhận / bổ sung

Các vị trí này được **tô vàng** trong file Word (12 chỗ `[CẦN BỔ SUNG …]`, 12 ô `[Chưa ghi nhận]`):

1. **Ảnh chụp màn hình giao diện** (11 khung, Hình 4.1 – 4.11): trang chủ, danh sách sản phẩm, chi tiết sản phẩm, giỏ hàng, thanh toán, đặt hàng thành công, tra cứu đơn, đăng nhập/đăng ký, tài khoản, chi tiết đơn hàng, Swagger UI. Thay khung chữ bằng ảnh, **giữ nguyên dòng chú thích** để danh mục hình tự cập nhật.
2. **Cột "Kết quả thực tế" của Bảng 4.4** (12 kịch bản kiểm thử chức năng): chạy thử trên giao diện rồi điền Đạt/Không đạt.
3. **Lời cảm ơn**: bản nháp, nhóm sửa theo ý mình rồi xóa dòng ghi chú vàng.
4. **Ngày ký Lời cam đoan** ("ngày ..... tháng ..... năm 2026").
5. **Tài liệu tham khảo**: bổ sung giáo trình/bài giảng học phần và ngày truy cập tài liệu trực tuyến nếu Khoa yêu cầu.
6. **Chạy lại `mvn test`** trên mã nguồn cuối cùng trước khi nộp (máy hiện tại không có Maven trên PATH nên chưa chạy lại được). Nếu số ca kiểm thử thay đổi, cập nhật Bảng 4.3.
7. Kiểm tra tên trường trên bìa ("TRƯỜNG KỸ THUẬT VÀ CÔNG NGHỆ") và hình thức chương ("CHƯƠNG 1:" theo mẫu, hay "CHƯƠNG 1." nếu Khoa yêu cầu): giữ nguyên theo mẫu gốc.

## 6. Vấn đề chưa thể hoàn thiện do thiếu dữ liệu

- Chưa có ảnh giao diện thật: muốn chụp phải chạy đủ MySQL, backend và frontend; việc import script sẽ xóa và tạo lại CSDL `techgear` trên máy, nên chưa tự thực hiện.
- Chưa có số liệu đánh giá hiệu năng hay khảo sát người dùng, nên báo cáo không đưa nội dung này vào.
- Luồng đánh giá sản phẩm chưa thể chạy trọn vẹn qua giao diện vì chưa có chức năng quản trị chuyển đơn sang "hoàn thành" và duyệt đánh giá (đã nêu trong mục Hạn chế).
- ERD (Hình 3.11) và sơ đồ lớp (Hình 3.12) rất lớn nên khi thu về khổ A4 chữ khá nhỏ. Nếu cần, có thể đặt hai hình này trên trang ngang hoặc tách theo nhóm bảng; bản đầy đủ nằm trong `docs/thiet-ke/`.

## 7. Lưu ý khi chỉnh sửa tiếp trong Word

- Sau khi thêm hoặc sửa nội dung: nhấn `Ctrl+A` rồi `F9` (chọn *Update entire table*) để cập nhật mục lục, danh mục bảng/hình và số thứ tự.
- Thêm chú thích mới: References → Insert Caption, nhãn "Bảng" hoặc "Hình", bật *Include chapter number*; hoặc sao chép một chú thích có sẵn rồi sửa phần tên.
- File .docx khoảng 11 MB vì giữ ảnh sơ đồ ở độ phân giải gốc. Nếu cần giảm dung lượng, dùng File → Compress Pictures trong Word.

## 8. Kết quả đối chiếu báo cáo với mã nguồn (21/11/2026)

Đã đối chiếu từng con số và mô tả trong báo cáo với mã nguồn tại commit `6598f06`.

**Khớp:** 45 endpoint; 16 service; 18 repository; 23 entity (24 bảng); 11 enum; 17 trang React; 24 bảng, 32 khóa ngoại; phiên bản Spring Boot 3.3.5, Java 17, jjwt 0.12.6, React 18.3.1, Vite 5.4.11, React Router 6.28.0, Axios 1.7.9; dữ liệu mẫu (2 tài khoản, 11 danh mục, 6 thương hiệu, 8 sản phẩm, 15 biến thể, 2 mã giảm giá); quy tắc phí vận chuyển 30.000 / 500.000 đ; JWT 24 giờ; mã đặt lại mật khẩu 15 phút, giới hạn 60 giây; sổ địa chỉ tối đa 10; khóa bi quan khi trừ kho; trạng thái VNPay (503), đăng nhập Google (chỉ có nút), quản trị (chỉ API thương hiệu và tải ảnh); 10 lớp kiểm thử, 37 ca.

**Đã sửa:**
- Số controller: 18 → **16**.
- Chương 1: bỏ ví dụ "thiết bị lưu trữ", "thẻ nhớ" (không có trong dữ liệu dự án), thay bằng loa, củ sạc, sạc dự phòng nhiều dung lượng.
- Mục 3.1.1: "cây danh mục hai cấp" → "cây danh mục (cha – con)", vì mã nguồn hỗ trợ nhiều cấp (dữ liệu mẫu có 2 cấp); thêm tùy chọn "độ dài cáp".
- Bảng 2.1: ghi phiên bản springdoc-openapi 2.6.0.
- API `/cai-dat`: bổ sung "ngưỡng miễn phí vận chuyển".
- Bảng 4.2 (trang chủ): mô tả đúng các khối hiện có; banner chỉ hiển thị khi có dữ liệu.
- Bảng 4.1: ghi rõ script mẫu chưa có dữ liệu banner, nên khi chụp ảnh trang chủ cần thêm banner thủ công nếu muốn minh họa.
