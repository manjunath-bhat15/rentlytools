import { useState } from "react";
import { motion } from "framer-motion";
import { Mail } from "lucide-react";
import AuthBg from "../components/AuthBg";

export default function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [sent, setSent] = useState(false);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-600 via-indigo-600 to-purple-600 relative p-4">
      <AuthBg />

      <motion.div
        initial={{ opacity: 0, y: 40 }}
        animate={{ opacity: 1, y: 0 }}
        className="w-full max-w-md bg-white/10 backdrop-blur-xl p-10 rounded-3xl shadow-2xl border border-white/20"
      >
        <h2 className="text-3xl text-white font-bold text-center mb-6">
          Forgot Password?
        </h2>

        <p className="text-white/70 text-center mb-8 text-sm">
          Enter your email and we’ll send a reset link.
        </p>

        {!sent ? (
          <form
            onSubmit={(e) => {
              e.preventDefault();
              setSent(true);
            }}
            className="space-y-6"
          >
            <div className="relative">
              <Mail className="absolute left-3 top-3 text-white/60" size={18} />
              <input
                type="email"
                required
                placeholder="Your email"
                className="w-full pl-10 bg-white/10 border border-white/30 text-white rounded-xl p-3 focus:ring-2 focus:ring-indigo-300 outline-none"
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>

            <motion.button
              whileTap={{ scale: 0.97 }}
              className="w-full bg-white/20 hover:bg-white/30 text-white py-3 rounded-xl font-semibold shadow-lg"
            >
              Send Reset Link
            </motion.button>
          </form>
        ) : (
          <p className="text-white text-center text-lg">
            ✔ Reset link sent to <strong>{email}</strong>
          </p>
        )}

        <p
          onClick={() => (window.location.href = "/auth")}
          className="mt-6 text-center text-white/70 cursor-pointer hover:underline"
        >
          Back to Login
        </p>
      </motion.div>
    </div>
  );
}