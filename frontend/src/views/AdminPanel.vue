<template>
  <div class="admin-panel">

    <!-- ✅ CENTERED HEADER -->
    <div class="center-header">
      <h1>Complaint Control Center</h1>
      <p class="muted">
        Monitor, triage, and resolve grievances submitted across the city.
      </p>
      <el-button size="large" @click="fetchData" :loading="loading">
        Refresh
      </el-button>
    </div>

    <!-- ✅ SUMMARY IN ONE ROW -->
    <div class="summary-row">
      <el-card class="summary-card primary" shadow="hover">
        <div class="summary-label">Total Complaints</div>
        <div class="summary-value">{{ totalCount }}</div>
      </el-card>

      <el-card class="summary-card warning" shadow="hover">
        <div class="summary-label">Open</div>
        <div class="summary-value">{{ openCount }}</div>
      </el-card>

      <el-card class="summary-card success" shadow="hover">
        <div class="summary-label">Resolved</div>
        <div class="summary-value">{{ resolvedCount }}</div>
      </el-card>
    </div>

    <!-- ✅ COMPLAINT TABLE BELOW -->
    <el-card class="content-card" shadow="never">
      <template #header>
        <div class="card-header">
          <h3>Complaint List</h3>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="complaints"
        class="table"
        stripe
        border
      >
        <el-table-column label="Ticket" width="140">
          <template #default="{ row }">
            CMP-{{ row.id.toString().padStart(6, '0') }}
          </template>
        </el-table-column>

        <el-table-column label="Title" min-width="240">
          <template #default="{ row }">
            <strong>{{ row.title }}</strong><br />
            <small>{{ formatCategory(row.category) }}</small>
          </template>
        </el-table-column>

        <el-table-column label="Status" width="160">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">
              {{ formatStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="Priority" width="140">
          <template #default="{ row }">
            <el-tag :type="priorityType(row.priority)">
              {{ row.priority }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="Updated" width="180">
          <template #default="{ row }">
            {{ formatDate(row.updatedAt || row.createdAt) }}
          </template>
        </el-table-column>

      </el-table>
    </el-card>

  </div>
</template>


<script setup>
import { computed, onMounted, ref } from 'vue'
import { adminListComplaints } from '../api/complaints'

const loading = ref(false)
const complaints = ref([])

const totalCount = computed(() => complaints.value.length)

const resolvedStatuses = ['RESOLVED', 'CLOSED', 'RESOLVED_CLOSED']

const resolvedCount = computed(() =>
  complaints.value.filter(c => resolvedStatuses.includes(c.status)).length
)

const openCount = computed(() =>
  complaints.value.filter(c => !resolvedStatuses.includes(c.status)).length
)

const fetchData = async () => {
  loading.value = true
  try {
    const { data } = await adminListComplaints()
    complaints.value = data || []
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)

const formatDate = (value) => {
  if (!value) return '—'
  return new Date(value).toLocaleString()
}

const formatCategory = (value) => {
  if (!value) return 'Uncategorized'
  return value
    .toLowerCase()
    .replace(/_/g, ' ')
    .replace(/\b\w/g, c => c.toUpperCase())
}

const formatStatus = (value) => {
  if (!value) return 'Unknown'
  return value
    .toLowerCase()
    .replace(/_/g, ' ')
    .replace(/\b\w/g, c => c.toUpperCase())
}

const statusType = (status) => {
  if (status === 'NEW') return 'info'
  if (status === 'UNDER_REVIEW') return 'warning'
  if (status === 'IN_PROGRESS') return 'primary'
  if (status === 'ESCALATED') return 'danger'
  if (resolvedStatuses.includes(status)) return 'success'
  return 'default'
}

const priorityType = (priority) => {
  if (priority === 'URGENT' || priority === 'HIGH') return 'danger'
  if (priority === 'MEDIUM') return 'warning'
  if (priority === 'LOW') return 'info'
  return 'default'
}
</script>
  

<style scoped>
.admin-panel {
  padding: 28px;
  min-height: 100vh;
  background: linear-gradient(180deg, #0b1220 0%, #0f172a 35%, #0b1220 100%);
  color: #e2e8f0;
  max-width: 1200px;
  margin: 0 auto;
}

/* ✅ CENTER TITLE */
.center-header {
  text-align: center;
  margin-bottom: 24px;
}

.center-header h1 {
  font-size: 34px;
  font-weight: 800;
  color: #f8fafc;
}

.center-header p {
  color: #cbd5e1;
  margin: 6px 0 12px;
}

/* ✅ ONE ROW SUMMARY */
.summary-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
  max-width: 900px;
  margin-left: auto;
  margin-right: auto;
}

/* ✅ COLORED KPI BOXES */
.summary-card {
  border-radius: 16px !important;
  padding: 20px !important;
  text-align: center;
  color: white !important;
  border: none !important;
}

.summary-card.primary {
  background: linear-gradient(135deg, #2563eb, #1e40af);
}

.summary-card.warning {
  background: linear-gradient(135deg, #f59e0b, #b45309);
}

.summary-card.success {
  background: linear-gradient(135deg, #16a34a, #065f46);
}

.summary-label {
  font-size: 14px;
  opacity: 0.9;
}

.summary-value {
  font-size: 36px;
  font-weight: 800;
}

/* ✅ TABLE BELOW */
.content-card {
  border-radius: 16px !important;
  background: rgba(255,255,255,0.05) !important;
  border: 1px solid rgba(255,255,255,0.08) !important;
}

.table :deep(.el-table__header) th {
  background: rgba(255, 255, 255, 0.06);
  color: #e5e7eb;
}

.table :deep(.el-table__cell) {
  color: #e5e7eb;
}


</style>
