export default function Navbar() {
  return (
    <header className="h-14 bg-white shadow flex items-center justify-between px-6 border-b">
      <h1 className="text-lg font-semibold text-gray-800">Dashboard</h1>
      <div className="flex items-center gap-4">
        <span className="text-sm text-gray-600">Welcome, Manjunath 👋</span>
        <img
          src="https://api.dicebear.com/9.x/adventurer/svg?seed=manju"
          alt="Profile"
          className="w-8 h-8 rounded-full border"
        />
      </div>
    </header>
  );
}