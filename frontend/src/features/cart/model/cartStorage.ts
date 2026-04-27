const CART_COUNT_KEY = 'itx-cart-count';

export function readStoredCartCount(): number {
  const value = window.localStorage.getItem(CART_COUNT_KEY);
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
}

export function storeCartCount(count: number): void {
  window.localStorage.setItem(CART_COUNT_KEY, String(count));
}
