import { describe, it, expect, vi, beforeEach } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createRouter, createMemoryHistory } from 'vue-router';
import StockHistoryView from '@/views/StockHistoryView.vue';
import * as stockApi from '@/api/stockApi';
import * as productApi from '@/api/productApi';

vi.mock('@/api/stockApi');
vi.mock('@/api/productApi');

const mockProduct = {
  productId: 1,
  productCode: 'P001',
  productName: 'テスト商品A',
  categoryId: 1,
  unitPrice: 1000,
  status: 'ACTIVE' as const,
  version: 0,
};

const mockTransactions = [
  {
    transactionId: 1,
    productId: 1,
    transactionType: 'IN' as const,
    quantity: 100,
    transactionAt: '2024-01-01T10:00:00',
    note: '初回入庫',
  },
  {
    transactionId: 2,
    productId: 1,
    transactionType: 'OUT' as const,
    quantity: 30,
    transactionAt: '2024-01-02T10:00:00',
    note: '出庫',
  },
];

function createTestRouter(productId = 1) {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      {
        path: '/stock/:id/transactions',
        name: 'stock-history',
        component: StockHistoryView,
      },
      { path: '/stock', name: 'stock-list', component: { template: '<div>stock</div>' } },
    ],
  });
  router.push(`/stock/${productId}/transactions`);
  return router;
}

describe('StockHistoryView', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(productApi.fetchProduct).mockResolvedValue(mockProduct);
  });

  it('F-10-FE-01 履歴一覧の描画: 行が描画される', async () => {
    vi.mocked(stockApi.fetchStockTransactions).mockResolvedValue(mockTransactions);

    const router = createTestRouter(1);
    await router.isReady();

    const wrapper = mount(StockHistoryView, {
      global: { plugins: [router] },
    });
    await flushPromises();

    const rows = wrapper.findAll('[data-testid="transaction-row"]');
    expect(rows).toHaveLength(2);
    expect(rows[0].text()).toContain('IN');
    expect(rows[0].text()).toContain('100');
    expect(rows[1].text()).toContain('OUT');
    expect(rows[1].text()).toContain('30');
  });
});
