<template>
  <div class="officer-complaints page">
    <div class="page-header">
      <div>
        <h2>Officer Complaint Dashboard</h2>
        <p>View and update complaints assigned to you by the admin.</p>
      </div>
      <el-button type="primary" :loading="loading" @click="fetchComplaints">
        Refresh
      </el-button>
    </div>

    <div class="kpi-row">
      <el-card class="kpi-card assigned" shadow="hover">
        <div class="kpi-label">Assigned</div>
        <div class="kpi-value">{{ totalAssigned }}</div>
      </el-card>
      <el-card class="kpi-card in-progress" shadow="hover">
        <div class="kpi-label">In Progress</div>
        <div class="kpi-value">{{ inProgressCount }}</div>
      </el-card>
      <el-card class="kpi-card resolved" shadow="hover">
        <div class="kpi-label">Resolved / Closed</div>
        <div class="kpi-value">{{ resolvedCount }}</div>
      </el-card>
      <el-card class="kpi-card overdue" shadow="hover">
        <div class="kpi-label">Overdue (Deadline Passed)</div>
        <div class="kpi-value">{{ overdueCount }}</div>
      </el-card>
    </div>

    <el-table
      v-loading="loading"
      :data="complaints"
      border
      stripe
      class="complaints-table"
      empty-text="No complaints assigned to you yet"
    >
      <el-table-column prop="ticketNumber" label="Ticket" width="140" />

      <el-table-column label="Title" min-width="260">
        <template #default="{ row }">
          <div class="title-cell">
            <strong>{{ row.title }}</strong>
            <small>{{ row.category }}</small>
            <div class="title-pills">
              <span v-if="row.status === 'ESCALATED'" class="pill pill-escalated">
                Escalated to L2
              </span>
              <span v-else-if="isOverdue(row)" class="pill pill-overdue">
                Overdue
              </span>
              <span v-else-if="isNearDeadline(row)" class="pill pill-warning">
                Deadline soon
              </span>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="Citizen" min-width="200">
        <template #default="{ row }">
          <div v-if="row.citizen">
            <strong>{{ row.citizen.firstName }} {{ row.citizen.lastName }}</strong>
            <small>{{ row.citizen.email }}</small>
          </div>
          <span v-else>Anonymous</span>
        </template>
      </el-table-column>

      <el-table-column label="Rating" width="180">
        <template #default="{ row }">
          <div v-if="row.officerRating" class="rating-cell">
            <el-rate v-model="row.officerRating" disabled :max="5" />
            <small v-if="row.officerFeedback" class="rating-feedback">
              "{{ row.officerFeedback }}"
            </small>
          </div>
          <span v-else-if="isResolvedOrClosed(row.status)" class="no-rating">Not yet rated</span>
          <span v-else class="no-rating">—</span>
        </template>
      </el-table-column>

      <el-table-column label="Status" width="180">
        <template #default="{ row }">
          <el-select v-model="row.editStatus" size="small" placeholder="Status">
            <el-option
              v-for="status in statusOptions"
              :key="status"
              :label="status.replace('_', ' ')"
              :value="status"
            />
          </el-select>
        </template>
      </el-table-column>

      <el-table-column label="Last Updated" width="220">
        <template #default="{ row }">
          <div class="timestamp-cell" :class="deadlineClass(row)">
            <span>{{ formatDate(row.updatedAt) }}</span>
            <small>Due {{ formatDate(row.dueDate) || '—' }}</small>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="Actions" width="220" fixed="right">
        <template #default="{ row }">
          <div class="actions-cell">
            <el-button size="small" @click="openDetails(row)">Details</el-button>
            <el-popconfirm
              title="Apply updates to this complaint?"
              confirm-button-text="Update"
              cancel-button-text="Cancel"
              @confirm="handleSave(row)"
            >
              <template #reference>
                <el-button type="primary" size="small" :loading="row.saving">
                  Save
                </el-button>
              </template>
            </el-popconfirm>
          </div>
          <el-input
            v-model="row.editComment"
            type="textarea"
            :rows="2"
            placeholder="Add an internal note (optional)"
            class="comment-input"
          />
        </template>
      </el-table-column>
    </el-table>

    <el-drawer
      v-model="detailsVisible"
      title="Complaint Details"
      size="40%"
      destroy-on-close
    >
      <div v-if="detailsLoading" class="drawer-loading">
        <el-skeleton :rows="6" animated />
      </div>
      <div v-else-if="selectedComplaint" class="drawer-content">
        <section class="details-section">
          <h3>{{ selectedComplaint.title }}</h3>
          <p class="description">{{ selectedComplaint.description }}</p>
          <div class="meta">
            <span class="badge">{{ selectedComplaint.category }}</span>
            <span class="badge">{{ selectedComplaint.priority }}</span>
            <span class="badge">{{ selectedComplaint.status }}</span>
          </div>
        </section>

        <section class="details-section">
          <h4>Citizen</h4>
          <p v-if="selectedComplaint.citizen">
            {{ selectedComplaint.citizen.firstName }} {{ selectedComplaint.citizen.lastName }}<br />
            <small>{{ selectedComplaint.citizen.email }}</small>
          </p>
          <p v-else>Submitted anonymously</p>
        </section>

        <section class="details-section">
          <h4>Citizen Rating</h4>
          <div v-if="selectedComplaint.officerRating">
            <el-rate v-model="selectedComplaint.officerRating" disabled :max="5" />
            <p v-if="selectedComplaint.officerFeedback" class="rating-feedback-text">
              "{{ selectedComplaint.officerFeedback }}"
            </p>
          </div>
          <p v-else-if="isResolvedOrClosed(selectedComplaint.status)">
            This complaint has not yet been rated by the citizen.
          </p>
          <p v-else>
            Rating will be available once the complaint is resolved or closed.
          </p>
        </section>

        <section class="details-section">
          <h4>Status History</h4>
          <el-timeline>
            <el-timeline-item
              v-for="entry in selectedComplaint.statusHistory"
              :key="entry.changedAt + entry.status"
              :timestamp="formatDate(entry.changedAt)"
            >
              <strong>{{ entry.status }}</strong>
              <p v-if="entry.comment">{{ entry.comment }}</p>
              <small v-if="entry.changedByUsername">
                Updated by {{ entry.changedByUsername }}
              </small>
            </el-timeline-item>
          </el-timeline>
        </section>
      </div>
      <div v-else class="drawer-empty">
        <p>Select a complaint to view details.</p>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { officerListComplaints, officerGetComplaint, officerUpdateComplaint } from '../api/complaints'

