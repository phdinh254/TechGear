import axiosClient from './axiosClient';

const catalogApi = {
  layDanhMuc: () => axiosClient.get('/danh-muc'),
  layThuongHieu: () => axiosClient.get('/thuong-hieu'),
  timSanPham: (thamSo) => axiosClient.get('/san-pham', { params: thamSo }),
  laySanPhamNoiBat: () => axiosClient.get('/san-pham/noi-bat'),
  layChiTietSanPham: (slug) => axiosClient.get(`/san-pham/${slug}`),
};

export default catalogApi;
