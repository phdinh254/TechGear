import axiosClient from './axiosClient';

const storeApi = {
  layBanner: () => axiosClient.get('/banner'),
  layCaiDat: () => axiosClient.get('/cai-dat'),
  traCuuDonHang: (maDonHang, email) => axiosClient.post('/don-hang/tra-cuu', { maDonHang, email }),
};

export default storeApi;
