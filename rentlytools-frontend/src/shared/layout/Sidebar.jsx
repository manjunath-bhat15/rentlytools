// src/shared/layout/Sidebar.jsx
import { useNavigate } from "react-router-dom";

export default function Sidebar() {
  const navigate = useNavigate();

  return (
    <aside className="w-60 border-r bg-white p-4">
      <nav className="flex flex-col gap-3">
        <button onClick={() => navigate("/dashboard")}>
          Dashboard
        </button>
        <button onClick={() => navigate("/bookings")}>
          Bookings
        </button>
        <button
          onClick={() => {
            localStorage.clear();
            navigate("/login");
          }}
          className="text-red-500"
        >
          Logout
        </button>
      </nav>
    </aside>
  );
}
