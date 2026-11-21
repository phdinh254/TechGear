import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import accountApi from '../api/accountApi';
import { layThongBaoLoi } from '../api/axiosClient';
import AccountShell, { Avatar } from '../components/AccountShell.jsx';
import ErrorMessage from '../components/ErrorMessage.jsx';
import EmptyState from '../components/ui/EmptyState.jsx';
import Icon from '../components/ui/Icon.jsx';
import PasswordInput from '../components/ui/PasswordInput.jsx';
import { SkeletonBlock } from '../components/ui/Skeleton.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import { PasswordHints } from './RegisterPage.jsx';

const DIA_CHI_RONG = { tenNguoiNhan: '', soDienThoai: '', tinhThanh: '', phuongXa: '', diaChiChiTiet: '', laMacDinh: false };

function HoSo() {
  const { nguoiDung, capNhatNguoiDung } = useAuth();
  const toast = useToast();
  const [hoSo, setHoSo] = useState({
    hoTen: nguoiDung.hoTen || '',
    soDienThoai: nguoiDung.soDienThoai || '',
    gioiTinh: nguoiDung.gioiTinh || '',
    ngaySinh: nguoiDung.ngaySinh || '',
  });
  const [dangGui, setDangGui] = useState(false);
  const [loi, setLoi] = useState('');

  const luu = async (e) => {
    e.preventDefault();
    setLoi('');
    setDangGui(true);
    try {
      capNhatNguoiDung(await accountApi.capNhatHoSo({ ...hoSo, gioiTinh: hoSo.gioiTinh || null, ngaySinh: hoSo.ngaySinh || null }));
      toast.hien('Đã lưu thông tin cá nhân');
    } catch (error) {
      setLoi(layThongBaoLoi(error));
    } finally {
      setDangGui(false);
    }
  };

  const doiAnh = async (e) => {
    const file = e.target.files?.[0];
    e.target.value = '';
    if (!file) return;
    setLoi('');
    setDangGui(true);
    try {
      capNhatNguoiDung(await accountApi.doiAnhDaiDien(file));
      toast.hien('Đã cập nhật ảnh đại diện');
    } catch (error) {
      setLoi(layThongBaoLoi(error));
    } finally {
      setDangGui(false);
    }
  };

  return (
    <div className="panel">
      <h1 className="panel-title">Thông tin cá nhân</h1>
      <ErrorMessage>{loi}</ErrorMessage>
      <div className="avatar-row">
        <Avatar nguoiDung={nguoiDung} size={72} />
        <div>
          <label className="btn btn-outline btn-sm avatar-upload">
            <Icon name="user" size={16} /> Đổi ảnh đại diện
            <input type="file" accept="image/jpeg,image/png,image/webp,image/gif" disabled={dangGui} onChange={doiAnh} className="visually-hidden" />
          </label>
          <p className="field-hint">JPG, PNG, WEBP hoặc GIF · tối đa 5MB</p>
        </div>
      </div>
      <form className="form" onSubmit={luu}>
        <div className="form-grid">
          <label className="field">Email<input value={nguoiDung.email} disabled /></label>
          <label className="field">Họ và tên<input required maxLength="100" value={hoSo.hoTen} onChange={(e) => setHoSo({ ...hoSo, hoTen: e.target.value })} /></label>
          <label className="field">Số điện thoại<input type="tel" pattern="0[0-9]{9}" title="10 chữ số, bắt đầu bằng 0" value={hoSo.soDienThoai} onChange={(e) => setHoSo({ ...hoSo, soDienThoai: e.target.value })} /></label>
          <label className="field">Giới tính
            <select value={hoSo.gioiTinh} onChange={(e) => setHoSo({ ...hoSo, gioiTinh: e.target.value })}>
              <option value="">Chưa chọn</option><option value="nam">Nam</option><option value="nu">Nữ</option><option value="khac">Khác</option>
            </select>
          </label>
          <label className="field">Ngày sinh<input type="date" max={new Date().toISOString().slice(0, 10)} value={hoSo.ngaySinh} onChange={(e) => setHoSo({ ...hoSo, ngaySinh: e.target.value })} /></label>
        </div>
        <div><button className="btn btn-primary" disabled={dangGui}>{dangGui ? 'Đang lưu...' : 'Lưu thay đổi'}</button></div>
      </form>
    </div>
  );
}

