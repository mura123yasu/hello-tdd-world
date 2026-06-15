import { describe, it, expect, vi, beforeEach } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createRouter, createMemoryHistory } from 'vue-router';
import StockTransactionView from '@/views/StockTransactionView.vue';
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

const mockStockBefore = {
  productId: 1,
  productCode: 'P001',
  productName: 'テスト商品A',
  quantity: 100,
};

const mockStockAfterIn = {
  productId: 1,
  productCode: 'P001',
  productName: 'テスト商品A',
  quantity: 150,
};

function createTestRouter(productId = 1) {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      {
        path: '/stock/:id/transaction',
        name: 'stock-transaction',
        component: StockTransactionView,
      },
      { path: '/stock', name: 'stock-list', component: { template: '<div>stock</div>' } },
    ],
  });
  router.push(`/stock/${productId}/transaction`);
  return router;
}

describe('StockTransactionView', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(productApi.fetchProduct).mockResolvedValue(mockProduct);
    vi.mocked(stockApi.fetchStock).mockResolvedValue(mockStockBefore);
  });

  it('F-08-FE-01 入庫フォーム送信: 成功表示と在庫反映', async () => {
    vi.mocked(stockApi.stockIn).mockResolvedValue(mockStockAfterIn);

    const router = createTestRouter(1);
    await router.isReady();

    const wrapper = mount(StockTransactionView, {
      global: { plugins: [router] },
    });
    await flushPromises();

    // 入庫を選択
    await wrapper.get('[data-testid="transaction-type-in"]').trigger('click');
    await wrapper.get('[data-testid="transaction-quantity"]').setValue('50');
    await wrapper.get('[data-testid="submit-transaction"]').trigger('click');
    await flushPromises();

    expect(stockApi.stockIn).toHaveBeenCalledWith(1, expect.objectContaining({ quantity: 50 }));
    expect(wrapper.text()).toContain('入庫しました');
    // 在庫数量が更新されていること
    expect(wrapper.text()).toContain('150');
  });

  it('F-09-FE-01 出庫の在庫不足エラー表示: エラーメッセージが表示される', async () => {
    const stockError = Object.assign(new Error('Insufficient stock'), {
      response: { status: 409, data: { message: '在庫が不足しています' } },
    });
    vi.mocked(stockApi.stockOut).mockRejectedValue(stockError);

    const router = createTestRouter(1);
    await router.isReady();

    const wrapper = mount(StockTransactionView, {
      global: { plugins: [router] },
    });
    await flushPromises();

    // 出庫を選択
    await wrapper.get('[data-testid="transaction-type-out"]').trigger('click');
    await wrapper.get('[data-testid="transaction-quantity"]').setValue('999');
    await wrapper.get('[data-testid="submit-transaction"]').trigger('click');
    await flushPromises();

    expect(stockApi.stockOut).toHaveBeenCalledWith(
      1,
      expect.objectContaining({ quantity: 999 }),
    );
    expect(wrapper.text()).toContain('在庫が不足しています');
  });
});
