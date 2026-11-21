import { useEffect, useMemo, useRef, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import adminApi from '../api/adminApi.js';
import ConfirmDialog from '../components/ConfirmDialog.jsx';
import ProductImages from '../components/ProductImages.jsx';
import VariantEditor, { loiSku, loiTuyChon } from '../components/VariantEditor.jsx';
import { ghepBienThe, sinhToHop, skuTrung } from '../utils/bienThe.js';
import luuSanPham, { tuPhanHoi } from '../utils/luuSanPham.js';
import ErrorMessage from '../../components/ErrorMessage.jsx';
import Loading from '../../components/Loading.jsx';
import { useToast } from '../../context/ToastContext.jsx';
import { layThongBaoLoi } from '../../api/axiosClient.js';

const FORM_TRONG = {
  ten: '', danhMucId: '', thuongHieuId: '', moTaNgan: '', moTa: '',
  giaBan: '', giaGoc: '', baoHanhThang: 12, noiBat: false, hienThi: true,
};

/** Các ô có chỗ hiện lỗi riêng; lỗi khác từ backend (vd bienThes[0].sku) hiện thành danh sách ở đầu form. */
const O_CO_LOI_RIENG = ['ten', 'danhMucId', 'thuongHieuId', 'moTaNgan', 'moTa', 'giaBan', 'giaGoc', 'baoHanhThang', 'hinhAnhs', 'thongSos', 'tuyChons', 'bienThes'];

const trangThaiMoi = () => ({
  form: FORM_TRONG,
  anhs: [],
  thongSos: [],
  tuyChons: [],
  bienThes: ghepBienThe(sinhToHop([]), [], ''),
  bienTheGoc: [],
  slug: null,
});

const rong = (v) => String(v ?? '').trim() === '';

const NHAN_MUC = { bienThes: 'Biến thể', hinhAnhs: 'Ảnh', thongSos: 'Thông số', tuyChons: 'Tùy chọn', giaTris: 'Giá trị' };
const NHAN_TRUONG = {
  sku: 'SKU', giaBan: 'Giá bán', giaGoc: 'Giá gốc', nguongCanhBao: 'Ngưỡng cảnh báo', tonKhoBanDau: 'Tồn kho ban đầu',
  giaTri: 'Giá trị', giaTris: 'Giá trị', ten: 'Tên', duongDan: 'Đường dẫn', loai: 'Loại', maMau: 'Mã màu', moTa: 'Mô tả',
};

/** "bienThes[0].sku" -> "Biến thể 1 – SKU"; khóa không nhận ra thì giữ nguyên. */
function nhanLoi(khoa) {
  const phan = khoa.split('.').map((p) => {
    const m = /^(\w+)\[(\d+)\]$/.exec(p);
    if (m && NHAN_MUC[m[1]]) return `${NHAN_MUC[m[1]]} ${Number(m[2]) + 1}`;
    return NHAN_TRUONG[p] || p;
  });
  return phan.join(' – ');
}

/** Các cặp tùy chọn / giá trị (tên đã trim) để gắn ảnh. */
function capTuyChonGiaTri(tuyChons) {
  return tuyChons.flatMap((tc) => tc.giaTris
    .filter((gt) => gt.giaTri.trim())
    .map((gt) => ({ khoa: gt.khoa, tuyChon: tc.ten.trim(), giaTri: gt.giaTri.trim() })));
}

const laSoNguyenKhongAm = (v) => !rong(v) && Number.isInteger(Number(v)) && Number(v) >= 0;

/** Kiểm tra trước khi gửi; trả về { tenO: thongBao }. */
function kiemTra({ form, anhs, thongSos, tuyChons, bienThes }) {
  const loi = {};
  if (rong(form.ten)) loi.ten = 'Nhập tên sản phẩm';
  if (rong(form.danhMucId)) loi.danhMucId = 'Vui lòng chọn danh mục';
  if (rong(form.giaBan) || Number(form.giaBan) <= 0) loi.giaBan = 'Giá bán phải lớn hơn 0';
  if (!rong(form.giaGoc)) {
    if (Number(form.giaGoc) <= 0) loi.giaGoc = 'Giá gốc phải lớn hơn 0';
    else if (Number(form.giaGoc) < Number(form.giaBan)) loi.giaGoc = 'Giá gốc phải lớn hơn hoặc bằng giá bán';
  }
  if (!laSoNguyenKhongAm(form.baoHanhThang) || Number(form.baoHanhThang) > 120) loi.baoHanhThang = 'Bảo hành từ 0 đến 120 tháng';
  if (form.hienThi && anhs.length === 0) loi.hinhAnhs = 'Cần ít nhất 1 ảnh khi sản phẩm đang hiển thị';
  if (thongSos.some((t) => rong(t.ten) !== rong(t.giaTri))) loi.thongSos = 'Mỗi dòng thông số cần đủ tên và giá trị';

  const loiTc = loiTuyChon(tuyChons);
  if (loiTc) loi.tuyChons = loiTc;

  const trung = skuTrung(bienThes);
  for (const b of bienThes) {
    const ten = b.sku.trim() || (b.giaTris.map((g) => g.giaTri).join(' / ') || 'mặc định');
    const loiS = loiSku(b.sku, trung);
    if (loiS) { loi.bienThes = `${ten}: ${loiS}`; break; }
    if ((!rong(b.giaBan) && Number(b.giaBan) <= 0) || (!rong(b.giaGoc) && Number(b.giaGoc) <= 0)) {
      loi.bienThes = `${ten}: giá riêng phải lớn hơn 0`; break;
    }
    const giaBan = rong(b.giaBan) ? form.giaBan : b.giaBan;
    const giaGoc = rong(b.giaGoc) ? form.giaGoc : b.giaGoc;
    if (!rong(giaGoc) && !rong(giaBan) && Number(giaGoc) < Number(giaBan)) {
      loi.bienThes = `Giá gốc của biến thể ${ten} phải lớn hơn hoặc bằng giá bán`; break;
    }
    if (!laSoNguyenKhongAm(b.nguongCanhBao)) { loi.bienThes = `${ten}: ngưỡng cảnh báo phải là số nguyên ≥ 0`; break; }
    if (b.id == null && !laSoNguyenKhongAm(b.tonKhoBanDau)) { loi.bienThes = `${ten}: tồn kho ban đầu phải là số nguyên ≥ 0`; break; }
  }
  return loi;
}

function ThongSoEditor({ thongSos, onChange }) {
  const sua = (khoa, thayDoi) => onChange(thongSos.map((t) => (t.khoa === khoa ? { ...t, ...thayDoi } : t)));
  const doiCho = (i, j) => {
    const moi = [...thongSos];
    [moi[i], moi[j]] = [moi[j], moi[i]];
    onChange(moi);
  };
  return (
    <div className="spec-editor">
      {thongSos.map((t, i) => (
        <div key={t.khoa} className="spec-row">
          <input aria-label="Tên thông số" placeholder="Tên (vd: Dung lượng pin)" maxLength={100} value={t.ten} onChange={(e) => sua(t.khoa, { ten: e.target.value })} />
          <input aria-label="Giá trị thông số" placeholder="Giá trị" maxLength={255} value={t.giaTri} onChange={(e) => sua(t.khoa, { giaTri: e.target.value })} />
          <button type="button" className="icon-btn" aria-label="Chuyển lên" disabled={i === 0} onClick={() => doiCho(i, i - 1)}>↑</button>
          <button type="button" className="icon-btn" aria-label="Chuyển xuống" disabled={i === thongSos.length - 1} onClick={() => doiCho(i, i + 1)}>↓</button>
          <button type="button" className="icon-btn" aria-label="Xóa thông số" onClick={() => onChange(thongSos.filter((x) => x.khoa !== t.khoa))}>×</button>
        </div>
      ))}
      <div>
        <button type="button" className="btn btn-outline btn-sm" onClick={() => onChange([...thongSos, { khoa: crypto.randomUUID(), ten: '', giaTri: '' }])}>+ Thêm thông số</button>
      </div>
    </div>
  );
}

export default function SanPhamFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const toast = useToast();
  const [st, setSt] = useState(trangThaiMoi);
  const [cayDanhMuc, setCayDanhMuc] = useState([]);
  const [thuongHieus, setThuongHieus] = useState([]);
  const [dangTai, setDangTai] = useState(!!id);
  const [loiTai, setLoiTai] = useState('');
  const [loi, setLoi] = useState('');
  const [loiTheoO, setLoiTheoO] = useState({});
  const [dangLuu, setDangLuu] = useState(false);
  const [canhBaoXoa, setCanhBaoXoa] = useState(null); // { soBienThe, tonKho }
  const boQuaTai = useRef(null); // id vừa lưu: dữ liệu đã nạp từ phản hồi, không tải lại

  useEffect(() => {
    adminApi.danhMuc.layCay().then(setCayDanhMuc).catch((err) => setLoiTai(layThongBaoLoi(err)));
    adminApi.thuongHieu.layTatCa().then(setThuongHieus).catch((err) => setLoiTai(layThongBaoLoi(err)));
  }, []);

  useEffect(() => {
    setLoi('');
    setLoiTheoO({});
    if (!id) {
      setSt(trangThaiMoi());
      setDangTai(false);
      return undefined;
    }
    if (boQuaTai.current === id) {
      boQuaTai.current = null;
      return undefined;
    }
    let huy = false;
    setSt(trangThaiMoi());
    setDangTai(true);
    setLoiTai('');
    adminApi.sanPham.layChiTiet(id)
      .then((sp) => { if (!huy) setSt(tuPhanHoi(sp)); })
      .catch((err) => { if (!huy) setLoiTai(layThongBaoLoi(err)); })
      .finally(() => { if (!huy) setDangTai(false); });
    return () => { huy = true; };
  }, [id]);

  const datForm = (khoa, giaTri) => setSt((s) => ({ ...s, form: { ...s.form, [khoa]: giaTri } }));
  /** Nhận giá trị mới hoặc hàm cập nhật (giá trị cũ) => giá trị mới. */
  const datPhan = (khoa) => (giaTri) => setSt((s) => ({ ...s, [khoa]: typeof giaTri === 'function' ? giaTri(s[khoa]) : giaTri }));

  const danhMucPhang = useMemo(() => cayDanhMuc.flatMap((goc) => [
    { id: goc.id, nhan: goc.ten },
    ...(goc.danhMucCons || []).map((con) => ({ id: con.id, nhan: `    ${con.ten}` })),
  ]), [cayDanhMuc]);

  const tuyChonGiaTris = useMemo(() => capTuyChonGiaTri(st.tuyChons), [st.tuyChons]);

  const gui = async () => {
    setCanhBaoXoa(null);
    setDangLuu(true);
    try {
      const sp = await luuSanPham(id, st, (idMoi) => {
        // Sản phẩm đã tạo: chuyển sang trang sửa ngay (giữ dữ liệu đang nhập) để lần lưu sau không tạo trùng
        boQuaTai.current = String(idMoi);
        navigate(`/admin/san-pham/${idMoi}`, { replace: true });
      });
      setSt(tuPhanHoi(sp));
      toast.hien(id ? 'Đã lưu sản phẩm' : 'Đã thêm sản phẩm');
    } catch (err) {
      setLoi(layThongBaoLoi(err));
      setLoiTheoO(err.response?.data?.loi || {});
    } finally {
      setDangLuu(false);
    }
  };

  const luu = (e) => {
    e.preventDefault();
    const loiMoi = kiemTra(st);
    setLoiTheoO(loiMoi);
    if (Object.keys(loiMoi).length) {
      setLoi('Vui lòng kiểm tra lại các ô được đánh dấu');
      return;
    }
    setLoi('');
    const conLai = new Set(st.bienThes.map((b) => b.khoa));
    const biXoa = st.bienTheGoc.filter((b) => !conLai.has(b.khoa));
    if (biXoa.length) {
      setCanhBaoXoa({ soBienThe: biXoa.length, tonKho: biXoa.reduce((t, b) => t + b.soLuongTon, 0) });
      return;
    }
    gui();
  };

  if (dangTai) return <Loading />;
  if (id && loiTai && !st.slug) {
    return (
      <>
        <h1>Sửa sản phẩm</h1>
        <ErrorMessage>{loiTai}</ErrorMessage>
        <Link to="/admin/san-pham">← Về danh sách sản phẩm</Link>
      </>
    );
  }

  const { form } = st;
  const loiO = (khoa) => loiTheoO[khoa] && <small className="field-error">{loiTheoO[khoa]}</small>;
  const loiKhac = Object.entries(loiTheoO).filter(([k]) => !O_CO_LOI_RIENG.includes(k));

  return (
    <form className="form product-form" onSubmit={luu} noValidate>
      <div className="admin-toolbar">
        <h1 className="admin-toolbar-grow">{id ? 'Sửa sản phẩm' : 'Thêm sản phẩm'}</h1>
        {id && st.slug && <a className="btn btn-outline btn-sm" href={`/san-pham/${st.slug}`} target="_blank" rel="noreferrer">Xem trên cửa hàng</a>}
        <Link className="btn btn-outline btn-sm" to="/admin/san-pham">← Danh sách</Link>
      </div>
      <ErrorMessage>{loiTai}</ErrorMessage>
      <ErrorMessage>{loi}</ErrorMessage>
      {loiKhac.length > 0 && (
        <ul className="field-error admin-error-list">
          {loiKhac.map(([k, v]) => <li key={k}>{nhanLoi(k)}: {v}</li>)}
        </ul>
      )}

      <section className="admin-card admin-form-section">
        <h2>Thông tin chung</h2>
        <div className="admin-form-grid">
          <label className="span-2">Tên sản phẩm
            <input value={form.ten} onChange={(e) => datForm('ten', e.target.value)} maxLength={255} required />
            {loiO('ten')}
          </label>
          <label>Danh mục
            <select value={form.danhMucId} onChange={(e) => datForm('danhMucId', e.target.value)} required>
              <option value="">— Chọn danh mục —</option>
              {danhMucPhang.map((d) => <option key={d.id} value={d.id}>{d.nhan}</option>)}
            </select>
            {loiO('danhMucId')}
          </label>
          <label>Thương hiệu
            <select value={form.thuongHieuId} onChange={(e) => datForm('thuongHieuId', e.target.value)}>
              <option value="">— Không có —</option>
              {thuongHieus.map((th) => <option key={th.id} value={th.id}>{th.ten}</option>)}
            </select>
            {loiO('thuongHieuId')}
          </label>
          <label className="span-2">Mô tả ngắn
            <textarea rows={2} maxLength={500} value={form.moTaNgan} onChange={(e) => datForm('moTaNgan', e.target.value)} />
            {loiO('moTaNgan')}
          </label>
          <label className="span-2">Mô tả (HTML cơ bản)
            <textarea rows={8} value={form.moTa} onChange={(e) => datForm('moTa', e.target.value)} />
            {loiO('moTa')}
          </label>
          <label>Bảo hành (tháng)
            <input type="number" min={0} max={120} value={form.baoHanhThang} onChange={(e) => datForm('baoHanhThang', e.target.value)} />
            {loiO('baoHanhThang')}
          </label>
          <div className="field admin-form-checks">
            <label className="checkbox-label">
              <input type="checkbox" checked={form.noiBat} onChange={(e) => datForm('noiBat', e.target.checked)} /> Sản phẩm nổi bật
            </label>
            <label className="checkbox-label">
              <input type="checkbox" checked={form.hienThi} onChange={(e) => datForm('hienThi', e.target.checked)} /> Hiển thị trên website
            </label>
          </div>
        </div>
      </section>

      <section className="admin-card admin-form-section">
        <h2>Giá</h2>
        <div className="admin-form-grid">
          <label>Giá bán (đ)
            <input type="number" min={1} value={form.giaBan} onChange={(e) => datForm('giaBan', e.target.value)} required />
            {loiO('giaBan')}
          </label>
          <label>Giá gốc (đ)
            <input type="number" min={1} value={form.giaGoc} onChange={(e) => datForm('giaGoc', e.target.value)} />
            <span className="field-hint">Để trống nếu không giảm giá. Giá gốc lớn hơn giá bán sẽ hiện nhãn giảm giá.</span>
            {loiO('giaGoc')}
          </label>
        </div>
      </section>

      <section className="admin-card admin-form-section">
        <h2>Hình ảnh</h2>
        <ProductImages anhs={st.anhs} onChange={datPhan('anhs')} tuyChonGiaTris={tuyChonGiaTris} />
        {loiO('hinhAnhs')}
      </section>

      <section className="admin-card admin-form-section">
        <h2>Thông số kỹ thuật</h2>
        <ThongSoEditor thongSos={st.thongSos} onChange={datPhan('thongSos')} />
        {loiO('thongSos')}
      </section>

      <section className="admin-card admin-form-section">
        <h2>Tùy chọn &amp; biến thể</h2>
        <VariantEditor
          tenSanPham={form.ten}
          tuyChons={st.tuyChons}
          onTuyChonsChange={datPhan('tuyChons')}
          bienThes={st.bienThes}
          onBienThesChange={datPhan('bienThes')}
          giaBanSanPham={form.giaBan}
        />
        {loiO('tuyChons')}
        {loiO('bienThes')}
      </section>

      <div className="admin-savebar">
        <span className="field-error admin-toolbar-grow">{loi}</span>
        <Link className="btn btn-outline" to="/admin/san-pham">Hủy</Link>
        <button type="submit" className="btn btn-primary" disabled={dangLuu}>{dangLuu ? 'Đang lưu...' : 'Lưu sản phẩm'}</button>
      </div>

      <ConfirmDialog
        mo={canhBaoXoa !== null}
        tieuDe="Biến thể sẽ ngừng bán"
        noiDung={canhBaoXoa && `${canhBaoXoa.soBienThe} biến thể sẽ ngừng bán (còn ${canhBaoXoa.tonKho} sản phẩm trong kho)`}
        nhanXacNhan="Vẫn lưu"
        nguyHiem
        dangXuLy={dangLuu}
        onXacNhan={gui}
        onHuy={() => setCanhBaoXoa(null)}
      />
    </form>
  );
}
