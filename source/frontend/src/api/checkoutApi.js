import axiosClient from './axiosClient';

const checkoutApi = {
  tinhTien: (duLieu) => axiosClient.post('/dat-hang/tinh-tien', duLieu),
  datHang: (duLieu) => axiosClient.post('/dat-hang', duLieu),
};

export default checkoutApi;
