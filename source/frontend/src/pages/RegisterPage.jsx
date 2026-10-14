import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { layThongBaoLoi } from '../api/axiosClient';
import ErrorMessage from '../components/ErrorMessage.jsx';

export default function RegisterPage() {
  const { dangKy } = useAuth();
  const navigate = useNavigate();
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
      navigate('/', { replace: true });
    } catch (err) {
      setLoi(layThongBaoLoi(err));
      setLoiTruong(err.response?.data?.loi || {});
    } finally {
      setDangGui(false);
    }
  };

  const truong = (khoa, nhan, kieu = 'text', thuocTinh = {}) => (
    <label>
      {nhan}
      <input type={kieu} value={form[khoa]} onChange={doi(khoa)} {...thuocTinh} />
      {loiTruong[khoa] && <small className="field-error">{loiTruong[khoa]}</small>}
    </label>
  );

  return (
    <div className="auth-box">
      <h1>Đăng ký tài khoản</h1>
      <ErrorMessage>{loi}</ErrorMessage>
      <form onSubmit={guiForm} className="form">
        {truong('hoTen', 'Họ và tên', 'text', { required: true, maxLength: 100 })}
        {truong('email', 'Email', 'email', { required: true, autoComplete: 'email' })}
        {truong('soDienThoai', 'Số điện thoại (không bắt buộc)', 'tel', { pattern: '0[0-9]{9}' })}
        {truong('matKhau', 'Mật khẩu (ít nhất 6 ký tự)', 'password', {
          required: true,
          minLength: 6,
          autoComplete: 'new-password',
        })}
        {truong('nhapLai', 'Nhập lại mật khẩu', 'password', { required: true, autoComplete: 'new-password' })}
        <button type="submit" className="btn" disabled={dangGui}>
          {dangGui ? 'Đang xử lý...' : 'Đăng ký'}
        </button>
      </form>
      <p>
        Đã có tài khoản? <Link to="/dang-nhap">Đăng nhập</Link>
      </p>
    </div>
  );
}
