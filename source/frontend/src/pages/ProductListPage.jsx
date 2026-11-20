import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import catalogApi from '../api/catalogApi';
import { layThongBaoLoi } from '../api/axiosClient';
import ProductCard from '../components/ProductCard.jsx';
import Pagination from '../components/Pagination.jsx';
import ErrorMessage from '../components/ErrorMessage.jsx';
import Breadcrumb from '../components/ui/Breadcrumb.jsx';
import EmptyState from '../components/ui/EmptyState.jsx';
import Icon, { iconTheoSlug } from '../components/ui/Icon.jsx';
import { ProductGridSkeleton } from '../components/ui/Skeleton.jsx';
import { timDanhMuc, useDanhMuc, useThuongHieu } from '../hooks/useCatalog.js';
import { formatTien } from '../utils/format';

const SAP_XEP = [
  { giaTri: 'moi_nhat', nhan: 'Mới nhất' },
  { giaTri: 'pho_bien', nhan: 'Phổ biến' },
  { giaTri: 'gia_tang', nhan: 'Giá thấp đến cao' },
  { giaTri: 'gia_giam', nhan: 'Giá cao đến thấp' },
];

const MUC_GIA = [
  { nhan: 'Dưới 200.000đ', giaDen: 200000 },
  { nhan: '200.000đ – 500.000đ', giaTu: 200000, giaDen: 500000 },
  { nhan: '500.000đ – 1 triệu', giaTu: 500000, giaDen: 1000000 },
  { nhan: '1 – 2 triệu', giaTu: 1000000, giaDen: 2000000 },
  { nhan: 'Trên 2 triệu', giaTu: 2000000 },
];

const KICH_THUOC = 12;

function FilterGroup({ tieuDe, children, macDinhMo = true }) {
  const [mo, setMo] = useState(macDinhMo);
  return (
    <div className="filter-group">
      <button type="button" className="filter-group-head" aria-expanded={mo} onClick={() => setMo(!mo)}>
        {tieuDe}
        <Icon name="chevronDown" size={18} className={mo ? 'rotate-180' : ''} />
      </button>
      {mo && <div className="filter-group-body">{children}</div>}
    </div>
  );
}

