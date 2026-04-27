import { describe, expect, it, vi, beforeEach } from 'vitest';
import { getProductDetail, getProducts } from './productsApi';

const products = [{ id: '1', brand: 'Apple', model: 'iPhone', price: '799', imgUrl: '/iphone.png' }];
const productDetail = {
  id: '1',
  brand: 'Apple',
  model: 'iPhone',
  price: '799',
  imgUrl: '/iphone.png',
  cpu: 'A15',
  ram: '4GB',
  os: 'iOS',
  displayResolution: '2532x1170',
  battery: '3095mAh',
  primaryCamera: '12MP',
  secondaryCamera: '12MP',
  dimensions: '146.7 x 71.5 x 7.7 mm',
  weight: '174g',
  options: {
    colors: [{ code: 1, name: 'Black' }],
    storages: [{ code: 2, name: '128GB' }]
  }
};

describe('productsApi', () => {
  beforeEach(() => {
    window.localStorage.clear();
    vi.restoreAllMocks();
  });

  it('caches product list in localStorage', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue({ ok: true, json: async () => products } as Response);

    const first = await getProducts();
    const second = await getProducts();

    expect(first).toEqual(products);
    expect(second).toEqual(products);
    expect(fetchMock).toHaveBeenCalledTimes(1);
  });

  it('caches product details in localStorage', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue({ ok: true, json: async () => productDetail } as Response);

    const first = await getProductDetail('1');
    const second = await getProductDetail('1');

    expect(first).toEqual(productDetail);
    expect(second).toEqual(productDetail);
    expect(fetchMock).toHaveBeenCalledTimes(1);
  });
});
