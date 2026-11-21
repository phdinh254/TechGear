import { useState } from 'react';
import { Link } from 'react-router-dom';
import authApi from '../api/authApi';
import { layThongBaoLoi } from '../api/axiosClient';
import AuthLayout from '../components/AuthLayout.jsx';
import ErrorMessage from '../components/ErrorMessage.jsx';
import Icon from '../components/ui/Icon.jsx';

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [thongBao, setThongBao] = useState('');
  const [loi, setLoi] = useState('');
  const [dangGui, setDangGui] = useState(false);

  const gui = async (e) => {
    e.preventDefault();
    setLoi('');
    setDangGui(true);
    try {
      const res = await authApi.quenMatKhau(email.trim());
      setThongBao(res.thongBao);
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangGui(false);
    }
  };

  return (
    <AuthLayout
      tieuDe="Quên mật khẩu"
      moTa="Nhập email đã đăng ký, chúng tôi sẽ gửi liên kết đặt lại mật khẩu."
      chanTrang={<><Link to="/dang-nhap">← Quay lại đăng nhập</Link></>}
    >
      {thongBao ? (
        <div className="auth-success">
          <span className="success-mark success-mark-sm"><Icon name="mail" size={26} /></span>
          <p>{thongBao}</p>
          <p className="muted">Liên kết có hiệu lực trong thời gian ngắn. Kiểm tra cả hộp thư Spam nếu chưa thấy email.</p>
          <button type="button" className="btn btn-outline btn-block" onClick={() => setThongBao('')}>Gửi lại với email khác</button>
        </div>
      ) : (
        <>
          <ErrorMessage>{loi}</ErrorMessage>
          <form className="form" onSubmit={gui}>
            <label className="field">Email
              <input type="email" required autoComplete="email" placeholder="ban@email.com" value={email} onChange={(e) => setEmail(e.target.value)} />
            </label>
            <button type="submit" className="btn btn-primary btn-lg btn-block" disabled={dangGui}>
              {dangGui ? <><span className="spinner" /> Đang gửi...</> : 'Gửi liên kết đặt lại'}
            </button>
          </form>
        </>
      )}
    </AuthLayout>
  );
}