const complaints = ref([])
const loading = ref(false)
const detailsLoading = ref(false)
const detailsVisible = ref(false)
const selectedComplaint = ref(null)

const statusOptions = [
  'NEW',
  'UNDER_REVIEW',
  'IN_PROGRESS',
  'RESOLVED',
  'CLOSED',
  'ESCALATED'
]

const DEADLINE_WARNING_HOURS = 2

const isResolvedOrClosed = (status) =>
  status === 'RESOLVED' || status === 'CLOSED'

const isOverdue = (complaint) => {
  if (!complaint.dueDate || isResolvedOrClosed(complaint.status)) return false
  const due = new Date(complaint.dueDate).getTime()
  const now = Date.now()
  return due < now
}

const isNearDeadline = (complaint) => {
  if (!complaint.dueDate || isResolvedOrClosed(complaint.status)) return false
  const due = new Date(complaint.dueDate).getTime()
  const now = Date.now()
  if (due <= now) return false
  const diffHours = (due - now) / (1000 * 60 * 60)
  return diffHours <= DEADLINE_WARNING_HOURS
}

const deadlineClass = (complaint) => {
  if (complaint.status === 'ESCALATED') return 'deadline-escalated'
  if (isOverdue(complaint)) return 'deadline-overdue'
  if (isNearDeadline(complaint)) return 'deadline-warning'
  return ''
}

