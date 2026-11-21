# TECHGEAR ADMIN – Đợt 1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Khu quản trị `/admin/*` cho Danh mục, Thương hiệu, Sản phẩm (kèm tùy chọn / biến thể tự sinh) và Tồn kho. Mọi thao tác phản ánh ngay lên website khách.

**Architecture:** Backend thêm 3 controller `/api/admin/{danh-muc,san-pham,ton-kho}` và sửa API thương hiệu có sẵn. Mỗi controller có một service riêng. Phần đồng bộ biến thể tách thành lớp thuần `BienTheDongBo` (không dùng repository) để kiểm thử đơn vị. Frontend thêm thư mục `src/admin/`, tải lười bằng `React.lazy` trong cùng ứng dụng React. Các hàm thuần (sinh tổ hợp, gợi ý SKU) được kiểm thử bằng `node --test`, không thêm thư viện nào.

**Tech Stack:** Spring Boot 3.3.5 / Java 17 / Spring Data JPA (Hibernate 6.5) / JUnit 5 + Mockito; React 18 + Vite 5 + React Router 6 + Axios; MariaDB 10.4 (XAMPP).

**Spec:** `docs/superpowers/specs/2026-11-21-techgear-admin-dot-1-design.md`

## Global Constraints

- **Không đổi cấu trúc CSDL.** Không thêm Docker. Không thêm dependency vào `pom.xml` hay `package.json`.
- Quy ước:
  - API quản trị nằm dưới `/api/admin/**` (đã chặn `hasRole("QUAN_TRI")`).
  - Đường dẫn tiếng Việt không dấu; JSON dùng camelCase tiếng Việt.
  - DTO là `record` có `@Valid`, thông báo lỗi tiếng Việt.
  - Lỗi nghiệp vụ ném `ApiException` (400 `yeuCauKhongHopLe`, 404 `khongTimThay`, 409 `xungDot`).
- Kiểm tra trùng `slug` / `sku` phải tính cả bản ghi đã xóa mềm.
- Sản phẩm không có tùy chọn vẫn có đúng **một biến thể mặc định** không gắn giá trị.
- Giá biến thể `NULL` nghĩa là lấy giá sản phẩm.
- Mọi thay đổi `so_luong_ton` phải có một dòng `lich_su_ton_kho` đi kèm, người thực hiện là admin đang đăng nhập.
- Giới hạn: tối đa **3 tùy chọn** và **50 biến thể** mỗi sản phẩm. Cây danh mục 2 cấp.
- **Git:**
  - Commit thẳng vào `main`, không thêm dòng `Co-Authored-By`.
  - Chỉ `git add` đúng file của task. Đang có thay đổi chưa commit không thuộc kế hoạch (`RegisterPage.jsx`, `README.md`, `.gitignore`, `docs/BAO_CAO_TIEN_DO.md`, `mua-sam-an-tam.jpg`): không đưa vào commit của task.
  - Không bao giờ add `docs/BC_HDV*`.
- **Lệnh chạy backend** (Maven không có trong PATH), chạy trong `source/backend`:
  ```powershell
  & "$env:USERPROFILE\.m2\wrapper\dists\apache-maven-3.9.14\ed7edd442f634ac1c1ef5ba2b61b6d690b5221091f1a8e1123f5fadcc967520d\bin\mvn.cmd" -q test
  ```
  Kế hoạch viết tắt lệnh này là `MVN test`. Thêm `-Dtest=TenTest` để chạy một lớp test.
- **Lệnh chạy frontend** (trong `source/frontend`): `npm run build`; kiểm thử hàm thuần: `node --test src/admin/utils/`.

## Review Focus

1. **Bỏ một giá trị rồi thêm lại đúng giá trị đó, giữ nguyên SKU gợi ý.**
   - Người dùng mong lưu được bình thường.
   - Rủi ro: biến thể cũ đã xóa mềm vẫn giữ SKU và ràng buộc UNIQUE gây 409.
   - Cách xử lý: biến thể bị xóa mềm được đổi SKU thành `sku#id` (giải phóng SKU), và SKU hợp lệ không được chứa `#`.
   - Test: Task 4 `skuCuaBienTheBiXoaDuocGiaiPhong`, Task 5 `skuDangDungOSanPhamKhacBiTuChoi`.
2. **Khách đang có biến thể (hoặc sản phẩm) trong giỏ hoặc danh sách yêu thích thì admin xóa nó.**
   - Giỏ hàng và yêu thích của khách vẫn phải mở được, không lỗi 500.
   - Cách xử lý: xóa các dòng giỏ hàng / yêu thích trong cùng giao dịch.
   - Test: Task 5 `xoaBienTheGoKhoiGioHang`, `xoaSanPhamGoGioHangVaYeuThich`.
3. **Sản phẩm thuộc danh mục con mà danh mục cha bị ẩn.**
   - Sản phẩm phải biến mất khỏi cửa hàng: không xem chi tiết được, không thêm vào giỏ được, không đặt hàng được.
   - Test: Task 1 `sanPhamThuocDanhMucChaAnKhongConBan`, `themVaoGioSanPhamDanhMucAnBiChan`.
4. **Điều chỉnh tồn kho về đúng số đang có, hoặc nhập số âm / 0.**
   - Phải trả 400, không ghi dòng lịch sử nào.
   - Test: Task 6 `dieuChinhKhongChenhLechBiTuChoi`; `@Min` trên DTO.
5. **Bật hiển thị cho sản phẩm chưa có ảnh bằng nút ẩn/hiện nhanh** (đi đường PATCH, không qua form).
   - Phải trả 400 giống khi lưu form.
   - Test: Task 5 `batHienThiSanPhamKhongAnhBiTuChoi`.

**Cần kiểm tra thủ công (không viết test tự động):** khách mở lại một đơn hàng cũ có sản phẩm hoặc biến thể đã bị xóa mềm. `DonHangResponse` gọi `item.getSanPham().getSlug()`. Hibernate có thể không áp `@SQLRestriction` khi nạp quan hệ `@ManyToOne`, nên có thể không lỗi. Kiểm thủ công trong Task 12. Nếu ra 500, thêm `@NotFound(action = NotFoundAction.IGNORE)` cho `ChiTietDonHang.sanPham` và `ChiTietDonHang.bienThe`.

---

## Cấu trúc file

**Backend** (`source/backend/src/main/java/com/techgear/`):

| File | Việc |
|---|---|
| `entity/SanPham.java` (sửa) | thêm `dangBan()` (sản phẩm + danh mục + danh mục cha đều hiển thị) |
| `service/SanPhamService.java`, `GioHangService.java`, `DatHangService.java`, `YeuThichService.java` (sửa) | dùng quy tắc "đang bán" mới |
| `util/SkuUtil.java` (mới) | `giaiPhong(sku, id)` |
| `dto/admin/*.java` (mới) | DTO quản trị (gói `com.techgear.dto.admin`) |
| `service/admin/AdminDanhMucService.java` | CRUD danh mục |
| `service/ThuongHieuService.java` (sửa) | chặn xóa khi còn sản phẩm, đếm sản phẩm |
| `service/admin/BienTheDongBo.java` | logic thuần: kiểm tra và đồng bộ tùy chọn / biến thể |
| `service/admin/AdminSanPhamService.java` | danh sách, chi tiết, lưu, ẩn/hiện, xóa sản phẩm |
| `service/admin/AdminTonKhoService.java` | danh sách tồn, nhập kho, điều chỉnh, lịch sử |
| `controller/AdminDanhMucController.java`, `AdminSanPhamController.java`, `AdminTonKhoController.java` (mới) | REST |
| `repository/*` (sửa) | các truy vấn bổ sung nêu trong từng task |

**Frontend** (`source/frontend/src/`):

