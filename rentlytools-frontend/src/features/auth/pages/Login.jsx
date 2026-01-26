import { useState } from "react";
import api from "@/services/axios";

export default function Login() {
  const [form, setForm] = useState({ email: "", password: "" });
  const [error, setError] = useState("");

  const handleChange = (e) =>
    setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
  e.preventDefault();
  console.log("CURRENT LOGIN FILE LOADED: v10");

  try {
    const res = await api.post("/auth/login", form);
    console.log("LOGIN RESPONSE =>", res.data);

    const { token, user } = res.data;

    // Save all required values
    localStorage.setItem("token", token);
    localStorage.setItem("userId", user.id);
    localStorage.setItem("name", user.name);

    localStorage.setItem("role_admin", user.role_admin ? "true" : "false");
    localStorage.setItem("role_owner", user.role_owner ? "true" : "false");
    localStorage.setItem("role_user", user.role_user ? "true" : "false");

    // redirect to dashboard
    window.location.href = "/dashboard";
  } catch (err) {
    console.log("LOGIN ERROR", err);
    setError("Invalid email or password");
  }
};

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 px-6">
      <div className="bg-white p-8 rounded-2xl shadow-lg w-full max-w-sm">
        <h2 className="text-2xl font-bold text-gray-800 mb-6 text-center">
          Welcome Back 👋
        </h2>

        {error && <p className="text-red-500 text-sm mb-3">{error}</p>}

        <form onSubmit={handleSubmit} className="space-y-4">
          <input
            type="email"
            name="email"
            placeholder="Email"
            value={form.email}
            onChange={handleChange}
            className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-400 outline-none"
          />

          <input
            type="password"
            name="password"
            placeholder="Password"
            value={form.password}
            onChange={handleChange}
            className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-400 outline-none"
          />

          <button
            type="submit"
            className="w-full bg-blue-600 text-white py-3 rounded-lg font-semibold hover:bg-blue-700 transition"
          >
            Log In
          </button>
        </form>

        <p className="text-center text-sm text-gray-600 mt-4">
          Don’t have an account?{" "}
          <a href="/register" className="text-blue-600 font-medium">
            Register
          </a>
        </p>
      </div>
    </div>
  );
}