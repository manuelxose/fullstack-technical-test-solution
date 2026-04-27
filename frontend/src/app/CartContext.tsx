import { createContext, useContext, useMemo, useState, type ReactNode } from 'react';
import { readStoredCartCount, storeCartCount } from '../features/cart/model/cartStorage';

type CartContextValue = {
  count: number;
  setCount: (count: number) => void;
};

const CartContext = createContext<CartContextValue | undefined>(undefined);

export function CartProvider({ children }: { children: ReactNode }) {
  const [count, setCountState] = useState(() => readStoredCartCount());

  const value = useMemo<CartContextValue>(() => ({
    count,
    setCount: (newCount: number) => {
      setCountState(newCount);
      storeCartCount(newCount);
    }
  }), [count]);

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export function useCart(): CartContextValue {
  const context = useContext(CartContext);
  if (!context) throw new Error('useCart must be used inside CartProvider');
  return context;
}
