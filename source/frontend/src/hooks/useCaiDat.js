import { useEffect, useState } from 'react';
import storeApi from '../api/storeApi';

/** Giá trị dùng khi API chưa trả về hoặc lỗi, để giao diện luôn có thông tin liên hệ. */
export const CAI_DAT_MAC_DINH = {
  ten_cua_hang: 'TechGear',
  hotline: '0900000000',
  email_lien_he: 'hotro@techgear.vn',
};

let boNho = null;

/** Cài đặt công khai của cửa hàng, chỉ gọi API một lần cho cả phiên. */
export default function useCaiDat() {
  const [caiDat, setCaiDat] = useState(CAI_DAT_MAC_DINH);

  useEffect(() => {
    let conGanKet = true;
    boNho ??= storeApi.layCaiDat().catch(() => {
      boNho = null;
      return {};
    });
    boNho.then((duLieu) => {
      if (conGanKet) setCaiDat({ ...CAI_DAT_MAC_DINH, ...duLieu });
    });
    return () => { conGanKet = false; };
  }, []);

  return caiDat;
}

/** 0912345678 -> 0912 345 678 */
export function dinhDangSoDienThoai(so = '') {
  const chuSo = so.replace(/\D/g, '');
  return chuSo.length === 10 ? chuSo.replace(/(\d{4})(\d{3})(\d{3})/, '$1 $2 $3') : so;
}
