<script setup lang="ts">
import HealthStatus from '@/components/HealthStatus.vue';
</script>

<template>
  <header>
    <h1>商品在庫管理システム</h1>
    <nav>
      <RouterLink to="/products">商品一覧</RouterLink>
      |
      <RouterLink to="/stock">在庫一覧</RouterLink>
    </nav>
    <HealthStatus />
  </header>
  <main>
    <RouterView />
  </main>
</template>
