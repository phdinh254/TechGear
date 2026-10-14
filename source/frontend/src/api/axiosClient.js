import axios from 'axios';

export const TOKEN_KEY = 'techgear_token';

const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
});

// Tự gắn JWT vào mọi request nếu đã đăng nhập
axiosClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Token hết hạn / không hợp lệ -> xóa token để người dùng đăng nhập lại
axiosClient.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem(TOKEN_KEY);
    }
    return Promise.reject(error);
  },
);

/** Lấy thông báo lỗi tiếng Việt từ phản hồi của backend. */
export function layThongBaoLoi(error) {
  if (!error.response) return 'Không kết nối được máy chủ. Vui lòng kiểm tra backend.';
  return error.response.data?.thongBao || 'Đã xảy ra lỗi, vui lòng thử lại.';
}

export default axiosClient;
