import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import accountApi from '../api/accountApi';
import { layThongBaoLoi } from '../api/axiosClient';
import ErrorMessage from '../components/ErrorMessage.jsx';
import Loading from '../components/Loading.jsx';
import { useAuth } from '../context/AuthContext.jsx';

const DIA_CHI_RONG = {
  tenNguoiNhan: '',
  soDienThoai: '',
  tinhThanh: '',
  phuongXa: '',
  diaChiChiTiet: '',
  laMacDinh: false,
};

export default function AccountPage() {
  const { nguoiDung, capNhatNguoiDung } = useAuth();
  const [tab, setTab] = useState('ho-so');
  const [hoSo, setHoSo] = useState({
    hoTen: nguoiDung.hoTen || '',
    soDienThoai: nguoiDung.soDienThoai || '',
    gioiTinh: nguoiDung.gioiTinh || '',
    ngaySinh: nguoiDung.ngaySinh || '',
    anhDaiDien: nguoiDung.anhDaiDien || '',
  });
  const [matKhau, setMatKhau] = useState({ matKhauHienTai: '', matKhauMoi: '', nhapLai: '' });
  const [diaChis, setDiaChis] = useState([]);
  const [diaChiForm, setDiaChiForm] = useState(DIA_CHI_RONG);
  const [diaChiDangSua, setDiaChiDangSua] = useState(null);
  const [dangTaiDiaChi, setDangTaiDiaChi] = useState(true);
  const [dangGui, setDangGui] = useState(false);
  const [loi, setLoi] = useState('');
  const [thongBao, setThongBao] = useState('');

  const taiDiaChi = () => {
    setDangTaiDiaChi(true);
    accountApi.layDiaChi()
      .then(setDiaChis)
      .catch((error) => setLoi(layThongBaoLoi(error)))
      .finally(() => setDangTaiDiaChi(false));
  };

  useEffect(taiDiaChi, []);

  const batDau = () => {
    setLoi('');
    setThongBao('');
    setDangGui(true);
  };

  const luuHoSo = async (event) => {
    event.preventDefault();
    batDau();
    try {
      const moi = await accountApi.capNhatHoSo({
        ...hoSo,
        gioiTinh: hoSo.gioiTinh || null,
        ngaySinh: hoSo.ngaySinh || null,
      });
      capNhatNguoiDung(moi);
      setThongBao('Cập nhật hồ sơ thành công');
    } catch (error) {
      setLoi(layThongBaoLoi(error));
    } finally {
      setDangGui(false);
    }
  };

  const doiMatKhau = async (event) => {
    event.preventDefault();
    setLoi('');
    setThongBao('');
    if (matKhau.matKhauMoi !== matKhau.nhapLai) {
      setLoi('Mật khẩu nhập lại không khớp');
      return;
    }
    setDangGui(true);
    try {
      const { nhapLai, ...duLieu } = matKhau;
      const response = await accountApi.doiMatKhau(duLieu);
      setThongBao(response.thongBao);
      setMatKhau({ matKhauHienTai: '', matKhauMoi: '', nhapLai: '' });
    } catch (error) {
      setLoi(layThongBaoLoi(error));
    } finally {
      setDangGui(false);
    }
  };

  const luuDiaChi = async (event) => {
    event.preventDefault();
    batDau();
    try {
      if (diaChiDangSua) await accountApi.capNhatDiaChi(diaChiDangSua, diaChiForm);
      else await accountApi.taoDiaChi(diaChiForm);
      setDiaChiForm(DIA_CHI_RONG);
      setDiaChiDangSua(null);
      setThongBao(diaChiDangSua ? 'Cập nhật địa chỉ thành công' : 'Thêm địa chỉ thành công');
      taiDiaChi();
    } catch (error) {
      setLoi(layThongBaoLoi(error));
    } finally {
      setDangGui(false);
    }
  };

  const suaDiaChi = (diaChi) => {
    setDiaChiDangSua(diaChi.id);
    setDiaChiForm({ ...diaChi });
    setLoi('');
    setThongBao('');
  };

  const datMacDinh = async (id) => {
    try {
      await accountApi.datDiaChiMacDinh(id);
      taiDiaChi();
    } catch (error) {
      setLoi(layThongBaoLoi(error));
    }
  };

  const xoaDiaChi = async (id) => {
    if (!window.confirm('Bạn có chắc muốn xóa địa chỉ này?')) return;
    try {
      await accountApi.xoaDiaChi(id);
      taiDiaChi();
    } catch (error) {
      setLoi(layThongBaoLoi(error));
    }
  };

  return (
    <div className="account-layout">
      <aside className="account-nav">
        <h2>Tài khoản</h2>
        {[
          ['ho-so', 'Thông tin cá nhân'],
          ['dia-chi', 'Sổ địa chỉ'],
          ['mat-khau', 'Đổi mật khẩu'],
        ].map(([giaTri, nhan]) => (
          <button key={giaTri} type="button" className={tab === giaTri ? 'active' : ''} onClick={() => {
            setTab(giaTri);
            setLoi('');
            setThongBao('');
          }}>
            {nhan}
          </button>
        ))}
        <Link to="/tai-khoan/don-hang">Đơn hàng của tôi</Link>
        <Link to="/tai-khoan/yeu-thich">Sản phẩm yêu thích</Link>
      </aside>

      <section className="account-content">
        <ErrorMessage>{loi}</ErrorMessage>
        {thongBao && <p className="success">{thongBao}</p>}

        {tab === 'ho-so' && (
          <form className="form" onSubmit={luuHoSo}>
            <h1>Thông tin cá nhân</h1>
            <label>Email<input value={nguoiDung.email} disabled /></label>
            <label>Họ và tên<input required maxLength="100" value={hoSo.hoTen} onChange={(e) => setHoSo({ ...hoSo, hoTen: e.target.value })} /></label>
            <label>Số điện thoại<input type="tel" pattern="0[0-9]{9}" value={hoSo.soDienThoai} onChange={(e) => setHoSo({ ...hoSo, soDienThoai: e.target.value })} /></label>
            <label>Giới tính<select value={hoSo.gioiTinh} onChange={(e) => setHoSo({ ...hoSo, gioiTinh: e.target.value })}>
              <option value="">Chưa chọn</option><option value="nam">Nam</option><option value="nu">Nữ</option><option value="khac">Khác</option>
            </select></label>
            <label>Ngày sinh<input type="date" max={new Date().toISOString().slice(0, 10)} value={hoSo.ngaySinh} onChange={(e) => setHoSo({ ...hoSo, ngaySinh: e.target.value })} /></label>
            <label>Ảnh đại diện (đường dẫn)<input maxLength="255" value={hoSo.anhDaiDien} onChange={(e) => setHoSo({ ...hoSo, anhDaiDien: e.target.value })} /></label>
            <button className="btn" disabled={dangGui}>Lưu thay đổi</button>
          </form>
        )}

        {tab === 'mat-khau' && (
          <form className="form" onSubmit={doiMatKhau}>
            <h1>Đổi mật khẩu</h1>
            <label>Mật khẩu hiện tại<input type="password" required autoComplete="current-password" value={matKhau.matKhauHienTai} onChange={(e) => setMatKhau({ ...matKhau, matKhauHienTai: e.target.value })} /></label>
            <label>Mật khẩu mới<input type="password" required minLength="8" maxLength="100" pattern="(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]).{8,100}" autoComplete="new-password" value={matKhau.matKhauMoi} onChange={(e) => setMatKhau({ ...matKhau, matKhauMoi: e.target.value })} /></label>
            <label>Nhập lại mật khẩu<input type="password" required minLength="8" maxLength="100" autoComplete="new-password" value={matKhau.nhapLai} onChange={(e) => setMatKhau({ ...matKhau, nhapLai: e.target.value })} /></label>
            <button className="btn" disabled={dangGui}>Đổi mật khẩu</button>
          </form>
        )}

        {tab === 'dia-chi' && (
          <div>
            <h1>Sổ địa chỉ</h1>
            {dangTaiDiaChi ? <Loading /> : (
              <div className="address-list">
                {diaChis.map((diaChi) => (
                  <article key={diaChi.id} className="address-card">
                    <div><strong>{diaChi.tenNguoiNhan}</strong> · {diaChi.soDienThoai}{diaChi.laMacDinh && <span className="default-tag">Mặc định</span>}</div>
                    <p>{diaChi.diaChiChiTiet}, {diaChi.phuongXa}, {diaChi.tinhThanh}</p>
                    <div className="inline-actions">
                      <button type="button" onClick={() => suaDiaChi(diaChi)}>Sửa</button>
                      {!diaChi.laMacDinh && <button type="button" onClick={() => datMacDinh(diaChi.id)}>Đặt mặc định</button>}
                      <button type="button" className="danger-link" onClick={() => xoaDiaChi(diaChi.id)}>Xóa</button>
                    </div>
                  </article>
                ))}
                {diaChis.length === 0 && <p className="muted">Bạn chưa lưu địa chỉ giao hàng.</p>}
              </div>
            )}

            <form className="form address-form" onSubmit={luuDiaChi}>
              <h2>{diaChiDangSua ? 'Sửa địa chỉ' : 'Thêm địa chỉ'}</h2>
              <label>Tên người nhận<input required maxLength="100" value={diaChiForm.tenNguoiNhan} onChange={(e) => setDiaChiForm({ ...diaChiForm, tenNguoiNhan: e.target.value })} /></label>
              <label>Số điện thoại<input required type="tel" pattern="0[0-9]{9}" value={diaChiForm.soDienThoai} onChange={(e) => setDiaChiForm({ ...diaChiForm, soDienThoai: e.target.value })} /></label>
              <label>Tỉnh/thành<input required maxLength="100" value={diaChiForm.tinhThanh} onChange={(e) => setDiaChiForm({ ...diaChiForm, tinhThanh: e.target.value })} /></label>
              <label>Phường/xã<input required maxLength="100" value={diaChiForm.phuongXa} onChange={(e) => setDiaChiForm({ ...diaChiForm, phuongXa: e.target.value })} /></label>
              <label>Địa chỉ chi tiết<input required maxLength="255" value={diaChiForm.diaChiChiTiet} onChange={(e) => setDiaChiForm({ ...diaChiForm, diaChiChiTiet: e.target.value })} /></label>
              <label className="checkbox-label"><input type="checkbox" checked={diaChiForm.laMacDinh} onChange={(e) => setDiaChiForm({ ...diaChiForm, laMacDinh: e.target.checked })} /> Đặt làm địa chỉ mặc định</label>
              <div className="inline-actions">
                <button className="btn" disabled={dangGui}>{diaChiDangSua ? 'Lưu địa chỉ' : 'Thêm địa chỉ'}</button>
                {diaChiDangSua && <button type="button" onClick={() => { setDiaChiDangSua(null); setDiaChiForm(DIA_CHI_RONG); }}>Hủy</button>}
              </div>
            </form>
          </div>
        )}
      </section>
    </div>
  );
}
