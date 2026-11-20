import { Link } from 'react-router-dom';
import Breadcrumb from '../components/ui/Breadcrumb.jsx';
import Icon from '../components/ui/Icon.jsx';
import useCaiDat, { dinhDangSoDienThoai } from '../hooks/useCaiDat.js';

/** Thông tin liên hệ lấy từ cài đặt cửa hàng (API /cai-dat). */
export default function ContactPage() {
  const caiDat = useCaiDat();
  const hotline = caiDat.hotline;
  const email = caiDat.email_lien_he;

  const kenh = [
    {
      icon: 'phone',
      tieuDe: 'Hotline',
      moTa: 'Tư vấn sản phẩm và hỗ trợ đơn hàng',
      giaTri: dinhDangSoDienThoai(hotline),
      href: `tel:${hotline.replace(/\s/g, '')}`,
      nhan: 'Gọi ngay',
    },
    {
      icon: 'mail',
      tieuDe: 'Email',
      moTa: 'Gửi yêu cầu bảo hành, đổi trả kèm mã đơn hàng',
      giaTri: email,
      href: `mailto:${email}`,
      nhan: 'Gửi email',
    },
    {
      icon: 'package',
      tieuDe: 'Tra cứu đơn hàng',
      moTa: 'Xem trạng thái đơn bằng mã đơn và email hoặc số điện thoại',
      giaTri: 'Theo dõi trực tuyến',
      to: '/tra-cuu-don-hang',
      nhan: 'Tra cứu',
    },
  ];

  return (
    <div className="contact-page">
      <Breadcrumb items={[{ nhan: 'Liên hệ' }]} />
      <header className="page-hero page-hero-compact">
        <h1>Hỗ trợ 24/7</h1>
        <p className="lead">Liên hệ {caiDat.ten_cua_hang} qua các kênh dưới đây.</p>
      </header>

      <div className="contact-grid">
        {kenh.map((k) => {
          const noiDung = (
            <>
              <span className="contact-icon"><Icon name={k.icon} size={24} /></span>
              <h2>{k.tieuDe}</h2>
              <p className="muted">{k.moTa}</p>
              <strong className="contact-value">{k.giaTri}</strong>
              <span className="link-arrow">{k.nhan} <Icon name="arrowRight" size={16} /></span>
            </>
          );
          return k.to
            ? <Link key={k.tieuDe} to={k.to} className="contact-card">{noiDung}</Link>
            : <a key={k.tieuDe} href={k.href} className="contact-card">{noiDung}</a>;
        })}
      </div>

      <section className="policy-contact contact-cta">
        <div>
          <h2>Tìm câu trả lời nhanh hơn</h2>
          <p>Xem chính sách thanh toán, vận chuyển, bảo hành và đổi trả.</p>
        </div>
        <Link to="/chinh-sach" className="btn btn-light">Trung tâm hỗ trợ <Icon name="arrowRight" size={18} /></Link>
      </section>
    </div>
  );
}
