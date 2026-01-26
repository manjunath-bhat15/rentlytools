import React, { useState, useEffect } from 'react';
import api from "@/services/axios";
import { toast } from "react-toastify";

export default function UserDashboard() {
  const [tools, setTools] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedTool, setSelectedTool] = useState(null);
  
  // State for start and end times
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");

  useEffect(() => {
    const fetchActiveTools = async () => {
      try {
        const res = await api.get("/listings/all");
        // We only want tools approved by admin
        setTools(res.data.filter(tool => tool.status === 'ACTIVE'));
      } catch (err) {
        toast.error("Failed to load tools");
      } finally {
        setLoading(false);
      }
    };
    fetchActiveTools();
  }, []);

  const handleBooking = async () => {
    if (!startDate || !endDate) return toast.error("Please select both dates");

    try {
      // Backend expects: yyyy-MM-dd HH:mm
      // input type="datetime-local" gives: yyyy-MM-ddTHH:mm
      // We need to replace the 'T' with a space
      const formattedStart = startDate.replace("T", " ");
      const formattedEnd = endDate.replace("T", " ");

      const payload = {
        listingId: selectedTool.id,
        renterId: parseInt(localStorage.getItem("user_id")),
        startAt: formattedStart,
        endAt: formattedEnd
      };

      // ✅ Corrected Endpoint: /request
      const res = await api.post("/bookings/request", payload);
      
      if (res.data.error) {
        toast.error(res.data.error);
      } else {
        toast.success("Booking request submitted! (ID: " + res.data.bookingId + ")");
        setSelectedTool(null);
      }
    } catch (err) {
      toast.error("Booking failed. Check console for details.");
      console.error(err);
    }
  };

  if (loading) return <div className="p-10 text-center text-gray-500">Scanning for tools...</div>;

  return (
    <div className="p-6 bg-gray-50 min-h-screen">
      <h1 className="text-2xl font-bold mb-6">Rent a Tool</h1>
      
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {tools.map(tool => (
          <div key={tool.id} className="bg-white p-5 rounded-2xl shadow-sm border border-gray-100">
            <h2 className="text-lg font-bold text-gray-800">{tool.title}</h2>
            <p className="text-gray-500 text-sm my-2 h-10 overflow-hidden">{tool.description}</p>
            <div className="flex justify-between items-center mt-4">
              <span className="text-xl font-bold text-blue-600">₹{tool.pricePerDay}<small className="text-gray-400 font-normal">/day</small></span>
              <button 
                onClick={() => setSelectedTool(tool)}
                className="bg-blue-600 text-white px-4 py-2 rounded-xl hover:bg-blue-700 transition-colors"
              >
                Book
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* BOOKING MODAL */}
      {selectedTool && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className="bg-white p-8 rounded-3xl shadow-2xl max-w-md w-full">
            <h2 className="text-xl font-bold mb-1">Rent {selectedTool.title}</h2>
            <p className="text-sm text-gray-500 mb-6">Owner ID: {selectedTool.ownerId}</p>
            
            <div className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-gray-400 uppercase mb-1">Start Time</label>
                <input 
                  type="datetime-local" 
                  className="w-full border-gray-200 border p-3 rounded-xl focus:ring-2 focus:ring-blue-500 outline-none"
                  onChange={(e) => setStartDate(e.target.value)}
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-400 uppercase mb-1">End Time</label>
                <input 
                  type="datetime-local" 
                  className="w-full border-gray-200 border p-3 rounded-xl focus:ring-2 focus:ring-blue-500 outline-none"
                  onChange={(e) => setEndDate(e.target.value)}
                />
              </div>
            </div>

            <div className="flex gap-3 mt-8">
              <button onClick={() => setSelectedTool(null)} className="flex-1 py-3 text-gray-600 font-semibold hover:bg-gray-100 rounded-xl">Cancel</button>
              <button onClick={handleBooking} className="flex-1 py-3 bg-blue-600 text-white font-semibold rounded-xl hover:bg-blue-700 shadow-lg shadow-blue-200">Request Tool</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}