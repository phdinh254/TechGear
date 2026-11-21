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

const FORM_TRONG = { ten: '', logo: null, moTa: '', hienThi: true };

export default function ThuongHieuPage() {
  const toast = useToast();
  const [ds, setDs] = useState([]);
  const [dangTai, setDangTai] = useState(true);
  const [loiTai, setLoiTai] = useState('');
  const [dangSua, setDangSua] = useState(null);
  const [form, setForm] = useState(FORM_TRONG);
  const [loi, setLoi] = useState('');
  const [loiTheoO, setLoiTheoO] = useState({});
  const [dangLuu, setDangLuu] = useState(false);
  const [canXoa, setCanXoa] = useState(null);
  const [dangXoa, setDangXoa] = useState(false);

  const taiLai = useCallback(async () => {
    try {
      setDs(await adminApi.thuongHieu.layTatCa());
      setLoiTai('');
    } catch (err) {
      setLoiTai(layThongBaoLoi(err));
    } finally {
      setDangTai(false);
    }
  }, []);

  useEffect(() => { taiLai(); }, [taiLai]);

  const moForm = (th) => {
    setLoi('');
    setLoiTheoO({});
    if (th) {
      setForm({ ten: th.ten, logo: th.logo || null, moTa: th.moTa || '', hienThi: th.hienThi });
      setDangSua(th);
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
    const duLieu = { ten: form.ten.trim(), logo: form.logo, moTa: form.moTa.trim() || null, hienThi: form.hienThi };
    try {
      if (dangSua.id) await adminApi.thuongHieu.capNhat(dangSua.id, duLieu);
      else await adminApi.thuongHieu.tao(duLieu);
      toast.hien(dangSua.id ? 'Đã cập nhật thương hiệu' : 'Đã thêm thương hiệu');
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
      await adminApi.thuongHieu.xoa(canXoa.id);
      toast.hien('Đã xóa thương hiệu');
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
    { khoa: 'logo', tieuDe: 'Logo', hienThi: (d) => (d.logo ? <img className="admin-thumb" src={duongDanAnh(d.logo)} alt="" /> : <span className="admin-thumb" />) },
    { khoa: 'ten', tieuDe: 'Tên' },
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
      <h1>Thương hiệu</h1>
      <div className="admin-toolbar">
        <div className="admin-toolbar-grow" />
        <button type="button" className="btn btn-primary" onClick={() => moForm(null)}>Thêm thương hiệu</button>
      </div>
      <ErrorMessage>{loiTai}</ErrorMessage>
      <DataTable cot={cot} duLieu={ds} dangTai={dangTai} trong="Chưa có thương hiệu" khoaDong="id" />

      <Modal tieuDe={dangSua?.id ? 'Sửa thương hiệu' : 'Thêm thương hiệu'} mo={dangSua !== null} onClose={dongForm}>
        <form className="form" onSubmit={luu} noValidate>
          <ErrorMessage>{loi}</ErrorMessage>
          <label>Tên thương hiệu
            <input value={form.ten} onChange={(e) => dat('ten', e.target.value)} maxLength={100} required />
            {loiTheoO.ten && <small className="field-error">{loiTheoO.ten}</small>}
          </label>
          <div className="field">Logo
            <ImageUploader thuMuc="thuong-hieu" giaTri={form.logo} onChange={(v) => dat('logo', v)} />
            {loiTheoO.logo && <small className="field-error">{loiTheoO.logo}</small>}
          </div>
          <label>Mô tả
            <textarea rows={3} value={form.moTa} onChange={(e) => dat('moTa', e.target.value)} />
            {loiTheoO.moTa && <small className="field-error">{loiTheoO.moTa}</small>}
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
        tieuDe="Xóa thương hiệu"
        noiDung={`Bạn có chắc muốn xóa thương hiệu "${canXoa?.ten}"?`}
        nguyHiem
        dangXuLy={dangXoa}
        onXacNhan={xoa}
        onHuy={() => setCanXoa(null)}
      />
    </>
  );
}
