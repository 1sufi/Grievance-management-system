import http from './http'

// List officers for admin. Optionally filter by level (e.g. 'L1' or 'L2').
export function adminListOfficers(params) {
  return http.get('/admin/officers', { params })
}
