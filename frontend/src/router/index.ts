import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'home',
    component: () => import('@/views/HomeView.vue'),
  },
  {
    path: '/products',
    name: 'product-list',
    component: () => import('@/views/ProductListView.vue'),
  },
  {
    path: '/products/new',
    name: 'product-new',
    component: () => import('@/views/ProductDetailView.vue'),
  },
  {
    path: '/products/:id',
    name: 'product-detail',
    component: () => import('@/views/ProductDetailView.vue'),
  },
  {
    path: '/stock',
    name: 'stock-list',
    component: () => import('@/views/StockListView.vue'),
  },
  {
    path: '/stock/:id/transaction',
    name: 'stock-transaction',
    component: () => import('@/views/StockTransactionView.vue'),
  },
  {
    path: '/stock/:id/transactions',
    name: 'stock-history',
    component: () => import('@/views/StockHistoryView.vue'),
  },
];

export const router = createRouter({
  history: createWebHistory(),
  routes,
});
