import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import catalogApi from '../api/catalogApi';
import storeApi from '../api/storeApi';
import { layThongBaoLoi } from '../api/axiosClient';
import ProductCarousel from '../components/ProductCarousel.jsx';
import ErrorMessage from '../components/ErrorMessage.jsx';
import Icon, { iconTheoSlug } from '../components/ui/Icon.jsx';
import Price from '../components/ui/Price.jsx';
import ProductMedia from '../components/ui/ProductMedia.jsx';
import Reveal from '../components/ui/Reveal.jsx';
import { SkeletonBlock } from '../components/ui/Skeleton.jsx';
import { useDanhMuc, useThuongHieu } from '../hooks/useCatalog.js';
import { duongDanAnh } from '../utils/format';

function Hero({ noiBat, dangTai }) {
  const [chon, setChon] = useState(0);
  const sp = noiBat[chon];
  const showcase = noiBat.slice(0, 4);

  return (
    <section className="hero" aria-labelledby="hero-title">
      <div className="hero-bg" aria-hidden="true" />
      <div className="container hero-inner">
        <div className="hero-copy">
          <h1 id="hero-title">Nâng cấp <span className="text-gradient">mọi kết nối</span> của bạn.</h1>
          <p className="hero-lead">
            Phụ kiện công nghệ được chọn lọc – bảo hành rõ ràng, giao tận nơi, thanh toán khi nhận hàng.
          </p>
          <div className="hero-actions">
            <Link to="/san-pham" className="btn btn-accent btn-lg">Khám phá sản phẩm <Icon name="arrowRight" size={18} /></Link>
          </div>
        </div>

        <div className="hero-showcase">
          {dangTai ? (
            <div className="showcase-card"><SkeletonBlock height="100%" className="skeleton-dark" /></div>
          ) : sp ? (
            <>
              <Link to={`/san-pham/${sp.slug}`} className="showcase-card" key={sp.id}>
                <span className="showcase-tag"><Icon name="sparkle" size={14} /> Nổi bật</span>
                <div className="showcase-media"><ProductMedia src={sp.anhChinh} ten={sp.ten} size="xl" loading="eager" /></div>
                <div className="showcase-info">
                  <div>
                    {sp.tenThuongHieu && <span className="showcase-brand">{sp.tenThuongHieu}</span>}
                    <strong>{sp.ten}</strong>
                  </div>
                  <Price giaBan={sp.giaBan} giaGoc={sp.giaGoc} size="lg" />
                </div>
              </Link>
              {showcase.length > 1 && (
                <div className="showcase-thumbs" role="tablist" aria-label="Sản phẩm nổi bật">
                  {showcase.map((item, i) => (
                    <button
                      type="button"
                      role="tab"
                      key={item.id}
                      aria-selected={i === chon}
                      aria-label={item.ten}
                      className={i === chon ? 'is-active' : undefined}
                      onClick={() => setChon(i)}
                    >
                      <ProductMedia src={item.anhChinh} ten={item.ten} size="sm" />
                    </button>
                  ))}
                </div>
              )}
            </>
          ) : (
            <div className="showcase-card showcase-empty">
              <div className="showcase-media"><Icon name="headphones" size={140} strokeWidth={0.8} /></div>
            </div>
          )}
        </div>
      </div>
    </section>
  );
}

function BrandStrip({ thuongHieu }) {
  if (thuongHieu.length === 0) return null;
  const nhom = (anHien) => (
    <div className="brand-group" aria-hidden={anHien || undefined}>
      {thuongHieu.map((th) => (
        anHien
          ? <span key={th.id} className="brand-name">{th.ten}</span>
          : <Link key={th.id} to={`/san-pham?thuongHieu=${encodeURIComponent(th.slug)}`} className="brand-name">{th.ten}</Link>
      ))}
    </div>
  );
  return (
    <section className="brand-strip" aria-label="Thương hiệu">
      <div className="container brand-strip-inner">
        <div className="brand-marquee">
          <div className={`brand-track${thuongHieu.length > 2 ? ' is-moving' : ''}`}>
            {nhom(false)}
            {thuongHieu.length > 2 && nhom(true)}
          </div>
        </div>
      </div>
    </section>
  );
}

function SectionHead({ tieuDe, to, nhanLink = 'Xem tất cả', canGiua = false }) {
  return (
    <div className={`section-head${canGiua ? ' section-head-center' : ''}`}>
      <div>
        <h2>{tieuDe}</h2>
      </div>
      {to && <Link to={to} className="link-arrow">{nhanLink} <Icon name="arrowRight" size={16} /></Link>}
    </div>
  );
}

function CategoryBento({ danhMucs, dangTai }) {
  if (!dangTai && danhMucs.length === 0) return null;
  return (
    <Reveal as="section" className="section container" aria-labelledby="dm-title">
      <SectionHead tieuDe={<span id="dm-title">Mua sắm theo nhu cầu</span>} to="/san-pham" />
      <div className="bento">
        {dangTai
          ? Array.from({ length: 4 }, (_, i) => <div key={i} className="bento-tile skeleton" />)
          : danhMucs.map((dm, i) => (
            <Link
              key={dm.id}
              to={`/san-pham?danhMuc=${dm.slug}`}
              className={`bento-tile reveal-item${i === 0 ? ' bento-tile-feature' : ''}`}
              style={{ '--i': i }}
            >
              {dm.hinhAnh ? (
                <img className="bento-img" src={duongDanAnh(dm.hinhAnh)} alt="" loading="lazy" />
              ) : (
                <span className="bento-art" aria-hidden="true"><Icon name={iconTheoSlug(dm.slug)} size={i === 0 ? 180 : 112} strokeWidth={0.9} /></span>
              )}
              <div className="bento-body">
                <h3>{dm.ten}</h3>
                {dm.danhMucCons?.length > 0 && <p>{dm.danhMucCons.map((c) => c.ten).join(' · ')}</p>}
              </div>
              <span className="bento-arrow" aria-hidden="true"><Icon name="arrowRight" size={20} /></span>
            </Link>
          ))}
      </div>
    </Reveal>
  );
}

