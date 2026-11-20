import { useEffect, useRef, useState } from 'react';
import ProductCard from './ProductCard.jsx';
import Icon from './ui/Icon.jsx';
import { ProductCardSkeleton } from './ui/Skeleton.jsx';

/**
 * Dải sản phẩm cuộn ngang (scroll-snap).
 * dieuHuong: 'tren' – nút trước/sau ở góc trên phải (mặc định), 'khong' – không có nút (chỉ vuốt/cuộn).
 */
export default function ProductCarousel({ sanPhams, dangTai, badge, nhan = 'Danh sách sản phẩm', dieuHuong = 'tren' }) {
  const track = useRef(null);
  const [dau, setDau] = useState(true);
  const [cuoi, setCuoi] = useState(false);

  const capNhat = () => {
    const el = track.current;
    if (!el) return;
    setDau(el.scrollLeft <= 4);
    setCuoi(el.scrollLeft + el.clientWidth >= el.scrollWidth - 4);
  };

  useEffect(() => {
    capNhat();
    window.addEventListener('resize', capNhat);
    return () => window.removeEventListener('resize', capNhat);
  }, [sanPhams, dangTai]);

  const cuon = (huong) => {
    const el = track.current;
    if (el) el.scrollBy({ left: huong * el.clientWidth * 0.85, behavior: 'smooth' });
  };

  return (
    <div className={`carousel carousel-nav-${dieuHuong}`}>
      {dieuHuong !== 'khong' && !dangTai && sanPhams.length > 0 && (
        <div className="carousel-nav">
          <button type="button" onClick={() => cuon(-1)} disabled={dau} aria-label="Xem sản phẩm trước">
            <Icon name="chevronLeft" size={20} />
          </button>
          <button type="button" onClick={() => cuon(1)} disabled={cuoi} aria-label="Xem sản phẩm tiếp">
            <Icon name="chevronRight" size={20} />
          </button>
        </div>
      )}
      <div className="carousel-track" ref={track} onScroll={capNhat} role="region" aria-label={nhan} tabIndex={0}>
        {dangTai
          ? Array.from({ length: 5 }, (_, i) => (
            <div className="carousel-item" key={i}><ProductCardSkeleton /></div>
          ))
          : sanPhams.map((sp, i) => (
            <div className="carousel-item reveal-item" style={{ '--i': i }} key={sp.id}>
              <ProductCard sanPham={sp} badge={badge} />
            </div>
          ))}
      </div>
    </div>
  );
}
