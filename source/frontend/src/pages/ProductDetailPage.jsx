import { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import catalogApi from '../api/catalogApi';
import { layThongBaoLoi } from '../api/axiosClient';
import { useCart } from '../context/CartContext.jsx';
import { duongDanAnh, formatTien } from '../utils/format';
import Loading from '../components/Loading.jsx';
import ErrorMessage from '../components/ErrorMessage.jsx';
import Pagination from '../components/Pagination.jsx';
import { lamSachHtml } from '../utils/sanitize';
import { wishlistApi } from '../api/customerApi';
import { useAuth } from '../context/AuthContext.jsx';

const dinhDangNgay = new Intl.DateTimeFormat('vi-VN', {
  day: '2-digit',
  month: '2-digit',
  year: 'numeric',
});

/** So sánh 2 mảng id không phân biệt thứ tự. */
const cungGiaTri = (a, b) =>
  a.length === b.length && [...a].sort((x, y) => x - y).every((v, i) => v === [...b].sort((x, y) => x - y)[i]);

export default function ProductDetailPage() {
  const { slug } = useParams();
  const { themVaoGio } = useCart();
  const { nguoiDung } = useAuth();
  const [sp, setSp] = useState(null);
  const [loi, setLoi] = useState('');
  const [chon, setChon] = useState({}); // { tuyChonId: giaTriId }
  const [soLuong, setSoLuong] = useState(1);
  const [anhDangXem, setAnhDangXem] = useState(0);
  const [thongBao, setThongBao] = useState('');
  const [loiThaoTac, setLoiThaoTac] = useState('');
  const [danhGia, setDanhGia] = useState(null);
  const [trangDanhGia, setTrangDanhGia] = useState(0);
  const [dangTaiDanhGia, setDangTaiDanhGia] = useState(true);
  const [loiDanhGia, setLoiDanhGia] = useState('');
  const [yeuThich, setYeuThich] = useState(false);

  useEffect(() => {
    const controller = new AbortController();
    setSp(null);
    setLoi('');
    setTrangDanhGia(0);
    catalogApi
      .layChiTietSanPham(slug, { signal: controller.signal })
      .then((data) => {
        setSp(data);
        // Chọn sẵn biến thể đầu tiên còn hàng
        const macDinh = data.bienThes.find((b) => b.soLuongTon > 0) || data.bienThes[0];
        const chonBanDau = {};
        if (macDinh) {
          data.tuyChons.forEach((tc) => {
            const gt = tc.giaTris.find((g) => macDinh.giaTriIds.includes(g.id));
            if (gt) chonBanDau[tc.id] = gt.id;
          });
        }
        setChon(chonBanDau);
        const viTriAnhChinh = data.hinhAnhs.findIndex((anh) => anh.laAnhChinh);
        setAnhDangXem(viTriAnhChinh >= 0 ? viTriAnhChinh : 0);
      })
      .catch((e) => {
        if (e.code !== 'ERR_CANCELED') setLoi(layThongBaoLoi(e));
      });
    return () => controller.abort();
  }, [slug]);

  useEffect(() => {
    const controller = new AbortController();
    setDangTaiDanhGia(true);
    setLoiDanhGia('');
    setDanhGia(null);
    catalogApi
      .layDanhGiaSanPham(slug, trangDanhGia, { signal: controller.signal })
      .then(setDanhGia)
      .catch((e) => {
        if (e.code !== 'ERR_CANCELED') setLoiDanhGia(layThongBaoLoi(e));
      })
      .finally(() => {
        if (!controller.signal.aborted) setDangTaiDanhGia(false);
      });
    return () => controller.abort();
  }, [slug, trangDanhGia]);

  useEffect(() => {
    if (!nguoiDung || !sp) return;
    wishlistApi.kiemTra(sp.id).then((response) => setYeuThich(response.yeuThich)).catch(() => {});
  }, [nguoiDung, sp]);

  const bienThe = useMemo(() => {
    if (!sp) return null;
    if (sp.tuyChons.length === 0) return sp.bienThes[0] || null;
    const daChon = Object.values(chon);
    if (daChon.length !== sp.tuyChons.length) return null;
    return sp.bienThes.find((b) => cungGiaTri(b.giaTriIds, daChon)) || null;
  }, [sp, chon]);

  const moTaAnToan = useMemo(() => lamSachHtml(sp?.moTa), [sp?.moTa]);

  if (loi) return <ErrorMessage>{loi}</ErrorMessage>;
  if (!sp) return <Loading />;

  const giaBan = bienThe ? bienThe.giaBan : sp.giaBan;
  const giaGoc = bienThe ? bienThe.giaGoc : sp.giaGoc;
  const conHang = bienThe && bienThe.soLuongTon > 0;
  const anh = sp.hinhAnhs[anhDangXem];

  const xuLyThemVaoGio = async () => {
    if (!bienThe) return;
    const phanLoai = sp.tuyChons
      .map((tc) => tc.giaTris.find((g) => g.id === chon[tc.id])?.giaTri)
      .filter(Boolean)
      .join(' / ');
    try {
      setLoiThaoTac('');
      await themVaoGio(
        {
          bienTheId: bienThe.id,
          sanPhamSlug: sp.slug,
          ten: sp.ten,
          phanLoai,
          giaBan: bienThe.giaBan,
          anh: sp.hinhAnhs.find((hinhAnh) => hinhAnh.laAnhChinh)?.duongDan || sp.hinhAnhs[0]?.duongDan || null,
          tonKho: bienThe.soLuongTon,
        },
        soLuong,
      );
      setThongBao('Đã thêm vào giỏ hàng');
      setTimeout(() => setThongBao(''), 2000);
    } catch (error) {
      setLoiThaoTac(layThongBaoLoi(error));
    }
  };

  const doiYeuThich = async () => {
    try {
      const response = yeuThich ? await wishlistApi.xoa(sp.id) : await wishlistApi.them(sp.id);
      setYeuThich(response.yeuThich);
    } catch (error) {
      setLoiThaoTac(layThongBaoLoi(error));
    }
  };

  return (
    <div className="detail">
      <p className="breadcrumb">
        <Link to="/">Trang chủ</Link> / <Link to={`/san-pham?danhMuc=${sp.danhMuc.slug}`}>{sp.danhMuc.ten}</Link> /{' '}
        {sp.ten}
      </p>

      <div className="detail-grid">
        <div className="gallery">
          <div className="gallery-main">
            {anh ? <img src={duongDanAnh(anh.duongDan)} alt={anh.moTa || sp.ten} /> : <span>Chưa có ảnh</span>}
          </div>
          {sp.hinhAnhs.length > 1 && (
            <div className="gallery-thumbs">
              {sp.hinhAnhs.map((ha, i) => (
                <button
                  type="button"
                  key={ha.id}
                  className={i === anhDangXem ? 'active' : ''}
                  onClick={() => setAnhDangXem(i)}
                >
                  <img src={duongDanAnh(ha.duongDan)} alt={ha.moTa || sp.ten} />
                </button>
              ))}
            </div>
          )}
        </div>

        <div className="detail-info">
          {sp.thuongHieu && <p className="brand">{sp.thuongHieu.ten}</p>}
          <h1>{sp.ten}</h1>
          {nguoiDung ? <button type="button" className="wishlist-button" onClick={doiYeuThich}>{yeuThich ? '♥ Đã yêu thích' : '♡ Thêm vào yêu thích'}</button> : <Link to="/dang-nhap" state={{ tu: `/san-pham/${sp.slug}` }}>Đăng nhập để lưu sản phẩm yêu thích</Link>}
          <p className="price big">
            {formatTien(giaBan)}
            {giaGoc && Number(giaGoc) > Number(giaBan) && <del>{formatTien(giaGoc)}</del>}
          </p>
          {sp.moTaNgan && <p>{sp.moTaNgan}</p>}

          {sp.tuyChons.map((tc) => (
            <div key={tc.id} className="option-group">
              <p className="option-label">{tc.ten}</p>
              <div className="option-values">
                {tc.giaTris.map((gt) => (
                  <button
                    type="button"
                    key={gt.id}
                    className={chon[tc.id] === gt.id ? 'option active' : 'option'}
                    onClick={() => {
                      setChon({ ...chon, [tc.id]: gt.id });
                      setSoLuong(1);
                    }}
                  >
                    {gt.maMau && <span className="swatch" style={{ background: gt.maMau }} />}
                    {gt.giaTri}
                  </button>
                ))}
              </div>
            </div>
          ))}

          <p className="muted">
            {bienThe
              ? conHang
                ? `Còn ${bienThe.soLuongTon} sản phẩm · SKU: ${bienThe.sku}`
                : 'Phiên bản này tạm hết hàng'
              : 'Phiên bản này hiện không có'}
          </p>

          <div className="buy-row">
            <input
              type="number"
              min="1"
              max={bienThe?.soLuongTon || 1}
              value={soLuong}
              onChange={(e) => {
                const toiDa = bienThe?.soLuongTon || 1;
                setSoLuong(Math.min(toiDa, Math.max(1, Number(e.target.value) || 1)));
              }}
              aria-label="Số lượng"
            />
            <button type="button" className="btn" disabled={!conHang} onClick={xuLyThemVaoGio}>
              Thêm vào giỏ
            </button>
          </div>
          {thongBao && <p className="success">{thongBao}</p>}
          <ErrorMessage>{loiThaoTac}</ErrorMessage>

          <p className="muted">
            Bảo hành: {sp.baoHanhThang > 0 ? `${sp.baoHanhThang} tháng` : 'Không bảo hành'}
          </p>
        </div>
      </div>

      {sp.thongSos.length > 0 && (
        <section className="section">
          <h2>Thông số kỹ thuật</h2>
          <table className="spec-table">
            <tbody>
              {sp.thongSos.map((ts) => (
                <tr key={ts.ten}>
                  <th>{ts.ten}</th>
                  <td>{ts.giaTri}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      )}

      {moTaAnToan && (
        <section className="section">
          <h2>Mô tả sản phẩm</h2>
          <div className="description" dangerouslySetInnerHTML={{ __html: moTaAnToan }} />
        </section>
      )}

      <section className="section reviews">
        <div className="review-heading">
          <h2>Đánh giá sản phẩm</h2>
          {danhGia && danhGia.tongDanhGia > 0 && (
            <div className="review-summary" aria-label={`${danhGia.diemTrungBinh} trên 5 sao`}>
              <strong>{danhGia.diemTrungBinh}/5</strong>
              <span className="stars" aria-hidden="true">
                {'★'.repeat(Math.round(danhGia.diemTrungBinh))}
                <span className="stars-empty">{'★'.repeat(5 - Math.round(danhGia.diemTrungBinh))}</span>
              </span>
              <span className="muted">{danhGia.tongDanhGia} đánh giá</span>
            </div>
          )}
        </div>

        {dangTaiDanhGia && <Loading />}
        <ErrorMessage>{loiDanhGia}</ErrorMessage>
        {!dangTaiDanhGia && danhGia?.tongDanhGia === 0 && (
          <p className="muted">Sản phẩm chưa có đánh giá đã được duyệt.</p>
        )}
        {!dangTaiDanhGia && danhGia?.danhSach.noiDung.map((dg) => (
          <article className="review-item" key={dg.id}>
            <div className="review-meta">
              <strong>{dg.tenNguoiDung}</strong>
              <span className="stars" aria-label={`${dg.soSao} trên 5 sao`}>
                {'★'.repeat(dg.soSao)}<span className="stars-empty">{'★'.repeat(5 - dg.soSao)}</span>
              </span>
              {dg.ngayDanhGia && <time dateTime={dg.ngayDanhGia}>{dinhDangNgay.format(new Date(dg.ngayDanhGia))}</time>}
            </div>
            {dg.noiDung && <p>{dg.noiDung}</p>}
          </article>
        ))}
        {danhGia && (
          <Pagination
            trang={danhGia.danhSach.trang}
            tongSoTrang={danhGia.danhSach.tongSoTrang}
            onChange={setTrangDanhGia}
          />
        )}
      </section>
    </div>
  );
}
