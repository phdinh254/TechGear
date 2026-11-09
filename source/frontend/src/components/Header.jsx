import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { useCart } from '../context/CartContext.jsx';

function SearchIcon() {
  return <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" aria-hidden="true"><circle cx="10.8" cy="10.8" r="6.8" /><path d="m16 16 5 5" /></svg>;
}

function CartIcon() {
  return <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d="M2 3h2l2.2 11.1a2 2 0 0 0 2 1.6h9.4a2 2 0 0 0 2-1.6L21 7H5" /><circle cx="9" cy="20" r="1" /><circle cx="18" cy="20" r="1" /></svg>;
}

export default function Header() {
  const { nguoiDung, dangXuat } = useAuth();
  const { tongSoLuong } = useCart();
  const [params] = useSearchParams();
  const [tuKhoa, setTuKhoa] = useState(params.get('tuKhoa') || '');
  const [moTimKiem, setMoTimKiem] = useState(false);
  const navigate = useNavigate();
  const { pathname, hash } = useLocation();

  useEffect(() => {
    setTuKhoa(pathname === '/san-pham' ? params.get('tuKhoa') || '' : '');
  }, [pathname, params]);

  const timKiem = (event) => {
    event.preventDefault();
    const q = tuKhoa.trim();
    setMoTimKiem(false);
    navigate(q ? `/san-pham?tuKhoa=${encodeURIComponent(q)}` : '/san-pham');
  };

  return (
    <header className="header">
      <div className="container header-inner">
        <Link to="/" className="logo" aria-label="TechGear - Trang chủ">
          <span className="logo-mark" aria-hidden="true" />
          <span>Tech<strong>Gear</strong></span>
        </Link>

        <nav className="main-nav" aria-label="Menu chính">
          <Link to="/" className={pathname === '/' && !hash ? 'active' : undefined}>Trang chủ</Link>
          <Link to="/san-pham" className={pathname.startsWith('/san-pham') && !hash ? 'active' : undefined}>Sản phẩm</Link>
          <a href="#ho-tro" className={hash === '#ho-tro' ? 'active' : undefined}>Hỗ trợ</a>
          <a href="#lien-he" className={hash === '#lien-he' ? 'active' : undefined}>Liên hệ</a>
        </nav>

        <div className="header-actions">
          <div className="header-search-wrap">
            <button type="button" className="header-icon-button" aria-label="Tìm kiếm sản phẩm" aria-expanded={moTimKiem} aria-controls="header-search" onClick={() => setMoTimKiem((open) => !open)}>
              <SearchIcon />
            </button>
            {moTimKiem && (
              <form id="header-search" className="header-search" onSubmit={timKiem} onKeyDown={(event) => { if (event.key === 'Escape') setMoTimKiem(false); }} role="search">
                <input type="search" placeholder="Tìm tai nghe, củ sạc, chuột..." value={tuKhoa} onChange={(event) => setTuKhoa(event.target.value)} aria-label="Tìm kiếm sản phẩm" autoFocus />
                <button type="submit">Tìm</button>
              </form>
            )}
          </div>

          <Link to="/gio-hang" className="header-icon-button cart-link" aria-label={`Giỏ hàng, ${tongSoLuong} sản phẩm`}>
            <CartIcon />
            {tongSoLuong > 0 && <span className="cart-count">{tongSoLuong}</span>}
          </Link>

          {nguoiDung ? (
            <>
              <Link to="/tai-khoan" className="header-account">{nguoiDung.hoTen}</Link>
              <button type="button" className="header-logout" onClick={dangXuat}>Đăng xuất</button>
            </>
          ) : (
            <Link to="/dang-nhap" className="header-login">Đăng nhập</Link>
          )}
        </div>
      </div>
    </header>
  );
}
