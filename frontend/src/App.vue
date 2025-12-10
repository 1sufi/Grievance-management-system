<template>
  <div id="app">
    <header v-if="isAuthenticated" class="app-header">
      <div class="brand">
        <span class="brand-mark">ResolveIT</span>
        <small class="brand-tagline">Smart Grievance Hub</small>
      </div>
      <nav class="main-nav">
        <router-link v-if="userRole !== 'ADMIN'" to="/dashboard" class="nav-link">Dashboard</router-link>
        <router-link v-if="userRole === 'ADMIN'" to="/admin/complaints" class="nav-link">Admin Panel</router-link>
        <router-link v-if="userRole === 'ADMIN'" to="/admin/analytics" class="nav-link">Analytics & Reporting</router-link>
        <router-link v-if="userRole === 'CITIZEN' || !userRole" to="/complaints" class="nav-link">My Complaints</router-link>
        <router-link v-if="userRole === 'OFFICER'" to="/officer" class="nav-link">Officer Panel</router-link>
      </nav>
      <div class="header-actions">
        <span class="welcome">Hi, {{ userName || 'User' }}</span>
        <el-button size="small" type="danger" @click="handleLogout">
          Logout
        </el-button>
      </div>
    </header>
    <main class="app-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const store = useStore()
const router = useRouter()

const isAuthenticated = computed(() => store.getters.isAuthenticated)
const userName = computed(() => store.getters.userName)
const userRole = computed(() => store.getters.userRole)

const handleLogout = async () => {
  await store.dispatch('logout')
  ElMessage.success('You have been logged out')
  router.push('/login')
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

body {
  margin: 0;
  padding: 0;
  background-color: #f5f5f5;
}

* {
  box-sizing: border-box;
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  background-color: #1f2937;
  color: #f9fafb;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}

.brand {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.brand-mark {
  font-weight: 700;
  letter-spacing: 0.5px;
}

.brand-tagline {
  font-size: 12px;
  color: rgba(249, 250, 251, 0.7);
}

.main-nav {
  display: flex;
  align-items: center;
  gap: 24px;
  flex: 1;
  justify-content: center;
}

.nav-link {
  color: rgba(249, 250, 251, 0.9);
  text-decoration: none;
  font-size: 14px;
  padding: 6px 12px;
  border-radius: 4px;
  transition: all 0.2s;
}

.nav-link:hover {
  background-color: rgba(255, 255, 255, 0.1);
  color: #fff;
}

.nav-link.router-link-active {
  background-color: rgba(255, 255, 255, 0.15);
  color: #fff;
  font-weight: 500;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.welcome {
  font-size: 14px;
}

.app-main {
  flex: 1;
}
</style>
