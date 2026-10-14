export default function ErrorMessage({ children }) {
  if (!children) return null;
  return (
    <p className="alert" role="alert">
      {children}
    </p>
  );
}
