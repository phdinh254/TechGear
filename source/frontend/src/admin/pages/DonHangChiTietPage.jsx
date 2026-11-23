import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import adminApi from '../api/adminApi.js';
import Modal from '../components/Modal.jsx';
import DataTable from '../components/DataTable.jsx';
import ErrorMessage from '../../components/ErrorMessage.jsx';
import OrderView, { dinhDangThoiGian } from '../../components/OrderView.jsx';
import Icon from '../../components/ui/Icon.jsx';
import { useToast } from '../../context/ToastContext.jsx';
import { layThongBaoLoi } from '../../api/axiosClient.js';
import { formatTien } from '../../utils/format.js';
import { Badge, NHAN_GIAO_DICH, NHAN_PHUONG_THUC, NHAN_THANH_TOAN, NHAN_TRANG_THAI_DON } from '../utils/nhan.jsx';

// Nhãn nút theo trạng thái đích (backend trả danh sách trangThaiTiepTheo hợp lệ)
const NUT_CHUYEN = {
  da_xac_nhan: 'Xác nhận đơn',
  dang_giao: 'Bắt đầu giao',
  hoan_thanh: 'Đã giao xong',
  da_huy: 'Hủy đơn',
};

export default function DonHangChiTietPage() {
  const { maDonHang } = useParams();
  const toast = useToast();
  const [ct, setCt] = useState(null);
  const [loiTai, setLoiTai] = useState('');
  const [chuyen, setChuyen] = useState(null); // trạng thái đích đang chờ xác nhận
  const [ghiChuChuyen, setGhiChuChuyen] = useState('');
  const [loiChuyen, setLoiChuyen] = useState('');
  const [dangXuLy, setDangXuLy] = useState(false);
  const [ghiChuQT, setGhiChuQT] = useState('');

  useEffect(() => {
    let huy = false;
    setCt(null);
    adminApi.donHang.layChiTiet(maDonHang)
      .then((kq) => { if (!huy) { setCt(kq); setGhiChuQT(kq.ghiChuQuanTri || ''); setLoiTai(''); } })
      .catch((err) => { if (!huy) setLoiTai(layThongBaoLoi(err)); });
    return () => { huy = true; };
  }, [maDonHang]);

  const capNhat = (kq, thongBao) => {
    setCt(kq);
    setGhiChuQT(kq.ghiChuQuanTri || '');
    toast.hien(thongBao);
  };

  const moChuyen = (trangThai) => { setChuyen(trangThai); setGhiChuChuyen(''); setLoiChuyen(''); };

  const xacNhanChuyen = async (e) => {
    e.preventDefault();
    if (chuyen === 'da_huy' && !ghiChuChuyen.trim()) { setLoiChuyen('Vui lòng nhập lý do hủy'); return; }
    setDangXuLy(true);
    setLoiChuyen('');
    try {
      capNhat(await adminApi.donHang.doiTrangThai(maDonHang, chuyen, ghiChuChuyen.trim()), `Đơn đã chuyển sang "${NHAN_TRANG_THAI_DON[chuyen]}"`);
      setChuyen(null);
    } catch (err) {
      setLoiChuyen(layThongBaoLoi(err));
    } finally {
      setDangXuLy(false);
    }
  };

  const doiThanhToan = async (trangThaiThanhToan) => {
    setDangXuLy(true);
    try {
      capNhat(await adminApi.donHang.doiThanhToan(maDonHang, trangThaiThanhToan), `Đã cập nhật: ${NHAN_THANH_TOAN[trangThaiThanhToan].nhan}`);
    } catch (err) {
      toast.hien(layThongBaoLoi(err), { loai: 'error' });
    } finally {
      setDangXuLy(false);
    }
  };

  const luuGhiChu = async (e) => {
    e.preventDefault();
    setDangXuLy(true);
    try {
      capNhat(await adminApi.donHang.luuGhiChu(maDonHang, ghiChuQT.trim()), 'Đã lưu ghi chú nội bộ');
    } catch (err) {
      toast.hien(layThongBaoLoi(err), { loai: 'error' });
    } finally {
      setDangXuLy(false);
    }
  };

  if (loiTai) {
    return (
      <>
        <Link to="/admin/don-hang" className="admin-back"><Icon name="chevronLeft" size={18} /> Danh sách đơn</Link>
        <ErrorMessage>{loiTai}</ErrorMessage>
      </>
    );
  }
  if (!ct) return <div className="admin-table-note"><span className="spinner" aria-label="Đang tải" /></div>;

  const { don } = ct;
  // Quy tắc thanh toán của backend: COD chưa trả → đã trả (đơn chưa hủy); đã trả → hoàn tiền khi đơn đã hủy
  const coTheThu = don.phuongThucThanhToan === 'cod' && don.trangThaiThanhToan === 'chua_thanh_toan' && don.trangThai !== 'da_huy';
  const coTheHoan = don.trangThaiThanhToan === 'da_thanh_toan' && don.trangThai === 'da_huy';
  const coGhiChuMoi = ghiChuQT.trim() !== (ct.ghiChuQuanTri || '');

  const cotGiaoDich = [
    { khoa: 'maThamChieu', tieuDe: 'Mã tham chiếu' },
    { khoa: 'soTien', tieuDe: 'Số tiền', canh: 'right', hienThi: (g) => formatTien(g.soTien) },
    { khoa: 'trangThai', tieuDe: 'Kết quả', hienThi: (g) => <Badge bang={NHAN_GIAO_DICH} giaTri={g.trangThai} /> },
    { khoa: 'maGiaoDich', tieuDe: 'Mã giao dịch', hienThi: (g) => g.maGiaoDich || '—' },
    { khoa: 'thoiGian', tieuDe: 'Thời gian', hienThi: (g) => dinhDangThoiGian(g.thanhToanLuc || g.ngayTao) },
  ];

  return (
    <>
      <Link to="/admin/don-hang" className="admin-back"><Icon name="chevronLeft" size={18} /> Danh sách đơn</Link>

      <section className="admin-card admin-order-actions" aria-label="Xử lý đơn hàng">
        <div className="admin-order-meta">
          <span>Thanh toán <Badge bang={NHAN_THANH_TOAN} giaTri={don.trangThaiThanhToan} /></span>
          <span>Phương thức <strong>{NHAN_PHUONG_THUC[don.phuongThucThanhToan] || don.phuongThucThanhToan}</strong></span>
          <span>Email <strong>{don.email}</strong></span>
          <span>Khách <strong>{ct.nguoiDungId ? 'Có tài khoản' : 'Vãng lai'}</strong></span>
        </div>
        <div className="admin-order-buttons">
          {ct.trangThaiTiepTheo.length === 0 && <span className="field-hint">Đơn đã kết thúc, không còn bước xử lý.</span>}
          {ct.trangThaiTiepTheo.map((t) => (
            <button key={t} type="button" className={`btn ${t === 'da_huy' ? 'btn-outline admin-btn-danger' : 'btn-primary'}`} onClick={() => moChuyen(t)} disabled={dangXuLy}>
              {NUT_CHUYEN[t] || NHAN_TRANG_THAI_DON[t]}
            </button>
          ))}
          {coTheThu && <button type="button" className="btn btn-outline" onClick={() => doiThanhToan('da_thanh_toan')} disabled={dangXuLy}>Đánh dấu đã thu tiền</button>}
          {coTheHoan && <button type="button" className="btn btn-outline" onClick={() => doiThanhToan('da_hoan_tien')} disabled={dangXuLy}>Đánh dấu đã hoàn tiền</button>}
        </div>
      </section>

      <OrderView donHang={don} />

      <div className="admin-order-extra">
        <form className="admin-card form" onSubmit={luuGhiChu}>
          <h2 className="admin-card-title">Ghi chú nội bộ</h2>
          <textarea rows={3} maxLength={500} value={ghiChuQT} onChange={(e) => setGhiChuQT(e.target.value)} placeholder="Chỉ quản trị viên thấy, VD: khách hẹn giao sau 17h" aria-label="Ghi chú nội bộ" />
          <div className="modal-actions">
            <button type="submit" className="btn btn-primary" disabled={dangXuLy || !coGhiChuMoi}>Lưu ghi chú</button>
          </div>
        </form>
        {ct.thanhToans.length > 0 && (
          <section className="admin-card">
            <h2 className="admin-card-title">Giao dịch thanh toán online</h2>
            <DataTable cot={cotGiaoDich} duLieu={ct.thanhToans} khoaDong="maThamChieu" />
          </section>
        )}
      </div>

      <Modal tieuDe={chuyen ? `${NUT_CHUYEN[chuyen] || NHAN_TRANG_THAI_DON[chuyen]} – ${don.maDonHang}` : ''} mo={chuyen !== null} onClose={() => { if (!dangXuLy) setChuyen(null); }}>
        <form className="form" onSubmit={xacNhanChuyen} noValidate>
          <ErrorMessage>{loiChuyen}</ErrorMessage>
          {chuyen === 'da_huy' && <p className="modal-text">Hủy đơn sẽ hoàn lại tồn kho và trả lượt mã giảm giá cho khách.</p>}
          <label>{chuyen === 'da_huy' ? 'Lý do hủy' : 'Ghi chú (không bắt buộc)'}
            <textarea rows={3} maxLength={500} value={ghiChuChuyen} onChange={(e) => setGhiChuChuyen(e.target.value)} />
            <small className="field-hint">Ghi chú hiện trong lịch sử trạng thái mà khách xem được.</small>
          </label>
          <div className="modal-actions">
            <button type="button" className="btn btn-outline" onClick={() => setChuyen(null)} disabled={dangXuLy}>Đóng</button>
            <button type="submit" className={`btn ${chuyen === 'da_huy' ? 'btn-danger' : 'btn-primary'}`} disabled={dangXuLy}>{dangXuLy ? 'Đang xử lý...' : 'Xác nhận'}</button>
          </div>
        </form>
      </Modal>
    </>
  );
}
