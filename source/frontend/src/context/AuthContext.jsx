import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import authApi from '../api/authApi';
import { TOKEN_KEY } from '../api/axiosClient';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [nguoiDung, setNguoiDung] = useState(null);
  const [dangTai, setDangTai] = useState(true);

  // Khi tải trang: nếu còn token thì lấy lại thông tin người dùng
  useEffect(() => {
    if (!localStorage.getItem(TOKEN_KEY)) {
      setDangTai(false);
      return;
    }
    authApi
      .layThongTin()
      .then(setNguoiDung)
      .catch(() => localStorage.removeItem(TOKEN_KEY))
      .finally(() => setDangTai(false));
  }, []);

  const luuPhien = useCallback((phanHoi) => {
    localStorage.setItem(TOKEN_KEY, phanHoi.token);
    setNguoiDung(phanHoi.nguoiDung);
  }, []);

  const dangNhap = useCallback(
    async (email, matKhau) => luuPhien(await authApi.dangNhap(email, matKhau)),
    [luuPhien],
  );

  const dangKy = useCallback(async (duLieu) => luuPhien(await authApi.dangKy(duLieu)), [luuPhien]);

  const dangXuat = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY);
    setNguoiDung(null);
  }, []);

  const giaTri = useMemo(
    () => ({
      nguoiDung,
      dangTai,
      laQuanTri: nguoiDung?.vaiTro === 'quan_tri',
      dangNhap,
      dangKy,
      dangXuat,
    }),
    [nguoiDung, dangTai, dangNhap, dangKy, dangXuat],
  );

  return <AuthContext.Provider value={giaTri}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
