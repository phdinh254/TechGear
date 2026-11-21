import { useRef, useState } from 'react';
import adminApi from '../api/adminApi.js';
import ErrorMessage from '../../components/ErrorMessage.jsx';
import { duongDanAnh } from '../../utils/format.js';
import { layThongBaoLoi } from '../../api/axiosClient.js';

const TOI_DA = 5 * 1024 * 1024;

/**
 * anhs: [{ khoa, duongDan, moTa, laAnhChinh, giaTriKhoa }] — giaTriKhoa '' = ảnh chung.
 * onChange nhận mảng mới hoặc hàm cập nhật (mảng cũ) => mảng mới.
 * tuyChonGiaTris: [{ khoa, tuyChon, giaTri }] — các cặp tùy chọn / giá trị đang có.
 */
export default function ProductImages({ anhs, onChange, tuyChonGiaTris }) {
  const inputRef = useRef(null);
  const [dangTai, setDangTai] = useState(false);
  const [loi, setLoi] = useState('');

  const chonFile = async (e) => {
    const files = [...(e.target.files || [])];
    e.target.value = '';
    if (files.length === 0) return;
    const sai = files.find((f) => !f.type.startsWith('image/') || f.size > TOI_DA);
    if (sai) {
      setLoi(`${sai.name}: chỉ chấp nhận file ảnh tối đa 5MB`);
      return;
    }
    setLoi('');
    setDangTai(true);
    try {
      const duongDans = await Promise.all(files.map((f) => adminApi.taiAnh(f, 'san-pham')));
      // Dùng hàm cập nhật: giữ các thay đổi ảnh (xóa, đổi thứ tự, ảnh chính...) làm trong lúc đang tải.
      onChange((truoc) => {
        const coAnhChinh = truoc.some((a) => a.laAnhChinh);
        return [
          ...truoc,
          ...duongDans.map((duongDan, i) => ({
            khoa: crypto.randomUUID(), duongDan, moTa: '', laAnhChinh: !coAnhChinh && i === 0, giaTriKhoa: '',
          })),
        ];
      });
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangTai(false);
    }
  };

  const doiCho = (i, j) => {
    const moi = [...anhs];
    [moi[i], moi[j]] = [moi[j], moi[i]];
    onChange(moi);
  };

  const xoa = (khoa) => {
    const conLai = anhs.filter((a) => a.khoa !== khoa);
    if (conLai.length && !conLai.some((a) => a.laAnhChinh)) conLai[0] = { ...conLai[0], laAnhChinh: true };
    onChange(conLai);
  };

  const sua = (khoa, thayDoi) => onChange(anhs.map((a) => (a.khoa === khoa ? { ...a, ...thayDoi } : a)));

  const datAnhChinh = (khoa) => onChange(anhs.map((a) => ({ ...a, laAnhChinh: a.khoa === khoa })));

  const coGiaTri = new Set(tuyChonGiaTris.map((g) => g.khoa));

  return (
    <div className="product-images">
      {anhs.length > 0 && (
        <ul className="product-images-list">
          {anhs.map((a, i) => (
            <li key={a.khoa} className={`product-image${a.laAnhChinh ? ' is-main' : ''}`}>
              <img src={duongDanAnh(a.duongDan)} alt={a.moTa || `Ảnh ${i + 1}`} />
              <label className="checkbox-label">
                <input type="radio" name="anh-chinh" checked={a.laAnhChinh} onChange={() => datAnhChinh(a.khoa)} /> Ảnh chính
              </label>
              <label className="product-image-field">Gắn với giá trị
                <select value={coGiaTri.has(a.giaTriKhoa) ? a.giaTriKhoa : ''} onChange={(e) => sua(a.khoa, { giaTriKhoa: e.target.value })}>
                  <option value="">— Ảnh chung —</option>
                  {tuyChonGiaTris.map((g) => <option key={g.khoa} value={g.khoa}>{g.tuyChon} / {g.giaTri}</option>)}
                </select>
              </label>
              <div className="product-image-actions">
                <button type="button" className="icon-btn" aria-label="Chuyển ảnh sang trái" disabled={i === 0} onClick={() => doiCho(i, i - 1)}>←</button>
                <button type="button" className="icon-btn" aria-label="Chuyển ảnh sang phải" disabled={i === anhs.length - 1} onClick={() => doiCho(i, i + 1)}>→</button>
                <button type="button" className="btn btn-outline btn-sm" onClick={() => xoa(a.khoa)}>Xóa</button>
              </div>
            </li>
          ))}
        </ul>
      )}
      <div>
        <button type="button" className="btn btn-outline" onClick={() => inputRef.current?.click()} disabled={dangTai}>
          {dangTai ? 'Đang tải...' : '+ Tải ảnh lên'}
        </button>
        <span className="field-hint"> Chọn được nhiều ảnh, mỗi ảnh tối đa 5MB.</span>
      </div>
      <input ref={inputRef} type="file" accept="image/*" multiple hidden onChange={chonFile} />
      <ErrorMessage>{loi}</ErrorMessage>
    </div>
  );
}
