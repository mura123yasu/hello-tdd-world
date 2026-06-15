<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { fetchProduct } from '@/api/productApi';
import { fetchStockTransactions } from '@/api/stockApi';
import type { Product, StockTransaction } from '@/types';

const route = useRoute();
const router = useRouter();

const productId = computed(() => Number(route.params.id));

const product = ref<Product | null>(null);
const transactions = ref<StockTransaction[]>([]);

onMounted(async () => {
  const [p, t] = await Promise.all([
    fetchProduct(productId.value),
    fetchStockTransactions(productId.value),
  ]);
  product.value = p;
  transactions.value = t;
});

function goBack() {
  router.push('/stock');
}
</script>

<template>
  <div>
    <h1>入出庫履歴</h1>
    <p v-if="product">商品: {{ product.productName }} ({{ product.productCode }})</p>

    <table>
      <thead>
        <tr>
          <th>取引区分</th>
          <th>数量</th>
          <th>取引日時</th>
          <th>備考</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="tx in transactions"
          :key="tx.transactionId"
          data-testid="transaction-row"
        >
          <td>{{ tx.transactionType }}</td>
          <td>{{ tx.quantity }}</td>
          <td>{{ tx.transactionAt }}</td>
          <td>{{ tx.note }}</td>
        </tr>
      </tbody>
    </table>

    <button @click="goBack">在庫一覧へ戻る</button>
  </div>
</template>
