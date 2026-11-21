import adminApi from '../api/adminApi.js';

const chuan = (s) => String(s ?? '').trim().toLowerCase();
const rong = (v) => String(v ?? '').trim() === '';
const so = (v) => (rong(v) ? null : Number(v));
const khoaToHop = (ids) => [...ids].sort((a, b) => a - b).join(',');
const chuoiSo = (v) => (v === null || v === undefined ? '' : String(v));

/** SanPhamQT (backend) -> state của form. Khóa giá trị = `id:<id giá trị>` để ảnh, biến thể tham chiếu. */
export function tuPhanHoi(sp) {
  const tuyChons = (sp.tuyChons || []).map((tc) => ({
    khoa: `tc:${tc.id}`,
    id: tc.id,
    ten: tc.ten,
    loai: tc.loai,
    giaTris: tc.giaTris.map((gt) => ({ khoa: `id:${gt.id}`, id: gt.id, giaTri: gt.giaTri, maMau: gt.maMau || '' })),
  }));
  const bienThes = (sp.bienThes || []).flatMap((bt) => {
    // Theo thứ tự tùy chọn (như sinhToHop) để khóa tổ hợp khớp với biến thể sinh ra trong form
    const giaTris = tuyChons.filter((tc) => tc.giaTris.length > 0)
      .map((tc) => tc.giaTris.find((gt) => (bt.giaTriIds || []).includes(gt.id)));
    // Dữ liệu lệch (thiếu giá trị của một tùy chọn): bỏ qua biến thể thay vì làm trắng trang.
    if (giaTris.some((gt) => !gt)) return [];
    return [{
      khoa: giaTris.map((gt) => gt.khoa).join('|'),
      giaTris,
      id: bt.id,
      sku: bt.sku,
      giaBan: chuoiSo(bt.giaBan),
      giaGoc: chuoiSo(bt.giaGoc),
      tonKhoBanDau: 0,
      nguongCanhBao: bt.nguongCanhBao,
      dangBan: bt.hienThi,
      soLuongTon: bt.soLuongTon,
    }];
  });
  return {
    form: {
      ten: sp.ten,
      danhMucId: chuoiSo(sp.danhMucId),
      thuongHieuId: chuoiSo(sp.thuongHieuId),
      moTaNgan: sp.moTaNgan || '',
      moTa: sp.moTa || '',
      giaBan: sp.giaBan ? String(sp.giaBan) : '', // giá bán mặc định cho biến thể = giá đang hiển thị
      giaGoc: chuoiSo(sp.giaGoc),
      baoHanhThang: chuoiSo(sp.baoHanhThang),
      noiBat: !!sp.noiBat,
      hienThi: !!sp.hienThi,
    },
    anhs: (sp.hinhAnhs || []).map((a) => ({
      khoa: crypto.randomUUID(), id: a.id, duongDan: a.duongDan, moTa: a.moTa || '', laAnhChinh: a.laAnhChinh,
      giaTriKhoa: a.giaTriTuyChonId ? `id:${a.giaTriTuyChonId}` : '',
    })),
    thongSos: (sp.thongSos || []).map((t) => ({ khoa: crypto.randomUUID(), ten: t.ten, giaTri: t.giaTri })),
    tuyChons,
    bienThes,
    bienTheGoc: bienThes.map((b) => ({ khoa: b.khoa, soLuongTon: b.soLuongTon })),
    slug: sp.slug,
  };
}

/**
 * Lưu form sản phẩm qua các API con của backend, theo thứ tự:
 * thông tin chung → xóa biến thể bị bỏ → tùy chọn → biến thể → ảnh.
 * Mỗi bước đối chiếu với sản phẩm vừa trả về (tùy chọn theo id hoặc tên, giá trị theo id hoặc chữ,
 * biến thể theo id hoặc tổ hợp, ảnh theo id hoặc đường dẫn) nên lưu lại sau khi lỗi giữa chừng không tạo trùng.
 * onTaoMoi(id) được gọi ngay khi sản phẩm mới được tạo, để trang chuyển sang chế độ sửa dù bước sau lỗi.
 * Trả về sản phẩm (SanPhamQT) sau bước cuối.
 */
