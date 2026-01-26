// src/pages/Auth.jsx
import React, { useEffect, useState, useRef } from "react";
import Webcam from "react-webcam";
import { toast, ToastContainer } from "react-toastify";
import { Mail, Lock, User, Loader2, ArrowLeft } from "lucide-react";
import { cva } from "class-variance-authority";
import { twMerge } from "tailwind-merge";
import api from "@/services/axios"; // <-- axios baseURL -> http://localhost:8080

import "react-toastify/dist/ReactToastify.css";

// helper for classnames
function cn(...inputs) {
  return twMerge(inputs.filter(Boolean).join(" "));
}

/* ---------------- UI primitives ---------------- */
const Card = React.forwardRef(({ className, children, ...props }, ref) => (
  <div ref={ref} className={cn("rounded-xl border bg-white text-gray-900 shadow-lg", className)} {...props}>
    {children}
  </div>
));
Card.displayName = "Card";

const CardHeader = ({ children, className }) => <div className={cn("flex flex-col space-y-1.5 p-6 relative", className)}>{children}</div>;
const CardContent = ({ children, className }) => <div className={cn("p-6 pt-0", className)}>{children}</div>;
const CardFooter = ({ children, className }) => <div className={cn("flex flex-col items-stretch gap-3 p-6 pt-0", className)}>{children}</div>;

const Label = ({ children, className, htmlFor }) => <label htmlFor={htmlFor} className={cn("text-sm font-medium mb-1 block", className)}>{children}</label>;

