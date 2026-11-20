export function ProductCardSkeleton() {
  return (
    <div className="product-card product-card-skeleton" aria-hidden="true">
      <div className="product-card-media skeleton" />
      <div className="product-card-body">
        <span className="skeleton skeleton-line" style={{ width: '35%' }} />
        <span className="skeleton skeleton-line" style={{ width: '90%' }} />
        <span className="skeleton skeleton-line" style={{ width: '55%' }} />
      </div>
    </div>
  );
}

export function ProductGridSkeleton({ soLuong = 8 }) {
  return Array.from({ length: soLuong }, (_, i) => <ProductCardSkeleton key={i} />);
}

export function SkeletonBlock({ height = 16, width = '100%', className = '' }) {
  return <span className={`skeleton skeleton-block ${className}`} style={{ height, width }} aria-hidden="true" />;
}