| File | Việc |
|---|---|
| `App.jsx` (sửa) | route `/admin/*` lazy |
| `components/Header.jsx` (sửa) | mục "Trang quản trị" |
| `styles/admin.css` (mới) | toàn bộ style admin, import trong `AdminLayout.jsx` (không đưa vào `index.css`, để tải lười theo) |
| `admin/AdminApp.jsx` | `<Routes>` con của khu admin |
| `admin/AdminLayout.jsx` | menu trái + `<Outlet/>` |
| `admin/api/adminApi.js` | mọi lời gọi API admin |
| `admin/components/ConfirmDialog.jsx`, `Modal.jsx`, `ImageUploader.jsx`, `DataTable.jsx`, `VariantEditor.jsx`, `StockDialog.jsx` | component dùng chung |
| `admin/pages/DanhMucPage.jsx`, `ThuongHieuPage.jsx`, `SanPhamListPage.jsx`, `SanPhamFormPage.jsx`, `TonKhoPage.jsx` | trang |
| `admin/utils/bienThe.js` + `bienThe.test.js` | hàm thuần + test `node:test` |

---

### Task 1: Quy tắc "đang bán" tính cả danh mục (website khách)

**Files:**
- Modify: `entity/SanPham.java`, `service/SanPhamService.java`, `service/GioHangService.java:118,160`, `service/DatHangService.java:130-131`, `service/YeuThichService.java:31,48`, `repository/SanPhamRepository.java`
- Test: `src/test/java/com/techgear/service/SanPhamServiceTest.java`, `src/test/java/com/techgear/entity/SanPhamDangBanTest.java` (mới)

**Interfaces:**
- Produces:
  - `public boolean SanPham.dangBan()`: true khi `hienThi`, `danhMuc.hienThi` và (`danhMuc.danhMucCha == null` hoặc `danhMucCha.hienThi`) đều đúng.
  - `static Specification<SanPham> SanPhamService.dieuKienDangBan()`: cùng điều kiện ở dạng Criteria, dùng `join("danhMuc")` và `join("danhMucCha", JoinType.LEFT)`.

- [ ] **Step 1: Viết test thất bại** `SanPhamDangBanTest`:
  - `sanPhamVaDanhMucHienThiThiDangBan`: kết quả `true`.
  - `danhMucAnThiKhongBan`: kết quả `false`.
  - `sanPhamThuocDanhMucChaAnKhongConBan`: con hiển thị, cha `hienThi=false`, kết quả `false`.

  Trong `SanPhamServiceTest` thêm:
  - `chiTietSanPhamThuocDanhMucAnTra404`: `findBySlugAndHienThiTrue` trả về sản phẩm có danh mục ẩn → `ApiException` 404 với thông báo `"Không tìm thấy sản phẩm"`, và `verify(sanPhamRepository, never()).tangLuotXem(any())`.

  Tạo `GioHangServiceTest` (mới), lấy constructor từ `GioHangService`:
  - `themVaoGioSanPhamDanhMucAnBiChan`: thêm vào giỏ một biến thể có sản phẩm hiển thị nhưng danh mục ẩn → 404 `"Sản phẩm không còn được bán"`.
- [ ] **Step 2: Chạy** `MVN test -Dtest=SanPhamDangBanTest,SanPhamServiceTest,GioHangServiceTest`. Kết quả mong đợi: FAIL (không biên dịch được vì chưa có `dangBan`).
- [ ] **Step 3: Cài đặt.**
  - Thêm `SanPham.dangBan()`.
  - `SanPhamService.timKiem`: bỏ `cb.isTrue(root.get("hienThi"))`, thay bằng `dieuKienDangBan().and(spec cũ)`.
  - `layNoiBat`: dùng `sanPhamRepository.findAll(dieuKienDangBan().and(noiBat), PageRequest.of(0, 8, Sort.by(DESC, "id"))).getContent()`, sau đó xóa `findTop8ByHienThiTrueAndNoiBatTrueOrderByIdDesc` khỏi repository.
  - `layChiTiet`: sau khi tìm theo slug, nếu `!sp.dangBan()` thì ném 404 trước khi tăng lượt xem.
  - `GioHangService` (2 chỗ), `DatHangService.java:131`, `YeuThichService` (2 bộ lọc): thay `getHienThi()` của sản phẩm bằng `dangBan()`.
- [ ] **Step 4: Chạy** `MVN test`. Kết quả mong đợi: tất cả PASS, kể cả test cũ.
- [ ] **Step 5: Commit**
  ```bash
  git add source/backend/src
  git commit -m "Ẩn sản phẩm khỏi cửa hàng khi danh mục hoặc danh mục cha bị ẩn"
  ```

---

### Task 2: API quản trị danh mục

**Files:**
- Create: `dto/admin/AdminDanhMucRequest.java`, `dto/admin/AdminDanhMucResponse.java`, `service/admin/AdminDanhMucService.java`, `controller/AdminDanhMucController.java`
- Modify: `repository/DanhMucRepository.java`, `repository/SanPhamRepository.java`
- Test: `src/test/java/com/techgear/service/admin/AdminDanhMucServiceTest.java`

**Interfaces:**
- `AdminDanhMucRequest`:
  - `@NotBlank @Size(max=100) String ten`
  - `Long danhMucChaId`
  - `String moTa`
  - `@Size(max=255) String hinhAnh`
  - `@NotNull @Min(0) Integer thuTu`
  - `Boolean hienThi` (null = true)
- `AdminDanhMucResponse(Long id, String ten, String slug, Long danhMucChaId, String moTa, String hinhAnh, Integer thuTu, Boolean hienThi, long soSanPham, List<AdminDanhMucResponse> danhMucCons)`
- Repository:
  - `DanhMucRepository.existsBySlugAndIdNot(String, Long)`
  - `DanhMucRepository.long countByDanhMucChaId(Long)`
  - `SanPhamRepository.long countByDanhMucId(Long)`
  - `SanPhamRepository.@Query("SELECT s.danhMuc.id, COUNT(s) FROM SanPham s GROUP BY s.danhMuc.id") List<Object[]> demTheoDanhMuc()`
- Service:
  - `List<AdminDanhMucResponse> layCay()`: gồm cả danh mục ẩn, sắp theo `thuTu`.
  - `tao(req)`, `capNhat(id, req)`, `void xoa(id)`.
- Controller: `GET/POST /api/admin/danh-muc`, `PUT/DELETE /api/admin/danh-muc/{id}` (POST trả 201, DELETE trả 204).

- [ ] **Step 1: Viết test thất bại** (Mockito, mock `DanhMucRepository` + `SanPhamRepository`):
  - `taoTrungTenBiTuChoi`: `existsBySlug("tai-nghe")` = true → 409 `"Danh mục đã tồn tại"`.
  - `chonChaKhongPhaiGocBiTuChoi`: cha có `danhMucCha != null` → 400 `"Chỉ chọn danh mục cấp gốc làm danh mục cha"`.
  - `danhMucCoConKhongTheThanhCon`: sửa danh mục có `countByDanhMucChaId(id) > 0` và gán cha → 400 `"Danh mục đang có danh mục con nên không thể làm danh mục con"`.
  - `khongTheLaChaCuaChinhNo`: `danhMucChaId == id` → 400.
  - `xoaDanhMucConConBiChan`: `countByDanhMucChaId > 0` → 409 `"Không thể xóa: danh mục còn danh mục con"`.
  - `xoaDanhMucConSanPhamBiChan`: `countByDanhMucId > 0` → 409 `"Không thể xóa: danh mục còn sản phẩm"`.
  - `suaTenGiuSlugCu`: slug `tai-nghe-2`, tên mới tạo slug gốc `tai-nghe` → slug giữ `tai-nghe-2`.
- [ ] **Step 2: Chạy** `MVN test -Dtest=AdminDanhMucServiceTest`. Kết quả mong đợi: FAIL (thiếu lớp).
- [ ] **Step 3: Cài đặt** service theo đúng mẫu `ThuongHieuService`:
  - Slug tạo bằng `SlugUtil.taoSlugDuyNhat` với `demSlugKeCaDaXoa`; dùng `SlugUtil.cungGoc` để giữ slug cũ khi sửa tên.
  - Xóa bằng `danhMucRepository.delete` (xóa mềm).
  - `soSanPham` lấy từ `demTheoDanhMuc()`.
  - Controller theo mẫu `AdminThuongHieuController`, `@Tag(name = "Quản trị - Danh mục")`.
