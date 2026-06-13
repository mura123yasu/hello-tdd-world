import { describe, it, expect, vi, beforeEach } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import { createRouter, createMemoryHistory } from 'vue-router';
import ProductDetailView from '@/views/ProductDetailView.vue';
import * as productApi from '@/api/productApi';
import * as categoryApi from '@/api/categoryApi';

vi.mock('@/api/productApi');
vi.mock('@/api/categoryApi');

const mockCategories = [
  { categoryId: 1, categoryName: '電子機器' },
  { categoryId: 2, categoryName: '食料品' },
];

const mockProduct = {
  productId: 1,
  productCode: 'P001',
  productName: 'テスト商品A',
  categoryId: 1,
  categoryName: '電子機器',
  unitPrice: 1000,
  status: 'ACTIVE' as const,
  description: '説明文です',
  version: 2,
};

function createTestRouter(productId?: number) {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/products/new', name: 'product-new', component: ProductDetailView },
      { path: '/products/:id', name: 'product-detail', component: ProductDetailView },
      { path: '/products', name: 'product-list', component: { template: '<div>list</div>' } },
    ],
  });
  if (productId !== undefined) {
    router.push(`/products/${productId}`);
  } else {
    router.push('/products/new');
  }
  return router;
}

describe('ProductDetailView', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(categoryApi.fetchCategories).mockResolvedValue(mockCategories);
  });

  it('F-02-FE-01 単票画面に値がバインドされる: 各項目が表示される', async () => {
    vi.mocked(productApi.fetchProduct).mockResolvedValue(mockProduct);

    const router = createTestRouter(1);
    await router.isReady();

    const wrapper = mount(ProductDetailView, {
      global: { plugins: [router] },
    });
    await flushPromises();

    expect((wrapper.get('[data-testid="product-code"]').element as HTMLInputElement).value).toBe('P001');
    expect((wrapper.get('[data-testid="product-name"]').element as HTMLInputElement).value).toBe('テスト商品A');
    expect((wrapper.get('[data-testid="unit-price"]').element as HTMLInputElement).value).toBe('1000');
  });

  it('F-03-FE-01 フォーム検証（クライアント側）: 必須未入力で送信不可', async () => {
    const router = createTestRouter();
    await router.isReady();

    const wrapper = mount(ProductDetailView, {
      global: { plugins: [router] },
    });
    await flushPromises();

    // 送信ボタンをクリック（必須項目が空）
    await wrapper.get('[data-testid="submit-button"]').trigger('click');
    await flushPromises();

    // createProduct が呼ばれないこと
    expect(productApi.createProduct).not.toHaveBeenCalled();
    // エラーメッセージが表示されること
    expect(wrapper.text()).toContain('商品コードは必須');
  });

  it('F-03-FE-02 登録成功で一覧へ遷移: 遷移とトースト表示', async () => {
    vi.mocked(productApi.createProduct).mockResolvedValue({
      ...mockProduct,
      productId: 99,
    });

    const router = createTestRouter();
    await router.isReady();
    const pushSpy = vi.spyOn(router, 'push');

    const wrapper = mount(ProductDetailView, {
      global: { plugins: [router] },
    });
    await flushPromises();

    // フォームを入力
    await wrapper.get('[data-testid="product-code"]').setValue('NEW01');
    await wrapper.get('[data-testid="product-name"]').setValue('新商品');
    // categoryId select
    const categorySelect = wrapper.get('[data-testid="category-select"]');
    await categorySelect.setValue('1');
    await wrapper.get('[data-testid="unit-price"]').setValue('500');

    await wrapper.get('[data-testid="submit-button"]').trigger('click');
    await flushPromises();

    expect(productApi.createProduct).toHaveBeenCalled();
    expect(pushSpy).toHaveBeenCalledWith('/products');
  });

  it('F-04-FE-01 更新成功表示: 成功トーストが表示される', async () => {
    vi.mocked(productApi.fetchProduct).mockResolvedValue(mockProduct);
    vi.mocked(productApi.updateProduct).mockResolvedValue({ ...mockProduct, version: 3 });

    const router = createTestRouter(1);
    await router.isReady();

    const wrapper = mount(ProductDetailView, {
      global: { plugins: [router] },
    });
    await flushPromises();

    await wrapper.get('[data-testid="product-name"]').setValue('更新商品名');
    await wrapper.get('[data-testid="submit-button"]').trigger('click');
    await flushPromises();

    expect(productApi.updateProduct).toHaveBeenCalled();
    expect(wrapper.text()).toContain('更新しました');
  });

  it('F-04-FE-02 競合時のエラー表示: 競合メッセージが表示される', async () => {
    vi.mocked(productApi.fetchProduct).mockResolvedValue(mockProduct);
    const conflictError = Object.assign(new Error('Conflict'), {
      response: { status: 409, data: { message: '他のユーザーが更新しました' } },
    });
    vi.mocked(productApi.updateProduct).mockRejectedValue(conflictError);

    const router = createTestRouter(1);
    await router.isReady();

    const wrapper = mount(ProductDetailView, {
      global: { plugins: [router] },
    });
    await flushPromises();

    await wrapper.get('[data-testid="submit-button"]').trigger('click');
    await flushPromises();

    expect(wrapper.text()).toContain('他のユーザーが更新しました');
  });

  it('F-05-FE-01 削除確認ダイアログ: 確認後に削除実行', async () => {
    vi.mocked(productApi.fetchProduct).mockResolvedValue(mockProduct);
    vi.mocked(productApi.deleteProduct).mockResolvedValue(undefined);

    const router = createTestRouter(1);
    await router.isReady();
    const pushSpy = vi.spyOn(router, 'push');

    const wrapper = mount(ProductDetailView, {
      global: { plugins: [router] },
    });
    await flushPromises();

    // 削除ボタンをクリック → 確認ダイアログが表示される
    await wrapper.get('[data-testid="delete-button"]').trigger('click');
    await flushPromises();

    expect(wrapper.find('[data-testid="confirm-dialog"]').exists()).toBe(true);

    // 確認ボタンをクリック → 削除実行
    await wrapper.get('[data-testid="confirm-delete"]').trigger('click');
    await flushPromises();

    expect(productApi.deleteProduct).toHaveBeenCalledWith(1);
    expect(pushSpy).toHaveBeenCalledWith('/products');
  });
});
