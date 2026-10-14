import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import catalogApi from '../api/catalogApi';
import { useAuth } from '../context/AuthContext.jsx';
import { useCart } from '../context/CartContext.jsx';

export default function Header() {
  const { nguoiDung, dangXuat } = useAuth();
  const { tongSoLuong } = useCart();
  const [danhMuc, setDanhMuc] = useState([]);
  const [params] = useSearchParams();
  const [tuKhoa, setTuKhoa] = useState(params.get('tuKhoa') || '');
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const danhMucDangChon = pathname === '/san-pham' ? params.get('danhMuc') || '' : null;
  const lop = (slug) => (danhMucDangChon === slug ? 'active' : undefined);

  useEffect(() => {
    catalogApi.layDanhMuc().then(setDanhMuc).catch(() => setDanhMuc([]));
  }, []);

  const timKiem = (e) => {
    e.preventDefault();
    const q = tuKhoa.trim();
    navigate(q ? `/san-pham?tuKhoa=${encodeURIComponent(q)}` : '/san-pham');
  };

  return (
    <header className="header">
      <div className="container header-top">
        <Link to="/" className="logo">
          Tech<span>Gear</span>
        </Link>

        <form className="search" onSubmit={timKiem} role="search">
          <input
            type="search"
            placeholder="Tìm tai nghe, củ sạc, chuột..."
            value={tuKhoa}
            onChange={(e) => setTuKhoa(e.target.value)}
            aria-label="Tìm kiếm sản phẩm"
          />
          <button type="submit">Tìm</button>
        </form>

        <nav className="header-actions">
          <Link to="/gio-hang" className="cart-link">
            Giỏ hàng <span className="badge">{tongSoLuong}</span>
          </Link>
          {nguoiDung ? (
            <>
              <Link to="/tai-khoan">{nguoiDung.hoTen}</Link>
              <button type="button" className="link-button" onClick={dangXuat}>
                Đăng xuất
              </button>
            </>
          ) : (
            <>
              <Link to="/dang-nhap">Đăng nhập</Link>
              <Link to="/dang-ky" className="btn btn-small">
                Đăng ký
              </Link>
            </>
          )}
        </nav>
      </div>

      <nav className="category-bar">
        <div className="container category-list">
          <Link to="/san-pham" className={lop('')}>
            Tất cả
          </Link>
          {danhMuc.map((dm) => (
            <Link key={dm.id} to={`/san-pham?danhMuc=${dm.slug}`} className={lop(dm.slug)}>
              {dm.ten}
            </Link>
          ))}
        </div>
      </nav>
    </header>
  );
}
