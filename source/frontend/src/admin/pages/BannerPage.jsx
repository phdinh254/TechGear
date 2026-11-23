import { useCallback, useEffect, useState } from 'react';
import adminApi from '../api/adminApi.js';
import DataTable from '../components/DataTable.jsx';
import Modal from '../components/Modal.jsx';
import ConfirmDialog from '../components/ConfirmDialog.jsx';
import ImageUploader from '../components/ImageUploader.jsx';
import ErrorMessage from '../../components/ErrorMessage.jsx';
import { useToast } from '../../context/ToastContext.jsx';
import { layThongBaoLoi } from '../../api/axiosClient.js';
import { duongDanAnh } from '../../utils/format.js';
import { dinhDangThoiGian } from '../../components/OrderView.jsx';
import { sangONhap, tuONhap } from '../utils/thoiGian.js';

const FORM_TRONG = { tieuDe: '', hinhAnh: null, lienKet: '', thuTu: '0', hienThi: true, batDauLuc: '', ketThucLuc: '' };

/** Banner có đang hiện ngoài trang chủ không (bật + trong khung giờ nếu có). */
function tinhTrang(b) {
  const bayGio = Date.now();
  if (!b.hienThi) return { nhan: 'Ẩn', lop: 'admin-badge--muted' };
  if (b.ketThucLuc && new Date(b.ketThucLuc).getTime() < bayGio) return { nhan: 'Hết hạn', lop: 'admin-badge--muted' };
  if (b.batDauLuc && new Date(b.batDauLuc).getTime() > bayGio) return { nhan: 'Chưa đến giờ', lop: 'admin-badge--warning' };
  return { nhan: 'Đang hiện', lop: 'admin-badge--success' };
}

const duLieuGui = (b) => ({
  tieuDe: b.tieuDe || null, hinhAnh: b.hinhAnh, lienKet: b.lienKet || null, thuTu: b.thuTu,
  hienThi: b.hienThi, batDauLuc: b.batDauLuc || null, ketThucLuc: b.ketThucLuc || null,
});

