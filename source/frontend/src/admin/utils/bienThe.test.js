import { test } from 'node:test';
import assert from 'node:assert/strict';
import { taoSlug, goiYSku, sinhToHop, ghepBienThe, skuTrung } from './bienThe.js';

test('taoSlug bỏ dấu và chuẩn hóa', () => {
  assert.equal(taoSlug('Củ sạc nhanh 65W'), 'cu-sac-nhanh-65w');
});

test('goiYSku ghép viết tắt và giá trị', () => {
  assert.equal(goiYSku('Sạc dự phòng Anker', ['Đen', '20000mAh']), 'TG-SDPA-DEN-20000MAH');
});

test('sinhToHop tạo tích Đề-các', () => {
  const gt = (k) => ({ khoa: k, giaTri: k.toUpperCase() });
  const r = sinhToHop([
    { khoa: 'a', giaTris: [gt('a1'), gt('a2')] },
    { khoa: 'b', giaTris: [gt('b1'), gt('b2'), gt('b3')] },
  ]);
  assert.equal(r.length, 6);
  assert.equal(r[0].khoa, 'a1|b1');
});

test('sinhToHop không có tùy chọn', () => {
  assert.deepEqual(sinhToHop([]), [{ khoa: '', giaTris: [] }]);
});

test('ghepBienThe giữ dòng cũ và thêm dòng mới', () => {
  const a1 = { khoa: 'a1', giaTri: 'A1' };
  const a2 = { khoa: 'a2', giaTri: 'A2' };
  const toHops = [
    { khoa: 'a1', giaTris: [a1] },
    { khoa: 'a2', giaTris: [a2] },
  ];
  const cu = [{ khoa: 'a1', id: 7, giaBan: '100' }];
  const r = ghepBienThe(toHops, cu, 'Chuột');
  assert.equal(r[0].id, 7);
  assert.equal(r[0].giaBan, '100');
  assert.equal(r[1].id, null);
  assert.equal(r[1].khoa, 'a2');
});

test('skuTrung tìm SKU lặp không phân biệt hoa thường', () => {
  assert.deepEqual(skuTrung([{ sku: 'A' }, { sku: 'a' }, { sku: 'B' }]), new Set(['A']));
});