- [ ] **Step 4: Chạy** `MVN test`. Kết quả mong đợi: PASS.
- [ ] **Step 5: Commit** `git add source/backend/src && git commit -m "Thêm API quản trị danh mục"`

---

### Task 3: Thương hiệu – chặn xóa khi còn sản phẩm, đếm sản phẩm

**Files:**
- Create: `dto/admin/AdminThuongHieuResponse.java`
- Modify: `service/ThuongHieuService.java`, `controller/AdminThuongHieuController.java`, `repository/SanPhamRepository.java`
- Test: `src/test/java/com/techgear/service/ThuongHieuServiceTest.java` (mới)

**Interfaces:**
- `AdminThuongHieuResponse(Long id, String ten, String slug, String logo, String moTa, Boolean hienThi, long soSanPham)`
- `SanPhamRepository`:
  - `long countByThuongHieuId(Long)`
  - `@Query("SELECT s.thuongHieu.id, COUNT(s) FROM SanPham s WHERE s.thuongHieu IS NOT NULL GROUP BY s.thuongHieu.id") List<Object[]> demTheoThuongHieu()`
- `ThuongHieuService.layTatCaChoQuanTri(): List<AdminThuongHieuResponse>`. `GET /api/admin/thuong-hieu` trả kiểu này. `layTatCa()` cũ xóa đi nếu không còn nơi nào gọi.
- Constructor `ThuongHieuService` có thêm `SanPhamRepository`.

- [ ] **Step 1: Viết test thất bại:**
  - `xoaThuongHieuConSanPhamBiChan`: `countByThuongHieuId(5L)` = 2 → 409 `"Không thể xóa: thương hiệu còn sản phẩm"`, `verify(thuongHieuRepository, never()).delete(any())`.
  - `layTatCaChoQuanTriKemSoSanPham`: hai thương hiệu, `demTheoThuongHieu` trả `[[1L, 3L]]` → `soSanPham` lần lượt 3 và 0.
- [ ] **Step 2: Chạy** `MVN test -Dtest=ThuongHieuServiceTest`. Kết quả mong đợi: FAIL.
- [ ] **Step 3: Cài đặt.**
- [ ] **Step 4: Chạy** `MVN test`. Kết quả mong đợi: PASS.
- [ ] **Step 5: Commit** `git commit -m "Chặn xóa thương hiệu còn sản phẩm, trả số sản phẩm cho trang quản trị"` (chỉ add file của task).

---

### Task 4: `BienTheDongBo` – kiểm tra và đồng bộ tùy chọn / biến thể (logic thuần)

Đây là phần khó nhất của Đợt 1. Lớp này **không** dùng repository: nó nhận entity `SanPham` đang có trong bộ nhớ cùng request, sửa đồ thị entity, rồi trả về những gì service cần làm tiếp.

**Files:**
- Create:
  - `dto/admin/AdminSanPhamRequest.java`
  - `service/admin/BienTheDongBo.java`
  - `util/SkuUtil.java`
- Test:
  - `src/test/java/com/techgear/service/admin/BienTheDongBoTest.java`
  - `src/test/java/com/techgear/SkuUtilTest.java`

**Interfaces:**

`AdminSanPhamRequest` là record, với các record lồng:
```java
public record AdminSanPhamRequest(
    @NotBlank @Size(max = 255) String ten,
    @NotNull(message = "Vui lòng chọn danh mục") Long danhMucId,
    Long thuongHieuId,
    @Size(max = 500) String moTaNgan,
    String moTa,
    @NotNull @Positive BigDecimal giaBan,
    @Positive BigDecimal giaGoc,
    @NotNull @Min(0) @Max(120) Integer baoHanhThang,
    Boolean noiBat, Boolean hienThi,
    @Valid List<HinhAnh> hinhAnhs,
    @Valid List<ThongSo> thongSos,
    @Valid @Size(max = 3, message = "Tối đa 3 tùy chọn") List<TuyChon> tuyChons,
    @Valid @NotEmpty @Size(max = 50, message = "Tối đa 50 biến thể") List<BienThe> bienThes) {
  public record HinhAnh(@NotBlank @Size(max = 255) String duongDan, @Size(max = 255) String moTa,
                        boolean laAnhChinh, String tuyChon, String giaTri) {}   // tuyChon+giaTri null = ảnh chung
  public record ThongSo(@NotBlank @Size(max = 100) String ten, @NotBlank @Size(max = 255) String giaTri) {}
  public record TuyChon(Long id, @NotBlank @Size(max = 50) String ten, @NotNull LoaiTuyChon loai,
                        @Valid @NotEmpty List<GiaTri> giaTris) {}
  public record GiaTri(Long id, @NotBlank @Size(max = 50) String giaTri,
                       @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Mã màu dạng #RRGGBB") String maMau) {}
  public record BienThe(@NotNull List<String> giaTris,          // giá trị theo đúng thứ tự tuyChons
                        @NotBlank @Size(max = 100)
                        @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "SKU chỉ gồm chữ, số, dấu chấm, gạch ngang, gạch dưới")
                        String sku,
                        @Positive BigDecimal giaBan, @Positive BigDecimal giaGoc,
                        @Min(0) Integer tonKhoBanDau,
                        @NotNull @Min(0) Integer nguongCanhBao,
                        Boolean dangBan) {}
}
```

`SkuUtil.giaiPhong(String sku, long id): String`
- Trả về `sku + "#" + id`.
- Nếu dài hơn 100 ký tự thì cắt bớt phần `sku` để tổng đúng 100.
- SKU hợp lệ không chứa `#`, nên SKU đã giải phóng không bao giờ trùng SKU mới.

`BienTheDongBo` là `@Component`, không có dependency:
- `void kiemTra(AdminSanPhamRequest req)`: ném `ApiException`, quy tắc ở Step 3.
- `KetQua dongBo(SanPham sp, AdminSanPhamRequest req)`: sửa `sp.getTuyChons()` và `sp.getBienThes()`.
- `record KetQua(List<BienTheSanPham> moi, List<BienTheSanPham> biXoa)`
  - `moi`: biến thể mới, **chưa** gắn vào `sp.getBienThes()`, đã gán `sanPham`, `giaTris`, `soLuongTon = tonKhoBanDau ?: 0`.
  - `biXoa`: biến thể đã bị gỡ khỏi `sp.getBienThes()`. SKU của chúng đã đổi bằng `SkuUtil.giaiPhong`.
- `static String khoa(List<String> giaTris)` và `static String khoa(BienTheSanPham bt)`: khóa so khớp là các chuỗi `tenTuyChon=giaTri`, nối theo thứ tự tùy chọn. Biến thể mặc định có khóa `""`.

