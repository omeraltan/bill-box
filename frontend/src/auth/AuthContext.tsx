import { createContext, useContext, useMemo, useState, type ReactNode } from "react";
import { api, clearSession, getStoredUser, setSession } from "../api/client";
import type { AuthResponse } from "../api/types";

interface AuthContextValue {
  user: AuthResponse | null;
  login: (email: string, password: string) => Promise<void>;
  register: (payload: { fullName: string; email: string; password: string; organizationName?: string }) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthResponse | null>(getStoredUser());

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      async login(email, password) {
        const auth = await api<AuthResponse>("/api/auth/login", {
          method: "POST",
          body: JSON.stringify({ email, password }),
        });
        setSession(auth);
        setUser(auth);
      },
      async register(payload) {
        const auth = await api<AuthResponse>("/api/auth/register", {
          method: "POST",
          body: JSON.stringify(payload),
        });
        setSession(auth);
        setUser(auth);
      },
      logout() {
        clearSession();
        setUser(null);
      },
    }),
    [user]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth AuthProvider içinde kullanılmalı.");
  }
  return context;
}
