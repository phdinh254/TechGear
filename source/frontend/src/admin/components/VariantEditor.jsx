import { useEffect, useRef, useState } from 'react';
import { ghepBienThe, goiYSku, sinhToHop, skuTrung } from '../utils/bienThe.js';
import { formatTien } from '../../utils/format.js';

export const TOI_DA_TUY_CHON = 3;
export const TOI_DA_BIEN_THE = 50;

export const LOAI_TUY_CHON = [
  { giaTri: 'mau_sac', nhan: 'Màu sắc' },
  { giaTri: 'dung_luong', nhan: 'Dung lượng' },
  { giaTri: 'phien_ban', nhan: 'Phiên bản' },
  { giaTri: 'khac', nhan: 'Khác' },
];

const SKU_HOP_LE = /^[A-Za-z0-9._-]+$/;
const MA_MAU_HOP_LE = /^#[0-9A-Fa-f]{6}$/;

/** Lỗi của một ô SKU (chuỗi rỗng = hợp lệ). trung: Set SKU viết hoa bị trùng (từ skuTrung). */
export function loiSku(sku, trung) {
  const s = String(sku ?? '').trim();
  if (!s) return 'Nhập SKU';
  if (s.length > 100) return 'SKU tối đa 100 ký tự';
  if (!SKU_HOP_LE.test(s)) return 'SKU chỉ gồm chữ, số, dấu chấm, gạch ngang, gạch dưới';
  if (trung.has(s.toUpperCase())) return 'SKU bị trùng';
  return '';
}

/** Lỗi khai báo tùy chọn (chuỗi rỗng = hợp lệ). */
export function loiTuyChon(tuyChons) {
  const tenDaCo = new Set();
  for (const tc of tuyChons) {
    const ten = tc.ten.trim();
    if (!ten) return 'Nhập tên cho mọi tùy chọn';
    if (tenDaCo.has(ten.toLowerCase())) return `Tên tùy chọn bị trùng: ${ten}`;
    tenDaCo.add(ten.toLowerCase());
    if (tc.giaTris.length === 0) return `Tùy chọn "${ten}" chưa có giá trị nào`;
    const gtDaCo = new Set();
    for (const gt of tc.giaTris) {
      const g = gt.giaTri.trim();
      if (!g) return `Tùy chọn "${ten}" có giá trị để trống`;
      if (gtDaCo.has(g.toLowerCase())) return `Giá trị "${g}" bị trùng trong tùy chọn "${ten}"`;
      gtDaCo.add(g.toLowerCase());
      if (gt.maMau && !MA_MAU_HOP_LE.test(gt.maMau)) return `Mã màu của "${g}" phải dạng #RRGGBB`;
    }
  }
  return '';
}

const soToHop = (tuyChons) => tuyChons.reduce((tich, tc) => tich * (tc.giaTris.length || 1), 1);

/** Ghép lại bảng biến thể theo tùy chọn mới; cập nhật giaTris để tên giá trị vừa đổi hiện đúng. */
function tinhBienThe(tuyChons, bienThes, tenSanPham) {
  const toHops = sinhToHop(tuyChons);
  return ghepBienThe(toHops, bienThes, tenSanPham).map((b, i) => ({ ...b, giaTris: toHops[i].giaTris }));
}

const APDUNG_TRONG = { giaBan: '', giaGoc: '', nguongCanhBao: '' };

