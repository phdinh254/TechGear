import axiosClient from './axiosClient';

const authApi = {
  dangNhap: (email, matKhau) => axiosClient.post('/auth/dang-nhap', { email, matKhau }),
  dangKy: (duLieu) => axiosClient.post('/auth/dang-ky', duLieu),
  layThongTin: () => axiosClient.get('/auth/toi'),
};

export default authApi;
