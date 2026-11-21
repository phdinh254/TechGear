export default function DataTable({ cot, duLieu, dangTai = false, trong = 'Chưa có dữ liệu', khoaDong = 'id' }) {
  return (
    <div className="admin-table-wrap">
      <table className="admin-table">
        <thead>
          <tr>
            {cot.map((c) => <th key={c.khoa} style={c.canh ? { textAlign: c.canh } : undefined}>{c.tieuDe}</th>)}
          </tr>
        </thead>
        <tbody>
          {dangTai ? (
            <tr><td colSpan={cot.length} className="admin-table-note"><span className="spinner" aria-label="Đang tải" /></td></tr>
          ) : !duLieu || duLieu.length === 0 ? (
            <tr><td colSpan={cot.length} className="admin-table-note">{trong}</td></tr>
          ) : (
            duLieu.map((dong) => (
              <tr key={dong[khoaDong]}>
                {cot.map((c) => (
                  <td key={c.khoa} style={c.canh ? { textAlign: c.canh } : undefined}>
                    {c.hienThi ? c.hienThi(dong) : dong[c.khoa]}
                  </td>
                ))}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}
