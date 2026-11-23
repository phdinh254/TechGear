import { useEffect, useRef, useState } from 'react';
import { Link, NavLink, useLocation, useNavigate } from 'react-router-dom';
import catalogApi from '../api/catalogApi';
import { useAuth } from '../context/AuthContext.jsx';
import { useCart } from '../context/CartContext.jsx';
import { useWishlist } from '../context/WishlistContext.jsx';
import { useDanhMuc } from '../hooks/useCatalog.js';
import useCaiDat from '../hooks/useCaiDat.js';
import { formatTien } from '../utils/format';
import Icon, { iconTheoSlug } from './ui/Icon.jsx';
import ProductMedia from './ui/ProductMedia.jsx';

function Logo() {
  return (
    <Link to="/" className="logo" aria-label="TechGear – Trang chủ">
      <span className="logo-mark" aria-hidden="true">
        <svg viewBox="0 0 32 32"><path d="M8 9.5h16v3.4h-6.2V24h-3.6V12.9H8z" fill="currentColor" /><circle cx="24" cy="22" r="2.6" className="logo-dot" /></svg>
      </span>
      <span className="logo-text">Tech<span>Gear</span></span>
    </Link>
  );
}

function AnnouncementBar() {
  const caiDat = useCaiDat();
  const nguong = Number(caiDat.mien_phi_van_chuyen_tu);
  return (
    <div className="announcement">
      <div className="container announcement-inner">
        <p>
          <Icon name="truck" size={16} />
          {nguong > 0 ? <>Miễn phí vận chuyển cho đơn từ <strong>{formatTien(nguong)}</strong></> : 'Giao hàng toàn quốc'}
          <span className="announcement-sep" aria-hidden="true">·</span>
          <span className="announcement-extra">COD hoặc chuyển khoản qua PayOS</span>
        </p>
        <Link to="/tra-cuu-don-hang" className="announcement-link">Tra cứu đơn hàng</Link>
      </div>
    </div>
  );
}

/** Mega menu: cột trái là danh mục cha, bên phải là danh mục con của mục đang chọn. */
function MegaMenu({ danhMucs, onClose }) {
  const [dangChon, setDangChon] = useState(0);
  const cha = danhMucs[dangChon];
  if (!cha) return null;

  return (
    <div className="mega" role="region" aria-label="Danh mục sản phẩm">
      <div className="container mega-inner">
        <ul className="mega-parents">
          {danhMucs.map((dm, i) => (
            <li key={dm.id}>
              <Link
                to={`/san-pham?danhMuc=${dm.slug}`}
                className={i === dangChon ? 'is-active' : undefined}
                onMouseEnter={() => setDangChon(i)}
                onFocus={() => setDangChon(i)}
                onClick={onClose}
              >
                <Icon name={iconTheoSlug(dm.slug)} size={20} />
                <span>{dm.ten}</span>
                <Icon name="chevronRight" size={16} className="mega-chevron" />
              </Link>
            </li>
          ))}
        </ul>

        <div className="mega-children" key={cha.id}>
          <p className="mega-eyebrow">{cha.ten}</p>
          {cha.danhMucCons?.length > 0 ? (
            <div className="mega-tiles">
              {cha.danhMucCons.map((con) => (
                <Link key={con.id} to={`/san-pham?danhMuc=${con.slug}`} className="mega-tile" onClick={onClose}>
                  <span className="mega-tile-icon"><Icon name={iconTheoSlug(con.slug)} size={26} strokeWidth={1.4} /></span>
                  <span>{con.ten}</span>
                </Link>
              ))}
            </div>
          ) : (
            <p className="muted">Xem toàn bộ sản phẩm trong danh mục {cha.ten}.</p>
          )}
          <Link to={`/san-pham?danhMuc=${cha.slug}`} className="link-arrow" onClick={onClose}>
            Xem tất cả {cha.ten} <Icon name="arrowRight" size={16} />
          </Link>
        </div>

        <Link to={`/san-pham?danhMuc=${cha.slug}`} className="mega-feature" onClick={onClose}>
          <span className="mega-feature-art" aria-hidden="true"><Icon name={iconTheoSlug(cha.slug)} size={88} strokeWidth={1} /></span>
          <span className="mega-feature-kicker">Danh mục</span>
          <strong>{cha.ten}</strong>
          <span className="mega-feature-cta">Khám phá <Icon name="arrowRight" size={16} /></span>
        </Link>
      </div>
    </div>
  );
}

