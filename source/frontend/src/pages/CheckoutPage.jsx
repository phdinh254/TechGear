import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import accountApi from '../api/accountApi';
import checkoutApi from '../api/checkoutApi';
import { layThongBaoLoi } from '../api/axiosClient';
import ErrorMessage from '../components/ErrorMessage.jsx';
import Loading from '../components/Loading.jsx';
import EmptyState from '../components/ui/EmptyState.jsx';
import Icon from '../components/ui/Icon.jsx';
import ProductMedia from '../components/ui/ProductMedia.jsx';
import { SkeletonBlock } from '../components/ui/Skeleton.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { useCart } from '../context/CartContext.jsx';
import { formatTien } from '../utils/format';

function Step({ so, tieuDe, children, phu }) {
  return (
    <section className="checkout-step">
      <header>
        <span className="step-num">{so}</span>
        <h2>{tieuDe}</h2>
        {phu}
      </header>
      {children}
    </section>
  );
}

const PHUONG_THUC = [
  { giaTri: 'cod', icon: 'wallet', nhan: 'Thanh toán khi nhận hàng (COD)', moTa: 'Kiểm tra hàng rồi thanh toán cho nhân viên giao hàng' },
  { giaTri: 'payos', icon: 'bolt', nhan: 'Chuyển khoản / quét mã QR qua PayOS', moTa: 'Thanh toán ngay bằng ứng dụng ngân hàng, đơn được xác nhận nhanh hơn' },
];

