import { Link } from 'react-router-dom';

const productLinks = [
  { label: 'Tai nghe', slug: 'tai-nghe' },
  { label: 'Củ sạc', slug: 'cu-sac' },
  { label: 'Cáp sạc', slug: 'cap-sac' },
  { label: 'Sạc dự phòng', slug: 'sac-du-phong' },
  { label: 'Phụ kiện máy tính', slug: 'phu-kien-may-tinh' },
];

function Icon({ name }) {
  const paths = {
    truck: <><path d="M3 7h11v10H3zM14 10h4l3 3v4h-7z" /><circle cx="7" cy="18" r="2" /><circle cx="18" cy="18" r="2" /></>,
    shield: <path d="M12 2 4 6v6c0 5 3.5 8.5 8 10 4.5-1.5 8-5 8-10V6z" />,
    card: <><rect x="2" y="5" width="20" height="14" rx="2" /><path d="M2 10h20" /></>,
    headset: <><path d="M3 14v-2a9 9 0 0 1 18 0v2" /><rect x="3" y="13" width="4" height="7" rx="2" /><rect x="17" y="13" width="4" height="7" rx="2" /></>,
  };

  return (
    <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      {paths[name]}
    </svg>
  );
}

const benefits = [
  { icon: 'truck', title: 'Giao hàng nhanh', detail: 'Giao hàng toàn quốc' },
  { icon: 'shield', title: 'Bảo hành chính hãng', detail: 'Thông tin rõ ràng' },
  { icon: 'card', title: 'Thanh toán tiện lợi', detail: 'COD khi nhận hàng' },
  { icon: 'headset', title: 'Hỗ trợ khách hàng', detail: 'Luôn sẵn sàng' },
];

export default function Footer() {
  return (
    <footer className="footer">
      <div className="container footer-inner">
        <div className="footer-main">
          <div className="footer-about">
            <Link to="/" className="footer-brand" aria-label="TechGear - Trang chủ">
              <span className="footer-brand-mark">T</span>
              <span>TechGear</span>
            </Link>
            <p>Phụ kiện công nghệ cho trải nghiệm mỗi ngày của bạn.</p>
          </div>

          <nav className="footer-column" aria-label="Danh mục sản phẩm">
            <h2>Sản phẩm</h2>
            {productLinks.map(({ label, slug }) => (
              <Link key={slug} to={`/san-pham?danhMuc=${slug}`}>{label}</Link>
            ))}
          </nav>

          <nav id="ho-tro" className="footer-column" aria-label="Hỗ trợ khách hàng">
            <h2>Hỗ trợ</h2>
            <Link to="/tai-khoan/don-hang">Theo dõi đơn hàng</Link>
            <Link to="/tai-khoan">Tài khoản của tôi</Link>
            <Link to="/gio-hang">Giỏ hàng</Link>
            <a href="mailto:hotro@techgear.vn">Liên hệ hỗ trợ</a>
          </nav>

          <div id="lien-he" className="footer-column footer-contact">
            <h2>Liên hệ</h2>
            <a href="tel:0900000000">Hotline: 0900 000 000</a>
            <a href="mailto:hotro@techgear.vn">Email: hotro@techgear.vn</a>
            <p>TechGear · Phụ kiện công nghệ</p>
          </div>
        </div>

        <div className="footer-benefits" aria-label="Cam kết dịch vụ">
          {benefits.map(({ icon, title, detail }) => (
            <div className="footer-benefit" key={icon}>
              <Icon name={icon} />
              <div><strong>{title}</strong><span>{detail}</span></div>
            </div>
          ))}
        </div>

        <div className="footer-bottom">
          <span>© {new Date().getFullYear()} TechGear. Bảo lưu mọi quyền.</span>
          <span>Website thương mại điện tử phụ kiện công nghệ</span>
        </div>
      </div>
    </footer>
  );
}