function DoiMatKhau() {
  const toast = useToast();
  const [matKhau, setMatKhau] = useState({ matKhauCu: '', matKhauMoi: '', nhapLai: '' });
  const [dangGui, setDangGui] = useState(false);
  const [loi, setLoi] = useState('');

  const doi = async (e) => {
    e.preventDefault();
    setLoi('');
    if (matKhau.matKhauMoi !== matKhau.nhapLai) {
      setLoi('Mật khẩu nhập lại không khớp');
      return;
    }
    setDangGui(true);
    try {
      const { nhapLai, ...duLieu } = matKhau;
      const res = await accountApi.doiMatKhau(duLieu);
      toast.hien(res.thongBao || 'Đã đổi mật khẩu');
      setMatKhau({ matKhauCu: '', matKhauMoi: '', nhapLai: '' });
    } catch (error) {
      setLoi(layThongBaoLoi(error));
    } finally {
      setDangGui(false);
    }
  };

  return (
    <div className="panel">
      <h1 className="panel-title">Đổi mật khẩu</h1>
      <ErrorMessage>{loi}</ErrorMessage>
      <form className="form form-narrow" onSubmit={doi}>
        <div className="field"><label htmlFor="mk-cu">Mật khẩu hiện tại</label><PasswordInput id="mk-cu" required autoComplete="current-password" value={matKhau.matKhauCu} onChange={(e) => setMatKhau({ ...matKhau, matKhauCu: e.target.value })} /></div>
        <div className="field">
          <label htmlFor="mk-moi-tk">Mật khẩu mới</label>
          <PasswordInput id="mk-moi-tk" required minLength="8" maxLength="100" pattern="(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]).{8,100}" title="Ít nhất 8 ký tự, gồm chữ hoa, chữ thường và số" autoComplete="new-password" value={matKhau.matKhauMoi} onChange={(e) => setMatKhau({ ...matKhau, matKhauMoi: e.target.value })} />
          <PasswordHints matKhau={matKhau.matKhauMoi} />
        </div>
        <div className="field"><label htmlFor="mk-lai-tk">Nhập lại mật khẩu mới</label><PasswordInput id="mk-lai-tk" required minLength="8" maxLength="100" autoComplete="new-password" value={matKhau.nhapLai} onChange={(e) => setMatKhau({ ...matKhau, nhapLai: e.target.value })} /></div>
        <div><button className="btn btn-primary" disabled={dangGui}>{dangGui ? 'Đang lưu...' : 'Đổi mật khẩu'}</button></div>
      </form>
    </div>
  );
}

