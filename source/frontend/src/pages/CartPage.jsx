import { Link } from 'react-router-dom';
import { useCart } from '../context/CartContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import ErrorMessage from '../components/ErrorMessage.jsx';
import Loading from '../components/Loading.jsx';
import EmptyState from '../components/ui/EmptyState.jsx';
import Icon from '../components/ui/Icon.jsx';
import ProductMedia from '../components/ui/ProductMedia.jsx';
import QuantityStepper from '../components/ui/QuantityStepper.jsx';
import useCaiDat from '../hooks/useCaiDat.js';
import { formatTien } from '../utils/format';

function FreeShipProgress({ tamTinh }) {
  const caiDat = useCaiDat();
  const nguong = Number(caiDat.mien_phi_van_chuyen_tu);
  if (!nguong) return null;
  const conThieu = Math.max(0, nguong - tamTinh);
  const phanTram = Math.min(100, (tamTinh / nguong) * 100);
  return (
    <div className={`ship-progress${conThieu === 0 ? ' is-done' : ''}`}>
      <p>
        <Icon name="truck" size={18} />
        <span>
          {conThieu > 0
            ? <>Mua thêm <strong>{formatTien(conThieu)}</strong> để được miễn phí vận chuyển</>
            : <>Đơn hàng của bạn được <strong>miễn phí vận chuyển</strong></>}
        </span>
      </p>
      <div className="ship-bar" role="progressbar" aria-valuemin={0} aria-valuemax={100} aria-valuenow={Math.round(phanTram)} aria-label="Tiến độ miễn phí vận chuyển">
        <span style={{ width: `${phanTram}%` }} />
      </div>
    </div>
  );
}

export default function CartPage() {
  const { dong, tamTinh, tongSoLuong, capNhatSoLuong, xoaKhoiGio, xoaHet, dangDongBo, loiGio } = useCart();
  const toast = useToast();

  if (dangDongBo) return <Loading noiDung="Đang đồng bộ giỏ hàng..." />;

  if (dong.length === 0) {
    return (
      <div className="cart-page">
        <h1 className="page-title">Giỏ hàng</h1>
        <ErrorMessage>{loiGio}</ErrorMessage>
        <EmptyState icon="bag" tieuDe="Giỏ hàng đang trống" moTa="Khám phá tai nghe, sạc nhanh và phụ kiện máy tính đang được nhiều người chọn.">
          <Link to="/san-pham" className="btn btn-primary">Bắt đầu mua sắm <Icon name="arrowRight" size={18} /></Link>
        </EmptyState>
      </div>
    );
  }

  const xoa = async (item) => {
    try {
      await xoaKhoiGio(item.bienTheId);
      toast.hien(`Đã xóa "${item.ten}" khỏi giỏ`, { loai: 'info' });
    } catch { /* lỗi hiển thị qua loiGio */ }
  };

  const xoaTatCa = async () => {
    if (!window.confirm('Xóa toàn bộ sản phẩm trong giỏ hàng?')) return;
    try { await xoaHet(); } catch { /* lỗi hiển thị qua loiGio */ }
  };

  const conHangLoi = dong.some((d) => d.conBan === false);

  return (
    <div className="cart-page">
      <div className="page-heading">
        <h1 className="page-title">Giỏ hàng <span>({tongSoLuong})</span></h1>
        <button type="button" className="link-button" onClick={xoaTatCa}>Xóa tất cả</button>
      </div>
      <ErrorMessage>{loiGio}</ErrorMessage>

      <div className="cart-layout">
        <section className="cart-items" aria-label="Sản phẩm trong giỏ">
          <FreeShipProgress tamTinh={tamTinh} />
          <ul className="cart-list">
            {dong.map((d) => (
              <li key={d.bienTheId} className={`cart-item${d.conBan === false ? ' is-unavailable' : ''}`}>
                <Link to={`/san-pham/${d.sanPhamSlug}`} className="cart-thumb" tabIndex={-1} aria-hidden="true">
                  <ProductMedia src={d.anh} ten={d.ten} size="sm" />
                </Link>
                <div className="cart-item-info">
                  <Link to={`/san-pham/${d.sanPhamSlug}`} className="cart-item-name">{d.ten}</Link>
                  {d.phanLoai && <p className="cart-item-variant">{d.phanLoai}</p>}
                  <p className="cart-item-unit">{formatTien(d.giaBan)}</p>
                  {d.conBan === false && <p className="field-error">Sản phẩm tạm ngừng bán – vui lòng xóa khỏi giỏ</p>}
                </div>
                <QuantityStepper
                  size="sm"
                  value={d.soLuong}
                  max={d.tonKho || 1}
                  disabled={d.conBan === false}
                  label={`Số lượng ${d.ten}`}
                  onChange={(so) => capNhatSoLuong(d.bienTheId, so).catch(() => {})}
                />
                <strong className="cart-item-total">{formatTien(Number(d.giaBan) * d.soLuong)}</strong>
                <button type="button" className="icon-btn cart-remove" onClick={() => xoa(d)} aria-label={`Xóa ${d.ten}`}>
                  <Icon name="trash" size={18} />
                </button>
              </li>
            ))}
          </ul>
          <Link to="/san-pham" className="link-arrow cart-continue"><Icon name="chevronLeft" size={16} /> Tiếp tục mua sắm</Link>
        </section>

        <aside className="summary">
          <h2>Tóm tắt đơn hàng</h2>
          <dl className="summary-rows">
            <div><dt>Tạm tính ({tongSoLuong} sản phẩm)</dt><dd>{formatTien(tamTinh)}</dd></div>
            <div><dt>Phí vận chuyển</dt><dd className="muted">Tính ở bước sau</dd></div>
            <div><dt>Mã giảm giá</dt><dd className="muted">Nhập khi thanh toán</dd></div>
          </dl>
          <div className="summary-total"><span>Tạm tính</span><strong>{formatTien(tamTinh)}</strong></div>
          <Link to="/dat-hang" className={`btn btn-primary btn-lg btn-block${conHangLoi ? ' is-disabled' : ''}`} aria-disabled={conHangLoi} onClick={(e) => { if (conHangLoi) e.preventDefault(); }}>
            Tiến hành thanh toán <Icon name="arrowRight" size={18} />
          </Link>
          <ul className="summary-trust">
            <li><Icon name="lock" size={16} /> Thông tin được bảo mật</li>
            <li><Icon name="wallet" size={16} /> Thanh toán khi nhận hàng</li>
          </ul>
        </aside>
      </div>
    </div>
  );
}
