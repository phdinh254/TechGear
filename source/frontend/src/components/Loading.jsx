export default function Loading({ noiDung = 'Đang tải...' }) {
  return (
    <div className="loading" role="status">
      <span className="spinner spinner-lg" />
      <span>{noiDung}</span>
    </div>
  );
}
