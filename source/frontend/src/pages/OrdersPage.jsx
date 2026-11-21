import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { orderApi } from '../api/customerApi';
import { layThongBaoLoi } from '../api/axiosClient';
import AccountShell from '../components/AccountShell.jsx';
import ErrorMessage from '../components/ErrorMessage.jsx';
import { NHAN_TRANG_THAI, dinhDangThoiGian } from '../components/OrderView.jsx';
import Pagination from '../components/Pagination.jsx';
import EmptyState from '../components/ui/EmptyState.jsx';
import Icon from '../components/ui/Icon.jsx';
import { SkeletonBlock } from '../components/ui/Skeleton.jsx';
import { formatTien } from '../utils/format';

// Giữ export cũ để các trang khác không bị ảnh hưởng
export { NHAN_TRANG_THAI };

export default function OrdersPage() {
  const [duLieu, setDuLieu] = useState(null);
  const [trang, setTrang] = useState(0);
  const [loi, setLoi] = useState('');
  const [dangTai, setDangTai] = useState(true);

  useEffect(() => {
    setDangTai(true);
    orderApi.layDanhSach(trang)
      .then(setDuLieu)
      .catch((error) => setLoi(layThongBaoLoi(error)))
      .finally(() => setDangTai(false));
  }, [trang]);

  return (
    <AccountShell>
      <div className="panel">
        <h1 className="panel-title">Đơn hàng của tôi</h1>
        <ErrorMessage>{loi}</ErrorMessage>
        {dangTai && !duLieu ? (
          <div className="order-list">{[0, 1, 2].map((i) => <SkeletonBlock key={i} height={84} />)}</div>
        ) : duLieu?.noiDung.length === 0 ? (
          <EmptyState compact icon="package" tieuDe="Bạn chưa có đơn hàng" moTa="Khi đặt hàng, đơn của bạn sẽ xuất hiện tại đây để tiện theo dõi.">
            <Link to="/san-pham" className="btn btn-primary">Mua sắm ngay</Link>
          </EmptyState>
        ) : (
          <ul className="order-list">
            {duLieu?.noiDung.map((dh) => (
              <li key={dh.id}>
                <Link className="order-row" to={`/tai-khoan/don-hang/${dh.maDonHang}`}>
                  <span className="order-row-icon"><Icon name="package" size={20} /></span>
                  <span className="order-row-main">
                    <strong>{dh.maDonHang}</strong>
                    <small>{dinhDangThoiGian(dh.ngayDat)}</small>
                  </span>
                  <span className={`status status-${dh.trangThai}`}>{NHAN_TRANG_THAI[dh.trangThai]}</span>
                  <strong className="order-row-total">{formatTien(dh.tongTien)}</strong>
                  <Icon name="chevronRight" size={18} className="order-row-chevron" />
                </Link>
              </li>
            ))}
          </ul>
        )}
        {duLieu && <Pagination trang={duLieu.trang} tongSoTrang={duLieu.tongSoTrang} onChange={setTrang} />}
      </div>
    </AccountShell>
  );
}
