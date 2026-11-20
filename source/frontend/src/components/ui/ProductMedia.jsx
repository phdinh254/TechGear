import { useState } from 'react';
import Icon, { iconTheoTen } from './Icon.jsx';
import { duongDanAnh } from '../../utils/format';

/**
 * Ảnh sản phẩm; khi chưa có ảnh hoặc ảnh lỗi thì hiện minh họa theo loại sản phẩm
 * thay vì khung trống, để lưới sản phẩm vẫn gọn gàng và có nhận diện.
 */
export default function ProductMedia({ src, alt, ten, icon, size = 'md', loading = 'lazy', className = '' }) {
  const [loi, setLoi] = useState(false);
  const url = duongDanAnh(src);

  if (url && !loi) {
    return (
      <img
        className={`media-img ${className}`}
        src={url}
        alt={alt ?? ten ?? ''}
        loading={loading}
        decoding="async"
        onError={() => setLoi(true)}
      />
    );
  }

  return (
    <div className={`media-placeholder media-placeholder-${size} ${className}`} role="img" aria-label={alt ?? ten ?? 'Chưa có ảnh'}>
      <Icon name={icon || iconTheoTen(ten)} strokeWidth={1.25} />
    </div>
  );
}
