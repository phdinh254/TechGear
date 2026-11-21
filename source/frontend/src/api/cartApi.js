import axiosClient from './axiosClient';

const cartApi = {
  layGioHang: () => axiosClient.get('/gio-hang'),
  them: (bienTheId, soLuong) => axiosClient.post('/gio-hang/items', { bienTheId, soLuong }),
  capNhat: (bienTheId, soLuong) => axiosClient.put(`/gio-hang/items/${bienTheId}`, { soLuong }),
  xoa: (bienTheId) => axiosClient.delete(`/gio-hang/items/${bienTheId}`),
  xoaTatCa: () => axiosClient.delete('/gio-hang'),
  gop: (items) => axiosClient.post('/gio-hang/gop', { items }),
};

export default cartApi;
