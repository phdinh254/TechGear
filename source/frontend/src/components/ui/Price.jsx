import { formatTien } from '../../utils/format';

export function phanTramGiam(giaBan, giaGoc) {
  const ban = Number(giaBan);
  const goc = Number(giaGoc);
  return goc > ban && ban > 0 ? Math.round((1 - ban / goc) * 100) : 0;
}

export default function Price({ giaBan, giaGoc, size = 'md' }) {
  const giam = phanTramGiam(giaBan, giaGoc);
  return (
    <p className={`price price-${size}${giam ? ' is-sale' : ''}`}>
      <span className="price-current">{formatTien(giaBan)}</span>
      {giam > 0 && <del className="price-old">{formatTien(giaGoc)}</del>}
    </p>
  );
}
