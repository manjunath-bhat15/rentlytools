import React, { useState, useRef } from "react";
import Webcam from "react-webcam";
import api from "@/services/axios";
import { toast } from "react-toastify";

export default function OwnerOnboardingStart() {
  const webcamRef = useRef(null);

  const [aadhaarFile, setAadhaarFile] = useState(null);
  const [selfieImage, setSelfieImage] = useState(null);
  const [location, setLocation] = useState({ lat: null, lng: null });
  const [submitting, setSubmitting] = useState(false);

  // Convert base64 → File
  const dataURLtoFile = (dataUrl, filename) => {
    const arr = dataUrl.split(",");
    const mime = arr[0].match(/:(.*?);/)[1];
    const bstr = atob(arr[1]);
    let n = bstr.length;
    const u8arr = new Uint8Array(n);

    while (n--) u8arr[n] = bstr.charCodeAt(n);
    return new File([u8arr], filename, { type: mime });
  };

  // Capture selfie
  const captureSelfie = () => {
    const imgSrc = webcamRef.current.getScreenshot();
    setSelfieImage(imgSrc);
  };

  // GPS location
  const fetchLocation = () => {
    navigator.geolocation.getCurrentPosition(
      (pos) =>
        setLocation({
          lat: pos.coords.latitude,
          lng: pos.coords.longitude,
        }),
      () => toast.error("Enable location access!")
    );
  };

  // Submit to backend
  const handleSubmit = async () => {
    if (!aadhaarFile || !selfieImage) {
      toast.error("Upload Aadhaar & capture selfie first!");
      return;
    }

    const userId = localStorage.getItem("userId");
    if (!userId) return toast.error("Login required");

    if (!location.lat || !location.lng) {
      return toast.error("Please capture location");
    }

    setSubmitting(true);

    try {
      const formData = new FormData();

      formData.append("userId", userId);
      formData.append("aadhaar", aadhaarFile);

      // VERY IMPORTANT — convert base64 → actual file
      const selfieFile = dataURLtoFile(selfieImage, "selfie.jpg");
      formData.append("selfie", selfieFile);

      formData.append("lat", location.lat);
      formData.append("lng", location.lng);

      // ✔ correct endpoint
      const res = await api.post("/owner/verify/start", formData);

      toast.success("Verification submitted!");
      window.location.href = "/owner-dashboard";
    } catch (err) {
      console.error(err);
      toast.error("Error submitting verification");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="p-6 max-w-3xl mx-auto text-white">
      <h2 className="text-3xl font-bold mb-4">Become an Owner</h2>

      {/* Aadhaar Upload */}
      <div className="mb-6">
        <label className="block text-sm mb-2">Upload Aadhaar (Front)</label>
        <input
          type="file"
          accept="image/*"
          onChange={(e) => setAadhaarFile(e.target.files[0])}
          className="text-black"
        />
      </div>

      {/* Webcam Selfie */}
      <div className="mb-6">
        <label className="block text-sm mb-2">Capture Selfie</label>

        {!selfieImage ? (
          <div>
            <Webcam
              ref={webcamRef}
              width={300}
              height={300}
              screenshotFormat="image/jpeg"
              className="rounded-xl"
            />
            <button
              onClick={captureSelfie}
              className="mt-3 px-4 py-2 bg-blue-600 rounded-lg"
            >
              Capture Selfie
            </button>
          </div>
        ) : (
          <div>
            <img src={selfieImage} alt="Selfie" className="w-48 rounded-lg" />
            <button
              onClick={() => setSelfieImage(null)}
              className="mt-3 px-4 py-2 bg-red-600 rounded-lg"
            >
              Retake
            </button>
          </div>
        )}
      </div>

      {/* Location */}
      <div className="mb-6">
        <label className="block text-sm mb-2">Location</label>

        <button
          onClick={fetchLocation}
          className="px-4 py-2 bg-green-600 rounded-lg"
        >
          Get My Location
        </button>

        {location.lat && (
          <p className="mt-2 text-sm">
            📍 {location.lat}, {location.lng}
          </p>
        )}
      </div>

      {/* Submit */}
      <button
        onClick={handleSubmit}
        disabled={submitting}
        className="px-6 py-2 bg-purple-600 rounded-lg w-full mt-4 disabled:bg-gray-500"
      >
        {submitting ? "Submitting..." : "Submit Verification"}
      </button>
    </div>
  );
}