import { useEffect, useMemo, useRef, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import catalogApi from '../api/catalogApi';
import { layThongBaoLoi } from '../api/axiosClient';
import { useAuth } from '../context/AuthContext.jsx';
import { useCart } from '../context/CartContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import { useWishlist } from '../context/WishlistContext.jsx';
import ErrorMessage from '../components/ErrorMessage.jsx';
import Pagination from '../components/Pagination.jsx';
import ProductCarousel from '../components/ProductCarousel.jsx';
import Breadcrumb from '../components/ui/Breadcrumb.jsx';
import EmptyState from '../components/ui/EmptyState.jsx';
import Icon, { iconTheoSlug } from '../components/ui/Icon.jsx';
import Price, { phanTramGiam } from '../components/ui/Price.jsx';
import ProductMedia from '../components/ui/ProductMedia.jsx';
import QuantityStepper from '../components/ui/QuantityStepper.jsx';
import { SkeletonBlock } from '../components/ui/Skeleton.jsx';
import Stars from '../components/ui/Stars.jsx';
import useCaiDat from '../hooks/useCaiDat.js';
import { taoDongGio } from '../utils/cart';
import { formatTien } from '../utils/format';
import { lamSachHtml } from '../utils/sanitize';

const dinhDangNgay = new Intl.DateTimeFormat('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });

/** So sánh 2 mảng id không phân biệt thứ tự. */
const cungGiaTri = (a, b) => {
  if (a.length !== b.length) return false;
  const x = [...a].sort((m, n) => m - n);
  const y = [...b].sort((m, n) => m - n);
  return x.every((v, i) => v === y[i]);
};

function DetailSkeleton() {
  return (
    <div className="detail-grid" aria-hidden="true">
      <div className="gallery-main skeleton" />
      <div className="detail-info">
        <SkeletonBlock height={14} width="25%" />
        <SkeletonBlock height={40} width="85%" className="mt-3" />
        <SkeletonBlock height={28} width="40%" className="mt-6" />
        <SkeletonBlock height={90} className="mt-6" />
        <SkeletonBlock height={54} className="mt-6" />
      </div>
    </div>
  );
}

function Reviews({ slug, onTongQuan }) {
  const [danhGia, setDanhGia] = useState(null);
  const [trang, setTrang] = useState(0);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');

  useEffect(() => { setTrang(0); }, [slug]);

  useEffect(() => {
    const controller = new AbortController();
    setDangTai(true);
    setLoi('');
    catalogApi.layDanhGiaSanPham(slug, trang, { signal: controller.signal })
      .then((kq) => { setDanhGia(kq); onTongQuan(kq); })
      .catch((e) => { if (e.code !== 'ERR_CANCELED') setLoi(layThongBaoLoi(e)); })
      .finally(() => { if (!controller.signal.aborted) setDangTai(false); });
    return () => controller.abort();
  }, [slug, trang]);

  if (loi) return <ErrorMessage>{loi}</ErrorMessage>;
  if (!danhGia && dangTai) return <SkeletonBlock height={120} />;
  if (!danhGia || danhGia.tongDanhGia === 0) {
    return (
      <EmptyState compact icon="star" tieuDe="Chưa có đánh giá" moTa="Khách đã mua và nhận hàng có thể đánh giá sản phẩm trong mục Đơn hàng của tôi." />
    );
  }

  return (
    <div className="reviews">
      <div className="review-overview">
        <strong>{Number(danhGia.diemTrungBinh).toFixed(1)}</strong>
        <div>
          <Stars diem={danhGia.diemTrungBinh} size={18} />
          <span className="muted">{danhGia.tongDanhGia} đánh giá đã được duyệt</span>
        </div>
      </div>
      <div className={`review-list${dangTai ? ' is-loading' : ''}`}>
        {danhGia.danhSach.noiDung.map((dg) => (
          <article className="review-item" key={dg.id}>
            <div className="review-meta">
              <span className="avatar avatar-soft">{(dg.tenNguoiDung || '?').trim().split(/\s+/).pop()[0]}</span>
              <div>
                <strong>{dg.tenNguoiDung}</strong>
                <Stars diem={dg.soSao} size={13} />
              </div>
              {dg.ngayDanhGia && <time dateTime={dg.ngayDanhGia}>{dinhDangNgay.format(new Date(dg.ngayDanhGia))}</time>}
            </div>
            {dg.noiDung && <p>{dg.noiDung}</p>}
          </article>
        ))}
      </div>
      <Pagination trang={danhGia.danhSach.trang} tongSoTrang={danhGia.danhSach.tongSoTrang} onChange={setTrang} />
    </div>
  );
}

export default function ProductDetailPage() {
  const { slug } = useParams();
  const navigate = useNavigate();
  const { themVaoGio } = useCart();
  const { nguoiDung } = useAuth();
  const { laYeuThich, doiYeuThich } = useWishlist();
  const toast = useToast();
  const caiDat = useCaiDat();
  const [sp, setSp] = useState(null);
  const [loi, setLoi] = useState('');
  const [chon, setChon] = useState({});
  const [soLuong, setSoLuong] = useState(1);
  const [anhDangXem, setAnhDangXem] = useState(0);
  const [dangThem, setDangThem] = useState(false);
  const [tab, setTab] = useState('mo-ta');
  const [tongQuan, setTongQuan] = useState(null);
  const [lienQuan, setLienQuan] = useState([]);
  const [dangTaiLienQuan, setDangTaiLienQuan] = useState(true);
  const tabsRef = useRef(null);

  useEffect(() => {
    const controller = new AbortController();
    setSp(null);
    setLoi('');
    setTongQuan(null);
    setSoLuong(1);
    catalogApi.layChiTietSanPham(slug, { signal: controller.signal })
      .then((data) => {
        setSp(data);
        // Chọn sẵn biến thể đầu tiên còn hàng
        const macDinh = data.bienThes.find((b) => b.soLuongTon > 0) || data.bienThes[0];
        const chonBanDau = {};
        if (macDinh) {
          data.tuyChons.forEach((tc) => {
            const gt = tc.giaTris.find((g) => macDinh.giaTriIds.includes(g.id));
            if (gt) chonBanDau[tc.id] = gt.id;
          });
        }
        setChon(chonBanDau);
        const viTriAnhChinh = data.hinhAnhs.findIndex((anh) => anh.laAnhChinh);
        setAnhDangXem(viTriAnhChinh >= 0 ? viTriAnhChinh : 0);
        setTab(data.moTa ? 'mo-ta' : data.thongSos.length ? 'thong-so' : 'danh-gia');
      })
      .catch((e) => { if (e.code !== 'ERR_CANCELED') setLoi(layThongBaoLoi(e)); });
    return () => controller.abort();
  }, [slug]);

  useEffect(() => {
    if (!sp) return undefined;
    const controller = new AbortController();
    setDangTaiLienQuan(true);
    catalogApi.timSanPham({ danhMuc: sp.danhMuc.slug, kichThuoc: 9 }, { signal: controller.signal })
      .then((kq) => setLienQuan(kq.noiDung.filter((item) => item.id !== sp.id).slice(0, 8)))
      .catch(() => {})
      .finally(() => { if (!controller.signal.aborted) setDangTaiLienQuan(false); });
    return () => controller.abort();
  }, [sp]);

  const bienThe = useMemo(() => {
    if (!sp) return null;
    if (sp.tuyChons.length === 0) return sp.bienThes[0] || null;
    const daChon = Object.values(chon);
    if (daChon.length !== sp.tuyChons.length) return null;
    return sp.bienThes.find((b) => cungGiaTri(b.giaTriIds, daChon)) || null;
  }, [sp, chon]);

  const moTaAnToan = useMemo(() => lamSachHtml(sp?.moTa), [sp?.moTa]);

  if (loi) {
    return (
      <EmptyState icon="search" tieuDe="Không tìm thấy sản phẩm" moTa={loi}>
        <Link to="/san-pham" className="btn btn-primary">Xem sản phẩm khác</Link>
      </EmptyState>
    );
  }
  if (!sp) return <><Breadcrumb items={[{ nhan: 'Sản phẩm', to: '/san-pham' }, { nhan: '…' }]} /><DetailSkeleton /></>;

  const giaBan = bienThe ? bienThe.giaBan : sp.giaBan;
  const giaGoc = bienThe ? bienThe.giaGoc : sp.giaGoc;
  const giam = phanTramGiam(giaBan, giaGoc);
  const conHang = Boolean(bienThe && bienThe.soLuongTon > 0);
  const sapHet = conHang && bienThe.soLuongTon <= 5;
  const anh = sp.hinhAnhs[anhDangXem];
  const icon = iconTheoSlug(sp.danhMuc.slug);
  const thich = laYeuThich(sp.id);
  const nguongMienPhi = Number(caiDat.mien_phi_van_chuyen_tu);

  /** Thêm vào giỏ; muaNgay = true thì chuyển thẳng tới bước đặt hàng. */
  const them = async (muaNgay = false) => {
    if (!bienThe || !conHang) return;
    setDangThem(true);
    try {
      await themVaoGio(taoDongGio(sp, bienThe, chon), soLuong);
      if (muaNgay) navigate('/dat-hang');
      else toast.hien(`Đã thêm ${soLuong} sản phẩm vào giỏ`, { hanhDong: { nhan: 'Xem giỏ', to: '/gio-hang' } });
    } catch (error) {
      toast.hien(layThongBaoLoi(error), { loai: 'error' });
    } finally {
      setDangThem(false);
    }
  };

  const bamYeuThich = async () => {
    if (!nguoiDung) {
      navigate('/dang-nhap', { state: { tu: `/san-pham/${sp.slug}` } });
      return;
    }
    try {
      const moi = await doiYeuThich(sp.id);
      toast.hien(moi ? 'Đã lưu vào danh sách yêu thích' : 'Đã bỏ khỏi yêu thích');
    } catch (error) {
      toast.hien(error.message, { loai: 'error' });
    }
  };

  const xemDanhGia = () => {
    setTab('danh-gia');
    tabsRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  };

  const tabs = [
    moTaAnToan && { id: 'mo-ta', nhan: 'Mô tả' },
    sp.thongSos.length > 0 && { id: 'thong-so', nhan: 'Thông số kỹ thuật' },
    { id: 'danh-gia', nhan: `Đánh giá${tongQuan?.tongDanhGia ? ` (${tongQuan.tongDanhGia})` : ''}` },
  ].filter(Boolean);

  const breadcrumb = [{ nhan: 'Sản phẩm', to: '/san-pham' }, { nhan: sp.danhMuc.ten, to: `/san-pham?danhMuc=${sp.danhMuc.slug}` }, { nhan: sp.ten }];

  return (
    <div className="detail">
      <Breadcrumb items={breadcrumb} />

      <div className="detail-grid">
        <div className="gallery">
          <div className="gallery-main">
            {giam > 0 && <span className="badge badge-sale gallery-badge">-{giam}%</span>}
            <div className="gallery-stage" key={anh?.id ?? 'none'}>
              <ProductMedia src={anh?.duongDan} alt={anh?.moTa || sp.ten} ten={sp.ten} icon={icon} loading="eager" />
            </div>
          </div>
          {sp.hinhAnhs.length > 1 && (
            <div className="gallery-thumbs">
              {sp.hinhAnhs.map((ha, i) => (
                <button type="button" key={ha.id} className={i === anhDangXem ? 'is-active' : undefined} onClick={() => setAnhDangXem(i)} aria-label={`Xem ảnh ${i + 1}`} aria-current={i === anhDangXem}>
                  <ProductMedia src={ha.duongDan} alt="" ten={sp.ten} icon={icon} size="sm" />
                </button>
              ))}
            </div>
          )}
        </div>

        <div className="detail-info">
          <div className="detail-meta">
            {sp.thuongHieu && <Link to={`/san-pham?thuongHieu=${sp.thuongHieu.slug}`} className="detail-brand">{sp.thuongHieu.ten}</Link>}
            {bienThe && <span className="detail-sku">SKU: {bienThe.sku}</span>}
          </div>
          <h1>{sp.ten}</h1>

          {tongQuan?.tongDanhGia > 0 && (
            <button type="button" className="detail-rating" onClick={xemDanhGia}>
              <Stars diem={tongQuan.diemTrungBinh} size={15} />
              <span>{Number(tongQuan.diemTrungBinh).toFixed(1)}</span>
              <span className="muted">({tongQuan.tongDanhGia} đánh giá)</span>
            </button>
          )}

          <div className="detail-price">
            <Price giaBan={giaBan} giaGoc={giaGoc} size="xl" />
            {giam > 0 && <span className="detail-save">Tiết kiệm {formatTien(Number(giaGoc) - Number(giaBan))}</span>}
          </div>

          {sp.moTaNgan && <p className="detail-short">{sp.moTaNgan}</p>}

          {sp.tuyChons.map((tc) => {
            const daChon = tc.giaTris.find((g) => g.id === chon[tc.id]);
            return (
              <div key={tc.id} className="option-group">
                <p className="option-label">{tc.ten}{daChon && <span>: {daChon.giaTri}</span>}</p>
                <div className="option-values">
                  {tc.giaTris.map((gt) => (
                    <button
                      type="button"
                      key={gt.id}
                      className={`option${chon[tc.id] === gt.id ? ' is-active' : ''}`}
                      aria-pressed={chon[tc.id] === gt.id}
                      onClick={() => { setChon({ ...chon, [tc.id]: gt.id }); setSoLuong(1); }}
                    >
                      {gt.maMau && <span className="swatch" style={{ background: gt.maMau }} />}
                      {gt.giaTri}
                    </button>
                  ))}
                </div>
              </div>
            );
          })}

          <p className={`stock ${!bienThe ? 'stock-none' : conHang ? (sapHet ? 'stock-low' : 'stock-in') : 'stock-out'}`}>
            <span className="stock-dot" />
            {!bienThe ? 'Phiên bản này hiện không có' : !conHang ? 'Tạm hết hàng' : sapHet ? `Chỉ còn ${bienThe.soLuongTon} sản phẩm` : 'Còn hàng'}
          </p>

          <div className="buy-box">
            <QuantityStepper value={soLuong} max={bienThe?.soLuongTon || 1} onChange={setSoLuong} disabled={!conHang} />
            <button type="button" className="btn btn-lg buy-add" disabled={!conHang || dangThem} onClick={() => them(false)}>
              {dangThem ? <span className="spinner" /> : <Icon name="bag" size={18} />} Thêm vào giỏ
            </button>
            <button type="button" className={`icon-btn like-btn${thich ? ' is-active' : ''}`} onClick={bamYeuThich} aria-pressed={thich} aria-label={thich ? 'Bỏ yêu thích' : 'Thêm vào yêu thích'}>
              <Icon name="heart" size={22} />
            </button>
          </div>
          <button type="button" className="btn btn-primary btn-lg btn-block" disabled={!conHang || dangThem} onClick={() => them(true)}>
            Mua ngay
          </button>

          <ul className="perks">
            <li><Icon name="shield" size={20} /><span><strong>{sp.baoHanhThang > 0 ? `Bảo hành ${sp.baoHanhThang} tháng` : 'Không áp dụng bảo hành'}</strong>{sp.baoHanhThang > 0 ? 'Chính hãng, giữ mã đơn để được hỗ trợ' : 'Sản phẩm tiêu hao / phụ kiện đi kèm'}</span></li>
            <li><Icon name="truck" size={20} /><span><strong>Giao hàng toàn quốc</strong>{nguongMienPhi > 0 ? `Miễn phí cho đơn từ ${formatTien(nguongMienPhi)}` : 'Phí hiển thị khi thanh toán'}</span></li>
            <li><Icon name="wallet" size={20} /><span><strong>Thanh toán khi nhận hàng</strong>Kiểm tra hàng trước khi trả tiền</span></li>
          </ul>
        </div>
      </div>

      <section className="detail-tabs" ref={tabsRef}>
        <div className="tabs" role="tablist" aria-label="Thông tin sản phẩm">
          {tabs.map((t) => (
            <button type="button" role="tab" key={t.id} id={`tab-${t.id}`} aria-selected={tab === t.id} aria-controls={`panel-${t.id}`} className={tab === t.id ? 'is-active' : undefined} onClick={() => setTab(t.id)}>
              {t.nhan}
            </button>
          ))}
        </div>
        <div className="tab-panel" role="tabpanel" id={`panel-${tab}`} aria-labelledby={`tab-${tab}`}>
          {tab === 'mo-ta' && moTaAnToan && <div className="prose" dangerouslySetInnerHTML={{ __html: moTaAnToan }} />}
          {tab === 'thong-so' && (
            <dl className="spec-list">
              {sp.thongSos.map((ts) => (
                <div key={ts.ten}><dt>{ts.ten}</dt><dd>{ts.giaTri}</dd></div>
              ))}
            </dl>
          )}
          <div hidden={tab !== 'danh-gia'}><Reviews slug={slug} onTongQuan={setTongQuan} /></div>
        </div>
      </section>

      {(dangTaiLienQuan || lienQuan.length > 0) && (
        <section className="section">
          <div className="section-head">
            <h2>Có thể bạn cũng thích</h2>
            <Link to={`/san-pham?danhMuc=${sp.danhMuc.slug}`} className="link-arrow">Xem thêm <Icon name="arrowRight" size={16} /></Link>
          </div>
          <ProductCarousel sanPhams={lienQuan} dangTai={dangTaiLienQuan} nhan="Sản phẩm liên quan" />
        </section>
      )}

      <div className="buy-bar" aria-hidden="true">
        <div>
          <span className="buy-bar-name">{sp.ten}</span>
          <Price giaBan={giaBan} giaGoc={giaGoc} />
        </div>
        <button type="button" className="btn btn-primary" tabIndex={-1} disabled={!conHang || dangThem} onClick={() => them(false)}>
          <Icon name="bag" size={18} /> Thêm vào giỏ
        </button>
      </div>
    </div>
  );
}
