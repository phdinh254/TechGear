import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { wishlistApi } from '../api/customerApi';
import { layThongBaoLoi } from '../api/axiosClient';
import ErrorMessage from '../components/ErrorMessage.jsx';
import Loading from '../components/Loading.jsx';
import ProductCard from '../components/ProductCard.jsx';

export default function WishlistPage() {
  const [sanPhams, setSanPhams] = useState(null);
  const [loi, setLoi] = useState('');
  useEffect(() => { wishlistApi.layDanhSach().then(setSanPhams).catch((error) => setLoi(layThongBaoLoi(error))); }, []);
  const xoa = async (id) => {
    try { await wishlistApi.xoa(id); setSanPhams((cu) => cu.filter((item) => item.id !== id)); }
    catch (error) { setLoi(layThongBaoLoi(error)); }
  };
  return <div><div className="page-heading"><h1>Sản phẩm yêu thích</h1><Link to="/tai-khoan">Quay lại tài khoản</Link></div><ErrorMessage>{loi}</ErrorMessage>{!sanPhams && !loi && <Loading />}{sanPhams?.length === 0 && <p className="muted">Danh sách yêu thích đang trống.</p>}<div className="product-grid">{sanPhams?.map((sp) => <div className="wishlist-item" key={sp.id}><ProductCard sanPham={sp} /><button type="button" onClick={() => xoa(sp.id)}>Bỏ yêu thích</button></div>)}</div></div>;
}
