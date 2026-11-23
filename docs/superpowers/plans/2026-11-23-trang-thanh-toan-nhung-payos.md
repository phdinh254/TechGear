# Trang thanh toán nhúng PayOS – Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Khách thanh toán PayOS trên trang `/thanh-toan/:maDonHang` của TechGear với khung PayOS nhúng.

**Architecture:** Backend `POST /api/thanh-toan/payos/{ma}` trả thêm thông tin tóm tắt. Frontend có trang mới dùng `@payos/payos-checkout` (`embedded: true`); mọi nút thanh toán điều hướng tới trang đó; tín hiệu khung chỉ để điều hướng sang trang kết quả (backend vẫn tự hỏi PayOS).

**Tech Stack:** Spring Boot 4 / JUnit + MockMvc (IT, Testcontainers); React 18 + React Router 6, `node --test`.

**Spec:** `docs/superpowers/specs/2026-11-23-trang-thanh-toan-nhung-payos-design.md`

## Global Constraints

- Hạn thanh toán 30 phút kể từ lúc đặt (`ThanhToanService.HAN_THANH_TOAN`, `HAN_THANH_TOAN_PHUT`).
- Chỉ nhúng khi `duongDan` khớp `^https://pay\.payos\.vn/web/[0-9a-fA-F]{32}/?$`; ngược lại (giả lập dev: link về thẳng trang kết quả) thì `window.location.assign(duongDan)`.
- Không đổi luồng ghi nhận ở backend; trang kết quả giữ nguyên.
- Commit thẳng `main`, không Co-Authored-By Claude, không `docs/BC_HDV*`.

## Review Focus

- Tải lại trang khi giao dịch còn chờ → dùng lại link cũ, số lần thử không tăng (test `chonHanhDongKhiMo`).
- Link đã nhớ nhưng giao dịch đã hủy/thất bại → tạo link mới (test `chonHanhDongKhiMo`).
- Giả lập dev (link không phải pay.payos.vn) → chuyển hướng, không nhúng (test `laLinkNhung`).
- Đồng hồ về 0 → gỡ khung, báo quá hạn (test `giayConLai` âm → 0).
- Tạo link lỗi (đơn đã trả / quá hạn / sai phương thức) → hiện thông báo lỗi backend + nút xem đơn, không treo "Đang tải".

---

### Task 1: Backend trả thông tin tóm tắt khi tạo link

**Files:**
- Modify: `source/backend/src/main/java/com/techgear/thanhtoan/ThanhToanService.java` (`taoLink`)
- Modify: `source/backend/src/main/java/com/techgear/thanhtoan/ThanhToanController.java`
- Test: `source/backend/src/test/java/com/techgear/thanhtoan/ThanhToanPayOsIT.java`

**Interfaces:**
- Produces: `public record LinkThanhToan(String duongDan, long orderCode, String maDonHang, long tongTien, OffsetDateTime hetHan)` lồng trong `ThanhToanService`; `taoLink(String) : LinkThanhToan`; controller trả thẳng record → JSON `{duongDan, orderCode, maDonHang, tongTien, hetHan}`.

- [ ] **Step 1:** Trong `taoLink_traCheckoutUrlVaGhiGiaoDich` thêm kiểm JSON: `$.orderCode == idDon(ma)*100+1`, `$.maDonHang == ma`, `$.tongTien == 600000`, `$.hetHan` là thời điểm `created_at + 30 phút` (so `OffsetDateTime.parse(...).toEpochSecond() == datLuc.toEpochSecond()+1800`).
- [ ] **Step 2:** `mvn -q test-compile` (IT cần Docker: `mvn -q verify -Dit.test=ThanhToanPayOsIT`) → FAIL.
- [ ] **Step 3:** Cài đặt; xóa `record DuongDan` trong controller.
- [ ] **Step 4:** `mvn -q verify -Dit.test=ThanhToanPayOsIT` → PASS; `mvn -q test` → PASS.

### Task 2: Quy tắc trang thanh toán (thuần JS, có test)

**Files:**
- Modify: `source/frontend/src/utils/thanhToan.js`
- Test: `source/frontend/src/utils/thanhToan.test.js`

