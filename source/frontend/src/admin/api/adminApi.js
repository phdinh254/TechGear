import axiosClient from '../../api/axiosClient';

const adminApi = {
  danhMuc: {
    // Backend trả danh sách phẳng (2 cấp, theo thứ tự): ghép thành cây gốc → danhMucCons
    layCay: () => axiosClient.get('/admin/danh-muc').then((ds) => ds
      .filter((d) => !d.danhMucChaId)
      .map((goc) => ({ ...goc, danhMucCons: ds.filter((con) => con.danhMucChaId === goc.id) }))),
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
  // Các API con của sản phẩm: mỗi lời gọi trả lại toàn bộ sản phẩm (SanPhamQT)
  tuyChon: {
    them: (sanPhamId, duLieu) => axiosClient.post(`/admin/san-pham/${sanPhamId}/tuy-chon`, duLieu),
    sua: (id, duLieu) => axiosClient.put(`/admin/tuy-chon/${id}`, duLieu),
    xoa: (id) => axiosClient.delete(`/admin/tuy-chon/${id}`),
  },
  bienThe: {
    them: (sanPhamId, duLieu) => axiosClient.post(`/admin/san-pham/${sanPhamId}/bien-the`, duLieu),
    sua: (id, duLieu) => axiosClient.put(`/admin/bien-the/${id}`, duLieu),
    xoa: (id) => axiosClient.delete(`/admin/bien-the/${id}`),
  },
  hinhAnh: {
    them: (sanPhamId, duLieu) => axiosClient.post(`/admin/san-pham/${sanPhamId}/hinh-anh`, duLieu),
    sua: (id, duLieu) => axiosClient.put(`/admin/hinh-anh/${id}`, duLieu),
    xoa: (id) => axiosClient.delete(`/admin/hinh-anh/${id}`),
  },
  tonKho: {
    danhSach: (thamSo) => axiosClient.get('/admin/kho', { params: thamSo }),
    nhapKho: (bienTheId, duLieu) => axiosClient.post(`/admin/bien-the/${bienTheId}/nhap-kho`, duLieu),
    dieuChinh: (bienTheId, duLieu) => axiosClient.post(`/admin/bien-the/${bienTheId}/dieu-chinh`, duLieu),
    lichSu: (bienTheId, trang = 0) => axiosClient.get('/admin/kho/lich-su', { params: { bienTheId, trang } }),
  },
  donHang: {
    timKiem: (thamSo) => axiosClient.get('/admin/don-hang', { params: thamSo }),
    // Trả { don, ghiChuQuanTri?, nguoiDungId?, trangThaiTiepTheo, thanhToans }; 3 API sửa bên dưới trả lại đúng dạng này
    layChiTiet: (ma) => axiosClient.get(`/admin/don-hang/${ma}`),
    doiTrangThai: (ma, trangThai, ghiChu) => axiosClient.patch(`/admin/don-hang/${ma}/trang-thai`, { trangThai, ghiChu: ghiChu || null }),
    doiThanhToan: (ma, trangThaiThanhToan) => axiosClient.patch(`/admin/don-hang/${ma}/thanh-toan`, { trangThaiThanhToan }),
    luuGhiChu: (ma, ghiChuQuanTri) => axiosClient.patch(`/admin/don-hang/${ma}/ghi-chu`, { ghiChuQuanTri: ghiChuQuanTri || null }),
  },
  maGiamGia: {
    timKiem: (thamSo) => axiosClient.get('/admin/ma-giam-gia', { params: thamSo }),
    tao: (duLieu) => axiosClient.post('/admin/ma-giam-gia', duLieu),
    capNhat: (id, duLieu) => axiosClient.put(`/admin/ma-giam-gia/${id}`, duLieu),
    xoa: (id) => axiosClient.delete(`/admin/ma-giam-gia/${id}`),
  },
  banner: {
    layTatCa: () => axiosClient.get('/admin/banner'),
    tao: (duLieu) => axiosClient.post('/admin/banner', duLieu),
    capNhat: (id, duLieu) => axiosClient.put(`/admin/banner/${id}`, duLieu),
    xoa: (id) => axiosClient.delete(`/admin/banner/${id}`),
  },
  caiDat: {
    layTatCa: () => axiosClient.get('/admin/cai-dat'),
    luu: (giaTriTheoKhoa) => axiosClient.put('/admin/cai-dat', giaTriTheoKhoa),
  },
  nguoiDung: {
    timKiem: (thamSo) => axiosClient.get('/admin/nguoi-dung', { params: thamSo }),
    doiTrangThai: (id, trangThai) => axiosClient.patch(`/admin/nguoi-dung/${id}/trang-thai`, { trangThai }),
  },
  danhGia: {
    timKiem: (thamSo) => axiosClient.get('/admin/danh-gia', { params: thamSo }),
    doiTrangThai: (id, trangThai) => axiosClient.patch(`/admin/danh-gia/${id}`, { trangThai }),
    xoa: (id) => axiosClient.delete(`/admin/danh-gia/${id}`),
  },
  thongKe: {
    doanhThu: (tu, den, theo = 'ngay') => axiosClient.get('/admin/thong-ke/doanh-thu', { params: { tu, den, theo } }),
    donHang: (tu, den) => axiosClient.get('/admin/thong-ke/don-hang', { params: { tu, den } }),
    banChay: (tu, den, gioiHan = 5) => axiosClient.get('/admin/thong-ke/ban-chay', { params: { tu, den, gioiHan } }),
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
