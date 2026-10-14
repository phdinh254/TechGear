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

  const thamSo = Object.fromEntries(params.entries());

  useEffect(() => {
    catalogApi.layDanhMuc().then(setDanhMuc).catch(() => {});
    catalogApi.layThuongHieu().then(setThuongHieu).catch(() => {});
  }, []);

  useEffect(() => {
    setDangTai(true);
    setLoi('');
    catalogApi
      .timSanPham({ kichThuoc: 12, ...Object.fromEntries(params.entries()) })
      .then(setKetQua)
      .catch((e) => setLoi(layThongBaoLoi(e)))
      .finally(() => setDangTai(false));
  }, [params]);

  /** Đổi một tham số lọc; đổi bộ lọc thì quay về trang đầu. */
  const doiThamSo = (khoa, giaTri) => {
    const moi = new URLSearchParams(params);
    if (giaTri === '' || giaTri === null || giaTri === undefined) moi.delete(khoa);
    else moi.set(khoa, giaTri);
    if (khoa !== 'trang') moi.delete('trang');
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

        <h3>Khoảng giá (VNĐ)</h3>
        <div className="price-range">
          <input
            type="number"
            min="0"
            step="10000"
            placeholder="Từ"
            defaultValue={thamSo.giaTu || ''}
            onBlur={(e) => doiThamSo('giaTu', e.target.value)}
          />
          <input
            type="number"
            min="0"
            step="10000"
            placeholder="Đến"
            defaultValue={thamSo.giaDen || ''}
            onBlur={(e) => doiThamSo('giaDen', e.target.value)}
          />
        </div>

        <button type="button" className="link-button" onClick={() => setParams({})}>
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
            <div className="product-grid">
              {ketQua.noiDung.map((sp) => (
                <ProductCard key={sp.id} sanPham={sp} />
              ))}
            </div>
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
