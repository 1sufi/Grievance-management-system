<template>
  <div class="admin-complaints page">
    <div class="page-header">
      <div class="header-text">
        <h2>Complaint Control Center</h2>
        <p>Monitor, triage, and resolve grievances submitted across the city.</p>
      </div>
      <div class="header-actions">
        <el-button type="default" plain @click="fetchComplaints" :loading="loading">
          Refresh
        </el-button>
      </div>
    </div>

    <div class="kpi-row">
      <el-card class="kpi-card total" shadow="hover">
        <div class="kpi-label">Total complaints</div>
        <div class="kpi-value">{{ complaints.length }}</div>
      </el-card>
      <el-card class="kpi-card open" shadow="hover">
        <div class="kpi-label">Open (New / Under review / In progress)</div>
        <div class="kpi-value">{{ openCount }}</div>
      </el-card>
      <el-card class="kpi-card resolved" shadow="hover">
        <div class="kpi-label">Resolved / Closed</div>
        <div class="kpi-value">{{ resolvedCount }}</div>
      </el-card>
    </div>

    <el-table
      v-loading="loading"
      :data="complaints"
      border
      stripe
      class="complaints-table"
      empty-text="No complaints reported yet"
    >
      <el-table-column prop="ticketNumber" label="Ticket" width="140" />
      <el-table-column label="Title" min-width="260">
        <template #default="{ row }">
          <div class="title-cell">
            <strong>{{ row.title }}</strong>
            <small>{{ row.category }}</small>
            <div class="title-pills">
              <span v-if="row.status === 'ESCALATED'" class="pill pill-escalated">
                Sent to Senior Officer
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
      <el-table-column label="Citizen" min-width="180">
        <template #default="{ row }">
          <div v-if="row.citizen">
            <strong>{{ row.citizen.firstName }} {{ row.citizen.lastName }}</strong>
            <small>{{ row.citizen.email }}</small>
          </div>
          <span v-else>Anonymous</span>
        </template>
      </el-table-column>
      <el-table-column label="Assigned L1 Officer" min-width="220">
        <template #default="{ row }">
          <el-select
            v-model="row.editAssignedOfficerId"
            size="small"
            placeholder="Select L1 officer"
            filterable
            clearable
            :loading="officersLoading"
          >
            <el-option
              :key="'none'"
              label="Unassigned"
              :value="null"
            />
            <el-option
              v-for="officer in officers"
              :key="officer.id"
              :label="formatOfficer(officer)"
              :value="officer.id"
            />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="Status" width="180">
        <template #default="{ row }">
          <el-select v-model="row.editStatus" size="small" placeholder="Status">
            <el-option
              v-for="status in statusOptions"
              :key="status"
              :label="status === 'RESOLVED_CLOSED' ? 'Resolved/Closed' : status.replace('_', ' ')"
              :value="status"
            />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="Remaining SLA (hrs)" width="180">
        <template #default="{ row }">
          <span :class="slaClass(row)">{{ remainingHours(row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="Deadline" width="200">
        <template #default="{ row }">
          <span>{{ formatDate(getDeadline(row)) || '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="Last Updated" width="200">
        <template #default="{ row }">
          <div class="timestamp-cell" :class="deadlineClass(row)">
            <span>{{ formatDate(row.updatedAt) }}</span>
            <small>{{ formatDate(row.createdAt) }}</small>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="Actions" width="320" fixed="right">
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
            <el-dropdown v-if="isResolved(row)" trigger="click">
              <span class="el-dropdown-link">
                <el-button size="small" type="info">Export</el-button>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="exportRowCsv(row)">Export CSV</el-dropdown-item>
                  <el-dropdown-item @click="exportRowPdf(row)">Export PDF</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button size="small" type="warning" @click="openInternalNote(row)">
              Internal note
            </el-button>
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
          <h4>Status History</h4>
          <el-timeline>
            <el-timeline-item
              v-for="entry in selectedComplaint.statusHistory"
              :key="entry.changedAt + entry.status + (entry.internalNote ? '-internal' : '')"
              :timestamp="formatDate(entry.changedAt)"
            >
              <strong>
                <span v-if="entry.internalNote" class="internal-pill">INTERNAL NOTE</span>
                {{ entry.status }}
              </strong>
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
    <el-dialog
      v-model="internalNoteDialogVisible"
      title="Send internal note to officer"
      width="500px"
    >
      <p v-if="internalNoteTarget">
        Ticket: <strong>{{ internalNoteTarget.ticketNumber || internalNoteTarget.id }}</strong>
      </p>
      <el-input
        v-model="internalNoteMessage"
        type="textarea"
        :rows="4"
        placeholder="Write a private note for the assigned officer"
        maxlength="500"
        show-word-limit
      />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="internalNoteDialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="submitInternalNote">Send</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import JsPDF from 'jspdf'
import { adminListComplaints, adminGetComplaint, adminUpdateComplaint, adminAddInternalNote } from '../api/complaints'
import { adminListOfficers } from '../api/officers'

const complaints = ref([])
const loading = ref(false)

const openCount = computed(() =>
  complaints.value.filter(c => !['RESOLVED', 'CLOSED'].includes(c.status)).length
)
const resolvedCount = computed(() =>
  complaints.value.filter(c => ['RESOLVED', 'CLOSED'].includes(c.status)).length
)
const detailsLoading = ref(false)
const detailsVisible = ref(false)
const selectedComplaint = ref(null)

const internalNoteDialogVisible = ref(false)
const internalNoteTarget = ref(null)
const internalNoteMessage = ref('')

const officers = ref([])
const officersLoading = ref(false)

const statusOptions = [
  'NEW',
  'UNDER_REVIEW',
  'IN_PROGRESS',
  'RESOLVED_CLOSED'
]

const DEADLINE_WARNING_HOURS = 2

const isResolved = (complaint) =>
  complaint && (complaint.status === 'RESOLVED' || complaint.status === 'CLOSED')

const isResolvedOrClosed = (status) =>
  status === 'RESOLVED' || status === 'CLOSED'

const isOverdue = (complaint) => {
  const deadline = getDeadline(complaint)
  if (!deadline || isResolvedOrClosed(complaint.status)) return false
  const due = deadline.getTime()
  const now = Date.now()
  return due < now
}

const isNearDeadline = (complaint) => {
  const deadline = getDeadline(complaint)
  if (!deadline || isResolvedOrClosed(complaint.status)) return false
  const due = deadline.getTime()
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

const SLA_HOURS = {
  LOW: 48,
  MEDIUM: 24,
  HIGH: 12,
  URGENT: 8
}

const getDeadline = (complaint) => {
  if (!complaint) return null
  if (complaint.dueDate) return new Date(complaint.dueDate)
  if (!complaint.priority || !complaint.createdAt) return null
  const hours = SLA_HOURS[complaint.priority] || SLA_HOURS.LOW
  return new Date(new Date(complaint.createdAt).getTime() + hours * 60 * 60 * 1000)
}

const remainingHours = (complaint) => {
  const deadline = getDeadline(complaint)
  if (!deadline) return '—'
  const now = Date.now()
  const remaining = Math.round((deadline.getTime() - now) / (1000 * 60 * 60))
  if (remaining <= 0) return 'Overdue'
  return `${remaining}h`
}

const slaClass = (complaint) => {
  const value = remainingHours(complaint)
  if (value === 'Overdue') return 'deadline-overdue'
  return ''
}

const formatDate = (value) => {
  if (!value) return ''
  const date = new Date(value)
  return date.toLocaleString()
}

const formatOfficer = (officer) => {
  if (!officer) return ''
  const name = [officer.firstName, officer.lastName].filter(Boolean).join(' ')
  const displayName = name || officer.username
  return officer.officerLevel ? `${displayName} (${officer.officerLevel})` : displayName
}

const decorateComplaint = (complaint) => ({
  ...complaint,
  editStatus: isResolvedOrClosed(complaint.status) ? 'RESOLVED_CLOSED' : complaint.status,
  editAssignedOfficerId: complaint.assignedOfficer?.id || null,
  editComment: '',
  saving: false
})

const fetchComplaints = async () => {
  loading.value = true
  try {
    const { data } = await adminListComplaints()
    complaints.value = data.map(decorateComplaint)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || 'Unable to load complaints')
  } finally {
    loading.value = false
  }
}

const fetchOfficers = async () => {
  officersLoading.value = true
  try {
    const { data } = await adminListOfficers({ level: 'L1' })
    officers.value = data || []
  } catch (error) {
    ElMessage.error(error.response?.data?.message || 'Unable to load officers')
  } finally {
    officersLoading.value = false
  }
}

const openDetails = async (row) => {
  detailsVisible.value = true
  detailsLoading.value = true
  try {
    const { data } = await adminGetComplaint(row.id)
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
      status: row.editStatus === 'RESOLVED_CLOSED' ? 'RESOLVED' : row.editStatus,
      assignedOfficerId: row.editAssignedOfficerId,
      // If no officer selected, explicitly unassign
      unassignOfficer: row.editAssignedOfficerId == null,
      adminComment: row.editComment || null
    }

    const { data } = await adminUpdateComplaint(row.id, payload)
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

const openInternalNote = (row) => {
  internalNoteTarget.value = row
  internalNoteMessage.value = ''
  internalNoteDialogVisible.value = true
}

const submitInternalNote = async () => {
  if (!internalNoteTarget.value) return
  if (!internalNoteMessage.value.trim()) {
    ElMessage.error('Please enter a message')
    return
  }
  try {
    const { data } = await adminAddInternalNote(internalNoteTarget.value.id, {
      message: internalNoteMessage.value.trim()
    })
    // If drawer is open for this complaint, refresh its data so admin sees the note
    if (selectedComplaint.value && selectedComplaint.value.id === data.id) {
      selectedComplaint.value = data
    }
    ElMessage.success('Internal note sent to officer')
    internalNoteDialogVisible.value = false
    internalNoteTarget.value = null
    internalNoteMessage.value = ''
  } catch (error) {
    ElMessage.error(error.response?.data?.message || 'Failed to send internal note')
  }
}

const buildUserName = (user) => {
  if (!user) return ''
  const name = [user.firstName, user.lastName].filter(Boolean).join(' ')
  return name || user.username || ''
}

const exportRowCsv = (row) => {
  const complaint = row
  const rows = [
    ['Ticket ID', complaint.ticketNumber || complaint.id],
    ['Title', complaint.title],
    ['Description', complaint.description],
    ['Category', complaint.category],
    ['Priority', complaint.priority],
    ['Status', complaint.status],
    ['Created At', formatDate(complaint.createdAt)],
    ['Due Date', complaint.dueDate ? formatDate(complaint.dueDate) : ''],
    ['Resolved At', complaint.resolvedAt ? formatDate(complaint.resolvedAt) : ''],
    ['Citizen', complaint.citizen ? buildUserName(complaint.citizen) : 'Anonymous'],
    ['Assigned Officer', complaint.assignedOfficer ? buildUserName(complaint.assignedOfficer) : '']
  ]

  const csvContent = rows
    .map(cols => cols.map(value => {
      if (value == null) return ''
      const s = String(value).replace(/"/g, '""')
      return `"${s}` + '"'
    }).join(','))
    .join('\r\n')

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  const fileName = `admin-complaint-${complaint.id || 'summary'}.csv`
  link.setAttribute('href', url)
  link.setAttribute('download', fileName)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

const exportRowPdf = (row) => {
  const complaint = row
  const doc = new JsPDF()
  const marginLeft = 14
  let y = 16

  doc.setFontSize(16)
  doc.text('Complaint Summary (Admin)', marginLeft, y)
  y += 8

  doc.setFontSize(11)
  const addLine = (label, value) => {
    if (y > 280) {
      doc.addPage()
      y = 16
    }
    doc.text(`${label}: ${value || ''}`, marginLeft, y)
    y += 6
  }

  addLine('Ticket ID', complaint.ticketNumber || complaint.id)
  addLine('Title', complaint.title)
  addLine('Description', complaint.description)
  addLine('Category', complaint.category)
  addLine('Priority', complaint.priority)
  addLine('Status', complaint.status)
  addLine('Created At', formatDate(complaint.createdAt))
  addLine('Due Date', complaint.dueDate ? formatDate(complaint.dueDate) : '')
  addLine('Resolved At', complaint.resolvedAt ? formatDate(complaint.resolvedAt) : '')
  addLine('Citizen', complaint.citizen ? buildUserName(complaint.citizen) : 'Anonymous')
  addLine('Assigned Officer', complaint.assignedOfficer ? buildUserName(complaint.assignedOfficer) : '')

  const fileName = `admin-complaint-${complaint.id || 'summary'}.pdf`
  doc.save(fileName)
}

onMounted(() => {
  fetchComplaints()
  fetchOfficers()
})
</script>

<style scoped>
/* Global Container */
.admin-complaints {
  padding: 32px 32px 40px;
  min-height: 100vh;
  background: #0b1220;
  color: #e2e8f0;
  width: 100%;
}

.page-header {
  text-align: center;
  margin-bottom: 24px;
}

.header-text h2 {
  margin: 0;
  font-size: 32px;
  font-weight: 800;
  color: #f8fafc;
}

.header-text p {
  margin-top: 6px;
  font-size: 14px;
  color: #cbd5f5;
}

.header-actions {
  display: flex;
  justify-content: center;
  margin-top: 12px;
}

/* KPI row */
.kpi-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin: 0 0 20px;
}

.kpi-card {
  border-radius: 14px !important;
  border: 1px solid rgba(148, 163, 184, 0.5) !important;
  background: radial-gradient(circle at top, rgba(15, 23, 42, 0.9), rgba(15, 23, 42, 0.8));
}

.kpi-label {
  font-size: 13px;
  color: #9ca3af;
  margin-bottom: 4px;
}

.kpi-value {
  font-size: 24px;
  font-weight: 700;
  color: #f9fafb;
}

.kpi-card.total { box-shadow: 0 10px 28px rgba(59, 130, 246, 0.4); }
.kpi-card.open { box-shadow: 0 10px 28px rgba(251, 191, 36, 0.4); }
.kpi-card.resolved { box-shadow: 0 10px 28px rgba(34, 197, 94, 0.4); }

/* TABLE */
.complaints-table {
  background-color: rgba(255, 255, 255, 0.03);
  border-radius: 16px;
  overflow: hidden;
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.06);
  box-shadow: 0 10px 35px rgba(0, 0, 0, 0.45);
  width: 100%;
}

/* Title Cell */
.title-cell {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.title-cell strong {
  color: #f1f5f9;
  font-weight: 600;
}

.title-cell small {
  color: #93a3b8;
  text-transform: capitalize;
}

/* Timestamp Cell */
.internal-pill {
  display: inline-block;
  margin-right: 6px;
  padding: 2px 6px;
  border-radius: 999px;
  font-size: 10px;
  font-weight: 600;
  background: rgba(251, 191, 36, 0.2);
  color: #facc15;
}

.timestamp-cell {
  display: flex;
  flex-direction: column;
  font-size: 13px;
  color: #d1d5db;
}

.timestamp-cell small {
  color: #9ca3af;
}

/* Deadline state highlighting */
.timestamp-cell.deadline-warning small {
  color: #fbbf24 !important; /* amber */
  font-weight: 600;
}

.timestamp-cell.deadline-overdue small,
.deadline-overdue {
  color: #f97373 !important; /* red */
  font-weight: 700;
}

.timestamp-cell.deadline-escalated small {
  color: #38bdf8 !important; /* sky */
  font-weight: 700;
}

/* Pill badges under title */
.title-pills {
  display: flex;
  gap: 6px;
  margin-top: 4px;
}

.pill {
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.02em;
  text-transform: uppercase;
}

.pill-escalated {
  background: rgba(56, 189, 248, 0.15);
  color: #38bdf8;
}

.pill-warning {
  background: rgba(251, 191, 36, 0.18);
  color: #fbbf24;
}

.pill-overdue {
  background: rgba(248, 113, 113, 0.18);
  color: #f87171;
}

/* Dropdown Styles */
:deep(.el-select .el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.08);
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  transition: 0.2s ease;
}

:deep(.el-select:hover .el-input__wrapper) {
  border-color: #3b82f6;
}

/* Actions */
.actions-cell {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.comment-input {
  margin-top: 6px;
}

/* Drawer Styling */
.drawer-content {
  padding: 10px 0;
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.details-section {
  padding: 12px 14px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
}

.details-section h3,
.details-section h4 {
  margin: 0 0 8px;
  color: #e2e8f0;
}

.details-section .description {
  margin-bottom: 10px;
  color: #cbd5e1;
  line-height: 1.5;
}

.meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* Status Badges */
.badge {
  padding: 4px 12px;
  font-size: 11px;
  border-radius: 50px;
  font-weight: 600;
  background-color: rgba(255, 255, 255, 0.15);
  color: #f8fafc;
}

/* Drawer Empty */
.drawer-empty,
.drawer-loading {
  padding: 20px;
  text-align: center;
  color: #cbd5e1;
}

/* Hover effects */
:deep(.el-button:hover) {
  transform: translateY(-1px);
  transition: 0.2s ease;
}
/* Drawer background fix */
:deep(.el-drawer__body) {
  background: #0f172a !important;      /* Dark navy background */
  color: #e2e8f0 !important;           /* Light readable text */
  padding: 24px !important;
}

/* Drawer title bar */
:deep(.el-drawer__header) {
  background: #0f172a !important;
  color: #f8fafc !important;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

/* Improve headings inside drawer */
.details-section h3,
.details-section h4 {
  color: #f1f5f9 !important;
}

/* Description text */
.details-section .description {
  color: #cbd5e1 !important;
}

/* Citizen text */
.drawer-content p {
  color: #e5e7eb !important;
}

/* Timeline text fix */
:deep(.el-timeline-item__content),
:deep(.el-timeline-item__timestamp) {
  color: #e2e8f0 !important;
}

/* Badge fix (dark mode) */
.badge {
  background-color: rgba(255, 255, 255, 0.12) !important;
  color: #f8fafc !important;
}
/* Make table text readable on white background */
:deep(.el-table),
:deep(.el-table__body),
:deep(.el-table__row),
:deep(.el-table__cell) {
  color: #1e293b !important;   /* dark slate */
  font-weight: 500;
}

/* Title and category inside table */
.title-cell strong {
  color: #0f172a !important; /* strong dark */
}

.title-cell small {
  color: #475569 !important; /* mid gray */
}

/* Citizen section */
:deep(.el-table__cell strong) {
  color: #0f172a !important;
}

:deep(.el-table__cell small) {
  color: #475569 !important;
}

/* Timestamp */
.timestamp-cell span {
  color: #1e293b !important;
}

.timestamp-cell small {
  color: #64748b !important;
}

/* Comment input placeholder */
:deep(.el-textarea__inner::placeholder) {
  color: #94a3b8 !important;
}

</style>
