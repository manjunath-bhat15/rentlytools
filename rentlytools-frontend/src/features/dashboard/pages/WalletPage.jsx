import TransactionHistory from "../components/TransactionHistory";

export default function WalletPage() {
  return (
    <div className="p-8 max-w-4xl mx-auto">
      {/* Your Balance Card UI */}
      <div className="bg-blue-600 text-white p-8 rounded-3xl shadow-lg mb-8">
         <p className="text-sm font-medium opacity-80 uppercase">Available Balance</p>
         <h1 className="text-4xl font-bold mt-2">₹10,000.00</h1>
      </div>

      {/* Drop the History Component here */}
      <TransactionHistory />
    </div>
  );
}