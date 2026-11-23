// Nhãn hiển thị cho các giá trị liệt kê của API quản trị (giá trị viết thường như CSDL)
export { NHAN_TRANG_THAI as NHAN_TRANG_THAI_DON } from '../../components/OrderView.jsx';

export const NHAN_THANH_TOAN = {
  chua_thanh_toan: { nhan: 'Chưa thanh toán', lop: 'admin-badge--warning' },
  da_thanh_toan: { nhan: 'Đã thanh toán', lop: 'admin-badge--success' },
  da_hoan_tien: { nhan: 'Đã hoàn tiền', lop: 'admin-badge--muted' },
};

export const NHAN_PHUONG_THUC = { cod: 'COD', vnpay: 'VNPay' };

export const NHAN_GIAO_DICH = {
  cho_xu_ly: { nhan: 'Chờ xử lý', lop: 'admin-badge--warning' },
  thanh_cong: { nhan: 'Thành công', lop: 'admin-badge--success' },
  that_bai: { nhan: 'Thất bại', lop: 'admin-badge--danger' },
};

export const NHAN_DANH_GIA = {
  cho_duyet: { nhan: 'Chờ duyệt', lop: 'admin-badge--warning' },
  da_duyet: { nhan: 'Đã duyệt', lop: 'admin-badge--success' },
  da_an: { nhan: 'Đã ẩn', lop: 'admin-badge--muted' },
};

export const NHAN_VAI_TRO = { quan_tri: 'Quản trị', khach_hang: 'Khách hàng' };

export const NHAN_NGUOI_DUNG = {
  hoat_dong: { nhan: 'Hoạt động', lop: 'admin-badge--success' },
  bi_khoa: { nhan: 'Bị khóa', lop: 'admin-badge--danger' },
};

/** Badge từ bảng nhãn { giaTri: { nhan, lop } }; giá trị lạ thì hiện nguyên văn. */
export function Badge({ bang, giaTri }) {
  const n = bang[giaTri];
  return n ? <span className={`admin-badge ${n.lop}`}>{n.nhan}</span> : <span className="admin-badge admin-badge--muted">{giaTri}</span>;
}
