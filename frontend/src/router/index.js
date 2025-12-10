import { createRouter, createWebHistory } from 'vue-router'
import store from '../store'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresGuest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { requiresGuest: true }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/complaints',
    name: 'Complaints',
    component: () => import('../views/Complaints.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/complaints/new',
    name: 'NewComplaint',
    component: () => import('../views/NewComplaint.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/complaints/anonymous',
    name: 'AnonymousComplaint',
    component: () => import('../views/AnonymousComplaint.vue')
  },
  {
    path: '/complaints/:id',
    name: 'ComplaintDetails',
    component: () => import('../views/ComplaintDetails.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    name: 'AdminPanel',
    component: () => import('../views/AdminPanel.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/complaints',
    name: 'AdminComplaints',
    component: () => import('../views/AdminComplaints.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/analytics',
    name: 'AdminAnalytics',
    component: () => import('../views/AdminAnalytics.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/officers',
    name: 'OfficerManagement',
    component: () => import('../views/OfficerManagement.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/officer',
    name: 'OfficerPanel',
    component: () => import('../views/OfficerPanel.vue'),
    meta: { requiresAuth: true, requiresOfficer: true }
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  const isAuthenticated = store.getters.isAuthenticated
  const userRole = store.getters.userRole
  
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
  } else if (to.meta.requiresGuest && isAuthenticated) {
    // Redirect authenticated users away from auth pages based on role
    if (userRole === 'ADMIN') {
      next('/admin/complaints')
    } else if (userRole === 'OFFICER') {
      next('/officer')
    } else {
      next('/dashboard')
    }
  } else if (to.path === '/dashboard' && userRole === 'OFFICER') {
    // Officers should use their own dashboard, not the generic one
    next('/officer')
  } else if (to.meta.requiresAdmin && userRole !== 'ADMIN') {
    next('/dashboard')
  } else if (to.meta.requiresOfficer && !['ADMIN', 'OFFICER'].includes(userRole)) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
