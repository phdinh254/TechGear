/**
 * Bố cục chung cho trang đăng nhập / đăng ký / quên mật khẩu: một thẻ form căn giữa.
 * ghiChu: dòng chữ nhỏ hiển thị bên dưới, ngoài thẻ (ví dụ: đồng ý điều khoản).
 */
export default function AuthLayout({ tieuDe, moTa, children, chanTrang, ghiChu }) {
  return (
    <div className="auth-page">
      <div className="auth">
        <div className="auth-form">
          <div className="auth-form-inner">
            <h1>{tieuDe}</h1>
            {moTa && <p className="auth-desc">{moTa}</p>}
            {children}
            {chanTrang && <div className="auth-foot">{chanTrang}</div>}
          </div>
        </div>
      </div>
      {ghiChu && <p className="auth-legal">{ghiChu}</p>}
    </div>
  );
}
