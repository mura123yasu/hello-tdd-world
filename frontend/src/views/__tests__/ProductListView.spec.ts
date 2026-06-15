import { describe, it, expect, vi, beforeEach } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createRouter, createMemoryHistory } from 'vue-router';
import ProductListView from '@/views/ProductListView.vue';
import * as productApi from '@/api/productApi';
import * as categoryApi from '@/api/categoryApi';

vi.mock('@/api/productApi');
vi.mock('@/api/categoryApi');

const mockCategories = [
  { categoryId: 1, categoryName: '電子機器' },
  { categoryId: 2, categoryName: '食料品' },
];

const mockSearchResult = {
  items: [
    {
      productId: 1,
      productCode: 'P001',
      productName: 'テスト商品A',
      categoryId: 1,
      categoryName: '電子機器',
      unitPrice: 1000,
      status: 'ACTIVE' as const,
      version: 0,
    },
    {
      productId: 2,
      productCode: 'P002',
      productName: 'テスト商品B',
      categoryId: 2,
      categoryName: '食料品',
      unitPrice: 500,
      status: 'ACTIVE' as const,
      version: 0,
    },
  ],
  totalCount: 2,
  page: 0,
  size: 20,
};

function createTestRouter() {
  return createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: ProductListView },
      { path: '/products/new', component: { template: '<div>new</div>' } },
      { path: '/products/:id', component: { template: '<div>detail</div>' } },
    ],
  });
}

describe('ProductListView', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(categoryApi.fetchCategories).mockResolvedValue(mockCategories);
  });

  it('F-01-FE-01 検索フォーム入力→一覧描画: 結果行が描画される', async () => {
    vi.mocked(productApi.searchProducts).mockResolvedValue(mockSearchResult);

    const router = createTestRouter();
    const wrapper = mount(ProductListView, {
      global: { plugins: [router] },
    });
    await flushPromises();

    const rows = wrapper.findAll('[data-testid="product-row"]');
    expect(rows).toHaveLength(2);
    expect(rows[0].text()).toContain('P001');
    expect(rows[0].text()).toContain('テスト商品A');
    expect(rows[1].text()).toContain('P002');
  });

  it('F-01-FE-02 0件時の空表示: 「該当データなし」を表示', async () => {
    vi.mocked(productApi.searchProducts).mockResolvedValue({
      items: [],
      totalCount: 0,
      page: 0,
      size: 20,
    });

    const router = createTestRouter();
    const wrapper = mount(ProductListView, {
      global: { plugins: [router] },
    });
    await flushPromises();

    expect(wrapper.text()).toContain('該当データなし');
    expect(wrapper.findAll('[data-testid="product-row"]')).toHaveLength(0);
  });

  it('F-06-FE-01 プルダウンに反映: カテゴリ選択肢が描画される', async () => {
    vi.mocked(productApi.searchProducts).mockResolvedValue(mockSearchResult);

    const router = createTestRouter();
    const wrapper = mount(ProductListView, {
      global: { plugins: [router] },
    });
    await flushPromises();

    const options = wrapper.findAll('[data-testid="category-option"]');
    expect(options.length).toBeGreaterThanOrEqual(2);
    const texts = options.map((o) => o.text());
    expect(texts).toContain('電子機器');
    expect(texts).toContain('食料品');
  });
});
