import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import adminApi from '../api/adminApi.js';
import ErrorMessage from '../../components/ErrorMessage.jsx';
import { layThongBaoLoi } from '../../api/axiosClient.js';
import { formatTien } from '../../utils/format.js';
import { NHAN_TRANG_THAI_DON } from '../utils/nhan.jsx';
import { ngayCach, ngayISO } from '../utils/thoiGian.js';

const KHOANG = [
  { soNgay: 7, nhan: '7 ngày' },
  { soNgay: 30, nhan: '30 ngày' },
  { soNgay: 90, nhan: '90 ngày' },
];

const tienGon = new Intl.NumberFormat('vi-VN', { notation: 'compact', maximumFractionDigits: 1 });
const nhanMoc = (moc) => { const [, m, d] = moc.split('-'); return `${d}/${m}`; };

/** Biểu đồ cột một chuỗi (doanh thu theo ngày): không cần chú giải, tiêu đề đã gọi tên; di chuột xem từng cột. */
function BieuDoDoanhThu({ duLieu }) {
  const [chon, setChon] = useState(null);
  const lonNhat = Math.max(...duLieu.map((d) => d.doanhThu), 0);
  // Mốc trục: 0, nửa, đỉnh (làm tròn lên để vạch đẹp)
  const buoc = lonNhat > 0 ? 10 ** Math.floor(Math.log10(lonNhat)) / 2 : 1;
  const dinh = lonNhat > 0 ? Math.ceil(lonNhat / buoc) * buoc : 1;
  const vach = [dinh, dinh / 2, 0];
  const nhanCach = Math.ceil(duLieu.length / 8); // tối đa ~8 nhãn ngày trên trục
  const dangChon = chon != null ? duLieu[chon] : null;

  return (
    <div className="admin-chart">
      <div className="admin-chart-plot" onMouseLeave={() => setChon(null)}>
        <div className="admin-chart-axis" aria-hidden="true">
          {vach.map((v) => <span key={v}>{v === 0 ? '0' : tienGon.format(v)}</span>)}
        </div>
        <div className="admin-chart-area">
          {vach.map((v) => <span key={v} className="admin-chart-grid" style={{ bottom: `${(v / dinh) * 100}%` }} aria-hidden="true" />)}
          <div className="admin-chart-bars" role="list" aria-label="Doanh thu từng ngày">
            {duLieu.map((d, i) => (
              <div
                key={d.moc}
                role="listitem"
                tabIndex={0}
                className={`admin-chart-col${chon === i ? ' is-active' : ''}`}
                onMouseEnter={() => setChon(i)}
                onFocus={() => setChon(i)}
                onBlur={() => setChon(null)}
                aria-label={`${nhanMoc(d.moc)}: ${formatTien(d.doanhThu)}, ${d.soDon} đơn`}
              >
                <span className="admin-chart-bar" style={{ height: d.doanhThu > 0 ? `max(2px, ${(d.doanhThu / dinh) * 100}%)` : 0 }} />
              </div>
            ))}
          </div>
          {dangChon && (
            // Đặt cạnh cột (không đè lên cột đang xem); nửa phải thì lật sang trái
            <div className={`admin-chart-tip${chon > duLieu.length / 2 ? ' is-left' : ''}`} style={{ left: `${((chon + 0.5) / duLieu.length) * 100}%` }} role="status">
              <strong>{nhanMoc(dangChon.moc)}</strong>
              <span>{formatTien(dangChon.doanhThu)}</span>
              <span className="field-hint">{dangChon.soDon} đơn hoàn thành</span>
            </div>
          )}
        </div>
      </div>
      <div className="admin-chart-x" aria-hidden="true">
        {duLieu.map((d, i) => <span key={d.moc}>{i % nhanCach === 0 ? nhanMoc(d.moc) : ''}</span>)}
      </div>
    </div>
  );
}

