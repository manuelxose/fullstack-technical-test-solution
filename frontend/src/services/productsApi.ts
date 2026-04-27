import { cache } from '../shared/cache';
import { getJson, postJson } from './apiClient';
import type { AddToCartRequest, AddToCartResponse, ProductDetail, ProductSummary } from '../features/products/model/Product';

export async function getProducts(signal?: AbortSignal): Promise<ProductSummary[]> {
  const cacheKey = 'products:list';
  const cached = cache.get<ProductSummary[]>(cacheKey);
  if (cached) return cached;
  const products = await getJson<ProductSummary[]>('/api/product', signal);
  cache.set(cacheKey, products);
  return products;
}

export async function getProductDetail(productId: string, signal?: AbortSignal): Promise<ProductDetail> {
  const cacheKey = `products:detail:${productId}`;
  const cached = cache.get<ProductDetail>(cacheKey);
  if (cached) return cached;
  const product = await getJson<ProductDetail>(`/api/product/${productId}`, signal);
  cache.set(cacheKey, product);
  return product;
}

export async function addProductToCart(request: AddToCartRequest, signal?: AbortSignal): Promise<AddToCartResponse> {
  return postJson<AddToCartRequest, AddToCartResponse>('/api/cart', request, signal);
}
