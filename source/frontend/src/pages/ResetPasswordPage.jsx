import { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import authApi from '../api/authApi';
import { layThongBaoLoi } from '../api/axiosClient';
import AuthLayout from '../components/AuthLayout.jsx';
import ErrorMessage from '../components/ErrorMessage.jsx';
import Icon from '../components/ui/Icon.jsx';
import PasswordInput from '../components/ui/PasswordInput.jsx';
import { PasswordHints } from './RegisterPage.jsx';

/** Trang mở từ liên kết trong email: /dat-lai-mat-khau?ma=... */
export default function ResetPasswordPage() {
  const [params] = useSearchParams();
  const ma = params.get('ma') || '';
  const [form, setForm] = useState({ matKhauMoi: '', nhapLai: '' });
  const [loi, setLoi] = useState('');
  const [xong, setXong] = useState('');
  const [dangGui, setDangGui] = useState(false);

  const gui = async (e) => {
    e.preventDefault();
    setLoi('');
    if (form.matKhauMoi !== form.nhapLai) {
      setLoi('Mật khẩu nhập lại không khớp');
      return;
    }
    setDangGui(true);
    try {
      const res = await authApi.datLaiMatKhau(ma, form.matKhauMoi);
      setXong(res.thongBao);
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangGui(false);
    }
  };

  if (!ma) {
    return (
      <AuthLayout tieuDe="Liên kết không hợp lệ" moTa="Liên kết đặt lại mật khẩu thiếu mã xác thực hoặc đã bị cắt ngắn.">
        <Link to="/quen-mat-khau" className="btn btn-primary btn-lg btn-block">Yêu cầu liên kết mới</Link>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout tieuDe="Đặt lại mật khẩu" moTa="Tạo mật khẩu mới cho tài khoản của bạn." chanTrang={<Link to="/dang-nhap">← Quay lại đăng nhập</Link>}>
      {xong ? (
        <div className="auth-success">
          <span className="success-mark success-mark-sm"><Icon name="check" size={26} strokeWidth={2.2} /></span>
          <p>{xong}</p>
          <Link to="/dang-nhap" className="btn btn-primary btn-lg btn-block">Đăng nhập</Link>
        </div>
      ) : (
        <>
          <ErrorMessage>{loi}</ErrorMessage>
          <form className="form" onSubmit={gui}>
            <div className="field">
              <label htmlFor="mk-moi">Mật khẩu mới</label>
              <PasswordInput id="mk-moi" required minLength={8} maxLength={100} pattern="(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]).{8,100}" title="Ít nhất 8 ký tự, gồm chữ hoa, chữ thường và số" autoComplete="new-password" value={form.matKhauMoi} onChange={(e) => setForm({ ...form, matKhauMoi: e.target.value })} />
              <PasswordHints matKhau={form.matKhauMoi} />
            </div>
            <div className="field">
              <label htmlFor="mk-nhap-lai">Nhập lại mật khẩu</label>
              <PasswordInput id="mk-nhap-lai" required minLength={8} maxLength={100} autoComplete="new-password" value={form.nhapLai} onChange={(e) => setForm({ ...form, nhapLai: e.target.value })} />
            </div>
            <button type="submit" className="btn btn-primary btn-lg btn-block" disabled={dangGui}>
              {dangGui ? <><span className="spinner" /> Đang lưu...</> : 'Lưu mật khẩu mới'}
            </button>
          </form>
        </>
      )}
    </AuthLayout>
  );
}