const formatDate = (value) => {
  if (!value) return ''
  const date = new Date(value)
  return date.toLocaleString()
}

const totalAssigned = computed(() => complaints.value.length)

const inProgressCount = computed(() =>
  complaints.value.filter(c => c.status === 'IN_PROGRESS').length
)

const resolvedCount = computed(() =>
  complaints.value.filter(c => isResolvedOrClosed(c.status)).length
)

const overdueCount = computed(() =>
  complaints.value.filter(c => isOverdue(c)).length
)

const decorateComplaint = (complaint) => ({
  ...complaint,
  editStatus: complaint.status,
  editComment: '',
  saving: false
})

const fetchComplaints = async () => {
  loading.value = true
  try {
    const { data } = await officerListComplaints()
    complaints.value = data.map(decorateComplaint)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || 'Unable to load complaints')
  } finally {
    loading.value = false
  }
}

const openDetails = async (row) => {
  detailsVisible.value = true
  detailsLoading.value = true
  try {
    const { data } = await officerGetComplaint(row.id)
    selectedComplaint.value = data
  } catch (error) {
    detailsVisible.value = false
    ElMessage.error(error.response?.data?.message || 'Unable to load complaint details')
  } finally {
    detailsLoading.value = false
  }
}

const handleSave = async (row) => {
  row.saving = true
  try {
    const payload = {
      status: row.editStatus,
      officerComment: row.editComment || null
    }

    const { data } = await officerUpdateComplaint(row.id, payload)
    Object.assign(row, decorateComplaint(data))
    ElMessage.success('Complaint updated successfully')

    if (selectedComplaint.value && selectedComplaint.value.id === row.id) {
      selectedComplaint.value = data
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || 'Failed to update complaint')
  } finally {
    row.saving = false
  }
}

onMounted(() => {
  fetchComplaints()
})
</script>

<style scoped>
.officer-complaints {
  padding: 32px;
  min-height: 100vh;
  color: #e5e7eb;
  font-family: "Inter", sans-serif;
  backdrop-filter: blur(16px);
  background: white !important;
  border-radius: 18px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
}

/* KPI Row */
.kpi-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.kpi-card {
  border-radius: 16px !important;
  padding: 16px 18px !important;
  border: 1px solid rgba(148, 163, 184, 0.4) !important;
}

.kpi-label {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 6px;
}

.kpi-value {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
}

