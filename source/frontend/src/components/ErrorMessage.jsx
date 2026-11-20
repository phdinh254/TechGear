import Icon from './ui/Icon.jsx';

export default function ErrorMessage({ children }) {
  if (!children) return null;
  return (
    <p className="alert alert-error" role="alert">
      <Icon name="close" size={16} strokeWidth={2.2} />
      <span>{children}</span>
    </p>
  );
}