function SoDiaChi() {
  const toast = useToast();
  const [diaChis, setDiaChis] = useState([]);
  const [dangTai, setDangTai] = useState(true);
  const [form, setForm] = useState(DIA_CHI_RONG);
  const [dangSua, setDangSua] = useState(null);
  const [moForm, setMoForm] = useState(false);
  const [dangGui, setDangGui] = useState(false);
  const [loi, setLoi] = useState('');

  const tai = () => {
    setDangTai(true);
    accountApi.layDiaChi()
      .then(setDiaChis)
      .catch((error) => setLoi(layThongBaoLoi(error)))
      .finally(() => setDangTai(false));
  };
  useEffect(tai, []);

  const dongForm = () => { setDangSua(null); setForm(DIA_CHI_RONG); setMoForm(false); };

  const luu = async (e) => {
    e.preventDefault();
    setLoi('');
    setDangGui(true);
    try {
      if (dangSua) await accountApi.capNhatDiaChi(dangSua, form);
      else await accountApi.taoDiaChi(form);
      toast.hien(dangSua ? 'Đã cập nhật địa chỉ' : 'Đã thêm địa chỉ mới');
      dongForm();
      tai();
    } catch (error) {
      setLoi(layThongBaoLoi(error));
    } finally {
      setDangGui(false);
    }
  };

  const thaoTac = async (ham, thongBao) => {
    try { await ham(); toast.hien(thongBao); tai(); } catch (error) { setLoi(layThongBaoLoi(error)); }
  };

  const doi = (khoa) => (e) => setForm({ ...form, [khoa]: e.target.value });

  return (
    <div className="panel">
      <div className="panel-head">
        <h1 className="panel-title">Sổ địa chỉ</h1>
        {!moForm && <button type="button" className="btn btn-outline btn-sm" onClick={() => setMoForm(true)}><Icon name="plus" size={16} /> Thêm địa chỉ</button>}
      </div>
      <ErrorMessage>{loi}</ErrorMessage>

      {moForm && (
        <form className="form address-form" onSubmit={luu}>
          <h2>{dangSua ? 'Sửa địa chỉ' : 'Địa chỉ mới'}</h2>
          <div className="form-grid">
            <label className="field">Tên người nhận<input required maxLength="100" value={form.tenNguoiNhan} onChange={doi('tenNguoiNhan')} /></label>
            <label className="field">Số điện thoại<input required type="tel" pattern="0[0-9]{9}" title="10 chữ số, bắt đầu bằng 0" value={form.soDienThoai} onChange={doi('soDienThoai')} /></label>
            <label className="field">Tỉnh / thành phố<input required maxLength="100" value={form.tinhThanh} onChange={doi('tinhThanh')} /></label>
            <label className="field">Phường / xã<input required maxLength="100" value={form.phuongXa} onChange={doi('phuongXa')} /></label>
            <label className="field span-2">Địa chỉ chi tiết<input required maxLength="255" value={form.diaChiChiTiet} onChange={doi('diaChiChiTiet')} /></label>
          </div>
          <label className="checkbox-label"><input type="checkbox" checked={form.laMacDinh} onChange={(e) => setForm({ ...form, laMacDinh: e.target.checked })} /> Đặt làm địa chỉ mặc định</label>
          <div className="inline-actions">
            <button className="btn btn-primary" disabled={dangGui}>{dangSua ? 'Lưu địa chỉ' : 'Thêm địa chỉ'}</button>
            <button type="button" className="btn btn-ghost" onClick={dongForm}>Hủy</button>
          </div>
        </form>
      )}

      {dangTai ? (
        <div className="address-grid"><SkeletonBlock height={120} /><SkeletonBlock height={120} /></div>
      ) : diaChis.length === 0 ? (
        !moForm && <EmptyState compact icon="mapPin" tieuDe="Chưa có địa chỉ" moTa="Lưu địa chỉ để thanh toán nhanh hơn ở lần mua sau." />
      ) : (
        <div className="address-grid">
          {diaChis.map((dc) => (
            <article key={dc.id} className={`address-card${dc.laMacDinh ? ' is-default' : ''}`}>
              <div className="address-card-head">
                <strong>{dc.tenNguoiNhan}</strong>
                {dc.laMacDinh && <span className="badge badge-soft">Mặc định</span>}
              </div>
              <p className="muted">{dc.soDienThoai}</p>
              <p>{dc.diaChiDayDu || `${dc.diaChiChiTiet}, ${dc.phuongXa}, ${dc.tinhThanh}`}</p>
              <div className="inline-actions">
                <button type="button" className="link-button" onClick={() => { setDangSua(dc.id); setForm({ ...dc }); setMoForm(true); window.scrollTo({ top: 0, behavior: 'smooth' }); }}>Sửa</button>
                {!dc.laMacDinh && <button type="button" className="link-button" onClick={() => thaoTac(() => accountApi.datDiaChiMacDinh(dc.id), 'Đã đặt làm mặc định')}>Đặt mặc định</button>}
                <button type="button" className="link-button text-danger" onClick={() => { if (window.confirm('Xóa địa chỉ này?')) thaoTac(() => accountApi.xoaDiaChi(dc.id), 'Đã xóa địa chỉ'); }}>Xóa</button>
              </div>
            </article>
          ))}
        </div>
      )}
    </div>
  );
}

export default function AccountPage() {
  const [params] = useSearchParams();
  const tab = params.get('tab') || 'ho-so';
  return (
    <AccountShell>
      {tab === 'dia-chi' ? <SoDiaChi /> : tab === 'mat-khau' ? <DoiMatKhau /> : <HoSo />}
    </AccountShell>
  );
}
