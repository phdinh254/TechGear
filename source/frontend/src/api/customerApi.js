import axiosClient from './axiosClient';

export const orderApi = {
  layDanhSach: (trang = 0) => axiosClient.get('/don-hang', { params: { trang, kichThuoc: 10 } }),
  layChiTiet: (maDonHang) => axiosClient.get(`/don-hang/${maDonHang}`),
  huy: (maDonHang, lyDo) => axiosClient.post(`/don-hang/${maDonHang}/huy`, { lyDo }),
};

export const wishlistApi = {
  layDanhSach: () => axiosClient.get('/yeu-thich'),
  them: (sanPhamId) => axiosClient.put(`/yeu-thich/${sanPhamId}`),
  xoa: (sanPhamId) => axiosClient.delete(`/yeu-thich/${sanPhamId}`),
};

export const reviewApi = {
  tao: (duLieu) => axiosClient.post('/danh-gia', duLieu),
};
