import Modal from './Modal.jsx';

export default function ConfirmDialog({ mo, tieuDe, noiDung, nhanXacNhan = 'Xóa', nguyHiem = false, onXacNhan, onHuy, dangXuLy = false }) {
  return (
    <Modal tieuDe={tieuDe} mo={mo} onClose={dangXuLy ? () => {} : onHuy}>
      <p className="modal-text">{noiDung}</p>
      <div className="modal-actions">
        <button type="button" className="btn btn-outline" onClick={onHuy} disabled={dangXuLy}>Hủy</button>
        <button type="button" className={`btn ${nguyHiem ? 'btn-danger' : 'btn-primary'}`} onClick={onXacNhan} disabled={dangXuLy}>
          {dangXuLy ? 'Đang xử lý...' : nhanXacNhan}
        </button>
      </div>
    </Modal>
  );
}
