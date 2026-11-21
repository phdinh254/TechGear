import { Navigate, Route, Routes } from 'react-router-dom';
import AdminLayout from './AdminLayout.jsx';
import DanhMucPage from './pages/DanhMucPage.jsx';
import ThuongHieuPage from './pages/ThuongHieuPage.jsx';
import SanPhamListPage from './pages/SanPhamListPage.jsx';
import SanPhamFormPage from './pages/SanPhamFormPage.jsx';
import TonKhoPage from './pages/TonKhoPage.jsx';

export default function AdminApp() {
  return (
    <Routes>
      <Route element={<AdminLayout />}>
        <Route index element={<Navigate to="san-pham" replace />} />
        <Route path="danh-muc" element={<DanhMucPage />} />
        <Route path="thuong-hieu" element={<ThuongHieuPage />} />
        <Route path="san-pham" element={<SanPhamListPage />} />
        <Route path="san-pham/moi" element={<SanPhamFormPage />} />
        <Route path="san-pham/:id" element={<SanPhamFormPage />} />
        <Route path="ton-kho" element={<TonKhoPage />} />
        <Route path="*" element={<Navigate to="/admin/san-pham" replace />} />
      </Route>
    </Routes>
  );
}
