# Thanh toán PayOS thay VNPay – Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Thay toàn bộ thanh toán trực tuyến VNPay bằng PayOS ở backend, CSDL, frontend, kiểm thử và tài liệu.

**Architecture:** Gói `thanhtoan` giữ cấu trúc cũ (Controller → Service → Repository JdbcClient). Lời gọi PayOS tách sau interface
`PayOsClient` (bản HTTP dùng `RestClient`, bản giả lập cho dev, bản giả trong test). Ghi nhận thanh toán dùng chung cho webhook và
trang kết quả (backend tự hỏi PayOS). Frontend thêm lựa chọn PayOS, chuyển hướng sang `checkoutUrl`, trang `/thanh-toan/ket-qua`.

**Tech Stack:** Spring Boot 4 / Java 21, Flyway + MySQL 8.4, JUnit 5 + Testcontainers + MockMvc, React 18 + Vite.

**Spec:** `docs/superpowers/specs/2026-11-23-thanh-toan-payos-design.md`

## Global Constraints
- Giá trị liệt kê: `phuongThucThanhToan` / `phuong_thuc` = `cod | payos` (chữ thường). Không còn chuỗi `vnpay`/`VNPay` trong `source/`, `postman/`, `database/`, `README.md`, `docs/` (trừ `docs/BC_HDV*` và spec/plan này).
- Không sửa `V1`–`V7`; thay đổi lược đồ ở `V8__thanh_toan_payos.sql`.
- `orderCode = id đơn × 100 + lần thử` (lần 1–99); lưu dạng chuỗi số trong `thanh_toan.ma_tham_chieu`.
- Link hết hạn 30 phút sau lúc đặt (`ThanhToanService.HAN_THANH_TOAN`); tự hủy sau 35 phút (`DonHangService.HUY_PAYOS_SAU`).
- `description` = `"TG" + orderCode`, > 9 ký tự thì lấy 9 ký tự cuối.
- Chữ ký: HMAC-SHA256, khóa = checksum key, kết quả hex chữ thường.
- Biến môi trường: `PAYOS_CLIENT_ID`, `PAYOS_API_KEY`, `PAYOS_CHECKSUM_KEY`, `PAYOS_API_URL` (mặc định `https://api-merchant.payos.vn`), `PAYOS_RETURN_URL`, `PAYOS_CANCEL_URL` (mặc định `${app.frontend-url}/thanh-toan/ket-qua`).
- Giả lập chỉ ở profile dev và chỉ khi chưa đặt khóa thật (`app.payos.gia-lap`).
- Không commit cho tới khi người dùng yêu cầu; commit thẳng `main`, không dòng Co-Authored-By.
- Không thêm phụ thuộc Maven/npm mới.

## Review Focus
- Webhook mẫu PayOS gửi khi xác nhận URL (orderCode không có trong CSDL) → trả 200, không lỗi, không ghi gì. *(test ở Task 3)*
- Webhook và trang kết quả cùng ghi một giao dịch gần như đồng thời → chỉ ghi một lần, đơn không bị ghi chú "nhận tiền sau khi hủy" sai. *(test lặp webhook ở Task 3)*
- Khách mở lại trang kết quả nhiều lần / F5 sau khi đã thanh toán → luôn hiện thành công, không gọi PayOS nữa. *(test ở Task 3)*
- Tiền về sau khi đơn đã tự hủy → giữ `da_huy`, đánh dấu đã thanh toán, ghi chú cần hoàn tiền. *(test ở Task 3)*
- `orderCode` trên URL kết quả không phải số / không tồn tại → `hopLe=false`, trang hiện thông báo thân thiện, không 500. *(test ở Task 3, xử lý ở Task 5)*

---

### Task 1: Chữ ký PayOS

**Files:**
- Create: `source/backend/src/main/java/com/techgear/thanhtoan/ChuKyPayOs.java`
- Create: `source/backend/src/test/java/com/techgear/thanhtoan/ChuKyPayOsTest.java`
- Delete: `ChuKyVnPay.java`, `ChuKyVnPayTest.java`

