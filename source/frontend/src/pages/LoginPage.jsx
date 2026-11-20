import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import { layThongBaoLoi } from '../api/axiosClient';
import AuthLayout from '../components/AuthLayout.jsx';
import ErrorMessage from '../components/ErrorMessage.jsx';
import GoogleButton from '../components/ui/GoogleButton.jsx';
import PasswordInput from '../components/ui/PasswordInput.jsx';

export default function LoginPage() {
  const { dangNhap } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ email: '', matKhau: '' });
  const [loi, setLoi] = useState('');
  const [dangGui, setDangGui] = useState(false);

  const guiForm = async (e) => {
    e.preventDefault();
    setLoi('');
    setDangGui(true);
    try {
      await dangNhap(form.email, form.matKhau);
      toast.hien('Đăng nhập thành công');
      navigate(location.state?.tu || '/', { replace: true });
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangGui(false);
    }
  };

  return (
    <AuthLayout
      tieuDe="Chào mừng trở lại"
      moTa="Đăng nhập tài khoản TechGear"
      chanTrang={<>Chưa có tài khoản? <Link to="/dang-ky" state={location.state}>Đăng ký ngay</Link></>}
      ghiChu={(
        <>
          Tiếp tục nghĩa là bạn đồng ý <Link to="/chinh-sach#dieu-khoan">Điều khoản</Link>
          <span aria-hidden="true"> · </span>
          <Link to="/chinh-sach#bao-mat">Bảo mật</Link>
        </>
      )}
    >
      <ErrorMessage>{loi}</ErrorMessage>
      <form onSubmit={guiForm} className="form">
        <label className="field">
          Email
          <input type="email" required autoComplete="email" placeholder="ban@email.com" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
        </label>
        <div className="field">
          <div className="field-row">
            <label htmlFor="mat-khau">Mật khẩu</label>
            <Link to="/quen-mat-khau" className="field-link">Quên mật khẩu?</Link>
          </div>
          <PasswordInput id="mat-khau" required autoComplete="current-password" value={form.matKhau} onChange={(e) => setForm({ ...form, matKhau: e.target.value })} />
        </div>
        <button type="submit" className="btn btn-primary btn-lg btn-block" disabled={dangGui}>
          {dangGui ? <><span className="spinner" /> Đang đăng nhập...</> : 'Đăng nhập'}
        </button>
      </form>
      <div className="auth-divider"><span>hoặc</span></div>
      {/* Backend chưa có API đăng nhập Google: báo cho người dùng thay vì để nút không phản hồi */}
      <GoogleButton onClick={() => toast.hien('Đăng nhập bằng Google đang được phát triển', { loai: 'info' })} />
    </AuthLayout>
  );
}
