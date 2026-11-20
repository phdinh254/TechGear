import { Link } from 'react-router-dom';
import Icon from './Icon.jsx';

/** items: [{ nhan, to? }] – mục cuối là trang hiện tại. */
export default function Breadcrumb({ items }) {
  return (
    <nav className="breadcrumb" aria-label="Đường dẫn">
      <ol>
        <li><Link to="/">Trang chủ</Link></li>
        {items.map((item, i) => (
          <li key={`${item.nhan}-${i}`}>
            <Icon name="chevronRight" size={14} />
            {item.to && i < items.length - 1 ? (
              <Link to={item.to}>{item.nhan}</Link>
            ) : (
              <span aria-current="page">{item.nhan}</span>
            )}
          </li>
        ))}
      </ol>
    </nav>
  );
}
