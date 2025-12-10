<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <div class="welcome-section">
        <h1>Welcome back, {{ userName || 'User' }}</h1>
        <p class="subtitle">Submit new grievances and keep track of their progress.</p>
      </div>
      <div class="header-actions">
        <el-button size="large" @click="profileDialogVisible = true">
          Profile
        </el-button>
        <el-button type="primary" size="large" @click="$router.push('/complaints/new')">
          Submit new complaint
        </el-button>
      </div>
    </div>

    <div class="top-row">
      <el-card class="summary-card" shadow="never">
        <div class="summary-grid">
          
            <div class="summary-item total-box">
              <span class="summary-label">Total complaints</span>
              <span class="summary-value">{{ stats.total }}</span>
            </div>

            <div class="summary-item new-box">
              <span class="summary-label">New</span>
              <span class="summary-value">{{ stats.NEW }}</span>
            </div>

            <div class="summary-item review-box">
             <span class="summary-label">Under review</span>
             <span class="summary-value">{{ stats.UNDER_REVIEW }}</span>
            </div>

            <div class="summary-item resolved-box">
             <span class="summary-label">Resolved</span>
             <span class="summary-value">{{ stats.RESOLVED }}</span>
           </div>
        </div>
      </el-card>
    </div>

    <div class="content-row single-column">
      <el-card class="content-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>My recent complaints</span>
            <el-button type="text" @click="$router.push('/complaints')">View all</el-button>
          </div>
        </template>

        <el-empty v-if="recentComplaints.length === 0" description="No complaints yet">
          <el-button type="primary" @click="$router.push('/complaints/new')">Submit a complaint</el-button>
        </el-empty>

        <el-table
          v-else
          :data="recentComplaints"
          size="small"
          class="recent-table"
          :show-header="true"
        >
          <el-table-column label="Ticket" width="120">
            <template #default="scope">
              <span class="ticket-id">CMP-{{ scope.row.id.toString().padStart(6, '0') }}</span>
            </template>
          </el-table-column>
          <el-table-column label="Title" min-width="220">
            <template #default="scope">
              <div class="title-cell">
                <strong>{{ scope.row.title }}</strong>
                <small>{{ formatCategory(scope.row.category) }}</small>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="Status" width="150">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)" size="small">
                {{ formatStatus(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="Last updated" width="190">
            <template #default="scope">
              <span class="timestamp">{{ formatDate(scope.row.updatedAt || scope.row.createdAt) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

    </div>

    <el-dialog v-model="profileDialogVisible" title="Citizen profile" width="480px">
      <div class="profile-details">
        <div class="detail-row">
          <span class="label">Name</span>
          <span class="value">{{ displayName }}</span>
        </div>
        <div class="detail-row">
          <span class="label">Username</span>
          <span class="value">{{ userName || '—' }}</span>
        </div>
        <div class="detail-row">
          <span class="label">Email</span>
          <span class="value">{{ userEmail || '—' }}</span>
        </div>
        <div class="detail-row">
          <span class="label">Phone</span>
          <span class="value">{{ userPhone || 'Not provided' }}</span>
        </div>
        <div class="detail-row">
          <span class="label">Address</span>
          <span class="value">{{ userAddress || 'Not provided' }}</span>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="profileDialogVisible = false">Close</el-button>
          <el-button type="primary" @click="handleChangePassword">Change password</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { listMyComplaints } from '../api/complaints'

export default {
  name: 'AppDashboard',

  data() {
    return { 
      stats: { total: 0, NEW: 0, UNDER_REVIEW: 0, RESOLVED: 0 },
      loading: true,
      allComplaints: [],
      profileDialogVisible: false
    }
  },

  computed: {
    userName() {
      return this.$store.getters.UserName
    },
    userEmail() {
      return this.$store.state.user?.Email || ''
    },
    userPhone() {
      return this.$store.state.user?.PhoneNumber || ''
    },
    userAddress() {
      return this.$store.state.user?.Address || ''
    },
    displayName() {
      const first = this.$store.state.user?.firstName
      const last = this.$store.state.user?.lastName
      const name = [first, last].filter(Boolean).join(' ')
      return name || this.userName || 'Citizen'
    },
    userRole() {
      return this.$store.state.user?.role || 'CITIZEN'
    },
    userInitials() {
      const name = this.userName || 'C'
      return name.slice(0, 2).toUpperCase()
    },
    recentComplaints() {
      return this.allComplaints.slice(0, 5)
    }
  },

  mounted() {
    this.loadStats()
  },

  methods: {
    handleChangePassword() {
      this.$router.push('/change-password')
    },
    loadStats() {
      this.loading = true
      listMyComplaints()
        .then(r => {
          const rows = r.data || []

          rows.sort((a, b) => {
            const da = new Date(a.updatedAt || a.createdAt)
            const db = new Date(b.updatedAt || b.createdAt)
            return db - da
          })

          this.allComplaints = rows

          const counts = {
            total: rows.length,
            NEW: 0,
            UNDER_REVIEW: 0,
            RESOLVED: 0
          }

          rows.forEach(c => { 
            if (counts[c.status] !== undefined) {
              counts[c.status]++
            }
          })

          this.stats = counts
        })
        .finally(() => {
          this.loading = false
        })
    },

    formatCategory(category) {
      if (!category) return 'N/A'
      return category
        .toLowerCase()
        .replace(/_/g, ' ')
        .replace(/\b\w/g, char => char.toUpperCase())
    },

    formatStatus(status) {
      if (!status) return 'Unknown'
      return status
        .toLowerCase()
        .replace(/_/g, ' ')
        .replace(/\b\w/g, char => char.toUpperCase())
    },

    getStatusType(status) {
      if (status === 'NEW') return 'info'
      if (status === 'UNDER_REVIEW') return 'warning'
      if (status === 'RESOLVED') return 'success'
      return 'default'
    },

    formatDate(value) {
      if (!value) return '—'
      return new Date(value).toLocaleString()
    }
  }
}
</script>


<style scoped>
  /* ✅ COLORED STAT BOXES */

.total-box {
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  color: white;
  padding: 16px;
  border-radius: 14px;
}

.new-box {
  background: linear-gradient(135deg, #f59e0b, #fbbf24);
  color: white;
  padding: 16px;
  border-radius: 14px;
}

.review-box {
  background: linear-gradient(135deg, #8b5cf6, #a78bfa);
  color: white;
  padding: 16px;
  border-radius: 14px;
}

.resolved-box {
  background: linear-gradient(135deg, #16a34a, #22c55e);
  color: white;
  padding: 16px;
  border-radius: 14px;
}

/* Make text white inside colored boxes */
.total-box .summary-label,
.new-box .summary-label,
.review-box .summary-label,
.resolved-box .summary-label,
.total-box .summary-value,
.new-box .summary-value,
.review-box .summary-value,
.resolved-box .summary-value {
  color: white;
}

.dashboard {
  padding: 32px;
  max-width: 1200px;
  margin: 0 auto;
  background: #f9fafb;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  gap: 16px;
}

.welcome-section h1 {
  margin: 0 0 6px 0;
  font-size: 26px;
  font-weight: 700;
  color: #111827;
}

.subtitle {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.top-row {
  margin-bottom: 16px;
}

.summary-card {
  border-radius: 16px;
  border: 1px solid #e5e7eb;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 16px;
}

.summary-item {
  display: flex;
  flex-direction: column;
}

.summary-label {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: #9ca3af;
}

.summary-value {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
}

.content-row {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
  align-items: flex-start;
}

.content-card {
  border-radius: 16px;
  border: 1px solid #e5e7eb;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #111827;
}

.recent-table {
  margin-top: 8px;
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

.timestamp {
  font-size: 12px;
  color: #6b7280;
}

.quick-links {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 8px;
}

.profile-card,
.avatar,
.profile-text,
.role-pill,
.profile-stats,
.profile-stat {
  display: none;
}

:deep(.el-table__header) th {
  background-color: #f9fafb !important;
  color: #4b5563 !important;
  font-weight: 600;
}

:deep(.el-table__row:hover > td) {
  background-color: #f3f4f6 !important;
}

@media (max-width: 900px) {
  .dashboard {
    padding: 20px;
  }

  .content-row {
    grid-template-columns: 1fr;
  }

  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
  }
  /* ✅ COLORED STAT BOXES */



}
</style>

