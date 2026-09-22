import { FormEvent, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { Button } from "primereact/button";
import { Password } from "primereact/password";
import { AuthShell } from "../components/AuthShell";
import { useAuth } from "../auth/AuthContext";
import { useCapsLock } from "../auth/useCapsLock";
import { api } from "../api/client";

export function ResetPasswordPage() {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const token = params.get("token") ?? "";
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const { capsLock, syncCapsLock, clearCapsLock } = useCapsLock();

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    if (!token) {
      setError("Sıfırlama bağlantısı eksik.");
      return;
    }
    if (password.length < 8) {
      setError("Şifre en az 8 karakter olmalı.");
      return;
    }
    if (password !== confirm) {
      setError("Şifreler aynı değil.");
      return;
    }
    setLoading(true);
    setError("");
    try {
      await api<void>("/api/auth/reset-password", {
        method: "POST",
        body: JSON.stringify({ token, password }),
      });
      logout();
      navigate("/giris", { replace: true, state: { passwordReset: true } });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Şifre yenilenemedi.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthShell>
      <form className="auth-form" onSubmit={submit} noValidate>
        <header>
          <p className="kicker">Yeni şifre</p>
          <h2>Şifrenizi belirleyin</h2>
          <p>Bu bağlantı bir kez kullanılır.</p>
        </header>

        {error && (
          <div className="auth-alert" role="alert">
            <i className="pi pi-exclamation-circle" aria-hidden="true" />
            <span>{error}</span>
          </div>
        )}

        <label className="auth-field" htmlFor="new-password">
          <span>Yeni şifre</span>
          <span className="auth-input">
            <i className="pi pi-lock" aria-hidden="true" />
            <Password
              inputId="new-password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              onKeyDown={syncCapsLock}
              onKeyUp={syncCapsLock}
              onMouseDown={syncCapsLock}
              onBlur={clearCapsLock}
              feedback={false}
              toggleMask
              autoComplete="new-password"
              pt={{
                showIcon: { "aria-label": "Şifreyi göster" },
                hideIcon: { "aria-label": "Şifreyi gizle" },
              }}
            />
          </span>
          <small>En az 8 karakter.</small>
          {capsLock && (
            <small className="auth-caps" role="status">
              Caps Lock açık.
            </small>
          )}
        </label>

        <label className="auth-field" htmlFor="confirm-password">
          <span>Yeni şifre tekrar</span>
          <span className="auth-input">
            <i className="pi pi-lock" aria-hidden="true" />
            <Password
              inputId="confirm-password"
              value={confirm}
              onChange={(event) => setConfirm(event.target.value)}
              onKeyDown={syncCapsLock}
              onKeyUp={syncCapsLock}
              onMouseDown={syncCapsLock}
              onBlur={clearCapsLock}
              feedback={false}
              toggleMask
              autoComplete="new-password"
              pt={{
                showIcon: { "aria-label": "Şifreyi göster" },
                hideIcon: { "aria-label": "Şifreyi gizle" },
              }}
            />
          </span>
        </label>

        <Button
          type="submit"
          label="Şifreyi kaydet"
          icon="pi pi-check"
          iconPos="right"
          className="auth-submit"
          loading={loading}
          disabled={!token}
        />

        <p className="auth-switch">
          <Link to="/giris">Girişe dön</Link>
        </p>
      </form>
    </AuthShell>
  );
}
