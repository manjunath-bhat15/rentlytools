// src/shared/layout/TopBar.jsx
export default function TopBar() {
  const user = localStorage.getItem("user_name");

  return (
    <header className="h-14 px-6 flex items-center justify-between border-b bg-white">
      <div className="font-semibold">RentlyTools</div>

      <div className="flex items-center gap-4">
        <span className="text-sm text-gray-600">
          👤 {user || "User"}
        </span>
        <button className="text-sm">💰 Wallet</button>
        <button className="text-sm font-medium text-blue-600">
          Become lender
        </button>
      </div>
    </header>
  );
}
