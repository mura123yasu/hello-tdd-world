import { apiClient } from '@/api/client';
import type {
  Product,
  ProductSearchParams,
  ProductSearchResult,
  ProductCreateRequest,
  ProductUpdateRequest,
} from '@/types';

export async function searchProducts(params: ProductSearchParams): Promise<ProductSearchResult> {
  const response = await apiClient.get<ProductSearchResult>('/products', { params });
  return response.data;
}

export async function fetchProduct(id: number): Promise<Product> {
  const response = await apiClient.get<Product>(`/products/${id}`);
  return response.data;
}

export async function createProduct(data: ProductCreateRequest): Promise<Product> {
  const response = await apiClient.post<Product>('/products', data);
  return response.data;
}

export async function updateProduct(id: number, data: ProductUpdateRequest): Promise<Product> {
  const response = await apiClient.put<Product>(`/products/${id}`, data);
  return response.data;
}

export async function deleteProduct(id: number): Promise<void> {
  await apiClient.delete(`/products/${id}`);
}
