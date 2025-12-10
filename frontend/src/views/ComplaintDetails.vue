<template>
  <div class="complaint-details page">
    <div class="page-header">
      <h2>Complaint Details</h2>
      <el-button @click="$router.push('/complaints')">Back</el-button>
    </div>

    <div v-if="c" class="details-content">
      <el-card>
        <div class="header-row">
          <div>
            <h3>{{ c.title }}</h3>
            <p class="description">{{ c.description }}</p>
          </div>
          <div v-if="isResolved" class="export-actions">
            <el-button size="small" @click="exportCsv" type="info">Export CSV</el-button>
            <el-button size="small" @click="exportPdf" type="primary">Export PDF</el-button>
          </div>
        </div>
        
        <div class="status-section">
          <strong>Current Status:</strong>
          <el-tag :type="getStatusType(c.status)" size="large">{{ formatStatus(c.status) }}</el-tag>
        </div>
      </el-card>

      <el-card>
        <h3>Complaint Information</h3>
        <div class="info-grid">
          <div class="info-item">
            <strong>Category:</strong>
            <span>{{ formatCategory(c.category) }}</span>
          </div>
          <div class="info-item">
            <strong>Priority:</strong>
            <el-tag :type="getPriorityType(c.priority)" size="small">{{ c.priority }}</el-tag>
          </div>
          <div class="info-item" v-if="c.assignedOfficer">
            <strong>Assigned Officer:</strong>
            <span>{{ c.assignedOfficer.firstName }} {{ c.assignedOfficer.lastName }}</span>
          </div>
          <div class="info-item">
            <strong>Created:</strong>
            <span>{{ formatDate(c.createdAt) }}</span>
          </div>
          <div class="info-item" v-if="c.dueDate">
            <strong>Due Date:</strong>
            <span>{{ formatDate(c.dueDate) }}</span>
          </div>
          <div class="info-item" v-if="c.resolvedAt">
            <strong>Resolved:</strong>
            <span>{{ formatDate(c.resolvedAt) }}</span>
          </div>
        </div>
      </el-card>

      <el-card v-if="showRatingSection">
        <h3>Rate Officer</h3>
        <div v-if="c.assignedOfficer" class="rating-section">
          <p>
            Please rate how the assigned officer handled this complaint.
          </p>
          <div class="rating-row">
            <span class="rating-label">Your Rating:</span>
            <el-rate v-model="ratingForm.rating" :max="5" />
          </div>
          <el-input
            v-model="ratingForm.feedback"
            type="textarea"
            :rows="3"
            placeholder="Optional feedback about the officer's handling"
            maxlength="1000"
            show-word-limit
          />
          <div class="rating-actions">
            <el-button type="primary" size="small" :loading="ratingSubmitting" @click="submitRating">
              Submit Rating
            </el-button>
          </div>
        </div>
        <div v-else class="no-messages">
          <p>No officer is assigned to this complaint.</p>
        </div>
      </el-card>

      <el-card>
        <h3>Status History & Updates</h3>
        <div v-if="statusHistory.length > 0" class="timeline-container">
          <el-timeline>
            <el-timeline-item
              v-for="(entry, index) in statusHistory"
              :key="index"
              :timestamp="formatDate(entry.changedAt)"
              :type="getTimelineType(entry.status)"
              placement="top"
            >
              <div class="timeline-content">
                <div class="timeline-header">
                  <strong>{{ formatStatus(entry.status) }}</strong>
                  <el-tag v-if="entry.changedByUsername" size="small" type="info">
                    {{ entry.changedByUsername }}
                  </el-tag>
                </div>
                <p v-if="entry.comment" class="timeline-comment">{{ entry.comment }}</p>
                <p v-else class="timeline-comment no-comment">Status updated</p>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
        <div v-else class="no-messages">
          <p>No status updates yet.</p>
        </div>
      </el-card>

      <div v-if="c && (c.status === 'NEW' || c.status === 'UNDER_REVIEW')" class="actions">
        <el-button type="warning" @click="withdraw" :loading="loading">Withdraw Complaint</el-button>
      </div>
    </div>

    <div v-else class="loading">
      <el-skeleton :rows="3" animated />
    </div>
  </div>
