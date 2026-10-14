import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import Loading from './Loading.jsx';

/** Chỉ cho vào khi đã đăng nhập; nếu chiQuanTri = true thì yêu cầu vai trò quản trị. */
export default function ProtectedRoute({ children, chiQuanTri = false }) {
  const { nguoiDung, dangTai, laQuanTri } = useAuth();
  const location = useLocation();

  if (dangTai) return <Loading />;
  if (!nguoiDung) return <Navigate to="/dang-nhap" replace state={{ tu: location.pathname }} />;
  if (chiQuanTri && !laQuanTri) return <Navigate to="/" replace />;
  return children;
}