/** Ô tìm kiếm có gợi ý sản phẩm (gọi API tìm kiếm sẵn có, debounce 250ms). */
function SearchPanel({ onClose }) {
  const [tuKhoa, setTuKhoa] = useState('');
  const [goiY, setGoiY] = useState([]);
  const [dangTim, setDangTim] = useState(false);
  const [danhMucs] = useDanhMuc();
  const navigate = useNavigate();
  const input = useRef(null);

  useEffect(() => { input.current?.focus(); }, []);

  useEffect(() => {
    const q = tuKhoa.trim();
    if (q.length < 2) {
      setGoiY([]);
      return undefined;
    }
    const controller = new AbortController();
    const hen = window.setTimeout(() => {
      setDangTim(true);
      catalogApi.timSanPham({ tuKhoa: q, kichThuoc: 5 }, { signal: controller.signal })
        .then((kq) => setGoiY(kq.noiDung))
        .catch(() => {})
        .finally(() => setDangTim(false));
    }, 250);
    return () => { window.clearTimeout(hen); controller.abort(); };
  }, [tuKhoa]);

  const gui = (e) => {
    e.preventDefault();
    const q = tuKhoa.trim();
    onClose();
    navigate(q ? `/san-pham?tuKhoa=${encodeURIComponent(q)}` : '/san-pham');
  };

  return (
    <div className="search-panel" onKeyDown={(e) => { if (e.key === 'Escape') onClose(); }}>
      <div className="container">
        <form className="search-field" onSubmit={gui} role="search">
          <Icon name="search" size={22} />
          <input
            ref={input}
            type="search"
            value={tuKhoa}
            onChange={(e) => setTuKhoa(e.target.value)}
            placeholder="Tìm tai nghe, củ sạc, bàn phím..."
            aria-label="Tìm kiếm sản phẩm"
            maxLength={100}
          />
          {dangTim && <span className="spinner" />}
          <button type="submit" className="btn btn-primary btn-sm">Tìm kiếm</button>
          <button type="button" className="icon-btn" onClick={onClose} aria-label="Đóng tìm kiếm"><Icon name="close" size={20} /></button>
        </form>

        {tuKhoa.trim().length >= 2 ? (
          <div className="search-results">
            {goiY.length > 0 ? (
              <>
                <p className="search-label">Sản phẩm gợi ý</p>
                <ul>
                  {goiY.map((sp) => (
                    <li key={sp.id}>
                      <Link to={`/san-pham/${sp.slug}`} onClick={onClose} className="search-item">
                        <span className="search-thumb"><ProductMedia src={sp.anhChinh} ten={sp.ten} size="sm" /></span>
                        <span className="search-item-name">{sp.ten}</span>
                        <span className="search-item-price">{formatTien(sp.giaBan)}</span>
                      </Link>
                    </li>
                  ))}
                </ul>
                <button type="button" className="link-arrow" onClick={gui}>
                  Xem tất cả kết quả cho "{tuKhoa.trim()}" <Icon name="arrowRight" size={16} />
                </button>
              </>
            ) : (
              !dangTim && <p className="search-empty">Không có sản phẩm khớp "{tuKhoa.trim()}". Thử từ khóa ngắn hơn.</p>
            )}
          </div>
        ) : (
          danhMucs.length > 0 && (
            <div className="search-results">
              <p className="search-label">Danh mục phổ biến</p>
              <div className="chip-row">
                {danhMucs.flatMap((dm) => (dm.danhMucCons?.length ? dm.danhMucCons : [dm])).slice(0, 8).map((dm) => (
                  <Link key={dm.id} to={`/san-pham?danhMuc=${dm.slug}`} className="chip" onClick={onClose}>
                    <Icon name={iconTheoSlug(dm.slug)} size={16} /> {dm.ten}
                  </Link>
                ))}
              </div>
            </div>
          )
        )}
      </div>
    </div>
  );
}

function AccountMenu({ nguoiDung, dangXuat }) {
  const [mo, setMo] = useState(false);
  const { laQuanTri } = useAuth();
  const ref = useRef(null);
  const chuCai = (nguoiDung.hoTen || nguoiDung.email || '?').trim().split(/\s+/).pop()[0]?.toUpperCase();

  useEffect(() => {
    if (!mo) return undefined;
    const dong = (e) => { if (!ref.current?.contains(e.target)) setMo(false); };
    document.addEventListener('pointerdown', dong);
    return () => document.removeEventListener('pointerdown', dong);
  }, [mo]);

  return (
    <div className="account-menu" ref={ref} onKeyDown={(e) => { if (e.key === 'Escape') setMo(false); }}>
      <button type="button" className="icon-btn account-trigger" aria-expanded={mo} aria-haspopup="menu" onClick={() => setMo((v) => !v)} aria-label="Tài khoản của tôi">
        <span className="avatar">{chuCai}</span>
      </button>
      {mo && (
        <div className="dropdown" role="menu">
          <div className="dropdown-head">
            <strong>{nguoiDung.hoTen}</strong>
            <span>{nguoiDung.email}</span>
          </div>
          <Link role="menuitem" to="/tai-khoan" onClick={() => setMo(false)}><Icon name="user" size={18} /> Tài khoản</Link>
          <Link role="menuitem" to="/tai-khoan/don-hang" onClick={() => setMo(false)}><Icon name="package" size={18} /> Đơn hàng của tôi</Link>
          {laQuanTri && <Link role="menuitem" to="/admin" onClick={() => setMo(false)}><Icon name="grid" size={18} /> Trang quản trị</Link>}
          <button role="menuitem" type="button" onClick={() => { setMo(false); dangXuat(); }}><Icon name="logout" size={18} /> Đăng xuất</button>
        </div>
      )}
    </div>
  );
}

