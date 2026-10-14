import { Link } from 'react-router-dom';
import { duongDanAnh, formatTien } from '../utils/format';

export default function ProductCard({ sanPham }) {
  const anh = duongDanAnh(sanPham.anhChinh);
  const giam =
    sanPham.giaGoc && Number(sanPham.giaGoc) > Number(sanPham.giaBan)
      ? Math.round((1 - Number(sanPham.giaBan) / Number(sanPham.giaGoc)) * 100)
      : 0;

  return (
    <Link to={`/san-pham/${sanPham.slug}`} className="product-card">
      <div className="product-image">
        {anh ? <img src={anh} alt={sanPham.ten} loading="lazy" /> : <span>Chưa có ảnh</span>}
        {giam > 0 && <span className="tag-sale">-{giam}%</span>}
        {!sanPham.conHang && <span className="tag-out">Hết hàng</span>}
      </div>
      <div className="product-info">
        {sanPham.tenThuongHieu && <p className="brand">{sanPham.tenThuongHieu}</p>}
        <h3>{sanPham.ten}</h3>
        <p className="price">
          {formatTien(sanPham.giaBan)}
          {giam > 0 && <del>{formatTien(sanPham.giaGoc)}</del>}
        </p>
      </div>
    </Link>
  );
}
