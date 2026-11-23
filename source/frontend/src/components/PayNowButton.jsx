import { Link } from 'react-router-dom';
import Icon from './ui/Icon.jsx';

/** Nút "Thanh toán ngay" qua PayOS: mở trang thanh toán nhúng của đơn. */
export default function PayNowButton({ maDonHang, nhan = 'Thanh toán ngay', className = 'btn btn-primary btn-block' }) {
  return (
    <Link className={className} to={`/thanh-toan/${encodeURIComponent(maDonHang)}`}>
      <Icon name="wallet" size={18} /> {nhan}
    </Link>
  );
}
