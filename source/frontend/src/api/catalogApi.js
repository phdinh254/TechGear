import axiosClient from './axiosClient';

const catalogApi = {
  layDanhMuc: () => axiosClient.get('/danh-muc'),
  layThuongHieu: () => axiosClient.get('/thuong-hieu'),
  timSanPham: (thamSo, config = {}) => axiosClient.get('/san-pham', { ...config, params: thamSo }),
  laySanPhamNoiBat: () => axiosClient.get('/san-pham/noi-bat'),
  layChiTietSanPham: (slug, config = {}) => axiosClient.get(`/san-pham/${slug}`, config),
  layDanhGiaSanPham: (slug, trang = 0, config = {}) =>
    axiosClient.get(`/san-pham/${slug}/danh-gia`, {
      ...config,
      params: { trang, kichThuoc: 5 },
    }),
};

export default catalogApi;
