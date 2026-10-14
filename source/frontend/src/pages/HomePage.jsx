import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import catalogApi from '../api/catalogApi';
import { layThongBaoLoi } from '../api/axiosClient';
import ProductCard from '../components/ProductCard.jsx';
import Loading from '../components/Loading.jsx';
import ErrorMessage from '../components/ErrorMessage.jsx';

export default function HomePage() {
  const [noiBat, setNoiBat] = useState([]);
  const [danhMuc, setDanhMuc] = useState([]);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');

  useEffect(() => {
    Promise.all([catalogApi.laySanPhamNoiBat(), catalogApi.layDanhMuc()])
      .then(([sp, dm]) => {
        setNoiBat(sp);
        setDanhMuc(dm);
      })
      .catch((e) => setLoi(layThongBaoLoi(e)))
      .finally(() => setDangTai(false));
  }, []);

  return (
    <>
      <section className="hero">
        <h1>Phụ kiện công nghệ cho mọi thiết bị</h1>
        <p>Tai nghe, củ sạc, cáp, sạc dự phòng, chuột, bàn phím kèm thông tin bảo hành rõ ràng.</p>
        <Link to="/san-pham" className="btn">
          Xem tất cả sản phẩm
        </Link>
      </section>

      {danhMuc.length > 0 && (
        <section className="section">
          <h2>Danh mục</h2>
          <div className="category-grid">
            {danhMuc.map((dm) => (
              <Link key={dm.id} to={`/san-pham?danhMuc=${dm.slug}`} className="category-card">
                <strong>{dm.ten}</strong>
                {dm.danhMucCons.length > 0 && (
                  <span className="muted">{dm.danhMucCons.map((c) => c.ten).join(' · ')}</span>
                )}
              </Link>
            ))}
          </div>
        </section>
      )}

      <section className="section">
        <h2>Sản phẩm nổi bật</h2>
        {dangTai && <Loading />}
        <ErrorMessage>{loi}</ErrorMessage>
        <div className="product-grid">
          {noiBat.map((sp) => (
            <ProductCard key={sp.id} sanPham={sp} />
          ))}
        </div>
      </section>
    </>
  );
}
