// Chuyển đổi giữa thời gian API (ISO-8601 có múi giờ) và ô <input type="datetime-local"> / <input type="date">

const hai = (n) => String(n).padStart(2, '0');

/** ISO → 'yyyy-MM-ddTHH:mm' theo giờ máy (để điền vào datetime-local). */
export function sangONhap(iso) {
  if (!iso) return '';
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return '';
  return `${d.getFullYear()}-${hai(d.getMonth() + 1)}-${hai(d.getDate())}T${hai(d.getHours())}:${hai(d.getMinutes())}`;
}

/** 'yyyy-MM-ddTHH:mm' (giờ máy) → ISO có múi giờ; rỗng → null. */
export function tuONhap(giaTri) {
  if (!giaTri) return null;
  const d = new Date(giaTri);
  return Number.isNaN(d.getTime()) ? null : d.toISOString();
}

/** Date → 'yyyy-MM-dd' theo giờ máy (tham số ngày của API thống kê, lọc đơn). */
export function ngayISO(d) {
  return `${d.getFullYear()}-${hai(d.getMonth() + 1)}-${hai(d.getDate())}`;
}

/** Ngày cách hôm nay n ngày (n âm là về trước). */
export function ngayCach(n) {
  const d = new Date();
  d.setDate(d.getDate() + n);
  return d;
}
