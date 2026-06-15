<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { fetchProduct } from '@/api/productApi';
import { fetchStock, stockIn, stockOut } from '@/api/stockApi';
import type { Product, Stock } from '@/types';

const route = useRoute();
const router = useRouter();

const productId = computed(() => Number(route.params.id));

const product = ref<Product | null>(null);
const currentStock = ref<Stock | null>(null);

const transactionType = ref<'IN' | 'OUT'>('IN');
const quantity = ref<number | ''>('');
const note = ref('');

const successMessage = ref('');
const errorMessage = ref('');

onMounted(async () => {
  const [p, s] = await Promise.all([
    fetchProduct(productId.value),
    fetchStock(productId.value),
  ]);
  product.value = p;
  currentStock.value = s;
});

function selectIn() {
  transactionType.value = 'IN';
}

function selectOut() {
  transactionType.value = 'OUT';
}

async function handleSubmit() {
  successMessage.value = '';
  errorMessage.value = '';

  const qty = Number(quantity.value);
  const payload = { quantity: qty, note: note.value || undefined };

  try {
    if (transactionType.value === 'IN') {
      const updated = await stockIn(productId.value, payload);
      currentStock.value = updated;
      successMessage.value = '入庫しました';
    } else {
      const updated = await stockOut(productId.value, payload);
      currentStock.value = updated;
      successMessage.value = '出庫しました';
    }
    quantity.value = '';
    note.value = '';
  } catch (err: unknown) {
    const axiosErr = err as { response?: { status: number; data?: { message?: string } } };
    if (axiosErr.response?.status === 409) {
      errorMessage.value = axiosErr.response.data?.message ?? '在庫が不足しています';
    } else {
      errorMessage.value = '処理中にエラーが発生しました';
    }
  }
}

function goBack() {
  router.push('/stock');
}
</script>

<template>
  <div>
    <h1>入出庫登録</h1>
    <p v-if="product">商品: {{ product.productName }} ({{ product.productCode }})</p>
    <p v-if="currentStock">
      現在在庫: <span data-testid="current-quantity">{{ currentStock.quantity }}</span>
    </p>

    <p v-if="successMessage" data-testid="success-message" style="color: green">
      {{ successMessage }}
    </p>
    <p v-if="errorMessage" data-testid="error-message" style="color: red">{{ errorMessage }}</p>

    <div>
      <button
        data-testid="transaction-type-in"
        :style="{ fontWeight: transactionType === 'IN' ? 'bold' : 'normal' }"
        type="button"
        @click="selectIn"
      >
        入庫
      </button>
      <button
        data-testid="transaction-type-out"
        :style="{ fontWeight: transactionType === 'OUT' ? 'bold' : 'normal' }"
        type="button"
        @click="selectOut"
      >
        出庫
      </button>
    </div>

    <div>
      <div>
        <label>数量</label>
        <input
          v-model="quantity"
          data-testid="transaction-quantity"
          type="number"
          min="1"
        />
      </div>
      <div>
        <label>備考</label>
        <input v-model="note" data-testid="transaction-note" type="text" />
      </div>
      <button type="button" data-testid="submit-transaction" @click="handleSubmit">
        {{ transactionType === 'IN' ? '入庫登録' : '出庫登録' }}
      </button>
    </div>

    <button type="button" @click="goBack">在庫一覧へ戻る</button>
  </div>
</template>
