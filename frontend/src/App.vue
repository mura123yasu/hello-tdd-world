<script setup lang="ts">
import HealthStatus from '@/components/HealthStatus.vue';
</script>

<template>
  <header>
    <h1>商品在庫管理システム</h1>
    <HealthStatus />
  </header>
  <main>
    <RouterView />
  </main>
</template>
