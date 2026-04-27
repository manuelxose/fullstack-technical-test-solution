import { Link, useLocation } from 'react-router-dom';
import { useCart } from '../app/CartContext';

export function Header() {
  const { count } = useCart();
  const location = useLocation();
  const isDetail = location.pathname.startsWith('/product/');

  return (
    <header className="app-header">
      <div className="header-topline">
        <Link className="brand-link" to="/" aria-label="Ir al listado de productos">
          <span className="brand-mark">ITX</span>
          <span className="brand-text">Mobile Store</span>
        </Link>
        <div className="cart-counter" aria-label={`Productos en cesta: ${count}`}>
          <span className="cart-icon" aria-hidden="true">🛒</span>
          <span>{count}</span>
        </div>
      </div>
      <nav className="breadcrumbs" aria-label="Breadcrumb">
        <Link to="/">Productos</Link>
        {isDetail && <><span>/</span><span>Detalle</span></>}
      </nav>
    </header>
  );
}
