import { useEffect, useMemo, useState } from 'react';
import { getProducts } from '../../../services/productsApi';
import { ProductCard } from '../components/ProductCard';
import { SearchBox } from '../components/SearchBox';
import type { ProductSummary } from '../model/Product';

export function ProductListPage() {
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [query, setQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setError(false);
    getProducts(controller.signal)
      .then(setProducts)
      .catch((error: unknown) => {
        if (error instanceof DOMException && error.name === 'AbortError') return;
        setError(true);
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false);
      });
    return () => controller.abort();
  }, []);

  const filteredProducts = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();
    if (!normalizedQuery) return products;
    return products.filter((product) => {
      const target = `${product.brand} ${product.model}`.toLowerCase();
      return target.includes(normalizedQuery);
    });
  }, [products, query]);

  if (loading) return <main className="page-state">Cargando productos...</main>;
  if (error) return <main className="page-state error">No se pudo cargar el catálogo.</main>;

  return (
    <main className="page-shell">
      <SearchBox value={query} onChange={setQuery} total={products.length} visible={filteredProducts.length} />
      {filteredProducts.length === 0 ? (
        <section className="empty-state">No hay productos para esa búsqueda.</section>
      ) : (
        <section className="product-grid" aria-label="Listado de productos">
          {filteredProducts.map((product) => <ProductCard key={product.id} product={product} />)}
        </section>
      )}
    </main>
  );
}
