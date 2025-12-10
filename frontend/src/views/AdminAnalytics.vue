<template>
  <div class="page analytics-page">
    <div class="page-header">
      <div class="page-title">
        <h2>Analytics & Reporting</h2>
        <p class="muted">
          Visual overview of how complaints are progressing through the system —
          resolved vs remaining and other key comparisons.
        </p>
      </div>

      <div class="page-actions">
        <el-button type="primary" :loading="loading" @click="fetchComplaints">
          Refresh Data
        </el-button>
      </div>
    </div>

    <el-alert
      v-if="errorMessage"
      :title="errorMessage"
      type="error"
      show-icon
      class="mb-16"
      @close="errorMessage = ''"
    />

    <div v-if="!loading && total === 0" class="empty-state">
      <p>No complaints available yet to generate analytics.</p>
    </div>

    <div v-else>
      <!-- KPI row -->
       <!-- Box 1 -->
    <div class="kpi-grid">
  <el-card class="kpi-card total" shadow="hover">
    <div class="kpi-label">Total Complaints</div>
    <div class="kpi-value">{{ total }}</div>
  </el-card>

  <!-- Box 2 -->
  <el-card class="kpi-card resolved" shadow="hover">
    <div class="kpi-label">Resolved / Closed</div>
    <div class="kpi-value">{{ resolvedCount }}</div>
    <div class="kpi-sub">{{ resolvedPercent }}% of total</div>
  </el-card>

  <!-- Box 3 -->
  <el-card class="kpi-card remaining" shadow="hover">
    <div class="kpi-label">Remaining (Open)</div>
    <div class="kpi-value">{{ remainingCount }}</div>
    <div class="kpi-sub">{{ remainingPercent }}% of total</div>
  </el-card>

      </div>

      <!-- Two-column charts: bar (left) + pie (right) -->
      <div class="charts-row">
        <el-card class="chart-card chart-left" shadow="never">
          <div class="chart-header">
            <div>
              <h3>Resolution Overview</h3>
              <p class="muted">Comparison of resolved vs remaining complaints.</p>
            </div>
          </div>

          <div class="resolution-graph">
            <div class="resolution-layout">
              <!-- left: bar + legend -->
              <div class="resolution-bar-section">
                <div class="resolution-bar" aria-hidden="true">
                  <div
                    class="segment segment-resolved"
                    :style="{ width: resolvedPercent + '%' }"
                  ></div>
                  <div
                    class="segment segment-remaining"
                    :style="{ width: remainingPercent + '%' }"
                  ></div>
                </div>

                <div class="resolution-legend">
                  <div class="legend-item">
                    <span class="dot dot-resolved" />
                    <div class="legend-text">
                      <div class="legend-label">Resolved / Closed</div>
                      <div class="legend-value">{{ resolvedCount }} ({{ resolvedPercent }}%)</div>
                    </div>
                  </div>

                  <div class="legend-item">
                    <span class="dot dot-remaining" />
                    <div class="legend-text">
                      <div class="legend-label">Remaining (Open)</div>
                      <div class="legend-value">{{ remainingCount }} ({{ remainingPercent }}%)</div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- right: pie summary -->
              <div class="resolution-pie-section" v-if="total">
                <div class="pie-wrap">
                  <div class="pie-chart" :style="pieChartStyle" aria-hidden="true">
                    <div class="pie-inner">
                      <div class="pie-value">{{ resolvedPercent }}%</div>
                      <div class="pie-label">Resolved</div>
                    </div>
                  </div>
                </div>
                <div class="pie-caption muted">
                  <div>Total: <strong>{{ total }}</strong></div>
                  <div>Resolved: <strong>{{ resolvedCount }}</strong></div>
                </div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- status distribution -->
        <el-card class="chart-card chart-right" shadow="never">
          <div class="chart-header">
            <div>
              <h3>Status Distribution</h3>
              <p class="muted">How complaints are spread across different statuses.</p>
            </div>
          </div>

          <div class="status-chart">
  <div
    v-for="item in statusStats"
    :key="item.key"
    class="status-row"
  >
    <div class="status-label">
      <span>{{ item.label }}</span>
      <span class="status-value">{{ item.count }} ({{ item.percent }}%)</span>
    </div>

    <div class="status-bar-track">
      <div
        class="status-bar-fill"
        :class="item.key.toLowerCase()"
        :style="{ width: item.percent + '%' }"
      ></div>
    </div>
  </div>
