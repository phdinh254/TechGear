import { useEffect, useRef, useState } from 'react';
import adminApi from '../api/adminApi.js';
import Modal from './Modal.jsx';
import DataTable from './DataTable.jsx';
import ErrorMessage from '../../components/ErrorMessage.jsx';
import Pagination from '../../components/Pagination.jsx';
import { useToast } from '../../context/ToastContext.jsx';
import { layThongBaoLoi } from '../../api/axiosClient.js';

const LOAI = {
  khoi_tao: 'Khởi tạo',
  nhap_kho: 'Nhập kho',
  ban_hang: 'Bán hàng',
  huy_don: 'Hủy đơn',
  dieu_chinh: 'Điều chỉnh',
};

const dau = (n) => (n > 0 ? `+${n}` : n < 0 ? `−${Math.abs(n)}` : '0');

const thoiGianHienThi = (iso) => {
  if (!iso) return '';
  const d = new Date(iso);
  return Number.isNaN(d.getTime()) ? iso : d.toLocaleString('vi-VN');
};

function LichSu({ bienThe }) {
  const [trang, setTrang] = useState(0);
  const [ketQua, setKetQua] = useState(null);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');
  const dem = useRef(0);

  useEffect(() => {
    const luot = ++dem.current;
    setDangTai(true);
    adminApi.tonKho.lichSu(bienThe.bienTheId, trang)
      .then((kq) => { if (luot === dem.current) { setKetQua(kq); setLoi(''); } })
      .catch((err) => { if (luot === dem.current) setLoi(layThongBaoLoi(err)); })
      .finally(() => { if (luot === dem.current) setDangTai(false); });
  }, [bienThe.bienTheId, trang]);

  const cot = [
    { khoa: 'thoiGian', tieuDe: 'Thời gian', hienThi: (d) => thoiGianHienThi(d.thoiGian) },
    { khoa: 'loai', tieuDe: 'Loại', hienThi: (d) => LOAI[d.loai] || d.loai },
    { khoa: 'soLuongThayDoi', tieuDe: 'Thay đổi', canh: 'right', hienThi: (d) => dau(d.soLuongThayDoi) },
    { khoa: 'soLuongSau', tieuDe: 'Tồn sau', canh: 'right' },
    { khoa: 'ghiChu', tieuDe: 'Ghi chú', hienThi: (d) => d.ghiChu || '' },
    { khoa: 'nguoiThucHien', tieuDe: 'Người thực hiện / Đơn hàng', hienThi: (d) => d.maDonHang || d.nguoiThucHien || '' },
  ];

  return (
    <>
      <ErrorMessage>{loi}</ErrorMessage>
      <DataTable cot={cot} duLieu={ketQua?.noiDung} dangTai={dangTai && !ketQua} trong="Chưa có lịch sử" />
      {ketQua && ketQua.tongSoTrang > 1 && (
        <div className="admin-toolbar admin-list-foot">
          <Pagination trang={ketQua.trang} tongSoTrang={ketQua.tongSoTrang} onChange={setTrang} />
        </div>
      )}
    </>
  );
}

export default function StockDialog({ che, bienThe, onClose, onDone }) {
  const toast = useToast();
  const [soLuong, setSoLuong] = useState('');
  const [ghiChu, setGhiChu] = useState('');
  const [lyDo, setLyDo] = useState('');
  const [loi, setLoi] = useState('');
  const [loiTheoO, setLoiTheoO] = useState({});
  const [dangLuu, setDangLuu] = useState(false);

  const tenBienThe = `${bienThe.tenSanPham}${bienThe.phanLoai ? ` — ${bienThe.phanLoai}` : ''}`;
  const nhap = che === 'nhap';
  const thucTe = soLuong === '' ? null : Number(soLuong);
  const chenhLech = thucTe === null || Number.isNaN(thucTe) ? null : thucTe - bienThe.soLuongTon;

  const dongHop = () => { if (!dangLuu) onClose(); };

  const gui = async (e) => {
    e.preventDefault();
    setLoi('');
    setLoiTheoO({});
    setDangLuu(true);
    try {
      const capNhat = nhap
        ? await adminApi.tonKho.nhapKho(bienThe.bienTheId, { soLuong: soLuong === '' ? null : Number(soLuong), ghiChu: ghiChu.trim() || null })
        : await adminApi.tonKho.dieuChinh(bienThe.bienTheId, { soLuongMoi: soLuong === '' ? null : Number(soLuong), ghiChu: lyDo.trim() });
      toast.hien(nhap ? 'Đã nhập kho' : 'Đã điều chỉnh tồn kho');
      onDone(capNhat);
    } catch (err) {
      setLoi(layThongBaoLoi(err));
      setLoiTheoO(err.response?.data?.loi || {});
      setDangLuu(false);
    }
  };

  const tieuDe = { nhap: 'Nhập kho', dieuChinh: 'Điều chỉnh tồn kho', lichSu: 'Lịch sử tồn kho' }[che];

  return (
    <Modal tieuDe={tieuDe} mo onClose={dongHop} rong={che === 'lichSu'}>
      <p className="field-hint">{tenBienThe}{bienThe.sku ? ` (${bienThe.sku})` : ''} — tồn hiện tại: <strong>{bienThe.soLuongTon}</strong></p>
      {che === 'lichSu' ? (
        <LichSu bienThe={bienThe} />
      ) : (
        <form className="form" onSubmit={gui} noValidate>
          <ErrorMessage>{loi}</ErrorMessage>
          {nhap ? (
            <>
              <label>Số lượng nhập
                <input type="number" min={1} step={1} value={soLuong} onChange={(e) => setSoLuong(e.target.value)} required autoFocus />
                {loiTheoO.soLuong && <small className="field-error">{loiTheoO.soLuong}</small>}
              </label>
              <label>Ghi chú
                <textarea rows={3} value={ghiChu} onChange={(e) => setGhiChu(e.target.value)} />
                {loiTheoO.ghiChu && <small className="field-error">{loiTheoO.ghiChu}</small>}
              </label>
            </>
          ) : (
            <>
              <label>Số lượng thực tế
                <input type="number" min={0} step={1} value={soLuong} onChange={(e) => setSoLuong(e.target.value)} required autoFocus />
                {loiTheoO.soLuongMoi && <small className="field-error">{loiTheoO.soLuongMoi}</small>}
              </label>
              {chenhLech !== null && <p className="field-hint">Chênh lệch: <strong>{dau(chenhLech)}</strong></p>}
              <label>Lý do
                <textarea rows={3} value={lyDo} onChange={(e) => setLyDo(e.target.value)} required />
                {loiTheoO.ghiChu && <small className="field-error">{loiTheoO.ghiChu}</small>}
              </label>
            </>
          )}
          <div className="modal-actions">
            <button type="button" className="btn btn-outline" onClick={dongHop} disabled={dangLuu}>Hủy</button>
            <button type="submit" className="btn btn-primary" disabled={dangLuu}>{dangLuu ? 'Đang lưu...' : 'Xác nhận'}</button>
          </div>
        </form>
      )}
    </Modal>
  );
}
