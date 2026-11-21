/**
 * Bộ icon nét (stroke) dùng chung cho toàn site. Tất cả vẽ trên lưới 24×24,
 * nét 1.75 để đồng nhất. Dùng: <Icon name="cart" size={22} />
 */
const PATHS = {
  search: <><circle cx="11" cy="11" r="6.5" /><path d="m16 16 4.5 4.5" /></>,
  cart: <><path d="M3 4h2.2l2.1 10.2a1.8 1.8 0 0 0 1.8 1.4h8.3a1.8 1.8 0 0 0 1.8-1.4L20.8 8H6" /><circle cx="9.5" cy="19.5" r="1.2" /><circle cx="17.5" cy="19.5" r="1.2" /></>,
  user: <><circle cx="12" cy="8.5" r="3.8" /><path d="M4.5 20c1.2-3.6 4.1-5.4 7.5-5.4s6.3 1.8 7.5 5.4" /></>,
  heart: <path d="M12 20s-7.5-4.4-7.5-10.1A4.3 4.3 0 0 1 12 7.3a4.3 4.3 0 0 1 7.5 2.6C19.5 15.6 12 20 12 20Z" />,
  menu: <><path d="M4 7h16" /><path d="M4 12h16" /><path d="M4 17h10" /></>,
  close: <><path d="m6 6 12 12" /><path d="M18 6 6 18" /></>,
  chevronDown: <path d="m6 9 6 6 6-6" />,
  chevronRight: <path d="m9 6 6 6-6 6" />,
  chevronLeft: <path d="m15 6-6 6 6 6" />,
  arrowRight: <><path d="M5 12h14" /><path d="m13 6 6 6-6 6" /></>,
  plus: <><path d="M12 5v14" /><path d="M5 12h14" /></>,
  minus: <path d="M5 12h14" />,
  check: <path d="m5 12.5 4.2 4.2L19 7" />,
  trash: <><path d="M4.5 7h15" /><path d="M9.5 7V4.8h5V7" /><path d="M6.5 7l.9 12.2a1.5 1.5 0 0 0 1.5 1.3h6.2a1.5 1.5 0 0 0 1.5-1.3L17.5 7" /></>,
  filter: <><path d="M4 6h16" /><path d="M7 12h10" /><path d="M10 18h4" /></>,
  truck: <><path d="M2.5 6.5h11v9h-11z" /><path d="M13.5 9.5h4l3 3v3h-7" /><circle cx="6.5" cy="17.5" r="1.8" /><circle cx="17" cy="17.5" r="1.8" /></>,
  shield: <><path d="M12 3 5 6v5.5c0 4.4 3 7.9 7 9.5 4-1.6 7-5.1 7-9.5V6z" /><path d="m9 12 2.2 2.2L15.5 10" /></>,
  refresh: <><path d="M19.5 12a7.5 7.5 0 0 1-13.1 5" /><path d="M4.5 12a7.5 7.5 0 0 1 13.1-5" /><path d="M17.8 3.5v3.7h-3.7" /><path d="M6.2 20.5v-3.7h3.7" /></>,
  headset: <><path d="M4 14v-2a8 8 0 0 1 16 0v2" /><rect x="3.5" y="13" width="4" height="6.5" rx="1.6" /><rect x="16.5" y="13" width="4" height="6.5" rx="1.6" /><path d="M20 19.5c0 1.2-1.6 2-4 2h-2" /></>,
  wallet: <><rect x="3" y="6" width="18" height="13" rx="2.5" /><path d="M3 10h18" /><path d="M16 14.5h2" /></>,
  package: <><path d="m12 3 8 4.5v9L12 21l-8-4.5v-9z" /><path d="m4 7.5 8 4.5 8-4.5" /><path d="M12 12v9" /></>,
  mail: <><rect x="3" y="5.5" width="18" height="13" rx="2" /><path d="m4 7 8 6 8-6" /></>,
  phone: <path d="M6.6 3.5h2.6l1.4 4-2 1.4a11 11 0 0 0 6.5 6.5l1.4-2 4 1.4v2.6a2 2 0 0 1-2.2 2A16.5 16.5 0 0 1 4.6 5.7a2 2 0 0 1 2-2.2Z" />,
  logout: <><path d="M14 4.5h4a1.5 1.5 0 0 1 1.5 1.5v12a1.5 1.5 0 0 1-1.5 1.5h-4" /><path d="M10 16.5 5.5 12 10 7.5" /><path d="M5.5 12H15" /></>,
  bag: <><path d="M5 8h14l-1 12H6z" /><path d="M9 8V6.5a3 3 0 0 1 6 0V8" /></>,
  sparkle: <path d="M12 3.5 13.8 10l6.7 2-6.7 2L12 20.5 10.2 14 3.5 12l6.7-2z" />,
  star: <path d="m12 3.8 2.5 5.1 5.6.8-4 4 .9 5.6-5-2.7-5 2.7.9-5.6-4-4 5.6-.8z" />,
  bolt: <path d="M13 3 5 13.5h6L10.5 21 19 10.5h-6z" />,
  mapPin: <><path d="M12 21s-6.5-5.6-6.5-11a6.5 6.5 0 0 1 13 0c0 5.4-6.5 11-6.5 11Z" /><circle cx="12" cy="10" r="2.3" /></>,
  lock: <><rect x="5" y="10.5" width="14" height="10" rx="2" /><path d="M8 10.5V7.5a4 4 0 0 1 8 0v3" /></>,
  grid: <><rect x="4" y="4" width="7" height="7" rx="1.5" /><rect x="13" y="4" width="7" height="7" rx="1.5" /><rect x="4" y="13" width="7" height="7" rx="1.5" /><rect x="13" y="13" width="7" height="7" rx="1.5" /></>,

  /* Minh họa danh mục */
  headphones: <><path d="M4 15v-3a8 8 0 0 1 16 0v3" /><rect x="3" y="14" width="4.5" height="6.5" rx="2" /><rect x="16.5" y="14" width="4.5" height="6.5" rx="2" /></>,
  speaker: <><rect x="6" y="2.5" width="12" height="19" rx="3" /><circle cx="12" cy="14.5" r="3.5" /><circle cx="12" cy="7" r="1.3" /></>,
  charger: <><rect x="6.5" y="7" width="11" height="12.5" rx="2.5" /><path d="M9.5 7V3.5" /><path d="M14.5 7V3.5" /><path d="m12.6 10.5-2 3h2.8l-2 3" /></>,
  cable: <><path d="M7 3.5v4" /><rect x="5" y="7.5" width="4" height="5" rx="1" /><path d="M7 12.5v2a4.5 4.5 0 0 0 9 0V9.5" /><rect x="14" y="4.5" width="4" height="5" rx="1" /><path d="M16 4.5V2.5" /></>,
  powerbank: <><rect x="6" y="3" width="12" height="18" rx="2.8" /><path d="M10 7h4" /><path d="m12.6 10.5-2 3h2.8l-2 3" /></>,
  mouse: <><rect x="6.5" y="3" width="11" height="18" rx="5.5" /><path d="M12 3v5.5" /></>,
  keyboard: <><rect x="2.5" y="6.5" width="19" height="11" rx="2" /><path d="M6 10h.01M9.5 10h.01M13 10h.01M16.5 10h.01M7.5 13.8h9" /></>,
  phoneCase: <><rect x="6.5" y="2.5" width="11" height="19" rx="2.8" /><rect x="8.5" y="4.5" width="3.5" height="3.5" rx="1" /><path d="M10.5 18.5h3" /></>,
  laptop: <><rect x="4.5" y="5" width="15" height="10" rx="1.5" /><path d="M2.5 18.5h19" /></>,
  sound: <><path d="M4 9.5v5h3.5L12 18.5v-13L7.5 9.5z" /><path d="M15.5 9a4 4 0 0 1 0 6" /><path d="M18 6.5a7.5 7.5 0 0 1 0 11" /></>,
};

