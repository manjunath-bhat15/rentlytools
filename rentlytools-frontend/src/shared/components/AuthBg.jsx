export default function AuthBg() {
  return (
    <div className="absolute inset-0 overflow-hidden -z-10">
      <div className="absolute w-[600px] h-[600px] bg-purple-500/40 blur-[150px] rounded-full top-[-100px] left-[-150px] animate-pulse"></div>
      <div className="absolute w-[600px] h-[600px] bg-blue-500/40 blur-[150px] rounded-full bottom-[-150px] right-[-150px] animate-pulse delay-700"></div>
    </div>
  );
}