const Input = React.forwardRef(({ icon: Icon, className, ...props }, ref) => (
  <div className="relative">
    {Icon && <div className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"><Icon size={16} /></div>}
    <input ref={ref} {...props} className={cn("w-full rounded-md border px-3 py-2 text-sm placeholder:text-gray-400 focus:ring-2 focus:ring-blue-500 outline-none", Icon ? "pl-10" : "", className)} />
  </div>
));
Input.displayName = "Input";

const buttonVariants = cva("inline-flex items-center justify-center rounded-md text-sm font-medium transition", {
  variants: {
    variant: {
      default: "bg-blue-600 text-white hover:bg-blue-700",
      link: "text-blue-600 underline",
      ghost: "bg-transparent"
    },
    size: {
      default: "h-10 px-4",
      lg: "h-12 px-6"
    }
  },
  defaultVariants: { variant: "default", size: "default" }
});

const Button = React.forwardRef(({ children, variant, size, className, ...props }, ref) => (
  <button ref={ref} className={cn(buttonVariants({ variant, size }), className)} {...props}>{children}</button>
));
Button.displayName = "Button";

const LoadingIcon = () => <Loader2 className="animate-spin mr-2" size={16} />;

/* ---------------- Auth component ---------------- */
export default function Auth() {
  // modes: login, register-start, otp, register, forgot, forgot-otp, forgot-reset
  const [mode, setMode] = useState("login");
  const [loading, setLoading] = useState(false);

 const [form, setForm] = useState({
  name: "",
  email: "",
  password: "",
  otp: "",
  newPassword: "",
  role: "user",      // ADD THIS FIX
});

  // owner onboarding states
  const [showOwnerFields, setShowOwnerFields] = useState(false);
  const [aadhaarFile, setAadhaarFile] = useState(null);
  const [selfieImage, setSelfieImage] = useState(null);
  const [location, setLocation] = useState({ lat: null, lng: null });

  // webcam ref
  const webcamRef = useRef(null);

  // OTP/resend state
  const [otpSentFor, setOtpSentFor] = useState(null); // "register:email" or "forgot:email"
  const [resendTimer, setResendTimer] = useState(0);

  useEffect(() => {
    let t;
    if (resendTimer > 0) t = setTimeout(() => setResendTimer((s) => s - 1), 1000);
    return () => clearTimeout(t);
  }, [resendTimer]);

  const update = (e) => setForm((p) => ({ ...p, [e.target.name]: e.target.value }));

  const isEmail = (e) => Boolean(e && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(e).toLowerCase()));

  /* ---------------- API actions ---------------- */

  // LOGIN
 const login = async () => {
  if (!form.email || !form.password)
    return toast.error("Email and password required");

  if (!isEmail(form.email)) return toast.error("Enter valid email");

  setLoading(true);
  try {
    const res = await api.post("/auth/login", {
      email: form.email,
      password: form.password,
    });

    console.log("RAW LOGIN RESPONSE:", res.data);

    // Store session
    localStorage.setItem("token", res.data.token);
    localStorage.setItem("user_id", res.data.userId);
    localStorage.setItem("user_name", res.data.name);
    localStorage.setItem("role_admin", String(res.data.role_admin));
    localStorage.setItem("role_owner", String(res.data.role_owner));
    localStorage.setItem("role_user", String(res.data.role_user));

    // Debug roles
    console.log("role_admin:", res.data.role_admin);
    console.log("role_owner:", res.data.role_owner);
    console.log("role_user:", res.data.role_user);

    toast.success("Logged in");

    // 🔥 FINAL ROLE BASED REDIRECT
    if (res.data.role_admin === true || res.data.role_admin === "true") {
      window.location.href = "/admin";
      return;
    }

    if (res.data.role_owner === true || res.data.role_owner === "true") {
      window.location.href = "/owner-dashboard";
      return;
    }

    // Default: normal user
    window.location.href = "/dashboard";

  } catch (err) {
    toast.error(err?.response?.data?.message || "Login failed");
  } finally {
    setLoading(false);
  }
};
  // RESEND OTP
  const resendOtp = async () => {
    if (!otpSentFor) return toast.error("No OTP flow in progress");
    const [, email] = otpSentFor.split(":");
    if (!email) return toast.error("Invalid OTP flow");
    if (resendTimer > 0) return toast.info(`Please wait ${resendTimer}s`);
    await sendOtp(email);
  };

  // VERIFY OTP (after send)
  const verifyOtp = async () => {
    const emailToVerify = otpSentFor?.split(":")[1] || form.email;
    if (!emailToVerify || !form.otp) return toast.error("Email & OTP required");
    setLoading(true);
    try {
      await api.post("/auth/verify-otp", { email: emailToVerify, otp: form.otp });
      toast.success("OTP verified");
     setMode("register");
setForm((f) => ({ ...f, otp: "", email: emailToVerify, role: f.role || "user" }));
      setOtpSentFor(null);
      setResendTimer(0);
    } catch (err) {
      toast.error(err?.response?.data?.message || "Invalid OTP");
    } finally {
      setLoading(false);
    }
  };

  // FORGOT: send OTP (was missing)
  // SEND OTP for registration or forgot
const sendOtp = async (emailFor) => {
  const email = emailFor || form.email;
  if (!email) return toast.error("Email required");
  if (!isEmail(email)) return toast.error("Enter valid email");

  setLoading(true);

  try {
    await api.post(`/auth/send-otp?email=${encodeURIComponent(email)}`);

    toast.success("OTP sent to email");

    // helps handle resend + identify flow
    setOtpSentFor(`${mode === "forgot" ? "forgot" : "register"}:${email}`);

    setMode("otp");
    setResendTimer(30);

  } catch (err) {
    toast.error(err?.response?.data?.message || "Failed to send OTP");
  } finally {
    setLoading(false);
  }
};

  // FORGOT: verify OTP
  const forgotVerifyOtp = async () => {
    const emailToVerify = otpSentFor?.split(":")[1] || form.email;
    if (!emailToVerify || !form.otp) return toast.error("Email & OTP required");
    setLoading(true);
    try {
      await api.post("/auth/forgot-password/verify-otp", { email: emailToVerify, otp: form.otp });
      toast.success("OTP verified");
      setMode("forgot-reset");
      setForm((f) => ({ ...f, otp: "", email: emailToVerify }));
      setOtpSentFor(null);
      setResendTimer(0);
    } catch (err) {
      toast.error(err?.response?.data?.message || "Invalid OTP");
    } finally {
      setLoading(false);
    }
  };

  // RESET password (after forgot)
  const resetPassword = async () => {
    if (!form.newPassword) return toast.error("New password required");
    if (form.newPassword.length < 6) return toast.error("Password should be >= 6 chars");
    setLoading(true);
    try {
      await api.post("/auth/forgot-password/reset", { email: form.email, password: form.newPassword });
      toast.success("Password reset success");
      setMode("login");
      setForm((f) => ({ ...f, newPassword: "" }));
    } catch (err) {
      toast.error(err?.response?.data?.message || "Reset failed");
    } finally {
      setLoading(false);
    }
  };

  // helper to convert base64 selfie to File
  function dataURLtoFile(dataUrl, filename) {
    const arr = dataUrl.split(",");
    const mime = arr[0].match(/:(.*?);/)[1];
    const bstr = atob(arr[1]);
    let n = bstr.length;
    const u8arr = new Uint8Array(n);
    while (n--) u8arr[n] = bstr.charCodeAt(n);
    return new File([u8arr], filename, { type: mime });
  }

  // COMPLETE REGISTRATION (after OTP)
  const register = async () => {
    if (!form.name || !form.email || !form.password) return toast.error("All fields are required");

    setLoading(true);
    try {
      const fd = new FormData();
      fd.append("name", form.name);
      fd.append("email", form.email);
      fd.append("password", form.password);

      const isOwner = form.role === "owner";
      fd.append("isOwner", String(isOwner)); // server expects a part "isOwner"

      if (isOwner) {
        if (!aadhaarFile) {
          setLoading(false);
          return toast.error("Upload Aadhaar");
        }
        if (!selfieImage) {
          setLoading(false);
          return toast.error("Take a selfie");
        }
        if (!location.lat) {
          setLoading(false);
          return toast.error("Capture location");
        }

        fd.append("aadhaar", aadhaarFile);
        fd.append("selfie", dataURLtoFile(selfieImage, "selfie.jpg"));
        fd.append("lat", String(location.lat));
        fd.append("lng", String(location.lng));
      }

      // IMPORTANT: DO NOT set Content-Type header manually for FormData.
      // Let the browser set the proper multipart boundary.
      await api.post("/auth/register", fd);

      toast.success("Registration successful! Redirecting to login...");
      // clear owner fields
      setAadhaarFile(null);
      setSelfieImage(null);
      setLocation({ lat: null, lng: null });
      setForm({ name: "", email: "", password: "", otp: "", newPassword: "", role: "user" });
      setShowOwnerFields(false);

      // go back to login after a short delay so user sees toast
      setTimeout(() => setMode("login"), 900);
    } catch (err) {
      // server may send message in err.response.data.message or .error
      const msg = err?.response?.data?.message || err?.response?.data?.error || "Registration failed";
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  // universal submit
  const handleSubmit = (e) => {
    e?.preventDefault?.();
    if (mode === "login") login();
    else if (mode === "register-start") sendOtp();
    else if (mode === "otp") verifyOtp();
    else if (mode === "register") register();
    else if (mode === "forgot") forgotSendOtp();
    else if (mode === "forgot-otp") forgotVerifyOtp();
    else if (mode === "forgot-reset") resetPassword();
  };

  // helper to switch modes and reset small pieces
  const goMode = (m) => {
    if (m === "login") setForm((f) => ({ ...f, password: "", otp: "", newPassword: "" }));
    if (m === "register-start") setForm((f) => ({ ...f, name: "", password: "", otp: "", newPassword: "" }));
    if (m === "forgot") setForm((f) => ({ ...f, otp: "", newPassword: "" }));
    setOtpSentFor(null);
    setResendTimer(0);
    setMode(m);
  };

  /* ---------------- Views ---------------- */

  const LoginView = (
    <>
      <CardHeader>
        <h3 className="text-2xl font-semibold">Welcome back</h3>
        <p className="text-sm text-gray-600">Login to your RentlyTools account</p>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          <div>
            <Label htmlFor="email">Email</Label>
            <Input id="email" name="email" icon={Mail} placeholder="you@example.com" value={form.email} onChange={update} />
          </div>
          <div>
            <Label htmlFor="password">Password</Label>
            <Input id="password" name="password" type="password" icon={Lock} placeholder="••••••" value={form.password} onChange={update} />
          </div>
          <div className="flex justify-between items-center">
            <button type="button" className="text-sm text-blue-600" onClick={() => goMode("forgot")}>Forgot password?</button>
            <button type="button" className="text-sm text-blue-600" onClick={() => goMode("register-start")}>Register</button>
          </div>
        </div>
      </CardContent>
      <CardFooter>
        <Button onClick={handleSubmit} className="w-full">{loading ? <LoadingIcon /> : "Login"}</Button>
      </CardFooter>
    </>
  );

  const RegisterStartView = (
    <>
      <CardHeader>
        <h3 className="text-2xl font-semibold">Create an account</h3>
        <p className="text-sm text-gray-600">Enter email to receive OTP</p>
      </CardHeader>
      <CardContent>
        <div>
          <Label htmlFor="email-register">Email</Label>
          <Input id="email-register" name="email" icon={Mail} placeholder="you@example.com" value={form.email} onChange={update} />
        </div>
      </CardContent>
      <CardFooter>
        <Button onClick={handleSubmit} className="w-full">{loading ? <LoadingIcon /> : "Send OTP"}</Button>
        <Button variant="link" onClick={() => goMode("login")}>Back to login</Button>
      </CardFooter>
    </>
  );

  const OtpView = (
    <>
      <CardHeader>
        <button type="button" className="absolute left-4 top-4 text-gray-500" onClick={() => goMode(otpSentFor?.startsWith("forgot") ? "forgot" : "register-start")}><ArrowLeft /></button>
        <h3 className="text-2xl font-semibold pt-6">Verify OTP</h3>
        <p className="text-sm text-gray-600">We sent an OTP to <strong>{(otpSentFor?.split(":")[1]) || form.email}</strong></p>
      </CardHeader>
      <CardContent>
        <div>
          <Label htmlFor="otp">OTP</Label>
          <Input id="otp" name="otp" placeholder="123456" value={form.otp} onChange={update} />
        </div>
        <div className="mt-3">
          <button type="button" className="text-sm text-blue-600" disabled={resendTimer > 0 || loading} onClick={resendOtp}>
            {resendTimer > 0 ? `Resend in ${resendTimer}s` : "Resend OTP"}
          </button>
        </div>
      </CardContent>
      <CardFooter>
        <Button onClick={handleSubmit} className="w-full">{loading ? <LoadingIcon /> : "Verify OTP"}</Button>
      </CardFooter>
    </>
  );

  const RegisterCompleteView = (
    <>
      <CardHeader>
        <h3 className="text-2xl font-semibold">Complete registration</h3>
        <p className="text-sm text-gray-600">Email verified: <strong>{form.email}</strong></p>
      </CardHeader>

      <CardContent>
        <div className="space-y-4">
          {/* NAME */}
          <div>
            <Label htmlFor="name">Full name</Label>
            <Input id="name" name="name" icon={User} placeholder="John Doe" value={form.name} onChange={update} />
          </div>

          {/* PASSWORD */}
          <div>
            <Label htmlFor="password-reg">Password</Label>
            <Input id="password-reg" name="password" type="password" icon={Lock} placeholder="Choose a password" value={form.password} onChange={update} />
          </div>

          {/* ACCOUNT TYPE */}
          <div>
            <Label htmlFor="role">Account Type</Label>
            <select id="role" name="role" value={form.role} onChange={(e) => { update(e); setShowOwnerFields(e.target.value === "owner"); }} className="w-full p-2 border rounded-md text-sm">
              <option value="user">User</option>
              <option value="owner">Owner</option>
            </select>
          </div>

          {/* OWNER EXTRA FIELDS */}
          {showOwnerFields && (
            <div className="p-3 border rounded-lg bg-gray-50">
              {/* Aadhaar Upload */}
              <div className="mb-3">
                <Label>Aadhaar Card (Front)</Label>
                <input type="file" accept="image/*" onChange={(e) => setAadhaarFile(e.target.files[0])} className="text-sm" />
              </div>

              {/* Webcam Selfie */}
              <div className="mb-3">
                <Label>Selfie Verification</Label>

                {!selfieImage ? (
                  <div>
                    <Webcam ref={webcamRef} width={250} height={250} screenshotFormat="image/jpeg" className="rounded-md border" />
                    <div className="mt-2 flex gap-2">
                      <button type="button" onClick={() => { const img = webcamRef.current?.getScreenshot(); if (img) setSelfieImage(img); }} className="px-3 py-1 bg-blue-600 text-white rounded-md">Capture Selfie</button>
                      <button type="button" onClick={() => { /* request permission again if needed */ navigator.mediaDevices?.getUserMedia?.({ video: true }).catch(()=>{}); }} className="px-3 py-1 bg-gray-200 rounded-md">Enable Camera</button>
                    </div>
                  </div>
                ) : (
                  <div>
                    <img src={selfieImage} alt="selfie" className="w-32 rounded-md" />
                    <div className="mt-2 flex gap-2">
                      <button type="button" onClick={() => setSelfieImage(null)} className="px-3 py-1 bg-red-600 text-white rounded-md">Retake</button>
                    </div>
                  </div>
                )}
              </div>

              {/* Location */}
              <div>
                <Label>Location (GPS)</Label>
                <div className="flex items-center gap-2">
                  <button type="button" onClick={() => navigator.geolocation.getCurrentPosition((pos) => setLocation({ lat: pos.coords.latitude, lng: pos.coords.longitude }), (err) => toast.error("Location permission denied"))} className="px-3 py-1 bg-green-600 text-white rounded-md">Capture Location</button>
                  {location.lat && <p className="text-xs mt-1">📍 {location.lat.toFixed(6)}, {location.lng.toFixed(6)}</p>}
                </div>
              </div>
            </div>
          )}
        </div>
      </CardContent>

      <CardFooter>
        <Button onClick={handleSubmit} className="w-full">{loading ? <LoadingIcon /> : "Register"}</Button>
      </CardFooter>
    </>
  );

  const ForgotView = (
    <>
      <CardHeader>
        <button type="button" className="absolute left-4 top-4 text-gray-500" onClick={() => goMode("login")}><ArrowLeft /></button>
        <h3 className="text-2xl font-semibold pt-6">Forgot password</h3>
        <p className="text-sm text-gray-600">Enter your email to receive a reset OTP</p>
      </CardHeader>
      <CardContent>
        <div>
          <Label htmlFor="forgot-email">Email</Label>
          <Input id="forgot-email" name="email" icon={Mail} placeholder="you@example.com" value={form.email} onChange={update} />
        </div>
      </CardContent>
      <CardFooter>
        <Button onClick={handleSubmit} className="w-full">{loading ? <LoadingIcon /> : "Send OTP"}</Button>
      </CardFooter>
    </>
  );

  const ForgotResetView = (
    <>
      <CardHeader>
        <h3 className="text-2xl font-semibold">Reset password</h3>
        <p className="text-sm text-gray-600">Set new password for <strong>{form.email}</strong></p>
      </CardHeader>
      <CardContent>
        <div>
          <Label htmlFor="newPassword">New password</Label>
          <Input id="newPassword" name="newPassword" type="password" icon={Lock} placeholder="New password" value={form.newPassword} onChange={update} />
        </div>
      </CardContent>
      <CardFooter>
        <Button onClick={handleSubmit} className="w-full">{loading ? <LoadingIcon /> : "Reset password"}</Button>
      </CardFooter>
    </>
  );

  const currentView = () => {
    switch (mode) {
      case "login": return LoginView;
      case "register-start": return RegisterStartView;
      case "otp": return OtpView;
      case "register": return RegisterCompleteView;
      case "forgot": return ForgotView;
      case "forgot-otp": return OtpView;
      case "forgot-reset": return ForgotResetView;
      default: return LoginView;
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100 p-4">
      <div className="w-full max-w-md">
        <Card>{currentView()}</Card>
      </div>

      <ToastContainer position="top-right" autoClose={3000} hideProgressBar={false} newestOnTop closeOnClick pauseOnHover />
    </div>
  );
}