export default function BannerPage() {
  const toast = useToast();
  const [ds, setDs] = useState([]);
  const [dangTai, setDangTai] = useState(true);
  const [loiTai, setLoiTai] = useState('');
  const [dangSua, setDangSua] = useState(null);
  const [form, setForm] = useState(FORM_TRONG);
  const [loi, setLoi] = useState('');
  const [loiTheoO, setLoiTheoO] = useState({});
  const [dangLuu, setDangLuu] = useState(false);
  const [canXoa, setCanXoa] = useState(null);
  const [dangXoa, setDangXoa] = useState(false);
  const [dangDoi, setDangDoi] = useState(null); // id banner đang bật/tắt nhanh

  const taiLai = useCallback(async () => {
    try {
      setDs(await adminApi.banner.layTatCa());
      setLoiTai('');
    } catch (err) {
      setLoiTai(layThongBaoLoi(err));
    } finally {
      setDangTai(false);
    }
  }, []);

  useEffect(() => { taiLai(); }, [taiLai]);

  const moForm = (b) => {
    setLoi('');
    setLoiTheoO({});
    if (b) {
      setForm({
        tieuDe: b.tieuDe || '', hinhAnh: b.hinhAnh, lienKet: b.lienKet || '', thuTu: String(b.thuTu ?? 0),
        hienThi: b.hienThi, batDauLuc: sangONhap(b.batDauLuc), ketThucLuc: sangONhap(b.ketThucLuc),
      });
      setDangSua(b);
    } else {
      setForm({ ...FORM_TRONG, thuTu: String(ds.length) });
      setDangSua({});
    }
  };
  const dongForm = () => { if (!dangLuu) setDangSua(null); };
  const dat = (khoa, giaTri) => setForm((f) => ({ ...f, [khoa]: giaTri }));

  const luu = async (e) => {
    e.preventDefault();
    if (!form.hinhAnh) { setLoi('Vui lòng chọn ảnh banner'); return; }
    setLoi('');
    setLoiTheoO({});
    setDangLuu(true);
    const duLieu = duLieuGui({
      tieuDe: form.tieuDe.trim(), hinhAnh: form.hinhAnh, lienKet: form.lienKet.trim(),
      thuTu: Number(form.thuTu) || 0, hienThi: form.hienThi,
      batDauLuc: tuONhap(form.batDauLuc), ketThucLuc: tuONhap(form.ketThucLuc),
    });
    try {
      if (dangSua.id) await adminApi.banner.capNhat(dangSua.id, duLieu);
      else await adminApi.banner.tao(duLieu);
      toast.hien(dangSua.id ? 'Đã cập nhật banner' : 'Đã thêm banner');
      setDangSua(null);
      await taiLai();
    } catch (err) {
      setLoi(layThongBaoLoi(err));
      setLoiTheoO(err.response?.data?.loi || {});
    } finally {
      setDangLuu(false);
    }
  };

  const doiHienThi = async (b) => {
    setDangDoi(b.id);
    try {
      await adminApi.banner.capNhat(b.id, duLieuGui({ ...b, hienThi: !b.hienThi }));
      await taiLai();
    } catch (err) {
      toast.hien(layThongBaoLoi(err), { loai: 'error' });
    } finally {
      setDangDoi(null);
    }
  };

  const xoa = async () => {
    setDangXoa(true);
    try {
      await adminApi.banner.xoa(canXoa.id);
      toast.hien('Đã xóa banner');
      await taiLai();
    } catch (err) {
      toast.hien(layThongBaoLoi(err), { loai: 'error' });
    } finally {
      setCanXoa(null);
      setDangXoa(false);
    }
  };

  const cot = [
    { khoa: 'hinhAnh', tieuDe: 'Ảnh', hienThi: (b) => <img className="admin-banner-thumb" src={duongDanAnh(b.hinhAnh)} alt="" /> },
    { khoa: 'tieuDe', tieuDe: 'Tiêu đề', hienThi: (b) => (
      <div className="admin-product-name">
        <span>{b.tieuDe || <span className="field-hint">(không tiêu đề)</span>}</span>
        {b.lienKet && <small className="field-hint">{b.lienKet}</small>}
      </div>
    ) },
    { khoa: 'thuTu', tieuDe: 'Thứ tự', canh: 'right' },
    { khoa: 'lich', tieuDe: 'Lịch hiển thị', hienThi: (b) => (b.batDauLuc || b.ketThucLuc ? (
      <div className="admin-product-name">
        <small>{b.batDauLuc ? dinhDangThoiGian(b.batDauLuc) : 'Ngay lập tức'}</small>
        <small className="field-hint">→ {b.ketThucLuc ? dinhDangThoiGian(b.ketThucLuc) : 'Không hạn'}</small>
      </div>
    ) : <span className="field-hint">Luôn hiện</span>) },
    { khoa: 'tinhTrang', tieuDe: 'Tình trạng', hienThi: (b) => { const t = tinhTrang(b); return <span className={`admin-badge ${t.lop}`}>{t.nhan}</span>; } },
    { khoa: 'hienThi', tieuDe: 'Bật', hienThi: (b) => (
      <label className="admin-switch" aria-label={b.hienThi ? 'Tắt banner' : 'Bật banner'}>
        <input type="checkbox" checked={b.hienThi} disabled={dangDoi === b.id} onChange={() => doiHienThi(b)} />
        <span className="admin-switch-track" />
      </label>
    ) },
    { khoa: 'thaoTac', tieuDe: '', canh: 'right', hienThi: (b) => (
      <div className="admin-row-actions">
        <button type="button" className="btn btn-outline btn-sm" onClick={() => moForm(b)}>Sửa</button>
        <button type="button" className="btn btn-outline btn-sm" onClick={() => setCanXoa(b)}>Xóa</button>
      </div>
    ) },
  ];

  return (
    <>
      <div className="admin-toolbar">
        <h1 className="admin-toolbar-grow">Banner</h1>
        <button type="button" className="btn btn-primary" onClick={() => moForm(null)}>Thêm banner</button>
      </div>
      <p className="field-hint admin-intro">Banner hiện ở trang chủ theo thứ tự tăng dần. Ảnh nên có tỉ lệ 16:7 (VD 1600 × 700).</p>
      <ErrorMessage>{loiTai}</ErrorMessage>
      <DataTable cot={cot} duLieu={ds} dangTai={dangTai} trong="Chưa có banner" />

      <Modal tieuDe={dangSua?.id ? 'Sửa banner' : 'Thêm banner'} mo={dangSua !== null} onClose={dongForm} rong>
        <form className="form" onSubmit={luu} noValidate>
          <ErrorMessage>{loi}</ErrorMessage>
          <div className="field">Ảnh banner
            <ImageUploader thuMuc="banner" giaTri={form.hinhAnh} onChange={(v) => dat('hinhAnh', v)} />
            {loiTheoO.hinhAnh && <small className="field-error">{loiTheoO.hinhAnh}</small>}
          </div>
          <div className="admin-form-grid">
            <label>Tiêu đề
              <input value={form.tieuDe} onChange={(e) => dat('tieuDe', e.target.value)} maxLength={200} />
              {loiTheoO.tieuDe && <small className="field-error">{loiTheoO.tieuDe}</small>}
            </label>
            <label>Liên kết khi bấm
              <input value={form.lienKet} onChange={(e) => dat('lienKet', e.target.value)} maxLength={500} placeholder="/san-pham?danhMuc=tai-nghe" />
              <small className="field-hint">Bắt đầu bằng / hoặc http(s)://</small>
              {loiTheoO.lienKet && <small className="field-error">{loiTheoO.lienKet}</small>}
            </label>
            <label>Bắt đầu hiện
              <input type="datetime-local" value={form.batDauLuc} onChange={(e) => dat('batDauLuc', e.target.value)} />
              <small className="field-hint">Để trống = hiện ngay</small>
            </label>
            <label>Ngừng hiện
              <input type="datetime-local" value={form.ketThucLuc} min={form.batDauLuc || undefined} onChange={(e) => dat('ketThucLuc', e.target.value)} />
              <small className="field-hint">Để trống = không hạn</small>
              {loiTheoO.ketThucLuc && <small className="field-error">{loiTheoO.ketThucLuc}</small>}
            </label>
            <label>Thứ tự
              <input type="number" min={0} value={form.thuTu} onChange={(e) => dat('thuTu', e.target.value)} />
            </label>
            <label className="checkbox-label admin-form-checks">
              <input type="checkbox" checked={form.hienThi} onChange={(e) => dat('hienThi', e.target.checked)} /> Hiển thị trên trang chủ
            </label>
          </div>
          <div className="modal-actions">
            <button type="button" className="btn btn-outline" onClick={dongForm} disabled={dangLuu}>Hủy</button>
            <button type="submit" className="btn btn-primary" disabled={dangLuu}>{dangLuu ? 'Đang lưu...' : 'Lưu'}</button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        mo={canXoa !== null}
        tieuDe="Xóa banner"
        noiDung={`Xóa banner${canXoa?.tieuDe ? ` "${canXoa.tieuDe}"` : ''}? Thao tác không hoàn tác được.`}
        nguyHiem
        dangXuLy={dangXoa}
        onXacNhan={xoa}
        onHuy={() => setCanXoa(null)}
      />
    </>
  );
}
