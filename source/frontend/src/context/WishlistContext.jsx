import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { wishlistApi } from '../api/customerApi';
import { layThongBaoLoi } from '../api/axiosClient';
import { useAuth } from './AuthContext.jsx';

const WishlistContext = createContext(null);

/**
 * Giữ tập id sản phẩm yêu thích của người dùng (1 request khi đăng nhập)
 * để mọi thẻ sản phẩm hiển thị đúng trạng thái trái tim mà không gọi API từng thẻ.
 */
export function WishlistProvider({ children }) {
  const { nguoiDung } = useAuth();
  const [ids, setIds] = useState(() => new Set());

  useEffect(() => {
    if (!nguoiDung) {
      setIds(new Set());
      return;
    }
    wishlistApi.layDanhSach()
      .then((ds) => setIds(new Set(ds.map((sp) => sp.id))))
      .catch(() => setIds(new Set()));
  }, [nguoiDung]);

  /** Đổi trạng thái; trả về trạng thái mới hoặc ném lỗi tiếng Việt. */
  const doiYeuThich = useCallback(async (sanPhamId) => {
    const dangThich = ids.has(sanPhamId);
    try {
      // API trả 204 không có nội dung: trạng thái mới là đảo của trạng thái cũ
      if (dangThich) await wishlistApi.xoa(sanPhamId);
      else await wishlistApi.them(sanPhamId);
      setIds((cu) => {
        const moi = new Set(cu);
        if (dangThich) moi.delete(sanPhamId);
        else moi.add(sanPhamId);
        return moi;
      });
      return !dangThich;
    } catch (error) {
      throw new Error(layThongBaoLoi(error));
    }
  }, [ids]);

  const giaTri = useMemo(() => ({
    laYeuThich: (id) => ids.has(id),
    soLuong: ids.size,
    doiYeuThich,
  }), [ids, doiYeuThich]);

  return <WishlistContext.Provider value={giaTri}>{children}</WishlistContext.Provider>;
}

export function useWishlist() {
  return useContext(WishlistContext);
}
