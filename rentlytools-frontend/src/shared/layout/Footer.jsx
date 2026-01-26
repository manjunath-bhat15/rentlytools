// src/shared/layout/Footer.jsx
export default function Footer() {
  return (
    <footer className="h-12 border-t text-sm flex items-center justify-center text-gray-500">
      © {new Date().getFullYear()} RentlyTools
    </footer>
  );
}
