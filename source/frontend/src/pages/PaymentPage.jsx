import { useCallback, useEffect, useLayoutEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
// usePayOS của thư viện không dùng hook React (chỉ tạo đối tượng) nên gọi được trong effect
import { usePayOS as taoKhungPayOS } from '@payos/payos-checkout';
import paymentApi, { docLink, luuLink } from '../api/paymentApi';
import { layThongBaoLoi } from '../api/axiosClient';
import ErrorMessage from '../components/ErrorMessage.jsx';
import Loading from '../components/Loading.jsx';
import Icon from '../components/ui/Icon.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { formatTien } from '../utils/format';
import { chonHanhDongKhiMo, dinhDangDongHo, giayConLai, laLinkNhung } from '../utils/thanhToan.js';

const ID_KHUNG = 'payos-khung';
const SAP_HET_GIAY = 300;

// Một lần mở trang chỉ một yêu cầu tạo/lấy link cho mỗi đơn (StrictMode chạy effect hai lần ở dev)
const dangLay = new Map();

async function layLink(maDonHang) {
  const daNho = docLink(maDonHang);
  const ketQua = daNho ? await paymentApi.ketQua(daNho.orderCode, maDonHang).catch(() => null) : null;
  const hanhDong = chonHanhDongKhiMo(daNho, ketQua);
  if (hanhDong === 'sang_ket_qua') return { sangKetQua: daNho.orderCode };
  if (hanhDong === 'dung_lai') return { link: daNho };
  const link = await paymentApi.taoLink(maDonHang);
  luuLink(maDonHang, link);
  return { link };
}

function layLinkMotLan(maDonHang) {
  if (!dangLay.has(maDonHang)) {
    dangLay.set(maDonHang, layLink(maDonHang).finally(() => dangLay.delete(maDonHang)));
  }
  return dangLay.get(maDonHang);
}

/** Trang thanh toán PayOS nhúng: khung QR của PayOS bên trái, tóm tắt đơn và đồng hồ bên phải. */
export default function PaymentPage() {
  const { maDonHang = '' } = useParams();
  const navigate = useNavigate();
  const { nguoiDung } = useAuth();
  // dang_tai | nhung | da_dong | qua_han | loi
  const [trangThai, setTrangThai] = useState('dang_tai');
  const [link, setLink] = useState(null);
  const [loi, setLoi] = useState('');
  const [conLai, setConLai] = useState(null);
  const [lanMo, setLanMo] = useState(0);

  const veKetQua = useCallback((orderCode) => {
    navigate(`/thanh-toan/ket-qua/${encodeURIComponent(maDonHang)}?orderCode=${orderCode}`, { replace: true });
  }, [maDonHang, navigate]);

  const tai = useCallback(async () => {
    setTrangThai('dang_tai');
    setLoi('');
    try {
      const kq = await layLinkMotLan(maDonHang);
      if (kq.sangKetQua) {
        veKetQua(kq.sangKetQua);
        return;
      }
      if (!laLinkNhung(kq.link.duongDan)) {
        // Giả lập ở dev: link dẫn thẳng về trang kết quả
        window.location.replace(kq.link.duongDan);
        return;
      }
      setLink(kq.link);
      setConLai(giayConLai(kq.link.hetHan));
      setTrangThai(giayConLai(kq.link.hetHan) > 0 ? 'nhung' : 'qua_han');
    } catch (error) {
      setLoi(layThongBaoLoi(error));
      setTrangThai('loi');
    }
  }, [maDonHang, veKetQua]);

  useEffect(() => { tai(); }, [tai]);

  // Nhúng khung PayOS khi ô chứa đã có trên trang; rời trang / hết giờ thì gỡ khung. Layout effect: phần dọn chạy
  // trước khi React gỡ ô chứa khỏi DOM (thư viện cần tìm thấy ô chứa khi gỡ khung)
  useLayoutEffect(() => {
    if (trangThai !== 'nhung' || !link) return undefined;
    let daDong = false;
    const dong = (sau) => (event) => { daDong = true; sau(event); };
    const { open, exit } = taoKhungPayOS({
      RETURN_URL: `${window.location.origin}/thanh-toan/ket-qua/${encodeURIComponent(maDonHang)}`,
      ELEMENT_ID: ID_KHUNG,
      CHECKOUT_URL: link.duongDan,
      embedded: true,
      // Tín hiệu từ khung chỉ để chuyển trang – trang kết quả tự hỏi lại PayOS qua backend
      onSuccess: dong(() => veKetQua(link.orderCode)),
      onCancel: dong(() => veKetQua(link.orderCode)),
      onExit: dong(() => setTrangThai('da_dong')),
    });
    open();
    return () => { if (!daDong) exit(); };
  }, [trangThai, link, lanMo, maDonHang, veKetQua]);

  // Đồng hồ đếm ngược tới hạn thanh toán
  useEffect(() => {
    if (!link || (trangThai !== 'nhung' && trangThai !== 'da_dong')) return undefined;
    const dem = setInterval(() => {
      const giay = giayConLai(link.hetHan);
      setConLai(giay);
      if (giay === 0) setTrangThai('qua_han');
    }, 1000);
    return () => clearInterval(dem);
  }, [link, trangThai]);

  const xemDon = nguoiDung
    ? `/tai-khoan/don-hang/${encodeURIComponent(maDonHang)}`
    : `/tra-cuu-don-hang?ma=${encodeURIComponent(maDonHang)}`;
  const nutXemDon = (lop) => <Link className={lop} to={xemDon}>{nguoiDung ? 'Xem đơn hàng' : 'Tra cứu đơn hàng'}</Link>;

  if (trangThai === 'dang_tai') return <Loading noiDung="Đang mở cổng thanh toán PayOS..." />;

  if (trangThai === 'loi') {
    return (
      <div className="order-success payment-result is-failed">
        <span className="success-mark"><Icon name="close" size={36} strokeWidth={2.4} /></span>
        <h1>Chưa mở được thanh toán</h1>
        <ErrorMessage>{loi}</ErrorMessage>
        <div className="success-actions">
          <button type="button" className="btn btn-primary btn-lg" onClick={tai}>Thử lại</button>
          {nutXemDon('btn btn-outline btn-lg')}
        </div>
      </div>
    );
  }

  const sapHet = conLai !== null && conLai <= SAP_HET_GIAY;

  return (
    <div className="payment-page">
      <header className="checkout-top">
        <h1 className="page-title">Thanh toán đơn hàng</h1>
        <p className="muted">Mã đơn <strong>{maDonHang}</strong> · Chuyển khoản / quét mã VietQR qua PayOS</p>
      </header>

      <div className="checkout-layout payment-layout">
        <section className="payment-card" aria-label="Khung thanh toán PayOS">
          {/* Ô chứa luôn có trên trang (ẩn khi không nhúng): thư viện cần tìm thấy nó cả lúc gỡ khung */}
          <div id={ID_KHUNG} className="payment-frame" hidden={trangThai !== 'nhung'} />
          {trangThai === 'da_dong' && (
            <div className="payment-frame payment-frame-empty">
              <Icon name="wallet" size={40} />
              <p>Bạn đã đóng khung thanh toán. Đơn hàng vẫn chờ thanh toán.</p>
              <button type="button" className="btn btn-primary" onClick={() => { setLanMo((n) => n + 1); setTrangThai('nhung'); }}>
                Mở lại mã QR
              </button>
            </div>
          )}
          {trangThai === 'qua_han' && (
            <div className="payment-frame payment-frame-empty is-expired">
              <Icon name="close" size={40} />
              <p><strong>Đơn hàng đã quá hạn thanh toán.</strong></p>
              <p className="muted">Đơn PayOS chưa thanh toán sau 30 phút sẽ tự hủy. Nếu bạn đã chuyển khoản, cửa hàng sẽ liên hệ hoàn tiền.</p>
              {nutXemDon('btn btn-primary')}
            </div>
          )}
          {trangThai !== 'qua_han' && link && (
            <a className="payment-fallback" href={link.duongDan} target="_blank" rel="noopener noreferrer">
              Không hiện mã QR? Mở trang PayOS ↗
            </a>
          )}
        </section>

        <aside className="summary payment-summary">
          <div className={`payment-timer${sapHet ? ' is-warning' : ''}${trangThai === 'qua_han' ? ' is-expired' : ''}`} aria-live="off">
            <span>Thời gian còn lại</span>
            <strong>{dinhDangDongHo(conLai ?? 0)}</strong>
          </div>
          <dl className="summary-rows">
            <div><dt>Mã đơn hàng</dt><dd>{maDonHang}</dd></div>
            <div><dt>Phương thức</dt><dd>PayOS (VietQR)</dd></div>
          </dl>
          <div className="summary-total">
            <span>Tổng thanh toán</span>
            <strong>{link ? formatTien(link.tongTien) : '—'}</strong>
          </div>
          <ol className="payment-steps">
            <li>Mở ứng dụng ngân hàng hoặc ví có hỗ trợ VietQR.</li>
            <li>Quét mã QR – số tiền và nội dung chuyển khoản đã điền sẵn, không cần sửa.</li>
            <li>Xác nhận chuyển khoản. Trang sẽ tự chuyển khi PayOS báo đã nhận tiền.</li>
          </ol>
          {nutXemDon('btn btn-outline btn-block')}
          <ul className="summary-trust">
            <li><Icon name="shield" size={16} /> Thanh toán bảo mật qua PayOS, TechGear không lưu thông tin tài khoản của bạn</li>
          </ul>
        </aside>
      </div>
    </div>
  );
}
