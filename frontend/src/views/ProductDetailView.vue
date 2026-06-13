<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { fetchProduct, createProduct, updateProduct, deleteProduct } from '@/api/productApi';
import { fetchCategories } from '@/api/categoryApi';
import type { Category } from '@/types';

const route = useRoute();
const router = useRouter();

const productId = computed(() => {
  const id = route.params.id;
  if (!id || id === 'new') return null;
  return Number(id);
});

const isNew = computed(() => productId.value === null);

// フォームフィールド
const productCode = ref('');
const productName = ref('');
const categoryId = ref<number | ''>('');
const unitPrice = ref<number | ''>('');
const status = ref<'ACTIVE' | 'INACTIVE'>('ACTIVE');
const description = ref('');
const version = ref(0);

// UI 状態
const categories = ref<Category[]>([]);
const successMessage = ref('');
const errorMessage = ref('');
const validationErrors = ref<Record<string, string>>({});
const showConfirmDialog = ref(false);

onMounted(async () => {
  categories.value = await fetchCategories();
  if (!isNew.value && productId.value !== null) {
    const product = await fetchProduct(productId.value);
    productCode.value = product.productCode;
    productName.value = product.productName;
    categoryId.value = product.categoryId;
    unitPrice.value = product.unitPrice;
    status.value = product.status;
    description.value = product.description ?? '';
    version.value = product.version;
  }
});

function validate(): boolean {
  const errors: Record<string, string> = {};
  if (!productCode.value.trim()) {
    errors.productCode = '商品コードは必須です';
  }
  if (!productName.value.trim()) {
    errors.productName = '商品名は必須です';
  }
  if (categoryId.value === '' || categoryId.value === 0) {
    errors.categoryId = 'カテゴリは必須です';
  }
  if (unitPrice.value === '' || unitPrice.value < 0) {
    errors.unitPrice = '単価は0以上で入力してください';
  }
  validationErrors.value = errors;
  return Object.keys(errors).length === 0;
}

async function handleSubmit() {
  successMessage.value = '';
  errorMessage.value = '';

  if (!validate()) {
    return;
  }

  try {
    if (isNew.value) {
      await createProduct({
        productCode: productCode.value,
        productName: productName.value,
        categoryId: Number(categoryId.value),
        unitPrice: Number(unitPrice.value),
        status: status.value,
        description: description.value || undefined,
      });
      router.push('/products');
    } else {
      const updated = await updateProduct(productId.value!, {
        productName: productName.value,
        categoryId: Number(categoryId.value),
        unitPrice: Number(unitPrice.value),
        status: status.value,
        description: description.value || undefined,
        version: version.value,
      });
      version.value = updated.version;
      successMessage.value = '更新しました';
    }
  } catch (err: unknown) {
    const axiosErr = err as { response?: { status: number; data?: { message?: string } } };
    if (axiosErr.response?.status === 409) {
      errorMessage.value = axiosErr.response.data?.message ?? '競合エラーが発生しました';
    } else {
      errorMessage.value = '処理中にエラーが発生しました';
    }
  }
}

function handleDeleteClick() {
  showConfirmDialog.value = true;
}

async function handleConfirmDelete() {
  showConfirmDialog.value = false;
  try {
    await deleteProduct(productId.value!);
    router.push('/products');
  } catch {
    errorMessage.value = '削除中にエラーが発生しました';
  }
}

function handleCancelDelete() {
  showConfirmDialog.value = false;
}
</script>

<template>
  <div>
    <h1>{{ isNew ? '商品登録' : '商品詳細' }}</h1>

    <p v-if="successMessage" data-testid="success-message" style="color: green">
      {{ successMessage }}
    </p>
    <p v-if="errorMessage" data-testid="error-message" style="color: red">{{ errorMessage }}</p>

    <div>
      <div>
        <label>商品コード</label>
        <input
          v-model="productCode"
          data-testid="product-code"
          type="text"
          :disabled="!isNew"
        />
        <span v-if="validationErrors.productCode" data-testid="error-product-code">
          {{ validationErrors.productCode }}
        </span>
      </div>
      <div>
        <label>商品名</label>
        <input v-model="productName" data-testid="product-name" type="text" />
        <span v-if="validationErrors.productName" data-testid="error-product-name">
          {{ validationErrors.productName }}
        </span>
      </div>
      <div>
        <label>カテゴリ</label>
        <select v-model="categoryId" data-testid="category-select">
          <option value="">-- 選択してください --</option>
          <option
            v-for="cat in categories"
            :key="cat.categoryId"
            :value="cat.categoryId"
          >
            {{ cat.categoryName }}
          </option>
        </select>
        <span v-if="validationErrors.categoryId" data-testid="error-category">
          {{ validationErrors.categoryId }}
        </span>
      </div>
      <div>
        <label>単価</label>
        <input v-model="unitPrice" data-testid="unit-price" type="number" min="0" />
        <span v-if="validationErrors.unitPrice" data-testid="error-unit-price">
          {{ validationErrors.unitPrice }}
        </span>
      </div>
      <div>
        <label>ステータス</label>
        <select v-model="status" data-testid="status-select">
          <option value="ACTIVE">ACTIVE</option>
          <option value="INACTIVE">INACTIVE</option>
        </select>
      </div>
      <div>
        <label>説明</label>
        <textarea v-model="description" data-testid="description"></textarea>
      </div>

      <button type="button" data-testid="submit-button" @click="handleSubmit">
        {{ isNew ? '登録' : '更新' }}
      </button>
      <button
        v-if="!isNew"
        type="button"
        data-testid="delete-button"
        @click="handleDeleteClick"
      >
        削除
      </button>
    </div>

    <!-- 削除確認ダイアログ -->
    <div v-if="showConfirmDialog" data-testid="confirm-dialog">
      <p>本当に削除しますか？</p>
      <button type="button" data-testid="confirm-delete" @click="handleConfirmDelete">削除する</button>
      <button type="button" data-testid="cancel-delete" @click="handleCancelDelete">キャンセル</button>
    </div>
  </div>
</template>
