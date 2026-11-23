import axiosClient from './axiosClient';

const paymentApi = {
  /** Tạo link thanh toán PayOS cho đơn, trả { duongDan, orderCode, maDonHang, tongTien, hetHan }. */
  taoLink: (maDonHang) => axiosClient.post(`/thanh-toan/payos/${maDonHang}`),
  /** Kết quả giao dịch: orderCode (PayOS gắn vào URL) + mã đơn (trong đường dẫn trả về); backend tự hỏi lại PayOS. */
  ketQua: (orderCode, ma) => axiosClient.get('/thanh-toan/payos/ket-qua', { params: { orderCode, ma } }),
};

// Link vừa tạo được nhớ theo tab để tải lại trang thanh toán không tạo thêm giao dịch
const khoa = (maDonHang) => `payos:${maDonHang}`;

/** Lưu link vừa tạo ({ duongDan, orderCode, maDonHang, tongTien, hetHan }). */
export function luuLink(maDonHang, link) {
  try {
    sessionStorage.setItem(khoa(maDonHang), JSON.stringify(link));
  } catch {
    // Trình duyệt chặn sessionStorage: lần sau chỉ tạo link mới
  }
}

export function docLink(maDonHang) {
  try {
    const link = JSON.parse(sessionStorage.getItem(khoa(maDonHang)));
    return link?.orderCode && link?.duongDan ? link : null;
  } catch {
    return null;
  }
}

export default paymentApi;