export default async function luuSanPham(id, { form, anhs, thongSos, tuyChons, bienThes, slug }, onTaoMoi) {
  const thongTin = {
    ten: form.ten.trim(),
    slug: slug || null,
    danhMucId: Number(form.danhMucId),
    thuongHieuId: so(form.thuongHieuId),
    moTaNgan: form.moTaNgan.trim() || null,
    moTa: form.moTa.trim() || null,
    baoHanhThang: so(form.baoHanhThang),
    noiBat: form.noiBat,
    hienThi: form.hienThi,
    thongSos: thongSos.filter((t) => !rong(t.ten) || !rong(t.giaTri)).map((t) => ({ ten: t.ten.trim(), giaTri: t.giaTri.trim() })),
  };
  let sp;
  if (id) {
    sp = await adminApi.sanPham.capNhat(id, thongTin);
  } else {
    sp = await adminApi.sanPham.tao(thongTin);
    onTaoMoi?.(sp.id);
  }

  const tuyChonServer = (tc) => sp.tuyChons.find((s) => (tc.id != null ? s.id === tc.id : chuan(s.ten) === chuan(tc.ten)));
  /** khóa giá trị trong form -> id giá trị trên server (chỉ những giá trị server đã có). */
  const idGiaTri = () => {
    const ketQua = new Map();
    for (const tc of tuyChons) {
      const s = tuyChonServer(tc);
      for (const gt of tc.giaTris) {
        const v = s?.giaTris.find((x) => (gt.id != null ? x.id === gt.id : chuan(x.giaTri) === chuan(gt.giaTri)));
        if (v) ketQua.set(gt.khoa, v.id);
      }
    }
    return ketQua;
  };

  // Biến thể không còn trong form: xóa trước, để tùy chọn/giá trị của chúng được sửa, xóa
  let theoKhoa = idGiaTri();
  const idGiu = new Set(bienThes.filter((b) => b.id != null).map((b) => b.id));
  const toHopGiu = new Set(bienThes
    .map((b) => b.giaTris.map((gt) => theoKhoa.get(gt.khoa)))
    .filter((ids) => ids.every((x) => x != null))
    .map(khoaToHop));
  for (const bt of sp.bienThes) {
    if (!idGiu.has(bt.id) && !toHopGiu.has(khoaToHop(bt.giaTriIds))) sp = await adminApi.bienThe.xoa(bt.id);
  }

  for (const s of sp.tuyChons) {
    if (!tuyChons.some((tc) => tuyChonServer(tc)?.id === s.id)) sp = await adminApi.tuyChon.xoa(s.id);
  }
  for (const tc of tuyChons) {
    const s = tuyChonServer(tc);
    const giaTris = tc.giaTris.map((gt) => ({
      id: gt.id ?? s?.giaTris.find((x) => chuan(x.giaTri) === chuan(gt.giaTri))?.id ?? null,
      giaTri: gt.giaTri.trim(),
      maMau: gt.maMau || null,
    }));
    const duLieu = { ten: tc.ten.trim(), loai: tc.loai, giaTris };
    sp = s ? await adminApi.tuyChon.sua(s.id, duLieu) : await adminApi.tuyChon.them(sp.id, duLieu);
  }

  theoKhoa = idGiaTri();
  for (const b of bienThes) {
    const giaTriIds = b.giaTris.map((gt) => theoKhoa.get(gt.khoa));
    const duLieu = {
      sku: b.sku.trim(),
      giaBan: so(b.giaBan) ?? so(form.giaBan),
      giaGoc: so(b.giaGoc) ?? so(form.giaGoc),
      nguongCanhBao: so(b.nguongCanhBao),
      hienThi: b.dangBan,
    };
    const cu = sp.bienThes.find((x) => (b.id != null ? x.id === b.id : khoaToHop(x.giaTriIds) === khoaToHop(giaTriIds)));
    sp = cu
      ? await adminApi.bienThe.sua(cu.id, duLieu)
      : await adminApi.bienThe.them(sp.id, { ...duLieu, soLuongTon: Number(b.tonKhoBanDau || 0), giaTriIds });
  }

  const anhServer = (a) => sp.hinhAnhs.find((x) => (a.id != null ? x.id === a.id : x.duongDan === a.duongDan));
  const idAnhGiu = new Set(anhs.map((a) => anhServer(a)?.id).filter((x) => x != null));
  for (const x of sp.hinhAnhs) {
    if (!idAnhGiu.has(x.id)) sp = await adminApi.hinhAnh.xoa(x.id);
  }
  for (const [i, a] of anhs.entries()) {
    const duLieu = {
      duongDan: a.duongDan,
      moTa: a.moTa.trim() || null,
      giaTriTuyChonId: theoKhoa.get(a.giaTriKhoa) ?? null,
      laAnhChinh: a.laAnhChinh,
      thuTu: i,
    };
    const cu = anhServer(a);
    sp = cu ? await adminApi.hinhAnh.sua(cu.id, duLieu) : await adminApi.hinhAnh.them(sp.id, duLieu);
  }
  return sp;
}