</div>

        </el-card>
      </div>

      <el-card class="chart-card category-card" shadow="never">
  <div class="chart-header">
    <div>
      <h3>Complaints by Category</h3>
      <p class="muted">Volume of citizen complaints per category.</p>
    </div>
  </div>

  <div v-if="categoryStats.length === 0" class="muted">
    No category data available.
  </div>

  <div v-else class="bar-graph-container">
    <div class="y-axis">
      <span>10</span>
      <span>8</span>
      <span>6</span>
      <span>4</span>
      <span>2</span>
      <span>0</span>
    </div>

    <div class="bar-graph">
      <div
        v-for="item in categoryStats"
        :key="item.key"
        class="bar-column"
      >
        <div
          class="bar-vertical"
          :style="{ height: item.percent + '%' }"
        >
          <span class="bar-count">{{ item.count }}</span>
        </div>
        <span class="bar-name">{{ item.label }}</span>
      </div>
    </div>
  </div>
</el-card>

    </div>
  </div>
</template>


<script setup>


import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminListComplaints } from '../api/complaints'

const loading = ref(false)
const complaints = ref([])
const errorMessage = ref('')

const resolvedStatuses = ['RESOLVED', 'CLOSED']

const total = computed(() => complaints.value.length)

const resolvedCount = computed(() =>
  complaints.value.filter(c => resolvedStatuses.includes(c.status)).length
)

const remainingCount = computed(() =>
  complaints.value.filter(c => !resolvedStatuses.includes(c.status)).length
)

const resolvedPercent = computed(() => {
  return total.value ? Math.round((resolvedCount.value / total.value) * 100) : 0
})

const remainingPercent = computed(() => {
  return total.value ? 100 - resolvedPercent.value : 0
})

const pieChartStyle = computed(() => ({
  '--resolved': resolvedPercent.value
}))

const statusOrder = ['NEW', 'UNDER_REVIEW', 'IN_PROGRESS', 'ESCALATED', 'RESOLVED_CLOSED']

const statusLabels = {
  NEW: 'New',
  UNDER_REVIEW: 'Under Review',
  IN_PROGRESS: 'In Progress',
  ESCALATED: 'Escalated',
  RESOLVED_CLOSED: 'Resolved/Closed'
}

const statusStats = computed(() => {
  if (!total.value) return []

  const counts = {}
  statusOrder.forEach(key => { counts[key] = 0 })

  complaints.value.forEach(c => {
    if (c.status === 'RESOLVED' || c.status === 'CLOSED') {
      counts.RESOLVED_CLOSED++
    } else if (counts[c.status] !== undefined) {
      counts[c.status]++
    }
  })

  return statusOrder.map(key => {
    const count = counts[key]
    const percent = total.value ? Math.round((count / total.value) * 100) : 0
    return { key, label: statusLabels[key] || key, count, percent }
  })
})

const categoryStats = computed(() => {
  if (!total.value) return []
  const counts = {}
  complaints.value.forEach(c => {
    const key = c.category || 'UNCATEGORIZED'
    counts[key] = (counts[key] || 0) + 1
  })
  return Object.entries(counts)
    .map(([key, count]) => {
      const percent = total.value ? Math.round((count / total.value) * 100) : 0
      const label = key
        .toLowerCase()
        .replace(/_/g, ' ')
        .replace(/\b\w/g, ch => ch.toUpperCase())
      return { key, label, count, percent }
    })
    .sort((a, b) => b.count - a.count)
})

