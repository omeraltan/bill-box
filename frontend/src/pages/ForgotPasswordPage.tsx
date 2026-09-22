import { FormEvent, useState } from "react";
import { Link, Navigate } from "react-router-dom";
import { Button } from "primereact/button";
import { InputText } from "primereact/inputtext";
import { AuthShell } from "../components/AuthShell";
import { useAuth } from "../auth/AuthContext";
import { api } from "../api/client";

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function ForgotPasswordPage() {
  const { user } = useAuth();
  const [email, setEmail] = useState("");
  const [emailError, setEmailError] = useState("");
  const [error, setError] = useState("");
  const [sent, setSent] = useState(false);
  const [loading, setLoading] = useState(false);

  if (user) {
    return <Navigate to="/" replace />;
  }

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    const nextEmail = email.trim();
    if (!emailPattern.test(nextEmail)) {
      setEmailError("Geçerli bir e-posta adresi girin.");
      return;
    }
    setLoading(true);
    setError("");
    setEmailError("");
    try {
      await api<{ message: string }>("/api/auth/forgot-password", {
        method: "POST",
        body: JSON.stringify({ email: nextEmail }),
      });
      setSent(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : "İleti gönderilemedi.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthShell>
      <form className="auth-form" onSubmit={submit} noValidate>
        <header>
          <p className="kicker">Şifre yenileme</p>
          <h2>Şifrenizi sıfırlayın</h2>
          <p>Kayıtlı adresinize 30 dakika geçerli bir bağlantı gönderilir.</p>
        </header>

        {error && (
          <div className="auth-alert" role="alert">
            <i className="pi pi-exclamation-circle" aria-hidden="true" />
            <span>{error}</span>
          </div>
        )}

        {sent ? (
          <div className="auth-success" role="status">
            <i className="pi pi-envelope" aria-hidden="true" />
            <span>
              Kayıtlı bir hesapsa bağlantı gönderildi. Bu kurulumda iletiler{" "}
              <a href="http://localhost:8025" target="_blank" rel="noreferrer">
                Mailpit gelen kutusunda
              </a>{" "}
              görünür.
            </span>
          </div>
        ) : (
          <label className="auth-field" htmlFor="forgot-email">
            <span>E-posta</span>
            <span className="auth-input">
              <i className="pi pi-envelope" aria-hidden="true" />
              <InputText
                id="forgot-email"
                type="email"
                inputMode="email"
                autoComplete="email"
                value={email}
                onChange={(event) => {
                  setEmail(event.target.value);
                  setEmailError("");
                }}
                invalid={Boolean(emailError)}
                aria-invalid={emailError ? true : undefined}
                aria-describedby={emailError ? "forgot-email-error" : undefined}
                placeholder="ornek@posta.com"
              />
            </span>
            {emailError && (
              <small id="forgot-email-error" role="alert">
                {emailError}
              </small>
            )}
          </label>
        )}

        {!sent && (
          <Button
            type="submit"
            label="Bağlantı gönder"
            icon="pi pi-envelope"
            iconPos="right"
            className="auth-submit"
            loading={loading}
          />
        )}

        <p className="auth-switch">
          <Link to="/giris">Girişe dön</Link>
        </p>
      </form>
    </AuthShell>
  );
}
