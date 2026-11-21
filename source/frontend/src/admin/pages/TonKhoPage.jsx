import { useCallback, useEffect, useRef, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import adminApi from '../api/adminApi.js';
import DataTable from '../components/DataTable.jsx';
import StockDialog from '../components/StockDialog.jsx';
import ErrorMessage from '../../components/ErrorMessage.jsx';
import Pagination from '../../components/Pagination.jsx';
import { layThongBaoLoi } from '../../api/axiosClient.js';

const KICH_THUOC = 20;

const TRANG_THAI = [
  { giaTri: '', nhan: 'Tất cả trạng thái' },
  { giaTri: 'con_hang', nhan: 'Còn hàng' },
  { giaTri: 'sap_het', nhan: 'Sắp hết' },
  { giaTri: 'het_hang', nhan: 'Hết hàng' },
];

const NHAN = {
  con_hang: { nhan: 'Còn hàng', lop: 'admin-badge--success' },
  sap_het: { nhan: 'Sắp hết', lop: 'admin-badge--warning' },
  het_hang: { nhan: 'Hết hàng', lop: 'admin-badge--danger' },
};

export default function TonKhoPage() {
  const [params, setParams] = useSearchParams();
  const tuKhoa = params.get('tuKhoa') || '';
  const trangThai = params.get('trangThai') || '';
  const trang = Math.max(0, Number(params.get('trang')) || 0);

  const [oTim, setOTim] = useState(tuKhoa);
  const [ketQua, setKetQua] = useState(null);
  const [dangTai, setDangTai] = useState(true);
  const [loiTai, setLoiTai] = useState('');
  const [hop, setHop] = useState(null); // { che, bienThe }
  const dem = useRef(0);
  const [lanTai, setLanTai] = useState(0); // tăng để tải lại sau khi nhập/điều chỉnh

  const datLoc = useCallback((khoa, giaTri) => {
    setParams((p) => {
      const moi = new URLSearchParams(p);
      if (giaTri === '' || giaTri == null) moi.delete(khoa);
      else moi.set(khoa, giaTri);
      if (khoa !== 'trang') moi.delete('trang');
      return moi;
    }, { replace: khoa !== 'trang' });
  }, [setParams]);

  useEffect(() => { setOTim(tuKhoa); }, [tuKhoa]);
  useEffect(() => {
    if (oTim.trim() === tuKhoa) return undefined;
    const hen = setTimeout(() => datLoc('tuKhoa', oTim.trim()), 400);
    return () => clearTimeout(hen);
  }, [oTim, tuKhoa, datLoc]);

  useEffect(() => {
    const luot = ++dem.current; // bỏ qua phản hồi cũ nếu đã có yêu cầu mới hơn
    setDangTai(true);
    adminApi.tonKho.danhSach({
      tuKhoa: tuKhoa || undefined,
      trangThai: trangThai || undefined,
      trang,
      kichThuoc: KICH_THUOC,
    })
      .then((kq) => { if (luot === dem.current) { setKetQua(kq); setLoiTai(''); } })
      .catch((err) => { if (luot === dem.current) setLoiTai(layThongBaoLoi(err)); })
      .finally(() => { if (luot === dem.current) setDangTai(false); });
  }, [tuKhoa, trangThai, trang, lanTai]);

  // API chỉ trả { bienTheId, soLuongTon }: tải lại để trạng thái còn/sắp hết/hết hàng do backend tính
  const thayDong = () => {
    setLanTai((n) => n + 1);
    setHop(null);
  };

  const cot = [
    { khoa: 'tenSanPham', tieuDe: 'Sản phẩm', hienThi: (d) => (
      <div className="admin-product-name">
        <span>{d.tenSanPham}</span>
        {!d.hienThi && <small className="field-hint">Không còn bán</small>}
      </div>
    ) },
    { khoa: 'phanLoai', tieuDe: 'Phân loại' },
    { khoa: 'sku', tieuDe: 'SKU' },
    { khoa: 'soLuongTon', tieuDe: 'Tồn kho', canh: 'right' },
    { khoa: 'nguongCanhBao', tieuDe: 'Ngưỡng cảnh báo', canh: 'right' },
    { khoa: 'trangThai', tieuDe: 'Trạng thái', hienThi: (d) => {
      const n = NHAN[d.trangThai];
      return n ? <span className={`admin-badge ${n.lop}`}>{n.nhan}</span> : d.trangThai;
    } },
    { khoa: 'thaoTac', tieuDe: '', canh: 'right', hienThi: (d) => (
      <div className="admin-row-actions">
        <button type="button" className="btn btn-outline btn-sm" onClick={() => setHop({ che: 'nhap', bienThe: d })}>Nhập kho</button>
        <button type="button" className="btn btn-outline btn-sm" onClick={() => setHop({ che: 'dieuChinh', bienThe: d })}>Điều chỉnh</button>
        <button type="button" className="btn btn-outline btn-sm" onClick={() => setHop({ che: 'lichSu', bienThe: d })}>Lịch sử</button>
      </div>
    ) },
  ];

  return (
    <>
      <div className="admin-toolbar">
        <h1 className="admin-toolbar-grow">Tồn kho</h1>
      </div>
      <div className="admin-toolbar admin-filters">
        <input type="search" className="admin-filter-search" placeholder="Tìm tên hoặc SKU…" aria-label="Tìm tên hoặc SKU" value={oTim} onChange={(e) => setOTim(e.target.value)} />
        <select aria-label="Lọc trạng thái" value={trangThai} onChange={(e) => datLoc('trangThai', e.target.value)}>
          {TRANG_THAI.map((t) => <option key={t.giaTri} value={t.giaTri}>{t.nhan}</option>)}
        </select>
      </div>
      <ErrorMessage>{loiTai}</ErrorMessage>
      <DataTable cot={cot} duLieu={ketQua?.noiDung} dangTai={dangTai && !ketQua} khoaDong="bienTheId" trong="Không có biến thể phù hợp" />
      {ketQua && (
        <div className="admin-toolbar admin-list-foot">
          <span className="field-hint admin-toolbar-grow">{ketQua.tongSoPhanTu} biến thể</span>
          <Pagination trang={ketQua.trang} tongSoTrang={ketQua.tongSoTrang} onChange={(t) => datLoc('trang', String(t))} />
        </div>
      )}
      {hop && <StockDialog che={hop.che} bienThe={hop.bienThe} onClose={() => setHop(null)} onDone={thayDong} />}
    </>
  );
}