- [ ] **Step 1: Viết test thất bại.**

  Dựng `SanPham` trong bộ nhớ có id bằng helper `sanPhamMau()`:
  - tùy chọn `Màu`(id 1) gồm `Đen`(11), `Trắng`(12);
  - biến thể `Đen`(id 101, sku `TG-A-DEN`, tồn 7) và `Trắng`(id 102, sku `TG-A-TRANG`, tồn 0).

  `BienTheDongBoTest`:
  - `giuNguyenIdVaTonKhoKhiToHopConTonTai`: request giữ `Đen`, `Trắng`, đổi giá `Đen` thành 150000 → biến thể 101 vẫn trong `sp.getBienThes()`, `giaBan` 150000, `soLuongTon` 7. `moi` và `biXoa` rỗng.
  - `themGiaTriTaoBienTheMoi`: thêm `Xanh` có `tonKhoBanDau` 5 → `moi.size()==1`, `soLuongTon==5`, `giaTris` chứa entity `Xanh` mới.
  - `boGiaTriXoaMemBienThe`: bỏ `Trắng` → `biXoa` chứa 102 và 102 không còn trong `sp.getBienThes()`; giá trị `Trắng` không còn trong tùy chọn.
  - `skuCuaBienTheBiXoaDuocGiaiPhong`: như trên → `biXoa.get(0).getSku()` bằng `"TG-A-TRANG#102"`.
  - `doiTenGiaTriGiuBienThe`: giá trị id 11 đổi thành `Đen nhám` (request `GiaTri(11L, "Đen nhám", null)`, biến thể `["Đen nhám"]`) → entity 11 có `giaTri` mới; biến thể 101 giữ nguyên; `moi` và `biXoa` rỗng.
  - `giaTriKhongIdTrungTenDungLaiEntityCu`: request `GiaTri(null, "Trắng", null)` → dùng lại entity 12, không có biến thể mới.
  - `chuyenTuMacDinhSangCoTuyChon`: sản phẩm chỉ có biến thể mặc định (id 201, không giá trị, tồn 3), request có `Màu: Đen, Trắng` → `biXoa` = [201], `moi.size()==2`.
  - `chuyenTuCoTuyChonVeMacDinh`: request `tuyChons` rỗng và 1 biến thể `giaTris` rỗng → `biXoa` gồm 101 và 102, `moi.size()==1`.
  - `kiemTraThieuToHopBiTuChoi`: 2 tùy chọn × 2 giá trị nhưng chỉ gửi 3 biến thể → 400 `"Danh sách biến thể phải gồm đủ 4 tổ hợp"`.
  - `kiemTraToHopTrungBiTuChoi` → 400 `"Có tổ hợp biến thể bị trùng"`.
  - `kiemTraVuot50ToHopBiTuChoi`: 3 tùy chọn 4 × 4 × 4 → 400 `"Tối đa 50 biến thể"`.
  - `kiemTraSkuTrungTrongRequest`: hai biến thể `tg-a` và `TG-A` → 409 `"SKU TG-A bị trùng"`.
  - `kiemTraGiaTriKhongThuocTuyChon` → 400 `"Giá trị \"Xám\" không thuộc tùy chọn \"Màu\""`.
  - `kiemTraTenTuyChonTrung` → 400 `"Tên tùy chọn bị trùng: Màu"`.
  - `kiemTraKhongTuyChonPhaiCoDungMotBienThe` → 400 `"Sản phẩm không có tùy chọn phải có đúng 1 biến thể"`.

  `SkuUtilTest`:
  - `giaiPhongThemHauToId`: `"ABC"`, 7 → `"ABC#7"`.
  - `giaiPhongCatBotKhiQuaDai`: chuỗi 100 ký tự, id 123 → độ dài 100 và kết thúc bằng `"#123"`.
- [ ] **Step 2: Chạy** `MVN test -Dtest=BienTheDongBoTest,SkuUtilTest`. Kết quả mong đợi: FAIL.
- [ ] **Step 3: Cài đặt.**

  `kiemTra` thực hiện theo thứ tự:
  1. Tên tùy chọn không trùng (so không phân biệt hoa thường, đã trim).
  2. Giá trị trong mỗi tùy chọn không trùng.
  3. Số tổ hợp không vượt 50.
  4. Không có tùy chọn thì phải có đúng 1 biến thể với `giaTris` rỗng.
  5. Mỗi biến thể có `giaTris.size() == tuyChons.size()`, và từng giá trị thuộc đúng tùy chọn ở vị trí tương ứng.
  6. Khóa không trùng.
  7. Số biến thể bằng tích số giá trị.
  8. SKU không trùng trong request (so không phân biệt hoa thường, thông báo in hoa).

  `dongBo` dùng thuật toán sau:
  ```text
  1. Tùy chọn: với mỗi TuyChon trong request (theo thứ tự i):
       id != null  -> entity có id đó trong sp (không có -> 400 "Tùy chọn không thuộc sản phẩm")
       id == null  -> entity cùng tên (trim, không phân biệt hoa thường) nếu có, ngược lại tạo mới
       gán ten, loai, thuTu = i
     Giá trị trong tùy chọn: cách so khớp giống hệt (id -> trùng tên -> mới); gán giaTri, maMau, thuTu.
     Giá trị / tùy chọn cũ không được so khớp -> gỡ khỏi collection (orphanRemoval xóa cứng; CSDL tự xóa
     bien_the_gia_tri, hinh_anh.gia_tri_tuy_chon_id = NULL).
  2. Biến thể: map khoa(bt) -> bt cho các biến thể hiện có (khóa tính SAU bước 1, nên đổi tên vẫn khớp).
     Với mỗi BienThe trong request: tìm theo khoa(giaTris)
       có  -> cập nhật sku (trim, in hoa), giaBan, giaGoc, nguongCanhBao, hienThi = dangBan != false; KHÔNG đụng soLuongTon
       không -> tạo mới (sku cũng trim, in hoa), thêm vào KetQua.moi
     Biến thể hiện có không được so khớp -> sku = SkuUtil.giaiPhong(sku, id), gỡ khỏi sp.getBienThes(),
     thêm vào biXoa.
  ```

  **Cảnh báo cho người cài đặt:** phải xóa giá trị/tùy chọn cũ *sau* khi đã tính khóa các biến thể hiện có. Nếu không, biến thể sắp bị xóa sẽ mất giá trị trước khi so khớp.
- [ ] **Step 4: Chạy** `MVN test`. Kết quả mong đợi: PASS.
- [ ] **Step 5: Commit** `git commit -m "Thêm logic đồng bộ tùy chọn và biến thể sản phẩm"`

---

### Task 5: API quản trị sản phẩm

**Files:**
- Create:
  - `dto/admin/AdminSanPhamTomTatResponse.java`
  - `dto/admin/AdminSanPhamChiTietResponse.java`
  - `dto/admin/HienThiRequest.java`
  - `service/admin/AdminSanPhamService.java`
  - `controller/AdminSanPhamController.java`
- Modify:
  - `repository/BienTheSanPhamRepository.java`
  - `repository/SanPhamRepository.java`
  - `repository/ChiTietGioHangRepository.java`
  - `repository/YeuThichRepository.java`
- Test: `src/test/java/com/techgear/service/admin/AdminSanPhamServiceTest.java`

**Interfaces:**
- Consumes: `BienTheDongBo.kiemTra/dongBo/KetQua`, `SkuUtil`, `AdminSanPhamRequest`.
- DTO:
  - `AdminSanPhamTomTatResponse(Long id, String ten, String slug, String anhChinh, String danhMucTen, String thuongHieuTen, BigDecimal giaBan, BigDecimal giaGoc, int tongTon, int soBienThe, Boolean noiBat, Boolean hienThi)`
  - `AdminSanPhamChiTietResponse`: các trường giống request, thêm `id`, `slug`. Các record lồng:
    - `HinhAnh(Long id, String duongDan, String moTa, boolean laAnhChinh, String tuyChon, String giaTri)`
    - `ThongSo(String ten, String giaTri)`
    - `TuyChon(Long id, String ten, LoaiTuyChon loai, List<GiaTri> giaTris)`
    - `GiaTri(Long id, String giaTri, String maMau)`
    - `BienThe(Long id, List<String> giaTris, String sku, BigDecimal giaBan, BigDecimal giaGoc, int soLuongTon, int nguongCanhBao, boolean dangBan)`
  - `HienThiRequest(@NotNull Boolean hienThi)`
- Repository:
  - `BienTheSanPhamRepository`:
    - `@Query(value = "SELECT san_pham_id FROM bien_the_san_pham WHERE sku = :sku", nativeQuery = true) List<Long> timSanPhamIdTheoSkuKeCaDaXoa(String sku)`
    - `@Modifying @Query(value = "UPDATE bien_the_san_pham SET deleted_at = NOW(), sku = CONCAT(LEFT(sku, 99 - CHAR_LENGTH(id)), '#', id) WHERE san_pham_id = :spId AND deleted_at IS NULL", nativeQuery = true) int xoaMemTheoSanPham(Long spId)`
  - `SanPhamRepository`: `@Modifying @Query("UPDATE SanPham s SET s.deletedAt = CURRENT_TIMESTAMP WHERE s.id = :id") int xoaMem(Long id)`
  - `ChiTietGioHangRepository`:
    - `@Modifying @Query("DELETE FROM ChiTietGioHang c WHERE c.bienThe.id IN :ids") int xoaTheoBienThe(Collection<Long> ids)`
    - `@Modifying @Query("DELETE FROM ChiTietGioHang c WHERE c.bienThe.id IN (SELECT b.id FROM BienTheSanPham b WHERE b.sanPham.id = :spId)") int xoaTheoSanPham(Long spId)`
  - `YeuThichRepository`: `@Modifying @Query("DELETE FROM YeuThich y WHERE y.sanPham.id = :spId") int xoaTheoSanPham(Long spId)`
