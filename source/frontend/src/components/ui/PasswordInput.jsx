import { useState } from 'react';

/** Ô mật khẩu có nút hiện/ẩn. Nhận mọi thuộc tính của <input>. */
export default function PasswordInput(props) {
  const [hien, setHien] = useState(false);
  return (
    <div className="password-input">
      <input {...props} type={hien ? 'text' : 'password'} />
      <button type="button" onClick={() => setHien(!hien)} aria-label={hien ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'} aria-pressed={hien}>
        {hien ? 'Ẩn' : 'Hiện'}
      </button>
    </div>
  );
}
