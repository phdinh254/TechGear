import { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import storeApi from '../api/storeApi';
import { layThongBaoLoi } from '../api/axiosClient';
import { useAuth } from '../context/AuthContext.jsx';
import ErrorMessage from '../components/ErrorMessage.jsx';
import OrderView from '../components/OrderView.jsx';
import PayNowButton from '../components/PayNowButton.jsx';
import { coTheThanhToanNgay } from '../utils/thanhToan.js';
import Breadcrumb from '../components/ui/Breadcrumb.jsx';
import Icon from '../components/ui/Icon.jsx';

export default function OrderLookupPage() {
  const [searchParams] = useSearchParams();
  const { nguoiDung } = useAuth();
  const [form, setForm] = useState({ maDonHang: searchParams.get('ma') || '', email: '' });
  const [donHang, setDonHang] = useState(null);
  const [loi, setLoi] = useState('');
  const [dangGui, setDangGui] = useState(false);

  const traCuu = async (event) => {
    event.preventDefault();
    setLoi('');
    setDangGui(true);
    try {
      setDonHang(await storeApi.traCuuDonHang(form.maDonHang.trim(), form.email.trim()));
    } catch (error) {
      setDonHang(null);
      setLoi(layThongBaoLoi(error));
    } finally {
      setDangGui(false);
    }
  };

  return (
    <div className="lookup">
      <Breadcrumb items={[{ nhan: 'Tra cứu đơn hàng' }]} />
      <div className="lookup-hero">
        <div className="lookup-copy">
          <h1>Đơn hàng của bạn đang ở đâu?</h1>
          <p className="lead">Nhập mã đơn và email đã dùng khi đặt hàng để xem trạng thái mới nhất.</p>
          {nguoiDung && (
            <p className="muted">Bạn đã đăng nhập – xem mọi đơn tại <Link to="/tai-khoan/don-hang" className="link-button">Đơn hàng của tôi</Link>.</p>
          )}
        </div>
        <form className="lookup-form panel" onSubmit={traCuu}>
          <ErrorMessage>{loi}</ErrorMessage>
          <label className="field">Mã đơn hàng
            <input required maxLength="20" placeholder="VD: DH261120A7K2QX" value={form.maDonHang} onChange={(e) => setForm({ ...form, maDonHang: e.target.value })} />
          </label>
          <label className="field">Email
            <input type="email" required maxLength="150" autoComplete="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
          </label>
          <button type="submit" className="btn btn-primary btn-lg btn-block" disabled={dangGui}>
            {dangGui ? <><span className="spinner" /> Đang tra cứu...</> : <><Icon name="search" size={18} /> Tra cứu</>}
          </button>
        </form>
      </div>

      {donHang && <div className="lookup-result" aria-live="polite"><OrderView donHang={donHang} hanhDong={coTheThanhToanNgay(donHang) && <PayNowButton maDonHang={donHang.maDonHang} />} /></div>}
    </div>
  );
}
