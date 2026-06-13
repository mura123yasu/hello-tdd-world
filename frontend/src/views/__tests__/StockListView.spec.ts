import { describe, it, expect, vi, beforeEach } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createRouter, createMemoryHistory } from 'vue-router';
import StockListView from '@/views/StockListView.vue';
import * as stockApi from '@/api/stockApi';

vi.mock('@/api/stockApi');

const mockStockList = [
  { productId: 1, productCode: 'P001', productName: 'テスト商品A', quantity: 100 },
  { productId: 2, productCode: 'P002', productName: 'テスト商品B', quantity: 50 },
];

function createTestRouter() {
  return createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/stock', component: StockListView },
      { path: '/stock/:id/transaction', component: { template: '<div>transaction</div>' } },
      {
        path: '/stock/:id/transactions',
        component: { template: '<div>transactions</div>' },
      },
    ],
  });
}

describe('StockListView', () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it('F-07-FE-01 在庫一覧の描画: 商品ごとの数量表示', async () => {
    vi.mocked(stockApi.fetchStockList).mockResolvedValue(mockStockList);

    const router = createTestRouter();
    const wrapper = mount(StockListView, {
      global: { plugins: [router] },
    });
    await flushPromises();

    const rows = wrapper.findAll('[data-testid="stock-row"]');
    expect(rows).toHaveLength(2);
    expect(rows[0].text()).toContain('P001');
    expect(rows[0].text()).toContain('テスト商品A');
    expect(rows[0].text()).toContain('100');
    expect(rows[1].text()).toContain('P002');
    expect(rows[1].text()).toContain('50');
  });
});