</template>

<script>
import { getComplaint, withdrawComplaint, rateComplaint } from '../api/complaints'
import { ElMessageBox, ElMessage } from 'element-plus'
import JsPDF from 'jspdf'

export default {
  name: 'AppComplaintDetails',
  data() { 
    return { 
      c: null, 
      loading: false,
      ratingForm: {
        rating: null,
        feedback: ''
      },
      ratingSubmitting: false
    } 
  },
  computed: {
    statusHistory() {
      if (!this.c || !this.c.statusHistory) return []
      return this.c.statusHistory
        .map(entry => ({
          status: entry.status,
          comment: entry.comment,
          changedAt: entry.changedAt,
          changedByUsername: entry.changedByUsername
        }))
        .sort((a, b) => new Date(b.changedAt) - new Date(a.changedAt))
    },
    isResolved() {
      if (!this.c || !this.c.status) return false
      return this.c.status === 'RESOLVED' || this.c.status === 'CLOSED'
    },
    showRatingSection() {
      if (!this.c) return false
      if (!this.isResolved) return false
      if (!this.c.assignedOfficer) return false
      // Only show form if not yet rated; if you allow editing, remove this check
      return !this.c.officerRating
    }
  },
  mounted() {
    this.fetchComplaint()
  },
  methods: {
    fetchComplaint() {
      const id = this.$route.params.id
      getComplaint(id)
        .then(r => { 
          this.c = r.data
        })
        .catch(e => {
          ElMessage.error(e.response?.data?.message || 'Failed to load complaint')
          this.$router.push('/complaints')
        })
    },
    submitRating() {
      if (!this.c) return
      if (!this.ratingForm.rating) {
        ElMessage.error('Please select a rating before submitting')
        return
      }
      this.ratingSubmitting = true
      rateComplaint(this.c.id, {
        rating: this.ratingForm.rating,
        feedback: this.ratingForm.feedback
      })
        .then((r) => {
          ElMessage.success('Thank you for your feedback')
          this.c = r.data
        })
        .catch((e) => {
          ElMessage.error(e.response?.data?.message || 'Failed to submit rating')
        })
        .finally(() => {
          this.ratingSubmitting = false
        })
    },
    withdraw() {
      if (!this.c) return
      ElMessageBox.confirm('Withdraw this complaint?', 'Confirm', { type: 'warning' })
        .then(() => {
          this.loading = true
          withdrawComplaint(this.c.id)
            .then(() => { 
              ElMessage.success('Complaint withdrawn successfully')
              this.$router.push('/complaints') 
            })
            .catch(e => ElMessage.error(e.response?.data?.message || 'Failed to withdraw'))
            .finally(() => { this.loading = false })
        })
        .catch(() => {})
    },
    formatDate(value) {
      if (!value) return ''
      return new Date(value).toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    formatStatus(status) {
      if (!status) return ''
      return status.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())
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
    },
    getTimelineType(status) {
      const types = {
        NEW: 'primary',
        UNDER_REVIEW: 'warning',
        IN_PROGRESS: 'success',
        RESOLVED: 'success',
        CLOSED: 'info',
        ESCALATED: 'danger'
      }
      return types[status] || 'primary'
    },
    formatCategory(category) {
      if (!category) return ''
      return category.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())
    },
    exportCsv() {
      if (!this.c) return
      const rows = [
        ['Ticket ID', this.c.id],
        ['Title', this.c.title],
        ['Description', this.c.description],
        ['Category', this.formatCategory(this.c.category)],
        ['Priority', this.c.priority],
        ['Status', this.formatStatus(this.c.status)],
        ['Created At', this.formatDate(this.c.createdAt)],
        ['Due Date', this.c.dueDate ? this.formatDate(this.c.dueDate) : ''],
        ['Resolved At', this.c.resolvedAt ? this.formatDate(this.c.resolvedAt) : ''],
        ['Assigned Officer', this.c.assignedOfficer ? `${this.c.assignedOfficer.firstName || ''} ${this.c.assignedOfficer.lastName || ''}`.trim() : '']
      ]

      const historyHeader = ['Status', 'Comment', 'Changed By', 'Changed At']
      const historyRows = (this.statusHistory || []).map(h => [
        this.formatStatus(h.status),
        h.comment || '',
        h.changedByUsername || '',
        this.formatDate(h.changedAt)
      ])

      const allRows = [
        ...rows,
        [],
        ['Status History'],
        historyHeader,
        ...historyRows
      ]

      const csvContent = allRows
        .map(cols => cols.map(value => {
          if (value == null) return ''
          const s = String(value).replace(/"/g, '""')
          return `"${s}"`
        }).join(','))
        .join('\r\n')

      const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      const fileName = `complaint-${this.c.id || 'summary'}.csv`
      link.setAttribute('href', url)
      link.setAttribute('download', fileName)
      link.style.visibility = 'hidden'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(url)
    },
    exportPdf() {
      if (!this.c) return
      const doc = new JsPDF()
      const marginLeft = 14
      let y = 16

      doc.setFontSize(16)
      doc.text('Complaint Summary', marginLeft, y)
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

      addLine('Ticket ID', this.c.id)
      addLine('Title', this.c.title)
      addLine('Description', this.c.description)
      addLine('Category', this.formatCategory(this.c.category))
      addLine('Priority', this.c.priority)
      addLine('Status', this.formatStatus(this.c.status))
      addLine('Created At', this.formatDate(this.c.createdAt))
      addLine('Due Date', this.c.dueDate ? this.formatDate(this.c.dueDate) : '')
      addLine('Resolved At', this.c.resolvedAt ? this.formatDate(this.c.resolvedAt) : '')
      addLine(
        'Assigned Officer',
        this.c.assignedOfficer
          ? `${this.c.assignedOfficer.firstName || ''} ${this.c.assignedOfficer.lastName || ''}`.trim()
          : ''
      )

      y += 4
      doc.setFontSize(13)
      doc.text('Status History', marginLeft, y)
      y += 6
      doc.setFontSize(11)

      ;(this.statusHistory || []).forEach((h) => {
        addLine('Status', this.formatStatus(h.status))
        if (h.comment) addLine('Comment', h.comment)
        if (h.changedByUsername) addLine('Changed By', h.changedByUsername)
        addLine('Changed At', this.formatDate(h.changedAt))
        y += 2
      })

      const fileName = `complaint-${this.c.id || 'summary'}.pdf`
      doc.save(fileName)
    }
  }
}
</script>

<style scoped>
.complaint-details {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.export-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
}

.details-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.el-card h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
}

.description {
  margin: 0 0 20px 0;
  line-height: 1.6;
  color: #374151;
}

.status-section {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item strong {
  color: #6b7280;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-item span {
  color: #374151;
  font-size: 15px;
}

.timeline-container {
  margin-top: 16px;
}

.timeline-content {
  padding: 8px 0;
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.timeline-comment {
  margin: 8px 0 0 0;
  color: #374151;
  line-height: 1.6;
  padding: 8px;
  background-color: #f9fafb;
  border-radius: 4px;
}

.timeline-comment.no-comment {
  color: #6b7280;
  font-style: italic;
  background-color: transparent;
  padding: 0;
}

.no-messages {
  color: #6b7280;
  text-align: center;
  padding: 20px;
}

.rating-section {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rating-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rating-label {
  min-width: 100px;
  font-weight: 600;
}

.rating-actions {
  margin-top: 8px;
}

.actions {
  margin-top: 20px;
}

.loading {
  padding: 24px;
}
</style>