**Interfaces — Produces:**
- `static String kyTaoLink(long amount, String cancelUrl, String description, long orderCode, String returnUrl, String khoa)`
- `static String chuoiDuLieu(Map<String, ?> data)` – khóa xếp tăng dần, `k=v` nối `&`, null/`"null"`/`"undefined"` → rỗng, số in không thập phân.
- `static String ky(String chuoi, String khoa)`; `static boolean hopLe(Map<String, ?> data, String chuKy, String khoa)` (so sánh thời gian hằng, khóa rỗng → false).

- [ ] **Step 1: Test** `kyTaoLink_dungChuoiTaiLieu`: chuỗi ký của `amount=2000, cancelUrl=https://a/c, description=TG4301, orderCode=4301, returnUrl=https://a/r` bằng `ky("amount=2000&cancelUrl=https://a/c&description=TG4301&orderCode=4301&returnUrl=https://a/r", khoa)`; `ky` khớp HMAC-SHA256 hex tính bằng `javax.crypto.Mac` ngay trong test.
  `chuoiDuLieu_xepKhoaVaNullThanhRong`: `{orderCode:4301, amount:2000, desc:"ok", counterAccountName:null}` → `"amount=2000&counterAccountName=&desc=ok&orderCode=4301"`.
  `hopLe_saiChuKyHoacKhoaRong`: chữ ký sửa 1 ký tự → false; khóa `""` → false; chữ ký viết hoa của chuỗi đúng → true.
- [ ] **Step 2:** `cd source/backend && mvn -q test -Dtest=ChuKyPayOsTest` → FAIL (lớp chưa có).
- [ ] **Step 3:** Viết `ChuKyPayOs` (final, package-private như `ChuKyVnPay`). Xóa `ChuKyVnPay*`.
- [ ] **Step 4:** chạy lại → PASS.

### Task 2: Cấu hình và PayOsClient

**Files:**
- Create: `thanhtoan/PayOsProperties.java`, `thanhtoan/PayOsClient.java`, `thanhtoan/PayOsClientHttp.java`, `thanhtoan/PayOsClientGiaLap.java`
- Delete: `thanhtoan/VnPayProperties.java`
- Modify: `resources/application.properties:41-45`, `resources/application-dev.properties:15-17`, `source/backend/.env.example:26-30`, chỗ bật `@ConfigurationPropertiesScan`/`@EnableConfigurationProperties` đang nhắc `VnPayProperties` (grep).

**Interfaces — Produces:**
- `record PayOsProperties(String clientId, String apiKey, String checksumKey, String apiUrl, String returnUrl, String cancelUrl, boolean giaLap)` với `boolean daCauHinh()` (= giaLap hoặc đủ 3 khóa).
- `interface PayOsClient { LinkThanhToan taoLink(YeuCauLink yc); ThongTinLink layThongTin(long orderCode); }`
  - `record YeuCauLink(long orderCode, long amount, String description, String returnUrl, String cancelUrl, long expiredAt)`
  - `record LinkThanhToan(String checkoutUrl, String paymentLinkId)`
  - `record ThongTinLink(long orderCode, long amount, long amountPaid, String status)` (`PENDING|PROCESSING|PAID|CANCELLED|EXPIRED`)
  - Lỗi PayOS (HTTP lỗi hoặc `code != "00"`) → `ApiException(HttpStatus.BAD_GATEWAY, "Không tạo được liên kết thanh toán PayOS")` (taoLink) / `"Không kiểm tra được giao dịch PayOS"` (layThongTin).
- `PayOsClientHttp`: `RestClient` tới `apiUrl`, header `x-client-id`, `x-api-key`; body tạo link thêm `signature` từ `ChuKyPayOs.kyTaoLink`.
- `PayOsClientGiaLap`: `taoLink` trả `checkoutUrl = returnUrl + "?orderCode=" + orderCode + "&status=PAID&gia_lap=1"`; `layThongTin` trả `PAID`, `amountPaid = amount` của giao dịch (tra `thanh_toan.so_tien` qua `JdbcClient`).
- Bean chọn theo `app.payos.gia-lap` (`@ConditionalOnProperty`, mặc định `false` → Http).
- Properties: `app.payos.*` theo Global Constraints; dev: `app.payos.gia-lap=${PAYOS_GIA_LAP:true}` và khóa giả `PAYOS_CHECKSUM_KEY:techgear-dev-payos-khong-phai-khoa-that` (để Postman ký webhook). Ghi chú trong `.env.example`: đặt khóa thật thì đặt `PAYOS_GIA_LAP=false`.

