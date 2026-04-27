import { useEffect, useMemo, useState } from 'react';
import { addProductToCart } from '../../../services/productsApi';
import { useCart } from '../../../app/CartContext';
import type { ProductDetail } from '../model/Product';

export function ProductActions({ product }: { product: ProductDetail }) {
  const { setCount } = useCart();
  const defaultColor = product.options.colors[0]?.code;
  const defaultStorage = product.options.storages[0]?.code;
  const [colorCode, setColorCode] = useState<number | undefined>(defaultColor);
  const [storageCode, setStorageCode] = useState<number | undefined>(defaultStorage);
  const [status, setStatus] = useState<'idle' | 'loading' | 'success' | 'error'>('idle');

  useEffect(() => {
    setColorCode(defaultColor);
    setStorageCode(defaultStorage);
  }, [defaultColor, defaultStorage]);

  const disabled = useMemo(() => status === 'loading' || colorCode === undefined || storageCode === undefined, [status, colorCode, storageCode]);

  async function handleAddToCart() {
    if (colorCode === undefined || storageCode === undefined) return;
    setStatus('loading');
    try {
      const response = await addProductToCart({ id: product.id, colorCode, storageCode });
      setCount(response.count);
      setStatus('success');
    } catch {
      setStatus('error');
    }
  }

  return (
    <section className="actions-card" aria-labelledby="product-actions-title">
      <h2 id="product-actions-title">Configura tu dispositivo</h2>
      <label>
        <span>Almacenamiento</span>
        <select value={storageCode ?? ''} onChange={(event) => setStorageCode(Number(event.target.value))}>
          {product.options.storages.map((storage) => (
            <option key={storage.code} value={storage.code}>{storage.name}</option>
          ))}
        </select>
      </label>
      <label>
        <span>Color</span>
        <select value={colorCode ?? ''} onChange={(event) => setColorCode(Number(event.target.value))}>
          {product.options.colors.map((color) => (
            <option key={color.code} value={color.code}>{color.name}</option>
          ))}
        </select>
      </label>
      <button type="button" disabled={disabled} onClick={handleAddToCart}>
        {status === 'loading' ? 'Añadiendo...' : 'Añadir a la cesta'}
      </button>
      {status === 'success' && <p className="status-message success">Producto añadido correctamente.</p>}
      {status === 'error' && <p className="status-message error">No se pudo añadir el producto. Inténtalo de nuevo.</p>}
    </section>
  );
}
