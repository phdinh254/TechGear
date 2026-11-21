import axiosClient from './axiosClient';

export const orderApi = {
  layDanhSach: (trang = 0) => axiosClient.get('/don-hang-cua-toi', { params: { trang, kichThuoc: 10 } }),
  layChiTiet: (maDonHang) => axiosClient.get(`/don-hang-cua-toi/${maDonHang}`),
  huy: (maDonHang, lyDo) => axiosClient.patch(`/don-hang-cua-toi/${maDonHang}/huy`, { lyDo }),
};

export const wishlistApi = {
  layDanhSach: () => axiosClient.get('/yeu-thich'),
  kiemTra: (sanPhamId) => axiosClient.get(`/yeu-thich/${sanPhamId}`),
  them: (sanPhamId) => axiosClient.post(`/yeu-thich/${sanPhamId}`),
  xoa: (sanPhamId) => axiosClient.delete(`/yeu-thich/${sanPhamId}`),
};

export const reviewApi = {
  tao: (duLieu) => axiosClient.post('/danh-gia', duLieu),
};
