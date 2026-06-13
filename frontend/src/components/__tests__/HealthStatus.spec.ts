import { describe, it, expect, vi, beforeEach } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import HealthStatus from '@/components/HealthStatus.vue';
import * as healthApi from '@/api/healthApi';

// API 呼び出しはモック化し、単体テストを高速・決定論的に保つ（FE テストの基本パターン）。
vi.mock('@/api/healthApi');

describe('HealthStatus', () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it('SKELETON-FE-01 backend の稼働状態 (UP) を表示する', async () => {
    vi.mocked(healthApi.fetchHealth).mockResolvedValue({ status: 'UP' });

    const wrapper = mount(HealthStatus);
    await flushPromises();

    expect(wrapper.get('[data-testid="health-status"]').text()).toContain('UP');
  });

  it('SKELETON-FE-02 取得失敗時は DOWN を表示する', async () => {
    vi.mocked(healthApi.fetchHealth).mockRejectedValue(new Error('network error'));

    const wrapper = mount(HealthStatus);
    await flushPromises();

    expect(wrapper.get('[data-testid="health-status"]').text()).toContain('DOWN');
  });
});
