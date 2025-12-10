<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <h2>Register</h2>
          <p>Create your ResolveIT account</p>
        </div>
      </template>
      
      <el-form
        ref="registerForm"
        :model="registerForm"
        :rules="rules"
        label-width="120px"
        @submit.prevent="handleRegister"
      >
        <el-form-item label="Username" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="Choose a username"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item label="Email" prop="email">
          <el-input
            v-model="registerForm.email"
            type="email"
            placeholder="Enter your email"
            prefix-icon="Message"
          />
        </el-form-item>
        
        <el-form-item label="Password" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="Create a password"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="Confirm Password" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="Confirm your password"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="First Name" prop="firstName">
          <el-input
            v-model="registerForm.firstName"
            placeholder="Enter your first name"
          />
        </el-form-item>
        
        <el-form-item label="Last Name" prop="lastName">
          <el-input
            v-model="registerForm.lastName"
            placeholder="Enter your last name"
          />
        </el-form-item>
        
        <el-form-item label="Phone Number" prop="phoneNumber">
          <el-input
            v-model="registerForm.phoneNumber"
            placeholder="Enter your phone number"
            prefix-icon="Phone"
          />
        </el-form-item>
        
        <el-form-item label="Role" prop="role">
          <el-select v-model="registerForm.role" placeholder="Select your role" style="width: 100%">
            <el-option label="Citizen" value="CITIZEN" />
            <el-option label="Officer" value="OFFICER" />
            <el-option label="Admin" value="ADMIN" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleRegister"
            style="width: 100%"
          >
            Register
          </el-button>
        </el-form-item>
        
        <div class="register-footer">
          <p>
            Already have an account? 
            <router-link to="/login">Login here</router-link>
          </p>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { ElMessage } from 'element-plus'

export default {
  name: 'Register',
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.registerForm.password) {
        callback(new Error('Passwords do not match'))
      } else {
        callback()
      }
    }
    
    return {
      registerForm: {
        username: '',
        email: '',
        password: '',
        confirmPassword: '',
        firstName: '',
        lastName: '',
        phoneNumber: '',
        role: 'CITIZEN'
      },
      loading: false,
      rules: {
        username: [
          { required: true, message: 'Please enter username', trigger: 'blur' },
          { min: 3, max: 20, message: 'Username must be 3-20 characters', trigger: 'blur' }
        ],
        email: [
          { required: true, message: 'Please enter email', trigger: 'blur' },
          { type: 'email', message: 'Please enter a valid email', trigger: 'blur' }
        ],
        password: [
          { required: true, message: 'Please enter password', trigger: 'blur' },
          { min: 6, max: 40, message: 'Password must be 6-40 characters', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: 'Please confirm password', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ],
        firstName: [
          { required: true, message: 'Please enter first name', trigger: 'blur' }
        ],
        lastName: [
          { required: true, message: 'Please enter last name', trigger: 'blur' }
        ],
        phoneNumber: [
          { required: true, message: 'Please enter phone number', trigger: 'blur' }
        ],
        role: [
          { required: true, message: 'Please select a role', trigger: 'change' }
        ]
      }
    }
  },
  methods: {
    handleRegister() {
      this.$refs.registerForm.validate((valid) => {
        if (valid) {
          this.loading = true
          const { confirmPassword, ...formData } = this.registerForm
          this.$store.dispatch('register', formData)
            .then(() => {
              ElMessage.success('Registration successful! Please login.')
              this.$router.push('/login')
            })
            .catch(error => {
              ElMessage.error(error.response?.data?.message || 'Registration failed')
            })
            .finally(() => {
              this.loading = false
            })
        }
      })
    }
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-card {
  width: 500px;
  max-width: 100%;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0;
  color: #409eff;
  font-size: 28px;
  font-weight: bold;
}

.card-header p {
  margin: 5px 0 0 0;
  color: #666;
  font-size: 14px;
}

.register-footer {
  text-align: center;
  margin-top: 20px;
}

.register-footer p {
  margin: 5px 0;
  font-size: 14px;
}

.register-footer a {
  color: #409eff;
  text-decoration: none;
}

.register-footer a:hover {
  text-decoration: underline;
}
</style>
