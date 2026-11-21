// Hàm thuần xử lý biến thể sản phẩm (không phụ thuộc React/Vite).

export function taoSlug(chuoi) {
  return String(chuoi ?? '')
    .trim()
    .replace(/đ/g, 'd')
    .replace(/Đ/g, 'D')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '');
}

export function vietTat(ten) {
  return taoSlug(ten)
    .split('-')
    .filter(Boolean)
    .slice(0, 5)
    .map((tu) => tu[0])
    .join('')
    .toUpperCase();
}

export function goiYSku(tenSanPham, giaTris) {
  const phan = (giaTris || []).map((gt) => taoSlug(gt).toUpperCase());
  return ['TG', vietTat(tenSanPham), ...phan].join('-');
}

export function sinhToHop(tuyChons) {
  let ketQua = [{ khoa: '', giaTris: [] }];
  for (const tc of tuyChons || []) {
    if (!tc.giaTris || tc.giaTris.length === 0) continue;
    const moi = [];
    for (const goc of ketQua) {
      for (const gt of tc.giaTris) {
        moi.push({
          khoa: goc.giaTris.length === 0 ? gt.khoa : `${goc.khoa}|${gt.khoa}`,
          giaTris: [...goc.giaTris, gt],
        });
      }
    }
    ketQua = moi;
  }
  return ketQua;
}

export function ghepBienThe(toHops, bienTheCu, tenSanPham) {
  const theoKhoa = new Map((bienTheCu || []).map((b) => [b.khoa, b]));
  return toHops.map((th) => {
    const cu = theoKhoa.get(th.khoa);
    if (cu) return cu;
    return {
      khoa: th.khoa,
      giaTris: th.giaTris,
      id: null,
      sku: goiYSku(tenSanPham, th.giaTris.map((g) => g.giaTri)),
      giaBan: '',
      giaGoc: '',
      tonKhoBanDau: 0,
      nguongCanhBao: 5,
      dangBan: true,
    };
  });
}

export function skuTrung(dongs) {
  const dem = new Map();
  for (const d of dongs || []) {
    const s = String(d.sku ?? '').trim().toUpperCase();
    if (!s) continue;
    dem.set(s, (dem.get(s) || 0) + 1);
  }
  return new Set([...dem].filter(([, n]) => n >= 2).map(([s]) => s));
}
