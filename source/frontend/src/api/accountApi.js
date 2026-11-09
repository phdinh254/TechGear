import axiosClient from './axiosClient';

const accountApi = {
  capNhatHoSo: (duLieu) => axiosClient.put('/tai-khoan/ho-so', duLieu),
  doiMatKhau: (duLieu) => axiosClient.put('/tai-khoan/mat-khau', duLieu),
  layDiaChi: () => axiosClient.get('/tai-khoan/dia-chi'),
  taoDiaChi: (duLieu) => axiosClient.post('/tai-khoan/dia-chi', duLieu),
  capNhatDiaChi: (id, duLieu) => axiosClient.put(`/tai-khoan/dia-chi/${id}`, duLieu),
  datDiaChiMacDinh: (id) => axiosClient.patch(`/tai-khoan/dia-chi/${id}/mac-dinh`),
  xoaDiaChi: (id) => axiosClient.delete(`/tai-khoan/dia-chi/${id}`),
};

export default accountApi;
