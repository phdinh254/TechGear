import Icon from './Icon.jsx';

export default function EmptyState({ icon = 'package', tieuDe, moTa, children, compact = false }) {
  return (
    <div className={`empty-state${compact ? ' empty-state-compact' : ''}`}>
      <span className="empty-state-icon"><Icon name={icon} size={compact ? 26 : 32} strokeWidth={1.5} /></span>
      {tieuDe && <h2>{tieuDe}</h2>}
      {moTa && <p>{moTa}</p>}
      {children && <div className="empty-state-actions">{children}</div>}
    </div>
  );
}
