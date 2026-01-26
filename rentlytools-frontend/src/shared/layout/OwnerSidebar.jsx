import { motion } from "framer-motion";
import { useEffect, useState } from "react";
import api from "@/services/axios";
import {
  Home,
  Package,
  ClipboardList,
  Wallet,
  MessageSquare,
  LogOut,
} from "lucide-react";

export default function OwnerSidebar() {
  const [ownerName, setOwnerName] = useState("");
  const [pendingCount, setPendingCount] = useState(0);

  useEffect(() => {
    const fetchData = async () => {
      const userId = localStorage.getItem("user_id");
      if (!userId) return;

      try {
        // 1. Fetch Owner Details for the Greeting
        const res = await api.get(`/users/details?id=${userId}`);
        if (res.data && res.data.name) {
          setOwnerName(res.data.name);
        }

        // 2. Fetch Pending Requests count for the notification badge
        const bookingRes = await api.get(`/bookings/owner/pending?ownerId=${userId}`);
        if (Array.isArray(bookingRes.data)) {
          setPendingCount(bookingRes.data.length);
        }
      } catch (err) {
        console.error("Sidebar Data Fetch Error:", err);
      }
    };

    fetchData();
    // Refresh the pending count every 30 seconds
    const interval = setInterval(fetchData, 30000);
    return () => clearInterval(interval);
  }, []);

  const menuItems = [
    { icon: <Home size={20} />, label: "Overview", path: "/owner-dashboard" },
    { icon: <Package size={20} />, label: "My Tools", path: "/owner/tools" },
    { 
      icon: <ClipboardList size={20} />, 
      label: "Rental Requests", 
      path: "/owner/bookings",
      badge: pendingCount 
    },
    { icon: <Wallet size={20} />, label: "Owner Wallet", path: "/owner/wallet" },
    { icon: <MessageSquare size={20} />, label: "Owner Chat", path: "/owner/chat" },
  ];

  const handleNavigation = (path) => {
    window.location.href = path;
  };

  const handleLogout = () => {
    localStorage.clear();
    window.location.href = "/auth";
  };

  return (
    <div className="w-64 bg-[#0f172a] text-white min-h-screen flex flex-col shadow-xl border-r border-white/5">
      {/* Brand Section */}
      <motion.div
        initial={{ opacity: 0, y: -10 }}
        animate={{ opacity: 1, y: 0 }}
        className="px-6 py-8 border-b border-white/10"
      >
        <h1 className="text-3xl font-extrabold bg-gradient-to-r from-pink-500 to-purple-500 bg-clip-text text-transparent">
          RentlyTools
        </h1>
        <p className="text-xs mt-1 text-white/40 tracking-widest uppercase">Owner Portal</p>

        <div className="mt-4 flex items-center gap-2 bg-white/5 p-3 rounded-xl border border-white/10">
          <div className="w-8 h-8 rounded-full bg-gradient-to-br from-pink-500 to-purple-600 flex items-center justify-center text-xs font-bold">
            {ownerName ? ownerName[0].toUpperCase() : "O"}
          </div>
          <div className="overflow-hidden">
            <p className="text-[10px] text-white/40 uppercase font-bold">Welcome back</p>
            <p className="text-sm text-pink-400 font-semibold truncate">
              {ownerName || "Verifying..."}
            </p>
          </div>
        </div>
      </motion.div>

      {/* Menu Items */}
      <nav className="flex-1 mt-6 px-4 space-y-1">
        {menuItems.map((item, idx) => (
          <motion.div
            key={idx}
            whileHover={{ x: 5 }}
            whileTap={{ scale: 0.98 }}
            onClick={() => handleNavigation(item.path)}
            className="group px-4 py-3.5 flex items-center justify-between cursor-pointer rounded-xl hover:bg-white/5 transition-all"
          >
            <div className="flex items-center gap-3">
              <div className="text-white/40 group-hover:text-pink-400 transition-colors">
                {item.icon}
              </div>
              <span className="text-white/70 group-hover:text-white font-medium text-sm">
                {item.label}
              </span>
            </div>
            
            {item.badge > 0 && (
              <span className="bg-pink-600 text-white text-[10px] px-2 py-0.5 rounded-full font-bold shadow-lg shadow-pink-900/20">
                {item.badge}
              </span>
            )}
          </motion.div>
        ))}
      </nav>

      {/* Footer / Logout */}
      <div className="p-4 border-t border-white/10">
        <button
          onClick={handleLogout}
          className="flex items-center justify-center gap-3 w-full py-3 rounded-xl bg-white/5 hover:bg-red-500/10 hover:text-red-400 border border-white/5 hover:border-red-500/20 transition-all group"
        >
          <LogOut size={18} className="text-white/40 group-hover:text-red-400" />
          <span className="text-sm font-semibold">Logout</span>
        </button>
        <p className="text-center mt-4 text-[10px] text-white/20 tracking-tighter">
          &copy; 2026 RENTLYTOOLS INTERACTIVE
        </p>
      </div>
    </div>
  );
}