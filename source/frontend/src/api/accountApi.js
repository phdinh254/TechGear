import axiosClient from './axiosClient';

const accountApi = {
  capNhatHoSo: (duLieu) => axiosClient.put('/tai-khoan', duLieu),
  doiAnhDaiDien: (file) => {
    const duLieu = new FormData();
    duLieu.append('file', file);
    return axiosClient.post('/tai-khoan/anh-dai-dien', duLieu, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
  doiMatKhau: (duLieu) => axiosClient.put('/tai-khoan/mat-khau', duLieu),
  layDiaChi: () => axiosClient.get('/tai-khoan/dia-chi'),
  taoDiaChi: (duLieu) => axiosClient.post('/tai-khoan/dia-chi', duLieu),
  capNhatDiaChi: (id, duLieu) => axiosClient.put(`/tai-khoan/dia-chi/${id}`, duLieu),
  datDiaChiMacDinh: (id) => axiosClient.patch(`/tai-khoan/dia-chi/${id}/mac-dinh`),
  xoaDiaChi: (id) => axiosClient.delete(`/tai-khoan/dia-chi/${id}`),
};

export default accountApi;
