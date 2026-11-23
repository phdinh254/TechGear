// Quy tắc thanh toán trực tuyến PayOS phía giao diện (backend vẫn kiểm lại khi tạo link)

/** Link thanh toán hết hạn sau từng này phút kể từ lúc đặt (khớp ThanhToanService.HAN_THANH_TOAN). */
export const HAN_THANH_TOAN_PHUT = 30;

/** Đơn PayOS chưa trả, còn chờ xác nhận và chưa quá hạn thì hiện nút "Thanh toán ngay". */
export function coTheThanhToanNgay(don, bayGio = Date.now()) {
  if (!don || don.phuongThucThanhToan !== 'payos') return false;
  if (don.trangThaiThanhToan !== 'chua_thanh_toan' || don.trangThai !== 'cho_xac_nhan') return false;
  return bayGio < Date.parse(don.ngayDat) + HAN_THANH_TOAN_PHUT * 60_000;
}

export function nhanPhuongThuc(phuongThuc) {
  return phuongThuc === 'payos' ? 'Thanh toán qua PayOS' : 'Thanh toán khi nhận hàng';
}

/**
 * Khung hiển thị của trang kết quả: thanh_cong | da_tra_don_huy (tiền về khi đơn đã hủy – chờ hoàn tiền)
 * | da_tra_lan_khac (giao dịch này hỏng nhưng đơn đã trả ở lần khác) | that_bai | cho_xu_ly | khong_hop_le.
 */
export function chonGiaoDienKetQua(kq) {
  if (!kq?.hopLe) return 'khong_hop_le';
  if (kq.trangThai === 'thanh_cong') return kq.trangThaiDon === 'da_huy' ? 'da_tra_don_huy' : 'thanh_cong';
  if (kq.trangThai === 'that_bai') return kq.trangThaiThanhToan === 'da_thanh_toan' ? 'da_tra_lan_khac' : 'that_bai';
  return 'cho_xu_ly';
}

// ---- Trang thanh toán nhúng PayOS (/thanh-toan/:maDonHang) ----

const LINK_NHUNG = /^https:\/\/pay\.payos\.vn\/web\/[0-9a-fA-F]{32}\/?$/;

/** Link PayOS thật nhúng được (thư viện payos-checkout chỉ nhận dạng này); giả lập dev thì chuyển hướng thẳng. */
export function laLinkNhung(url) {
  return typeof url === 'string' && LINK_NHUNG.test(url);
}

/** Số giây còn lại tới hạn thanh toán (làm tròn xuống, không âm). */
export function giayConLai(hetHanIso, bayGio = Date.now()) {
  return Math.max(0, Math.floor((Date.parse(hetHanIso) - bayGio) / 1000));
}

export function dinhDangDongHo(giay) {
  const hai = (n) => String(n).padStart(2, '0');
  return `${hai(Math.floor(giay / 60))}:${hai(giay % 60)}`;
}

/**
 * Mở trang thanh toán: link đã nhớ còn chờ → dùng lại (không tốn lượt thử, không sinh thêm QR); đã trả → sang trang
 * kết quả; chưa có / đã hủy / không hợp lệ → tạo link mới.
 */
export function chonHanhDongKhiMo(daNho, ketQua) {
  if (!daNho || !ketQua?.hopLe) return 'tao_moi';
  if (ketQua.trangThai === 'cho_xu_ly') return 'dung_lai';
  if (ketQua.trangThai === 'thanh_cong') return 'sang_ket_qua';
  return 'tao_moi';
}