.kpi-card.assigned {
  background: linear-gradient(135deg, #e0f2fe, #dbeafe);
}

.kpi-card.in-progress {
  background: linear-gradient(135deg, #fef3c7, #ffedd5);
}

.kpi-card.resolved {
  background: linear-gradient(135deg, #dcfce7, #bbf7d0);
}

.kpi-card.overdue {
  background: linear-gradient(135deg, #fee2e2, #fecaca);
}

/* Page Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
  padding: 26px 30px;
  border-radius: 20px;
  background: rgba(17, 24, 39, 0.8);
  backdrop-filter: blur(14px);
  border: 1px solid rgba(148, 163, 184, 0.3);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.6);
}

.page-header h2 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #f9fafb;
}

.page-header p {
  margin-top: 4px;
  font-size: 15px;
  color: #cbd5f5;
}

/* Table */
.complaints-table {
  background: white !important;
  border-radius: 18px;
  overflow: hidden;
  backdrop-filter: blur(12px);
  border: 1px solid rgba(148, 163, 184, 0.35);
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);

}

/* Table Cells */
:deep(.el-table__cell) {
  padding: 18px 14px !important;
  font-size: 14px;
  border-color: rgba(255, 255, 255, 0.05) !important;
  background: transparent !important;
  color: #e5e7eb !important;
}

:deep(.el-table__row:hover td) {
  background: rgba(255, 255, 255, 0.08) !important;
}

/* Title Cell */
.title-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.title-cell strong {
  color: #0f172a !important;
  font-weight: 600;
  font-size: 15px;
}

.title-cell small {
  color: #9ca3af;
  font-size: 13px;
  text-transform: capitalize;
}

/* Pills */
.title-pills {
  display: flex;
  gap: 8px;
  margin-top: 4px;
}

.pill {
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}

.pill-escalated {
  background: rgba(56, 189, 248, 0.2);
  color: #38bdf8;
}

.pill-warning {
  background: rgba(251, 191, 36, 0.22);
  color: #fbbf24;
}

.pill-overdue {
  background: rgba(248, 113, 113, 0.22);
  color: #f87171;
}

/* Citizen Column */
:deep(.el-table__cell strong) {
  color: #111827 !important;
}
:deep(.el-table__cell small) {
  color: #4b5563 !important;
}

/* Status Dropdown */
:deep(.el-select .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.05) !important;
  border: 1px solid rgba(148, 163, 184, 0.4) !important;
  border-radius: 8px !important;
  box-shadow: none !important;
}

:deep(.el-select:hover .el-input__wrapper) {
  border-color: #60a5fa !important;
}

/* Timestamp */
.rating-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.no-rating {
  font-size: 12px;
  color: #6b7280;
}

.rating-feedback,
.rating-feedback-text {
  font-size: 12px;
  color: #4b5563;
  font-style: italic;
}

.timestamp-cell {
  display: flex;
  flex-direction: column;
  gap: 3px;
  font-size: 13px;
  color: #d1d5db;
}

.timestamp-cell small {
  color: #4b5563 !important;
}

/* Deadline Colors */
.deadline-warning small {
  color: #fbbf24 !important;
  font-weight: 600;
}

.deadline-overdue small {
  color: #f87171 !important;
  font-weight: 700;
}

.deadline-escalated small {
  color: #38bdf8 !important;
  font-weight: 700;
}

/* Action Buttons */
.actions-cell {
  display: flex;
  gap: 10px;
  margin-bottom: 8px;
}

:deep(.el-button) {
  border-radius: 8px !important;
}

:deep(.el-button--primary) {
  background: linear-gradient(90deg, #4f46e5, #6366f1) !important;
  border: none !important;
}

:deep(.el-button:hover) {
  transform: translateY(-2px);
  transition: 0.2s ease;
}

/* Notes Input */
.comment-input {
  margin-top: 6px;
}

:deep(.el-textarea__inner) {
  background: rgba(255, 255, 255, 0.05) !important;
  border: 1px solid rgba(148, 163, 184, 0.35) !important;
  color: #e5e7eb !important;
}

/* Drawer */
.drawer-content {
  padding: 12px 0;
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.details-section {
  padding: 14px 16px;
  border-radius: 14px;
  background: rgba(17, 24, 39, 0.95);
  border: 1px solid rgba(148, 163, 184, 0.45);
}

.details-section h3,
.details-section h4 {
  margin: 0 0 10px;
  color: #f1f5f9;
}

.details-section .description {
  margin-bottom: 10px;
  color: #cbd5e1;
  line-height: 1.55;
}

.badge {
  padding: 4px 12px;
  font-size: 11px;
  border-radius: 50px;
  font-weight: 600;
  background-color: rgba(59, 130, 246, 0.22);
  color: #bfdbfe;
}

/* Drawer Theme */
:deep(.el-drawer__body),
:deep(.el-drawer__header) {
  background: #020617 !important;
  color: #f8fafc !important;
}

/* Make status history text high contrast inside dark drawer */
:deep(.drawer-content p),
:deep(.drawer-content small),
:deep(.drawer-content strong),
:deep(.el-timeline-item__content),
:deep(.el-timeline-item__timestamp) {
  color: #e5e7eb !important; /* light gray for readability on dark bg */
}

/* Table Text Color Fix */
:deep(.el-table), 
:deep(.el-table__body),
:deep(.el-table__row), 
:deep(.el-table__cell) {
  color: #1f2937 !important;
}

</style>
