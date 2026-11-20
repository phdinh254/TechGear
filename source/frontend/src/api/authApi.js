import axiosClient from './axiosClient';

const authApi = {
  dangNhap: (email, matKhau) => axiosClient.post('/auth/dang-nhap', { email, matKhau }),
  dangKy: (duLieu) => axiosClient.post('/auth/dang-ky', duLieu),
  layThongTin: () => axiosClient.get('/auth/toi'),
  quenMatKhau: (email) => axiosClient.post('/auth/quen-mat-khau', { email }),
  datLaiMatKhau: (ma, matKhauMoi) => axiosClient.post('/auth/dat-lai-mat-khau', { ma, matKhauMoi }),
};

export default authApi;
