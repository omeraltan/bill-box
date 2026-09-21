import type { AuthResponse } from "./types";

const TOKEN_KEY = "billbox.token";

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function setSession(auth: AuthResponse) {
  localStorage.setItem(TOKEN_KEY, auth.token);
  localStorage.setItem("billbox.user", JSON.stringify(auth));
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem("billbox.user");
}

export function getStoredUser(): AuthResponse | null {
  const raw = localStorage.getItem("billbox.user");
  return raw ? (JSON.parse(raw) as AuthResponse) : null;
}

export async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers);
  if (!(options.body instanceof FormData) && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }
  const token = getToken();
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }
  const response = await fetch(path, { ...options, headers });
  if (response.status === 401) {
    clearSession();
    if (!path.startsWith("/api/auth/")) {
      window.location.href = "/giris";
    }
  }
  if (!response.ok) {
    let message = "İşlem başarısız.";
    try {
      const body = await response.json();
      message = body.message ?? message;
    } catch {
      // gövde yok
    }
    throw new Error(message);
  }
  if (response.status === 204) {
    return undefined as T;
  }
  const contentType = response.headers.get("content-type") ?? "";
  if (contentType.includes("application/json")) {
    return response.json() as Promise<T>;
  }
  return response.blob() as Promise<T>;
}

export function toQuery(params: Record<string, string | number | undefined | null>) {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      search.set(key, String(value));
    }
  });
  const query = search.toString();
  return query ? `?${query}` : "";
}
