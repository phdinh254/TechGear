import { useCallback, useEffect, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import adminApi from '../api/adminApi.js';
import DataTable from '../components/DataTable.jsx';
import ConfirmDialog from '../components/ConfirmDialog.jsx';
import ErrorMessage from '../../components/ErrorMessage.jsx';
import Pagination from '../../components/Pagination.jsx';
import { useToast } from '../../context/ToastContext.jsx';
import { layThongBaoLoi } from '../../api/axiosClient.js';
import { dinhDangThoiGian } from '../../components/OrderView.jsx';
import { Badge, NHAN_DANH_GIA } from '../utils/nhan.jsx';

const KICH_THUOC = 20;
const TAB = [{ giaTri: '', nhan: 'Tất cả' }, ...Object.entries(NHAN_DANH_GIA).map(([k, v]) => ({ giaTri: k, nhan: v.nhan }))];

function Sao({ so }) {
  return (
    <span className="admin-stars" aria-label={`${so} sao`}>
      {'★'.repeat(so)}<span aria-hidden="true">{'★'.repeat(5 - so)}</span>
    </span>
  );
}

export default function DanhGiaPage() {
  const toast = useToast();
  const [params, setParams] = useSearchParams();
  // Mặc định mở tab "Chờ duyệt" vì đó là việc cần làm
  const trangThai = params.has('trangThai') ? params.get('trangThai') : 'cho_duyet';
  const soSao = params.get('soSao') || '';
  const trang = Math.max(0, Number(params.get('trang')) || 0);

  const [ketQua, setKetQua] = useState(null);
  const [dangTai, setDangTai] = useState(true);
  const [loiTai, setLoiTai] = useState('');
  const [lanTai, setLanTai] = useState(0);
  const [dangDoi, setDangDoi] = useState(null);
  const [canXoa, setCanXoa] = useState(null);
  const [dangXoa, setDangXoa] = useState(false);
  const dem = useRef(0);

  const datLoc = useCallback((khoa, giaTri) => {
    setParams((p) => {
      const moi = new URLSearchParams(p);
      if (giaTri == null || (giaTri === '' && khoa !== 'trangThai')) moi.delete(khoa);
      else moi.set(khoa, giaTri);
      if (khoa !== 'trang') moi.delete('trang');
      return moi;
    }, { replace: khoa !== 'trang' });
  }, [setParams]);

  useEffect(() => {
    const luot = ++dem.current;
    setDangTai(true);
    adminApi.danhGia.timKiem({ trangThai: trangThai || undefined, soSao: soSao || undefined, trang, kichThuoc: KICH_THUOC })
      .then((kq) => { if (luot === dem.current) { setKetQua(kq); setLoiTai(''); } })
      .catch((err) => { if (luot === dem.current) setLoiTai(layThongBaoLoi(err)); })
      .finally(() => { if (luot === dem.current) setDangTai(false); });
  }, [trangThai, soSao, trang, lanTai]);

  const doiTrangThai = async (dg, moi) => {
    setDangDoi(dg.id);
    try {
      await adminApi.danhGia.doiTrangThai(dg.id, moi);
      toast.hien(moi === 'da_duyet' ? 'Đã duyệt, đánh giá hiện trên trang sản phẩm' : moi === 'da_an' ? 'Đã ẩn đánh giá' : 'Đã chuyển về chờ duyệt');
      setLanTai((n) => n + 1); // tải lại vì đánh giá có thể rời khỏi tab đang lọc
    } catch (err) {
      toast.hien(layThongBaoLoi(err), { loai: 'error' });
    } finally {
      setDangDoi(null);
    }
  };

  const xoa = async () => {
    setDangXoa(true);
    try {
      await adminApi.danhGia.xoa(canXoa.id);
      toast.hien('Đã xóa đánh giá');
      setLanTai((n) => n + 1);
    } catch (err) {
      toast.hien(layThongBaoLoi(err), { loai: 'error' });
    } finally {
      setCanXoa(null);
      setDangXoa(false);
    }
  };

  const cot = [
    { khoa: 'noiDung', tieuDe: 'Đánh giá', hienThi: (d) => (
      <div className="admin-review">
        <Sao so={d.soSao} />
        {d.noiDung ? <p>{d.noiDung}</p> : <p className="field-hint">(Không có nội dung)</p>}
        <small className="field-hint">{dinhDangThoiGian(d.ngayTao)}</small>
      </div>
    ) },
    { khoa: 'tenSanPham', tieuDe: 'Sản phẩm', hienThi: (d) => (
      <div className="admin-product-name">
        <span>{d.tenSanPham}</span>
        <small className="field-hint">Đơn <Link to={`/admin/don-hang/${d.maDonHang}`}>{d.maDonHang}</Link></small>
      </div>
    ) },
    { khoa: 'hoTen', tieuDe: 'Khách', hienThi: (d) => (
      <div className="admin-product-name"><span>{d.hoTen}</span><small className="field-hint">{d.email}</small></div>
    ) },
    { khoa: 'trangThai', tieuDe: 'Trạng thái', hienThi: (d) => <Badge bang={NHAN_DANH_GIA} giaTri={d.trangThai} /> },
    { khoa: 'thaoTac', tieuDe: '', canh: 'right', hienThi: (d) => (
      <div className="admin-row-actions">
        {d.trangThai !== 'da_duyet' && <button type="button" className="btn btn-primary btn-sm" disabled={dangDoi === d.id} onClick={() => doiTrangThai(d, 'da_duyet')}>Duyệt</button>}
        {d.trangThai !== 'da_an' && <button type="button" className="btn btn-outline btn-sm" disabled={dangDoi === d.id} onClick={() => doiTrangThai(d, 'da_an')}>Ẩn</button>}
        <button type="button" className="btn btn-outline btn-sm" onClick={() => setCanXoa(d)}>Xóa</button>
      </div>
    ) },
  ];

  return (
    <>
      <div className="admin-toolbar">
        <h1 className="admin-toolbar-grow">Đánh giá</h1>
      </div>
      <div className="admin-toolbar admin-filters">
        <div className="admin-tabs" role="tablist" aria-label="Lọc trạng thái đánh giá">
          {TAB.map((t) => (
            <button key={t.giaTri} type="button" role="tab" aria-selected={trangThai === t.giaTri} className={trangThai === t.giaTri ? 'is-active' : undefined} onClick={() => datLoc('trangThai', t.giaTri)}>
              {t.nhan}
            </button>
          ))}
        </div>
        <div className="admin-toolbar-grow" />
        <select aria-label="Lọc số sao" value={soSao} onChange={(e) => datLoc('soSao', e.target.value)}>
          <option value="">Mọi số sao</option>
          {[5, 4, 3, 2, 1].map((s) => <option key={s} value={s}>{s} sao</option>)}
        </select>
      </div>
      <ErrorMessage>{loiTai}</ErrorMessage>
      <DataTable cot={cot} duLieu={ketQua?.noiDung} dangTai={dangTai && !ketQua} trong={trangThai === 'cho_duyet' ? 'Không có đánh giá nào chờ duyệt' : 'Không có đánh giá phù hợp'} />
      {ketQua && (
        <div className="admin-toolbar admin-list-foot">
          <span className="field-hint admin-toolbar-grow">{ketQua.tongSoPhanTu} đánh giá</span>
          <Pagination trang={ketQua.trang} tongSoTrang={ketQua.tongSoTrang} onChange={(t) => datLoc('trang', String(t))} />
        </div>
      )}

      <ConfirmDialog
        mo={canXoa !== null}
        tieuDe="Xóa đánh giá"
        noiDung={`Xóa hẳn đánh giá ${canXoa?.soSao} sao của ${canXoa?.hoTen}? Khách sẽ viết lại được đánh giá cho sản phẩm này. Muốn giấu đi mà giữ lại thì dùng "Ẩn".`}
        nguyHiem
        dangXuLy={dangXoa}
        onXacNhan={xoa}
        onHuy={() => setCanXoa(null)}
      />
    </>
  );
}
