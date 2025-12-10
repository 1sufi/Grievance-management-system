<template>
  <div class="login-container">
    <div class="login-background">
      <div class="background-shapes">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
      </div>
    </div>
    
    <div class="login-content">
      <div class="login-card">
        <div class="card-header">
          <div class="logo-container">
            <div class="logo-icon">
              <el-icon :size="48"><DocumentChecked /></el-icon>
            </div>
            <h1>ResolveIT</h1>
            <p class="tagline">Smart Grievance Management System</p>
          </div>
        </div>
        
        <el-form
          ref="loginForm"
          :model="loginForm"
          :rules="rules"
          @submit.prevent="handleLogin"
          class="login-form"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="Username"
              size="large"
              prefix-icon="User"
              class="input-field"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="Password"
              size="large"
              prefix-icon="Lock"
              show-password
              class="input-field"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          
          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              @click="handleLogin"
              size="large"
              class="login-button"
            >
              <span v-if="!loading">Sign In</span>
              <span v-else>Signing In...</span>
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="login-footer">
          <div class="footer-links">
            <router-link to="/register" class="link">
              <el-icon><UserFilled /></el-icon>
              Create Account
            </router-link>
            <span class="divider">|</span>
            <router-link to="/complaints/anonymous" class="link">
              <el-icon><Message /></el-icon>
              Submit Anonymously
            </router-link>
          </div>
        </div>
      </div>
      
      <div class="welcome-section">
        <h2>Welcome Back!</h2>
        <p>Manage your grievances efficiently with our smart platform (Developed By Sufyan)</p>
        <div class="features">
          <div class="feature-item">
            <el-icon><Check /></el-icon>
            <span>Track your complaints</span>
          </div>
          <div class="feature-item">
            <el-icon><Check /></el-icon>
            <span>Real-time updates</span>
          </div>
          <div class="feature-item">
            <el-icon><Check /></el-icon>
            <span>Secure & reliable</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ElMessage } from 'element-plus'
import { DocumentChecked, UserFilled, Message, Check } from '@element-plus/icons-vue'

export default {
  name: 'Login',
  components: {
    DocumentChecked,
    UserFilled,
    Message,
    Check
  },
  data() {
    return {
      loginForm: {
        username: '',
        password: ''
      },
      loading: false,
      rules: {
        username: [
          { required: true, message: 'Please enter username', trigger: 'blur' }
        ],
        password: [
          { required: true, message: 'Please enter password', trigger: 'blur' },
          { min: 6, message: 'Password must be at least 6 characters', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    handleLogin() {
      this.$refs.loginForm.validate((valid) => {
        if (valid) {
          this.loading = true
          this.$store.dispatch('login', this.loginForm)
            .then(() => {
              ElMessage.success('Login successful!')
              const role = this.$store.getters.userRole
              if (role === 'ADMIN') {
                this.$router.push('/admin/complaints')
              } else if (role === 'OFFICER') {
                this.$router.push('/officer')
              } else {
                this.$router.push('/dashboard')
              }
            })
            .catch(error => {
              ElMessage.error(error.response?.data?.message || 'Login failed')
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
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  z-index: 0;
}

.background-shapes {
  position: relative;
  width: 100%;
  height: 100%;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 20s infinite ease-in-out;
}

.shape-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.shape-2 {
  width: 200px;
  height: 200px;
  bottom: -50px;
  right: -50px;
  animation-delay: 5s;
}

.shape-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  right: 10%;
  animation-delay: 10s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
  }
  33% {
    transform: translate(30px, -30px) rotate(120deg);
  }
  66% {
    transform: translate(-20px, 20px) rotate(240deg);
  }
}

.login-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  max-width: 1000px;
  width: 90%;
  z-index: 1;
  position: relative;
}

.login-card {
  background: rgba(255, 255, 255, 0.98);
  border-radius: 24px;
  padding: 48px 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
}

.card-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.logo-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
}

.logo-container h1 {
  margin: 0;
  font-size: 36px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.tagline {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
  font-weight: 500;
}

.login-form {
  margin-top: 32px;
}

.input-field {
  margin-bottom: 20px;
}

.input-field :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
}

.input-field :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.login-button {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  transition: all 0.3s;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
}

.login-footer {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #e5e7eb;
}

.footer-links {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
}

.link {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #667eea;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.link:hover {
  color: #764ba2;
  transform: translateY(-1px);
}

.divider {
  color: #d1d5db;
}

.welcome-section {
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: white;
  padding: 40px;
}

.welcome-section h2 {
  font-size: 42px;
  font-weight: 700;
  margin: 0 0 16px 0;
  line-height: 1.2;
}

.welcome-section > p {
  font-size: 18px;
  margin: 0 0 40px 0;
  opacity: 0.9;
  line-height: 1.6;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
}

.feature-item .el-icon {
  width: 24px;
  height: 24px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  padding: 4px;
}

@media (max-width: 768px) {
  .login-content {
    grid-template-columns: 1fr;
    gap: 40px;
  }
  
  .welcome-section {
    text-align: center;
    padding: 20px;
  }
  
  .welcome-section h2 {
    font-size: 32px;
  }
}
</style>
