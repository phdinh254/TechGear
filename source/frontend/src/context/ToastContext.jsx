import { createContext, useCallback, useContext, useMemo, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import Icon from '../components/ui/Icon.jsx';

const ToastContext = createContext(null);

/** Thông báo nhỏ góc màn hình. loai: 'success' | 'error' | 'info'; hanhDong: { nhan, to }. */
export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);
  const dem = useRef(0);

  const dong = useCallback((id) => setToasts((ds) => ds.filter((t) => t.id !== id)), []);

  const hien = useCallback((noiDung, { loai = 'success', hanhDong, thoiGian = 3200 } = {}) => {
    dem.current += 1;
    const id = dem.current;
    setToasts((ds) => [...ds.slice(-2), { id, noiDung, loai, hanhDong }]);
    window.setTimeout(() => dong(id), thoiGian);
  }, [dong]);

  const giaTri = useMemo(() => ({ hien, dong }), [hien, dong]);

  return (
    <ToastContext.Provider value={giaTri}>
      {children}
      <div className="toast-region" role="status" aria-live="polite">
        {toasts.map((t) => (
          <div key={t.id} className={`toast toast-${t.loai}`}>
            <span className="toast-icon"><Icon name={t.loai === 'error' ? 'close' : t.loai === 'info' ? 'sparkle' : 'check'} size={16} strokeWidth={2.2} /></span>
            <span className="toast-text">{t.noiDung}</span>
            {t.hanhDong && <Link className="toast-action" to={t.hanhDong.to} onClick={() => dong(t.id)}>{t.hanhDong.nhan}</Link>}
            <button type="button" className="toast-close" onClick={() => dong(t.id)} aria-label="Đóng thông báo"><Icon name="close" size={14} /></button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  return useContext(ToastContext);
}
