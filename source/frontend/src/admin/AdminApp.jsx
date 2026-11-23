import { Navigate, Route, Routes } from 'react-router-dom';
import AdminLayout from './AdminLayout.jsx';
import TongQuanPage from './pages/TongQuanPage.jsx';
import DonHangListPage from './pages/DonHangListPage.jsx';
import DonHangChiTietPage from './pages/DonHangChiTietPage.jsx';
import DanhMucPage from './pages/DanhMucPage.jsx';
import ThuongHieuPage from './pages/ThuongHieuPage.jsx';
import SanPhamListPage from './pages/SanPhamListPage.jsx';
import SanPhamFormPage from './pages/SanPhamFormPage.jsx';
import TonKhoPage from './pages/TonKhoPage.jsx';
import MaGiamGiaPage from './pages/MaGiamGiaPage.jsx';
import BannerPage from './pages/BannerPage.jsx';
import DanhGiaPage from './pages/DanhGiaPage.jsx';
import NguoiDungPage from './pages/NguoiDungPage.jsx';
import CaiDatPage from './pages/CaiDatPage.jsx';

export default function AdminApp() {
  return (
    <Routes>
      <Route element={<AdminLayout />}>
        <Route index element={<Navigate to="tong-quan" replace />} />
        <Route path="tong-quan" element={<TongQuanPage />} />
        <Route path="don-hang" element={<DonHangListPage />} />
        <Route path="don-hang/:maDonHang" element={<DonHangChiTietPage />} />
        <Route path="danh-muc" element={<DanhMucPage />} />
        <Route path="thuong-hieu" element={<ThuongHieuPage />} />
        <Route path="san-pham" element={<SanPhamListPage />} />
        <Route path="san-pham/moi" element={<SanPhamFormPage />} />
        <Route path="san-pham/:id" element={<SanPhamFormPage />} />
        <Route path="ton-kho" element={<TonKhoPage />} />
        <Route path="ma-giam-gia" element={<MaGiamGiaPage />} />
        <Route path="banner" element={<BannerPage />} />
        <Route path="danh-gia" element={<DanhGiaPage />} />
        <Route path="nguoi-dung" element={<NguoiDungPage />} />
        <Route path="cai-dat" element={<CaiDatPage />} />
        <Route path="*" element={<Navigate to="/admin/tong-quan" replace />} />
      </Route>
    </Routes>
  );
}
