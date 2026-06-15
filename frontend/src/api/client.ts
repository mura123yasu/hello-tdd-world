import axios from 'axios';

/**
 * バックエンド API 用の共通 Axios クライアント。
 * 開発時は Vite のプロキシ経由で http://localhost:8080 に転送される。
 */
export const apiClient = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});