const fetchComplaints = async () => {
  loading.value = true
  errorMessage.value = ''
  try {
    const { data } = await adminListComplaints()
    complaints.value = data || []
  } catch (error) {
    const message = error.response?.data?.message || 'Unable to load analytics data'
    errorMessage.value = message
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchComplaints()
})
</script>

<style scoped>

/* ========== Page - Premium Light Layout ========== */
.analytics-page {
  padding: 28px;
  min-height: 100vh;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  color: #0f172a;
  font-family: "Inter", system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", Arial;
}

/* Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 20px 22px;
  background: #ffffff;
  border-radius: 14px;
  border: 1px solid #e6eef8;
  box-shadow: 0 8px 20px rgba(16, 24, 40, 0.06);
  margin-bottom: 20px;
}

.page-title h2 {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  color: #0b1220;
}

.muted {
  color: #5b6b7a;
  margin-top: 6px;
  font-size: 14px;
}

/* KPI grid */
/* KPI row - ALWAYS horizontal */
.kpi-grid {
  display: flex !important;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 22px;
}

/* Each KPI Card */
.kpi-card {
  flex: 1;
  min-width: 0;
  background: #fff;
  border-radius: 14px;
  padding: 20px 22px;
  border: 1px solid #eef6ff;
  box-shadow: 0 8px 20px rgba(16, 24, 40, 0.04);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
  min-height: 110px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}


.kpi-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 30px rgba(16, 24, 40, 0.08);
}

.kpi-label {
  color: #6b7685;
  font-size: 14px;
  margin-bottom: 8px;
}

.kpi-value {
  font-size: 36px;
  font-weight: 800;
  color: #0b1220;
}

.kpi-sub {
  margin-top: 8px;
  color: #6b7685;
  font-size: 13px;
}

