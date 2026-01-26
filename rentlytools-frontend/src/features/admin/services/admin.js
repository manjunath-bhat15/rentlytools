// src/api/admin.js
import api from "@/services/axios";


export const getPendingOwners = () => api.get("/admin/owner/pending");
export const getOwnerById = (id) => api.get(`/admin/owner/${id}`);
export const approveOwner = (id, adminId = 1) =>
  api.post(`/admin/owner/approve/${id}`, null, { params: { adminId }});
export const rejectOwner = (id, adminId = 1) =>
  api.post(`/admin/owner/reject/${id}`, null, { params: { adminId }});
