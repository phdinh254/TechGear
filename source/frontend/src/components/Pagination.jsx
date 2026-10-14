export default function Pagination({ trang, tongSoTrang, onChange }) {
  if (tongSoTrang <= 1) return null;
  return (
    <div className="pagination">
      <button type="button" disabled={trang === 0} onClick={() => onChange(trang - 1)}>
        Trước
      </button>
      <span>
        Trang {trang + 1} / {tongSoTrang}
      </span>
      <button type="button" disabled={trang >= tongSoTrang - 1} onClick={() => onChange(trang + 1)}>
        Sau
      </button>
    </div>
  );
}
