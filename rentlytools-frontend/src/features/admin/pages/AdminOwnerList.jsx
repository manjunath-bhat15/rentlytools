// src/pages/AdminOwnerList.jsx
import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getPendingOwners } from "@/features/admin/services/admin";

export default function AdminOwnerList() {
  const [owners, setOwners] = useState([]);
  const [loading, setLoading] = useState(true);
  const nav = useNavigate();

  useEffect(() => {
    async function load() {
      try {
        const res = await getPendingOwners();
        setOwners(res.data || []);
      } catch (err) {
        console.error(err);
        alert("Failed to load pending owners");
      } finally {
        setLoading(false);
      }
    }
    load();
  }, []);

  if (loading) return <div className="p-6">Loading...</div>;
  if (!owners.length)
    return <div className="p-6">No pending verifications at the moment.</div>;

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-4">Pending Owner Verifications</h1>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {owners.map((o) => (
          <div key={o.id} className="bg-white p-3 rounded shadow">
            <img
              src={o.selfieImageUrl || o.selfieUrl}
              alt="selfie"
              className="h-40 w-full object-cover rounded mb-2"
            />
            <div className="text-sm text-gray-600">{o.name || "-"}</div>
            <div className="font-semibold">{o.aadhaarNumber || "-"}</div>
            <div className="mt-2">Score: <span className="font-bold">{(o.faceScore ?? o.score ?? 0).toFixed(3)}</span></div>

            <div className="flex gap-2 mt-3">
              <button
                onClick={() => nav(`/admin/owners/${o.id}`)}
                className="flex-1 text-center bg-blue-600 text-white py-2 rounded"
              >
                Review
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
