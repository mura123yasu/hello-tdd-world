<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { searchProducts } from '@/api/productApi';
import { fetchCategories } from '@/api/categoryApi';
import type { Product, Category, ProductSearchParams } from '@/types';

const router = useRouter();

const categories = ref<Category[]>([]);
const products = ref<Product[]>([]);
const totalCount = ref(0);

const searchParams = ref<ProductSearchParams>({
  code: '',
  name: '',
  categoryId: undefined,
  status: '',
  page: 0,
  size: 20,
});

async function loadCategories() {
  categories.value = await fetchCategories();
}

async function doSearch() {
  const params: ProductSearchParams = { ...searchParams.value };
  if (!params.code) delete params.code;
  if (!params.name) delete params.name;
  if (!params.status) delete params.status;
  if (params.categoryId === undefined || params.categoryId === 0) delete params.categoryId;

  const result = await searchProducts(params);
  products.value = result.items;
  totalCount.value = result.totalCount;
}

onMounted(async () => {
  await loadCategories();
  await doSearch();
});

function goToNew() {
  router.push('/products/new');
}

function goToDetail(productId: number) {
  router.push(`/products/${productId}`);
}
</script>

<template>
  <div>
    <h1>商品検索一覧</h1>

    <section data-testid="search-form">
      <input
        v-model="searchParams.code"
        data-testid="search-code"
        placeholder="商品コード"
        type="text"
      />
      <input
        v-model="searchParams.name"
        data-testid="search-name"
        placeholder="商品名"
        type="text"
      />
      <select v-model="searchParams.categoryId" data-testid="search-category">
        <option :value="undefined">-- カテゴリ選択 --</option>
        <option
          v-for="cat in categories"
          :key="cat.categoryId"
          :value="cat.categoryId"
          data-testid="category-option"
        >
          {{ cat.categoryName }}
        </option>
      </select>
      <select v-model="searchParams.status" data-testid="search-status">
        <option value="">-- ステータス選択 --</option>
        <option value="ACTIVE">ACTIVE</option>
        <option value="INACTIVE">INACTIVE</option>
      </select>
      <button data-testid="search-button" @click="doSearch">検索</button>
      <button data-testid="new-button" @click="goToNew">新規登録</button>
    </section>

    <section data-testid="search-result">
      <p v-if="products.length === 0" data-testid="empty-message">該当データなし</p>
      <table v-else>
        <thead>
          <tr>
            <th>商品コード</th>
            <th>商品名</th>
            <th>カテゴリ</th>
            <th>単価</th>
            <th>ステータス</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="product in products"
            :key="product.productId"
            data-testid="product-row"
            style="cursor: pointer"
            @click="goToDetail(product.productId)"
          >
            <td>{{ product.productCode }}</td>
            <td>{{ product.productName }}</td>
            <td>{{ product.categoryName }}</td>
            <td>{{ product.unitPrice }}</td>
            <td>{{ product.status }}</td>
          </tr>
        </tbody>
      </table>
      <p>全 {{ totalCount }} 件</p>
    </section>
  </div>
</template>
