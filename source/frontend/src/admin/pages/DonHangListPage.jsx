import { useCallback, useEffect, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import adminApi from '../api/adminApi.js';
import DataTable from '../components/DataTable.jsx';
import ErrorMessage from '../../components/ErrorMessage.jsx';
import Pagination from '../../components/Pagination.jsx';
import { layThongBaoLoi } from '../../api/axiosClient.js';
import { formatTien } from '../../utils/format.js';
import { dinhDangThoiGian } from '../../components/OrderView.jsx';
import { Badge, NHAN_PHUONG_THUC, NHAN_THANH_TOAN, NHAN_TRANG_THAI_DON } from '../utils/nhan.jsx';

const KICH_THUOC = 20;
const LOC = ['tuKhoa', 'trangThai', 'trangThaiThanhToan', 'phuongThucThanhToan', 'tuNgay', 'denNgay'];

export default function DonHangListPage() {
  const [params, setParams] = useSearchParams();
  const loc = Object.fromEntries(LOC.map((k) => [k, params.get(k) || '']));
  const trang = Math.max(0, Number(params.get('trang')) || 0);
  const khoaLoc = LOC.map((k) => loc[k]).join('|');

  const [oTim, setOTim] = useState(loc.tuKhoa);
  const [ketQua, setKetQua] = useState(null);
  const [dangTai, setDangTai] = useState(true);
  const [loiTai, setLoiTai] = useState('');
  const dem = useRef(0);

  const datLoc = useCallback((khoa, giaTri) => {
    setParams((p) => {
      const moi = new URLSearchParams(p);
      if (giaTri === '' || giaTri == null) moi.delete(khoa);
      else moi.set(khoa, giaTri);
      if (khoa !== 'trang') moi.delete('trang');
      return moi;
    }, { replace: khoa !== 'trang' });
  }, [setParams]);

  useEffect(() => { setOTim(loc.tuKhoa); }, [loc.tuKhoa]);
  useEffect(() => {
    if (oTim.trim() === loc.tuKhoa) return undefined;
    const hen = setTimeout(() => datLoc('tuKhoa', oTim.trim()), 400);
    return () => clearTimeout(hen);
  }, [oTim, loc.tuKhoa, datLoc]);

  useEffect(() => {
    const luot = ++dem.current; // bỏ qua phản hồi cũ nếu đã có yêu cầu mới hơn
    setDangTai(true);
    const thamSo = { trang, kichThuoc: KICH_THUOC };
    LOC.forEach((k) => { if (loc[k]) thamSo[k] = loc[k]; });
    adminApi.donHang.timKiem(thamSo)
      .then((kq) => { if (luot === dem.current) { setKetQua(kq); setLoiTai(''); } })
      .catch((err) => { if (luot === dem.current) setLoiTai(layThongBaoLoi(err)); })
      .finally(() => { if (luot === dem.current) setDangTai(false); });
  }, [khoaLoc, trang]); // khoaLoc gom mọi bộ lọc thành một chuỗi

  const coLoc = LOC.some((k) => loc[k]);

  const cot = [
    { khoa: 'maDonHang', tieuDe: 'Mã đơn', hienThi: (d) => (
      <div className="admin-product-name">
        <Link to={`/admin/don-hang/${d.maDonHang}`}>{d.maDonHang}</Link>
        <small className="field-hint">{dinhDangThoiGian(d.ngayDat)}</small>
      </div>
    ) },
    { khoa: 'khach', tieuDe: 'Khách hàng', hienThi: (d) => (
      <div className="admin-product-name">
        <span>{d.tenNguoiNhan}{d.khachVangLai && <small className="field-hint"> · vãng lai</small>}</span>
        <small className="field-hint">{d.soDienThoai}</small>
      </div>
    ) },
    { khoa: 'soLuongSanPham', tieuDe: 'SL', canh: 'right' },
    { khoa: 'tongTien', tieuDe: 'Tổng tiền', canh: 'right', hienThi: (d) => <strong>{formatTien(d.tongTien)}</strong> },
    { khoa: 'thanhToan', tieuDe: 'Thanh toán', hienThi: (d) => (
      <div className="admin-stack">
        <Badge bang={NHAN_THANH_TOAN} giaTri={d.trangThaiThanhToan} />
        <small className="field-hint">{NHAN_PHUONG_THUC[d.phuongThucThanhToan] || d.phuongThucThanhToan}</small>
      </div>
    ) },
    { khoa: 'trangThai', tieuDe: 'Trạng thái', hienThi: (d) => <span className={`status status-${d.trangThai}`}>{NHAN_TRANG_THAI_DON[d.trangThai]}</span> },
    { khoa: 'thaoTac', tieuDe: '', canh: 'right', hienThi: (d) => <Link className="btn btn-outline btn-sm" to={`/admin/don-hang/${d.maDonHang}`}>Xem</Link> },
  ];

  return (
    <>
      <div className="admin-toolbar">
        <h1 className="admin-toolbar-grow">Đơn hàng</h1>
      </div>
      <div className="admin-toolbar admin-filters">
        <input type="search" className="admin-filter-search" placeholder="Mã đơn, tên, SĐT, email…" aria-label="Tìm đơn hàng" value={oTim} onChange={(e) => setOTim(e.target.value)} />
        <select aria-label="Lọc trạng thái đơn" value={loc.trangThai} onChange={(e) => datLoc('trangThai', e.target.value)}>
          <option value="">Mọi trạng thái</option>
          {Object.entries(NHAN_TRANG_THAI_DON).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
        </select>
        <select aria-label="Lọc thanh toán" value={loc.trangThaiThanhToan} onChange={(e) => datLoc('trangThaiThanhToan', e.target.value)}>
          <option value="">Mọi thanh toán</option>
          {Object.entries(NHAN_THANH_TOAN).map(([k, v]) => <option key={k} value={k}>{v.nhan}</option>)}
        </select>
        <select aria-label="Lọc phương thức" value={loc.phuongThucThanhToan} onChange={(e) => datLoc('phuongThucThanhToan', e.target.value)}>
          <option value="">Mọi phương thức</option>
          {Object.entries(NHAN_PHUONG_THUC).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
        </select>
      </div>
      <div className="admin-toolbar admin-filters">
        <label className="admin-inline-field">Từ ngày
          <input type="date" value={loc.tuNgay} max={loc.denNgay || undefined} onChange={(e) => datLoc('tuNgay', e.target.value)} />
        </label>
        <label className="admin-inline-field">Đến ngày
          <input type="date" value={loc.denNgay} min={loc.tuNgay || undefined} onChange={(e) => datLoc('denNgay', e.target.value)} />
        </label>
        {coLoc && <button type="button" className="btn btn-outline" onClick={() => setParams({}, { replace: true })}>Xóa bộ lọc</button>}
      </div>
      <ErrorMessage>{loiTai}</ErrorMessage>
      <DataTable cot={cot} duLieu={ketQua?.noiDung} dangTai={dangTai && !ketQua} khoaDong="maDonHang" trong={coLoc ? 'Không có đơn phù hợp' : 'Chưa có đơn hàng'} />
      {ketQua && (
        <div className="admin-toolbar admin-list-foot">
          <span className="field-hint admin-toolbar-grow">{ketQua.tongSoPhanTu} đơn hàng</span>
          <Pagination trang={ketQua.trang} tongSoTrang={ketQua.tongSoTrang} onChange={(t) => datLoc('trang', String(t))} />
        </div>
      )}
    </>
  );
}
