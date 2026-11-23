import { Link } from 'react-router-dom';
import useCaiDat, { dinhDangSoDienThoai } from '../hooks/useCaiDat.js';
import { useDanhMuc } from '../hooks/useCatalog.js';
import Icon from './ui/Icon.jsx';

export default function Footer() {
  const caiDat = useCaiDat();
  const [danhMucs] = useDanhMuc();
  const tenCuaHang = caiDat.ten_cua_hang;
  const hotline = caiDat.hotline;
  const email = caiDat.email_lien_he;

  return (
    <footer className="footer">
      <div className="container">
        <div className="footer-top">
          <div className="footer-brand-col">
            <Link to="/" className="footer-logo" aria-label={`${tenCuaHang} – Trang chủ`}>
              Tech<span>Gear</span>
            </Link>
            <p>Phụ kiện công nghệ được chọn lọc cho công việc, giải trí và mọi kết nối mỗi ngày.</p>
            <ul className="footer-contact">
              <li><Icon name="phone" size={18} /><a href={`tel:${hotline.replace(/\s/g, '')}`}>{dinhDangSoDienThoai(hotline)}</a></li>
              <li><Icon name="mail" size={18} /><a href={`mailto:${email}`}>{email}</a></li>
            </ul>
          </div>

          <nav className="footer-col" aria-label="Danh mục sản phẩm">
            <h2>Sản phẩm</h2>
            {danhMucs.length > 0 ? (
              danhMucs.map((dm) => <Link key={dm.id} to={`/san-pham?danhMuc=${dm.slug}`}>{dm.ten}</Link>)
            ) : (
              <Link to="/san-pham">Tất cả sản phẩm</Link>
            )}
          </nav>

          <nav className="footer-col" aria-label="Hỗ trợ khách hàng">
            <h2>Hỗ trợ</h2>
            <Link to="/tra-cuu-don-hang">Tra cứu đơn hàng</Link>
            <Link to="/tai-khoan/don-hang">Đơn hàng của tôi</Link>
            <Link to="/tai-khoan">Tài khoản</Link>
            <Link to="/gio-hang">Giỏ hàng</Link>
            <Link to="/lien-he">Liên hệ</Link>
          </nav>

          <nav className="footer-col" aria-label="Chính sách">
            <h2>Chính sách</h2>
            <Link to="/chinh-sach#mua-hang">Chính sách mua hàng</Link>
            <Link to="/chinh-sach#van-chuyen">Vận chuyển</Link>
            <Link to="/chinh-sach#bao-hanh">Bảo hành</Link>
            <Link to="/chinh-sach#doi-tra">Đổi trả</Link>
          </nav>
        </div>

        <div className="footer-bottom">
          <span>© {new Date().getFullYear()} {tenCuaHang}. Bảo lưu mọi quyền.</span>
          <div className="footer-pay" aria-label="Phương thức thanh toán">
            <span>Thanh toán</span>
            <span className="pay-chip"><Icon name="wallet" size={16} /> COD</span>
            <span className="pay-chip"><Icon name="bolt" size={16} /> PayOS</span>
          </div>
        </div>
      </div>
    </footer>
  );
}
