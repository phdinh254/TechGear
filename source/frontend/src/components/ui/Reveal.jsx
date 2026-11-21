import { createElement, useEffect, useRef, useState } from 'react';

const coIO = typeof window !== 'undefined' && 'IntersectionObserver' in window;

/**
 * Hiện nội dung khi cuộn tới (fade-up). Các phần tử con có class "reveal-item"
 * xuất hiện lần lượt (stagger) theo biến CSS --i.
 * Tôn trọng prefers-reduced-motion qua CSS.
 */
export default function Reveal({ as = 'div', className = '', children, delay = 0, ...rest }) {
  const ref = useRef(null);
  const [hien, setHien] = useState(!coIO);

  useEffect(() => {
    if (!coIO || !ref.current) return undefined;
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setHien(true);
          observer.disconnect();
        }
      },
      { rootMargin: '0px 0px -8% 0px', threshold: 0.08 },
    );
    observer.observe(ref.current);
    return () => observer.disconnect();
  }, []);

  return createElement(
    as,
    {
      ref,
      className: `reveal ${hien ? 'is-visible' : ''} ${className}`.trim(),
      style: delay ? { '--reveal-delay': `${delay}ms` } : undefined,
      ...rest,
    },
    children,
  );
}