- `AdminSanPhamService`:
  - `PageResponse<AdminSanPhamTomTatResponse> timKiem(String tuKhoa, Long danhMucId, Long thuongHieuId, String trangThai /* dang_ban|dang_an|het_hang */, int trang, int kichThuoc)`
    - Tìm theo tên **hoặc** SKU (subquery biến thể).
    - `het_hang`: không có biến thể `hienThi` nào có `soLuongTon > 0`.
    - Sắp `id DESC`, kích thước tối đa 50.
  - `AdminSanPhamChiTietResponse layChiTiet(Long id)`
  - `AdminSanPhamChiTietResponse tao(AdminSanPhamRequest req, Long adminId)`
  - `AdminSanPhamChiTietResponse capNhat(Long id, AdminSanPhamRequest req, Long adminId)`
  - `void doiHienThi(Long id, boolean hienThi)`
  - `void xoa(Long id)`
  - Constructor: `SanPhamRepository, BienTheSanPhamRepository, DanhMucRepository, ThuongHieuRepository, ChiTietGioHangRepository, YeuThichRepository, LichSuTonKhoRepository, NguoiDungRepository, BienTheDongBo, EntityManager`.
- Controller `/api/admin/san-pham`:
  - `GET` (query `tuKhoa`, `danhMucId`, `thuongHieuId`, `trangThai`, `trang=0`, `kichThuoc=20`)
  - `GET /{id}`
  - `POST` (201)
  - `PUT /{id}`
  - `PATCH /{id}/hien-thi`
  - `DELETE /{id}` (204)
  - `adminId` lấy từ `@AuthenticationPrincipal NguoiDungPrincipal`.

Thứ tự trong `luu(SanPham sp, req, adminId)`, dùng chung cho `tao` và `capNhat`:
```text
1. bienTheDongBo.kiemTra(req); kiểm tra cấp sản phẩm:
   - giaGoc != null && giaGoc < giaBan -> 400 "Giá gốc phải lớn hơn hoặc bằng giá bán"
   - mỗi biến thể: layGiaGoc hiệu lực < layGiaBan hiệu lực -> 400 "Giá gốc của biến thể <SKU> phải lớn hơn hoặc bằng giá bán"
   - hienThi != false && hinhAnhs rỗng -> 400 "Sản phẩm đang hiển thị phải có ít nhất 1 ảnh"
   - nhiều hơn 1 ảnh laAnhChinh -> 400 "Chỉ chọn một ảnh chính"; ảnh trỏ tới tuyChon/giaTri không có trong request -> 400
   - danhMuc / thuongHieu không tồn tại -> 404
   - mỗi SKU: timSanPhamIdTheoSkuKeCaDaXoa(sku) có phần tử khác sp.getId() -> 409 "SKU <SKU> đã được dùng cho sản phẩm khác"
2. gán trường đơn + slug (taoSlugDuyNhat với demSlugKeCaDaXoa; giữ slug cũ nếu SlugUtil.cungGoc)
3. KetQua kq = bienTheDongBo.dongBo(sp, req)
4. ảnh + thông số: clear() rồi thêm lại theo request (ảnh gắn GiaTriTuyChon tìm theo tên tùy chọn + tên giá trị
   trên entity sau bước 3); không có ảnh chính thì ảnh đầu tiên là ảnh chính
5. id biến thể bị xóa: chiTietGioHangRepository.xoaTheoBienThe(...) nếu khác rỗng
6. sanPhamRepository.saveAndFlush(sp)     // ghi SKU đã giải phóng + xóa mềm TRƯỚC khi chèn biến thể mới
7. sp.getBienThes().addAll(kq.moi()); entityManager.flush()
8. với mỗi biến thể mới: LichSuTonKho loai=khoi_tao, soLuongThayDoi=soLuongSau=soLuongTon,
   ghiChu="Khởi tạo biến thể", nguoiThucHien=nguoiDungRepository.getReferenceById(adminId)
```
`xoa(id)`:
1. `chiTietGioHangRepository.xoaTheoSanPham(id)`, `yeuThichRepository.xoaTheoSanPham(id)`, `bienTheSanPhamRepository.xoaMemTheoSanPham(id)`, `sanPhamRepository.xoaMem(id)`. Làm đúng thứ tự này: subquery giỏ hàng cần biến thể chưa bị xóa.
2. Không dùng `repository.delete(sp)`: cascade sẽ xóa cứng ảnh, thông số, tùy chọn.

`doiHienThi(id, true)`: sản phẩm chưa có ảnh thì trả 400 với cùng thông báo như khi lưu form.

- [ ] **Step 1: Viết test thất bại** (Mockito; `BienTheDongBo` dùng bản thật `new BienTheDongBo()`; mock `EntityManager`):
  - `taoSanPhamGhiLichSuKhoiTao`: request 1 biến thể mặc định, `tonKhoBanDau` 10 → `lichSuTonKhoRepository.save` được gọi đúng 1 lần với `loai==khoi_tao`, `soLuongThayDoi==10`, `soLuongSau==10`.
  - `giaGocNhoHonGiaBanBiTuChoi` → 400 `"Giá gốc phải lớn hơn hoặc bằng giá bán"`.
  - `sanPhamHienThiKhongAnhBiTuChoi` → 400 `"Sản phẩm đang hiển thị phải có ít nhất 1 ảnh"`.
  - `batHienThiSanPhamKhongAnhBiTuChoi`: `doiHienThi(id, true)` trên sản phẩm không có ảnh → 400 với thông báo như trên.
  - `skuDangDungOSanPhamKhacBiTuChoi`: `timSanPhamIdTheoSkuKeCaDaXoa("TG-X")` trả `[99L]` khi sửa sản phẩm 1 → 409 `"SKU TG-X đã được dùng cho sản phẩm khác"`.
  - `xoaBienTheGoKhoiGioHang`: sửa sản phẩm mẫu, bỏ giá trị `Trắng` → `verify(chiTietGioHangRepository).xoaTheoBienThe(List.of(102L))`.
  - `xoaSanPhamGoGioHangVaYeuThich`: `xoa(1L)` → `InOrder` xác nhận thứ tự `xoaTheoSanPham` (giỏ hàng) → `xoaTheoSanPham` (yêu thích) → `xoaMemTheoSanPham` → `xoaMem`; `never().delete(any())`.
- [ ] **Step 2: Chạy** `MVN test -Dtest=AdminSanPhamServiceTest`. Kết quả mong đợi: FAIL.
- [ ] **Step 3: Cài đặt** service và controller (`@Validated`, `@Tag(name = "Quản trị - Sản phẩm")`).
- [ ] **Step 4: Chạy** `MVN test`. Kết quả mong đợi: PASS.
- [ ] **Step 5: Kiểm tra trên CSDL thật** (cần XAMPP MySQL đang chạy).
  1. Chạy backend: `MVN spring-boot:run`.
  2. Đăng nhập `admin@techgear.vn` / `Admin@123` qua Swagger (`/swagger-ui.html`).
  3. `PUT` một sản phẩm có tùy chọn: bỏ một giá trị rồi thêm lại giá trị đó với đúng SKU cũ. Kết quả mong đợi: cả hai lần đều 200.
  4. `SELECT id, sku, deleted_at FROM bien_the_san_pham WHERE san_pham_id = ?` phải thấy dòng cũ có `sku#id` và `deleted_at` khác NULL.
- [ ] **Step 6: Commit** `git commit -m "Thêm API quản trị sản phẩm"`

---

### Task 6: API quản trị tồn kho

**Files:**
- Create:
  - `dto/admin/AdminTonKhoResponse.java`
  - `dto/admin/NhapKhoRequest.java`
  - `dto/admin/DieuChinhKhoRequest.java`
  - `dto/admin/LichSuTonKhoResponse.java`
  - `service/admin/AdminTonKhoService.java`
  - `controller/AdminTonKhoController.java`
