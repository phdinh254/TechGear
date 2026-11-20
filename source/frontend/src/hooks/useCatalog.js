import { useEffect, useState } from 'react';
import catalogApi from '../api/catalogApi';

const boNho = {};

/** Gọi API một lần mỗi phiên rồi dùng lại; lỗi thì cho phép thử lại ở lần mount sau. */
function useCached(khoa, goi) {
  const [duLieu, setDuLieu] = useState(boNho[khoa]?.ketQua ?? []);
  const [dangTai, setDangTai] = useState(!boNho[khoa]?.ketQua);

  useEffect(() => {
    let conGanKet = true;
    if (!boNho[khoa]) {
      boNho[khoa] = {
        promise: goi()
          .then((kq) => { boNho[khoa].ketQua = kq; return kq; })
          .catch((e) => { delete boNho[khoa]; throw e; }),
      };
    }
    boNho[khoa].promise
      .then((kq) => { if (conGanKet) setDuLieu(kq); })
      .catch(() => {})
      .finally(() => { if (conGanKet) setDangTai(false); });
    return () => { conGanKet = false; };
  }, [khoa]);

  return [duLieu, dangTai];
}

export function useDanhMuc() {
  return useCached('danhMuc', catalogApi.layDanhMuc);
}

export function useThuongHieu() {
  return useCached('thuongHieu', catalogApi.layThuongHieu);
}

/** Tìm danh mục (cha hoặc con) theo slug, kèm danh mục cha nếu là con. */
export function timDanhMuc(danhMucs, slug) {
  for (const cha of danhMucs) {
    if (cha.slug === slug) return { danhMuc: cha, cha: null };
    const con = cha.danhMucCons?.find((c) => c.slug === slug);
    if (con) return { danhMuc: con, cha };
  }
  return { danhMuc: null, cha: null };
}
