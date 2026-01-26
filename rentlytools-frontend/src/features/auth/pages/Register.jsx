import { useState } from "react";
import api from "@/services/axios";

export default function Register() {
  const [form, setForm] = useState({
    name: "",
    email: "",
    password: "",
    roleAdmin: false,
    roleOwner: false,
    roleUser: true,   // default normal user
  });

  const [error, setError] = useState("");

  const handleChange = (e) =>
    setForm({ ...form, [e.target.name]: e.target.value });

  const handleRoleChange = (e) =>
    setForm({ ...form, [e.target.name]: e.target.checked });

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const res = await api.post("/auth/register", form);

      const { token, userId, role_admin, role_owner, role_user } = res.data;

      // save token + roles
      localStorage.setItem("token", token);
      localStorage.setItem("userId", userId);
      localStorage.setItem("role_admin", role_admin);
      localStorage.setItem("role_owner", role_owner);
      localStorage.setItem("role_user", role_user);

      window.location.href = "/dashboard";
    } catch (err) {
      setError("Registration failed — try again");
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 px-6">
      <div className="bg-white p-8 rounded-2xl shadow-lg w-full max-w-sm">
        <h2 className="text-2xl font-bold text-gray-800 mb-6 text-center">
          Create Account 🚀
        </h2>

        {error && <p className="text-red-500 text-sm mb-3">{error}</p>}

        <form onSubmit={handleSubmit} className="space-y-4">
          <input
            type="text"
            name="name"
            placeholder="Full Name"
            value={form.name}
            onChange={handleChange}
            className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-green-400 outline-none"
          />

          <input
            type="email"
            name="email"
            placeholder="Email"
            value={form.email}
            onChange={handleChange}
            className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-green-400 outline-none"
          />

          <input
            type="password"
            name="password"
            placeholder="Password"
            value={form.password}
            onChange={handleChange}
            className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-green-400 outline-none"
          />

          {/* ROLE SELECTION */}
          <div className="border p-3 rounded-lg">
            <p className="text-sm font-medium mb-2">Select Role:</p>

            <label className="flex items-center gap-2 mb-2">
              <input
                type="checkbox"
                name="roleAdmin"
                checked={form.roleAdmin}
                onChange={handleRoleChange}
              />
              <span>Admin</span>
            </label>

            <label className="flex items-center gap-2 mb-2">
              <input
                type="checkbox"
                name="roleOwner"
                checked={form.roleOwner}
                onChange={handleRoleChange}
              />
              <span>Owner</span>
            </label>

            <label className="flex items-center gap-2">
              <input
                type="checkbox"
                name="roleUser"
                checked={form.roleUser}
                onChange={handleRoleChange}
              />
              <span>User</span>
            </label>
          </div>

          <button
            type="submit"
            className="w-full bg-green-600 text-white py-3 rounded-lg font-semibold hover:bg-green-700 transition"
          >
            Register
          </button>
        </form>

        <p className="text-center text-sm text-gray-600 mt-4">
          Already have an account?{" "}
          <a href="/login" className="text-green-600 font-medium">
            Login
          </a>
        </p>
      </div>
    </div>
  );
}