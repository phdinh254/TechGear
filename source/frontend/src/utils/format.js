const dinhDangTien = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' });

export function formatTien(soTien) {
  if (soTien === null || soTien === undefined) return '';
  return dinhDangTien.format(Number(soTien));
}

const UPLOAD_URL = import.meta.env.VITE_UPLOAD_URL || 'http://localhost:8080';

/** Ghép đường dẫn ảnh từ backend; trả null nếu không có ảnh. */
export function duongDanAnh(duongDan) {
  if (!duongDan) return null;
  if (/^https?:\/\//.test(duongDan)) return duongDan;
  return `${UPLOAD_URL}${duongDan.startsWith('/') ? '' : '/'}${duongDan}`;
}
