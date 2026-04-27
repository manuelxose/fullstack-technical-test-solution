import type { ProductDetail } from '../model/Product';

export function ProductDescription({ product }: { product: ProductDetail }) {
  const details = [
    ['Marca', product.brand],
    ['Modelo', product.model],
    ['Precio', formatPrice(product.price)],
    ['CPU', product.cpu],
    ['RAM', product.ram],
    ['Sistema operativo', product.os],
    ['Resolución de pantalla', product.displayResolution],
    ['Batería', product.battery],
    ['Cámara principal', formatValue(product.primaryCamera)],
    ['Cámara secundaria', formatValue(product.secondaryCamera ?? product.secondaryCmera)],
    ['Dimensiones', product.dimensions ?? product.dimentions ?? '-'],
    ['Peso', product.weight]
  ];

  return (
    <section className="detail-card" aria-labelledby="product-description-title">
      <h1 id="product-description-title">{product.brand} {product.model}</h1>
      <dl className="description-list">
        {details.map(([label, value]) => (
          <div key={label} className="description-row">
            <dt>{label}</dt>
            <dd>{value}</dd>
          </div>
        ))}
      </dl>
    </section>
  );
}

function formatValue(value: string | string[] | undefined): string {
  if (!value) return '-';
  return Array.isArray(value) ? value.join(', ') : value;
}

function formatPrice(price: string | number): string {
  const numericPrice = Number(price);
  if (Number.isFinite(numericPrice)) return `${numericPrice.toLocaleString('es-ES')} €`;
  return `${price} €`;
}
