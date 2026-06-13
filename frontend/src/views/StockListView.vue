<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { fetchStockList } from '@/api/stockApi';
import type { Stock } from '@/types';

const router = useRouter();
const stockList = ref<Stock[]>([]);

onMounted(async () => {
  stockList.value = await fetchStockList();
});

function goToTransaction(productId: number) {
  router.push(`/stock/${productId}/transaction`);
}

function goToHistory(productId: number) {
  router.push(`/stock/${productId}/transactions`);
}
</script>

<template>
  <div>
    <h1>在庫一覧</h1>

    <table>
      <thead>
        <tr>
          <th>商品コード</th>
          <th>商品名</th>
          <th>在庫数量</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="stock in stockList"
          :key="stock.productId"
          data-testid="stock-row"
        >
          <td>{{ stock.productCode }}</td>
          <td>{{ stock.productName }}</td>
          <td>{{ stock.quantity }}</td>
          <td>
            <button @click="goToTransaction(stock.productId)">入出庫</button>
            <button @click="goToHistory(stock.productId)">履歴</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
