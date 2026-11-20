export default function Stars({ diem = 0, size = 14 }) {
  const lam = Math.round(diem * 2) / 2;
  return (
    <span className="stars" style={{ '--star-size': `${size}px`, '--rating': lam }} aria-label={`${diem} trên 5 sao`} role="img">
      <span className="stars-fill" aria-hidden="true">★★★★★</span>
      <span className="stars-base" aria-hidden="true">★★★★★</span>
    </span>
  );
}