function MobileDrawer({ mo, onClose, danhMucs, nguoiDung, dangXuat }) {
  const { laQuanTri } = useAuth();
  const [moRong, setMoRong] = useState(null);
  return (
    <>
      <div className={`drawer-backdrop${mo ? ' is-open' : ''}`} onClick={onClose} aria-hidden="true" />
      <aside className={`drawer${mo ? ' is-open' : ''}`} aria-label="Menu" aria-hidden={!mo} inert={mo ? undefined : ''}>
        <div className="drawer-head">
          <Logo />
          <button type="button" className="icon-btn" onClick={onClose} aria-label="Đóng menu"><Icon name="close" size={22} /></button>
        </div>
        <nav className="drawer-nav">
          <NavLink to="/" end onClick={onClose}>Trang chủ</NavLink>
          <NavLink to="/san-pham" end onClick={onClose}>Tất cả sản phẩm</NavLink>
          <p className="drawer-label">Danh mục</p>
          {danhMucs.map((dm) => (
            <div key={dm.id} className="drawer-group">
              <div className="drawer-group-row">
                <Link to={`/san-pham?danhMuc=${dm.slug}`} onClick={onClose}>
                  <Icon name={iconTheoSlug(dm.slug)} size={20} /> {dm.ten}
                </Link>
                {dm.danhMucCons?.length > 0 && (
                  <button type="button" className="icon-btn" aria-expanded={moRong === dm.id} aria-label={`Mở ${dm.ten}`} onClick={() => setMoRong(moRong === dm.id ? null : dm.id)}>
                    <Icon name="chevronDown" size={18} className={moRong === dm.id ? 'rotate-180' : ''} />
                  </button>
                )}
              </div>
              {moRong === dm.id && (
                <div className="drawer-sub">
                  {dm.danhMucCons.map((con) => (
                    <Link key={con.id} to={`/san-pham?danhMuc=${con.slug}`} onClick={onClose}>{con.ten}</Link>
                  ))}
                </div>
              )}
            </div>
          ))}
          <p className="drawer-label">Hỗ trợ</p>
          <NavLink to="/chinh-sach" onClick={onClose}>Chính sách mua hàng</NavLink>
          <NavLink to="/tra-cuu-don-hang" onClick={onClose}>Tra cứu đơn hàng</NavLink>
          <NavLink to="/lien-he" onClick={onClose}>Liên hệ</NavLink>
        </nav>
        <div className="drawer-foot">
          {nguoiDung ? (
            <>
              <Link to="/tai-khoan" className="btn btn-outline" onClick={onClose}>Tài khoản</Link>
              {laQuanTri && <Link to="/admin" className="btn btn-outline" onClick={onClose}><Icon name="grid" size={18} /> Trang quản trị</Link>}
              <button type="button" className="btn btn-ghost" onClick={() => { onClose(); dangXuat(); }}>Đăng xuất</button>
            </>
          ) : (
            <>
              <Link to="/dang-nhap" className="btn btn-primary" onClick={onClose}>Đăng nhập</Link>
              <Link to="/dang-ky" className="btn btn-outline" onClick={onClose}>Đăng ký</Link>
            </>
          )}
        </div>
      </aside>
    </>
  );
}

