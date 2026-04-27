import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { StrictMode } from 'react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { CartProvider } from '../../../app/CartContext';
import { ProductDetailPage } from './ProductDetailPage';
import { addProductToCart, getProductDetail } from '../../../services/productsApi';

vi.mock('../../../services/productsApi', () => ({
  addProductToCart: vi.fn(),
  getProductDetail: vi.fn()
}));

const product = {
  id: '1',
  brand: 'Apple',
  model: 'iPhone 13',
  price: '799',
  imgUrl: '/iphone.png',
  cpu: 'A15 Bionic',
  ram: '4GB',
  os: 'iOS',
  displayResolution: '2532 x 1170',
  battery: '3095 mAh',
  primaryCamera: '12 MP',
  secondaryCamera: '12 MP',
  dimensions: '146.7 x 71.5 x 7.7 mm',
  weight: '174 g',
  options: {
    colors: [
      { code: 10, name: 'Black' },
      { code: 11, name: 'Blue' }
    ],
    storages: [
      { code: 20, name: '128GB' },
      { code: 21, name: '256GB' }
    ]
  }
};

describe('ProductDetailPage', () => {
  beforeEach(() => {
    window.localStorage.clear();
    vi.mocked(getProductDetail).mockResolvedValue(product);
    vi.mocked(addProductToCart).mockResolvedValue({ count: 3 });
  });

  it('loads default options and posts them to the cart', async () => {
    render(
      <MemoryRouter initialEntries={['/product/1']}>
        <CartProvider>
          <Routes>
            <Route path="/product/:productId" element={<ProductDetailPage />} />
          </Routes>
        </CartProvider>
      </MemoryRouter>
    );

    await waitFor(() => expect(screen.getByRole('heading', { name: /apple iphone 13/i })).toBeInTheDocument());

    expect(screen.getByLabelText(/almacenamiento/i)).toHaveValue('20');
    expect(screen.getByLabelText(/color/i)).toHaveValue('10');

    await userEvent.click(screen.getByRole('button', { name: /añadir a la cesta/i }));

    expect(addProductToCart).toHaveBeenCalledWith({ id: '1', colorCode: 10, storageCode: 20 });
    await waitFor(() => expect(window.localStorage.getItem('itx-cart-count')).toBe('3'));
  });

  it('ignores aborted detail requests during development remounts', async () => {
    vi.mocked(getProductDetail)
      .mockRejectedValueOnce(new DOMException('Request aborted', 'AbortError'))
      .mockResolvedValue(product);

    render(
      <StrictMode>
        <MemoryRouter initialEntries={['/product/1']}>
          <CartProvider>
            <Routes>
              <Route path="/product/:productId" element={<ProductDetailPage />} />
            </Routes>
          </CartProvider>
        </MemoryRouter>
      </StrictMode>
    );

    await waitFor(() => expect(screen.getByRole('heading', { name: /apple iphone 13/i })).toBeInTheDocument());

    expect(screen.queryByText(/no se pudo cargar el producto/i)).not.toBeInTheDocument();
  });
});
