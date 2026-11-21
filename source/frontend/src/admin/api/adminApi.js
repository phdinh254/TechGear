import axiosClient from '../../api/axiosClient';

const adminApi = {
  danhMuc: {
    layCay: () => axiosClient.get('/admin/danh-muc'),
    tao: (duLieu) => axiosClient.post('/admin/danh-muc', duLieu),
    capNhat: (id, duLieu) => axiosClient.put(`/admin/danh-muc/${id}`, duLieu),
    xoa: (id) => axiosClient.delete(`/admin/danh-muc/${id}`),
  },
  thuongHieu: {
    layTatCa: () => axiosClient.get('/admin/thuong-hieu'),
    tao: (duLieu) => axiosClient.post('/admin/thuong-hieu', duLieu),
    capNhat: (id, duLieu) => axiosClient.put(`/admin/thuong-hieu/${id}`, duLieu),
    xoa: (id) => axiosClient.delete(`/admin/thuong-hieu/${id}`),
  },
  sanPham: {
    timKiem: (thamSo) => axiosClient.get('/admin/san-pham', { params: thamSo }),
    layChiTiet: (id) => axiosClient.get(`/admin/san-pham/${id}`),
    tao: (duLieu) => axiosClient.post('/admin/san-pham', duLieu),
    capNhat: (id, duLieu) => axiosClient.put(`/admin/san-pham/${id}`, duLieu),
    doiHienThi: (id, hienThi) => axiosClient.patch(`/admin/san-pham/${id}/hien-thi`, { hienThi }),
    xoa: (id) => axiosClient.delete(`/admin/san-pham/${id}`),
  },
  tonKho: {
    danhSach: (thamSo) => axiosClient.get('/admin/ton-kho', { params: thamSo }),
    nhapKho: (bienTheId, duLieu) => axiosClient.post(`/admin/ton-kho/${bienTheId}/nhap-kho`, duLieu),
    dieuChinh: (bienTheId, duLieu) => axiosClient.post(`/admin/ton-kho/${bienTheId}/dieu-chinh`, duLieu),
    lichSu: (bienTheId, trang = 0) => axiosClient.get(`/admin/ton-kho/${bienTheId}/lich-su`, { params: { trang } }),
  },
  async taiAnh(file, thuMuc) {
    const form = new FormData();
    form.append('file', file);
    const res = await axiosClient.post('/admin/upload', form, {
      params: { thuMuc },
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return res.duongDan;
  },
};

export default adminApi;
