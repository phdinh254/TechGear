import { useEffect, useState } from 'react';
import adminApi from '../api/adminApi.js';
import ErrorMessage from '../../components/ErrorMessage.jsx';
import { useToast } from '../../context/ToastContext.jsx';
import { layThongBaoLoi } from '../../api/axiosClient.js';
import { formatTien } from '../../utils/format.js';

// Nhãn và kiểu ô cho các khóa đã biết; khóa khác (nếu backend thêm) hiện ô chữ với mô tả từ API
const KIEU = {
  ten_cua_hang: { nhan: 'Tên cửa hàng', nhom: 'chung' },
  hotline: { nhan: 'Hotline', nhom: 'chung', type: 'tel' },
  email_lien_he: { nhan: 'Email liên hệ', nhom: 'chung', type: 'email' },
  phi_van_chuyen: { nhan: 'Phí vận chuyển mỗi đơn (đ)', nhom: 'van_chuyen', so: true },
  mien_phi_van_chuyen_tu: { nhan: 'Miễn phí vận chuyển cho đơn từ (đ)', nhom: 'van_chuyen', so: true },
};
const NHOM = [
  { khoa: 'chung', tieuDe: 'Thông tin cửa hàng', moTa: 'Hiện ở chân trang, trang liên hệ và email gửi khách.' },
  { khoa: 'van_chuyen', tieuDe: 'Vận chuyển', moTa: 'Áp dụng ngay cho các đơn đặt sau khi lưu; đơn cũ giữ nguyên phí đã tính.' },
  { khoa: 'khac', tieuDe: 'Khác', moTa: '' },
];

export default function CaiDatPage() {
  const toast = useToast();
  const [ds, setDs] = useState(null);
  const [giaTri, setGiaTri] = useState({});
  const [loiTai, setLoiTai] = useState('');
  const [loi, setLoi] = useState('');
  const [dangLuu, setDangLuu] = useState(false);

  const nap = (kq) => {
    setDs(kq);
    setGiaTri(Object.fromEntries(kq.map((c) => [c.khoa, c.giaTri ?? ''])));
  };

  useEffect(() => {
    adminApi.caiDat.layTatCa().then(nap).catch((err) => setLoiTai(layThongBaoLoi(err)));
  }, []);

  const goc = Object.fromEntries((ds || []).map((c) => [c.khoa, c.giaTri ?? '']));
  const daDoi = Object.keys(giaTri).filter((k) => String(giaTri[k]).trim() !== String(goc[k]));

  const luu = async (e) => {
    e.preventDefault();
    setLoi('');
    setDangLuu(true);
    try {
      // Chỉ gửi các khóa đã đổi
      nap(await adminApi.caiDat.luu(Object.fromEntries(daDoi.map((k) => [k, String(giaTri[k]).trim()]))));
      toast.hien('Đã lưu cấu hình');
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangLuu(false);
    }
  };

  if (loiTai) return <><h1>Cấu hình</h1><ErrorMessage>{loiTai}</ErrorMessage></>;
  if (!ds) return <div className="admin-table-note"><span className="spinner" aria-label="Đang tải" /></div>;

  const THU_TU = Object.keys(KIEU);
  const viTri = (khoa) => (THU_TU.includes(khoa) ? THU_TU.indexOf(khoa) : THU_TU.length);
  const theoNhom = (nhom) => ds
    .filter((c) => (KIEU[c.khoa]?.nhom || 'khac') === nhom)
    .sort((a, b) => viTri(a.khoa) - viTri(b.khoa));
  const phi = Number(giaTri.phi_van_chuyen);
  const mienPhiTu = Number(giaTri.mien_phi_van_chuyen_tu);

  return (
    <form className="form admin-settings" onSubmit={luu} noValidate>
      <h1>Cấu hình</h1>
      <ErrorMessage>{loi}</ErrorMessage>
      {NHOM.map((n) => {
        const cacO = theoNhom(n.khoa);
        if (cacO.length === 0) return null;
        return (
          <section key={n.khoa} className="admin-card admin-form-section">
            <div>
              <h2>{n.tieuDe}</h2>
              {n.moTa && <p className="field-hint">{n.moTa}</p>}
            </div>
            <div className="admin-form-grid">
              {cacO.map((c) => {
                const k = KIEU[c.khoa] || {};
                return (
                  <label key={c.khoa}>{k.nhan || c.moTa || c.khoa}
                    <input
                      type={k.so ? 'number' : k.type || 'text'}
                      min={k.so ? 0 : undefined}
                      step={k.so ? 1000 : undefined}
                      value={giaTri[c.khoa]}
                      onChange={(e) => setGiaTri((g) => ({ ...g, [c.khoa]: e.target.value }))}
                    />
                    {!k.nhan && c.moTa && <small className="field-hint">Khóa: {c.khoa}</small>}
                  </label>
                );
              })}
            </div>
            {n.khoa === 'van_chuyen' && Number.isFinite(phi) && Number.isFinite(mienPhiTu) && (
              <p className="field-hint">
                Khách trả {phi === 0 ? 'không mất phí vận chuyển' : `${formatTien(phi)} phí vận chuyển`}
                {phi > 0 && `, miễn phí khi đơn (sau giảm giá) từ ${formatTien(mienPhiTu)}`}.
              </p>
            )}
          </section>
        );
      })}
      <div className="admin-savebar">
        <span className="field-hint admin-toolbar-grow">{daDoi.length ? `${daDoi.length} thay đổi chưa lưu` : 'Không có thay đổi'}</span>
        <button type="button" className="btn btn-outline" onClick={() => nap(ds)} disabled={dangLuu || !daDoi.length}>Hoàn tác</button>
        <button type="submit" className="btn btn-primary" disabled={dangLuu || !daDoi.length}>{dangLuu ? 'Đang lưu...' : 'Lưu cấu hình'}</button>
      </div>
    </form>
  );
}
