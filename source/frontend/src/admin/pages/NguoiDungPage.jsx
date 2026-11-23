import { useCallback, useEffect, useRef, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import adminApi from '../api/adminApi.js';
import DataTable from '../components/DataTable.jsx';
import ConfirmDialog from '../components/ConfirmDialog.jsx';
import ErrorMessage from '../../components/ErrorMessage.jsx';
import Pagination from '../../components/Pagination.jsx';
import { useAuth } from '../../context/AuthContext.jsx';
import { useToast } from '../../context/ToastContext.jsx';
import { layThongBaoLoi } from '../../api/axiosClient.js';
import { Badge, NHAN_NGUOI_DUNG, NHAN_VAI_TRO } from '../utils/nhan.jsx';

const KICH_THUOC = 20;

export default function NguoiDungPage() {
  const { nguoiDung: toi } = useAuth();
  const toast = useToast();
  const [params, setParams] = useSearchParams();
  const tuKhoa = params.get('tuKhoa') || '';
  const vaiTro = params.get('vaiTro') || '';
  const trangThai = params.get('trangThai') || '';
  const trang = Math.max(0, Number(params.get('trang')) || 0);

  const [oTim, setOTim] = useState(tuKhoa);
  const [ketQua, setKetQua] = useState(null);
  const [dangTai, setDangTai] = useState(true);
  const [loiTai, setLoiTai] = useState('');
  const [canDoi, setCanDoi] = useState(null);
  const [dangDoi, setDangDoi] = useState(false);
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

  useEffect(() => { setOTim(tuKhoa); }, [tuKhoa]);
  useEffect(() => {
    if (oTim.trim() === tuKhoa) return undefined;
    const hen = setTimeout(() => datLoc('tuKhoa', oTim.trim()), 400);
    return () => clearTimeout(hen);
  }, [oTim, tuKhoa, datLoc]);

  useEffect(() => {
    const luot = ++dem.current;
    setDangTai(true);
    adminApi.nguoiDung.timKiem({ tuKhoa: tuKhoa || undefined, vaiTro: vaiTro || undefined, trangThai: trangThai || undefined, trang, kichThuoc: KICH_THUOC })
      .then((kq) => { if (luot === dem.current) { setKetQua(kq); setLoiTai(''); } })
      .catch((err) => { if (luot === dem.current) setLoiTai(layThongBaoLoi(err)); })
      .finally(() => { if (luot === dem.current) setDangTai(false); });
  }, [tuKhoa, vaiTro, trangThai, trang]);

  const doiTrangThai = async () => {
    const moi = canDoi.trangThai === 'bi_khoa' ? 'hoat_dong' : 'bi_khoa';
    setDangDoi(true);
    try {
      const dong = await adminApi.nguoiDung.doiTrangThai(canDoi.id, moi);
      setKetQua((kq) => ({ ...kq, noiDung: kq.noiDung.map((u) => (u.id === dong.id ? dong : u)) }));
      toast.hien(moi === 'bi_khoa' ? `Đã khóa tài khoản ${dong.email}` : `Đã mở khóa tài khoản ${dong.email}`);
    } catch (err) {
      toast.hien(layThongBaoLoi(err), { loai: 'error' });
    } finally {
      setCanDoi(null);
      setDangDoi(false);
    }
  };

  const cot = [
    { khoa: 'hoTen', tieuDe: 'Người dùng', hienThi: (u) => (
      <div className="admin-product-name">
        <span>{u.hoTen}{u.id === toi?.id && <small className="field-hint"> (bạn)</small>}</span>
        <small className="field-hint">{u.email}</small>
      </div>
    ) },
    { khoa: 'soDienThoai', tieuDe: 'Điện thoại', hienThi: (u) => u.soDienThoai || '—' },
    { khoa: 'vaiTro', tieuDe: 'Vai trò', hienThi: (u) => NHAN_VAI_TRO[u.vaiTro] || u.vaiTro },
    { khoa: 'soDonHang', tieuDe: 'Số đơn', canh: 'right' },
    { khoa: 'ngayTao', tieuDe: 'Ngày tạo', hienThi: (u) => new Date(u.ngayTao).toLocaleDateString('vi-VN') },
    { khoa: 'trangThai', tieuDe: 'Trạng thái', hienThi: (u) => <Badge bang={NHAN_NGUOI_DUNG} giaTri={u.trangThai} /> },
    { khoa: 'thaoTac', tieuDe: '', canh: 'right', hienThi: (u) => (u.id === toi?.id ? null : (
      <button type="button" className={`btn btn-outline btn-sm${u.trangThai === 'bi_khoa' ? '' : ' admin-btn-danger'}`} onClick={() => setCanDoi(u)}>
        {u.trangThai === 'bi_khoa' ? 'Mở khóa' : 'Khóa'}
      </button>
    )) },
  ];

  const khoa = canDoi?.trangThai !== 'bi_khoa';

  return (
    <>
      <div className="admin-toolbar">
        <h1 className="admin-toolbar-grow">Người dùng</h1>
      </div>
      <div className="admin-toolbar admin-filters">
        <input type="search" className="admin-filter-search" placeholder="Tên, email, số điện thoại…" aria-label="Tìm người dùng" value={oTim} onChange={(e) => setOTim(e.target.value)} />
        <select aria-label="Lọc vai trò" value={vaiTro} onChange={(e) => datLoc('vaiTro', e.target.value)}>
          <option value="">Mọi vai trò</option>
          {Object.entries(NHAN_VAI_TRO).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
        </select>
        <select aria-label="Lọc trạng thái" value={trangThai} onChange={(e) => datLoc('trangThai', e.target.value)}>
          <option value="">Mọi trạng thái</option>
          {Object.entries(NHAN_NGUOI_DUNG).map(([k, v]) => <option key={k} value={k}>{v.nhan}</option>)}
        </select>
      </div>
      <ErrorMessage>{loiTai}</ErrorMessage>
      <DataTable cot={cot} duLieu={ketQua?.noiDung} dangTai={dangTai && !ketQua} trong="Không có người dùng phù hợp" />
      {ketQua && (
        <div className="admin-toolbar admin-list-foot">
          <span className="field-hint admin-toolbar-grow">{ketQua.tongSoPhanTu} người dùng</span>
          <Pagination trang={ketQua.trang} tongSoTrang={ketQua.tongSoTrang} onChange={(t) => datLoc('trang', String(t))} />
        </div>
      )}

      <ConfirmDialog
        mo={canDoi !== null}
        tieuDe={khoa ? 'Khóa tài khoản' : 'Mở khóa tài khoản'}
        noiDung={khoa
          ? `Khóa "${canDoi?.email}"? Người này bị đăng xuất ngay và không đăng nhập lại được cho đến khi mở khóa.`
          : `Mở khóa "${canDoi?.email}"? Người này đăng nhập lại được bình thường.`}
        nhanXacNhan={khoa ? 'Khóa' : 'Mở khóa'}
        nguyHiem={khoa}
        dangXuLy={dangDoi}
        onXacNhan={doiTrangThai}
        onHuy={() => setCanDoi(null)}
      />
    </>
  );
}