- Modify: `repository/BienTheSanPhamRepository.java`, `repository/LichSuTonKhoRepository.java`
- Test: `src/test/java/com/techgear/service/admin/AdminTonKhoServiceTest.java`

**Interfaces:**
- DTO:
  - `AdminTonKhoResponse(Long bienTheId, Long sanPhamId, String tenSanPham, String phanLoai, String sku, int soLuongTon, int nguongCanhBao, String trangThai /* con_hang|sap_het|het_hang */, boolean dangBan)`
    - `phanLoai` là các giá trị nối bằng `" / "` theo thứ tự tùy chọn, giống `GioHangService.sangDong`.
  - `NhapKhoRequest(@NotNull @Min(value = 1, message = "Số lượng nhập phải lớn hơn 0") @Max(100000) Integer soLuong, @Size(max = 255) String ghiChu)`
  - `DieuChinhKhoRequest(@NotNull @Min(value = 0, message = "Số lượng thực tế không được âm") Integer soLuongThucTe, @NotBlank(message = "Vui lòng nhập lý do điều chỉnh") @Size(max = 255) String lyDo)`
  - `LichSuTonKhoResponse(Long id, LocalDateTime thoiGian, String loai, int soLuongThayDoi, int soLuongSau, String ghiChu, String nguoiThucHien, String maDonHang)`
- Repository:
  - `BienTheSanPhamRepository.timChoQuanTri(String tuKhoa /* null hoặc "%x%" chữ thường */, String trangThai /* null|con_hang|sap_het|het_hang */, Pageable)` là `@Query` JPQL có `countQuery`:
    ```text
    SELECT b FROM BienTheSanPham b JOIN b.sanPham s
    WHERE (:tuKhoa IS NULL OR LOWER(s.ten) LIKE :tuKhoa OR LOWER(b.sku) LIKE :tuKhoa)
      AND (:trangThai IS NULL
           OR (:trangThai = 'het_hang' AND b.soLuongTon <= 0)
           OR (:trangThai = 'sap_het'  AND b.soLuongTon > 0 AND b.soLuongTon <= b.nguongCanhBao)
           OR (:trangThai = 'con_hang' AND b.soLuongTon > b.nguongCanhBao))
    ORDER BY CASE WHEN b.soLuongTon > 0 AND b.soLuongTon <= b.nguongCanhBao THEN 0
                  WHEN b.soLuongTon <= 0 THEN 1 ELSE 2 END, s.ten, b.id
    ```
    Truyền `Pageable` không kèm sort.
  - `LichSuTonKhoRepository.Page<LichSuTonKho> findByBienTheIdOrderByCreatedAtDescIdDesc(Long bienTheId, Pageable)`
- Service `AdminTonKhoService`:
  - `PageResponse<AdminTonKhoResponse> danhSach(String tuKhoa, String trangThai, int trang, int kichThuoc)`
  - `AdminTonKhoResponse nhapKho(Long bienTheId, NhapKhoRequest req, Long adminId)`
  - `AdminTonKhoResponse dieuChinh(Long bienTheId, DieuChinhKhoRequest req, Long adminId)`
  - `PageResponse<LichSuTonKhoResponse> lichSu(Long bienTheId, int trang, int kichThuoc)`
  - `static String trangThai(BienTheSanPham bt)`
  - Thao tác ghi dùng `findByIdForUpdate`; không tìm thấy thì 404 `"Không tìm thấy biến thể sản phẩm"`.
- Controller `/api/admin/ton-kho`:
  - `GET` (query `tuKhoa`, `trangThai`, `trang=0`, `kichThuoc=20`)
  - `POST /{bienTheId}/nhap-kho`
  - `POST /{bienTheId}/dieu-chinh`
  - `GET /{bienTheId}/lich-su` (`trang=0`, `kichThuoc=20`)

- [ ] **Step 1: Viết test thất bại:**
  - `nhapKhoCongTonVaGhiLichSu`: tồn 3, nhập 10 với ghi chú `"Lô 11/2026"` → tồn 13; lịch sử có `loai==nhap_kho`, `soLuongThayDoi==10`, `soLuongSau==13`, `ghiChu=="Lô 11/2026"`.
  - `dieuChinhGhiChenhLech`: tồn 13, thực tế 9, lý do `"Kiểm kê"` → tồn 9; lịch sử có `loai==dieu_chinh`, `soLuongThayDoi==-4`, `ghiChu=="Kiểm kê"`.
  - `dieuChinhKhongChenhLechBiTuChoi`: tồn 9, thực tế 9 → 400 `"Số lượng thực tế bằng tồn kho hiện tại, không cần điều chỉnh"`, `verify(lichSuTonKhoRepository, never()).save(any())`.
  - `trangThaiTheoNguong`: (0, 5) → `het_hang`; (5, 5) → `sap_het`; (6, 5) → `con_hang`.
  - `bienTheKhongTonTaiTra404`.
- [ ] **Step 2: Chạy** `MVN test -Dtest=AdminTonKhoServiceTest`. Kết quả mong đợi: FAIL.
- [ ] **Step 3: Cài đặt.**
- [ ] **Step 4: Chạy** `MVN test`. Kết quả mong đợi: PASS.
- [ ] **Step 5: Commit** `git commit -m "Thêm API quản trị tồn kho: nhập kho, điều chỉnh, lịch sử"`

---

### Task 7: Khung quản trị (frontend)

**Files:**
- Create:
  - `admin/AdminApp.jsx`
  - `admin/AdminLayout.jsx`
  - `admin/api/adminApi.js`
  - `admin/components/Modal.jsx`
  - `admin/components/ConfirmDialog.jsx`
  - `admin/components/DataTable.jsx`
  - `styles/admin.css`
- Modify: `App.jsx`, `components/Header.jsx` (`AccountMenu` và `MobileDrawer`)

**Interfaces:**
- `adminApi.js` (default export; mỗi hàm trả về promise dữ liệu đã bóc tách bởi interceptor):
  ```js
  danhMuc:    { layCay(), tao(duLieu), capNhat(id, duLieu), xoa(id) }
  thuongHieu: { layTatCa(), tao(duLieu), capNhat(id, duLieu), xoa(id) }
  sanPham:    { timKiem(thamSo), layChiTiet(id), tao(duLieu), capNhat(id, duLieu), doiHienThi(id, hienThi), xoa(id) }
  tonKho:     { danhSach(thamSo), nhapKho(bienTheId, duLieu), dieuChinh(bienTheId, duLieu), lichSu(bienTheId, trang) }
  taiAnh(file, thuMuc) -> Promise<string>   // POST /admin/upload?thuMuc=..., FormData 'file',
                                            // header 'Content-Type': 'multipart/form-data', trả về res.duongDan
  ```
- Component:
  - `<Modal tieuDe mo onClose rong?>{children}</Modal>`: đóng khi nhấn Esc hoặc bấm nền; `role="dialog"`, `aria-modal`.
  - `<ConfirmDialog mo tieuDe noiDung nhanXacNhan="Xóa" nguyHiem onXacNhan onHuy dangXuLy />`
  - `<DataTable cot={[{ khoa, tieuDe, hienThi: (dong) => node, canh? }]} duLieu dangTai trong="Chưa có dữ liệu" khoaDong="id" />`
- Route trong `App.jsx`, **đặt ngoài** `<Route element={<Layout/>}>`:
  ```jsx
  const AdminApp = lazy(() => import('./admin/AdminApp.jsx'));
  <Route path="admin/*" element={<ProtectedRoute chiQuanTri><Suspense fallback={<Loading />}><AdminApp /></Suspense></ProtectedRoute>} />
  ```
- `AdminApp`:
  - `index` → `<Navigate to="san-pham" replace/>`
  - các route `danh-muc`, `thuong-hieu`, `san-pham`, `san-pham/moi`, `san-pham/:id`, `ton-kho`
  - `*` → về `san-pham`
- Trong task này các trang tạm là `<h1>` giữ chỗ; Task 8–11 thay bằng trang thật.

