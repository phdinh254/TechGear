import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { wishlistApi } from '../api/customerApi';
import { layThongBaoLoi } from '../api/axiosClient';
import AccountShell from '../components/AccountShell.jsx';
import ErrorMessage from '../components/ErrorMessage.jsx';
import ProductCard from '../components/ProductCard.jsx';
import EmptyState from '../components/ui/EmptyState.jsx';
import { ProductGridSkeleton } from '../components/ui/Skeleton.jsx';
import { useWishlist } from '../context/WishlistContext.jsx';

export default function WishlistPage() {
  const [sanPhams, setSanPhams] = useState(null);
  const [loi, setLoi] = useState('');
  const { laYeuThich } = useWishlist();

  useEffect(() => {
    wishlistApi.layDanhSach().then(setSanPhams).catch((error) => setLoi(layThongBaoLoi(error)));
  }, []);

  // Bỏ tim ngay trên thẻ thì ẩn sản phẩm khỏi danh sách
  const hienThi = sanPhams?.filter((sp) => laYeuThich(sp.id)) ?? null;

  return (
    <AccountShell>
      <div className="panel">
        <h1 className="panel-title">Sản phẩm yêu thích {hienThi?.length > 0 && <span className="muted">({hienThi.length})</span>}</h1>
        <ErrorMessage>{loi}</ErrorMessage>
        {!hienThi && !loi && <div className="product-grid product-grid-3"><ProductGridSkeleton soLuong={3} /></div>}
        {hienThi?.length === 0 && (
          <EmptyState compact icon="heart" tieuDe="Chưa có sản phẩm yêu thích" moTa="Nhấn biểu tượng trái tim trên sản phẩm để lưu lại và xem sau.">
            <Link to="/san-pham" className="btn btn-primary">Khám phá sản phẩm</Link>
          </EmptyState>
        )}
        {hienThi?.length > 0 && (
          <div className="product-grid product-grid-3">
            {hienThi.map((sp) => <ProductCard key={sp.id} sanPham={sp} />)}
          </div>
        )}
      </div>
    </AccountShell>
  );
}
