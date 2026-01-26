import { useEffect, useState } from "react";
import api from "@/services/axios";
import { motion } from "framer-motion";
import { Clock, ArrowUpRight, ArrowDownLeft, Lock, Unlock, ReceiptText } from "lucide-react";

export default function TransactionHistory() {
  const userId = localStorage.getItem("user_id");
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchHistory = async () => {
    if (!userId) return;
    try {
      // Calls your existing WalletController endpoint
      const res = await api.get(`/wallet/transactions?userId=${userId}`);
      setHistory(res.data || []);
    } catch (err) {
      console.error("DEBUG: Transaction Fetch Failed", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHistory();
    // Auto-refresh history every 20 seconds to catch new payments
    const interval = setInterval(fetchHistory, 20000);
    return () => clearInterval(interval);
  }, [userId]);

  const getIcon = (type) => {
    switch (type) {
      case "HOLD": return <Lock className="text-amber-500" size={18} />;
      case "RELEASE": return <Unlock className="text-blue-500" size={18} />;
      case "DEBIT": return <ArrowUpRight className="text-red-500" size={18} />;
      case "CREDIT": return <ArrowDownLeft className="text-green-500" size={18} />;
      default: return <ReceiptText className="text-gray-400" size={18} />;
    }
  };

  if (loading) return <div className="p-8 text-center text-gray-400 animate-pulse">Loading wallet activity...</div>;

  return (
    <div className="mt-8 bg-white rounded-3xl border border-gray-100 p-6 shadow-sm">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-xl font-bold flex items-center gap-2">
          🕒 Wallet Activity
        </h2>
        <button onClick={fetchHistory} className="text-xs text-blue-600 font-semibold hover:underline">
          Refresh
        </button>
      </div>

      <div className="space-y-3">
        {history.length === 0 ? (
          <div className="text-center py-10 bg-gray-50 rounded-2xl border-2 border-dashed border-gray-100">
            <p className="text-gray-400 text-sm italic">No transactions recorded yet.</p>
          </div>
        ) : (
          history.map((txn) => (
            <motion.div 
              initial={{ opacity: 0, y: 5 }} 
              animate={{ opacity: 1, y: 0 }}
              key={txn.id} 
              className="flex justify-between items-center p-4 rounded-2xl bg-white border border-gray-100 hover:shadow-md hover:border-blue-100 transition-all"
            >
              <div className="flex items-center gap-4">
                <div className="p-3 bg-gray-50 rounded-xl">
                  {getIcon(txn.type)}
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <p className="font-bold text-gray-800 text-sm uppercase tracking-tight">{txn.type}</p>
                    <span className="text-[10px] bg-gray-100 text-gray-500 px-1.5 py-0.5 rounded font-mono">
                      BK-{txn.bookingId}
                    </span>
                  </div>
                  <p className="text-[10px] text-gray-400 mt-0.5">
                    {new Date(txn.createdAt).toLocaleString("en-IN", { 
                        dateStyle: 'medium', 
                        timeStyle: 'short' 
                    })}
                  </p>
                </div>
              </div>
              <div className="text-right">
                <p className={`font-bold text-base ${['DEBIT', 'HOLD'].includes(txn.type) ? 'text-red-600' : 'text-green-600'}`}>
                  {['DEBIT', 'HOLD'].includes(txn.type) ? '-' : '+'}₹{txn.amount.toFixed(2)}
                </p>
                <p className="text-[9px] text-gray-300 font-bold uppercase tracking-widest">Confirmed</p>
              </div>
            </motion.div>
          ))
        )}
      </div>
    </div>
  );
}