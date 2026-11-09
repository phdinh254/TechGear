import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import catalogApi from '../api/catalogApi';
import { layThongBaoLoi } from '../api/axiosClient';
import ProductCard from '../components/ProductCard.jsx';
import Pagination from '../components/Pagination.jsx';
import Loading from '../components/Loading.jsx';
import ErrorMessage from '../components/ErrorMessage.jsx';

const SAP_XEP = [
  { giaTri: 'moi_nhat', nhan: 'Mới nhất' },
  { giaTri: 'pho_bien', nhan: 'Phổ biến' },
  { giaTri: 'gia_tang', nhan: 'Giá tăng dần' },
  { giaTri: 'gia_giam', nhan: 'Giá giảm dần' },
];

export default function ProductListPage() {
  const [params, setParams] = useSearchParams();
  const [danhMuc, setDanhMuc] = useState([]);
  const [thuongHieu, setThuongHieu] = useState([]);
  const [ketQua, setKetQua] = useState(null);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');
  const [loiBoLoc, setLoiBoLoc] = useState('');
  const [khoangGia, setKhoangGia] = useState({ giaTu: '', giaDen: '' });

  const thamSo = Object.fromEntries(params.entries());
  const chuoiThamSo = params.toString();

  useEffect(() => {
    Promise.allSettled([catalogApi.layDanhMuc(), catalogApi.layThuongHieu()]).then(([dm, th]) => {
      if (dm.status === 'fulfilled') setDanhMuc(dm.value);
      if (th.status === 'fulfilled') setThuongHieu(th.value);
      if (dm.status === 'rejected' || th.status === 'rejected') {
        setLoiBoLoc('Một phần bộ lọc chưa tải được, vui lòng thử lại sau');
      }
    });
  }, []);

  useEffect(() => {
    setKhoangGia({ giaTu: params.get('giaTu') || '', giaDen: params.get('giaDen') || '' });
  }, [chuoiThamSo]);

  useEffect(() => {
    const controller = new AbortController();
    setDangTai(true);
    setLoi('');
    catalogApi
      .timSanPham({ kichThuoc: 12, ...Object.fromEntries(params.entries()) }, { signal: controller.signal })
      .then(setKetQua)
      .catch((e) => {
        if (e.code !== 'ERR_CANCELED') setLoi(layThongBaoLoi(e));
      })
      .finally(() => {
        if (!controller.signal.aborted) setDangTai(false);
      });
    return () => controller.abort();
  }, [chuoiThamSo]);

  /** Đổi một tham số lọc; đổi bộ lọc thì quay về trang đầu. */
  const doiThamSo = (khoa, giaTri) => {
    const moi = new URLSearchParams(params);
    if (giaTri === '' || giaTri === null || giaTri === undefined) moi.delete(khoa);
    else moi.set(khoa, giaTri);
    if (khoa !== 'trang') moi.delete('trang');
    setParams(moi);
  };

  const apDungKhoangGia = (e) => {
    e.preventDefault();
    const tu = khoangGia.giaTu === '' ? null : Number(khoangGia.giaTu);
    const den = khoangGia.giaDen === '' ? null : Number(khoangGia.giaDen);
    if ((tu !== null && tu < 0) || (den !== null && den < 0)) {
      setLoiBoLoc('Khoảng giá không được âm');
      return;
    }
    if (tu !== null && den !== null && tu > den) {
      setLoiBoLoc('Giá từ không được lớn hơn giá đến');
      return;
    }

    setLoiBoLoc('');
    const moi = new URLSearchParams(params);
    if (tu === null) moi.delete('giaTu');
    else moi.set('giaTu', String(tu));
    if (den === null) moi.delete('giaDen');
    else moi.set('giaDen', String(den));
    moi.delete('trang');
    setParams(moi);
  };

  return (
    <div className="list-layout">
      <aside className="filters">
        <h3>Danh mục</h3>
        <select value={thamSo.danhMuc || ''} onChange={(e) => doiThamSo('danhMuc', e.target.value)}>
          <option value="">Tất cả</option>
          {danhMuc.map((dm) => [
            <option key={dm.id} value={dm.slug}>
              {dm.ten}
            </option>,
            ...dm.danhMucCons.map((con) => (
              <option key={con.id} value={con.slug}>
                — {con.ten}
              </option>
            )),
          ])}
        </select>

        <h3>Thương hiệu</h3>
        <select value={thamSo.thuongHieu || ''} onChange={(e) => doiThamSo('thuongHieu', e.target.value)}>
          <option value="">Tất cả</option>
          {thuongHieu.map((th) => (
            <option key={th.id} value={th.slug}>
              {th.ten}
            </option>
          ))}
        </select>

        <form onSubmit={apDungKhoangGia}>
          <h3>Khoảng giá (VNĐ)</h3>
          <div className="price-range">
            <input
              type="number"
              min="0"
              step="10000"
              placeholder="Từ"
              value={khoangGia.giaTu}
              onChange={(e) => setKhoangGia({ ...khoangGia, giaTu: e.target.value })}
              aria-label="Giá từ"
            />
            <input
              type="number"
              min="0"
              step="10000"
              placeholder="Đến"
              value={khoangGia.giaDen}
              onChange={(e) => setKhoangGia({ ...khoangGia, giaDen: e.target.value })}
              aria-label="Giá đến"
            />
          </div>
          <button type="submit" className="filter-apply">Áp dụng giá</button>
        </form>

        {loiBoLoc && <p className="field-error filter-error">{loiBoLoc}</p>}

        <button type="button" className="link-button" onClick={() => {
          setLoiBoLoc('');
          setParams({});
        }}>
          Xóa bộ lọc
        </button>
      </aside>

      <section>
        <div className="list-toolbar">
          <h1>{thamSo.tuKhoa ? `Kết quả cho "${thamSo.tuKhoa}"` : 'Sản phẩm'}</h1>
          <select value={thamSo.sapXep || 'moi_nhat'} onChange={(e) => doiThamSo('sapXep', e.target.value)}>
            {SAP_XEP.map((s) => (
              <option key={s.giaTri} value={s.giaTri}>
                {s.nhan}
              </option>
            ))}
          </select>
        </div>

        {dangTai && <Loading />}
        <ErrorMessage>{loi}</ErrorMessage>
        {ketQua && !dangTai && (
          <>
            <p className="muted">Tìm thấy {ketQua.tongSoPhanTu} sản phẩm</p>
            <div className="product-grid" aria-live="polite">
              {ketQua.noiDung.map((sp) => (
                <ProductCard key={sp.id} sanPham={sp} />
              ))}
            </div>
            {ketQua.noiDung.length === 0 && (
              <div className="empty-result">
                <h2>Không tìm thấy sản phẩm</h2>
                <p className="muted">Hãy thử từ khóa khác hoặc xóa bớt bộ lọc.</p>
                <button type="button" className="btn" onClick={() => setParams({})}>Xóa bộ lọc</button>
              </div>
            )}
            <Pagination
              trang={ketQua.trang}
              tongSoTrang={ketQua.tongSoTrang}
              onChange={(t) => doiThamSo('trang', String(t))}
            />
          </>
        )}
      </section>
    </div>
  );
}
