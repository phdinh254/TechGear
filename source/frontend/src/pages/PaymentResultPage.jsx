import { useCallback, useEffect, useState } from 'react';
import { Link, useParams, useSearchParams } from 'react-router-dom';
import paymentApi from '../api/paymentApi';
import { layThongBaoLoi } from '../api/axiosClient';
import Loading from '../components/Loading.jsx';
import PayNowButton from '../components/PayNowButton.jsx';
import Icon from '../components/ui/Icon.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { chonGiaoDienKetQua } from '../utils/thanhToan.js';

// PayOS chuyển khách về đây kèm ?orderCode=…&status=… (không ký) – trạng thái thật lấy từ backend
const GIAO_DIEN = {
  thanh_cong: { icon: 'check', lop: 'is-success', tieuDe: 'Thanh toán thành công', moTa: 'Cảm ơn bạn! Đơn hàng đã được thanh toán và đang chờ cửa hàng xác nhận.' },
  cho_xu_ly: { icon: 'refresh', lop: 'is-pending', tieuDe: 'Đang chờ xác nhận thanh toán', moTa: 'PayOS chưa báo kết quả. Nếu bạn đã chuyển khoản, vui lòng đợi ít phút rồi kiểm tra lại.' },
  that_bai: { icon: 'close', lop: 'is-failed', tieuDe: 'Thanh toán chưa thành công', moTa: 'Giao dịch đã bị hủy hoặc không thành công. Bạn chưa bị trừ tiền.' },
  // Tiền về khi đơn đã bị hủy (quá hạn / khách hủy): cửa hàng hoàn tiền thủ công
  da_tra_don_huy: { icon: 'refresh', lop: 'is-pending', tieuDe: 'Đã nhận tiền – đơn hàng đã bị hủy', moTa: 'Đơn hàng đã bị hủy trước khi nhận được tiền. Cửa hàng sẽ liên hệ để hoàn tiền cho bạn.' },
  // Giao dịch này hỏng nhưng đơn đã được trả ở một lần thanh toán khác
  da_tra_lan_khac: { icon: 'check', lop: 'is-success', tieuDe: 'Đơn hàng đã được thanh toán', moTa: 'Lần thanh toán này không thành công, nhưng đơn hàng đã được thanh toán ở một giao dịch khác.' },
};

export default function PaymentResultPage() {
  const [params] = useSearchParams();
  const orderCode = params.get('orderCode') || '';
  const { maDonHang = '' } = useParams();
  const { nguoiDung } = useAuth();
  const [ketQua, setKetQua] = useState(null);
  const [loi, setLoi] = useState('');
  const [dangKiem, setDangKiem] = useState(true);

  const kiemTra = useCallback(async () => {
    setDangKiem(true);
    setLoi('');
    try {
      setKetQua(await paymentApi.ketQua(orderCode, maDonHang));
    } catch (error) {
      setLoi(layThongBaoLoi(error));
    } finally {
      setDangKiem(false);
    }
  }, [orderCode, maDonHang]);

  useEffect(() => { kiemTra(); }, [kiemTra]);

  if (dangKiem && !ketQua) return <Loading noiDung="Đang kiểm tra kết quả thanh toán..." />;

  if (loi || !ketQua?.hopLe) {
    return (
      <div className="order-success payment-result is-failed">
        <span className="success-mark"><Icon name="close" size={36} strokeWidth={2.4} /></span>
        <h1>Không tìm thấy giao dịch</h1>
        <p className="lead">{loi || 'Liên kết kết quả thanh toán không hợp lệ hoặc đã hết hạn.'}</p>
        <div className="success-actions">
          {loi && <button type="button" className="btn btn-primary btn-lg" onClick={kiemTra}>Thử lại</button>}
          <Link className="btn btn-outline btn-lg" to={nguoiDung ? '/tai-khoan/don-hang' : '/tra-cuu-don-hang'}>
            {nguoiDung ? 'Đơn hàng của tôi' : 'Tra cứu đơn hàng'}
          </Link>
        </div>
      </div>
    );
  }

  const kieu = chonGiaoDienKetQua(ketQua);
  const gd = GIAO_DIEN[kieu];
  const xemDon = nguoiDung
    ? `/tai-khoan/don-hang/${ketQua.maDonHang}`
    : `/tra-cuu-don-hang?ma=${encodeURIComponent(ketQua.maDonHang)}`;

  return (
    <div className={`order-success payment-result ${gd.lop}`} aria-live="polite">
      <span className="success-mark"><Icon name={gd.icon} size={36} strokeWidth={2.4} /></span>
      <h1>{gd.tieuDe}</h1>
      <p className="lead">{gd.moTa}</p>
      <dl className="success-info">
        <div><dt>Mã đơn hàng</dt><dd>{ketQua.maDonHang}</dd></div>
        <div><dt>Mã giao dịch PayOS</dt><dd>{orderCode}</dd></div>
        <div><dt>Thanh toán</dt><dd>{ketQua.trangThaiThanhToan === 'da_thanh_toan' ? 'Đã thanh toán' : 'Chưa thanh toán'}</dd></div>
      </dl>
      <div className="success-actions">
        {ketQua.trangThai === 'cho_xu_ly' && (
          <button type="button" className="btn btn-primary btn-lg" onClick={kiemTra} disabled={dangKiem}>
            {dangKiem ? 'Đang kiểm tra...' : 'Kiểm tra lại'}
          </button>
        )}
        {ketQua.trangThai === 'that_bai' && ketQua.coTheThanhToanLai && (
          <PayNowButton maDonHang={ketQua.maDonHang} nhan="Thanh toán lại" className="btn btn-primary btn-lg" />
        )}
        <Link className={`btn btn-lg ${ketQua.trangThai === 'thanh_cong' ? 'btn-primary' : 'btn-outline'}`} to={xemDon}>
          {nguoiDung ? 'Xem đơn hàng' : 'Tra cứu đơn hàng'}
        </Link>
        <Link className="btn btn-outline btn-lg" to="/san-pham">Tiếp tục mua sắm</Link>
      </div>
      {kieu === 'that_bai' && !ketQua.coTheThanhToanLai && (
        <p className="muted">Đơn hàng không còn thanh toán trực tuyến được (đã quá 30 phút hoặc đã được xử lý).</p>
      )}
      {!nguoiDung && <p className="muted">Tra cứu đơn bằng mã đơn và email đã dùng khi đặt hàng.</p>}
    </div>
  );
}
