import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { orderApi, reviewApi } from '../api/customerApi';
import { layThongBaoLoi } from '../api/axiosClient';
import AccountShell from '../components/AccountShell.jsx';
import ErrorMessage from '../components/ErrorMessage.jsx';
import OrderView from '../components/OrderView.jsx';
import Icon from '../components/ui/Icon.jsx';
import { SkeletonBlock } from '../components/ui/Skeleton.jsx';
import { useToast } from '../context/ToastContext.jsx';

function ReviewForm({ item, donHangId }) {
  const toast = useToast();
  const [soSao, setSoSao] = useState(5);
  const [noiDung, setNoiDung] = useState('');
  const [daGui, setDaGui] = useState(false);
  const [dangGui, setDangGui] = useState(false);

  const gui = async () => {
    setDangGui(true);
    try {
      await reviewApi.tao({ donHangId, sanPhamId: item.sanPhamId, soSao, noiDung });
      setDaGui(true);
      toast.hien('Cảm ơn bạn! Đánh giá đang chờ duyệt');
    } catch (error) {
      toast.hien(layThongBaoLoi(error), { loai: 'error' });
    } finally {
      setDangGui(false);
    }
  };

  if (daGui) return <p className="review-done"><Icon name="check" size={16} /> Đã gửi đánh giá</p>;

  return (
    <div className="review-form">
      <div className="star-picker" role="radiogroup" aria-label="Số sao">
        {[1, 2, 3, 4, 5].map((sao) => (
          <button type="button" key={sao} role="radio" aria-checked={soSao === sao} aria-label={`${sao} sao`} className={sao <= soSao ? 'is-on' : undefined} onClick={() => setSoSao(sao)}>★</button>
        ))}
      </div>
      <input placeholder="Chia sẻ cảm nhận của bạn" maxLength="2000" value={noiDung} onChange={(e) => setNoiDung(e.target.value)} aria-label="Nội dung đánh giá" />
      <button type="button" className="btn btn-sm" onClick={gui} disabled={dangGui}>Gửi</button>
    </div>
  );
}

export default function OrderDetailPage() {
  const { maDonHang } = useParams();
  const toast = useToast();
  const [donHang, setDonHang] = useState(null);
  const [loi, setLoi] = useState('');
  const [dangHuy, setDangHuy] = useState(false);

  useEffect(() => {
    setDonHang(null);
    setLoi('');
    orderApi.layChiTiet(maDonHang).then(setDonHang).catch((error) => setLoi(layThongBaoLoi(error)));
  }, [maDonHang]);

  const huyDon = async () => {
    const lyDo = window.prompt('Lý do hủy đơn (không bắt buộc):', '');
    if (lyDo === null) return;
    setDangHuy(true);
    try {
      setDonHang(await orderApi.huy(maDonHang, lyDo));
      toast.hien('Đã hủy đơn hàng');
    } catch (error) {
      toast.hien(layThongBaoLoi(error), { loai: 'error' });
    } finally {
      setDangHuy(false);
    }
  };

  return (
    <AccountShell>
      <Link to="/tai-khoan/don-hang" className="link-arrow back-link"><Icon name="chevronLeft" size={16} /> Tất cả đơn hàng</Link>
      <ErrorMessage>{loi}</ErrorMessage>
      {!donHang && !loi && <SkeletonBlock height={320} />}
      {donHang && (
        <OrderView
          donHang={donHang}
          renderItemExtra={(item) => donHang.trangThai === 'hoan_thanh' && item.sanPhamId && <ReviewForm item={item} donHangId={donHang.id} />}
          hanhDong={donHang.trangThai === 'cho_xac_nhan' && (
            <button type="button" className="btn btn-outline btn-block btn-cancel" onClick={huyDon} disabled={dangHuy}>
              {dangHuy ? 'Đang hủy...' : 'Hủy đơn hàng'}
            </button>
          )}
        />
      )}
    </AccountShell>
  );
}