export default function Header() {
  const { nguoiDung, dangXuat } = useAuth();
  const { tongSoLuong } = useCart();
  const { soLuong: soYeuThich } = useWishlist();
  const [danhMucs] = useDanhMuc();
  const { pathname, search } = useLocation();
  const [cuon, setCuon] = useState(false);
  const [moMega, setMoMega] = useState(false);
  const [moTim, setMoTim] = useState(false);
  const [moDrawer, setMoDrawer] = useState(false);
  const [nay, setNay] = useState(false);
  const soCu = useRef(tongSoLuong);
  const henDongMega = useRef(null);

  useEffect(() => {
    const onScroll = () => setCuon(window.scrollY > 8);
    onScroll();
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  // Đóng mọi lớp phủ khi đổi trang
  useEffect(() => {
    setMoMega(false);
    setMoTim(false);
    setMoDrawer(false);
  }, [pathname, search]);

  // Hiệu ứng "nảy" của số lượng giỏ hàng khi tăng
  useEffect(() => {
    if (tongSoLuong > soCu.current) {
      setNay(true);
      const t = window.setTimeout(() => setNay(false), 450);
      soCu.current = tongSoLuong;
      return () => window.clearTimeout(t);
    }
    soCu.current = tongSoLuong;
    return undefined;
  }, [tongSoLuong]);

  useEffect(() => {
    document.body.classList.toggle('no-scroll', moDrawer || moTim);
    return () => document.body.classList.remove('no-scroll');
  }, [moDrawer, moTim]);

  const moMegaMenu = () => { window.clearTimeout(henDongMega.current); setMoMega(true); };
  const dongMegaMenu = () => { henDongMega.current = window.setTimeout(() => setMoMega(false), 120); };

  return (
    <>
      <AnnouncementBar />
      <header className={`header${cuon ? ' is-scrolled' : ''}${moMega || moTim ? ' is-open' : ''}`}>
        <div className="container header-inner">
          <button type="button" className="icon-btn header-burger" onClick={() => setMoDrawer(true)} aria-label="Mở menu">
            <Icon name="menu" size={24} />
          </button>

          <Logo />

          <nav className="main-nav" aria-label="Menu chính">
            <NavLink to="/" end>Trang chủ</NavLink>
            <div className="nav-mega" onMouseEnter={moMegaMenu} onMouseLeave={dongMegaMenu}>
              <NavLink
                to="/san-pham"
                aria-expanded={moMega}
                onFocus={moMegaMenu}
                onKeyDown={(e) => { if (e.key === 'Escape') setMoMega(false); }}
              >
                Sản phẩm <Icon name="chevronDown" size={16} className={moMega ? 'rotate-180' : ''} />
              </NavLink>
              {moMega && danhMucs.length > 0 && (
                <div onMouseEnter={moMegaMenu} onMouseLeave={dongMegaMenu} onBlur={(e) => { if (!e.currentTarget.contains(e.relatedTarget)) setMoMega(false); }}>
                  <MegaMenu danhMucs={danhMucs} onClose={() => setMoMega(false)} />
                </div>
              )}
            </div>
            <NavLink to="/chinh-sach">Hỗ trợ</NavLink>
            <NavLink to="/lien-he">Liên hệ</NavLink>
          </nav>

          <div className="header-actions">
            <button type="button" className="icon-btn" onClick={() => setMoTim((v) => !v)} aria-label="Tìm kiếm" aria-expanded={moTim}>
              <Icon name="search" size={22} />
            </button>
            {/* Chưa đăng nhập: ProtectedRoute chuyển sang trang đăng nhập rồi quay lại trang yêu thích */}
            <Link to="/tai-khoan/yeu-thich" className="icon-btn wishlist-btn" aria-label={`Sản phẩm yêu thích${soYeuThich ? `, ${soYeuThich} sản phẩm` : ''}`}>
              <Icon name="heart" size={22} />
              {soYeuThich > 0 && <span className="cart-count">{soYeuThich > 99 ? '99+' : soYeuThich}</span>}
            </Link>
            <Link to="/gio-hang" className={`icon-btn cart-btn${nay ? ' is-bump' : ''}`} aria-label={`Giỏ hàng, ${tongSoLuong} sản phẩm`}>
              <Icon name="bag" size={22} />
              {tongSoLuong > 0 && <span className="cart-count">{tongSoLuong > 99 ? '99+' : tongSoLuong}</span>}
            </Link>
            {/* Avatar và nút "Đăng nhập" cùng ở cuối cụm để icon giỏ hàng không đổi chỗ khi đăng nhập/đăng xuất */}
            {nguoiDung
              ? <AccountMenu nguoiDung={nguoiDung} dangXuat={dangXuat} />
              : <Link to="/dang-nhap" className="btn btn-primary btn-sm header-login">Đăng nhập</Link>}
          </div>
        </div>
        {moTim && <SearchPanel onClose={() => setMoTim(false)} />}
      </header>
      {(moMega || moTim) && <div className="header-scrim" onClick={() => { setMoMega(false); setMoTim(false); }} aria-hidden="true" />}
      <MobileDrawer mo={moDrawer} onClose={() => setMoDrawer(false)} danhMucs={danhMucs} nguoiDung={nguoiDung} dangXuat={dangXuat} />
    </>
  );
}