- [ ] **Step 1:** Viết các lớp trên; xóa `VnPayProperties`.
- [ ] **Step 2:** `mvn -q compile` → BUILD SUCCESS (lỗi biên dịch còn lại ở `ThanhToanService` sửa ở Task 3 – làm Task 2 và 3 liền nhau, kiểm ở cuối Task 3).

### Task 3: Luồng thanh toán PayOS ở backend + CSDL V8

**Files:**
- Modify: `thanhtoan/ThanhToanService.java`, `thanhtoan/ThanhToanController.java`, `thanhtoan/ThanhToanRepository.java`
- Modify: `donhang/DatHangRequest.java:33`, `donhang/DonHangService.java:42,128-163`, `donhang/DonHangRepository.java:115-160`, `donhang/HuyDonQuaHan.java`, `donhang/AdminDonHangService.java:122-137`, `chung/baomat/BaoMatConfig.java:58,63`
- Create: `resources/db/migration/V8__thanh_toan_payos.sql`
- Test: Create `test/.../thanhtoan/ThanhToanPayOsIT.java` (thay `ThanhToanVnPayIT.java` – xóa), Modify `KiemThuTichHop.java:20-21`, `donhang/DatHangIT.java:119-127,204-207`, `donhang/AdminDonHangIT.java:126-130`

**Interfaces:**
- Consumes: Task 1, Task 2.
- Produces (HTTP, frontend dùng):
  - `POST /api/thanh-toan/payos/{maDonHang}` → `200 { "duongDan": checkoutUrl }`; 404 / 409 (thông báo như VNPay cũ, chữ "VNPay" → "PayOS") / 503 "Chưa cấu hình PayOS" / 502.
  - `POST /api/thanh-toan/payos/webhook` (JSON PayOS) → `200 { "success": true }` cho mọi trường hợp đã xử lý hoặc bỏ qua; 500 khi lỗi bất ngờ.
  - `GET /api/thanh-toan/payos/ket-qua?orderCode=` → `200 KetQua { hopLe, thanhCong, trangThai, maDonHang?, trangThaiThanhToan?, coTheThanhToanLai, thongBao }`;
    `trangThai` ∈ `cho_xu_ly|thanh_cong|that_bai`; `coTheThanhToanLai` = đơn PayOS, chưa trả, còn `cho_xac_nhan`, chưa quá 30 phút.
  - Cả ba công khai trong `BaoMatConfig` (thay hai dòng VNPay).
- Repository: `ghiKetQua(String maThamChieu, boolean thanhCong, String maGiaoDich, String duLieuJson)` khóa dòng (`khoaGiaoDich`), chỉ ghi khi còn `cho_xu_ly`, trả `boolean daGhi`; thành công thì gọi `danhDauDaThanhToan` (ghi chú "Đã nhận tiền PayOS sau khi đơn bị hủy – cần hoàn tiền"). `themGiaoDich` ghi `phuong_thuc = 'payos'`.
- Dịch vụ đơn: enum `PhuongThucThanhToan { cod, payos }`; đổi tên `huyDonVnPayQuaHan→huyDonPayOsQuaHan`, `donVnPayQuaHan→donPayOsQuaHan`, `khoaDonVnPayQuaHan→khoaDonPayOsQuaHan`, `HUY_VNPAY_SAU→HUY_PAYOS_SAU`; thông báo theo spec 5.1.
- V8 (theo thứ tự): `ALTER TABLE don_hang DROP CHECK don_hang_chk_4`; `ALTER TABLE thanh_toan DROP CHECK thanh_toan_chk_1`;
  `UPDATE don_hang SET phuong_thuc_thanh_toan='payos' WHERE phuong_thuc_thanh_toan='vnpay'`; `UPDATE thanh_toan SET phuong_thuc='payos'`;
  `UPDATE thanh_toan tt JOIN don_hang dh ON dh.id=tt.don_hang_id SET tt.ma_tham_chieu = CAST(dh.id*100 + CAST(RIGHT(tt.ma_tham_chieu,2) AS UNSIGNED) AS CHAR)`;
  thêm `CONSTRAINT ck_don_hang_phuong_thuc CHECK (phuong_thuc_thanh_toan IN ('cod','payos'))`, `CONSTRAINT ck_thanh_toan_phuong_thuc CHECK (phuong_thuc IN ('cod','payos'))`.
  Chú thích đầu file giải thích tên `_chk_N` do MySQL sinh từ V5/V6.

