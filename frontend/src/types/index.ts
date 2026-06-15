// ==============================
// 共通型定義
// ==============================

export interface Category {
  categoryId: number;
  categoryName: string;
}

export interface Product {
  productId: number;
  productCode: string;
  productName: string;
  categoryId: number;
  categoryName?: string;
  unitPrice: number;
  status: 'ACTIVE' | 'INACTIVE';
  description?: string;
  version: number;
}

export interface ProductSearchParams {
  code?: string;
  name?: string;
  categoryId?: number;
  status?: string;
  page?: number;
  size?: number;
  sort?: string;
}

export interface ProductSearchResult {
  items: Product[];
  totalCount: number;
  page: number;
  size: number;
}

export interface ProductCreateRequest {
  productCode: string;
  productName: string;
  categoryId: number;
  unitPrice: number;
  status: 'ACTIVE' | 'INACTIVE';
  description?: string;
}

export interface ProductUpdateRequest {
  productName: string;
  categoryId: number;
  unitPrice: number;
  status: 'ACTIVE' | 'INACTIVE';
  description?: string;
  version: number;
}

export interface Stock {
  productId: number;
  productCode: string;
  productName: string;
  quantity: number;
}

export interface StockTransactionRequest {
  quantity: number;
  note?: string;
}

export interface StockTransaction {
  transactionId: number;
  productId: number;
  transactionType: 'IN' | 'OUT';
  quantity: number;
  transactionAt: string;
  note?: string;
}
