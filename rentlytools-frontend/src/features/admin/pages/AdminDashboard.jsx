import React, { useEffect, useState } from "react";
import api from "@/services/axios";
import { toast } from "react-toastify";

export default function AdminDashboard() {
  const [loading, setLoading] = useState(true);
  const [pendingOwners, setPendingOwners] = useState([]);
  const [pendingListings, setPendingListings] = useState([]);
  const [editedNames, setEditedNames] = useState({});

  // Using 'user_id' as verified in your previous successful login fix
  const adminId = localStorage.getItem("user_id"); 
  const isAdmin = localStorage.getItem("role_admin") === "true";

  useEffect(() => {
    if (!isAdmin) {
      toast.error("Access denied");
      window.location.href = "/";
    }
  }, [isAdmin]);

 // src/pages/AdminDashboard.jsx

const loadData = async () => {
  try {
    const [ownerRes, listingRes] = await Promise.all([
      api.get("/admin/owner/pending"),
      api.get("/listings/all") 
    ]);

    setPendingOwners(ownerRes.data);

    // ✅ FIX: Use toUpperCase() to ensure the filter catches "PENDING"
    const filteredListings = listingRes.data.filter(l => 
      l.status && l.status.toUpperCase() === "PENDING"
    );
    
    setPendingListings(filteredListings);
    
    // Debug: Check exactly what the backend is sending
    console.log("Total listings from DB:", listingRes.data.length);
    console.log("Pending listings found:", filteredListings.length);

  } catch (err) {
    toast.error("Failed to load dashboard data");
  } finally {
    setLoading(false);
  }
};

  useEffect(() => { loadData(); }, []);

  /* --- OWNER ACTIONS --- */
  const approveOwner = async (id) => {
    const finalName = editedNames[id];
    if (!finalName || finalName.trim().length < 3) return toast.error("Enter verified name");
    try {
      await api.post(`/admin/owner/approve/${id}`, { finalName }, { params: { adminId } });
      toast.success("Owner Approved");
      loadData();
    } catch (err) { toast.error("Owner approval failed"); }
  };

  /* --- LISTING ACTIONS (Aligned to your Controller) --- */
  const handleListingAction = async (action, listingId) => {
    try {
      // Matches @PostMapping("/approve") and @PostMapping("/reject")
      // Uses @RequestParam for adminId and listingId
      const res = await api.post(`/listings/admin/${action}`, null, {
        params: { adminId, listingId }
      });

      if (res.data.includes("NOT ADMIN")) {
        toast.error("Security Error: You are not authorized");
      } else {
        toast.success(res.data);
        loadData();
      }
    } catch (err) {
      toast.error(`${action} failed`);
    }
  };

  if (loading) return <div className="p-6 text-center text-gray-500">Loading...</div>;

  return (
    <div className="min-h-screen bg-gray-100 p-6 space-y-10">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold">Admin Control Panel</h1>
        <button onClick={() => { localStorage.clear(); window.location.href = "/"; }} className="px-4 py-2 bg-red-500 text-white rounded-md">Logout</button>
      </div>

      {/* OWNER VERIFICATION SECTION */}
      <div className="bg-white shadow rounded-lg p-6">
        <h2 className="text-xl font-semibold mb-4">Owner Identity Verification</h2>
        {/* Render your existing Owner Table here using pendingOwners */}
      </div>

      {/* TOOL LISTING SECTION */}
      <div className="bg-white shadow rounded-lg p-6">
        <h2 className="text-xl font-semibold mb-4">Tool Listings for Approval</h2>
        {pendingListings.length === 0 ? (
          <p className="text-center text-gray-500">All tools have been reviewed.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left border">
              <thead className="bg-gray-50">
                <tr>
                  <th className="p-3 border">Title</th>
                  <th className="p-3 border">Price</th>
                  <th className="p-3 border">Owner ID</th>
                  <th className="p-3 border">Actions</th>
                </tr>
              </thead>
              <tbody>
                {pendingListings.map((l) => (
                  <tr key={l.id} className="border-b">
                    <td className="p-3 border">{l.title}</td>
                    <td className="p-3 border">₹{l.pricePerDay}</td>
                    <td className="p-3 border">{l.ownerId}</td>
                    <td className="p-3 border flex gap-2">
                      <button onClick={() => handleListingAction('approve', l.id)} className="bg-green-600 text-white px-3 py-1 rounded">Approve</button>
                      <button onClick={() => handleListingAction('reject', l.id)} className="bg-red-600 text-white px-3 py-1 rounded">Reject</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}