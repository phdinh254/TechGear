import { Link } from 'react-router-dom';
import { useCart } from '../context/CartContext.jsx';
import { duongDanAnh, formatTien } from '../utils/format';
import ErrorMessage from '../components/ErrorMessage.jsx';
import Loading from '../components/Loading.jsx';

export default function CartPage() {
  const { dong, tamTinh, capNhatSoLuong, xoaKhoiGio, dangDongBo, loiGio } = useCart();

  if (dangDongBo) return <Loading />;

  if (dong.length === 0) {
    return (
      <div className="state">
        <ErrorMessage>{loiGio}</ErrorMessage>
        <p>Giỏ hàng đang trống.</p>
        <Link to="/san-pham" className="btn">
          Tiếp tục mua sắm
        </Link>
      </div>
    );
  }

  return (
    <div>
      <h1>Giỏ hàng</h1>
      <ErrorMessage>{loiGio}</ErrorMessage>
      <div className="cart-table">
        {dong.map((d) => (
          <div key={d.bienTheId} className="cart-row">
            <div className="cart-thumb">{d.anh ? <img src={duongDanAnh(d.anh)} alt={d.ten} /> : null}</div>
            <div className="cart-name">
              <Link to={`/san-pham/${d.sanPhamSlug}`}>{d.ten}</Link>
              {d.phanLoai && <p className="muted">{d.phanLoai}</p>}
              <p>{formatTien(d.giaBan)}</p>
            </div>
            <input
              type="number"
              min="1"
              max={d.tonKho}
              value={d.soLuong}
              disabled={d.conBan === false}
              onChange={(e) => capNhatSoLuong(d.bienTheId, Number(e.target.value) || 1)}
              aria-label={`Số lượng ${d.ten}`}
            />
            <strong>{formatTien(Number(d.giaBan) * d.soLuong)}</strong>
            <button type="button" className="link-button" onClick={() => xoaKhoiGio(d.bienTheId)}>
              Xóa
            </button>
          </div>
        ))}
      </div>
      <div className="cart-summary">
        <p>
          Tạm tính: <strong>{formatTien(tamTinh)}</strong>
        </p>
        <p className="muted">Phí vận chuyển và mã giảm giá sẽ được tính ở bước thanh toán.</p>
        <Link to="/dat-hang" className="btn">Tiến hành đặt hàng</Link>
      </div>
    </div>
  );
}