- [ ] **Step 1: Test `ThanhToanPayOsIT`** (bean `PayOsClient` giả bằng `@TestConfiguration` + `@Primary`, ghi lại `YeuCauLink`, trả status cấu hình được; `KiemThuTichHop` đặt `app.payos.client-id=TEST`, `api-key=TEST`, `checksum-key=khoa-kiem-thu-payos`, `gia-lap=false`):
  - `taoLink_traCheckoutUrlVaGhiGiaoDich`: đơn 600.000 → `duongDan` = URL giả; `YeuCauLink.orderCode == idDon*100+1`, `amount == 600000`, `description == "TG"+orderCode` (≤ 9), `expiredAt == epochSecond(created_at)+1800`; bảng `thanh_toan` 1 dòng `payos`, `cho_xu_ly`.
  - `taoLinkLanHai_tangSoLanThu`: gọi hai lần → orderCode kết thúc `01`, `02`.
  - `taoLink_donCod409`, `taoLink_quaHan409` (lùi `created_at` 31 phút), `taoLink_daThanhToan409`.
  - `webhook_hopLe_danhDauDaThanhToan`: body ký bằng `khoa-kiem-thu-payos` với `data.code="00"`, `amount` đúng → 200 `{success:true}`; giao dịch `thanh_cong`, `ma_giao_dich = data.reference`; đơn `da_thanh_toan`.
  - `webhook_saiChuKy_khongGhi`, `webhook_saiSoTien_khongGhi`, `webhook_orderCodeLa_khongLoi` (orderCode 123 → 200, không ghi gì).
  - `webhook_lapLai_chiGhiMotLan`: gửi cùng body hai lần → lần hai không đổi `thanh_toan_luc`.
  - `ketQua_paid_ghiThanhCong`: client giả trả `PAID` → `thanhCong=true`, `trangThai="thanh_cong"`; gọi lại lần hai → client giả **không** được gọi thêm.
  - `ketQua_cancelled_ghiThatBai_vaChoThanhToanLai`: `CANCELLED` → `trangThai="that_bai"`, `coTheThanhToanLai=true`.
  - `ketQua_orderCodeKhongHopLe`: `orderCode=abc` và `orderCode=999999` → 200 `hopLe=false`.
  - `tienVeSauKhiDonTuHuy_giuHuyVaGhiChuHoanTien`: chạy `huyDonPayOsQuaHan()` với đơn quá 35 phút rồi webhook thành công → đơn `da_huy`, `da_thanh_toan`, `ghi_chu_quan_tri` chứa "cần hoàn tiền".
  - Sửa `DatHangIT` (`"payos"`, thông báo đơn 0đ), `AdminDonHangIT` (`"Đơn PayOS chưa thanh toán, chưa xác nhận được"`).
- [ ] **Step 2:** `mvn -q verify -Dit.test=ThanhToanPayOsIT` (cần Docker) → FAIL.
- [ ] **Step 3:** Cài đặt Service/Controller/Repository, V8, đổi tên phía đơn hàng, `BaoMatConfig`. Webhook đọc body `Map<String,Object>`; chữ ký kiểm trên `data`; `maGiaoDich = data.reference`; lưu nguyên body vào `du_lieu_phan_hoi`.
- [ ] **Step 4:** `mvn -q verify` → toàn bộ unit + IT PASS. `git grep -i vnpay -- source/backend` → không còn kết quả.

### Task 4: Dữ liệu mẫu và bản xuất CSDL

