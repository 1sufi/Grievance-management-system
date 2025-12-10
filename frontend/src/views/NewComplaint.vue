<template>
  <div class="page">
    <h2>New Complaint</h2>
    <el-form ref="form" :model="form" :rules="rules" label-width="120px" @submit.prevent>
      <el-form-item label="Title" prop="title">
        <el-input v-model="form.title" placeholder="Short title" />
      </el-form-item>
      <el-form-item label="Description" prop="description">
        <el-input v-model="form.description" type="textarea" rows="4" placeholder="Describe the issue" />
      </el-form-item>
      <el-form-item label="Category" prop="category">
        <el-select v-model="form.category" placeholder="Select">
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
        <el-select v-model="form.priority" placeholder="Select">
          <el-option label="Low" value="LOW" />
          <el-option label="Medium" value="MEDIUM" />
          <el-option label="High" value="HIGH" />
          <el-option label="Urgent" value="URGENT" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="submit">Submit</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { ElMessage } from 'element-plus'
import { createComplaint } from '../api/complaints'

export default {
  name: 'AppNewComplaint',
  data() {
    return {
      loading: false,
      form: {
        title: '',
        description: '',
        category: '',
        priority: ''
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
        createComplaint(this.form)
          .then(() => {
            ElMessage.success('Complaint submitted')
            this.$router.push('/complaints')
          })
          .catch(err => {
            ElMessage.error(err.response?.data?.message || 'Submission failed')
          })
          .finally(() => { this.loading = false })
      })
    }
  }
}
</script>
