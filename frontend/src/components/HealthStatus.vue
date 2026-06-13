<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { fetchHealth } from '@/api/healthApi';

const status = ref<string>('UNKNOWN');

onMounted(async () => {
  try {
    const result = await fetchHealth();
    status.value = result.status;
  } catch {
    status.value = 'DOWN';
  }
});
</script>

<template>
  <span data-testid="health-status">backend: {{ status }}</span>
</template>
