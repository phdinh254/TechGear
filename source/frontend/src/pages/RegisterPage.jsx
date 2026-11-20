import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import { layThongBaoLoi } from '../api/axiosClient';
import AuthLayout from '../components/AuthLayout.jsx';
import ErrorMessage from '../components/ErrorMessage.jsx';
import GoogleButton from '../components/ui/GoogleButton.jsx';
import PasswordInput from '../components/ui/PasswordInput.jsx';

const MAU_MAT_KHAU = '(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]).{8,100}';

/** Kiểm tra 3 tiêu chí mật khẩu để hiển thị gợi ý trực quan. */
export function tieuChiMatKhau(mk) {
  return [
    { nhan: 'Ít nhất 8 ký tự', dat: mk.length >= 8 },
    { nhan: 'Có chữ hoa và chữ thường', dat: /[a-z]/.test(mk) && /[A-Z]/.test(mk) },
    { nhan: 'Có chữ số', dat: /[0-9]/.test(mk) },
  ];
}

export function PasswordHints({ matKhau }) {
  if (!matKhau) return null;
  return (
    <ul className="pw-hints">
      {tieuChiMatKhau(matKhau).map((tc) => (
        <li key={tc.nhan} className={tc.dat ? 'is-ok' : undefined}>{tc.nhan}</li>
      ))}
    </ul>
  );
}

export default function RegisterPage() {
  const { dangKy } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ hoTen: '', email: '', soDienThoai: '', matKhau: '', nhapLai: '' });
  const [loi, setLoi] = useState('');
  const [loiTruong, setLoiTruong] = useState({});
  const [dangGui, setDangGui] = useState(false);

  const doi = (khoa) => (e) => setForm({ ...form, [khoa]: e.target.value });

  const guiForm = async (e) => {
    e.preventDefault();
    setLoi('');
    setLoiTruong({});
    if (form.matKhau !== form.nhapLai) {
      setLoiTruong({ nhapLai: 'Mật khẩu nhập lại không khớp' });
      return;
    }
    setDangGui(true);
    try {
      const { nhapLai, ...duLieu } = form;
      await dangKy(duLieu);
      toast.hien('Tạo tài khoản thành công. Chào mừng bạn!');
      navigate(location.state?.tu || '/', { replace: true });
    } catch (err) {
      setLoi(layThongBaoLoi(err));
      setLoiTruong(err.response?.data?.loi || {});
    } finally {
      setDangGui(false);
    }
  };

  const loiCua = (khoa) => loiTruong[khoa] && <small className="field-error">{loiTruong[khoa]}</small>;

  return (
    <AuthLayout
      tieuDe="Tạo tài khoản"
      moTa="Nhập thông tin để bắt đầu"
      chanTrang={<>Đã có tài khoản? <Link to="/dang-nhap" state={location.state}>Đăng nhập</Link></>}
    >
      <ErrorMessage>{loi}</ErrorMessage>
      <form onSubmit={guiForm} className="form">
        <label className="field">Họ và tên
          <input required maxLength={100} autoComplete="name" value={form.hoTen} onChange={doi('hoTen')} />
          {loiCua('hoTen')}
        </label>
        <div className="form-grid">
          <label className="field">Email
            <input type="email" required autoComplete="email" value={form.email} onChange={doi('email')} />
            {loiCua('email')}
          </label>
          <label className="field"><span>Số điện thoại <span className="field-hint">(không bắt buộc)</span></span>
            <input type="tel" pattern="0[0-9]{9}" title="10 chữ số, bắt đầu bằng 0" autoComplete="tel" value={form.soDienThoai} onChange={doi('soDienThoai')} />
            {loiCua('soDienThoai')}
          </label>
        </div>
        <div className="field">
          <label htmlFor="dk-mat-khau">Mật khẩu</label>
          <PasswordInput id="dk-mat-khau" required minLength={8} maxLength={100} pattern={MAU_MAT_KHAU} title="Ít nhất 8 ký tự, gồm chữ hoa, chữ thường và số" autoComplete="new-password" value={form.matKhau} onChange={doi('matKhau')} />
          <PasswordHints matKhau={form.matKhau} />
          {loiCua('matKhau')}
        </div>
        <div className="field">
          <label htmlFor="dk-nhap-lai">Nhập lại mật khẩu</label>
          <PasswordInput id="dk-nhap-lai" required minLength={8} maxLength={100} autoComplete="new-password" value={form.nhapLai} onChange={doi('nhapLai')} />
          {loiCua('nhapLai')}
        </div>
        <button type="submit" className="btn btn-primary btn-lg btn-block" disabled={dangGui}>
          {dangGui ? <><span className="spinner" /> Đang tạo tài khoản...</> : 'Đăng ký'}
        </button>
      </form>
      <div className="auth-divider"><span>hoặc</span></div>
      {/* Backend chưa có API đăng nhập Google: báo cho người dùng thay vì để nút không phản hồi */}
      <GoogleButton onClick={() => toast.hien('Đăng ký bằng Google đang được phát triển', { loai: 'info' })} />
    </AuthLayout>
  );
}
