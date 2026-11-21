import { NavLink, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { duongDanAnh } from '../utils/format';
import Icon from './ui/Icon.jsx';

const MUC = [
  { to: '/tai-khoan?tab=ho-so', tab: 'ho-so', icon: 'user', nhan: 'Thông tin cá nhân' },
  { to: '/tai-khoan?tab=dia-chi', tab: 'dia-chi', icon: 'mapPin', nhan: 'Sổ địa chỉ' },
  { to: '/tai-khoan?tab=mat-khau', tab: 'mat-khau', icon: 'lock', nhan: 'Đổi mật khẩu' },
  { to: '/tai-khoan/don-hang', icon: 'package', nhan: 'Đơn hàng của tôi' },
];

export function Avatar({ nguoiDung, size = 56 }) {
  const anh = duongDanAnh(nguoiDung.anhDaiDien);
  const chu = (nguoiDung.hoTen || nguoiDung.email || '?').trim().split(/\s+/).pop()[0]?.toUpperCase();
  return anh
    ? <img className="avatar-img" src={anh} alt="" style={{ width: size, height: size }} />
    : <span className="avatar avatar-lg" style={{ width: size, height: size }}>{chu}</span>;
}

/** Khung chung cho các trang tài khoản: menu bên trái (cuộn ngang trên mobile), nội dung bên phải. */
export default function AccountShell({ children }) {
  const { nguoiDung, dangXuat } = useAuth();
  const { pathname, search } = useLocation();
  const navigate = useNavigate();
  const tabHienTai = new URLSearchParams(search).get('tab') || 'ho-so';

  const dangChon = (m) => (m.tab ? pathname === '/tai-khoan' && tabHienTai === m.tab : pathname.startsWith(m.to));

  return (
    <div className="account">
      <aside className="account-side">
        <div className="account-user">
          <Avatar nguoiDung={nguoiDung} />
          <div>
            <strong>{nguoiDung.hoTen}</strong>
            <span>{nguoiDung.email}</span>
          </div>
        </div>
        <nav className="account-nav" aria-label="Tài khoản">
          {MUC.map((m) => (
            <NavLink key={m.to} to={m.to} className={() => (dangChon(m) ? 'active' : undefined)} aria-current={dangChon(m) ? 'page' : undefined}>
              <Icon name={m.icon} size={18} /> {m.nhan}
            </NavLink>
          ))}
          <button type="button" onClick={() => { dangXuat(); navigate('/'); }}>
            <Icon name="logout" size={18} /> Đăng xuất
          </button>
        </nav>
      </aside>
      <section className="account-main">{children}</section>
    </div>
  );
}
