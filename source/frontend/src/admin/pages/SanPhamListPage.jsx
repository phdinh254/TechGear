import { useCallback, useEffect, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import adminApi from '../api/adminApi.js';
import DataTable from '../components/DataTable.jsx';
import ConfirmDialog from '../components/ConfirmDialog.jsx';
import ErrorMessage from '../../components/ErrorMessage.jsx';
import Pagination from '../../components/Pagination.jsx';
import Price from '../../components/ui/Price.jsx';
import { useToast } from '../../context/ToastContext.jsx';
import { layThongBaoLoi } from '../../api/axiosClient.js';
import { duongDanAnh } from '../../utils/format.js';

const KICH_THUOC = 20;

const TRANG_THAI = [
  { giaTri: '', nhan: 'Tất cả trạng thái' },
  { giaTri: 'dang_ban', nhan: 'Đang bán' },
  { giaTri: 'dang_an', nhan: 'Đang ẩn' },
];

export default function SanPhamListPage() {
  const toast = useToast();
  const [params, setParams] = useSearchParams();
  const tuKhoa = params.get('tuKhoa') || '';
  const danhMucId = params.get('danhMucId') || '';
  const thuongHieuId = params.get('thuongHieuId') || '';
  const trangThai = params.get('trangThai') || '';
  const trang = Math.max(0, Number(params.get('trang')) || 0);

  const [oTim, setOTim] = useState(tuKhoa);
  const [ketQua, setKetQua] = useState(null);
  const [dangTai, setDangTai] = useState(true);
  const [loiTai, setLoiTai] = useState('');
  const [cayDanhMuc, setCayDanhMuc] = useState([]);
  const [thuongHieus, setThuongHieus] = useState([]);
  const [canXoa, setCanXoa] = useState(null);
  const [dangXoa, setDangXoa] = useState(false);

  /** Đổi một bộ lọc; mọi thay đổi lọc đưa về trang đầu. */
  const datLoc = useCallback((khoa, giaTri) => {
    setParams((p) => {
      const moi = new URLSearchParams(p);
      if (giaTri === '' || giaTri == null) moi.delete(khoa);
      else moi.set(khoa, giaTri);
      if (khoa !== 'trang') moi.delete('trang');
      return moi;
    }, { replace: khoa !== 'trang' });
  }, [setParams]);

  useEffect(() => {
    adminApi.danhMuc.layCay().then(setCayDanhMuc).catch(() => {});
    adminApi.thuongHieu.layTatCa().then(setThuongHieus).catch(() => {});
  }, []);

  // Ô tìm kiếm: debounce 400ms rồi mới ghi vào URL.
  useEffect(() => { setOTim(tuKhoa); }, [tuKhoa]);
  useEffect(() => {
    if (oTim.trim() === tuKhoa) return undefined;
    const hen = setTimeout(() => datLoc('tuKhoa', oTim.trim()), 400);
    return () => clearTimeout(hen);
  }, [oTim, tuKhoa, datLoc]);

  const dem = useRef(0);
  const taiLai = useCallback(async () => {
    const luot = ++dem.current; // bỏ qua phản hồi cũ nếu đã có yêu cầu mới hơn
    setDangTai(true);
    try {
      const kq = await adminApi.sanPham.timKiem({
        tuKhoa: tuKhoa || undefined,
        danhMucId: danhMucId || undefined,
        thuongHieuId: thuongHieuId || undefined,
        hienThi: { dang_ban: true, dang_an: false }[trangThai],
        trang,
        kichThuoc: KICH_THUOC,
      });
      if (luot !== dem.current) return;
      setKetQua(kq);
      setLoiTai('');
    } catch (err) {
      if (luot === dem.current) setLoiTai(layThongBaoLoi(err));
    } finally {
      if (luot === dem.current) setDangTai(false);
    }
  }, [tuKhoa, danhMucId, thuongHieuId, trangThai, trang]);

  useEffect(() => { taiLai(); }, [taiLai]);

  const suaDong = (spId, thayDoi) => setKetQua((kq) => kq && ({
    ...kq,
    noiDung: kq.noiDung.map((sp) => (sp.id === spId ? { ...sp, ...thayDoi } : sp)),
  }));

  const doiHienThi = async (sp) => {
    const cu = sp.hienThi;
    suaDong(sp.id, { hienThi: !cu });
    try {
      await adminApi.sanPham.doiHienThi(sp.id, !cu);
    } catch (err) {
      suaDong(sp.id, { hienThi: cu });
      toast.hien(layThongBaoLoi(err), { loai: 'error' });
    }
  };

  const xoa = async () => {
    setDangXoa(true);
    try {
      await adminApi.sanPham.xoa(canXoa.id);
      toast.hien('Đã xóa sản phẩm');
      setCanXoa(null);
      if (ketQua?.noiDung.length === 1 && trang > 0) datLoc('trang', String(trang - 1));
      else await taiLai();
    } catch (err) {
      toast.hien(layThongBaoLoi(err), { loai: 'error' });
      setCanXoa(null);
    } finally {
      setDangXoa(false);
    }
  };

  const cot = [
    { khoa: 'anhChinh', tieuDe: 'Ảnh', hienThi: (sp) => (sp.anhChinh ? <img className="admin-thumb" src={duongDanAnh(sp.anhChinh)} alt="" /> : <span className="admin-thumb" />) },
    { khoa: 'ten', tieuDe: 'Tên', hienThi: (sp) => (
      <div className="admin-product-name">
        <Link to={`/admin/san-pham/${sp.id}`}>{sp.ten}</Link>
        {sp.tenThuongHieu && <small className="field-hint">{sp.tenThuongHieu}</small>}
      </div>
    ) },
    { khoa: 'danhMucTen', tieuDe: 'Danh mục' },
    { khoa: 'gia', tieuDe: 'Giá bán', hienThi: (sp) => <Price giaBan={sp.giaBan} giaGoc={sp.giaGoc} size="sm" /> },
    { khoa: 'tongTon', tieuDe: 'Tổng tồn', canh: 'right', hienThi: (sp) => (
      <span className={sp.tongTon === 0 ? 'admin-badge admin-badge--danger' : undefined}>{sp.tongTon === 0 ? 'Hết hàng' : sp.tongTon}</span>
    ) },
    { khoa: 'soBienThe', tieuDe: 'Biến thể', canh: 'right' },
    { khoa: 'noiBat', tieuDe: 'Nổi bật', hienThi: (sp) => (sp.noiBat ? <span className="admin-badge admin-badge--warning">Nổi bật</span> : null) },
    { khoa: 'hienThi', tieuDe: 'Hiển thị', hienThi: (sp) => (
      <label className="admin-switch">
        <input type="checkbox" role="switch" checked={!!sp.hienThi} onChange={() => doiHienThi(sp)} aria-label={`Hiển thị ${sp.ten}`} />
        <span className="admin-switch-track" aria-hidden="true" />
      </label>
    ) },
    { khoa: 'thaoTac', tieuDe: '', canh: 'right', hienThi: (sp) => (
      <div className="admin-row-actions">
        <a className="btn btn-outline btn-sm" href={`/san-pham/${sp.slug}`} target="_blank" rel="noreferrer">Xem trên cửa hàng</a>
        <Link className="btn btn-outline btn-sm" to={`/admin/san-pham/${sp.id}`}>Sửa</Link>
        <button type="button" className="btn btn-outline btn-sm" onClick={() => setCanXoa(sp)}>Xóa</button>
      </div>
    ) },
  ];

  return (
    <>
      <div className="admin-toolbar">
        <h1 className="admin-toolbar-grow">Sản phẩm</h1>
        <Link className="btn btn-primary" to="/admin/san-pham/moi">+ Thêm sản phẩm</Link>
      </div>
      <div className="admin-toolbar admin-filters">
        <input type="search" className="admin-filter-search" placeholder="Tìm tên hoặc SKU…" aria-label="Tìm tên hoặc SKU" value={oTim} onChange={(e) => setOTim(e.target.value)} />
        <select aria-label="Lọc danh mục" value={danhMucId} onChange={(e) => datLoc('danhMucId', e.target.value)}>
          <option value="">Tất cả danh mục</option>
          {cayDanhMuc.flatMap((goc) => [
            <option key={goc.id} value={goc.id}>{goc.ten}</option>,
            ...(goc.danhMucCons || []).map((con) => <option key={con.id} value={con.id}>{`    ${con.ten}`}</option>),
          ])}
        </select>
        <select aria-label="Lọc thương hiệu" value={thuongHieuId} onChange={(e) => datLoc('thuongHieuId', e.target.value)}>
          <option value="">Tất cả thương hiệu</option>
          {thuongHieus.map((th) => <option key={th.id} value={th.id}>{th.ten}</option>)}
        </select>
        <select aria-label="Lọc trạng thái" value={trangThai} onChange={(e) => datLoc('trangThai', e.target.value)}>
          {TRANG_THAI.map((t) => <option key={t.giaTri} value={t.giaTri}>{t.nhan}</option>)}
        </select>
      </div>
      <ErrorMessage>{loiTai}</ErrorMessage>
      <DataTable cot={cot} duLieu={ketQua?.noiDung} dangTai={dangTai && !ketQua} trong="Không có sản phẩm phù hợp" />
      {ketQua && (
        <div className="admin-toolbar admin-list-foot">
          <span className="field-hint admin-toolbar-grow">{ketQua.tongSoPhanTu} sản phẩm</span>
          <Pagination trang={ketQua.trang} tongSoTrang={ketQua.tongSoTrang} onChange={(t) => datLoc('trang', String(t))} />
        </div>
      )}

      <ConfirmDialog
        mo={canXoa !== null}
        tieuDe="Xóa sản phẩm"
        noiDung={`Bạn có chắc muốn xóa sản phẩm "${canXoa?.ten}"? Sản phẩm sẽ bị gỡ khỏi cửa hàng và giỏ hàng của khách.`}
        nguyHiem
        dangXuLy={dangXoa}
        onXacNhan={xoa}
        onHuy={() => setCanXoa(null)}
      />
    </>
  );
}