- [ ] **Step 1: Cài đặt.**
  - `AdminLayout`:
    - Menu trái: tiêu đề "TECHGEAR ADMIN"; `NavLink` tới Danh mục, Thương hiệu, Sản phẩm, Tồn kho.
    - Nhóm "Đợt sau" hiển thị mờ, `aria-disabled`, không phải liên kết: Đơn hàng, Mã giảm giá, Banner, Cấu hình, Người dùng, Đánh giá, Tổng quan.
    - Cuối menu: "Về cửa hàng" (`/`), tên admin, nút Đăng xuất (`dangXuat` rồi `navigate('/')`).
    - Dưới 900px menu thu thành nút ☰.
    - `import '../styles/admin.css'`.
  - Header: trong `AccountMenu` và `MobileDrawer`, khi `laQuanTri` (lấy từ `useAuth()`) thêm `<Link to="/admin">Trang quản trị</Link>` với icon `grid`.
  - `admin.css`: dùng biến trong `tokens.css`. Định nghĩa các lớp `.admin-shell`, `.admin-sidebar`, `.admin-main`, `.admin-toolbar`, `.admin-table`, `.admin-badge--{success,warning,danger,muted}`, `.admin-form-grid` (2 cột, 1 cột dưới 900px), `.admin-card`, `.modal-backdrop`, `.modal`.
- [ ] **Step 2: Chạy** `npm run build`. Kết quả mong đợi: build thành công, có chunk riêng `AdminApp-*.js` trong `dist/assets`.
- [ ] **Step 3: Kiểm tra thủ công** (`npm run dev`):
  - Đăng nhập admin → menu tài khoản có "Trang quản trị" → `/admin` chuyển sang `/admin/san-pham`.
  - Đăng nhập tài khoản khách, gõ `/admin` → bị đưa về `/`.
  - Chưa đăng nhập → bị đưa về `/dang-nhap`.
- [ ] **Step 4: Commit**
  ```bash
  git add source/frontend/src/App.jsx source/frontend/src/components/Header.jsx source/frontend/src/admin source/frontend/src/styles/admin.css
  git commit -m "Thêm khung giao diện quản trị"
  ```

---

### Task 8: Trang Danh mục và Thương hiệu

**Files:**
- Create: `admin/components/ImageUploader.jsx`, `admin/pages/DanhMucPage.jsx`, `admin/pages/ThuongHieuPage.jsx`
- Modify: `admin/AdminApp.jsx`

**Interfaces:**
- Consumes: `adminApi.danhMuc`, `adminApi.thuongHieu`, `adminApi.taiAnh`, `Modal`, `ConfirmDialog`, `DataTable`.
- `<ImageUploader thuMuc giaTri={string|null} onChange={(duongDan|null) => void} />`
  - Một ảnh: xem trước bằng `duongDanAnh()` trong `utils/format.js`, có nút đổi và nút xóa.
  - Chặn file khác `image/*` hoặc lớn hơn 5MB ngay ở trình duyệt (thông báo `"Ảnh tối đa 5MB"`).
- Lỗi hiển thị theo cùng một cách ở mọi form:
  - `layThongBaoLoi(err)` hiện bằng `ErrorMessage`.
  - `err.response?.data?.loi` hiện dưới từng ô qua `<small className="field-error">`.
  - Thành công thì `toast.hien(...)`.

- [ ] **Step 1: Cài đặt.**
  - `DanhMucPage`:
    - Bảng cây: dòng danh mục con thụt vào. Cột: ảnh, tên, slug, thứ tự, số sản phẩm, nhãn hiển thị/ẩn, nút sửa/xóa.
    - Modal form: tên, danh mục cha (`<select>` chỉ liệt kê danh mục gốc khác chính nó, cộng "— Không có (cấp gốc) —"), mô tả, ảnh (`thuMuc="danh-muc"`), thứ tự, hiển thị.
    - Xóa phải qua `ConfirmDialog`; lỗi 409 hiện bằng toast loại lỗi.
  - `ThuongHieuPage`: cùng mẫu, cột logo, tên, số sản phẩm, hiển thị; ảnh dùng `thuMuc="thuong-hieu"`.
- [ ] **Step 2: Chạy** `npm run build`. Kết quả mong đợi: thành công.
- [ ] **Step 3: Kiểm tra thủ công:**
  - Tạo danh mục con "Test con" dưới một danh mục gốc → website khách thấy trong menu.
  - Ẩn danh mục cha → menu và sản phẩm của nó biến mất khỏi `/san-pham`.
  - Bật lại hiển thị.
  - Xóa danh mục còn sản phẩm → thấy thông báo `"Không thể xóa: danh mục còn sản phẩm"`.
  - Tải logo thương hiệu → logo hiện ngay trong bảng.
- [ ] **Step 4: Commit** `git commit -m "Thêm trang quản trị danh mục và thương hiệu"`

---

### Task 9: Hàm thuần cho biến thể (frontend)

**Files:**
- Create: `admin/utils/bienThe.js`, `admin/utils/bienThe.test.js`

**Interfaces** (named export, không import gì từ React/Vite để chạy được bằng `node --test`):
- `taoSlug(chuoi: string): string`: giống `SlugUtil.taoSlug` của backend (đ→d, bỏ dấu, chữ thường, ký tự khác thành `-`).
- `vietTat(ten: string): string`: chữ cái đầu của tối đa 5 từ trong `taoSlug(ten)`, viết hoa. Ví dụ `"Sạc dự phòng Anker 20000"` → `"SDPA2"`.
- `goiYSku(tenSanPham: string, giaTris: string[]): string`: `TG-<vietTat>` cộng `-<taoSlug(gt) viết hoa>` cho từng giá trị.
- `sinhToHop(tuyChons: {khoa, giaTris: {khoa, giaTri}[]}[]): {khoa: string, giaTris: {khoa, giaTri}[]}[]`
  - Tích Đề-các theo thứ tự tùy chọn; `khoa` của tổ hợp là các `khoa` giá trị nối bằng `'|'`.
  - Không có tùy chọn → `[{ khoa: '', giaTris: [] }]`.
  - Tùy chọn có 0 giá trị thì bị bỏ qua.
- `ghepBienThe(toHops, bienTheCu: {khoa, ...}[], tenSanPham): BienTheDong[]`
  - Giữ nguyên dòng cũ có cùng `khoa`. Dòng mới nhận `{ khoa, giaTris, id: null, sku: goiYSku(...), giaBan: '', giaGoc: '', tonKhoBanDau: 0, nguongCanhBao: 5, dangBan: true }`.
- `skuTrung(dongs): Set<string>`: các SKU (in hoa) xuất hiện từ 2 lần trở lên.

- [ ] **Step 1: Viết test thất bại** `bienThe.test.js` (`import { test } from 'node:test'; import assert from 'node:assert/strict';`):
  - `taoSlug('Củ sạc nhanh 65W')` bằng `'cu-sac-nhanh-65w'`.
  - `goiYSku('Sạc dự phòng Anker', ['Đen', '20000mAh'])` bằng `'TG-SDPA-DEN-20000MAH'`.
  - `sinhToHop` với 2 tùy chọn (2 và 3 giá trị) cho 6 tổ hợp; tổ hợp đầu có `khoa` `'a1|b1'`.
  - `sinhToHop([])` bằng `[{ khoa: '', giaTris: [] }]`.
  - `ghepBienThe` giữ `id` và `giaBan` của dòng `'a1'` khi thêm giá trị `a2`; dòng `a2` có `id` null.
  - `skuTrung` với `['A','a','B']` bằng `new Set(['A'])`.
- [ ] **Step 2: Chạy** `node --test src/admin/utils/`. Kết quả mong đợi: FAIL (thiếu export).
- [ ] **Step 3: Cài đặt.**
- [ ] **Step 4: Chạy** `node --test src/admin/utils/`. Kết quả mong đợi: `pass 6`, `fail 0`.
- [ ] **Step 5: Commit** `git commit -m "Thêm hàm sinh tổ hợp biến thể và gợi ý SKU"`

---

### Task 10: Trang danh sách và form sản phẩm

