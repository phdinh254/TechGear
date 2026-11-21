import axiosClient from './axiosClient';

const checkoutApi = {
  tinhTien: (duLieu) => axiosClient.post('/don-hang/tinh-thu', duLieu),
  // Đã đăng nhập: đơn gắn với tài khoản; chưa đăng nhập: đơn vãng lai (bắt buộc email)
  datHang: (duLieu, daDangNhap) => axiosClient.post(daDangNhap ? '/don-hang' : '/don-hang/vang-lai', duLieu),
};

export default checkoutApi;
