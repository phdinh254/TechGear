import { useEffect, useState } from 'react';
import { Link, NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import Icon from '../components/ui/Icon.jsx';
import '../styles/admin.css';

const MUC_CHINH = [
  { to: '/admin/danh-muc', nhan: 'Danh mục' },
  { to: '/admin/thuong-hieu', nhan: 'Thương hiệu' },
  { to: '/admin/san-pham', nhan: 'Sản phẩm' },
  { to: '/admin/ton-kho', nhan: 'Tồn kho' },
];
const MUC_SAU = ['Đơn hàng', 'Mã giảm giá', 'Banner', 'Cấu hình', 'Người dùng', 'Đánh giá', 'Tổng quan'];

export default function AdminLayout() {
  const { nguoiDung, dangXuat } = useAuth();
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const [mo, setMo] = useState(false);

  useEffect(() => { setMo(false); }, [pathname]);

  const thoat = () => {
    dangXuat();
    navigate('/');
  };

  return (
    <div className="admin-shell">
      <header className="admin-topbar">
        <button type="button" className="icon-btn" aria-label="Mở menu" aria-expanded={mo} onClick={() => setMo(true)}>
          <Icon name="menu" size={22} />
        </button>
        <strong>TECHGEAR ADMIN</strong>
      </header>
      <div className={`admin-backdrop${mo ? ' is-open' : ''}`} onClick={() => setMo(false)} aria-hidden="true" />
      <aside className={`admin-sidebar${mo ? ' is-open' : ''}`} aria-label="Menu quản trị">
        <div className="admin-sidebar-head">
          <strong>TECHGEAR ADMIN</strong>
          <button type="button" className="icon-btn admin-sidebar-close" aria-label="Đóng menu" onClick={() => setMo(false)}>
            <Icon name="close" size={20} />
          </button>
        </div>
        <nav className="admin-nav">
          {MUC_CHINH.map((m) => <NavLink key={m.to} to={m.to}>{m.nhan}</NavLink>)}
          <p className="admin-nav-label">Đợt sau</p>
          {MUC_SAU.map((nhan) => <span key={nhan} className="admin-nav-disabled" aria-disabled="true">{nhan}</span>)}
        </nav>
        <div className="admin-sidebar-foot">
          <Link to="/">Về cửa hàng</Link>
          <span className="admin-user">{nguoiDung?.hoTen || nguoiDung?.email}</span>
          <button type="button" className="btn btn-outline" onClick={thoat}><Icon name="logout" size={18} /> Đăng xuất</button>
        </div>
      </aside>
      <main className="admin-main">
        <Outlet />
      </main>
    </div>
  );
}