**Interfaces:**
- Produces: `laLinkNhung(url: string) : boolean`; `giayConLai(hetHanIso: string, bayGio = Date.now()) : number` (≥ 0, làm tròn xuống); `dinhDangDongHo(giay: number) : string` ("mm:ss"); `chonHanhDongKhiMo(daNho: {orderCode, duongDan} | null, ketQua: KetQua | null) : 'dung_lai' | 'tao_moi' | 'sang_ket_qua'` – `daNho` null → `tao_moi`; `ketQua.trangThai==='cho_xu_ly'` → `dung_lai`; `'thanh_cong'` → `sang_ket_qua`; còn lại → `tao_moi`.

- [ ] **Step 1: Test** – `laLinkNhung('https://pay.payos.vn/web/' + 'a'.repeat(32))` true; `.../web/gia-lap-1` false; `http://localhost:5173/thanh-toan/ket-qua/X?orderCode=1` false. `giayConLai('2026-11-23T10:30:00+07:00', Date.parse('2026-11-23T10:00:00+07:00'))===1800`, quá hạn → 0. `dinhDangDongHo(1799)==='29:59'`, `dinhDangDongHo(5)==='00:05'`. Bốn nhánh `chonHanhDongKhiMo`.
- [ ] **Step 2:** `node --test src/utils/thanhToan.test.js` → FAIL.
- [ ] **Step 3:** Cài đặt.
- [ ] **Step 4:** chạy lại → PASS.

### Task 3: Trang `/thanh-toan/:maDonHang` và nối các nút

**Files:**
- Create: `source/frontend/src/pages/PaymentPage.jsx`, `source/frontend/src/styles/payment.css`
- Modify: `source/frontend/src/App.jsx` (route `thanh-toan/:maDonHang` đặt **sau** `thanh-toan/ket-qua*`), `source/frontend/src/api/paymentApi.js` (`taoLink` trả object mới; bỏ `chuyenSangPayOs`; thêm `luuLink(ma, {orderCode, duongDan})`, `docLink(ma)` dùng `sessionStorage` khóa `payos:<ma>`), `source/frontend/src/components/PayNowButton.jsx` (thành `Link` tới `/thanh-toan/<ma>`), `source/frontend/src/pages/CheckoutPage.jsx` (đặt đơn payos → `navigate('/thanh-toan/' + ma, { replace: true })`).
- Dependency: `npm i @payos/payos-checkout@1.0.9`.

- [ ] **Step 1:** Cài thư viện; viết trang: khi mở → `docLink` → nếu có thì `paymentApi.ketQua` → `chonHanhDongKhiMo` → `dung_lai` / `taoLink`+`luuLink` / `navigate` sang kết quả. `laLinkNhung` sai → `window.location.assign`. Đúng → `usePayOS({RETURN_URL: origin+'/thanh-toan/ket-qua/'+ma, ELEMENT_ID:'payos-khung', CHECKOUT_URL, embedded:true, onSuccess/onCancel: e => navigate('/thanh-toan/ket-qua/'+ma+'?orderCode='+orderCode)})`, `open()` sau khi div đã render, `exit()` khi unmount/hết giờ. Bố cục 2 cột (khung trái cao ~600px; phải: mã đơn, tổng tiền, đồng hồ, 3 bước, "Xem đơn hàng"); dưới khung link "Không hiện mã QR? Mở trang PayOS ↗" (`target=_blank rel=noopener`). Lỗi tạo link → `ErrorMessage` + nút xem đơn. Mobile ≤ 900px xếp dọc.
- [ ] **Step 2:** `npx vite build` → không lỗi; `node --test src/utils/*.test.js src/admin/utils/*.test.js` → PASS.
- [ ] **Step 3:** Thử thật (backend dev + khóa thật): đặt đơn PayOS nhỏ → trang hiện khung QR PayOS; tải lại → cùng orderCode (DB `thanh_toan` chỉ 1 dòng cho đơn); bấm hủy trong khung → trang kết quả "chưa thành công" + "Thanh toán lại". Nếu khung báo lỗi tên miền → dừng, báo người dùng.

### Task 4: Tài liệu

- [ ] **Step 1:** `docs/thiet-ke/dac-ta-api.md` mục 3.5: response tạo link mới; Postman request tạo link: test kiểm `orderCode`, `hetHan`; README mục PayOS: một câu về trang nhúng.
- [ ] **Step 2:** Commit lên `main`.
