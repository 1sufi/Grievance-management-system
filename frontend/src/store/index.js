import { createStore } from 'vuex'
import axios from 'axios'

const API_URL = 'http://localhost:8080/api'

const savedToken = localStorage.getItem('token')
const savedUser = localStorage.getItem('user')

if (savedToken) {
  axios.defaults.headers.common['Authorization'] = `Bearer ${savedToken}`
}

const store = createStore({
  state: {
    user: savedUser ? JSON.parse(savedUser) : null,
    token: savedToken,
    status: ''
  },
  
  getters: {
    isAuthenticated: state => !!state.token,
    userRole: state => state.user ? state.user.role : null,
    userName: state => state.user ? state.user.username : null
  },
  
  mutations: {
    auth_request(state) {
      state.status = 'loading'
    },
    auth_success(state, { token, user }) {
      state.status = 'success'
      state.token = token
      state.user = user
    },
    auth_error(state) {
      state.status = 'error'
    },
    logout(state) {
      state.status = ''
      state.token = ''
      state.user = null
    }
  },
  
  actions: {
    login({ commit }, user) {
      return new Promise((resolve, reject) => {
        commit('auth_request')
        axios.post(`${API_URL}/auth/signin`, user)
          .then(response => {
            const token = response.data.token
            const userData = {
              id: response.data.id,
              username: response.data.username,
              email: response.data.email,
              role: response.data.role
            }
            localStorage.setItem('token', token)
            localStorage.setItem('user', JSON.stringify(userData))
            axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
            commit('auth_success', { token, user: userData })
            resolve(response)
          })
          .catch(err => {
            commit('auth_error')
            localStorage.removeItem('token')
            reject(err)
          })
      })
    },
    
    register({ commit }, user) {
      return new Promise((resolve, reject) => {
        commit('auth_request')
        axios.post(`${API_URL}/auth/signup`, user)
          .then(response => {
            resolve(response)
          })
          .catch(err => {
            commit('auth_error')
            reject(err)
          })
      })
    },
    
    logout({ commit }) {
      return new Promise((resolve) => {
        commit('logout')
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        delete axios.defaults.headers.common['Authorization']
        resolve()
      })
    }
  }
})

export default store