function PromoBanners({ banners }) {
  if (banners.length === 0) return null;
  return (
    <section className="section container">
      <div className="promo-banners">
        {banners.map((bn) => {
          const anh = <img src={duongDanAnh(bn.hinhAnh)} alt={bn.tieuDe || 'Khuyến mãi TechGear'} loading="lazy" />;
          if (!bn.lienKet) return <div key={bn.id} className="promo-banner">{anh}</div>;
          return /^https?:\/\//.test(bn.lienKet) ? (
            <a key={bn.id} className="promo-banner" href={bn.lienKet} target="_blank" rel="noopener noreferrer">{anh}</a>
          ) : (
            <Link key={bn.id} className="promo-banner" to={bn.lienKet}>{anh}</Link>
          );
        })}
      </div>
    </section>
  );
}

function BrandBanner() {
  return (
    <Reveal as="section" className="section container">
      <div className="statement">
        <div className="statement-copy">
          <h2>Ưu đãi có hạn<br />Giảm đến 70%!</h2>
          <p>
            Nhanh tay sở hữu các sản phẩm công nghệ với mức giá ưu đãi cực sốc. Chương trình chỉ áp dụng trong thời gian có hạn!
          </p>
          <Link to="/san-pham" className="btn btn-light statement-btn">Mua sắm ngay <Icon name="arrowRight" size={18} /></Link>
        </div>
        <div className="statement-media" aria-hidden="true">
          <img src="/images/uu-dai-tai-nghe.jpg" alt="" loading="lazy" />
        </div>
      </div>
    </Reveal>
  );
}

const LY_DO = [
  { icon: 'shield', tieuDe: 'Hàng chính hãng', moTa: 'Nguồn gốc rõ ràng, bảo hành theo từng sản phẩm.' },
  { icon: 'truck', tieuDe: 'Giao nhanh toàn quốc', moTa: 'Đóng gói cẩn thận, theo dõi trạng thái đơn trực tuyến.' },
  { icon: 'refresh', tieuDe: 'Đổi trả dễ dàng', moTa: 'Hỗ trợ đổi khi sản phẩm lỗi kỹ thuật hoặc giao sai.' },
  { icon: 'headset', tieuDe: 'Tư vấn tận tâm', moTa: 'Đội ngũ hỗ trợ chọn phụ kiện phù hợp thiết bị của bạn.' },
];

function WhyUs() {
  return (
    <Reveal as="section" className="section container" aria-labelledby="why-title">
      <SectionHead canGiua tieuDe={<span id="why-title">Trải nghiệm mua sắm đáng tin cậy</span>} />
      <div className="why-grid">
        {LY_DO.map((ld, i) => (
          <div key={ld.tieuDe} className="why-item reveal-item" style={{ '--i': i }}>
            <span className="why-icon"><Icon name={ld.icon} size={24} /></span>
            <h3>{ld.tieuDe}</h3>
            <p>{ld.moTa}</p>
          </div>
        ))}
      </div>
    </Reveal>
  );
}

export default function HomePage() {
  const [danhMucs, dangTaiDanhMuc] = useDanhMuc();
  const [thuongHieu] = useThuongHieu();
  const [noiBat, setNoiBat] = useState([]);
  const [moiNhat, setMoiNhat] = useState([]);
  const [banners, setBanners] = useState([]);
  const [dangTai, setDangTai] = useState(true);
  const [loi, setLoi] = useState('');

  useEffect(() => {
    Promise.allSettled([
      catalogApi.laySanPhamNoiBat(),
      catalogApi.timSanPham({ sapXep: 'moi_nhat', kichThuoc: 10 }),
      storeApi.layBanner(),
    ]).then(([nb, mn, bn]) => {
      if (nb.status === 'fulfilled') setNoiBat(nb.value);
      else setLoi(layThongBaoLoi(nb.reason));
      if (mn.status === 'fulfilled') setMoiNhat(mn.value.noiDung);
      if (bn.status === 'fulfilled') setBanners(bn.value);
    }).finally(() => setDangTai(false));
  }, []);


  return (
    <div className="home">
      <Hero noiBat={noiBat} dangTai={dangTai} />
      <BrandStrip thuongHieu={thuongHieu} />

      {loi && <div className="container section-tight"><ErrorMessage>{loi}</ErrorMessage></div>}

      <CategoryBento danhMucs={danhMucs} dangTai={dangTaiDanhMuc} />

      {(dangTai || noiBat.length > 0) && (
        <Reveal as="section" className="section container" aria-labelledby="nb-title">
          <SectionHead tieuDe={<span id="nb-title">Sản phẩm nổi bật</span>} />
          <ProductCarousel sanPhams={noiBat} dangTai={dangTai} nhan="Sản phẩm nổi bật" />
        </Reveal>
      )}

      <PromoBanners banners={banners} />

      {(dangTai || moiNhat.length > 0) && (
        <Reveal as="section" className="section container" aria-labelledby="mn-title">
          <SectionHead tieuDe={<span id="mn-title">Hàng mới về</span>} to="/san-pham?sapXep=moi_nhat" />
          <ProductCarousel sanPhams={moiNhat} dangTai={dangTai} badge="Mới" nhan="Hàng mới về" dieuHuong="khong" />
        </Reveal>
      )}

      <BrandBanner />
      <WhyUs />
    </div>
  );
}