function Filters({ thamSo, danhMucs, thuongHieus, doiThamSo, datKhoangGia, xoaBoLoc, ten }) {
  const [khoangGia, setKhoangGia] = useState({ giaTu: thamSo.giaTu || '', giaDen: thamSo.giaDen || '' });
  const [loiGia, setLoiGia] = useState('');

  useEffect(() => {
    setKhoangGia({ giaTu: thamSo.giaTu || '', giaDen: thamSo.giaDen || '' });
  }, [thamSo.giaTu, thamSo.giaDen]);

  const apDungGia = (e) => {
    e.preventDefault();
    const tu = khoangGia.giaTu === '' ? null : Number(khoangGia.giaTu);
    const den = khoangGia.giaDen === '' ? null : Number(khoangGia.giaDen);
    if ((tu !== null && tu < 0) || (den !== null && den < 0)) return setLoiGia('Khoảng giá không được âm');
    if (tu !== null && den !== null && tu > den) return setLoiGia('Giá từ không được lớn hơn giá đến');
    setLoiGia('');
    return datKhoangGia(tu, den);
  };

  const mucGiaDangChon = (m) => String(m.giaTu ?? '') === (thamSo.giaTu || '') && String(m.giaDen ?? '') === (thamSo.giaDen || '');

  return (
    <>
      <FilterGroup tieuDe="Danh mục">
        <ul className="filter-tree">
          <li>
            <button type="button" className={!thamSo.danhMuc ? 'is-active' : undefined} onClick={() => doiThamSo('danhMuc', '')}>
              Tất cả sản phẩm
            </button>
          </li>
          {danhMucs.map((dm) => (
            <li key={dm.id}>
              <button type="button" className={thamSo.danhMuc === dm.slug ? 'is-active' : undefined} onClick={() => doiThamSo('danhMuc', dm.slug)}>
                <Icon name={iconTheoSlug(dm.slug)} size={18} /> {dm.ten}
              </button>
              {dm.danhMucCons?.length > 0 && (
                <ul>
                  {dm.danhMucCons.map((con) => (
                    <li key={con.id}>
                      <button type="button" className={thamSo.danhMuc === con.slug ? 'is-active' : undefined} onClick={() => doiThamSo('danhMuc', con.slug)}>
                        {con.ten}
                      </button>
                    </li>
                  ))}
                </ul>
              )}
            </li>
          ))}
        </ul>
      </FilterGroup>

      {thuongHieus.length > 0 && (
        <FilterGroup tieuDe="Thương hiệu">
          <div className="filter-options">
            {thuongHieus.map((th) => {
              const chon = thamSo.thuongHieu === th.slug;
              return (
                <label key={th.id} className="checkbox-label">
                  <input type="checkbox" checked={chon} onChange={() => doiThamSo('thuongHieu', chon ? '' : th.slug)} />
                  {th.ten}
                </label>
              );
            })}
          </div>
        </FilterGroup>
      )}

      <FilterGroup tieuDe="Khoảng giá">
        <div className="filter-options">
          {MUC_GIA.map((m) => {
            const chon = mucGiaDangChon(m);
            return (
              <label key={m.nhan} className="checkbox-label">
                <input type="radio" name={`muc-gia-${ten}`} checked={chon} onChange={() => datKhoangGia(m.giaTu ?? null, m.giaDen ?? null)} />
                {m.nhan}
              </label>
            );
          })}
        </div>
        <form className="price-form" onSubmit={apDungGia}>
          <div className="price-inputs">
            <input type="number" min="0" step="10000" inputMode="numeric" placeholder="Từ" aria-label="Giá từ" value={khoangGia.giaTu} onChange={(e) => setKhoangGia({ ...khoangGia, giaTu: e.target.value })} />
            <span aria-hidden="true">–</span>
            <input type="number" min="0" step="10000" inputMode="numeric" placeholder="Đến" aria-label="Giá đến" value={khoangGia.giaDen} onChange={(e) => setKhoangGia({ ...khoangGia, giaDen: e.target.value })} />
          </div>
          {loiGia && <p className="field-error">{loiGia}</p>}
          <button type="submit" className="btn btn-outline btn-sm btn-block">Áp dụng</button>
        </form>
      </FilterGroup>

      <button type="button" className="link-button filter-reset" onClick={xoaBoLoc}>Xóa tất cả bộ lọc</button>
    </>
  );
}

