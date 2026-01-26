import React, { useState, useEffect } from 'react';
import { useNavigate } from "react-router-dom";
import api from "@/services/axios";

export default function OwnerDashboard() {
  const navigate = useNavigate();
  // Added activeTools to the state
  const [stats, setStats] = useState({ totalTools: 0, pendingTools: 0, activeTools: 0 });
  const ownerId = localStorage.getItem("user_id");

  const handleLogout = () => { 
    localStorage.clear(); 
    window.location.href = "/"; // Ensure fresh redirect to login
  };

  useEffect(() => {
    const fetchStats = async () => {
      try {
        // Fetch all tools for this specific owner (e.g., ID 56)
        const res = await api.get(`/listings/owner?ownerId=${ownerId}`);
        const tools = res.data;
        
        setStats({
          totalTools: tools.length,
          // Updated to match your ListingAdminController's "ACTIVE" status
          activeTools: tools.filter(t => t.status === 'ACTIVE').length,
          pendingTools: tools.filter(t => t.status.toUpperCase() === 'PENDING').length
        });
      } catch (err) {
        console.error("Dashboard error:", err);
      }
    };
    if (ownerId) fetchStats();
  }, [ownerId]);

  return (
    <div style={{ padding: '20px', fontFamily: 'sans-serif' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #ddd', paddingBottom: '10px' }}>
        <h1 style={{ fontSize: '24px', fontWeight: 'bold' }}>Owner Overview</h1>
        <button onClick={handleLogout} style={{ color: '#ef4444', border: 'none', background: 'none', cursor: 'pointer', fontWeight: '500' }}>Logout</button>
      </div>

      <div style={{ display: 'flex', gap: '20px', marginTop: '20px' }}>
        {/* Total Listings Card */}
        <div style={{ border: '1px solid #e2e8f0', padding: '20px', borderRadius: '12px', flex: 1, backgroundColor: '#f8fafc' }}>
          <h3 style={{ color: '#64748b', margin: '0', fontSize: '14px', textTransform: 'uppercase' }}>Total Listings</h3>
          <p style={{ fontSize: '32px', fontWeight: 'bold', margin: '10px 0', color: '#1e293b' }}>{stats.totalTools}</p>
        </div>

        {/* NEW: Active Listings Card */}
        <div style={{ border: '1px solid #bbf7d0', padding: '20px', borderRadius: '12px', flex: 1, backgroundColor: '#f0fdf4' }}>
          <h3 style={{ color: '#15803d', margin: '0', fontSize: '14px', textTransform: 'uppercase' }}>Active Tools</h3>
          <p style={{ fontSize: '32px', fontWeight: 'bold', margin: '10px 0', color: '#166534' }}>{stats.activeTools}</p>
        </div>
        
        {/* Pending Approval Card */}
        <div style={{ border: '1px solid #fde68a', padding: '20px', borderRadius: '12px', flex: 1, backgroundColor: '#fffbeb' }}>
          <h3 style={{ color: '#b45309', margin: '0', fontSize: '14px', textTransform: 'uppercase' }}>Pending Approval</h3>
          <p style={{ fontSize: '32px', fontWeight: 'bold', margin: '10px 0', color: '#d97706' }}>{stats.pendingTools}</p>
        </div>
      </div>

      <div style={{ marginTop: '30px', display: 'flex', gap: '10px' }}>
        <button 
          onClick={() => navigate("/owner/tools")}
          style={{ padding: '12px 24px', backgroundColor: '#0f172a', color: 'white', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: '500' }}
        >
          Manage My Tools
        </button>
        <button 
          onClick={() => navigate("/owner/add-tool")}
          style={{ padding: '12px 24px', backgroundColor: '#3b82f6', color: 'white', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: '500' }}
        >
          + Add New Tool
        </button>
      </div>
    </div>
  );
}