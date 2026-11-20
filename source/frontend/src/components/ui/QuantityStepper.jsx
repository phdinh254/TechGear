import Icon from './Icon.jsx';

export default function QuantityStepper({ value, min = 1, max = 99, onChange, disabled = false, label = 'Số lượng', size = 'md' }) {
  const dat = (so) => onChange(Math.min(max, Math.max(min, Number.isFinite(so) ? so : min)));
  return (
    <div className={`stepper stepper-${size}${disabled ? ' is-disabled' : ''}`} role="group" aria-label={label}>
      <button type="button" onClick={() => dat(value - 1)} disabled={disabled || value <= min} aria-label="Giảm số lượng">
        <Icon name="minus" size={16} />
      </button>
      <input
        type="number"
        inputMode="numeric"
        min={min}
        max={max}
        value={value}
        disabled={disabled}
        aria-label={label}
        onChange={(e) => dat(parseInt(e.target.value, 10))}
      />
      <button type="button" onClick={() => dat(value + 1)} disabled={disabled || value >= max} aria-label="Tăng số lượng">
        <Icon name="plus" size={16} />
      </button>
    </div>
  );
}
