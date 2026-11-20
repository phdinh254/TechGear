import { Link } from 'react-router-dom';
import { formatTien } from '../utils/format';
import Icon from './ui/Icon.jsx';
import ProductMedia from './ui/ProductMedia.jsx';

export const NHAN_TRANG_THAI = {
  cho_xac_nhan: 'Chờ xác nhận',
  da_xac_nhan: 'Đã xác nhận',
  dang_giao: 'Đang giao',
  hoan_thanh: 'Hoàn thành',
  da_huy: 'Đã hủy',
};

const TIEN_TRINH = ['cho_xac_nhan', 'da_xac_nhan', 'dang_giao', 'hoan_thanh'];
const ICON_BUOC = { cho_xac_nhan: 'package', da_xac_nhan: 'check', dang_giao: 'truck', hoan_thanh: 'star' };

export const dinhDangThoiGian = (t) => (t ? new Date(t).toLocaleString('vi-VN', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit', year: 'numeric' }) : '');

export function OrderProgress({ trangThai }) {
  if (trangThai === 'da_huy') {
    return <div className="order-cancelled"><Icon name="close" size={18} strokeWidth={2.2} /> Đơn hàng đã bị hủy</div>;
  }
  const viTri = TIEN_TRINH.indexOf(trangThai);
  return (
    <ol className="order-progress" style={{ '--progress': `${(Math.max(viTri, 0) / (TIEN_TRINH.length - 1)) * 100}%` }}>
      {TIEN_TRINH.map((buoc, i) => (
        <li key={buoc} className={i < viTri ? 'is-done' : i === viTri ? 'is-current' : undefined}>
          <span className="order-progress-dot"><Icon name={i < viTri ? 'check' : ICON_BUOC[buoc]} size={16} strokeWidth={2} /></span>
          <span>{NHAN_TRANG_THAI[buoc]}</span>
        </li>
      ))}
    </ol>
  );
}

/**
 * Hiển thị đầy đủ một đơn hàng. renderItemExtra(item) cho phép trang chi tiết gắn thêm
 * form đánh giá; hanhDong là khối nút ở cột phải (ví dụ: hủy đơn).
 */
export default function OrderView({ donHang, renderItemExtra, hanhDong }) {
  return (
    <div className="order-view">
      <div className="panel order-status-panel">
        <div className="order-status-head">
          <div>
            <p className="muted">Mã đơn hàng</p>
            <h2>{donHang.maDonHang}</h2>
          </div>
          <span className={`status status-${donHang.trangThai}`}>{NHAN_TRANG_THAI[donHang.trangThai]}</span>
        </div>
        <OrderProgress trangThai={donHang.trangThai} />
        <p className="muted">Đặt lúc {dinhDangThoiGian(donHang.ngayDat)}</p>
      </div>

      <div className="order-grid">
        <div className="order-col">
          <section className="panel">
            <h3 className="panel-title">Sản phẩm ({donHang.chiTiets.length})</h3>
            <ul className="order-items">
              {donHang.chiTiets.map((item) => (
                <li key={item.id} className="order-item">
                  <span className="order-thumb"><ProductMedia src={item.hinhAnh} ten={item.tenSanPham} size="sm" /></span>
                  <div className="order-item-info">
                    {item.sanPhamSlug ? <Link to={`/san-pham/${item.sanPhamSlug}`}>{item.tenSanPham}</Link> : <span>{item.tenSanPham}</span>}
                    <small>{item.phanLoai || item.sku}</small>
                    <small>{formatTien(item.donGia)} × {item.soLuong}</small>
                  </div>
                  <strong>{formatTien(item.thanhTien)}</strong>
                  {renderItemExtra?.(item)}
                </li>
              ))}
            </ul>
          </section>

          {donHang.lichSus?.length > 0 && (
            <section className="panel">
              <h3 className="panel-title">Lịch sử trạng thái</h3>
              <ol className="timeline">
                {donHang.lichSus.map((item, i) => (
                  <li key={`${item.trangThai}-${i}`}>
                    <strong>{NHAN_TRANG_THAI[item.trangThai]}</strong>
                    <time>{dinhDangThoiGian(item.thoiGian)}</time>
                    {item.ghiChu && <p>{item.ghiChu}</p>}
                  </li>
                ))}
              </ol>
            </section>
          )}
        </div>

        <aside className="order-col">
          <section className="panel">
            <h3 className="panel-title">Giao hàng</h3>
            <p className="order-address"><strong>{donHang.tenNguoiNhan}</strong><span>{donHang.soDienThoai}</span><span>{donHang.diaChiGiaoHang}</span></p>
            {donHang.ghiChu && <p className="muted">Ghi chú: {donHang.ghiChu}</p>}
          </section>
          <section className="panel">
            <h3 className="panel-title">Thanh toán</h3>
            <dl className="summary-rows">
              <div><dt>Tạm tính</dt><dd>{formatTien(donHang.tamTinh)}</dd></div>
              {Number(donHang.tienGiamGia) > 0 && <div className="is-discount"><dt>Giảm giá</dt><dd>-{formatTien(donHang.tienGiamGia)}</dd></div>}
              <div><dt>Phí vận chuyển</dt><dd>{Number(donHang.phiVanChuyen) === 0 ? 'Miễn phí' : formatTien(donHang.phiVanChuyen)}</dd></div>
            </dl>
            <div className="summary-total"><span>Tổng cộng</span><strong>{formatTien(donHang.tongTien)}</strong></div>
            <p className="muted">{donHang.phuongThucThanhToan === 'cod' ? 'Thanh toán khi nhận hàng' : donHang.phuongThucThanhToan}</p>
            {hanhDong}
          </section>
        </aside>
      </div>
    </div>
  );
}
