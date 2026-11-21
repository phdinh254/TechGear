import { useCallback, useEffect, useState } from 'react';
import adminApi from '../api/adminApi.js';
import DataTable from '../components/DataTable.jsx';
import Modal from '../components/Modal.jsx';
import ConfirmDialog from '../components/ConfirmDialog.jsx';
import ImageUploader from '../components/ImageUploader.jsx';
import ErrorMessage from '../../components/ErrorMessage.jsx';
import { useToast } from '../../context/ToastContext.jsx';
import { layThongBaoLoi } from '../../api/axiosClient.js';
import { duongDanAnh } from '../../utils/format.js';

const FORM_TRONG = { ten: '', danhMucChaId: '', moTa: '', hinhAnh: null, thuTu: 0, hienThi: true };

export default function DanhMucPage() {
  const toast = useToast();
  const [cay, setCay] = useState([]);
  const [dangTai, setDangTai] = useState(true);
  const [loiTai, setLoiTai] = useState('');
  const [dangSua, setDangSua] = useState(null); // null = đóng, {} = tạo mới, {id,...} = sửa
  const [form, setForm] = useState(FORM_TRONG);
  const [loi, setLoi] = useState('');
  const [loiTheoO, setLoiTheoO] = useState({});
  const [dangLuu, setDangLuu] = useState(false);
  const [canXoa, setCanXoa] = useState(null);
  const [dangXoa, setDangXoa] = useState(false);

  const taiLai = useCallback(async () => {
    try {
      setCay(await adminApi.danhMuc.layCay());
      setLoiTai('');
    } catch (err) {
      setLoiTai(layThongBaoLoi(err));
    } finally {
      setDangTai(false);
    }
  }, []);

  useEffect(() => { taiLai(); }, [taiLai]);

  const dong = cay.flatMap((goc) => [
    { ...goc, cap: 0 },
    ...(goc.danhMucCons || []).map((con) => ({ ...con, cap: 1 })),
  ]);

  const moForm = (dm) => {
    setLoi('');
    setLoiTheoO({});
    if (dm) {
      setForm({ ten: dm.ten, danhMucChaId: dm.danhMucChaId ?? '', moTa: dm.moTa || '', hinhAnh: dm.hinhAnh || null, thuTu: dm.thuTu ?? 0, hienThi: dm.hienThi });
      setDangSua(dm);
    } else {
      setForm(FORM_TRONG);
      setDangSua({});
    }
  };
  const dongForm = () => { if (!dangLuu) setDangSua(null); };
  const dat = (khoa, giaTri) => setForm((f) => ({ ...f, [khoa]: giaTri }));

  const luu = async (e) => {
    e.preventDefault();
    setLoi('');
    setLoiTheoO({});
    setDangLuu(true);
    const duLieu = {
      ten: form.ten.trim(),
      danhMucChaId: form.danhMucChaId === '' ? null : Number(form.danhMucChaId),
      moTa: form.moTa.trim() || null,
      hinhAnh: form.hinhAnh,
      thuTu: form.thuTu === '' ? null : Number(form.thuTu),
      hienThi: form.hienThi,
    };
    try {
      if (dangSua.id) await adminApi.danhMuc.capNhat(dangSua.id, duLieu);
      else await adminApi.danhMuc.tao(duLieu);
      toast.hien(dangSua.id ? 'Đã cập nhật danh mục' : 'Đã thêm danh mục');
      setDangSua(null);
      await taiLai();
    } catch (err) {
      setLoi(layThongBaoLoi(err));
      setLoiTheoO(err.response?.data?.loi || {});
    } finally {
      setDangLuu(false);
    }
  };

  const xoa = async () => {
    setDangXoa(true);
    try {
      await adminApi.danhMuc.xoa(canXoa.id);
      toast.hien('Đã xóa danh mục');
      setCanXoa(null);
      await taiLai();
    } catch (err) {
      toast.hien(layThongBaoLoi(err), { loai: 'error' });
      setCanXoa(null);
    } finally {
      setDangXoa(false);
    }
  };

  const cot = [
    { khoa: 'hinhAnh', tieuDe: 'Ảnh', hienThi: (d) => (d.hinhAnh ? <img className="admin-thumb" src={duongDanAnh(d.hinhAnh)} alt="" /> : <span className="admin-thumb" />) },
    { khoa: 'ten', tieuDe: 'Tên', hienThi: (d) => <span className={d.cap ? 'admin-indent' : undefined}>{d.cap ? '↳ ' : ''}{d.ten}</span> },
    { khoa: 'slug', tieuDe: 'Slug' },
    { khoa: 'thuTu', tieuDe: 'Thứ tự', canh: 'right' },
    { khoa: 'soSanPham', tieuDe: 'Số sản phẩm', canh: 'right' },
    { khoa: 'nhanHienThi', tieuDe: 'Hiển thị', hienThi: (d) => <span className={`admin-badge ${d.hienThi ? 'admin-badge--success' : 'admin-badge--muted'}`}>{d.hienThi ? 'Hiển thị' : 'Ẩn'}</span> },
    { khoa: 'thaoTac', tieuDe: '', canh: 'right', hienThi: (d) => (
      <div className="admin-row-actions">
        <button type="button" className="btn btn-outline" onClick={() => moForm(d)}>Sửa</button>
        <button type="button" className="btn btn-outline" onClick={() => setCanXoa(d)}>Xóa</button>
      </div>
    ) },
  ];

  return (
    <>
      <h1>Danh mục</h1>
      <div className="admin-toolbar">
        <div className="admin-toolbar-grow" />
        <button type="button" className="btn btn-primary" onClick={() => moForm(null)}>Thêm danh mục</button>
      </div>
      <ErrorMessage>{loiTai}</ErrorMessage>
      <DataTable cot={cot} duLieu={dong} dangTai={dangTai} trong="Chưa có danh mục" khoaDong="id" />

      <Modal tieuDe={dangSua?.id ? 'Sửa danh mục' : 'Thêm danh mục'} mo={dangSua !== null} onClose={dongForm}>
        <form className="form" onSubmit={luu} noValidate>
          <ErrorMessage>{loi}</ErrorMessage>
          <label>Tên danh mục
            <input value={form.ten} onChange={(e) => dat('ten', e.target.value)} maxLength={100} required />
            {loiTheoO.ten && <small className="field-error">{loiTheoO.ten}</small>}
          </label>
          <label>Danh mục cha
            <select value={form.danhMucChaId} onChange={(e) => dat('danhMucChaId', e.target.value)}>
              <option value="">— Không có (cấp gốc) —</option>
              {cay.filter((g) => g.id !== dangSua?.id).map((g) => <option key={g.id} value={g.id}>{g.ten}</option>)}
            </select>
            {loiTheoO.danhMucChaId && <small className="field-error">{loiTheoO.danhMucChaId}</small>}
          </label>
          <label>Mô tả
            <textarea rows={3} value={form.moTa} onChange={(e) => dat('moTa', e.target.value)} />
            {loiTheoO.moTa && <small className="field-error">{loiTheoO.moTa}</small>}
          </label>
          <div className="field">Ảnh
            <ImageUploader thuMuc="danh-muc" giaTri={form.hinhAnh} onChange={(v) => dat('hinhAnh', v)} />
            {loiTheoO.hinhAnh && <small className="field-error">{loiTheoO.hinhAnh}</small>}
          </div>
          <label>Thứ tự
            <input type="number" min={0} value={form.thuTu} onChange={(e) => dat('thuTu', e.target.value)} />
            {loiTheoO.thuTu && <small className="field-error">{loiTheoO.thuTu}</small>}
          </label>
          <label className="checkbox-label">
            <input type="checkbox" checked={form.hienThi} onChange={(e) => dat('hienThi', e.target.checked)} /> Hiển thị trên website
          </label>
          <div className="modal-actions">
            <button type="button" className="btn btn-outline" onClick={dongForm} disabled={dangLuu}>Hủy</button>
            <button type="submit" className="btn btn-primary" disabled={dangLuu}>{dangLuu ? 'Đang lưu...' : 'Lưu'}</button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        mo={canXoa !== null}
        tieuDe="Xóa danh mục"
        noiDung={`Bạn có chắc muốn xóa danh mục "${canXoa?.ten}"?`}
        nguyHiem
        dangXuLy={dangXoa}
        onXacNhan={xoa}
        onHuy={() => setCanXoa(null)}
      />
    </>
  );
}