export default function ProductListPage() {
  const [params, setParams] = useSearchParams();
  const [danhMucs] = useDanhMuc();
  const [thuongHieus] = useThuongHieu();
  const [ketQua, setKetQua] = useState(null);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');
  const [moLoc, setMoLoc] = useState(false);

  const thamSo = Object.fromEntries(params.entries());
  const chuoiThamSo = params.toString();

  useEffect(() => {
    const controller = new AbortController();
    setDangTai(true);
    setLoi('');
    catalogApi
      .timSanPham({ kichThuoc: KICH_THUOC, ...Object.fromEntries(params.entries()) }, { signal: controller.signal })
      .then(setKetQua)
      .catch((e) => { if (e.code !== 'ERR_CANCELED') setLoi(layThongBaoLoi(e)); })
      .finally(() => { if (!controller.signal.aborted) setDangTai(false); });
    return () => controller.abort();
  }, [chuoiThamSo]);

  useEffect(() => {
    document.body.classList.toggle('no-scroll', moLoc);
    return () => document.body.classList.remove('no-scroll');
  }, [moLoc]);

  /** Đổi một tham số; đổi bộ lọc thì quay về trang đầu. */
  const doiThamSo = (khoa, giaTri) => {
    const moi = new URLSearchParams(params);
    if (giaTri === '' || giaTri === null || giaTri === undefined) moi.delete(khoa);
    else moi.set(khoa, giaTri);
    if (khoa !== 'trang') moi.delete('trang');
    setParams(moi);
    if (khoa === 'trang') window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const datKhoangGia = (tu, den) => {
    const moi = new URLSearchParams(params);
    if (tu === null) moi.delete('giaTu'); else moi.set('giaTu', String(tu));
    if (den === null) moi.delete('giaDen'); else moi.set('giaDen', String(den));
    moi.delete('trang');
    setParams(moi);
  };

  const xoaBoLoc = () => {
    const moi = new URLSearchParams();
    if (thamSo.sapXep) moi.set('sapXep', thamSo.sapXep);
    setParams(moi);
  };

  const { danhMuc, cha } = timDanhMuc(danhMucs, thamSo.danhMuc);
  const thuongHieu = thuongHieus.find((th) => th.slug === thamSo.thuongHieu);
  const tieuDe = thamSo.tuKhoa ? `Kết quả cho "${thamSo.tuKhoa}"` : danhMuc?.ten || thuongHieu?.ten || 'Tất cả sản phẩm';

  const breadcrumb = [{ nhan: 'Sản phẩm', to: '/san-pham' }];
  if (cha) breadcrumb.push({ nhan: cha.ten, to: `/san-pham?danhMuc=${cha.slug}` });
  if (danhMuc) breadcrumb.push({ nhan: danhMuc.ten });
  else if (thamSo.tuKhoa) breadcrumb.push({ nhan: 'Tìm kiếm' });

  const dangLoc = [];
  if (thamSo.tuKhoa) dangLoc.push({ khoa: 'tuKhoa', nhan: `"${thamSo.tuKhoa}"` });
  if (danhMuc) dangLoc.push({ khoa: 'danhMuc', nhan: danhMuc.ten });
  if (thuongHieu) dangLoc.push({ khoa: 'thuongHieu', nhan: thuongHieu.ten });
  if (thamSo.giaTu || thamSo.giaDen) {
    const nhan = thamSo.giaTu && thamSo.giaDen
      ? `${formatTien(thamSo.giaTu)} – ${formatTien(thamSo.giaDen)}`
      : thamSo.giaTu ? `Từ ${formatTien(thamSo.giaTu)}` : `Đến ${formatTien(thamSo.giaDen)}`;
    dangLoc.push({ khoa: 'gia', nhan });
  }
  const boChip = (khoa) => (khoa === 'gia' ? datKhoangGia(null, null) : doiThamSo(khoa, ''));

  const danhMucCon = danhMuc?.danhMucCons?.length ? danhMuc.danhMucCons : cha?.danhMucCons || [];
  const boLoc = (ten) => <Filters ten={ten} thamSo={thamSo} danhMucs={danhMucs} thuongHieus={thuongHieus} doiThamSo={doiThamSo} datKhoangGia={datKhoangGia} xoaBoLoc={xoaBoLoc} />;

  return (
    <div className="listing">
      <Breadcrumb items={breadcrumb} />
      <header className="listing-hero">
        <div>
          <h1>{tieuDe}</h1>
          <p className="lead">
            {danhMuc
              ? `Phụ kiện ${danhMuc.ten.toLowerCase()} chính hãng, bảo hành rõ ràng, giao hàng toàn quốc.`
              : 'Khám phá phụ kiện công nghệ được chọn lọc cho công việc và giải trí.'}
          </p>
        </div>
        {danhMucCon.length > 0 && (
          <div className="chip-row listing-subcats">
            {(cha || danhMuc) && (
              <Link to={`/san-pham?danhMuc=${(cha || danhMuc).slug}`} className={`chip${!cha ? ' is-active' : ''}`}>Tất cả</Link>
            )}
            {danhMucCon.map((con) => (
              <Link key={con.id} to={`/san-pham?danhMuc=${con.slug}`} className={`chip${thamSo.danhMuc === con.slug ? ' is-active' : ''}`}>
                <Icon name={iconTheoSlug(con.slug)} size={16} /> {con.ten}
              </Link>
            ))}
          </div>
        )}
      </header>

      <div className="listing-layout">
        <aside className="listing-filters" aria-label="Bộ lọc sản phẩm">{boLoc('sidebar')}</aside>

        <section className="listing-main" aria-live="polite" aria-busy={dangTai}>
          <div className="listing-toolbar">
            <button type="button" className="btn btn-outline btn-sm listing-filter-btn" onClick={() => setMoLoc(true)}>
              <Icon name="filter" size={18} /> Bộ lọc{dangLoc.length > 0 && ` (${dangLoc.length})`}
            </button>
            <p className="listing-count">
              {ketQua ? <><strong>{ketQua.tongSoPhanTu}</strong> sản phẩm</> : ' '}
            </p>
            <label className="listing-sort">
              <span>Sắp xếp</span>
              <select value={thamSo.sapXep || 'moi_nhat'} onChange={(e) => doiThamSo('sapXep', e.target.value)}>
                {SAP_XEP.map((s) => <option key={s.giaTri} value={s.giaTri}>{s.nhan}</option>)}
              </select>
            </label>
          </div>

          {dangLoc.length > 0 && (
            <div className="active-filters">
              {dangLoc.map((f) => (
                <button type="button" key={f.khoa} className="chip chip-removable" onClick={() => boChip(f.khoa)} aria-label={`Bỏ lọc ${f.nhan}`}>
                  {f.nhan} <Icon name="close" size={14} />
                </button>
              ))}
              <button type="button" className="link-button" onClick={xoaBoLoc}>Xóa tất cả</button>
            </div>
          )}

          <ErrorMessage>{loi}</ErrorMessage>

          {dangTai ? (
            <div className="product-grid"><ProductGridSkeleton soLuong={KICH_THUOC / 2} /></div>
          ) : ketQua && ketQua.noiDung.length > 0 ? (
            <>
              <div className="product-grid">
                {ketQua.noiDung.map((sp, i) => (
                  <div key={sp.id} className="grid-item-in" style={{ '--i': i }}><ProductCard sanPham={sp} /></div>
                ))}
              </div>
              <Pagination trang={ketQua.trang} tongSoTrang={ketQua.tongSoTrang} onChange={(t) => doiThamSo('trang', String(t))} />
            </>
          ) : !loi && (
            <EmptyState icon="search" tieuDe="Không tìm thấy sản phẩm phù hợp" moTa="Thử từ khóa khác, chọn danh mục rộng hơn hoặc bỏ bớt bộ lọc.">
              <button type="button" className="btn btn-primary" onClick={xoaBoLoc}>Xóa bộ lọc</button>
              <Link to="/san-pham" className="btn btn-outline">Xem tất cả sản phẩm</Link>
            </EmptyState>
          )}
        </section>
      </div>

      <div className={`sheet-backdrop${moLoc ? ' is-open' : ''}`} onClick={() => setMoLoc(false)} aria-hidden="true" />
      <aside className={`sheet${moLoc ? ' is-open' : ''}`} aria-label="Bộ lọc" aria-hidden={!moLoc} inert={moLoc ? undefined : ''}>
        <div className="sheet-head">
          <h2>Bộ lọc</h2>
          <button type="button" className="icon-btn" onClick={() => setMoLoc(false)} aria-label="Đóng bộ lọc"><Icon name="close" size={22} /></button>
        </div>
        <div className="sheet-body">{boLoc('sheet')}</div>
        <div className="sheet-foot">
          <button type="button" className="btn btn-primary btn-block" onClick={() => setMoLoc(false)}>
            Xem {ketQua?.tongSoPhanTu ?? ''} sản phẩm
          </button>
        </div>
      </aside>
    </div>
  );
}
