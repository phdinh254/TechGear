/** Tạo dòng giỏ hàng (dùng cho giỏ khách vãng lai lưu localStorage) từ chi tiết sản phẩm + biến thể. */
export function taoDongGio(sp, bienThe, chon = {}) {
  const phanLoai = sp.tuyChons
    .map((tc) => tc.giaTris.find((g) => g.id === chon[tc.id])?.giaTri)
    .filter(Boolean)
    .join(' / ');
  return {
    bienTheId: bienThe.id,
    sanPhamSlug: sp.slug,
    ten: sp.ten,
    phanLoai,
    giaBan: bienThe.giaBan,
    anh: sp.hinhAnhs.find((ha) => ha.laAnhChinh)?.duongDan || sp.hinhAnhs[0]?.duongDan || null,
    tonKho: bienThe.soLuongTon,
  };
}
