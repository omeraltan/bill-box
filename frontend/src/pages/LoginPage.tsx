import { FormEvent, useState } from "react";
import { Link, Navigate, useLocation, useNavigate } from "react-router-dom";
import { Button } from "primereact/button";
import { InputText } from "primereact/inputtext";
import { Password } from "primereact/password";
import { AuthShell } from "../components/AuthShell";
import { useAuth } from "../auth/AuthContext";
import { useCapsLock } from "../auth/useCapsLock";

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function LoginPage() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const passwordReset = Boolean((location.state as { passwordReset?: boolean } | null)?.passwordReset);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [emailError, setEmailError] = useState("");
  const [passwordError, setPasswordError] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const { capsLock, syncCapsLock, clearCapsLock } = useCapsLock();
  const passwordDescribedBy = [passwordError ? "password-error" : null, capsLock ? "caps-lock" : null]
    .filter(Boolean)
    .join(" ") || undefined;

  if (user) {
    return <Navigate to="/" replace />;
  }

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    const nextEmail = email.trim();
    const nextEmailError = emailPattern.test(nextEmail) ? "" : "Geçerli bir e-posta adresi girin.";
    const nextPasswordError = password ? "" : "Şifrenizi girin.";
    setEmailError(nextEmailError);
    setPasswordError(nextPasswordError);
    setError("");
    if (nextEmailError || nextPasswordError) {
      return;
    }

    setLoading(true);
    try {
      await login(nextEmail, password);
      navigate("/");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Giriş başarısız.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthShell>
      <form className="auth-form" onSubmit={submit} noValidate>
        <header>
          <p className="kicker">Hesabınıza giriş</p>
          <h2>Kasanıza dönün</h2>
          <p>Faturalar, fişler ve garanti belgeleri bu oturumda açılır.</p>
        </header>

        {passwordReset && (
          <div className="auth-success" role="status">
            <i className="pi pi-check-circle" aria-hidden="true" />
            <span>Şifreniz yenilendi. Yeni şifrenizle giriş yapın.</span>
          </div>
        )}

        {error && (
          <div className="auth-alert" role="alert">
            <i className="pi pi-exclamation-circle" aria-hidden="true" />
            <span>{error}</span>
          </div>
        )}

        <label className="auth-field" htmlFor="email">
          <span>E-posta</span>
          <span className="auth-input">
            <i className="pi pi-envelope" aria-hidden="true" />
            <InputText
              id="email"
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
              aria-describedby={emailError ? "email-error" : undefined}
              placeholder="ornek@posta.com"
            />
          </span>
          {emailError && (
            <small id="email-error" role="alert">
              {emailError}
            </small>
          )}
        </label>

        <label className="auth-field" htmlFor="password">
          <span>Şifre</span>
          <span className="auth-input">
            <i className="pi pi-lock" aria-hidden="true" />
            <Password
              inputId="password"
              value={password}
              onChange={(event) => {
                setPassword(event.target.value);
                setPasswordError("");
              }}
              onKeyDown={syncCapsLock}
              onKeyUp={syncCapsLock}
              onMouseDown={syncCapsLock}
              onBlur={clearCapsLock}
              feedback={false}
              toggleMask
              autoComplete="current-password"
              invalid={Boolean(passwordError)}
              aria-invalid={passwordError ? true : undefined}
              aria-describedby={passwordDescribedBy}
              placeholder="Şifreniz"
              pt={{
                showIcon: { "aria-label": "Şifreyi göster" },
                hideIcon: { "aria-label": "Şifreyi gizle" },
              }}
            />
          </span>
          {passwordError && (
            <small id="password-error" role="alert">
              {passwordError}
            </small>
          )}
          {capsLock && (
            <small id="caps-lock" className="auth-caps" role="status">
              Caps Lock açık.
            </small>
          )}
        </label>

        <p className="auth-forgot">
          <Link to="/sifremi-unuttum">Şifremi unuttum</Link>
        </p>

        <Button
          type="submit"
          label="Giriş yap"
          icon="pi pi-arrow-right"
          iconPos="right"
          className="auth-submit"
          loading={loading}
        />

        <p className="auth-switch">
          Hesabınız yok mu? <Link to="/kayit">Kasa oluşturun</Link>
        </p>
        <p className="auth-note">Oturum bu tarayıcıda açık kalır. Ortak bir cihazdaysanız çıkış yapın.</p>
      </form>
    </AuthShell>
  );
}