export default function Icon({ name, size = 22, strokeWidth = 1.75, className, title, ...rest }) {
  const path = PATHS[name];
  if (!path) return null;
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={strokeWidth}
      strokeLinecap="round"
      strokeLinejoin="round"
      className={className}
      aria-hidden={title ? undefined : true}
      role={title ? 'img' : undefined}
      {...rest}
    >
      {title && <title>{title}</title>}
      {path}
    </svg>
  );
}

/** Chọn icon minh họa theo slug danh mục; dùng khi danh mục/sản phẩm chưa có ảnh. */
const ICON_DANH_MUC = [
  [/tai-nghe|headphone|earbud/, 'headphones'],
  [/loa|speaker/, 'speaker'],
  [/am-thanh|audio/, 'sound'],
  [/sac-du-phong|pin-du-phong|power/, 'powerbank'],
  [/cap/, 'cable'],
  [/cu-sac|sac/, 'charger'],
  [/chuot|mouse/, 'mouse'],
  [/ban-phim|keyboard/, 'keyboard'],
  [/may-tinh|laptop/, 'laptop'],
  [/op-lung|bao-ve|case/, 'phoneCase'],
];

export function iconTheoSlug(slug = '') {
  const timThay = ICON_DANH_MUC.find(([mau]) => mau.test(slug));
  return timThay ? timThay[1] : 'package';
}

/** Đoán icon cho sản phẩm từ tên (sản phẩm không trả slug danh mục ở API danh sách). */
const TU_KHOA_SAN_PHAM = [
  [/tai nghe|earbud|headphone/i, 'headphones'],
  [/loa|speaker/i, 'speaker'],
  [/sạc dự phòng|pin dự phòng|power ?bank/i, 'powerbank'],
  [/cáp|cable/i, 'cable'],
  [/củ sạc|sạc|charger|gan/i, 'charger'],
  [/chuột|mouse/i, 'mouse'],
  [/bàn phím|keyboard/i, 'keyboard'],
  [/ốp|case|bao da/i, 'phoneCase'],
  [/laptop|máy tính/i, 'laptop'],
];

export function iconTheoTen(ten = '') {
  const timThay = TU_KHOA_SAN_PHAM.find(([mau]) => mau.test(ten));
  return timThay ? timThay[1] : 'package';
}
