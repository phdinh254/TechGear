import axiosClient from './axiosClient';

const storeApi = {
  layBanner: () => axiosClient.get('/banner'),
  layCaiDat: () => axiosClient.get('/cai-dat'),
  traCuuDonHang: (maDonHang, lienHe) => axiosClient.post('/tra-cuu-don-hang', { maDonHang, lienHe }),
};

export default storeApi;
