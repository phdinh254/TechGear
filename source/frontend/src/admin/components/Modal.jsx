import { useEffect } from 'react';
import Icon from '../../components/ui/Icon.jsx';

export default function Modal({ tieuDe, mo, onClose, rong = false, children }) {
  useEffect(() => {
    if (!mo) return undefined;
    const onKey = (e) => { if (e.key === 'Escape') onClose(); };
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [mo, onClose]);

  if (!mo) return null;
  return (
    <div className="modal-backdrop" onMouseDown={(e) => { if (e.target === e.currentTarget) onClose(); }}>
      <div className={`modal${rong ? ' modal--wide' : ''}`} role="dialog" aria-modal="true" aria-label={tieuDe}>
        <div className="modal-head">
          <h2>{tieuDe}</h2>
          <button type="button" className="icon-btn" aria-label="Đóng" onClick={onClose}><Icon name="close" size={20} /></button>
        </div>
        <div className="modal-body">{children}</div>
      </div>
    </div>
  );
}
