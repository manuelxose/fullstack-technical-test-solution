import { Link, useParams } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { getProductDetail } from '../../../services/productsApi';
import { ProductActions } from '../components/ProductActions';
import { ProductDescription } from '../components/ProductDescription';
import type { ProductDetail } from '../model/Product';

export function ProductDetailPage() {
  const { productId } = useParams<{ productId: string }>();
  const [product, setProduct] = useState<ProductDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (!productId) return;
    const controller = new AbortController();
    setLoading(true);
    setError(false);
    getProductDetail(productId, controller.signal)
      .then(setProduct)
      .catch((error: unknown) => {
        if (error instanceof DOMException && error.name === 'AbortError') return;
        setError(true);
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false);
      });
    return () => controller.abort();
  }, [productId]);

  if (loading) return <main className="page-state">Cargando detalle...</main>;
  if (error || !product) return <main className="page-state error">No se pudo cargar el producto.</main>;

  return (
    <main className="page-shell detail-shell">
      <Link className="back-link" to="/">← Volver al listado</Link>
      <section className="detail-layout">
        <div className="detail-image-panel">
          <img src={product.imgUrl} alt={`${product.brand} ${product.model}`} />
        </div>
        <div className="detail-info-panel">
          <ProductDescription product={product} />
          <ProductActions product={product} />
        </div>
      </section>
    </main>
  );
}
