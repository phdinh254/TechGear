import { createContext, useContext, useEffect, useMemo, useRef, useState } from 'react';
import cartApi from '../api/cartApi';
import { layThongBaoLoi } from '../api/axiosClient';
import { useAuth } from './AuthContext.jsx';

const CART_KEY = 'techgear_cart';
const CartContext = createContext(null);

function docGioHangKhach() {
  try {
    const duLieu = JSON.parse(localStorage.getItem(CART_KEY));
    return Array.isArray(duLieu) ? duLieu : [];
  } catch {
    return [];
  }
}

function luuGioHangKhach(dong) {
  localStorage.setItem(CART_KEY, JSON.stringify(dong));
}

export function CartProvider({ children }) {
  const { nguoiDung, dangTai: dangTaiDangNhap } = useAuth();
  const [dong, setDong] = useState(docGioHangKhach);
  const [dangDongBo, setDangDongBo] = useState(false);
  const [loiGio, setLoiGio] = useState('');
  const nguoiDungDaTai = useRef(null);

  useEffect(() => {
    if (dangTaiDangNhap) return;

    if (!nguoiDung) {
      const vuaDangXuat = nguoiDungDaTai.current !== null;
      nguoiDungDaTai.current = null;
      if (vuaDangXuat) {
        localStorage.removeItem(CART_KEY);
        setDong([]);
      } else {
        setDong(docGioHangKhach());
      }
      setLoiGio('');
      return;
    }

    if (nguoiDungDaTai.current === nguoiDung.id) return;
    nguoiDungDaTai.current = nguoiDung.id;
    const gioKhach = docGioHangKhach();
    setDangDongBo(true);
    setLoiGio('');
    const request = gioKhach.length > 0
      ? cartApi.gop(gioKhach.map((item) => ({ bienTheId: item.bienTheId, soLuong: item.soLuong })))
      : cartApi.layGioHang();
    request
      .then((response) => {
        setDong(response.dongs);
        localStorage.removeItem(CART_KEY);
      })
      .catch((error) => {
        nguoiDungDaTai.current = null;
        setLoiGio(layThongBaoLoi(error));
      })
      .finally(() => setDangDongBo(false));
  }, [dangTaiDangNhap, nguoiDung]);

  const capNhatGioKhach = (hamCapNhat) => {
    setDong((cu) => {
      const moi = hamCapNhat(cu);
      luuGioHangKhach(moi);
      return moi;
    });
  };

  const xuLyResponse = (promise) => {
    setLoiGio('');
    return promise
      .then((response) => {
        setDong(response.dongs);
        return response;
      })
      .catch((error) => {
        setLoiGio(layThongBaoLoi(error));
        throw error;
      });
  };

  const giaTri = useMemo(() => {
    const themVaoGio = (sanPham, soLuong = 1) => {
      if (nguoiDung) return xuLyResponse(cartApi.them(sanPham.bienTheId, soLuong));
      capNhatGioKhach((cu) => {
        const daCo = cu.find((item) => item.bienTheId === sanPham.bienTheId);
        if (daCo) {
          return cu.map((item) => item.bienTheId === sanPham.bienTheId
            ? { ...item, soLuong: Math.min(item.soLuong + soLuong, item.tonKho) }
            : item);
        }
        return [...cu, { ...sanPham, soLuong: Math.min(soLuong, sanPham.tonKho), conBan: true }];
      });
      return Promise.resolve();
    };

    const capNhatSoLuong = (bienTheId, soLuong) => {
      if (nguoiDung) return xuLyResponse(cartApi.capNhat(bienTheId, soLuong));
      capNhatGioKhach((cu) => cu.map((item) => item.bienTheId === bienTheId
        ? { ...item, soLuong: Math.max(1, Math.min(soLuong, item.tonKho)) }
        : item));
      return Promise.resolve();
    };

    const xoaKhoiGio = (bienTheId) => {
      if (nguoiDung) return xuLyResponse(cartApi.xoa(bienTheId));
      capNhatGioKhach((cu) => cu.filter((item) => item.bienTheId !== bienTheId));
      return Promise.resolve();
    };

    const xoaHet = () => {
      if (nguoiDung) {
        setLoiGio('');
        return cartApi.xoaTatCa().then(() => setDong([]));
      }
      capNhatGioKhach(() => []);
      return Promise.resolve();
    };

    return {
      dong,
      dangDongBo,
      loiGio,
      tongSoLuong: dong.reduce((tong, item) => tong + item.soLuong, 0),
      tamTinh: dong.reduce((tong, item) => tong + Number(item.giaBan) * item.soLuong, 0),
      themVaoGio,
      capNhatSoLuong,
      xoaKhoiGio,
      xoaHet,
    };
  }, [dong, nguoiDung, dangDongBo, loiGio]);

  return <CartContext.Provider value={giaTri}>{children}</CartContext.Provider>;
}

export function useCart() {
  return useContext(CartContext);
}
