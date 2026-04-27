interface SearchBoxProps {
  value: string;
  onChange: (value: string) => void;
  total: number;
  visible: number;
}

export function SearchBox({ value, onChange, total, visible }: SearchBoxProps) {
  return (
    <section className="search-panel" aria-label="Búsqueda de productos">
      <div>
        <h1>Dispositivos móviles</h1>
        <p>{visible} de {total} productos</p>
      </div>
      <label className="search-field">
        <span>Buscar por marca o modelo</span>
        <input
          value={value}
          onChange={(event) => onChange(event.target.value)}
          placeholder="Ej. Apple, Galaxy, Xperia..."
          autoComplete="off"
        />
      </label>
    </section>
  );
}