export default function TongQuanPage() {
  const [soNgay, setSoNgay] = useState(30);
  const [dl, setDl] = useState(null);
  const [loi, setLoi] = useState('');

  useEffect(() => {
    let huy = false;
    const tu = ngayISO(ngayCach(-(soNgay - 1)));
    const den = ngayISO(new Date());
    setDl(null);
    Promise.all([
      adminApi.thongKe.donHang(tu, den),
      adminApi.thongKe.doanhThu(tu, den, 'ngay'),
      adminApi.thongKe.banChay(tu, den, 5),
      adminApi.donHang.timKiem({ trangThai: 'cho_xac_nhan', kichThuoc: 1 }),
      adminApi.danhGia.timKiem({ trangThai: 'cho_duyet', kichThuoc: 1 }),
      adminApi.tonKho.danhSach({ trangThai: 'sap_het', kichThuoc: 1 }),
      adminApi.tonKho.danhSach({ trangThai: 'het_hang', kichThuoc: 1 }),
    ])
      .then(([tongHop, doanhThu, banChay, choXacNhan, choDuyet, sapHet, hetHang]) => {
        if (huy) return;
        setDl({
          tongHop, doanhThu, banChay,
          viec: {
            choXacNhan: choXacNhan.tongSoPhanTu,
            choDuyet: choDuyet.tongSoPhanTu,
            sapHet: sapHet.tongSoPhanTu,
            hetHang: hetHang.tongSoPhanTu,
          },
        });
        setLoi('');
      })
      .catch((err) => { if (!huy) setLoi(layThongBaoLoi(err)); });
    return () => { huy = true; };
  }, [soNgay]);

  const tongHop = dl?.tongHop;
  const soHoanThanh = tongHop?.theoTrangThai?.hoan_thanh || 0;
  const giaTriTB = soHoanThanh ? Math.round(tongHop.doanhThu / soHoanThanh) : 0;
  const tyLeHuy = tongHop?.tongDon ? Math.round(((tongHop.theoTrangThai.da_huy || 0) / tongHop.tongDon) * 100) : 0;

  const viec = dl && [
    { so: dl.viec.choXacNhan, nhan: 'đơn chờ xác nhận', to: '/admin/don-hang?trangThai=cho_xac_nhan' },
    { so: dl.viec.choDuyet, nhan: 'đánh giá chờ duyệt', to: '/admin/danh-gia' },
    { so: dl.viec.sapHet, nhan: 'biến thể sắp hết hàng', to: '/admin/ton-kho?trangThai=sap_het' },
    { so: dl.viec.hetHang, nhan: 'biến thể đã hết hàng', to: '/admin/ton-kho?trangThai=het_hang' },
  ];

  return (
    <>
      <div className="admin-toolbar">
        <h1 className="admin-toolbar-grow">Tổng quan</h1>
        <div className="admin-tabs" role="group" aria-label="Khoảng thời gian">
          {KHOANG.map((k) => (
            <button key={k.soNgay} type="button" aria-pressed={soNgay === k.soNgay} className={soNgay === k.soNgay ? 'is-active' : undefined} onClick={() => setSoNgay(k.soNgay)}>
              {k.nhan}
            </button>
          ))}
        </div>
      </div>
      <ErrorMessage>{loi}</ErrorMessage>

      {viec && viec.some((v) => v.so > 0) && (
        <section className="admin-todo" aria-label="Việc cần xử lý">
          {viec.filter((v) => v.so > 0).map((v) => (
            <Link key={v.to} to={v.to} className="admin-todo-item"><strong>{v.so}</strong> {v.nhan}</Link>
          ))}
        </section>
      )}

      <section className="admin-stats" aria-label={`Số liệu ${soNgay} ngày qua`}>
        {[
          { nhan: 'Doanh thu', giaTri: tongHop && formatTien(tongHop.doanhThu), phu: `${soHoanThanh} đơn hoàn thành` },
          { nhan: 'Tổng đơn', giaTri: tongHop?.tongDon, phu: 'Mọi trạng thái' },
          { nhan: 'Giá trị TB / đơn', giaTri: tongHop && formatTien(giaTriTB), phu: 'Trên đơn hoàn thành' },
          { nhan: 'Tỉ lệ hủy', giaTri: tongHop && `${tyLeHuy}%`, phu: `${tongHop?.theoTrangThai?.da_huy || 0} đơn đã hủy` },
        ].map((s) => (
          <div key={s.nhan} className="admin-card admin-stat">
            <span className="admin-stat-label">{s.nhan}</span>
            {dl ? <strong className="admin-stat-value">{s.giaTri}</strong> : <span className="skeleton admin-stat-skeleton" />}
            <small className="field-hint">{s.phu}</small>
          </div>
        ))}
      </section>

      <div className="admin-dash-grid">
        <section className="admin-card admin-dash-wide">
          <h2 className="admin-card-title">Doanh thu theo ngày</h2>
          <p className="field-hint admin-card-sub">Tổng tiền đơn hoàn thành, tính theo ngày đặt</p>
          {dl ? <BieuDoDoanhThu duLieu={dl.doanhThu} /> : <div className="skeleton admin-chart-skeleton" />}
        </section>

        <section className="admin-card">
          <h2 className="admin-card-title">Đơn theo trạng thái</h2>
          {dl ? (
            <ul className="admin-status-list">
              {Object.keys(NHAN_TRANG_THAI_DON).map((k) => (
                <li key={k}>
                  <Link to={`/admin/don-hang?trangThai=${k}`}><span className={`status status-${k}`}>{NHAN_TRANG_THAI_DON[k]}</span></Link>
                  <strong>{tongHop.theoTrangThai[k] || 0}</strong>
                </li>
              ))}
            </ul>
          ) : <div className="skeleton admin-chart-skeleton" />}
        </section>

        <section className="admin-card">
          <h2 className="admin-card-title">Bán chạy nhất</h2>
          {dl && dl.banChay.length === 0 && <p className="field-hint">Chưa có đơn hoàn thành trong khoảng này.</p>}
          {dl ? (dl.banChay.length > 0 && (
            <ol className="admin-top-list">
              {dl.banChay.map((sp) => (
                <li key={sp.sanPhamId ?? sp.tenSanPham}>
                  <span className="admin-top-name">{sp.sanPhamId ? <Link to={`/admin/san-pham/${sp.sanPhamId}`}>{sp.tenSanPham}</Link> : sp.tenSanPham}</span>
                  <span className="field-hint">{sp.soLuongBan} đã bán · {formatTien(sp.doanhThu)}</span>
                </li>
              ))}
            </ol>
          )) : <div className="skeleton admin-chart-skeleton" />}
        </section>
      </div>
    </>
  );
}
