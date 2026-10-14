import { createContext, useContext, useEffect, useMemo, useState } from 'react';

/**
 * Giỏ hàng tạm lưu ở localStorage (dùng được cả khi chưa đăng nhập).
 * Mỗi dòng: { bienTheId, sanPhamSlug, ten, phanLoai, giaBan, anh, soLuong, tonKho }
 * Bước sau: khi đăng nhập sẽ đồng bộ lên API giỏ hàng của backend.
 */
const CART_KEY = 'techgear_cart';
const CartContext = createContext(null);

function docGioHang() {
  try {
    return JSON.parse(localStorage.getItem(CART_KEY)) || [];
  } catch {
    return [];
  }
}

export function CartProvider({ children }) {
  const [dong, setDong] = useState(docGioHang);

  useEffect(() => {
    localStorage.setItem(CART_KEY, JSON.stringify(dong));
  }, [dong]);

  const giaTri = useMemo(() => {
    const themVaoGio = (sanPham, soLuong = 1) =>
      setDong((cu) => {
        const daCo = cu.find((d) => d.bienTheId === sanPham.bienTheId);
        if (daCo) {
          return cu.map((d) =>
            d.bienTheId === sanPham.bienTheId
              ? { ...d, soLuong: Math.min(d.soLuong + soLuong, d.tonKho) }
              : d,
          );
        }
        return [...cu, { ...sanPham, soLuong: Math.min(soLuong, sanPham.tonKho) }];
      });

    const capNhatSoLuong = (bienTheId, soLuong) =>
      setDong((cu) =>
        cu.map((d) =>
          d.bienTheId === bienTheId ? { ...d, soLuong: Math.max(1, Math.min(soLuong, d.tonKho)) } : d,
        ),
      );

    const xoaKhoiGio = (bienTheId) => setDong((cu) => cu.filter((d) => d.bienTheId !== bienTheId));
    const xoaHet = () => setDong([]);

    return {
      dong,
      tongSoLuong: dong.reduce((t, d) => t + d.soLuong, 0),
      tamTinh: dong.reduce((t, d) => t + Number(d.giaBan) * d.soLuong, 0),
      themVaoGio,
      capNhatSoLuong,
      xoaKhoiGio,
      xoaHet,
    };
  }, [dong]);

  return <CartContext.Provider value={giaTri}>{children}</CartContext.Provider>;
}

export function useCart() {
  return useContext(CartContext);
}
