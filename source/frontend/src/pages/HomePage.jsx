import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import catalogApi from '../api/catalogApi';
import { layThongBaoLoi } from '../api/axiosClient';
import ProductCard from '../components/ProductCard.jsx';
import Loading from '../components/Loading.jsx';
import ErrorMessage from '../components/ErrorMessage.jsx';
import { duongDanAnh } from '../utils/format.js';

export default function HomePage() {
  const [noiBat, setNoiBat] = useState([]);
  const [danhMuc, setDanhMuc] = useState([]);
  const [thuongHieu, setThuongHieu] = useState([]);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');

  useEffect(() => {
    Promise.allSettled([
      catalogApi.laySanPhamNoiBat(),
      catalogApi.layDanhMuc(),
      catalogApi.layThuongHieu(),
    ])
      .then(([sp, dm, th]) => {
        if (sp.status === 'fulfilled') setNoiBat(sp.value);
        else setLoi(layThongBaoLoi(sp.reason));
        if (dm.status === 'fulfilled') setDanhMuc(dm.value);
        if (th.status === 'fulfilled') setThuongHieu(th.value);
      })
      .finally(() => setDangTai(false));
  }, []);

  const sanPhamDau = noiBat.find((sp) => sp.anhChinh);

  const danhSachThuongHieu = thuongHieu.map((th) => (
    <Link key={th.id} to={`/san-pham?thuongHieu=${encodeURIComponent(th.slug)}`} className="brand-ticker-link">
      {th.ten}
    </Link>
  ));

  return (
    <>
      <section className={`hero${sanPhamDau ? ' hero-with-product' : ''}`}>
        <div className="hero-copy">
          <span className="hero-kicker">TECHGEAR</span>
          <h1>Phụ kiện cho mọi kết nối.</h1>
          <Link to="/san-pham" className="btn hero-action">Khám phá sản phẩm</Link>
        </div>
        {sanPhamDau && (
          <Link to={`/san-pham/${sanPhamDau.slug}`} className="hero-product" aria-label={`Xem ${sanPhamDau.ten}`}>
            <img src={duongDanAnh(sanPhamDau.anhChinh)} alt="" />
            <span>{sanPhamDau.ten}</span>
          </Link>
        )}
      </section>

      {thuongHieu.length > 0 && (
        <section className="brand-strip" aria-label="Thương hiệu">
          <span className="brand-strip-label">THƯƠNG HIỆU</span>
          <div className="brand-ticker">
            <div className={`brand-ticker-track${thuongHieu.length > 1 ? ' is-moving' : ''}`}>
              <div className="brand-ticker-group">{danhSachThuongHieu}</div>
              {thuongHieu.length > 1 && (
                <div className="brand-ticker-group" aria-hidden="true">
                  {thuongHieu.map((th) => <span key={th.id} className="brand-ticker-link">{th.ten}</span>)}
                </div>
              )}
            </div>
          </div>
        </section>
      )}

      {danhMuc.length > 0 && (
        <section className="section">
          <div className="section-heading"><h2>Danh mục</h2><Link to="/san-pham">Xem tất cả</Link></div>
          <div className="category-grid">
            {danhMuc.map((dm) => (
              <Link key={dm.id} to={`/san-pham?danhMuc=${dm.slug}`} className="category-card">
                <strong>{dm.ten}</strong>
                {dm.danhMucCons?.length > 0 && (
                  <span className="muted">{dm.danhMucCons.map((c) => c.ten).join(' · ')}</span>
                )}
              </Link>
            ))}
          </div>
        </section>
      )}

      <section className="section">
        <div className="section-heading"><h2>Sản phẩm nổi bật</h2><Link to="/san-pham">Xem tất cả</Link></div>
        {dangTai && <Loading />}
        <ErrorMessage>{loi}</ErrorMessage>
        <div className="product-grid">
          {noiBat.map((sp) => (
            <ProductCard key={sp.id} sanPham={sp} />
          ))}
        </div>
        {!dangTai && !loi && noiBat.length === 0 && (
          <p className="muted">Hiện chưa có sản phẩm nổi bật.</p>
        )}
      </section>
    </>
  );
}
