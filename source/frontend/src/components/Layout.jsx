import { useEffect } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import Header from './Header.jsx';
import Footer from './Footer.jsx';

/** Cuộn lên đầu khi đổi trang; có #hash thì cuộn tới phần tử tương ứng. */
function useScrollRestore() {
  const { pathname, hash } = useLocation();
  useEffect(() => {
    if (hash) {
      const el = document.getElementById(decodeURIComponent(hash.slice(1)));
      if (el) {
        el.scrollIntoView({ block: 'start' });
        return;
      }
    }
    window.scrollTo({ top: 0, left: 0, behavior: 'instant' });
  }, [pathname, hash]);
}

export default function Layout() {
  const { pathname } = useLocation();
  useScrollRestore();
  const trangChu = pathname === '/';

  return (
    <div className="app">
      <a href="#noi-dung" className="skip-link">Bỏ qua tới nội dung</a>
      <Header />
      <main id="noi-dung" className={trangChu ? 'main main-full' : 'main container'}>
        <div className="page-transition" key={pathname}>
          <Outlet />
        </div>
      </main>
      <Footer />
    </div>
  );
}
