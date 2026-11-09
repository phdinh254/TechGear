import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import accountApi from '../api/accountApi';
import checkoutApi from '../api/checkoutApi';
import { layThongBaoLoi } from '../api/axiosClient';
import ErrorMessage from '../components/ErrorMessage.jsx';
import Loading from '../components/Loading.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { useCart } from '../context/CartContext.jsx';
import { formatTien } from '../utils/format';

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
  const [tinhTien, setTinhTien] = useState(null);
  const [dangTinh, setDangTinh] = useState(true);
  const [dangDat, setDangDat] = useState(false);
  const [loi, setLoi] = useState('');
  const [ketQua, setKetQua] = useState(null);

  const items = dong.map((item) => ({ bienTheId: item.bienTheId, soLuong: item.soLuong }));

  useEffect(() => {
    if (!nguoiDung) return;
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
    checkoutApi.tinhTien({ items })
      .then(setTinhTien)
      .catch((error) => setLoi(layThongBaoLoi(error)))
      .finally(() => setDangTinh(false));
  }, [dangDongBo, dong]);

  const chonDiaChi = (diaChi) => setForm((cu) => ({
    ...cu,
    tenNguoiNhan: diaChi.tenNguoiNhan,
    soDienThoai: diaChi.soDienThoai,
    tinhThanh: diaChi.tinhThanh,
    phuongXa: diaChi.phuongXa,
    diaChiChiTiet: diaChi.diaChiChiTiet,
  }));

  const apDungMa = async () => {
    setLoi('');
    setDangTinh(true);
    try {
      setTinhTien(await checkoutApi.tinhTien({ maGiamGia: form.maGiamGia.trim() || null, items }));
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
        email: form.email,
        soDienThoai: form.soDienThoai,
        diaChiGiaoHang: `${form.diaChiChiTiet}, ${form.phuongXa}, ${form.tinhThanh}`,
        ghiChu: form.ghiChu || null,
        maGiamGia: tinhTien?.maGiamGia || null,
        phuongThucThanhToan: form.phuongThucThanhToan,
        items,
      });
      await xoaHet();
      setKetQua(response);
    } catch (error) {
      setLoi(layThongBaoLoi(error));
    } finally {
      setDangDat(false);
    }
  };

  if (dangDongBo) return <Loading />;
  if (ketQua) {
    return (
      <div className="order-success">
        <h1>Đặt hàng thành công</h1>
        <p>Mã đơn hàng của bạn: <strong>{ketQua.maDonHang}</strong></p>
        <p>Tổng thanh toán: <strong>{formatTien(ketQua.tongTien)}</strong></p>
        <Link className="btn" to={nguoiDung ? '/tai-khoan' : '/'}>Tiếp tục</Link>
      </div>
    );
  }
  if (dong.length === 0) {
    return <div className="state"><p>Giỏ hàng đang trống.</p><Link className="btn" to="/san-pham">Mua sắm ngay</Link></div>;
  }

  return (
    <form className="checkout-layout" onSubmit={datHang}>
      <section className="checkout-form">
        <h1>Thông tin giao hàng</h1>
        <ErrorMessage>{loi}</ErrorMessage>
        {diaChis.length > 0 && (
          <label>Chọn địa chỉ đã lưu<select defaultValue="" onChange={(e) => {
            const diaChi = diaChis.find((item) => item.id === Number(e.target.value));
            if (diaChi) chonDiaChi(diaChi);
          }}><option value="">Nhập địa chỉ khác</option>{diaChis.map((item) => <option key={item.id} value={item.id}>{item.diaChiChiTiet}, {item.phuongXa}, {item.tinhThanh}</option>)}</select></label>
        )}
        <div className="form">
          <label>Họ tên người nhận<input required maxLength="100" value={form.tenNguoiNhan} onChange={(e) => setForm({ ...form, tenNguoiNhan: e.target.value })} /></label>
          <label>Email<input type="email" required maxLength="150" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} /></label>
          <label>Số điện thoại<input type="tel" required pattern="0[0-9]{9}" value={form.soDienThoai} onChange={(e) => setForm({ ...form, soDienThoai: e.target.value })} /></label>
          <label>Tỉnh/thành<input required maxLength="100" value={form.tinhThanh} onChange={(e) => setForm({ ...form, tinhThanh: e.target.value })} /></label>
          <label>Phường/xã<input required maxLength="100" value={form.phuongXa} onChange={(e) => setForm({ ...form, phuongXa: e.target.value })} /></label>
          <label>Địa chỉ chi tiết<input required maxLength="255" value={form.diaChiChiTiet} onChange={(e) => setForm({ ...form, diaChiChiTiet: e.target.value })} /></label>
          <label>Ghi chú<textarea maxLength="500" rows="3" value={form.ghiChu} onChange={(e) => setForm({ ...form, ghiChu: e.target.value })} /></label>
        </div>
      </section>

      <aside className="checkout-summary">
        <h2>Đơn hàng ({dong.length} sản phẩm)</h2>
        {dong.map((item) => <div className="checkout-line" key={item.bienTheId}><span>{item.ten} × {item.soLuong}</span><strong>{formatTien(Number(item.giaBan) * item.soLuong)}</strong></div>)}
        <div className="coupon-row"><input placeholder="Mã giảm giá" maxLength="50" value={form.maGiamGia} onChange={(e) => setForm({ ...form, maGiamGia: e.target.value.toUpperCase() })} /><button type="button" onClick={apDungMa}>Áp dụng</button></div>
        {dangTinh ? <Loading /> : tinhTien && <div className="totals">
          <p><span>Tạm tính</span><strong>{formatTien(tinhTien.tamTinh)}</strong></p>
          <p><span>Giảm giá</span><strong>-{formatTien(tinhTien.tienGiamGia)}</strong></p>
          <p><span>Phí vận chuyển</span><strong>{formatTien(tinhTien.phiVanChuyen)}</strong></p>
          <p className="total"><span>Tổng cộng</span><strong>{formatTien(tinhTien.tongTien)}</strong></p>
          {tinhTien.moTaGiamGia && <p className="success">{tinhTien.moTaGiamGia}</p>}
        </div>}
        <fieldset><legend>Thanh toán</legend><label className="checkbox-label"><input type="radio" checked readOnly /> Thanh toán khi nhận hàng (COD)</label><label className="checkbox-label muted"><input type="radio" disabled /> VNPay — chưa cấu hình</label></fieldset>
        <button className="btn checkout-button" disabled={dangDat || dangTinh || !tinhTien}>{dangDat ? 'Đang đặt hàng...' : 'Xác nhận đặt hàng'}</button>
      </aside>
    </form>
  );
}
