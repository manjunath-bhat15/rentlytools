import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";

// ===================== AUTH =====================
import Auth from "@/features/auth/pages/Auth";

// ===================== ADMIN =====================
import AdminDashboard from "@/features/admin/pages/AdminDashboard";
import AdminOwnerList from "@/features/admin/pages/AdminOwnerList";
import AdminOwnerDetail from "@/features/admin/pages/AdminOwnerDetail";

// ===================== OWNER & DASHBOARD =====================
// src/app/App.jsx

// ===================== OWNER & DASHBOARD =====================
// This path is now corrected to match your file location
import OwnerDashboard from "@/features/owner/pages/OwnerDashboard"; 

import MyTools from "@/features/owner/pages/MyTools";
import AddTool from "@/features/owner/pages/AddTool";
import WalletPage from "@/features/dashboard/pages/WalletPage";
import OwnerBookings from "@/features/owner/pages/OwnerBookings";
import OwnerOnboardingStart from "@/features/owner/pages/OwnerOnboardingStart";

// ===================== LAYOUT =====================
import DashboardLayout from "@/shared/layout/DashboardLayout";
import Sidebar from "@/shared/layout/Sidebar";
import OwnerSidebar from "@/shared/layout/OwnerSidebar";

// ===================== USER DASHBOARD =====================
import UserDashboard from "@/features/user/UserDashboard";

// ---------------------- Protected Wrapper ----------------------
function Protected({ children }) {
  const token = localStorage.getItem("token");
  return token ? children : <Navigate to="/auth" replace />;
}

// ------------------------- MAIN APP --------------------------
export default function App() {
  const role = {
    role_admin: localStorage.getItem("role_admin") === "true",
    role_owner: localStorage.getItem("role_owner") === "true",
    role_user: localStorage.getItem("role_user") === "true",
  };

  return (
    <BrowserRouter>
      <Routes>


        {/* ====================== AUTH ====================== */}
        <Route path="/auth" element={<Auth />} />

{/* ====================== User ====================== */}
        <Route
  path="/dashboard"
  element={
    <Protected>
      <DashboardLayout sidebar={<Sidebar role={role} />}>
        <UserDashboard />
      </DashboardLayout>
    </Protected>
  }
/>

        {/* ====================== OWNER DASHBOARD & TOOLS ====================== */}
        <Route
          path="/owner-dashboard"
          element={
            <Protected>
              <DashboardLayout sidebar={<OwnerSidebar />}>
                <OwnerDashboard />
              </DashboardLayout>
            </Protected>
          }
        />

        <Route
          path="/owner/tools"
          element={
            <Protected>
              <DashboardLayout sidebar={<OwnerSidebar />}>
                <MyTools />
              </DashboardLayout>
            </Protected>
          }
        />

        <Route
          path="/owner/add-tool"
          element={
            <Protected>
              <DashboardLayout sidebar={<OwnerSidebar />}>
                <AddTool />
              </DashboardLayout>
            </Protected>
          }
        />

        <Route
          path="/owner/wallet"
          element={
            <Protected>
              <DashboardLayout sidebar={<OwnerSidebar />}>
                <WalletPage />
              </DashboardLayout>
            </Protected>
          }
        />

        <Route
          path="/owner/bookings"
          element={
            <Protected>
              <DashboardLayout sidebar={<OwnerSidebar />}>
                <OwnerBookings />
              </DashboardLayout>
            </Protected>
          }
        />

        <Route
          path="/owner/onboarding/start"
          element={
            <Protected>
              <DashboardLayout sidebar={<OwnerSidebar />}>
                <OwnerOnboardingStart />
              </DashboardLayout>
            </Protected>
          }
        />

        {/* ====================== ADMIN ====================== */}
        <Route
          path="/admin"
          element={
            <Protected>
              <DashboardLayout sidebar={<Sidebar role={role} />}>
                <AdminDashboard />
              </DashboardLayout>
            </Protected>
          }
        />

        <Route
          path="/admin/owners"
          element={
            <Protected>
              <DashboardLayout sidebar={<Sidebar role={role} />}>
                <AdminOwnerList />
              </DashboardLayout>
            </Protected>
          }
        />

        <Route
          path="/admin/owners/:id"
          element={
            <Protected>
              <DashboardLayout sidebar={<Sidebar role={role} />}>
                <AdminOwnerDetail />
              </DashboardLayout>
            </Protected>
          }
        />

        {/* ====================== FALLBACK ====================== */}
        <Route path="*" element={<Navigate to="/auth" replace />} />
      </Routes>
    </BrowserRouter>
  );
}