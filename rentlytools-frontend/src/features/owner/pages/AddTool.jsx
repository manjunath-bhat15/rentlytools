import React, { useState } from 'react';
import { useNavigate } from "react-router-dom";
import { ownerService } from '../services/ownerService';

export default function AddTool() {
  const navigate = useNavigate();
  const ownerId = localStorage.getItem("user_id");

  // src/features/owner/pages/AddTool.jsx

const [formData, setFormData] = useState({
  ownerId: Number(localStorage.getItem("user_id")) || 56, // Force a number
  categoryId: 1, 
  title: '',
  description: '',
  lat: 12.97,
  lng: 77.59,
  pricePerDay: 0.0, // Match Double
  depositAmount: 0.0,
  photos: [] 
});

  const handleLogout = () => { localStorage.clear(); navigate("/login"); };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await ownerService.createListing(formData);
      if (response.error) {
        alert("Error: " + response.error);
      } else {
        alert("Success: " + response.message);
        navigate("/owner/tools");
      }
    } catch (err) {
      alert("Submission failed. Ensure backend is running on port 8080.");
    }
  };

  return (
    <div style={{ padding: '20px', fontFamily: 'sans-serif', maxWidth: '500px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '20px' }}>
        <h1>Add New Tool</h1>
        <button onClick={handleLogout} style={{ color: 'red', cursor: 'pointer' }}>Logout</button>
      </div>

      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
        <input type="text" placeholder="Title" required 
          onChange={e => setFormData({...formData, title: e.target.value})} />
        
        <textarea placeholder="Description" required 
          onChange={e => setFormData({...formData, description: e.target.value})} />
        
        <input type="number" placeholder="Price Per Day (₹)" required 
          onChange={e => setFormData({...formData, pricePerDay: parseFloat(e.target.value)})} />
        
        <input type="number" placeholder="Security Deposit (₹)" required 
          onChange={e => setFormData({...formData, depositAmount: parseFloat(e.target.value)})} />

        <button type="submit" style={{ padding: '12px', background: '#2ecc71', color: 'white', border: 'none' }}>
          Submit for Approval
        </button>
      </form>
    </div>
  );
}