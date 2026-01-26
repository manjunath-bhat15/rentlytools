import { useEffect, useState } from "react";
import api from "@/services/axios";
import { motion } from "framer-motion";
import { toast } from "react-toastify";

export default function OwnerBookings() {
  const ownerId = localStorage.getItem("user_id");

  const [tab, setTab] = useState("pending");
  const [lists, setLists] = useState({ pending: [], active: [], completed: [] });
  const [loading, setLoading] = useState(true);
  const [doing, setDoing] = useState(null);

  const loadData = async () => {
    if (!ownerId) {
        console.error("DEBUG: No ownerId found in localStorage!");
        return;
    }
    try {
      console.log(`DEBUG: Refreshing bookings for Owner: ${ownerId}`);
      const [pRes, aRes, cRes] = await Promise.all([
        api.get(`/bookings/owner/pending?ownerId=${ownerId}`),
        api.get(`/bookings/owner/active?ownerId=${ownerId}`),
        api.get(`/bookings/owner/completed?ownerId=${ownerId}`)
      ]);

      console.log("DEBUG - Pending:", pRes.data);
      console.log("DEBUG - Active:", aRes.data);
      console.log("DEBUG - Completed:", cRes.data);

      setLists({
        pending: pRes.data || [],
        active: aRes.data || [],
        completed: cRes.data || []
      });
    } catch (err) {
      console.error("DEBUG: Fetch Error:", err);
      toast.error("Could not sync bookings");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
    const interval = setInterval(loadData, 10000); 
    return () => clearInterval(interval);
  }, [ownerId]);

  const act = async (bookingId, action) => {
    console.log(`DEBUG: Starting action [${action}] for Booking ID: ${bookingId}`);
    setDoing(bookingId);
    
    try {
      let endpoint = `/bookings/owner/decide`;
      let params = { ownerId, bookingId, action };

      if (action === "COMPLETE") {
        endpoint = `/bookings/complete`;
        params = { bookingId: bookingId }; // Backend expects @RequestParam Long bookingId
        console.log("DEBUG: Routing to dedicated complete endpoint", endpoint, params);
      }

      const res = await api.post(endpoint, null, { params });
      console.log("DEBUG: Response from server:", res.data);
      
      if (res.data.error) {
        console.warn("DEBUG: Backend returned error logic:", res.data.error);
        toast.error(res.data.error);
      } else {
        toast.success(res.data.message || "Booking Completed & Owner Paid");
        await loadData(); 
      }
    } catch (err) {
      console.error("DEBUG: Axios Catch Error:", err);
      const msg = err.response?.data?.error || err.response?.data?.message || "Transaction failed";
      toast.error(msg);
    } finally {
      setDoing(null);
    }
  };

  const format = (d) => d ? new Date(d).toLocaleString("en-IN", { dateStyle: "medium", timeStyle: "short" }) : "N/A";

  if (loading) return <div className="p-10 text-center animate-pulse text-gray-400">Syncing with server...</div>;

  return (
    <div className="p-8 max-w-5xl mx-auto min-h-screen">
      <h1 className="text-3xl font-bold mb-8 flex items-center gap-3">📩 Rental Requests</h1>

      {/* Navigation Tabs */}
      <div className="flex bg-gray-100 p-1.5 rounded-2xl w-fit mb-8">
        {["pending", "active", "completed"].map((t) => (
          <button
            key={t}
            onClick={() => {
                console.log(`DEBUG: Switched to tab: ${t}`);
                setTab(t);
            }}
            className={`px-6 py-2.5 rounded-xl font-bold capitalize transition-all ${
              tab === t ? "bg-white text-blue-600 shadow-sm" : "text-gray-500 hover:bg-gray-200"
            }`}
          >
            {t} ({lists[t].length})
          </button>
        ))}
      </div>

      {/* Booking Cards Block */}
      <div className="space-y-4">
        {lists[tab].length === 0 ? (
          <div className="text-center py-20 bg-gray-50 border-2 border-dashed rounded-3xl text-gray-400">
            No {tab} bookings at the moment.
          </div>
        ) : (
          lists[tab].map((b) => (
            <motion.div layout initial={{ opacity: 0 }} animate={{ opacity: 1 }} key={b.id} className="bg-white border rounded-3xl p-6 flex flex-col md:flex-row justify-between items-center gap-6">
              <div className="flex-1">
                <div className="flex items-center gap-3 mb-3">
                  <span className={`text-[10px] font-bold px-2 py-1 rounded uppercase ${
                    b.status === 'ACC' || b.status === 'ACTIVE' ? 'bg-green-100 text-green-700' : 'bg-blue-100 text-blue-700'
                  }`}>
                    {b.status}
                  </span>
                  <span className="text-gray-400 text-xs font-mono">Booking #{b.id}</span>
                </div>
                <h3 className="text-lg font-bold text-gray-800">Tool ID: {b.listingId}</h3>
                <p className="text-sm text-gray-500 mb-4">Renter ID: {b.renterId}</p>
                
                <div className="flex gap-8 border-t pt-4">
                  <div>
                    <p className="text-[10px] uppercase text-gray-400 font-bold">Pick Up</p>
                    <p className="text-sm font-medium">{format(b.startAt)}</p>
                  </div>
                  <div>
                    <p className="text-[10px] uppercase text-gray-400 font-bold">Return</p>
                    <p className="text-sm font-medium">{format(b.endAt)}</p>
                  </div>
                </div>
              </div>

              <div className="flex flex-row md:flex-col gap-2 mt-4 md:mt-0 w-full md:w-auto">
                {tab === "pending" && (
                  <>
                    <button 
                      onClick={() => act(b.id, "APPROVE")} 
                      disabled={doing === b.id} 
                      className="flex-1 bg-green-600 text-white px-6 py-2.5 rounded-xl font-bold hover:bg-green-700 disabled:opacity-50"
                    >
                      {doing === b.id ? "..." : "Approve"}
                    </button>
                    <button 
                      onClick={() => act(b.id, "REJECT")} 
                      disabled={doing === b.id} 
                      className="flex-1 bg-white border border-red-100 text-red-600 px-6 py-2.5 rounded-xl font-bold hover:bg-red-50"
                    >
                      Reject
                    </button>
                  </>
                )}

                {tab === "active" && (
                  <div className="flex flex-col gap-2 w-full">
                    <span className="text-[10px] text-center text-amber-600 font-bold bg-amber-50 py-1 rounded-lg">
                      Currently Rented
                    </span>
                    <button 
                      onClick={() => {
                        console.log("DEBUG: User clicked Finish for Booking:", b.id);
                        act(b.id, "COMPLETE");
                      }} 
                      disabled={doing === b.id}
                      className="bg-purple-600 text-white px-6 py-2.5 rounded-xl font-bold hover:bg-purple-700 transition-all shadow-lg shadow-purple-100"
                    >
                      {doing === b.id ? "Processing..." : "Finish & Release"}
                    </button>
                  </div>
                )}

                {tab === "completed" && (
                  <span className="text-gray-400 text-sm font-medium italic">Archived / Paid</span>
                )}
              </div>
            </motion.div>
          ))
        )}
      </div>
    </div>
  );
}