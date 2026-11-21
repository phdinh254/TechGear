import axiosClient from './axiosClient';

const cartApi = {
  layGioHang: () => axiosClient.get('/gio-hang'),
  them: (bienTheId, soLuong) => axiosClient.post('/gio-hang', { bienTheId, soLuong }),
  capNhat: (bienTheId, soLuong) => axiosClient.put(`/gio-hang/${bienTheId}`, { soLuong }),
  xoa: (bienTheId) => axiosClient.delete(`/gio-hang/${bienTheId}`),
  xoaTatCa: () => axiosClient.delete('/gio-hang'),
  gop: (dongs) => axiosClient.post('/gio-hang/gop', dongs),
};

export default cartApi;
