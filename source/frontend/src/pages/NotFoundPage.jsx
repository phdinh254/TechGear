import { Link } from 'react-router-dom';
import Icon from '../components/ui/Icon.jsx';

export default function NotFoundPage() {
  return (
    <div className="not-found">
      <p className="not-found-code" aria-hidden="true">404</p>
      <h1>Không tìm thấy trang</h1>
      <p className="lead">Trang bạn tìm có thể đã được di chuyển hoặc không còn tồn tại.</p>
      <div className="success-actions">
        <Link to="/" className="btn btn-primary btn-lg">Về trang chủ</Link>
        <Link to="/san-pham" className="btn btn-outline btn-lg">Xem sản phẩm <Icon name="arrowRight" size={18} /></Link>
      </div>
    </div>
  );
}
