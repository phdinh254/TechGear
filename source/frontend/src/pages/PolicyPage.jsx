import { Link } from 'react-router-dom';
import Breadcrumb from '../components/ui/Breadcrumb.jsx';
import Icon from '../components/ui/Icon.jsx';
import useCaiDat, { dinhDangSoDienThoai } from '../hooks/useCaiDat.js';
import { formatTien } from '../utils/format';

export default function PolicyPage() {
  const caiDat = useCaiDat();
  const phi = Number(caiDat.phi_van_chuyen);
  const nguong = Number(caiDat.mien_phi_van_chuyen_tu);

  const muc = [
    {
      id: 'mua-hang',
      icon: 'bag',
      tieuDe: 'Mua hàng & thanh toán',
      noiDung: [
        'Đặt hàng trực tuyến không cần tài khoản; đăng nhập để lưu địa chỉ, theo dõi đơn và đánh giá sản phẩm.',
        'Thanh toán khi nhận hàng (COD). Bạn kiểm tra sản phẩm trước khi thanh toán cho nhân viên giao hàng.',
        'Có thể hủy đơn khi đơn còn ở trạng thái "Chờ xác nhận".',
      ],
    },
    {
      id: 'van-chuyen',
      icon: 'truck',
      tieuDe: 'Vận chuyển',
      noiDung: [
        phi > 0 ? `Phí giao hàng tiêu chuẩn ${formatTien(phi)} mỗi đơn.` : 'Phí giao hàng hiển thị ở bước thanh toán.',
        nguong > 0 ? `Miễn phí vận chuyển cho đơn hàng từ ${formatTien(nguong)}.` : null,
        'Theo dõi trạng thái đơn trong mục Đơn hàng của tôi hoặc trang Tra cứu đơn hàng.',
      ].filter(Boolean),
    },
    {
      id: 'bao-hanh',
      icon: 'shield',
      tieuDe: 'Bảo hành',
      noiDung: [
        'Thời gian bảo hành của từng sản phẩm được ghi rõ trên trang chi tiết sản phẩm.',
        'Giữ lại mã đơn hàng để được hỗ trợ bảo hành nhanh hơn.',
      ],
    },
    {
      id: 'doi-tra',
      icon: 'refresh',
      tieuDe: 'Đổi trả',
      noiDung: [
        'Liên hệ bộ phận hỗ trợ kèm mã đơn hàng khi sản phẩm lỗi kỹ thuật hoặc giao sai.',
        'Sản phẩm đổi trả cần còn đầy đủ phụ kiện và hộp đi kèm.',
      ],
    },
    {
      id: 'dieu-khoan',
      icon: 'package',
      tieuDe: 'Điều khoản sử dụng',
      noiDung: [
        'Bạn chịu trách nhiệm về tính chính xác của thông tin tài khoản và thông tin giao hàng đã cung cấp.',
        'Giá và tình trạng còn hàng hiển thị tại thời điểm đặt hàng; đơn hàng được xác nhận sau khi cửa hàng liên hệ.',
        'Đánh giá sản phẩm cần đúng sự thật và được duyệt trước khi hiển thị công khai.',
      ],
    },
    {
      id: 'bao-mat',
      icon: 'lock',
      tieuDe: 'Chính sách bảo mật',
      noiDung: [
        'Chúng tôi chỉ thu thập họ tên, email, số điện thoại và địa chỉ để xử lý đơn hàng và hỗ trợ khách hàng.',
        'Mật khẩu được mã hóa một chiều; cửa hàng không lưu thông tin thẻ thanh toán.',
        'Thông tin cá nhân không được chia sẻ cho bên thứ ba ngoài đơn vị vận chuyển phục vụ giao hàng.',
      ],
    },
  ];

  return (
    <div className="policy-page">
      <Breadcrumb items={[{ nhan: 'Chính sách' }]} />
      <header className="page-hero page-hero-compact">
        <h1>Chính sách mua hàng</h1>
        <p className="lead">Thông tin về thanh toán, vận chuyển, bảo hành và đổi trả tại {caiDat.ten_cua_hang}.</p>
      </header>

      <div className="policy-layout">
        <nav className="policy-toc" aria-label="Mục lục">
          {muc.map((m) => <a key={m.id} href={`#${m.id}`}>{m.tieuDe}</a>)}
        </nav>
        <div className="policy-sections">
          {muc.map((m) => (
            <section key={m.id} id={m.id} className="policy-section">
              <span className="policy-icon"><Icon name={m.icon} size={22} /></span>
              <div>
                <h2>{m.tieuDe}</h2>
                <ul>{m.noiDung.map((dong) => <li key={dong}>{dong}</li>)}</ul>
              </div>
            </section>
          ))}
          <section className="policy-contact">
            <div>
              <h2>Cần hỗ trợ thêm?</h2>
              <p>Hotline {dinhDangSoDienThoai(caiDat.hotline)} · {caiDat.email_lien_he}</p>
            </div>
            <Link to="/tra-cuu-don-hang" className="btn btn-primary">Tra cứu đơn hàng</Link>
          </section>
        </div>
      </div>
    </div>
  );
}
