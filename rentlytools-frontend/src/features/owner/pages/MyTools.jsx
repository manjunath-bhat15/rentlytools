import React, { useState, useEffect } from 'react';
import { useNavigate } from "react-router-dom";
import api from "@/services/axios";
import { toast } from "react-toastify";

export default function MyTools() {
  const navigate = useNavigate();
  const [tools, setTools] = useState([]);
  const [loading, setLoading] = useState(true);
  
  // Use the verified ID from your successful login fix
  const ownerId = localStorage.getItem("user_id");

  const handleLogout = () => { 
    localStorage.clear(); 
    navigate("/login"); 
  };

  // 1. Fetch real tools using the correct query parameter pattern
  const fetchTools = async () => {
    if (!ownerId) return;
    try {
      // ✅ FIX: Hits your verified backend route /api/listings/owner?ownerId=56
      const response = await api.get(`/listings/owner?ownerId=${ownerId}`);
      setTools(response.data);
    } catch (error) {
      console.error("Error fetching tools:", error);
      toast.error("Failed to load inventory");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTools();
  }, [ownerId]);

  // 2. NEW: Handle removing a tool
const handleRemove = async (listingId) => {
  if (!window.confirm("Are you sure you want to remove this tool?")) return;

  try {
    // This calls http://localhost:8080/api/listings/{id}
    const res = await api.delete(`/listings/${listingId}`);
    
    toast.success("Tool removed successfully");
    
    // Immediately update local state so the tool disappears from the table
    setTools(prevTools => prevTools.filter(t => t.id !== listingId));
  } catch (error) {
    console.error("Error removing tool:", error);
    if (error.response?.status === 404) {
      toast.error("Backend error: Delete endpoint not found. Add @DeleteMapping to your Controller.");
    } else {
      toast.error("Failed to remove tool.");
    }
  }
};

  return (
    <div style={{ padding: '20px', fontFamily: 'sans-serif' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #ddd', paddingBottom: '10px' }}>
        <h1 style={{ fontSize: '24px', fontWeight: 'bold' }}>My Inventory</h1>
        <button onClick={handleLogout} style={{ color: '#e74c3c', border: 'none', background: 'none', cursor: 'pointer', fontWeight: 'bold' }}>
          Logout
        </button>
      </div>

      <div style={{ marginTop: '20px' }}>
        <button 
          onClick={() => navigate("/owner/add-tool")} 
          style={{ padding: '10px 20px', marginBottom: '20px', backgroundColor: '#2ecc71', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: '500' }}
        >
          + Add New Tool
        </button>

        {loading ? (
          <p>Loading your tools...</p>
        ) : tools.length > 0 ? (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
              <thead>
                <tr style={{ textAlign: 'left', background: '#f8f9fa' }}>
                  <th style={{ padding: '12px', borderBottom: '2px solid #dee2e6' }}>Tool Name</th>
                  <th style={{ padding: '12px', borderBottom: '2px solid #dee2e6' }}>Price/Day</th>
                  <th style={{ padding: '12px', borderBottom: '2px solid #dee2e6' }}>Status</th>
                  <th style={{ padding: '12px', borderBottom: '2px solid #dee2e6' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {tools.map((tool) => (
                  <tr key={tool.id} style={{ borderBottom: '1px solid #eee' }}>
                    <td style={{ padding: '12px' }}>{tool.title}</td>
                    {/* Ensure field name matches your Java Record (pricePerDay) */}
                    <td style={{ padding: '12px' }}>₹{tool.pricePerDay}</td>
                    <td style={{ padding: '12px' }}>
                      <span style={{ 
                        padding: '4px 8px', 
                        borderRadius: '4px', 
                        fontSize: '12px',
                        backgroundColor: tool.status === 'ACTIVE' ? '#d4edda' : '#fff3cd',
                        color: tool.status === 'ACTIVE' ? '#155724' : '#856404'
                      }}>
                        {tool.status}
                      </span>
                    </td>
                    <td style={{ padding: '12px' }}>
                      <button 
                        onClick={() => handleRemove(tool.id)}
                        style={{ padding: '6px 12px', backgroundColor: '#ff4d4d', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '13px' }}
                      >
                        Remove
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div style={{ border: '1px solid #eee', padding: '40px', textAlign: 'center', color: '#666', borderRadius: '8px' }}>
            No tools listed yet. Click "+ Add New Tool" to get started.
          </div>
        )}
      </div>
    </div>
  );
}