.kpi-card.total { background: linear-gradient(135deg,#dbeafe,#bfdbfe); }
.kpi-card.resolved { background: linear-gradient(135deg,#bbf7d0,#86efac); }
.kpi-card.remaining { background: linear-gradient(135deg,#fed7aa,#fdba74); }

/* Charts row (two-column layout) */
.charts-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  align-items: start;
  margin-bottom: 20px;
}

.chart-card {
  background: #fff !important;
  border-radius: 14px !important;
  padding: 20px !important;
  border: 1px solid #eef6ff !important;
  box-shadow: 0 8px 18px rgba(16, 24, 40, 0.04);
}

/* Chart header text */
.chart-header h3 {
  margin: 0 0 6px;
  font-size: 18px;
  color: #0b1220;
}

/* Resolution bar / left column */
.resolution-graph {
  margin-top: 12px;
}

.resolution-layout {
  display: flex;
  gap: 18px;
  align-items: center;
}

.resolution-bar-section {
  flex: 1 1 auto;
}

.resolution-bar {
  height: 16px;
  border-radius: 999px;
  background: #eef3f8;
  overflow: hidden;
  position: relative;
  margin-bottom: 12px;
}

.segment {
  position: absolute;
  top: 0;
  bottom: 0;
}

.segment-resolved { left: 0; background: linear-gradient(90deg,#16a34a,#22c55e); }
.segment-remaining { right: 0; background: linear-gradient(90deg,#f97316,#fb923c); }

/* Legend items */
.resolution-legend {
  display: flex;
  gap: 14px;
  align-items: center;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  gap: 10px;
  align-items: center;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 999px;
  display: inline-block;
}

.dot-resolved { background: linear-gradient(90deg,#16a34a,#22c55e); }
.dot-remaining { background: linear-gradient(90deg,#f97316,#fb923c); }

.legend-text .legend-label {
  font-size: 14px;
  color: #23303b;
}

.legend-text .legend-value {
  font-size: 13px;
  color: #556472;
}

/* Pie column (right) */
.resolution-pie-section {
  width: 180px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.pie-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
}

.pie-chart {
  --size: 170px;
  width: var(--size);
  height: var(--size);
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: conic-gradient(
    #16a34a 0 calc(var(--resolved) * 1%),
    #f97316 calc(var(--resolved) * 1%) 100%
  );
  box-shadow: 0 6px 18px rgba(16, 24, 40, 0.04);
}

.pie-inner {
  width: calc(var(--size) * 0.68);
  height: calc(var(--size) * 0.68);
  border-radius: 999px;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: inset 0 0 0 2px #eef6ff;
}

.pie-value {
  font-size: 22px;
  font-weight: 800;
  color: #0b1220;
}

.pie-label {
  font-size: 12px;
  color: #64748b;
}

/* Pie caption */
.pie-caption {
  text-align: center;
  color: #4b5563;
  font-size: 13px;
}

/* Status distribution (right card) */
.distribution-list {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.distribution-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.distribution-labels {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.distribution-labels .name { font-size: 14px; color: #0b1220; }
.distribution-labels .value { font-size: 13px; color: #52606a; }

.distribution-bar {
  height: 10px;
  border-radius: 999px;
  background: #eef3f8;
  overflow: hidden;
}

.distribution-fill {
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg,#6366f1,#22c55e);
}

.category-card {
  margin-top: 12px;
}

.category-fill {
  background: linear-gradient(90deg, #2563eb, #10b981);
}

.bar-chart {
  padding: 8px 4px;
}

.bar-track {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(60px, 1fr));
  gap: 12px;
  align-items: end;
  min-height: 200px;
  padding: 8px 4px 0;
}

.bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.bar {
  width: 100%;
  max-width: 40px;
  background: linear-gradient(180deg, #2563eb, #1d4ed8);
  border-radius: 6px 6px 2px 2px;
  position: relative;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding-bottom: 6px;
  color: #fff;
  font-weight: 700;
  box-shadow: 0 6px 14px rgba(37, 99, 235, 0.25);
}

.bar-value {
  font-size: 12px;
}

.bar-label {
  font-size: 12px;
  color: #4b5563;
  text-align: center;
  word-break: break-word;
}

/* Responsive: stack columns on small screens */
@media (max-width: 980px) {
  .charts-row { grid-template-columns: 1fr; }
  .kpi-grid { grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); }
  .pie-caption { font-size: 12px; }
}

@media (max-width: 480px) {
  .page-header { padding: 16px; flex-direction: column; align-items: flex-start; gap: 10px; }
  .kpi-value { font-size: 30px; }
  .pie-chart { --size: 140px; }
}
/* ===== BAR GRAPH LIKE IMAGE ===== */

.bar-graph-container {
  display: flex;
  align-items: flex-end;
  gap: 14px;
  padding: 20px 10px 10px;
  height: 260px;
}

/* Y Axis */
.y-axis {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
  font-size: 12px;
  color: #64748b;
  padding-right: 8px;
}

/* Main bar area */
.bar-graph {
  display: flex;
  align-items: flex-end;
  gap: 22px;
  flex: 1;
  height: 100%;
  border-left: 2px solid #94a3b8;
  border-bottom: 2px solid #94a3b8;
  padding: 10px 12px;
}

/* Each bar column */
.bar-column {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  height: 100%;
  width: 50px;
}

/* The actual bar */
.bar-vertical {
  width: 100%;
  background: linear-gradient(180deg, #2563eb, #1e40af);
  border-radius: 6px 6px 0 0;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding-top: 6px;
  transition: 0.3s;
}

/* Count inside bar */
.bar-count {
  color: #fff;
  font-size: 12px;
  font-weight: 700;
}

/* Category name */
.bar-name {
  margin-top: 6px;
  font-size: 12px;
  text-align: center;
  color: #0f172a;
  word-break: break-word;
}
/* ===== STATUS DISTRIBUTION BAR CHART ===== */

.status-chart {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 14px;
}

.status-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.status-label {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #0b1220;
  font-weight: 500;
}

.status-value {
  color: #475569;
  font-size: 13px;
}

.status-bar-track {
  width: 100%;
  height: 12px;
  background: #e2e8f0;
  border-radius: 999px;
  overflow: hidden;
}

.status-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #3b82f6, #10b981);
  border-radius: inherit;
  transition: width 0.4s ease;
}


</style>