**Files:** Modify `resources/db/du-lieu-mau/R__du_lieu_mau_van_hanh.sql`, `database/DB_TechGear.sql`

- [ ] **Step 1:** Trong R__: `'vnpay'→'payos'`; `ma_tham_chieu = CAST(don_id*100 + lần AS CHAR)`; `du_lieu_phan_hoi` = `JSON_OBJECT('code','00'|'01','desc',…,'success',…,'data',JSON_OBJECT('orderCode',…,'amount',tong,'reference',CONCAT('FT',…),'transactionDateTime',DATE_FORMAT(…,'%Y-%m-%d %H:%i:%s'),'currency','VND','code',…))`; ghi chú "hoàn tiền qua VNPay" → "hoàn tiền chuyển khoản". Comment đầu file nói PayOS.
- [ ] **Step 2:** `docker compose down -v && docker compose up -d`, chạy backend dev một lần → khởi động không lỗi; `SELECT phuong_thuc_thanh_toan, count(*) FROM don_hang GROUP BY 1` chỉ có `cod`, `payos`; khối tự kiểm (SIGNAL) không báo lỗi.
- [ ] **Step 3:** Xuất lại `database/DB_TechGear.sql` bằng lệnh ghi ở đầu file hiện tại (giữ nguyên phần chú thích đầu, cập nhật ngày); `grep -ci vnpay database/DB_TechGear.sql` → 0.
- Lưu ý: CSDL dev của người dùng đã chạy V1–V7 sẽ được V8 chuyển đổi khi chạy backend; xác nhận bằng cách chạy backend trên volume cũ **trước** khi `down -v` (Step 2) – ghi lại kết quả.

### Task 5: Frontend

**Files:**
- Create: `source/frontend/src/api/paymentApi.js`, `source/frontend/src/pages/PaymentResultPage.jsx`
- Modify: `src/App.jsx` (route `thanh-toan/ket-qua`), `src/pages/CheckoutPage.jsx:41,123-150,233-245`, `src/pages/OrderDetailPage.jsx:80-90`, `src/pages/OrderLookupPage.jsx:58`, `src/components/OrderView.jsx:105`, `src/admin/utils/nhan.jsx` (`NHAN_PHUONG_THUC = { cod: 'COD', payos: 'PayOS' }`), CSS trong file style của trang đặt hàng/đơn.

**Interfaces:**
- Consumes: HTTP của Task 3.
- `paymentApi = { taoLink(maDonHang) → Promise<{duongDan}>, ketQua(orderCode) → Promise<KetQua> }`; helper `chuyenSangPayOs(maDonHang)` gọi `taoLink` rồi `window.location.assign(duongDan)`.

- [ ] **Step 1:** Trang đặt hàng: hai `radio-card` chọn được (`cod`, `payos`) – PayOS: "Chuyển khoản / quét mã QR qua PayOS", phụ đề "Thanh toán ngay, đơn được xác nhận nhanh hơn". Đặt đơn `payos` thành công → `xoaHet()` rồi `chuyenSangPayOs`; lỗi tạo link → hiện màn "Đặt hàng thành công" kèm cảnh báo và nút "Thanh toán ngay".
- [ ] **Step 2:** `PaymentResultPage`: đọc `orderCode` từ query; gọi `ketQua`; ba trạng thái: thành công (icon check, mã đơn, link xem đơn nếu đăng nhập / tra cứu đơn), đang xử lý (nút "Kiểm tra lại"), thất bại/hủy (nút "Thanh toán lại" khi `coTheThanhToanLai`); `hopLe=false` → "Không tìm thấy giao dịch".
- [ ] **Step 3:** `OrderDetailPage` & `OrderLookupPage`: đơn `payos` + `chua_thanh_toan` + `cho_xac_nhan` → nút "Thanh toán ngay" (qua `hanhDong` của `OrderView`). `OrderView` hiện "Thanh toán qua PayOS".
- [ ] **Step 4:** `npx vite build` → không lỗi. Chạy dev (giả lập bật): đặt đơn PayOS → về `/thanh-toan/ket-qua` hiện thành công; admin thấy đơn "Đã thanh toán", phương thức "PayOS". Chụp màn hình trang đặt hàng, trang kết quả (cả ba trạng thái – trạng thái hủy thử bằng `?orderCode=<mã>&status=CANCELLED` sau khi đặt `PAYOS_GIA_LAP` trả CANCELLED tạm thời hoặc mở với orderCode chưa trả), nút thanh toán lại.
- [ ] **Step 5:** `git grep -i vnpay -- source/frontend` → không còn.

