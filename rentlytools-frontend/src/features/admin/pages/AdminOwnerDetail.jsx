// src/pages/AdminOwnerDetail.jsx
import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  getOwnerById,
  approveOwner,
  rejectOwner,
} from "@/features/admin/services/admin";

export default function AdminOwnerDetail() {
  const { id } = useParams();
  const nav = useNavigate();
  const [data, setData] = useState(null);

  useEffect(() => {
    getOwnerById(id).then(res => setData(res.data)).catch(err => {
      console.error(err); alert("Failed to load owner");
    });
  }, [id]);

  if (!data) return <div className="p-6">Loading...</div>;

  const doApprove = async () => {
    try {
      await approveOwner(id, 1); // pass actual adminId if available
      alert("Approved");
      nav("/dashboard"); // or /admin/owners
    } catch (err) { console.error(err); alert("Approve failed"); }
  };

  const doReject = async () => {
    try {
      await rejectOwner(id, 1);
      alert("Rejected");
      nav("/dashboard");
    } catch (err) { console.error(err); alert("Reject failed"); }
  };

  return (
    <div className="p-6 max-w-4xl mx-auto">
      <h1 className="text-2xl font-bold mb-4">Review Owner #{id}</h1>

      <div className="grid md:grid-cols-2 gap-4">
        <img src={data.selfieImageUrl || data.selfieUrl} alt="selfie" className="w-full h-72 object-cover rounded" />
        <img src={data.aadhaarImageUrl || data.aadhaarUrl} alt="aadhaar" className="w-full h-72 object-cover rounded" />
      </div>

      <div className="mt-4 grid md:grid-cols-3 gap-3">
        <Info label="Name" value={data.name} />
        <Info label="DOB" value={data.dob} />
        <Info label="Gender" value={data.gender} />
        <Info label="Aadhaar" value={data.aadhaarNumber} />
        <Info label="Score" value={(data.faceScore ?? data.score ?? 0).toFixed(3)} />
      </div>

      <div className="mt-4">
        <h3 className="font-semibold">OCR Text</h3>
        <div className="bg-gray-100 p-3 rounded max-h-48 overflow-y-auto">{data.ocrText}</div>
      </div>

      <div className="flex gap-3 mt-4">
        <button onClick={doApprove} className="px-4 py-2 bg-green-600 text-white rounded">Approve</button>
        <button onClick={doReject} className="px-4 py-2 bg-red-600 text-white rounded">Reject</button>
      </div>
    </div>
  );
}

function Info({ label, value }) {
  return (
    <div className="bg-white p-3 rounded shadow">
      <div className="text-sm text-gray-500">{label}</div>
      <div className="font-medium">{value ?? "-"}</div>
    </div>
  );
}
