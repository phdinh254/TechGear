import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { orderApi, reviewApi } from '../api/customerApi';
import { layThongBaoLoi } from '../api/axiosClient';
import ErrorMessage from '../components/ErrorMessage.jsx';
import Loading from '../components/Loading.jsx';
import { duongDanAnh, formatTien } from '../utils/format';
import { NHAN_TRANG_THAI } from './OrdersPage.jsx';

export default function OrderDetailPage() {
  const { maDonHang } = useParams();
  const [donHang, setDonHang] = useState(null);
  const [loi, setLoi] = useState('');
  const [thongBao, setThongBao] = useState('');
  const [danhGia, setDanhGia] = useState({});

  const taiDon = () => orderApi.layChiTiet(maDonHang).then(setDonHang).catch((error) => setLoi(layThongBaoLoi(error)));
  useEffect(() => { taiDon(); }, [maDonHang]);

  const huyDon = async () => {
    const lyDo = window.prompt('Nhập lý do hủy đơn (không bắt buộc):', '') ?? null;
    if (lyDo === null) return;
    try {
      setDonHang(await orderApi.huy(maDonHang, lyDo));
      setThongBao('Đã hủy đơn hàng và hoàn lại tồn kho');
    } catch (error) { setLoi(layThongBaoLoi(error)); }
  };

  const guiDanhGia = async (item) => {
    const form = danhGia[item.sanPhamId] || { soSao: 5, noiDung: '' };
    try {
      await reviewApi.tao({ donHangId: donHang.id, sanPhamId: item.sanPhamId, ...form });
      setThongBao('Đánh giá đã được gửi và đang chờ duyệt');
      setDanhGia({ ...danhGia, [item.sanPhamId]: { ...form, daGui: true } });
    } catch (error) { setLoi(layThongBaoLoi(error)); }
  };

  if (!donHang && !loi) return <Loading />;
  if (!donHang) return <ErrorMessage>{loi}</ErrorMessage>;
  return <div>
    <p><Link to="/tai-khoan/don-hang">← Danh sách đơn hàng</Link></p>
    <div className="page-heading"><h1>Đơn {donHang.maDonHang}</h1><span className={`status status-${donHang.trangThai}`}>{NHAN_TRANG_THAI[donHang.trangThai]}</span></div>
    <ErrorMessage>{loi}</ErrorMessage>{thongBao && <p className="success">{thongBao}</p>}
    <div className="order-detail-grid">
      <section className="order-products">{donHang.chiTiets.map((item) => <article className="order-product" key={item.id}>
        <div className="cart-thumb">{item.hinhAnh && <img src={duongDanAnh(item.hinhAnh)} alt={item.tenSanPham} />}</div>
        <div><strong>{item.tenSanPham}</strong><p className="muted">{item.phanLoai || item.sku}</p><p>{formatTien(item.donGia)} × {item.soLuong}</p></div>
        <strong>{formatTien(item.thanhTien)}</strong>
        {donHang.trangThai === 'hoan_thanh' && item.sanPhamId && <div className="review-form">
          <select value={danhGia[item.sanPhamId]?.soSao || 5} disabled={danhGia[item.sanPhamId]?.daGui} onChange={(e) => setDanhGia({ ...danhGia, [item.sanPhamId]: { ...danhGia[item.sanPhamId], soSao: Number(e.target.value) } })}>{[5,4,3,2,1].map((sao) => <option key={sao} value={sao}>{sao} sao</option>)}</select>
          <input placeholder="Nhận xét của bạn" maxLength="2000" disabled={danhGia[item.sanPhamId]?.daGui} value={danhGia[item.sanPhamId]?.noiDung || ''} onChange={(e) => setDanhGia({ ...danhGia, [item.sanPhamId]: { ...danhGia[item.sanPhamId], noiDung: e.target.value } })} />
          <button type="button" disabled={danhGia[item.sanPhamId]?.daGui} onClick={() => guiDanhGia(item)}>{danhGia[item.sanPhamId]?.daGui ? 'Đã gửi' : 'Gửi đánh giá'}</button>
        </div>}
      </article>)}</section>
      <aside className="order-info"><h2>Giao hàng</h2><p><strong>{donHang.tenNguoiNhan}</strong></p><p>{donHang.soDienThoai}</p><p>{donHang.diaChiGiaoHang}</p><h2>Thanh toán</h2><p>Tạm tính <strong>{formatTien(donHang.tamTinh)}</strong></p><p>Giảm giá <strong>-{formatTien(donHang.tienGiamGia)}</strong></p><p>Phí giao hàng <strong>{formatTien(donHang.phiVanChuyen)}</strong></p><p className="price big">{formatTien(donHang.tongTien)}</p>{donHang.trangThai === 'cho_xac_nhan' && <button type="button" className="danger-button" onClick={huyDon}>Hủy đơn hàng</button>}</aside>
    </div>
    <section className="section"><h2>Lịch sử trạng thái</h2><div className="timeline">{donHang.lichSus.map((item, index) => <div key={`${item.trangThai}-${index}`}><strong>{NHAN_TRANG_THAI[item.trangThai]}</strong><span>{item.thoiGian ? new Date(item.thoiGian).toLocaleString('vi-VN') : ''}</span>{item.ghiChu && <p>{item.ghiChu}</p>}</div>)}</div></section>
  </div>;
}
