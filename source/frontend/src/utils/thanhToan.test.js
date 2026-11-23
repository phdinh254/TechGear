import { test } from 'node:test';
import assert from 'node:assert/strict';
import {
  chonGiaoDienKetQua, chonHanhDongKhiMo, coTheThanhToanNgay, dinhDangDongHo, giayConLai, HAN_THANH_TOAN_PHUT, laLinkNhung,
  nhanPhuongThuc,
} from './thanhToan.js';

const bayGio = Date.parse('2026-11-23T10:00:00+07:00');
const don = (ghiDe = {}) => ({
  phuongThucThanhToan: 'payos',
  trangThaiThanhToan: 'chua_thanh_toan',
  trangThai: 'cho_xac_nhan',
  ngayDat: '2026-11-23T09:45:00+07:00',
  ...ghiDe,
});

test('đơn PayOS chưa trả, còn chờ, trong 30 phút thì thanh toán ngay được', () => {
  assert.equal(HAN_THANH_TOAN_PHUT, 30);
  assert.equal(coTheThanhToanNgay(don(), bayGio), true);
});

test('không cho thanh toán khi là COD, đã trả, đã xác nhận/hủy hoặc quá 30 phút', () => {
  assert.equal(coTheThanhToanNgay(don({ phuongThucThanhToan: 'cod' }), bayGio), false);
  assert.equal(coTheThanhToanNgay(don({ trangThaiThanhToan: 'da_thanh_toan' }), bayGio), false);
  assert.equal(coTheThanhToanNgay(don({ trangThai: 'da_huy' }), bayGio), false);
  assert.equal(coTheThanhToanNgay(don({ trangThai: 'da_xac_nhan' }), bayGio), false);
  assert.equal(coTheThanhToanNgay(don({ ngayDat: '2026-11-23T09:30:00+07:00' }), bayGio), false);
  assert.equal(coTheThanhToanNgay(null, bayGio), false);
});

test('nhãn phương thức thanh toán', () => {
  assert.equal(nhanPhuongThuc('cod'), 'Thanh toán khi nhận hàng');
  assert.equal(nhanPhuongThuc('payos'), 'Thanh toán qua PayOS');
});

test('chọn giao diện trang kết quả theo giao dịch và đơn', () => {
  const kq = (ghiDe) => ({ hopLe: true, trangThai: 'thanh_cong', trangThaiThanhToan: 'da_thanh_toan', trangThaiDon: 'cho_xac_nhan', ...ghiDe });
  assert.equal(chonGiaoDienKetQua(kq()), 'thanh_cong');
  assert.equal(chonGiaoDienKetQua(kq({ trangThaiDon: 'da_huy' })), 'da_tra_don_huy');
  assert.equal(chonGiaoDienKetQua(kq({ trangThai: 'that_bai' })), 'da_tra_lan_khac');
  assert.equal(chonGiaoDienKetQua(kq({ trangThai: 'that_bai', trangThaiThanhToan: 'chua_thanh_toan' })), 'that_bai');
  assert.equal(chonGiaoDienKetQua(kq({ trangThai: 'cho_xu_ly', trangThaiThanhToan: 'chua_thanh_toan' })), 'cho_xu_ly');
  assert.equal(chonGiaoDienKetQua({ hopLe: false }), 'khong_hop_le');
});

// ---- Trang thanh toán nhúng PayOS ----

test('laLinkNhung: chỉ nhận link thanh toán pay.payos.vn/web/<32 hex>', () => {
  assert.equal(laLinkNhung(`https://pay.payos.vn/web/${'a'.repeat(32)}`), true);
  assert.equal(laLinkNhung(`https://pay.payos.vn/web/${'A1'.repeat(16)}/`), true);
  assert.equal(laLinkNhung('https://pay.payos.vn/web/gia-lap-1'), false);
  assert.equal(laLinkNhung('http://localhost:5173/thanh-toan/ket-qua/X?orderCode=1&status=PAID&gia_lap=1'), false);
  assert.equal(laLinkNhung(`https://evil.vn/web/${'a'.repeat(32)}`), false);
  assert.equal(laLinkNhung(''), false);
  assert.equal(laLinkNhung(undefined), false);
});

test('giayConLai: số giây tới hạn, không âm', () => {
  const luc = Date.parse('2026-11-23T10:00:00+07:00');
  assert.equal(giayConLai('2026-11-23T10:30:00+07:00', luc), 1800);
  assert.equal(giayConLai('2026-11-23T10:00:00.900+07:00', luc), 0);
  assert.equal(giayConLai('2026-11-23T09:59:00+07:00', luc), 0);
});

test('dinhDangDongHo: mm:ss', () => {
  assert.equal(dinhDangDongHo(1799), '29:59');
  assert.equal(dinhDangDongHo(5), '00:05');
  assert.equal(dinhDangDongHo(0), '00:00');
});

test('chonHanhDongKhiMo: dùng lại link còn chờ, sang kết quả khi đã trả, còn lại tạo mới', () => {
  const daNho = { orderCode: 4301, duongDan: `https://pay.payos.vn/web/${'a'.repeat(32)}` };
  assert.equal(chonHanhDongKhiMo(null, null), 'tao_moi');
  assert.equal(chonHanhDongKhiMo(daNho, { hopLe: true, trangThai: 'cho_xu_ly' }), 'dung_lai');
  assert.equal(chonHanhDongKhiMo(daNho, { hopLe: true, trangThai: 'thanh_cong' }), 'sang_ket_qua');
  assert.equal(chonHanhDongKhiMo(daNho, { hopLe: true, trangThai: 'that_bai' }), 'tao_moi');
  assert.equal(chonHanhDongKhiMo(daNho, { hopLe: false }), 'tao_moi');
  assert.equal(chonHanhDongKhiMo(daNho, null), 'tao_moi');
});
