import Icon from './ui/Icon.jsx';

/** Trang bắt đầu từ 0. Hiện tối đa 5 số trang quanh trang hiện tại. */
export default function Pagination({ trang, tongSoTrang, onChange }) {
  if (tongSoTrang <= 1) return null;
  const batDau = Math.max(0, Math.min(trang - 2, tongSoTrang - 5));
  const cacTrang = Array.from({ length: Math.min(5, tongSoTrang) }, (_, i) => batDau + i);

  return (
    <nav className="pagination" aria-label="Phân trang">
      <button type="button" disabled={trang === 0} onClick={() => onChange(trang - 1)} aria-label="Trang trước">
        <Icon name="chevronLeft" size={18} />
      </button>
      {cacTrang.map((t) => (
        <button
          type="button"
          key={t}
          className={t === trang ? 'is-active' : undefined}
          aria-current={t === trang ? 'page' : undefined}
          onClick={() => onChange(t)}
        >
          {t + 1}
        </button>
      ))}
      <button type="button" disabled={trang >= tongSoTrang - 1} onClick={() => onChange(trang + 1)} aria-label="Trang sau">
        <Icon name="chevronRight" size={18} />
      </button>
    </nav>
  );
}
