import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { orderApi } from '../api/customerApi';
import { layThongBaoLoi } from '../api/axiosClient';
import ErrorMessage from '../components/ErrorMessage.jsx';
import Loading from '../components/Loading.jsx';
import Pagination from '../components/Pagination.jsx';
import { formatTien } from '../utils/format';

export const NHAN_TRANG_THAI = {
  cho_xac_nhan: 'Chờ xác nhận', da_xac_nhan: 'Đã xác nhận', dang_giao: 'Đang giao',
  hoan_thanh: 'Hoàn thành', da_huy: 'Đã hủy',
};

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

  return <div>
    <div className="page-heading"><h1>Đơn hàng của tôi</h1><Link to="/tai-khoan">Quay lại tài khoản</Link></div>
    <ErrorMessage>{loi}</ErrorMessage>
    {dangTai && <Loading />}
    {!dangTai && duLieu?.noiDung.length === 0 && <p className="muted">Bạn chưa có đơn hàng.</p>}
    <div className="order-list">{duLieu?.noiDung.map((donHang) => <Link className="order-card" key={donHang.id} to={`/tai-khoan/don-hang/${donHang.maDonHang}`}>
      <div><strong>{donHang.maDonHang}</strong><span className={`status status-${donHang.trangThai}`}>{NHAN_TRANG_THAI[donHang.trangThai]}</span></div>
      <div><span>{donHang.ngayDat ? new Date(donHang.ngayDat).toLocaleString('vi-VN') : ''}</span><strong>{formatTien(donHang.tongTien)}</strong></div>
    </Link>)}</div>
    {duLieu && <Pagination trang={duLieu.trang} tongSoTrang={duLieu.tongSoTrang} onChange={setTrang} />}
  </div>;
}
