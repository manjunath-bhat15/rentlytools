import { useRef, useState } from "react";

export default function OtpInput({ onSubmit }) {
  const inputs = useRef([]);
  const [otp, setOtp] = useState(["", "", "", "", "", ""]);

  const handleChange = (value, index) => {
    if (!/^\d?$/.test(value)) return;

    const newOtp = [...otp];
    newOtp[index] = value;
    setOtp(newOtp);

    if (value && index < 5) {
      inputs.current[index + 1].focus();
    }

    if (newOtp.join("").length === 6) {
      onSubmit(newOtp.join(""));
    }
  };

  const handlePaste = (e) => {
    const text = e.clipboardData.getData("text");
    if (/^\d{6}$/.test(text)) {
      const digits = text.split("");
      setOtp(digits);
      digits.forEach((d, i) => (inputs.current[i].value = d));
      onSubmit(text);
    }
  };

  return (
    <div className="flex gap-2" onPaste={handlePaste}>
      {otp.map((digit, i) => (
        <input
          key={i}
          maxLength="1"
          ref={(el) => (inputs.current[i] = el)}
          className="w-12 h-12 text-center border rounded-lg text-xl"
          onChange={(e) => handleChange(e.target.value, i)}
        />
      ))}
    </div>
  );
}