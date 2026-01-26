// src/shared/layout/DashboardLayout.jsx
import TopBar from "./TopBar";
import Footer from "./Footer";

export default function DashboardLayout({ sidebar, children }) {
  return (
    <div className="min-h-screen flex flex-col">
      <TopBar />

      <div className="flex flex-1">
        {sidebar}
        <main className="flex-1 p-6 bg-gray-50">
          {children}
        </main>
      </div>

      <Footer />
    </div>
  );
}