export default function CheckoutPage() {
  const { nguoiDung } = useAuth();
  const { dong, xoaHet, dangDongBo } = useCart();
  const [form, setForm] = useState({
    tenNguoiNhan: nguoiDung?.hoTen || '',
    email: nguoiDung?.email || '',
    soDienThoai: nguoiDung?.soDienThoai || '',
    tinhThanh: '',
    phuongXa: '',
    diaChiChiTiet: '',
    ghiChu: '',
    maGiamGia: '',
    phuongThucThanhToan: 'cod',
  });
  const [diaChis, setDiaChis] = useState([]);
  const [diaChiChon, setDiaChiChon] = useState('');
  const [tinhTien, setTinhTien] = useState(null);
  const [dangTinh, setDangTinh] = useState(true);
  const [dangDat, setDangDat] = useState(false);
  const [loi, setLoi] = useState('');
  const [loiMa, setLoiMa] = useState('');
  const [ketQua, setKetQua] = useState(null);

  const dongs = dong.map((item) => ({ bienTheId: item.bienTheId, soLuong: item.soLuong }));
  // Email giúp backend kiểm mã giảm giá giới hạn theo khách; chỉ gửi khi đã đúng định dạng
  const emailTinh = () => (/^\S+@\S+\.\S+$/.test(form.email.trim()) ? form.email.trim() : null);
  const navigate = useNavigate();
  const doi = (khoa) => (e) => setForm({ ...form, [khoa]: e.target.value });

  // Mã giảm giá làm đơn về 0đ thì PayOS không dùng được: chuyển về COD
  useEffect(() => {
    if (tinhTien && Number(tinhTien.tongTien) === 0 && form.phuongThucThanhToan === 'payos') {
      setForm((f) => ({ ...f, phuongThucThanhToan: 'cod' }));
    }
  }, [tinhTien, form.phuongThucThanhToan]);

  const chonDiaChi = (diaChi) => {
    setDiaChiChon(String(diaChi.id));
    setForm((cu) => ({
      ...cu,
      tenNguoiNhan: diaChi.tenNguoiNhan,
      soDienThoai: diaChi.soDienThoai,
      tinhThanh: diaChi.tinhThanh,
      phuongXa: diaChi.phuongXa,
      diaChiChiTiet: diaChi.diaChiChiTiet,
    }));
  };

  useEffect(() => {
    if (!nguoiDung) return;
    // Thông tin người dùng có thể tải xong sau lần render đầu (vào thẳng /dat-hang hoặc F5):
    // chỉ điền vào các ô còn trống để không ghi đè dữ liệu khách đã nhập.
    setForm((cu) => ({
      ...cu,
      tenNguoiNhan: cu.tenNguoiNhan || nguoiDung.hoTen || '',
      email: cu.email || nguoiDung.email || '',
      soDienThoai: cu.soDienThoai || nguoiDung.soDienThoai || '',
    }));
    accountApi.layDiaChi().then((data) => {
      setDiaChis(data);
      const macDinh = data.find((item) => item.laMacDinh) || data[0];
      if (macDinh) chonDiaChi(macDinh);
    }).catch(() => {});
  }, [nguoiDung]);

  useEffect(() => {
    if (dangDongBo || dong.length === 0) {
      setDangTinh(false);
      return;
    }
    setDangTinh(true);
    checkoutApi.tinhTien({ maGiamGia: tinhTien?.maGiamGia || null, dongs, email: emailTinh() })
      .then(setTinhTien)
      .catch((error) => setLoi(layThongBaoLoi(error)))
      .finally(() => setDangTinh(false));
  }, [dangDongBo, dong]);

  const apDungMa = async () => {
    setLoiMa('');
    setDangTinh(true);
    try {
      setTinhTien(await checkoutApi.tinhTien({ maGiamGia: form.maGiamGia.trim() || null, dongs, email: emailTinh() }));
    } catch (error) {
      setLoiMa(layThongBaoLoi(error));
    } finally {
      setDangTinh(false);
    }
  };

  const boMa = async () => {
    setForm({ ...form, maGiamGia: '' });
    setLoiMa('');
    setDangTinh(true);
    try {
      setTinhTien(await checkoutApi.tinhTien({ maGiamGia: null, dongs, email: emailTinh() }));
    } catch (error) {
      setLoi(layThongBaoLoi(error));
    } finally {
      setDangTinh(false);
    }
  };

  const datHang = async (event) => {
    event.preventDefault();
    setLoi('');
    setDangDat(true);
    try {
      const response = await checkoutApi.datHang({
        tenNguoiNhan: form.tenNguoiNhan,
        email: form.email.trim() || null,
        soDienThoai: form.soDienThoai,
        tinhThanh: form.tinhThanh,
        phuongXa: form.phuongXa,
        diaChiChiTiet: form.diaChiChiTiet,
        ghiChu: form.ghiChu || null,
        maGiamGia: tinhTien?.maGiamGia || null,
        phuongThucThanhToan: form.phuongThucThanhToan,
        dongs,
      }, Boolean(nguoiDung));
      await xoaHet();
      if (response.phuongThucThanhToan === 'payos') {
        // Trang thanh toán nhúng PayOS tự tạo link (lỗi tạo link hiện ở đó, kèm nút xem đơn)
        navigate(`/thanh-toan/${encodeURIComponent(response.maDonHang)}`, { replace: true });
        return;
      }
      setKetQua(response);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } catch (error) {
      setLoi(layThongBaoLoi(error));
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } finally {
      setDangDat(false);
    }
  };

  if (dangDongBo) return <Loading noiDung="Đang chuẩn bị đơn hàng..." />;

  if (ketQua) {
    return (
      <div className="order-success">
        <span className="success-mark"><Icon name="check" size={36} strokeWidth={2.4} /></span>
        <h1>Đặt hàng thành công</h1>
        <p className="lead">Chúng tôi sẽ liên hệ xác nhận đơn hàng sớm nhất.</p>
        <dl className="success-info">
          <div><dt>Mã đơn hàng</dt><dd>{ketQua.maDonHang}</dd></div>
          <div><dt>Tổng thanh toán</dt><dd>{formatTien(ketQua.tongTien)}</dd></div>
          <div><dt>Thanh toán</dt><dd>Khi nhận hàng</dd></div>
        </dl>
        {!nguoiDung && <p className="muted">Lưu lại mã đơn để tra cứu tình trạng bằng email đặt hàng.</p>}
        <div className="success-actions">
          {nguoiDung ? (
            <Link className="btn btn-primary btn-lg" to={`/tai-khoan/don-hang/${ketQua.maDonHang}`}>Xem đơn hàng</Link>
          ) : (
            <Link className="btn btn-primary btn-lg" to={`/tra-cuu-don-hang?ma=${encodeURIComponent(ketQua.maDonHang)}`}>Tra cứu đơn hàng</Link>
          )}
          <Link className="btn btn-outline btn-lg" to="/san-pham">Tiếp tục mua sắm</Link>
        </div>
      </div>
    );
  }

  if (dong.length === 0) {
    return (
      <EmptyState icon="bag" tieuDe="Chưa có sản phẩm để thanh toán" moTa="Thêm sản phẩm vào giỏ hàng trước khi đặt hàng.">
        <Link className="btn btn-primary" to="/san-pham">Mua sắm ngay</Link>
      </EmptyState>
    );
  }

  return (
    <div className="checkout-page">
      <div className="checkout-top">
        <Link to="/gio-hang" className="link-arrow"><Icon name="chevronLeft" size={16} /> Quay lại giỏ hàng</Link>
        <h1 className="page-title">Thanh toán</h1>
        {!nguoiDung && (
          <p className="muted">Đã có tài khoản? <Link to="/dang-nhap" state={{ tu: '/dat-hang' }} className="link-button">Đăng nhập</Link> để dùng địa chỉ đã lưu.</p>
        )}
      </div>

      <form className="checkout-layout" onSubmit={datHang}>
        <div className="checkout-main">
          <ErrorMessage>{loi}</ErrorMessage>

          <Step so={1} tieuDe="Thông tin liên hệ">
            <div className="form-grid">
              <label className="field span-2">Họ tên người nhận<input required maxLength="100" autoComplete="name" value={form.tenNguoiNhan} onChange={doi('tenNguoiNhan')} /></label>
              <label className="field">Email<input type="email" required maxLength="150" autoComplete="email" value={form.email} onChange={doi('email')} /></label>
              <label className="field">Số điện thoại<input type="tel" required pattern="0[0-9]{9}" title="10 chữ số, bắt đầu bằng 0" autoComplete="tel" value={form.soDienThoai} onChange={doi('soDienThoai')} /></label>
            </div>
          </Step>

          <Step so={2} tieuDe="Địa chỉ giao hàng">
            {diaChis.length > 0 && (
              <div className="address-picker">
                {diaChis.map((dc) => (
                  <label key={dc.id} className={`radio-card${diaChiChon === String(dc.id) ? ' is-active' : ''}`}>
                    <input type="radio" name="dia-chi" checked={diaChiChon === String(dc.id)} onChange={() => chonDiaChi(dc)} />
                    <span>
                      <strong>{dc.tenNguoiNhan} · {dc.soDienThoai}{dc.laMacDinh && <span className="badge badge-soft">Mặc định</span>}</strong>
                      <span className="muted">{dc.diaChiDayDu || `${dc.diaChiChiTiet}, ${dc.phuongXa}, ${dc.tinhThanh}`}</span>
                    </span>
                  </label>
                ))}
                <label className={`radio-card${diaChiChon === '' ? ' is-active' : ''}`}>
                  <input type="radio" name="dia-chi" checked={diaChiChon === ''} onChange={() => { setDiaChiChon(''); setForm({ ...form, tinhThanh: '', phuongXa: '', diaChiChiTiet: '' }); }} />
                  <span><strong>Giao tới địa chỉ khác</strong></span>
                </label>
              </div>
            )}
            <div className="form-grid">
              <label className="field">Tỉnh / thành phố<input required maxLength="100" autoComplete="address-level1" value={form.tinhThanh} onChange={doi('tinhThanh')} /></label>
              <label className="field">Phường / xã<input required maxLength="100" autoComplete="address-level2" value={form.phuongXa} onChange={doi('phuongXa')} /></label>
              <label className="field span-2">Địa chỉ chi tiết<input required maxLength="255" autoComplete="street-address" placeholder="Số nhà, tên đường" value={form.diaChiChiTiet} onChange={doi('diaChiChiTiet')} /></label>
              <label className="field span-2"><span>Ghi chú <span className="field-hint">(không bắt buộc)</span></span><textarea maxLength="500" rows="3" placeholder="Ví dụ: giao giờ hành chính" value={form.ghiChu} onChange={doi('ghiChu')} /></label>
            </div>
          </Step>

          <Step so={3} tieuDe="Phương thức thanh toán">
            <div className="payment-options">
              {PHUONG_THUC.map((pt) => {
                // PayOS không nhận đơn 0đ (backend cũng chặn)
                const khoa = pt.giaTri === 'payos' && tinhTien && Number(tinhTien.tongTien) === 0;
                const chon = form.phuongThucThanhToan === pt.giaTri;
                return (
                  <label key={pt.giaTri} className={`radio-card${chon ? ' is-active' : ''}${khoa ? ' is-disabled' : ''}`}>
                    <input type="radio" name="thanh-toan" value={pt.giaTri} checked={chon} disabled={khoa} onChange={doi('phuongThucThanhToan')} />
                    <Icon name={pt.icon} size={22} />
                    <span><strong>{pt.nhan}</strong><span className="muted">{khoa ? 'Đơn 0đ không cần thanh toán trực tuyến' : pt.moTa}</span></span>
                  </label>
                );
              })}
            </div>
          </Step>
        </div>

        <aside className="summary checkout-summary">
          <h2>Đơn hàng <span className="muted">({dong.length} sản phẩm)</span></h2>
          <ul className="summary-items">
            {dong.map((item) => (
              <li key={item.bienTheId}>
                <span className="summary-thumb">
                  <ProductMedia src={item.anh} ten={item.ten} size="sm" />
                  <span className="summary-qty">{item.soLuong}</span>
                </span>
                <span className="summary-item-name">
                  {item.ten}
                  {item.phanLoai && <small>{item.phanLoai}</small>}
                </span>
                <strong>{formatTien(Number(item.giaBan) * item.soLuong)}</strong>
              </li>
            ))}
          </ul>

          {tinhTien?.maGiamGia ? (
            <div className="coupon-applied">
              <Icon name="check" size={16} strokeWidth={2.2} />
              <span><strong>{tinhTien.maGiamGia}</strong>{tinhTien.moTaGiamGia && ` · ${tinhTien.moTaGiamGia}`}</span>
              <button type="button" className="link-button" onClick={boMa}>Bỏ mã</button>
            </div>
          ) : (
            <div className="coupon">
              <div className="coupon-row">
                <input placeholder="Mã giảm giá" maxLength="50" aria-label="Mã giảm giá" value={form.maGiamGia} onChange={(e) => setForm({ ...form, maGiamGia: e.target.value.toUpperCase() })} onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); apDungMa(); } }} />
                <button type="button" className="btn btn-outline" onClick={apDungMa} disabled={!form.maGiamGia.trim() || dangTinh}>Áp dụng</button>
              </div>
              {loiMa && <p className="field-error">{loiMa}</p>}
            </div>
          )}

          {dangTinh && !tinhTien ? (
            <SkeletonBlock height={120} />
          ) : tinhTien && (
            <dl className={`summary-rows${dangTinh ? ' is-loading' : ''}`}>
              <div><dt>Tạm tính</dt><dd>{formatTien(tinhTien.tamTinh)}</dd></div>
              {Number(tinhTien.tienGiamGia) > 0 && <div className="is-discount"><dt>Giảm giá</dt><dd>-{formatTien(tinhTien.tienGiamGia)}</dd></div>}
              <div><dt>Phí vận chuyển</dt><dd>{Number(tinhTien.phiVanChuyen) === 0 ? 'Miễn phí' : formatTien(tinhTien.phiVanChuyen)}</dd></div>
            </dl>
          )}
          {tinhTien && <div className="summary-total"><span>Tổng cộng</span><strong>{formatTien(tinhTien.tongTien)}</strong></div>}

          <button type="submit" className="btn btn-primary btn-lg btn-block" disabled={dangDat || dangTinh || !tinhTien}>
            {dangDat ? <><span className="spinner" /> Đang đặt hàng...</> : form.phuongThucThanhToan === 'payos' ? 'Đặt hàng và thanh toán' : 'Xác nhận đặt hàng'}
          </button>
          <p className="summary-note"><Icon name="lock" size={14} /><span>Bằng việc đặt hàng, bạn đồng ý với <Link to="/chinh-sach">chính sách mua hàng</Link>.</span></p>
        </aside>
      </form>
    </div>
  );
}
