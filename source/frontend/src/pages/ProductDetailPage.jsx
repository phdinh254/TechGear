import { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import catalogApi from '../api/catalogApi';
import { layThongBaoLoi } from '../api/axiosClient';
import { useCart } from '../context/CartContext.jsx';
import { duongDanAnh, formatTien } from '../utils/format';
import Loading from '../components/Loading.jsx';
import ErrorMessage from '../components/ErrorMessage.jsx';

/** So sánh 2 mảng id không phân biệt thứ tự. */
const cungGiaTri = (a, b) =>
  a.length === b.length && [...a].sort((x, y) => x - y).every((v, i) => v === [...b].sort((x, y) => x - y)[i]);

export default function ProductDetailPage() {
  const { slug } = useParams();
  const { themVaoGio } = useCart();
  const [sp, setSp] = useState(null);
  const [loi, setLoi] = useState('');
  const [chon, setChon] = useState({}); // { tuyChonId: giaTriId }
  const [soLuong, setSoLuong] = useState(1);
  const [anhDangXem, setAnhDangXem] = useState(0);
  const [thongBao, setThongBao] = useState('');

  useEffect(() => {
    setSp(null);
    setLoi('');
    catalogApi
      .layChiTietSanPham(slug)
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
        setAnhDangXem(0);
      })
      .catch((e) => setLoi(layThongBaoLoi(e)));
  }, [slug]);

  const bienThe = useMemo(() => {
    if (!sp) return null;
    if (sp.tuyChons.length === 0) return sp.bienThes[0] || null;
    const daChon = Object.values(chon);
    if (daChon.length !== sp.tuyChons.length) return null;
    return sp.bienThes.find((b) => cungGiaTri(b.giaTriIds, daChon)) || null;
  }, [sp, chon]);

  if (loi) return <ErrorMessage>{loi}</ErrorMessage>;
  if (!sp) return <Loading />;

  const giaBan = bienThe ? bienThe.giaBan : sp.giaBan;
  const giaGoc = bienThe ? bienThe.giaGoc : sp.giaGoc;
  const conHang = bienThe && bienThe.soLuongTon > 0;
  const anh = sp.hinhAnhs[anhDangXem];

  const xuLyThemVaoGio = () => {
    if (!bienThe) return;
    const phanLoai = sp.tuyChons
      .map((tc) => tc.giaTris.find((g) => g.id === chon[tc.id])?.giaTri)
      .filter(Boolean)
      .join(' / ');
    themVaoGio(
      {
        bienTheId: bienThe.id,
        sanPhamSlug: sp.slug,
        ten: sp.ten,
        phanLoai,
        giaBan: bienThe.giaBan,
        anh: sp.hinhAnhs[0]?.duongDan || null,
        tonKho: bienThe.soLuongTon,
      },
      soLuong,
    );
    setThongBao('Đã thêm vào giỏ hàng');
    setTimeout(() => setThongBao(''), 2000);
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
              onChange={(e) => setSoLuong(Math.max(1, Number(e.target.value) || 1))}
              aria-label="Số lượng"
            />
            <button type="button" className="btn" disabled={!conHang} onClick={xuLyThemVaoGio}>
              Thêm vào giỏ
            </button>
          </div>
          {thongBao && <p className="success">{thongBao}</p>}

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

      {sp.moTa && (
        <section className="section">
          <h2>Mô tả sản phẩm</h2>
          {/* Mô tả do quản trị viên nhập (HTML). Cần lọc HTML ở backend trước khi lưu. */}
          <div className="description" dangerouslySetInnerHTML={{ __html: sp.moTa }} />
        </section>
      )}
    </div>
  );
}
