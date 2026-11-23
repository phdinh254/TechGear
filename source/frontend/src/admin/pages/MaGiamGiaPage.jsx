import { useCallback, useEffect, useRef, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import adminApi from '../api/adminApi.js';
import DataTable from '../components/DataTable.jsx';
import Modal from '../components/Modal.jsx';
import ConfirmDialog from '../components/ConfirmDialog.jsx';
import ErrorMessage from '../../components/ErrorMessage.jsx';
import Pagination from '../../components/Pagination.jsx';
import { useToast } from '../../context/ToastContext.jsx';
import { layThongBaoLoi } from '../../api/axiosClient.js';
import { formatTien } from '../../utils/format.js';
import { dinhDangThoiGian } from '../../components/OrderView.jsx';
import { sangONhap, tuONhap } from '../utils/thoiGian.js';

const KICH_THUOC = 20;

const FORM_TRONG = {
  ma: '', moTa: '', loaiGiam: 'phan_tram', giaTriGiam: '', donToiThieu: '', giamToiDa: '',
  tongLuotDung: '', luotDungMoiNguoi: '', batDauLuc: '', ketThucLuc: '', kichHoat: true,
};

/** Tình trạng hiện tại của mã: tắt / sắp áp dụng / đang áp dụng / hết hạn / hết lượt. */
function tinhTrang(m) {
  const bayGio = Date.now();
  if (!m.kichHoat) return { nhan: 'Đã tắt', lop: 'admin-badge--muted' };
  if (new Date(m.ketThucLuc).getTime() < bayGio) return { nhan: 'Hết hạn', lop: 'admin-badge--muted' };
  if (m.tongLuotDung != null && m.daDung >= m.tongLuotDung) return { nhan: 'Hết lượt', lop: 'admin-badge--danger' };
  if (new Date(m.batDauLuc).getTime() > bayGio) return { nhan: 'Sắp áp dụng', lop: 'admin-badge--warning' };
  return { nhan: 'Đang áp dụng', lop: 'admin-badge--success' };
}

const soHoacNull = (s) => (String(s).trim() === '' ? null : Number(s));

export default function MaGiamGiaPage() {
  const toast = useToast();
  const [params, setParams] = useSearchParams();
  const tuKhoa = params.get('tuKhoa') || '';
  const kichHoat = params.get('kichHoat') || '';
  const trang = Math.max(0, Number(params.get('trang')) || 0);

  const [oTim, setOTim] = useState(tuKhoa);
  const [ketQua, setKetQua] = useState(null);
  const [dangTai, setDangTai] = useState(true);
  const [loiTai, setLoiTai] = useState('');
  const [lanTai, setLanTai] = useState(0);
  const dem = useRef(0);

  const [dangSua, setDangSua] = useState(null);
  const [form, setForm] = useState(FORM_TRONG);
  const [loi, setLoi] = useState('');
  const [loiTheoO, setLoiTheoO] = useState({});
  const [dangLuu, setDangLuu] = useState(false);
  const [canXoa, setCanXoa] = useState(null);
  const [dangXoa, setDangXoa] = useState(false);

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
    adminApi.maGiamGia.timKiem({ tuKhoa: tuKhoa || undefined, kichHoat: kichHoat || undefined, trang, kichThuoc: KICH_THUOC })
      .then((kq) => { if (luot === dem.current) { setKetQua(kq); setLoiTai(''); } })
      .catch((err) => { if (luot === dem.current) setLoiTai(layThongBaoLoi(err)); })
      .finally(() => { if (luot === dem.current) setDangTai(false); });
  }, [tuKhoa, kichHoat, trang, lanTai]);

  const moForm = (m) => {
    setLoi('');
    setLoiTheoO({});
    if (m) {
      setForm({
        ma: m.ma, moTa: m.moTa || '', loaiGiam: m.loaiGiam, giaTriGiam: String(m.giaTriGiam),
        donToiThieu: m.donToiThieu ? String(m.donToiThieu) : '', giamToiDa: m.giamToiDa != null ? String(m.giamToiDa) : '',
        tongLuotDung: m.tongLuotDung != null ? String(m.tongLuotDung) : '', luotDungMoiNguoi: m.luotDungMoiNguoi != null ? String(m.luotDungMoiNguoi) : '',
        batDauLuc: sangONhap(m.batDauLuc), ketThucLuc: sangONhap(m.ketThucLuc), kichHoat: m.kichHoat,
      });
      setDangSua(m);
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
      ma: form.ma.trim().toUpperCase(),
      moTa: form.moTa.trim() || null,
      loaiGiam: form.loaiGiam,
      giaTriGiam: soHoacNull(form.giaTriGiam),
      donToiThieu: soHoacNull(form.donToiThieu),
      giamToiDa: form.loaiGiam === 'phan_tram' ? soHoacNull(form.giamToiDa) : null,
      tongLuotDung: soHoacNull(form.tongLuotDung),
      luotDungMoiNguoi: soHoacNull(form.luotDungMoiNguoi),
      batDauLuc: tuONhap(form.batDauLuc),
      ketThucLuc: tuONhap(form.ketThucLuc),
      kichHoat: form.kichHoat,
    };
    try {
      if (dangSua.id) await adminApi.maGiamGia.capNhat(dangSua.id, duLieu);
      else await adminApi.maGiamGia.tao(duLieu);
      toast.hien(dangSua.id ? 'Đã cập nhật mã giảm giá' : 'Đã thêm mã giảm giá');
      setDangSua(null);
      setLanTai((n) => n + 1);
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
      await adminApi.maGiamGia.xoa(canXoa.id);
      toast.hien('Đã xóa mã giảm giá');
      setLanTai((n) => n + 1);
    } catch (err) {
      toast.hien(layThongBaoLoi(err), { loai: 'error' });
    } finally {
      setCanXoa(null);
      setDangXoa(false);
    }
  };

  const cot = [
    { khoa: 'ma', tieuDe: 'Mã', hienThi: (m) => (
      <div className="admin-product-name">
        <strong className="admin-code">{m.ma}</strong>
        {m.moTa && <small className="field-hint">{m.moTa}</small>}
      </div>
    ) },
    { khoa: 'giam', tieuDe: 'Mức giảm', hienThi: (m) => (
      <div className="admin-product-name">
        <span>{m.loaiGiam === 'phan_tram' ? `${m.giaTriGiam}%` : formatTien(m.giaTriGiam)}{m.giamToiDa != null && <small className="field-hint"> · tối đa {formatTien(m.giamToiDa)}</small>}</span>
        {m.donToiThieu > 0 && <small className="field-hint">Đơn từ {formatTien(m.donToiThieu)}</small>}
      </div>
    ) },
    { khoa: 'luot', tieuDe: 'Đã dùng', canh: 'right', hienThi: (m) => (m.tongLuotDung != null ? `${m.daDung} / ${m.tongLuotDung}` : m.daDung) },
    { khoa: 'thoiGian', tieuDe: 'Thời gian', hienThi: (m) => (
      <div className="admin-product-name">
        <small>{dinhDangThoiGian(m.batDauLuc)}</small>
        <small className="field-hint">→ {dinhDangThoiGian(m.ketThucLuc)}</small>
      </div>
    ) },
    { khoa: 'tinhTrang', tieuDe: 'Tình trạng', hienThi: (m) => { const t = tinhTrang(m); return <span className={`admin-badge ${t.lop}`}>{t.nhan}</span>; } },
    { khoa: 'thaoTac', tieuDe: '', canh: 'right', hienThi: (m) => (
      <div className="admin-row-actions">
        <button type="button" className="btn btn-outline btn-sm" onClick={() => moForm(m)}>Sửa</button>
        <button type="button" className="btn btn-outline btn-sm" onClick={() => setCanXoa(m)}>Xóa</button>
      </div>
    ) },
  ];

  const loiO = (k) => loiTheoO[k] && <small className="field-error">{loiTheoO[k]}</small>;

  return (
    <>
      <div className="admin-toolbar">
        <h1 className="admin-toolbar-grow">Mã giảm giá</h1>
        <button type="button" className="btn btn-primary" onClick={() => moForm(null)}>Thêm mã</button>
      </div>
      <div className="admin-toolbar admin-filters">
        <input type="search" className="admin-filter-search" placeholder="Tìm mã…" aria-label="Tìm mã giảm giá" value={oTim} onChange={(e) => setOTim(e.target.value)} />
        <select aria-label="Lọc kích hoạt" value={kichHoat} onChange={(e) => datLoc('kichHoat', e.target.value)}>
          <option value="">Bật và tắt</option>
          <option value="true">Đang bật</option>
          <option value="false">Đã tắt</option>
        </select>
      </div>
      <ErrorMessage>{loiTai}</ErrorMessage>
      <DataTable cot={cot} duLieu={ketQua?.noiDung} dangTai={dangTai && !ketQua} trong="Chưa có mã giảm giá" />
      {ketQua && (
        <div className="admin-toolbar admin-list-foot">
          <span className="field-hint admin-toolbar-grow">{ketQua.tongSoPhanTu} mã</span>
          <Pagination trang={ketQua.trang} tongSoTrang={ketQua.tongSoTrang} onChange={(t) => datLoc('trang', String(t))} />
        </div>
      )}

      <Modal tieuDe={dangSua?.id ? `Sửa mã ${dangSua.ma}` : 'Thêm mã giảm giá'} mo={dangSua !== null} onClose={dongForm} rong>
        <form className="form" onSubmit={luu} noValidate>
          <ErrorMessage>{loi}</ErrorMessage>
          <div className="admin-form-grid">
            <label>Mã
              <input value={form.ma} onChange={(e) => dat('ma', e.target.value.toUpperCase())} maxLength={50} placeholder="VD: GIAM10" required />
              <small className="field-hint">Chữ in hoa, số, gạch dưới hoặc gạch ngang</small>
              {loiO('ma')}
            </label>
            <label>Mô tả
              <input value={form.moTa} onChange={(e) => dat('moTa', e.target.value)} maxLength={255} />
              {loiO('moTa')}
            </label>
            <label>Loại giảm
              <select value={form.loaiGiam} onChange={(e) => dat('loaiGiam', e.target.value)}>
                <option value="phan_tram">Theo phần trăm</option>
                <option value="so_tien">Số tiền cố định</option>
              </select>
            </label>
            <label>{form.loaiGiam === 'phan_tram' ? 'Phần trăm giảm (1–100)' : 'Số tiền giảm (đ)'}
              <input type="number" min={1} max={form.loaiGiam === 'phan_tram' ? 100 : undefined} value={form.giaTriGiam} onChange={(e) => dat('giaTriGiam', e.target.value)} required />
              {loiO('giaTriGiam')}
            </label>
            <label>Đơn tối thiểu (đ)
              <input type="number" min={0} value={form.donToiThieu} onChange={(e) => dat('donToiThieu', e.target.value)} placeholder="Không giới hạn" />
              {loiO('donToiThieu')}
            </label>
            {form.loaiGiam === 'phan_tram' ? (
              <label>Giảm tối đa (đ)
                <input type="number" min={1} value={form.giamToiDa} onChange={(e) => dat('giamToiDa', e.target.value)} placeholder="Không giới hạn" />
                {loiO('giamToiDa')}
              </label>
            ) : <div />}
            <label>Tổng lượt dùng
              <input type="number" min={1} value={form.tongLuotDung} onChange={(e) => dat('tongLuotDung', e.target.value)} placeholder="Không giới hạn" />
              {dangSua?.id && <small className="field-hint">Đã dùng {dangSua.daDung} lượt</small>}
              {loiO('tongLuotDung')}
            </label>
            <label>Lượt mỗi khách
              <input type="number" min={1} value={form.luotDungMoiNguoi} onChange={(e) => dat('luotDungMoiNguoi', e.target.value)} placeholder="Không giới hạn" />
              {loiO('luotDungMoiNguoi')}
            </label>
            <label>Bắt đầu
              <input type="datetime-local" value={form.batDauLuc} onChange={(e) => dat('batDauLuc', e.target.value)} required />
              {loiO('batDauLuc')}
            </label>
            <label>Kết thúc
              <input type="datetime-local" value={form.ketThucLuc} min={form.batDauLuc || undefined} onChange={(e) => dat('ketThucLuc', e.target.value)} required />
              {loiO('ketThucLuc')}
            </label>
          </div>
          <label className="checkbox-label">
            <input type="checkbox" checked={form.kichHoat} onChange={(e) => dat('kichHoat', e.target.checked)} /> Kích hoạt mã
          </label>
          <div className="modal-actions">
            <button type="button" className="btn btn-outline" onClick={dongForm} disabled={dangLuu}>Hủy</button>
            <button type="submit" className="btn btn-primary" disabled={dangLuu}>{dangLuu ? 'Đang lưu...' : 'Lưu'}</button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        mo={canXoa !== null}
        tieuDe="Xóa mã giảm giá"
        noiDung={`Xóa mã "${canXoa?.ma}"? Các đơn đã dùng mã này vẫn giữ nguyên.`}
        nguyHiem
        dangXuLy={dangXoa}
        onXacNhan={xoa}
        onHuy={() => setCanXoa(null)}
      />
    </>
  );
}