export default function VariantEditor({ tenSanPham, tuyChons, onTuyChonsChange, bienThes, onBienThesChange, giaBanSanPham }) {
  const [thongBao, setThongBao] = useState('');
  const [giaTriMoi, setGiaTriMoi] = useState({}); // khoa tùy chọn -> chữ đang gõ
  const [apDung, setApDung] = useState(APDUNG_TRONG);
  const tenTruoc = useRef(tenSanPham);

  // Đổi tên sản phẩm: SKU gợi ý của biến thể mới (chưa bị sửa tay) đi theo tên mới.
  useEffect(() => {
    const cu = tenTruoc.current;
    tenTruoc.current = tenSanPham;
    if (cu === tenSanPham) return;
    let doi = false;
    const moi = bienThes.map((b) => {
      const ten = b.giaTris.map((g) => g.giaTri);
      if (b.id == null && b.sku === goiYSku(cu, ten)) {
        doi = true;
        return { ...b, sku: goiYSku(tenSanPham, ten) };
      }
      return b;
    });
    if (doi) onBienThesChange(moi);
  }, [tenSanPham]); // chỉ chạy khi tên đổi

  const doiTuyChon = (moi) => {
    if (soToHop(moi) > TOI_DA_BIEN_THE) {
      setThongBao(`Tối đa ${TOI_DA_BIEN_THE} biến thể: không thể thêm giá trị này`);
      return false;
    }
    setThongBao('');
    onTuyChonsChange(moi);
    onBienThesChange(tinhBienThe(moi, bienThes, tenSanPham));
    return true;
  };

  const suaTuyChon = (khoa, thayDoi) => doiTuyChon(tuyChons.map((tc) => (tc.khoa === khoa ? { ...tc, ...thayDoi } : tc)));

  const themTuyChon = () => {
    if (tuyChons.length >= TOI_DA_TUY_CHON) {
      setThongBao(`Tối đa ${TOI_DA_TUY_CHON} tùy chọn cho mỗi sản phẩm`);
      return;
    }
    doiTuyChon([...tuyChons, { khoa: crypto.randomUUID(), id: null, ten: '', loai: tuyChons.length === 0 ? 'mau_sac' : 'khac', giaTris: [] }]);
  };

  const xoaTuyChon = (khoa) => doiTuyChon(tuyChons.filter((tc) => tc.khoa !== khoa));

  const themGiaTri = (tc) => {
    const ten = (giaTriMoi[tc.khoa] || '').trim();
    if (!ten) return;
    if (tc.giaTris.some((gt) => gt.giaTri.trim().toLowerCase() === ten.toLowerCase())) {
      setThongBao(`Giá trị "${ten}" đã có trong tùy chọn này`);
      return;
    }
    const ok = suaTuyChon(tc.khoa, { giaTris: [...tc.giaTris, { khoa: crypto.randomUUID(), id: null, giaTri: ten, maMau: '' }] });
    if (ok) setGiaTriMoi((g) => ({ ...g, [tc.khoa]: '' }));
  };

  const suaGiaTri = (tc, khoaGt, thayDoi) =>
    suaTuyChon(tc.khoa, { giaTris: tc.giaTris.map((gt) => (gt.khoa === khoaGt ? { ...gt, ...thayDoi } : gt)) });

  const xoaGiaTri = (tc, khoaGt) => suaTuyChon(tc.khoa, { giaTris: tc.giaTris.filter((gt) => gt.khoa !== khoaGt) });

  const suaBienThe = (khoa, thayDoi) => onBienThesChange(bienThes.map((b) => (b.khoa === khoa ? { ...b, ...thayDoi } : b)));

  const apDungTatCa = () => {
    const thayDoi = {};
    for (const [k, v] of Object.entries(apDung)) if (String(v).trim() !== '') thayDoi[k] = v;
    if (Object.keys(thayDoi).length === 0) return;
    onBienThesChange(bienThes.map((b) => ({ ...b, ...thayDoi })));
    setApDung(APDUNG_TRONG);
  };

  const trung = skuTrung(bienThes);

  return (
    <div className="variant-editor">
      <div className="variant-options">
        {tuyChons.map((tc) => (
          <div key={tc.khoa} className="variant-option">
            <div className="variant-option-head">
              <input aria-label="Tên tùy chọn" placeholder="Tên tùy chọn (vd: Màu sắc)" maxLength={50} value={tc.ten}
                onChange={(e) => suaTuyChon(tc.khoa, { ten: e.target.value })} />
              <select aria-label="Loại tùy chọn" value={tc.loai} onChange={(e) => suaTuyChon(tc.khoa, { loai: e.target.value })}>
                {LOAI_TUY_CHON.map((l) => <option key={l.giaTri} value={l.giaTri}>{l.nhan}</option>)}
              </select>
              <button type="button" className="btn btn-outline btn-sm" onClick={() => xoaTuyChon(tc.khoa)}>Xóa tùy chọn</button>
            </div>
            <div className="variant-values">
              {tc.giaTris.map((gt) => (
                <div key={gt.khoa} className="variant-value">
                  {tc.loai === 'mau_sac' && (
                    <input type="color" aria-label={`Chọn màu cho ${gt.giaTri}`} value={MA_MAU_HOP_LE.test(gt.maMau || '') ? gt.maMau : '#000000'}
                      onChange={(e) => suaGiaTri(tc, gt.khoa, { maMau: e.target.value.toUpperCase() })} />
                  )}
                  <input aria-label="Tên giá trị" maxLength={50} value={gt.giaTri} onChange={(e) => suaGiaTri(tc, gt.khoa, { giaTri: e.target.value })} />
                  {tc.loai === 'mau_sac' && (
                    <input className="variant-color-code" aria-label={`Mã màu của ${gt.giaTri}`} placeholder="#RRGGBB" maxLength={7} value={gt.maMau || ''}
                      onChange={(e) => suaGiaTri(tc, gt.khoa, { maMau: e.target.value.trim() })} />
                  )}
                  <button type="button" className="icon-btn variant-value-remove" aria-label={`Xóa giá trị ${gt.giaTri}`} onClick={() => xoaGiaTri(tc, gt.khoa)}>×</button>
                </div>
              ))}
              <div className="variant-value variant-value--new">
                <input placeholder="Thêm giá trị rồi nhấn Enter" maxLength={50} value={giaTriMoi[tc.khoa] || ''}
                  onChange={(e) => setGiaTriMoi((g) => ({ ...g, [tc.khoa]: e.target.value }))}
                  onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); themGiaTri(tc); } }} />
                <button type="button" className="btn btn-outline btn-sm" onClick={() => themGiaTri(tc)}>Thêm</button>
              </div>
            </div>
          </div>
        ))}
        <div className="admin-toolbar">
          <button type="button" className="btn btn-outline btn-sm" onClick={themTuyChon}>+ Thêm tùy chọn</button>
          <span className="field-hint">Tối đa {TOI_DA_TUY_CHON} tùy chọn, {TOI_DA_BIEN_THE} biến thể. Không có tùy chọn = một biến thể mặc định.</span>
        </div>
        {thongBao && <small className="field-error" role="alert">{thongBao}</small>}
      </div>

      <div className="admin-table-wrap">
        <table className="admin-table variant-table">
          <thead>
            <tr>
              <th>Phân loại</th>
              <th>SKU</th>
              <th>Giá bán</th>
              <th>Giá gốc</th>
              <th>Tồn kho</th>
              <th>Ngưỡng cảnh báo</th>
              <th>Đang bán</th>
            </tr>
          </thead>
          <tbody>
            {bienThes.length > 1 && (
              <tr className="variant-apply-row">
                <td>Áp dụng cho tất cả</td>
                <td />
                <td><input type="number" min={1} aria-label="Giá bán cho tất cả" placeholder="Giá bán" value={apDung.giaBan} onChange={(e) => setApDung((a) => ({ ...a, giaBan: e.target.value }))} /></td>
                <td><input type="number" min={1} aria-label="Giá gốc cho tất cả" placeholder="Giá gốc" value={apDung.giaGoc} onChange={(e) => setApDung((a) => ({ ...a, giaGoc: e.target.value }))} /></td>
                <td />
                <td><input type="number" min={0} aria-label="Ngưỡng cho tất cả" placeholder="Ngưỡng" value={apDung.nguongCanhBao} onChange={(e) => setApDung((a) => ({ ...a, nguongCanhBao: e.target.value }))} /></td>
                <td><button type="button" className="btn btn-outline btn-sm" onClick={apDungTatCa}>Áp dụng</button></td>
              </tr>
            )}
            {bienThes.map((b) => {
              const loi = loiSku(b.sku, trung);
              return (
                <tr key={b.khoa || 'mac-dinh'}>
                  <td>{b.giaTris.length ? b.giaTris.map((g) => g.giaTri).join(' / ') : 'Mặc định'}</td>
                  <td>
                    <input className={loi ? 'is-invalid' : undefined} aria-label="SKU" aria-invalid={!!loi} value={b.sku}
                      onChange={(e) => suaBienThe(b.khoa, { sku: e.target.value })} />
                    {loi && <small className="field-error">{loi}</small>}
                  </td>
                  <td><input type="number" min={1} aria-label="Giá bán riêng" placeholder={giaBanSanPham ? formatTien(giaBanSanPham) : 'Theo sản phẩm'} value={b.giaBan}
                    onChange={(e) => suaBienThe(b.khoa, { giaBan: e.target.value })} /></td>
                  <td><input type="number" min={1} aria-label="Giá gốc riêng" placeholder="Theo sản phẩm" value={b.giaGoc}
                    onChange={(e) => suaBienThe(b.khoa, { giaGoc: e.target.value })} /></td>
                  <td>
                    {b.id == null ? (
                      <input type="number" min={0} aria-label="Tồn kho ban đầu" value={b.tonKhoBanDau}
                        onChange={(e) => suaBienThe(b.khoa, { tonKhoBanDau: e.target.value })} />
                    ) : (
                      <span className="variant-stock">{b.soLuongTon}<small className="field-hint">Sửa ở trang Tồn kho</small></span>
                    )}
                  </td>
                  <td><input type="number" min={0} aria-label="Ngưỡng cảnh báo" value={b.nguongCanhBao}
                    onChange={(e) => suaBienThe(b.khoa, { nguongCanhBao: e.target.value })} /></td>
                  <td><input type="checkbox" aria-label="Đang bán" checked={b.dangBan} onChange={(e) => suaBienThe(b.khoa, { dangBan: e.target.checked })} /></td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
      <p className="field-hint">Giá riêng để trống = dùng giá sản phẩm.</p>
    </div>
  );
}
