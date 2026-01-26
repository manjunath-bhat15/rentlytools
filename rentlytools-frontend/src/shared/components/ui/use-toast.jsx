import * as React from "react"

const ToastContext = React.createContext({
  toast: () => {},
})

export function useToast() {
  return React.useContext(ToastContext)
}

export function ToastProvider({ children }) {
  const [toasts, setToasts] = React.useState([])

  const toast = ({ title, description, variant }) => {
    const id = Date.now()
    setToasts((prev) => [
      ...prev,
      { id, title, description, variant }
    ])
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id))
    }, 3000)
  }

  return (
    <ToastContext.Provider value={{ toast }}>
      {children}

      {/* Renders the Toaster */}
      <div className="fixed top-5 right-5 space-y-3 z-50">
        {toasts.map((t) => (
          <div
            key={t.id}
            className="px-4 py-3 bg-white shadow-lg rounded-xl border animate-in fade-in slide-in-from-right-4 duration-300"
          >
            <div className="font-semibold">{t.title}</div>
            {t.description && (
              <div className="text-sm text-gray-600">{t.description}</div>
            )}
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  )
}