### Task 6: Postman, README, cấu hình chạy

**Files:** `postman/TechGear.postman_collection.json`, `postman/TechGear.local.postman_environment.json` (nếu có biến VNPay), `README.md`, `docker-compose.yml`, `source/backend/.env.example` (đã sửa ở Task 2 – kiểm lại)

- [ ] **Step 1:** Postman thư mục Thanh toán: tạo link (giả lập trả URL), webhook có pre-request script ký HMAC-SHA256 `data` bằng khóa dev (`CryptoJS.HmacSHA256`), webhook sai chữ ký, kết quả theo orderCode. Đổi các request đặt hàng `vnpay→payos` và thông báo kỳ vọng.
- [ ] **Step 2:** `npx -y newman@6 run postman/TechGear.postman_collection.json -e postman/TechGear.local.postman_environment.json` trên CSDL mẫu mới (backend dev đang chạy) → 0 failed.
- [ ] **Step 3:** README: mục thanh toán PayOS (giả lập ở dev, cách đặt khóa thật, đăng ký webhook `https://<miền>/api/thanh-toan/payos/webhook`, localhost cần tunnel cho webhook nhưng trang kết quả vẫn tự xác nhận); danh sách API; `docker-compose.yml` biến môi trường PayOS cho service backend.

### Task 7: Tài liệu thiết kế và sơ đồ

**Files:** `docs/thiet-ke/dac-ta-api.md` (mục 3.4–3.5, 3.7, 4), `tu-dien-du-lieu.md`, `dac-ta-yeu-cau.md`, `docs/bao-cao-cho-frontend.md`, `docs/BAO_CAO_TIEN_DO.md`, `docs/so-do-use-case/{README.md, 01_*.drawio/svg, 03_*.drawio/svg, 10_uc_nguoi_dung.puml/svg}`, `docs/thiet-ke/{02_kien_truc_tong_the, 03_so_do_goi, 12_hd_dat_hang(svg), 15_tt_don_hang, 17_tt_thanh_toan}.drawio/svg`; Delete `16_td_thanh_toan_vnpay.*`, Create `16_td_thanh_toan_payos.drawio` + `.svg`; cập nhật mọi liên kết tới file 16.

- [ ] **Step 1:** Đặc tả API mục 3.5 viết lại theo Task 3 (bảng API, KetQua, quy tắc webhook/kết quả, giả lập dev); các mục khác đổi `vnpay→payos` và thông báo.
- [ ] **Step 2:** Sơ đồ: đổi nhãn (`VNPay`→`PayOS`, `VnPayService`→`PayOsClient`, `IPN`→`Webhook`) trong `.drawio` và `.svg` tương ứng; giữ bố cục.
- [ ] **Step 3:** Sơ đồ tuần tự 16 mới (lifeline: Khách, Frontend, ThanhToanController, ThanhToanService, PayOsClient, CSDL, PayOS): tạo link → chuyển hướng → (a) PayOS gọi webhook → kiểm chữ ký/số tiền/còn chờ → ghi; (b) quay về trang kết quả → GET ket-qua → còn chờ thì hỏi PayOS → ghi; xuất `.svg` (drawio CLI nếu có, không thì vẽ SVG tay cùng phong cách sơ đồ khác). Mở svg kiểm tra hiển thị.
- [ ] **Step 4:** `git grep -il vnpay -- . ':!docs/BC_HDV*' ':!docs/superpowers'` → không còn kết quả.

---
Kết thúc: báo người dùng kết quả kiểm thử, ảnh chụp, những gì cần họ làm (tạo tài khoản PayOS, đặt khóa, đăng ký webhook) và hỏi có commit không.