**Files:**
- Create: `admin/pages/SanPhamListPage.jsx`, `admin/pages/SanPhamFormPage.jsx`, `admin/components/VariantEditor.jsx`, `admin/components/ProductImages.jsx`
- Modify: `admin/AdminApp.jsx`

**Interfaces:**
- Consumes:
  - `adminApi.sanPham`, `adminApi.danhMuc.layCay`, `adminApi.thuongHieu.layTatCa`, `adminApi.taiAnh`
  - `sinhToHop`, `ghepBienThe`, `goiYSku`, `skuTrung` (Task 9)
  - `Pagination`, `Price` có sẵn
- `<VariantEditor tenSanPham tuyChons onTuyChonsChange bienThes onBienThesChange giaBanSanPham />`
  - Mỗi giá trị trong state `tuyChons` có `khoa`: lấy `"id:<id>"` nếu đã có id, ngược lại sinh bằng `crypto.randomUUID()`.
  - Mỗi lần `tuyChons` đổi, `bienThes = ghepBienThe(sinhToHop(tuyChons), bienThes, tenSanPham)`.
  - Chặn thêm tùy chọn thứ 4 và chặn khi số tổ hợp vượt 50, kèm thông báo.
  - Có hàng "Áp dụng cho tất cả" để điền nhanh giá bán, giá gốc, ngưỡng.
  - Ô tồn kho ban đầu chỉ sửa được khi `id == null`. Biến thể đã có hiển thị `soLuongTon` dạng chỉ đọc kèm chữ "Sửa ở trang Tồn kho".
  - SKU nằm trong `skuTrung` thì tô viền đỏ.
- `<ProductImages anhs onChange tuyChonGiaTris />`
  - Tải nhiều ảnh cùng lúc với `thuMuc="san-pham"`.
  - Mỗi ảnh có nút ←/→ để đổi thứ tự, radio "Ảnh chính", nút xóa, và `<select>` "Gắn với giá trị" liệt kê các cặp tùy chọn/giá trị hiện có.
- Chuyển state thành request (theo đúng `AdminSanPhamRequest` ở Task 4):
  - `giaTris` của biến thể là tên giá trị theo thứ tự tùy chọn.
  - Ô giá để trống gửi `null`.
  - Ảnh gắn giá trị gửi `tuyChon` và `giaTri` là tên tùy chọn và tên giá trị.

- [ ] **Step 1: Cài đặt `SanPhamListPage`.**
  - Thanh lọc: ô tìm tên hoặc SKU (debounce 400ms), danh mục, thương hiệu, trạng thái (Tất cả / Đang bán / Đang ẩn / Hết hàng).
  - Các bộ lọc giữ trong `useSearchParams` để tải lại trang không mất lọc.
  - Bảng có các cột theo spec mục 5.4.
  - Công tắc hiển thị gọi `doiHienThi`; nếu lỗi thì trả công tắc về trạng thái cũ và hiện toast lỗi.
  - Có liên kết "Xem trên cửa hàng" tới `/san-pham/<slug>` (`target="_blank"`), nút sửa và nút xóa (qua `ConfirmDialog`).
- [ ] **Step 2: Cài đặt `SanPhamFormPage`.**
  - Các khối: Thông tin chung, Giá, Hình ảnh, Thông số kỹ thuật, Tùy chọn & biến thể; nút Lưu dính ở cuối trang.
  - Kiểm tra trước khi gửi:
    - giá gốc ≥ giá bán;
    - có ít nhất 1 ảnh khi bật hiển thị;
    - không có SKU trùng.
  - Khi sửa sản phẩm: nếu có biến thể cũ không còn trong bảng mới, hiện `ConfirmDialog` với nội dung `"X biến thể sẽ ngừng bán (còn Y sản phẩm trong kho)"` trước khi gửi.
  - Lưu thành công: hiện toast, chuyển tới `/admin/san-pham/<id>` và nạp lại dữ liệu từ response.
- [ ] **Step 3: Chạy** `npm run build` và `node --test src/admin/utils/`. Kết quả mong đợi: thành công.
- [ ] **Step 4: Kiểm tra thủ công** (theo bảng 5.7):
  - Tạo sản phẩm mới có 2 màu × 2 dung lượng → thấy ở "Hàng mới về" và trang chi tiết có đủ lựa chọn.
  - Bật nổi bật → thấy ở "Sản phẩm nổi bật".
  - Đặt giá gốc lớn hơn giá bán → thẻ sản phẩm hiện giá gạch ngang và nhãn −x%.
  - Bỏ một màu → hộp cảnh báo hiện đúng số; lưu xong biến thể đó biến mất khỏi trang chi tiết và khỏi giỏ của khách.
  - Ẩn sản phẩm → trang chi tiết trả 404.
- [ ] **Step 5: Commit** `git commit -m "Thêm trang quản trị sản phẩm và biến thể"`

---

### Task 11: Trang Tồn kho

**Files:**
- Create: `admin/pages/TonKhoPage.jsx`, `admin/components/StockDialog.jsx`
- Modify: `admin/AdminApp.jsx`

**Interfaces:**
- Consumes: `adminApi.tonKho`, `Modal`, `DataTable`, `Pagination`.
- `<StockDialog che="nhap"|"dieuChinh"|"lichSu" bienThe={AdminTonKhoResponse} onClose onDone={(capNhat: AdminTonKhoResponse) => void} />`
  - **Nhập:** ô số lượng (≥ 1) và ghi chú.
  - **Điều chỉnh:** ô số lượng thực tế (≥ 0) và lý do bắt buộc. Hiện ngay dòng "Chênh lệch: +n / −n".
  - **Lịch sử:** bảng phân trang gồm thời gian, loại (đổi sang tiếng Việt: Khởi tạo / Nhập kho / Bán hàng / Hủy đơn / Điều chỉnh), thay đổi (có dấu +/−), tồn sau, ghi chú, người thực hiện hoặc mã đơn.

- [ ] **Step 1: Cài đặt `TonKhoPage`.**
  - Tìm theo tên hoặc SKU; lọc trạng thái.
  - Nhãn trạng thái: Còn hàng (success), Sắp hết (warning), Hết hàng (danger).
  - Mỗi dòng có các nút Nhập kho, Điều chỉnh, Lịch sử.
  - Sau `onDone`, thay đúng dòng đó trong bảng bằng dữ liệu trả về, không tải lại cả trang.
- [ ] **Step 2: Chạy** `npm run build`. Kết quả mong đợi: thành công.
- [ ] **Step 3: Kiểm tra thủ công:**
  - Nhập kho cho một biến thể hết hàng → thẻ sản phẩm trên website hết nhãn "Hết hàng", và biến thể đó mua được.
  - Khách đặt một đơn → trang Tồn kho giảm đúng số; Lịch sử có dòng "Bán hàng" kèm mã đơn.
  - Điều chỉnh với lý do để trống → hiện lỗi dưới ô lý do.
- [ ] **Step 4: Commit** `git commit -m "Thêm trang quản trị tồn kho"`

---

### Task 12: Nghiệm thu toàn bộ

**Files:** không có file mới (chỉ sửa nếu phát hiện lỗi).

- [ ] **Step 1: Chạy** `MVN test`. Kết quả mong đợi: `BUILD SUCCESS`, không test nào fail.
- [ ] **Step 2: Chạy** `npm run build` và `node --test src/admin/utils/`. Kết quả mong đợi: thành công.
- [ ] **Step 3: Đi lại toàn bộ bảng 5.7 của spec trên XAMPP**, ghi kết quả từng dòng.
- [ ] **Step 4: Kiểm tra các trường hợp sau và ghi lại kết quả:**
  - Mở `/tai-khoan/don-hang/<mã>` của một đơn có biến thể đã xóa mềm (từ Task 10) → phải hiển thị bình thường. Nếu bị 500, áp dụng cách sửa đã ghi ở mục Review Focus, viết test, rồi commit.
  - Dùng token khách gọi `GET /api/admin/san-pham` → 403.
- [ ] **Step 5: Commit** các sửa chữa nếu có (`git commit -m "Sửa lỗi nghiệm thu quản trị Đợt 1"`).
