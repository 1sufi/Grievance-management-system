<template>
  <div class="anonymous-page">
    <el-card class="anonymous-card" shadow="never">
      <div class="card-header">
        <h2>Anonymous Complaint</h2>
        <p class="subtitle">
          Submit your grievance without revealing your identity.
        </p>
      </div>

      <el-alert
        type="info"
        show-icon
        class="info-alert"
        title="You are submitting anonymously. Tracking will be limited."
      />

      <el-form
        ref="form"
        :model="form"
        :rules="rules"
        label-position="top"
        class="complaint-form"
      >
        <el-form-item label="Title" prop="title">
          <el-input v-model="form.title" placeholder="Enter short complaint title" />
        </el-form-item>

        <el-form-item label="Description" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            rows="4"
            placeholder="Describe your issue clearly"
          />
        </el-form-item>

        <el-form-item label="Category" prop="category">
          <el-select v-model="form.category" placeholder="Select category">
            <el-option label="Smart City" value="SMART_CITY" />
            <el-option label="Municipal Corporation" value="MUNICIPAL_CORPORATION" />
            <el-option label="Government Services" value="GOVERNMENT_SERVICES" />
            <el-option label="IT Helpdesk" value="IT_HELPDESK" />
            <el-option label="University/College" value="UNIVERSITY_COLLEGE" />
            <el-option label="Corporate Support" value="CORPORATE_SUPPORT" />
            <el-option label="Housing/Society" value="HOUSING_SOCIETY" />
            <el-option label="Citizen Grievance" value="CITIZEN_GRIEVANCE" />
          </el-select>
        </el-form-item>

        <el-form-item label="Priority" prop="priority">
          <el-select v-model="form.priority" placeholder="Select priority">
            <el-option label="Low" value="LOW" />
            <el-option label="Medium" value="MEDIUM" />
            <el-option label="High" value="HIGH" />
            <el-option label="Urgent" value="URGENT" />
          </el-select>
        </el-form-item>

        <el-divider />

        <el-form-item label="Email (optional)">
          <el-input v-model="form.anonymousEmail" placeholder="Optional email for updates" />
        </el-form-item>

        <el-form-item label="Phone (optional)">
          <el-input v-model="form.anonymousPhone" placeholder="Optional phone number" />
        </el-form-item>

        <el-form-item class="submit-row">
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="submit"
          >
            Submit Complaint
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>



<script>
import { ElMessage } from 'element-plus'
import { createAnonymousComplaint } from '../api/complaints'

export default {
  name: 'AppAnonymousComplaint',
  data() {
    return {
      loading: false,
      form: {
        title: '', description: '', category: '', priority: '', anonymousEmail: '', anonymousPhone: ''
      },
      rules: {
        title: [{ required: true, message: 'Title required', trigger: 'blur' }],
        description: [{ required: true, message: 'Description required', trigger: 'blur' }],
        category: [{ required: true, message: 'Category required', trigger: 'change' }],
        priority: [{ required: true, message: 'Priority required', trigger: 'change' }]
      }
    }
  },
  methods: {
    submit() {
      this.$refs.form.validate((valid) => {
        if (!valid) return
        this.loading = true
        createAnonymousComplaint(this.form)
          .then(() => { ElMessage.success('Submitted'); this.$router.push('/login') })
          .catch(e => { ElMessage.error(e.response?.data?.message || 'Submission failed') })
          .finally(() => { this.loading = false })
      })
    }
  }
}
</script>
<style scoped>
  .anonymous-page {
    min-height: 100vh;
    background: linear-gradient(180deg, #f8fafc, #eef2f7);
    display: flex;
    justify-content: center;
    align-items: flex-start;
    padding: 40px 16px;
  }
  
  .anonymous-card {
    width: 100%;
    max-width: 700px;
    border-radius: 16px;
    border: 1px solid #e5e7eb;
    box-shadow: 0 12px 36px rgba(15, 23, 42, 0.08);
    padding: 26px 28px;
  }
  
  .card-header {
    margin-bottom: 18px;
  }
  
  .card-header h2 {
    margin: 0;
    font-size: 26px;
    font-weight: 800;
    color: #0f172a;
  }
  
  .subtitle {
    font-size: 14px;
    color: #64748b;
    margin-top: 6px;
  }
  
  .info-alert {
    margin-bottom: 18px;
  }
  
  .complaint-form {
    margin-top: 10px;
  }
  
  .submit-row {
    margin-top: 20px;
    text-align: center;
  }
  </style>

