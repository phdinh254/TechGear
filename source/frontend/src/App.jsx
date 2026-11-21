import { lazy, Suspense } from 'react';
import { Route, Routes } from 'react-router-dom';
import Layout from './components/Layout.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import HomePage from './pages/HomePage.jsx';
import ProductListPage from './pages/ProductListPage.jsx';
import ProductDetailPage from './pages/ProductDetailPage.jsx';
import LoginPage from './pages/LoginPage.jsx';
import RegisterPage from './pages/RegisterPage.jsx';
import ForgotPasswordPage from './pages/ForgotPasswordPage.jsx';
import ResetPasswordPage from './pages/ResetPasswordPage.jsx';
import CartPage from './pages/CartPage.jsx';
import AccountPage from './pages/AccountPage.jsx';
import CheckoutPage from './pages/CheckoutPage.jsx';
import OrdersPage from './pages/OrdersPage.jsx';
import OrderDetailPage from './pages/OrderDetailPage.jsx';
import WishlistPage from './pages/WishlistPage.jsx';
import OrderLookupPage from './pages/OrderLookupPage.jsx';
import PolicyPage from './pages/PolicyPage.jsx';
import ContactPage from './pages/ContactPage.jsx';
import NotFoundPage from './pages/NotFoundPage.jsx';
import Loading from './components/Loading.jsx';

const AdminApp = lazy(() => import('./admin/AdminApp.jsx'));

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<HomePage />} />
        <Route path="san-pham" element={<ProductListPage />} />
        <Route path="san-pham/:slug" element={<ProductDetailPage />} />
        <Route path="gio-hang" element={<CartPage />} />
        <Route path="dat-hang" element={<CheckoutPage />} />
        <Route path="tra-cuu-don-hang" element={<OrderLookupPage />} />
        <Route path="chinh-sach" element={<PolicyPage />} />
        <Route path="lien-he" element={<ContactPage />} />
        <Route path="dang-nhap" element={<LoginPage />} />
        <Route path="dang-ky" element={<RegisterPage />} />
        <Route path="quen-mat-khau" element={<ForgotPasswordPage />} />
        <Route path="dat-lai-mat-khau" element={<ResetPasswordPage />} />
        <Route path="tai-khoan" element={<ProtectedRoute><AccountPage /></ProtectedRoute>} />
        <Route path="tai-khoan/don-hang" element={<ProtectedRoute><OrdersPage /></ProtectedRoute>} />
        <Route path="tai-khoan/don-hang/:maDonHang" element={<ProtectedRoute><OrderDetailPage /></ProtectedRoute>} />
        <Route path="tai-khoan/yeu-thich" element={<ProtectedRoute><WishlistPage /></ProtectedRoute>} />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
      <Route path="admin/*" element={<ProtectedRoute chiQuanTri><Suspense fallback={<Loading />}><AdminApp /></Suspense></ProtectedRoute>} />
    </Routes>
  );
}
