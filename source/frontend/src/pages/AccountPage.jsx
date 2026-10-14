import { useAuth } from '../context/AuthContext.jsx';

export default function AccountPage() {
  const { nguoiDung } = useAuth();
  return (
    <div className="auth-box">
      <h1>Tài khoản của tôi</h1>
      <table className="spec-table">
        <tbody>
          <tr>
            <th>Họ tên</th>
            <td>{nguoiDung.hoTen}</td>
          </tr>
          <tr>
            <th>Email</th>
            <td>{nguoiDung.email}</td>
          </tr>
          <tr>
            <th>Số điện thoại</th>
            <td>{nguoiDung.soDienThoai || '—'}</td>
          </tr>
          <tr>
            <th>Vai trò</th>
            <td>{nguoiDung.vaiTro === 'quan_tri' ? 'Quản trị viên' : 'Khách hàng'}</td>
          </tr>
        </tbody>
      </table>
    </div>
  );
}
