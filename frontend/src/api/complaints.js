import http from './http'

export function createComplaint(data) {
  return http.post('/complaints', data)
}

export function createAnonymousComplaint(data) {
  return http.post('/complaints/anonymous', data)
}

export function listMyComplaints() {
  return http.get('/complaints')
}

export function getComplaint(id) {
  return http.get(`/complaints/${id}`)
}

export function updateComplaint(id, data) {
  return http.put(`/complaints/${id}`, data)
}

export function withdrawComplaint(id) {
  return http.delete(`/complaints/${id}`)
}

export function rateComplaint(id, data) {
  return http.post(`/complaints/${id}/rating`, data)
}

export function adminListComplaints(params) {
  return http.get('/admin/complaints', { params })
}

export function adminGetComplaint(id) {
  return http.get(`/admin/complaints/${id}`)
}

export function adminUpdateComplaint(id, data) {
  return http.put(`/admin/complaints/${id}`, data)
}

export function adminAddInternalNote(id, data) {
  return http.post(`/admin/complaints/${id}/internal-note`, data)
}

// Officer endpoints
export function officerListComplaints() {
  return http.get('/officer/complaints')
}

export function officerGetComplaint(id) {
  return http.get(`/officer/complaints/${id}`)
}

export function officerUpdateComplaint(id, data) {
  return http.put(`/officer/complaints/${id}`, data)
}
