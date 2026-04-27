import { Link } from 'react-router-dom';
import type { ProductSummary } from '../model/Product';

export function ProductCard({ product }: { product: ProductSummary }) {
  return (
    <article className="product-card">
      <Link to={`/product/${product.id}`} aria-label={`Ver detalle de ${product.brand} ${product.model}`}>
        <div className="product-image-frame">
          <img src={product.imgUrl} alt={`${product.brand} ${product.model}`} loading="lazy" />
        </div>
        <div className="product-card-body">
          <p className="product-brand">{product.brand}</p>
          <h2>{product.model}</h2>
          <p className="product-price">{formatPrice(product.price)}</p>
        </div>
      </Link>
    </article>
  );
}

function formatPrice(price: string | number): string {
  if (price === '' || price === undefined || price === null) return 'Precio no disponible';
  const numericPrice = Number(price);
  if (Number.isFinite(numericPrice)) return `${numericPrice.toLocaleString('es-ES')} €`;
  return `${price} €`;
}
