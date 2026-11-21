import { useRef, useState } from 'react';
import adminApi from '../api/adminApi.js';
import ErrorMessage from '../../components/ErrorMessage.jsx';
import { duongDanAnh } from '../../utils/format.js';
import { layThongBaoLoi } from '../../api/axiosClient.js';

const TOI_DA = 5 * 1024 * 1024;

export default function ImageUploader({ thuMuc, giaTri, onChange }) {
  const inputRef = useRef(null);
  const [dangTai, setDangTai] = useState(false);
  const [loi, setLoi] = useState('');

  const chonFile = async (e) => {
    const file = e.target.files?.[0];
    e.target.value = '';
    if (!file) return;
    if (!file.type.startsWith('image/')) { setLoi('Chỉ chấp nhận file ảnh'); return; }
    if (file.size > TOI_DA) { setLoi('Ảnh tối đa 5MB'); return; }
    setLoi('');
    setDangTai(true);
    try {
      onChange(await adminApi.taiAnh(file, thuMuc));
    } catch (err) {
      setLoi(layThongBaoLoi(err));
    } finally {
      setDangTai(false);
    }
  };

  return (
    <div className="image-uploader">
      <div className="image-uploader-row">
        {giaTri ? <img className="image-uploader-preview" src={duongDanAnh(giaTri)} alt="Xem trước" /> : <div className="image-uploader-preview image-uploader-empty">Chưa có ảnh</div>}
        <div className="image-uploader-actions">
          <button type="button" className="btn btn-outline" onClick={() => inputRef.current?.click()} disabled={dangTai}>
            {dangTai ? 'Đang tải...' : giaTri ? 'Đổi ảnh' : 'Chọn ảnh'}
          </button>
          {giaTri && !dangTai && <button type="button" className="btn btn-outline" onClick={() => onChange(null)}>Xóa ảnh</button>}
        </div>
      </div>
      <input ref={inputRef} type="file" accept="image/*" hidden onChange={chonFile} />
      <ErrorMessage>{loi}</ErrorMessage>
    </div>
  );
}
