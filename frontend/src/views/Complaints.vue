<template>
  <div class="citizen-complaints page">
    <div class="page-header">
      <div>
        <h2>My Complaints</h2>
        <p>Track the status of the grievances you have submitted.</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="$router.push('/complaints/new')">
          New Complaint
        </el-button>
        <el-button type="default" @click="fetchComplaints" :loading="loading">
          Refresh
        </el-button>
      </div>
    </div>

    <el-card class="summary-card" shadow="never">
      <div class="summary-content">
        <div class="summary-item total-box">
  <span class="summary-label">Total</span>
  <span class="summary-value">{{ rows.length }}</span>
</div>

<div class="summary-item open-box">
  <span class="summary-label">Open</span>
  <span class="summary-value">{{ openCount }}</span>
</div>

<div class="summary-item resolved-box">
  <span class="summary-label">Resolved / Closed</span>
  <span class="summary-value">{{ resolvedCount }}</span>
</div>

      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="table-header">
          <span>Complaint history</span>
        </div>
      </template>

      <el-empty v-if="!loading && rows.length === 0" description="No complaints found">
        <el-button type="primary" @click="$router.push('/complaints/new')">
          Submit your first complaint
        </el-button>
      </el-empty>

      <el-table
        v-else
        :data="rows"
        v-loading="loading"
        class="complaints-table"
        border
        stripe
      >
        <el-table-column prop="id" label="Ticket" width="110">
          <template #default="scope">
            <span class="ticket-id">CMP-{{ scope.row.id.toString().padStart(6, '0') }}</span>
          </template>
        </el-table-column>

        <el-table-column label="Title" min-width="260">
          <template #default="scope">
            <div class="title-cell">
              <strong>{{ scope.row.title }}</strong>
              <small>{{ formatCategory(scope.row.category) }}</small>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="Priority" width="130">
          <template #default="scope">
            <el-tag :type="getPriorityType(scope.row.priority)" size="small">
              {{ scope.row.priority }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="Status" width="150">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ formatStatus(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="Last Updated" width="200">
          <template #default="scope">
            <div class="timestamp-cell">
              <span>{{ formatDate(scope.row.updatedAt || scope.row.createdAt) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="Actions" width="160" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" plain @click="go(scope.row.id)">
              View details
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { listMyComplaints } from '../api/complaints'

export default {
  name: 'AppComplaints',
  data() {
    return { 
      rows: [],
      loading: false
    }
  },
  computed: {
    openCount() {
      return this.rows.filter(c => !['RESOLVED', 'CLOSED'].includes(c.status)).length
    },
    resolvedCount() {
      return this.rows.filter(c => ['RESOLVED', 'CLOSED'].includes(c.status)).length
    }
  },
  mounted() {
    this.fetchComplaints()
  },
  methods: {
    fetchComplaints() {
      this.loading = true
      listMyComplaints()
        .then(r => { 
          this.rows = r.data || []
          // Sort by updated date, most recent first
          this.rows.sort((a, b) => {
            const dateA = new Date(a.updatedAt || a.createdAt)
            const dateB = new Date(b.updatedAt || b.createdAt)
            return dateB - dateA
          })
        })
        .catch(e => {
          console.error('Failed to load complaints:', e)
        })
        .finally(() => {
          this.loading = false
        })
    },
    go(id) { 
      this.$router.push(`/complaints/${id}`) 
    },
    formatStatus(status) {
      if (!status) return ''
      return status.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())
    },
    formatCategory(category) {
      if (!category) return ''
      return category.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())
    },
    formatDate(value) {
      if (!value) return '—'
      const date = new Date(value)
      return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    getStatusType(status) {
      const types = {
        NEW: 'info',
        UNDER_REVIEW: 'warning',
        IN_PROGRESS: 'primary',
        RESOLVED: 'success',
        CLOSED: 'info',
        ESCALATED: 'danger'
      }
      return types[status] || 'info'
    },
    getPriorityType(priority) {
      const types = {
        LOW: 'info',
        MEDIUM: 'warning',
        HIGH: 'danger',
        URGENT: 'danger'
      }
      return types[priority] || 'info'
    }
  }
}
</script>

<style scoped>
/* ✅ COLORED SUMMARY BOXES */

.total-box {
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  border-radius: 12px;
  padding: 14px 20px;
  color: white;
}

.open-box {
  background: linear-gradient(135deg, #f59e0b, #fbbf24);
  border-radius: 12px;
  padding: 14px 20px;
  color: white;
}

.resolved-box {
  background: linear-gradient(135deg, #16a34a, #22c55e);
  border-radius: 12px;
  padding: 14px 20px;
  color: white;
}

/* Make labels & numbers white */
.total-box .summary-label,
.open-box .summary-label,
.resolved-box .summary-label,
.total-box .summary-value,
.open-box .summary-value,
.resolved-box .summary-value {
  color: white !important;
}

/* Optional: hover effect */
.total-box:hover,
.open-box:hover,
.resolved-box:hover {
  transform: translateY(-2px);
  transition: 0.2s ease;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.citizen-complaints {
  padding: 32px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 16px;
}

.page-header h2 {
  margin: 0 0 4px;
  font-size: 24px;
  font-weight: 700;
  color: #111827;
}

.page-header p {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
}

.header-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.summary-card {
  margin-bottom: 16px;
  border-radius: 14px;
  border: 1px solid #e5e7eb;
}

.summary-content {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.summary-item {
  min-width: 140px;
}

.summary-label {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: #9ca3af;
}

.summary-value {
  font-size: 20px;
  font-weight: 700;
  color: #111827;
}

.table-card {
  margin-top: 8px;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #111827;
}

.complaints-table {
  width: 100%;
}

.ticket-id {
  font-weight: 600;
  color: #111827;
}

.title-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.title-cell strong {
  color: #111827;
}

.title-cell small {
  color: #6b7280;
  font-size: 13px;
}

.timestamp-cell span {
  font-size: 12px;
  color: #6b7280;
}

:deep(.el-table__cell) {
  font-size: 14px;
}

:deep(.el-table__header) th {
  background-color: #f9fafb !important;
  color: #4b5563 !important;
  font-weight: 600;
}

:deep(.el-table__row:hover > td) {
  background-color: #f3f4f6 !important;
}

@media (max-width: 768px) {
  .citizen-complaints {
    padding: 20px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
