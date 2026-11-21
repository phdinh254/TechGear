import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import catalogApi from '../api/catalogApi';
import { layThongBaoLoi } from '../api/axiosClient';
import { useAuth } from '../context/AuthContext.jsx';
import { useCart } from '../context/CartContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import { useWishlist } from '../context/WishlistContext.jsx';
import { taoDongGio } from '../utils/cart';
import Icon from './ui/Icon.jsx';
import Price, { phanTramGiam } from './ui/Price.jsx';
import ProductMedia from './ui/ProductMedia.jsx';

/**
 * Thẻ sản phẩm. Nút "Thêm vào giỏ" thêm thẳng khi sản phẩm chỉ có một phiên bản còn hàng;
 * sản phẩm có nhiều phân loại thì chuyển sang trang chi tiết để khách chọn.
 */
export default function ProductCard({ sanPham, badge, style }) {
  const { nguoiDung } = useAuth();
  const { themVaoGio } = useCart();
  const { laYeuThich, doiYeuThich } = useWishlist();
  const toast = useToast();
  const navigate = useNavigate();
  const [dangThem, setDangThem] = useState(false);
  const duongDan = `/san-pham/${sanPham.slug}`;
  const giam = phanTramGiam(sanPham.giaBan, sanPham.giaGoc);
  const thich = laYeuThich(sanPham.id);

  const themNhanh = async () => {
    setDangThem(true);
    try {
      const sp = await catalogApi.layChiTietSanPham(sanPham.slug);
      const conHang = sp.bienThes.filter((b) => b.soLuongTon > 0);
      if (sp.tuyChons.length > 0 || conHang.length !== 1) {
        navigate(duongDan);
        if (conHang.length > 0) toast.hien('Chọn phân loại bạn muốn mua', { loai: 'info' });
        return;
      }
      await themVaoGio(taoDongGio(sp, conHang[0]), 1);
      toast.hien(`Đã thêm "${sp.ten}" vào giỏ`, { hanhDong: { nhan: 'Xem giỏ', to: '/gio-hang' } });
    } catch (error) {
      toast.hien(layThongBaoLoi(error), { loai: 'error' });
    } finally {
      setDangThem(false);
    }
  };

  const bamYeuThich = async () => {
    if (!nguoiDung) {
      navigate('/dang-nhap', { state: { tu: duongDan } });
      return;
    }
    try {
      const moi = await doiYeuThich(sanPham.id);
      toast.hien(moi ? 'Đã thêm vào yêu thích' : 'Đã bỏ khỏi yêu thích', {
        hanhDong: moi ? { nhan: 'Xem', to: '/tai-khoan/yeu-thich' } : undefined,
      });
    } catch (error) {
      toast.hien(error.message, { loai: 'error' });
    }
  };

  return (
    <article className={`product-card${sanPham.conHang ? '' : ' is-out'}`} style={style}>
      <Link to={duongDan} className="product-card-media" tabIndex={-1} aria-hidden="true">
        <ProductMedia src={sanPham.anhChinh} ten={sanPham.ten} />
      </Link>

      <div className="product-card-badges">
        {giam > 0 && <span className="badge badge-sale">-{giam}%</span>}
        {badge && <span className="badge badge-new">{badge}</span>}
        {!sanPham.conHang && <span className="badge badge-muted">Hết hàng</span>}
      </div>

      <button
        type="button"
        className={`product-card-like${thich ? ' is-active' : ''}`}
        onClick={bamYeuThich}
        aria-pressed={thich}
        aria-label={thich ? `Bỏ yêu thích ${sanPham.ten}` : `Yêu thích ${sanPham.ten}`}
      >
        <Icon name="heart" size={18} />
      </button>

      <div className="product-card-body">
        {sanPham.tenThuongHieu && <p className="product-card-brand">{sanPham.tenThuongHieu}</p>}
        <h3 className="product-card-title"><Link to={duongDan}>{sanPham.ten}</Link></h3>
        <div className="product-card-foot">
          <Price giaBan={sanPham.giaBan} giaGoc={sanPham.giaGoc} />
          <button
            type="button"
            className="product-card-add"
            onClick={themNhanh}
            disabled={!sanPham.conHang || dangThem}
            aria-label={`Thêm ${sanPham.ten} vào giỏ`}
          >
            {dangThem ? <span className="spinner" /> : <Icon name="plus" size={18} strokeWidth={2} />}
            <span className="product-card-add-label">{sanPham.conHang ? 'Thêm vào giỏ' : 'Hết hàng'}</span>
          </button>
        </div>
      </div>
    </article>
  );
}
