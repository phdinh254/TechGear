import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { layThongBaoLoi } from '../api/axiosClient';
import ErrorMessage from '../components/ErrorMessage.jsx';

export default function LoginPage() {
  const { dangNhap } = useAuth();
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
      navigate(location.state?.tu || '/', { replace: true });
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangGui(false);
    }
  };

  return (
    <div className="auth-box">
      <h1>Đăng nhập</h1>
      <ErrorMessage>{loi}</ErrorMessage>
      <form onSubmit={guiForm} className="form">
        <label>
          Email
          <input
            type="email"
            required
            autoComplete="email"
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
          />
        </label>
        <label>
          Mật khẩu
          <input
            type="password"
            required
            autoComplete="current-password"
            value={form.matKhau}
            onChange={(e) => setForm({ ...form, matKhau: e.target.value })}
          />
        </label>
        <button type="submit" className="btn" disabled={dangGui}>
          {dangGui ? 'Đang đăng nhập...' : 'Đăng nhập'}
        </button>
      </form>
      <p>
        Chưa có tài khoản? <Link to="/dang-ky">Đăng ký</Link>
      </p>
    </div>
  );
}
