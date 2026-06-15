import { apiClient } from '@/api/client';
import type { Stock, StockTransaction, StockTransactionRequest } from '@/types';

export async function fetchStockList(): Promise<Stock[]> {
  const response = await apiClient.get<Stock[]>('/products/stock');
  return response.data;
}

export async function fetchStock(productId: number): Promise<Stock> {
  const response = await apiClient.get<Stock>(`/products/${productId}/stock`);
  return response.data;
}

export async function stockIn(productId: number, data: StockTransactionRequest): Promise<Stock> {
  const response = await apiClient.post<Stock>(`/products/${productId}/stock/in`, data);
  return response.data;
}

export async function stockOut(productId: number, data: StockTransactionRequest): Promise<Stock> {
  const response = await apiClient.post<Stock>(`/products/${productId}/stock/out`, data);
  return response.data;
}

export async function fetchStockTransactions(productId: number): Promise<StockTransaction[]> {
  const response = await apiClient.get<StockTransaction[]>(
    `/products/${productId}/stock/transactions`,
  );
  return response.data;
}
